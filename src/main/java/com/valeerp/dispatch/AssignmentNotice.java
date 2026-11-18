package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Contrato de acople: la asignación (guía u otro documento) avisa al escritorio
 * del transportista. No transporta líneas ni el documento de la guía.
 */
public record AssignmentNotice(
        String assignmentRef,
        OrderRefKind orderKind,
        Long orderId,
        Long carrierId,
        DeliveryScope scope,
        String summary,
        String destinationLabel,
        BigDecimal latitude,
        BigDecimal longitude
) {

    public static final int REF_MAX = 80;
    public static final int SUMMARY_MAX = 240;

    public AssignmentNotice {
        if (assignmentRef == null || assignmentRef.isBlank()) {
            throw new DomainException("La asignación exige una referencia");
        }
        assignmentRef = assignmentRef.trim();
        if (assignmentRef.length() > REF_MAX) {
            throw new DomainException("La referencia de asignación excede la longitud permitida");
        }
        if (orderKind == null || orderId == null) {
            throw new DomainException("La asignación exige el pedido");
        }
        if (carrierId == null) {
            throw new DomainException("La asignación exige un transportista");
        }
        if (scope == null) {
            throw new DomainException("La asignación exige el alcance de la entrega");
        }
        summary = blankToNull(summary);
        if (summary != null && summary.length() > SUMMARY_MAX) {
            throw new DomainException("El resumen de la entrega excede la longitud permitida");
        }
        destinationLabel = blankToNull(destinationLabel);
    }

    /**
     * Proyección de una guía ya asignada. No copia sus líneas como otro documento.
     */
    public static AssignmentNotice fromGuide(DispatchGuide guide) {
        if (guide.getAssignmentRef() == null || guide.getCarrierId() == null || guide.getDeliveryScope() == null) {
            throw new DomainException("La guía no tiene una asignación de delivery");
        }
        if (guide.getSaleOrderId() != null) {
            return new AssignmentNotice(
                    guide.getAssignmentRef(),
                    OrderRefKind.SALE_ORDER,
                    guide.getSaleOrderId(),
                    guide.getCarrierId(),
                    guide.getDeliveryScope(),
                    summary(guide),
                    guide.getDestinationAddress(),
                    null,
                    null);
        }
        return new AssignmentNotice(
                guide.getAssignmentRef(),
                OrderRefKind.DISPATCH_GUIDE,
                guide.getId(),
                guide.getCarrierId(),
                guide.getDeliveryScope(),
                summary(guide),
                guide.getDestinationAddress(),
                null,
                null);
    }

    private static String summary(DispatchGuide guide) {
        String products = guide.getLines().stream()
                .map(line -> line.getProductName() + " x" + line.getQuantity())
                .collect(Collectors.joining(", "));
        String head = guide.documentNumber() == null ? "Guía " + guide.getId() : guide.documentNumber();
        String text = products.isBlank() ? head : head + " · " + products;
        return text.length() <= SUMMARY_MAX ? text : text.substring(0, SUMMARY_MAX);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
