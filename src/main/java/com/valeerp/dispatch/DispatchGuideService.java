package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.kernel.api.PageResponse;
import com.valeerp.sales.SaleOrderRepository;
import com.valeerp.store.service.repository.SaleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class DispatchGuideService {

    private final DispatchGuideRepository dispatchGuideRepository;
    private final SaleRepository saleRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final CarrierService carrierService;
    private final CarrierDesk carrierDesk;
    private final TimeProvider timeProvider;

    public DispatchGuideService(DispatchGuideRepository dispatchGuideRepository,
                                SaleRepository saleRepository,
                                SaleOrderRepository saleOrderRepository,
                                CarrierService carrierService,
                                CarrierDesk carrierDesk,
                                TimeProvider timeProvider) {
        this.dispatchGuideRepository = dispatchGuideRepository;
        this.saleRepository = saleRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.carrierService = carrierService;
        this.carrierDesk = carrierDesk;
        this.timeProvider = timeProvider;
    }

    @Transactional(readOnly = true)
    public PageResponse<DispatchGuideResponse> list(String status, String assignmentStatus, String query,
                                                    boolean assignable, Pageable pageable) {
        DispatchGuideStatus parsedStatus = assignable ? DispatchGuideStatus.ISSUED : parseStatus(status);
        DeliveryAssignmentStatus parsedAssignment = parseAssignment(assignmentStatus);
        String q = query == null || query.isBlank() ? null : query.trim();
        return PageResponse.from(
                dispatchGuideRepository
                        .findAll(DispatchGuideSearch.matching(parsedStatus, parsedAssignment, q), page(pageable))
                        .map(DispatchGuideResponse::summary)
        );
    }

    @Transactional(readOnly = true)
    public DispatchGuideResponse get(Long id) {
        return DispatchGuideResponse.detail(require(id));
    }

    @Transactional
    public DispatchGuideResponse create(DispatchGuideRequests.CreateDispatchGuideRequest request) {
        if (request == null) {
            throw new DomainException("La guía es obligatoria");
        }
        requireReferences(request.saleId(), request.saleOrderId());
        DispatchGuide guide = DispatchGuide.draft(
                request.senderName(),
                request.recipientName(),
                request.recipientDocument(),
                parseReason(request.transferReason()),
                request.originAddress(),
                request.destinationAddress(),
                request.saleId(),
                request.saleOrderId(),
                request.notes(),
                timeProvider.now()
        );
        List<DispatchGuideRequests.LineRequest> lines = request.lines() == null ? List.of() : request.lines();
        for (DispatchGuideRequests.LineRequest line : lines) {
            if (line == null) {
                throw new DomainException("La línea de la guía es obligatoria");
            }
            guide.addLine(line.productId(), line.productName(), requireQuantity(line.quantity()));
        }
        return DispatchGuideResponse.detail(dispatchGuideRepository.save(guide));
    }

    @Transactional
    public DispatchGuideResponse issue(Long id) {
        DispatchGuide guide = require(id);
        guide.issue(DispatchGuidePolicy.GUIDE_SERIES, nextNumber(), timeProvider.now());
        return DispatchGuideResponse.detail(dispatchGuideRepository.save(guide));
    }

    @Transactional
    public DispatchGuideResponse voidGuide(Long id, DispatchGuideRequests.VoidDispatchGuideRequest request) {
        if (request == null) {
            throw new DomainException("El motivo de anulación es obligatorio");
        }
        DispatchGuide guide = require(id);
        guide.voidGuide(request.reason(), timeProvider.now());
        return DispatchGuideResponse.detail(dispatchGuideRepository.save(guide));
    }

    @Transactional
    public DispatchGuideResponse assignDelivery(Long id, DispatchGuideRequests.AssignDeliveryRequest request) {
        if (request == null || request.carrierId() == null) {
            throw new DomainException("La asignación exige un transportista");
        }
        Carrier carrier = carrierService.require(request.carrierId());
        DeliveryScope scope = request.scope() == null || request.scope().isBlank()
                ? DeliveryScope.TOTAL
                : DeliveryScope.parse(request.scope());
        DispatchGuide guide = require(id);
        guide.assignDelivery(
                carrier.getId(),
                carrier.getDisplayName(),
                carrier.getCode(),
                scope,
                request.medium(),
                timeProvider.now()
        );
        DispatchGuide saved = dispatchGuideRepository.save(guide);
        openCarrierRoute(saved, scope);
        return DispatchGuideResponse.detail(saved);
    }

    private void openCarrierRoute(DispatchGuide guide, DeliveryScope scope) {
        if (guide.getSaleOrderId() == null) {
            return;
        }
        carrierDesk.accept(new AssignmentNotice(
                guide.getAssignmentRef(),
                OrderRefKind.SALE_ORDER,
                guide.getSaleOrderId(),
                guide.getCarrierId(),
                scope,
                fit(guide.documentNumber() + " " + guide.getRecipientName(), AssignmentNotice.SUMMARY_MAX),
                fit(guide.getDestinationAddress(), AssignmentNotice.SUMMARY_MAX),
                null,
                null
        ));
    }

    private static String fit(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private DispatchGuide require(Long id) {
        if (id == null) {
            throw new DomainException("La guía es obligatoria");
        }
        return dispatchGuideRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Guía de remisión no encontrada"));
    }

    private void requireReferences(Long saleId, Long saleOrderId) {
        if (saleId != null && !saleRepository.existsById(saleId)) {
            throw new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "La venta referenciada no existe");
        }
        if (saleOrderId != null && !saleOrderRepository.existsById(saleOrderId)) {
            throw new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "El pedido referenciado no existe");
        }
    }

    private String nextNumber() {
        long next = dispatchGuideRepository.findMaxNumber(DispatchGuidePolicy.GUIDE_SERIES)
                .map(DispatchGuideService::parseDocumentNumber)
                .orElse(0L) + 1L;
        return String.format("%0" + DispatchGuidePolicy.GUIDE_NUMBER_WIDTH + "d", next);
    }

    private Pageable page(Pageable pageable) {
        int size = pageable.getPageSize();
        if (size < 1) {
            size = DispatchGuidePolicy.DEFAULT_PAGE_SIZE;
        }
        if (size > DispatchGuidePolicy.MAX_PAGE_SIZE) {
            size = DispatchGuidePolicy.MAX_PAGE_SIZE;
        }
        return PageRequest.of(Math.max(pageable.getPageNumber(), 0), size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private static DispatchGuideStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return DispatchGuideStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de guía no soportado");
        }
    }

    private static DeliveryAssignmentStatus parseAssignment(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return DeliveryAssignmentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de asignación no soportado");
        }
    }

    private static TransferReason parseReason(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El motivo de traslado es obligatorio");
        }
        try {
            return TransferReason.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Motivo de traslado no soportado");
        }
    }

    private static int requireQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("La cantidad debe ser mayor que cero");
        }
        return quantity;
    }

    private static long parseDocumentNumber(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
