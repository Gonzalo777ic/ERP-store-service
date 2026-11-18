package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Collections;
import java.util.List;

/**
 * Guía de remisión. La emisión es la transición de borrador a emitida.
 * No descuenta stock. La anulación deja el documento consultable.
 * El delivery se registra aquí: nombre o referencia, sin un segundo contexto.
 */
@Entity
@Table(name = "dispatch_guides", uniqueConstraints = @UniqueConstraint(columnNames = {"series", "number"}))
public class DispatchGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = DispatchGuidePolicy.SERIES_MAX_LENGTH)
    private String series;

    @Column(length = DispatchGuidePolicy.NUMBER_MAX_LENGTH)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private DispatchGuideStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_reason", nullable = false, length = 32)
    private TransferReason transferReason;

    @Column(name = "sender_name", nullable = false, length = DispatchGuidePolicy.PARTY_NAME_MAX_LENGTH)
    private String senderName;

    @Column(name = "recipient_name", nullable = false, length = DispatchGuidePolicy.PARTY_NAME_MAX_LENGTH)
    private String recipientName;

    @Column(name = "recipient_document", length = DispatchGuidePolicy.DOCUMENT_MAX_LENGTH)
    private String recipientDocument;

    @Column(name = "origin_address", nullable = false, length = DispatchGuidePolicy.ADDRESS_MAX_LENGTH)
    private String originAddress;

    @Column(name = "destination_address", nullable = false, length = DispatchGuidePolicy.ADDRESS_MAX_LENGTH)
    private String destinationAddress;

    @Column(name = "sale_id")
    private Long saleId;

    @Column(name = "sale_order_id")
    private Long saleOrderId;

    @Column(length = DispatchGuidePolicy.NOTE_MAX_LENGTH)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "void_reason", length = DispatchGuidePolicy.VOID_REASON_MAX_LENGTH)
    private String voidReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false, length = 24)
    private DeliveryAssignmentStatus assignmentStatus;

    @Column(name = "carrier_id")
    private Long carrierId;

    @Column(name = "carrier_name", length = Carrier.NAME_MAX)
    private String carrierName;

    @Column(name = "carrier_code", length = Carrier.CODE_MAX)
    private String carrierCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_scope", length = 16)
    private DeliveryScope deliveryScope;

    @Column(name = "assignment_ref", length = AssignmentNotice.REF_MAX)
    private String assignmentRef;

    @Column(length = DispatchGuidePolicy.MEDIUM_MAX_LENGTH)
    private String medium;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DispatchGuideLine> lines = new ArrayList<>();

    protected DispatchGuide() {
    }

    public static DispatchGuide draft(String senderName, String recipientName, String recipientDocument,
                                      TransferReason transferReason, String originAddress, String destinationAddress,
                                      Long saleId, Long saleOrderId, String notes, Instant createdAt) {
        if (transferReason == null) {
            throw new DomainException("El motivo de traslado es obligatorio");
        }
        if (createdAt == null) {
            throw new DomainException("La fecha de la guía es obligatoria");
        }
        if (saleId != null && saleId <= 0) {
            throw new DomainException("La venta referenciada no es válida");
        }
        if (saleOrderId != null && saleOrderId <= 0) {
            throw new DomainException("El pedido referenciado no es válido");
        }
        DispatchGuide guide = new DispatchGuide();
        guide.status = DispatchGuideStatus.DRAFT;
        guide.assignmentStatus = DeliveryAssignmentStatus.UNASSIGNED;
        guide.transferReason = transferReason;
        guide.senderName = requireText(senderName, "El remitente", DispatchGuidePolicy.PARTY_NAME_MAX_LENGTH);
        guide.recipientName = requireText(recipientName, "El destinatario", DispatchGuidePolicy.PARTY_NAME_MAX_LENGTH);
        guide.recipientDocument = optionalText(recipientDocument, "El documento del destinatario", DispatchGuidePolicy.DOCUMENT_MAX_LENGTH);
        guide.originAddress = requireText(originAddress, "El punto de partida", DispatchGuidePolicy.ADDRESS_MAX_LENGTH);
        guide.destinationAddress = requireText(destinationAddress, "El punto de llegada", DispatchGuidePolicy.ADDRESS_MAX_LENGTH);
        guide.saleId = saleId;
        guide.saleOrderId = saleOrderId;
        guide.notes = optionalText(notes, "La nota", DispatchGuidePolicy.NOTE_MAX_LENGTH);
        guide.createdAt = createdAt;
        return guide;
    }

    public DispatchGuideLine addLine(Long productId, String productName, int quantity) {
        assertDraft();
        DispatchGuideLine line = DispatchGuideLine.create(this, productId, productName, quantity);
        lines.add(line);
        return line;
    }

    /**
     * Emite la guía. El número lo asigna el caso de uso; el agregado solo acepta la transición.
     */
    public void issue(String series, String number, Instant when) {
        assertDraft();
        if (lines.isEmpty()) {
            throw new DomainException("La guía no tiene líneas para emitir");
        }
        if (when == null) {
            throw new DomainException("La fecha de emisión es obligatoria");
        }
        this.series = requireText(series, "La serie", DispatchGuidePolicy.SERIES_MAX_LENGTH).toUpperCase(Locale.ROOT);
        this.number = requireText(number, "El número", DispatchGuidePolicy.NUMBER_MAX_LENGTH);
        this.status = DispatchGuideStatus.ISSUED;
        this.issuedAt = when;
    }

    public void voidGuide(String reason, Instant when) {
        if (status == DispatchGuideStatus.VOIDED) {
            throw new DomainException("La guía ya está anulada");
        }
        if (when == null) {
            throw new DomainException("La fecha de anulación es obligatoria");
        }
        this.voidReason = requireText(reason, "El motivo de anulación", DispatchGuidePolicy.VOID_REASON_MAX_LENGTH);
        this.status = DispatchGuideStatus.VOIDED;
        this.voidedAt = when;
    }

    public void assignDelivery(Long carrierId, String carrierName, String carrierCode,
                               DeliveryScope scope, String medium, Instant when) {
        if (status != DispatchGuideStatus.ISSUED) {
            throw new DomainException("Solo se asigna delivery a una guía emitida");
        }
        if (when == null) {
            throw new DomainException("La fecha de asignación es obligatoria");
        }
        if (carrierId == null || carrierId <= 0) {
            throw new DomainException("La asignación exige un transportista");
        }
        if (scope == null) {
            throw new DomainException("La asignación exige el alcance de la entrega");
        }
        if (documentNumber() == null) {
            throw new DomainException("La guía emitida no tiene número");
        }
        String ref = documentNumber() + "/" + carrierId;
        if (ref.length() > AssignmentNotice.REF_MAX) {
            throw new DomainException("La referencia de asignación excede la longitud permitida");
        }
        this.carrierId = carrierId;
        this.carrierName = requireText(carrierName, "El transportista", Carrier.NAME_MAX);
        this.carrierCode = requireText(carrierCode, "El código del transportista", Carrier.CODE_MAX);
        this.deliveryScope = scope;
        this.assignmentRef = ref;
        this.medium = optionalText(medium, "El medio de entrega", DispatchGuidePolicy.MEDIUM_MAX_LENGTH);
        this.assignmentStatus = DeliveryAssignmentStatus.ASSIGNED;
        this.assignedAt = when;
    }

    public String documentNumber() {
        if (series == null || number == null) {
            return null;
        }
        return series + "-" + number;
    }

    public Long getId() { return id; }
    public String getSeries() { return series; }
    public String getNumber() { return number; }
    public DispatchGuideStatus getStatus() { return status; }
    public TransferReason getTransferReason() { return transferReason; }
    public String getSenderName() { return senderName; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientDocument() { return recipientDocument; }
    public String getOriginAddress() { return originAddress; }
    public String getDestinationAddress() { return destinationAddress; }
    public Long getSaleId() { return saleId; }
    public Long getSaleOrderId() { return saleOrderId; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getVoidedAt() { return voidedAt; }
    public String getVoidReason() { return voidReason; }
    public DeliveryAssignmentStatus getAssignmentStatus() { return assignmentStatus; }
    public Long getCarrierId() { return carrierId; }
    public String getCarrierName() { return carrierName; }
    public String getCarrierCode() { return carrierCode; }
    public DeliveryScope getDeliveryScope() { return deliveryScope; }
    public String getAssignmentRef() { return assignmentRef; }
    public String getMedium() { return medium; }
    public Instant getAssignedAt() { return assignedAt; }

    public List<DispatchGuideLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private void assertDraft() {
        if (!status.isDraft()) {
            throw new DomainException("Solo una guía en borrador admite este cambio (" + status.label() + ")");
        }
    }

    private static String requireText(String value, String label, int max) {
        String trimmed = optionalText(value, label, max);
        if (trimmed == null) {
            throw new DomainException(label + " es obligatorio");
        }
        return trimmed;
    }

    private static String optionalText(String value, String label, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new DomainException(label + " supera la longitud permitida");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DispatchGuide guide)) {
            return false;
        }
        return id != null && id.equals(guide.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
