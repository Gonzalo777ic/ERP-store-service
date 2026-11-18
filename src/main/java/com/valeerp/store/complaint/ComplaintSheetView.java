package com.valeerp.store.complaint;

import com.valeerp.kernel.Money;
import com.valeerp.partner.DocumentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ComplaintSheetView(
        Long id,
        String sheetNumber,
        Instant filedAt,
        Long customerId,
        String consumerName,
        String documentType,
        String documentTypeLabel,
        String documentNumber,
        String address,
        String phone,
        String email,
        boolean minor,
        String guardianName,
        String guardianDocumentType,
        String guardianDocumentTypeLabel,
        String guardianDocumentNumber,
        String subjectKind,
        String subjectKindLabel,
        String subjectDescription,
        String claimedAmount,
        String currency,
        String orderReference,
        String kind,
        String kindLabel,
        String detail,
        String consumerRequest,
        String status,
        String statusLabel,
        String providerResponse,
        Instant attentionStartedAt,
        String attentionStartedBy,
        Instant attendedAt,
        String attendedBy,
        Instant voidedAt,
        String voidedBy,
        String voidReason,
        List<ComplaintActionView> actions
) {

    public record ComplaintActionView(
            String code,
            String label,
            String path,
            String textField,
            String textLabel
    ) {
        static ComplaintActionView from(ComplaintAction action) {
            return new ComplaintActionView(
                    action.name(),
                    action.label(),
                    action.path(),
                    action.textField(),
                    action.textLabel()
            );
        }
    }

    public static ComplaintSheetView from(ComplaintSheet sheet) {
        return new ComplaintSheetView(
                sheet.id(),
                sheet.sheetNumber(),
                sheet.filedAt(),
                sheet.customerId(),
                sheet.fullName(),
                sheet.documentType(),
                documentLabel(sheet.documentType()),
                sheet.documentNumber(),
                sheet.address(),
                sheet.phone(),
                sheet.email(),
                sheet.minor(),
                sheet.guardianName(),
                sheet.guardianDocumentType(),
                documentLabel(sheet.guardianDocumentType()),
                sheet.guardianDocumentNumber(),
                sheet.subjectKind().name(),
                sheet.subjectKind().label(),
                sheet.subjectDescription(),
                moneyText(sheet.claimedAmount()),
                sheet.currency(),
                sheet.orderReference(),
                sheet.kind().name(),
                sheet.kind().label(),
                sheet.detail(),
                sheet.consumerRequest(),
                sheet.status().name(),
                sheet.status().label(),
                sheet.providerResponse(),
                sheet.attentionStartedAt(),
                sheet.attentionStartedBy(),
                sheet.attendedAt(),
                sheet.attendedBy(),
                sheet.voidedAt(),
                sheet.voidedBy(),
                sheet.voidReason(),
                sheet.availableActions().stream().map(ComplaintActionView::from).toList()
        );
    }

    private static String documentLabel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return DocumentType.require(code).description();
    }

    private static String moneyText(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(Money.SCALE, Money.ROUNDING).toPlainString();
    }
}
