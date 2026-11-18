package com.valeerp.store.complaint;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Hoja del libro de reclamaciones. El número, el monto y el estado solo cambian por sus métodos.
 */
@Entity
@Table(name = "complaint_sheets")
public class ComplaintSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sheet_number", nullable = false, unique = true, length = 16)
    private String sheetNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "full_name", nullable = false, length = ComplaintContact.NAME_MAX)
    private String fullName;

    @Column(name = "document_type", nullable = false, length = 24)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 32)
    private String documentNumber;

    @Column(nullable = false, length = ComplaintContact.ADDRESS_MAX)
    private String address;

    @Column(nullable = false, length = ComplaintContact.PHONE_MAX)
    private String phone;

    @Column(nullable = false, length = ComplaintContact.EMAIL_MAX)
    private String email;

    @Column(nullable = false)
    private boolean minor;

    @Column(name = "guardian_name", length = ComplaintContact.NAME_MAX)
    private String guardianName;

    @Column(name = "guardian_document_type", length = 24)
    private String guardianDocumentType;

    @Column(name = "guardian_document_number", length = 32)
    private String guardianDocumentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_kind", nullable = false, length = 24)
    private ComplaintSubjectKind subjectKind;

    @Column(name = "subject_description", nullable = false, length = ComplaintContact.SUBJECT_MAX)
    private String subjectDescription;

    @Column(name = "claimed_amount", precision = 19, scale = 2)
    private BigDecimal claimedAmount;

    @Column(length = 3)
    private String currency;

    @Column(name = "order_reference", length = ComplaintContact.REFERENCE_MAX)
    private String orderReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ComplaintKind kind;

    @Column(nullable = false, length = ComplaintContact.DETAIL_MAX)
    private String detail;

    @Column(name = "consumer_request", nullable = false, length = ComplaintContact.DETAIL_MAX)
    private String consumerRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ComplaintStatus status;

    @Column(name = "provider_response", length = ComplaintContact.RESPONSE_MAX)
    private String providerResponse;

    @Column(name = "void_reason", length = ComplaintContact.RESPONSE_MAX)
    private String voidReason;

    @Column(name = "filed_at", nullable = false)
    private Instant filedAt;

    @Column(name = "attention_started_at")
    private Instant attentionStartedAt;

    @Column(name = "attention_started_by", length = 128)
    private String attentionStartedBy;

    @Column(name = "attended_at")
    private Instant attendedAt;

    @Column(name = "attended_by", length = 128)
    private String attendedBy;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "voided_by", length = 128)
    private String voidedBy;

    protected ComplaintSheet() {
    }

    public static ComplaintSheet open(ComplaintSheetNumber number, ComplaintFiling filing, Instant filedAt) {
        if (number == null) {
            throw new DomainException("El número de hoja es obligatorio");
        }
        if (filing == null) {
            throw new DomainException("La hoja de reclamación está vacía");
        }
        if (filedAt == null) {
            throw new DomainException("La fecha de la hoja es obligatoria");
        }
        if (filing.document() == null) {
            throw new DomainException("El documento del consumidor es obligatorio");
        }
        if (filing.subjectKind() == null) {
            throw new DomainException("El bien o servicio es obligatorio");
        }
        if (filing.kind() == null) {
            throw new DomainException("El tipo de reclamo es obligatorio");
        }
        if (filing.customerId() != null && filing.customerId() <= 0) {
            throw new DomainException("El cliente de la sesión no es válido");
        }

        ComplaintSheet sheet = new ComplaintSheet();
        sheet.sheetNumber = number.text();
        sheet.customerId = filing.customerId();
        sheet.fullName = ComplaintContact.required(filing.fullName(), "El nombre", ComplaintContact.NAME_MAX);
        sheet.documentType = filing.document().type().name();
        sheet.documentNumber = filing.document().number();
        sheet.address = ComplaintContact.required(filing.address(), "El domicilio", ComplaintContact.ADDRESS_MAX);
        sheet.phone = ComplaintContact.phone(filing.phone());
        sheet.email = ComplaintContact.email(filing.email());
        sheet.minor = filing.minor();
        sheet.applyGuardian(filing);
        sheet.subjectKind = filing.subjectKind();
        sheet.subjectDescription = ComplaintContact.required(
                filing.subjectDescription(), "La descripción del bien o servicio", ComplaintContact.SUBJECT_MAX);
        sheet.applyAmount(filing.claimedAmount());
        sheet.orderReference = ComplaintContact.optional(
                filing.orderReference(), "El pedido o comprobante", ComplaintContact.REFERENCE_MAX);
        sheet.kind = filing.kind();
        sheet.detail = ComplaintContact.required(filing.detail(), "El detalle", ComplaintContact.DETAIL_MAX);
        sheet.consumerRequest = ComplaintContact.required(
                filing.consumerRequest(), "El pedido del consumidor", ComplaintContact.DETAIL_MAX);
        sheet.status = ComplaintStatus.RECEIVED;
        sheet.filedAt = filedAt;
        return sheet;
    }

    public void startAttention(Actor actor, Instant when) {
        if (status != ComplaintStatus.RECEIVED) {
            throw new DomainException("Solo una hoja recibida puede pasar a atención");
        }
        this.status = ComplaintStatus.IN_ATTENTION;
        this.attentionStartedAt = requireWhen(when);
        this.attentionStartedBy = requireActor(actor);
    }

    public void attend(Actor actor, Instant when, String response) {
        if (!status.acceptsAttention()) {
            throw new DomainException("La hoja ya no admite atención");
        }
        String answer = ComplaintContact.required(response, "La respuesta del proveedor", ComplaintContact.RESPONSE_MAX);
        Instant at = requireWhen(when);
        String actorId = requireActor(actor);
        if (attentionStartedAt == null) {
            this.attentionStartedAt = at;
            this.attentionStartedBy = actorId;
        }
        this.providerResponse = answer;
        this.status = ComplaintStatus.ATTENDED;
        this.attendedAt = at;
        this.attendedBy = actorId;
    }

    public void cancel(Actor actor, Instant when, String reason) {
        if (!status.acceptsAttention()) {
            throw new DomainException("La hoja ya no admite anulación");
        }
        this.voidReason = ComplaintContact.required(reason, "El motivo de anulación", ComplaintContact.RESPONSE_MAX);
        this.status = ComplaintStatus.VOIDED;
        this.voidedAt = requireWhen(when);
        this.voidedBy = requireActor(actor);
    }

    public List<ComplaintAction> availableActions() {
        return switch (status) {
            case RECEIVED -> List.of(ComplaintAction.START_ATTENTION, ComplaintAction.ATTEND, ComplaintAction.VOID);
            case IN_ATTENTION -> List.of(ComplaintAction.ATTEND, ComplaintAction.VOID);
            case ATTENDED, VOIDED -> List.of();
        };
    }

    public Long id() {
        return id;
    }

    public String sheetNumber() {
        return sheetNumber;
    }

    public Long customerId() {
        return customerId;
    }

    public String fullName() {
        return fullName;
    }

    public String documentType() {
        return documentType;
    }

    public String documentNumber() {
        return documentNumber;
    }

    public String address() {
        return address;
    }

    public String phone() {
        return phone;
    }

    public String email() {
        return email;
    }

    public boolean minor() {
        return minor;
    }

    public String guardianName() {
        return guardianName;
    }

    public String guardianDocumentType() {
        return guardianDocumentType;
    }

    public String guardianDocumentNumber() {
        return guardianDocumentNumber;
    }

    public ComplaintSubjectKind subjectKind() {
        return subjectKind;
    }

    public String subjectDescription() {
        return subjectDescription;
    }

    public BigDecimal claimedAmount() {
        return claimedAmount;
    }

    public String currency() {
        return currency;
    }

    public String orderReference() {
        return orderReference;
    }

    public ComplaintKind kind() {
        return kind;
    }

    public String detail() {
        return detail;
    }

    public String consumerRequest() {
        return consumerRequest;
    }

    public ComplaintStatus status() {
        return status;
    }

    public String providerResponse() {
        return providerResponse;
    }

    public String voidReason() {
        return voidReason;
    }

    public Instant filedAt() {
        return filedAt;
    }

    public Instant attentionStartedAt() {
        return attentionStartedAt;
    }

    public String attentionStartedBy() {
        return attentionStartedBy;
    }

    public Instant attendedAt() {
        return attendedAt;
    }

    public String attendedBy() {
        return attendedBy;
    }

    public Instant voidedAt() {
        return voidedAt;
    }

    public String voidedBy() {
        return voidedBy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ComplaintSheet that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? getClass().hashCode() : id.hashCode();
    }

    private void applyGuardian(ComplaintFiling filing) {
        if (!filing.minor()) {
            this.guardianName = null;
            this.guardianDocumentType = null;
            this.guardianDocumentNumber = null;
            return;
        }
        if (filing.guardianDocument() == null) {
            throw new DomainException("El documento del padre, madre o apoderado es obligatorio");
        }
        this.guardianName = ComplaintContact.required(
                filing.guardianName(), "El nombre del padre, madre o apoderado", ComplaintContact.NAME_MAX);
        this.guardianDocumentType = filing.guardianDocument().type().name();
        this.guardianDocumentNumber = filing.guardianDocument().number();
    }

    private void applyAmount(Money amount) {
        if (amount == null) {
            this.claimedAmount = null;
            this.currency = null;
            return;
        }
        if (amount.isNegative()) {
            throw new DomainException("El monto reclamado no puede ser negativo");
        }
        this.claimedAmount = amount.amount();
        this.currency = amount.currency();
    }

    private static Instant requireWhen(Instant when) {
        if (when == null) {
            throw new DomainException("La fecha de la transición es obligatoria");
        }
        return when;
    }

    private static String requireActor(Actor actor) {
        if (actor == null || actor.id() == null || actor.id().isBlank()) {
            throw new DomainException("El actor de la transición es obligatorio");
        }
        return actor.id();
    }
}
