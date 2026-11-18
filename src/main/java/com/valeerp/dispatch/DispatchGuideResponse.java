package com.valeerp.dispatch;

import java.time.Instant;
import java.util.List;

public record DispatchGuideResponse(
        Long id,
        String documentNumber,
        String series,
        String number,
        String status,
        String statusLabel,
        String transferReason,
        String transferReasonLabel,
        String senderName,
        String recipientName,
        String recipientDocument,
        String originAddress,
        String destinationAddress,
        Long saleId,
        Long saleOrderId,
        String notes,
        Instant createdAt,
        Instant issuedAt,
        Instant voidedAt,
        String voidReason,
        String assignmentStatus,
        String assignmentStatusLabel,
        Long carrierId,
        String carrierName,
        String carrierCode,
        String medium,
        String deliveryScope,
        String deliveryScopeLabel,
        String assignmentRef,
        Instant assignedAt,
        boolean canIssue,
        boolean canAssignDelivery,
        boolean canVoid,
        List<LineResponse> lines
) {

    public static DispatchGuideResponse summary(DispatchGuide guide) {
        return from(guide, List.of());
    }

    public static DispatchGuideResponse detail(DispatchGuide guide) {
        return from(guide, guide.getLines().stream().map(LineResponse::from).toList());
    }

    private static DispatchGuideResponse from(DispatchGuide guide, List<LineResponse> lines) {
        return new DispatchGuideResponse(
                guide.getId(),
                guide.documentNumber(),
                guide.getSeries(),
                guide.getNumber(),
                guide.getStatus().name(),
                guide.getStatus().label(),
                guide.getTransferReason().name(),
                guide.getTransferReason().label(),
                guide.getSenderName(),
                guide.getRecipientName(),
                guide.getRecipientDocument(),
                guide.getOriginAddress(),
                guide.getDestinationAddress(),
                guide.getSaleId(),
                guide.getSaleOrderId(),
                guide.getNotes(),
                guide.getCreatedAt(),
                guide.getIssuedAt(),
                guide.getVoidedAt(),
                guide.getVoidReason(),
                guide.getAssignmentStatus().name(),
                guide.getAssignmentStatus().label(),
                guide.getCarrierId(),
                guide.getCarrierName(),
                guide.getCarrierCode(),
                guide.getMedium(),
                guide.getDeliveryScope() == null ? null : guide.getDeliveryScope().name(),
                guide.getDeliveryScope() == null ? null : guide.getDeliveryScope().label(),
                guide.getAssignmentRef(),
                guide.getAssignedAt(),
                guide.getStatus().isDraft(),
                guide.getStatus().isIssued(),
                guide.getStatus() != DispatchGuideStatus.VOIDED,
                lines
        );
    }

    public record LineResponse(Long id, Long productId, String productName, int quantity) {
        static LineResponse from(DispatchGuideLine line) {
            return new LineResponse(line.getId(), line.getProductId(), line.getProductName(), line.getQuantity());
        }
    }
}
