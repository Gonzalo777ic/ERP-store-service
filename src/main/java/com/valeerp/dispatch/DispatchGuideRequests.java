package com.valeerp.dispatch;

import java.util.List;

public final class DispatchGuideRequests {

    private DispatchGuideRequests() {
    }

    public record CreateDispatchGuideRequest(
            String senderName,
            String recipientName,
            String recipientDocument,
            String transferReason,
            String originAddress,
            String destinationAddress,
            Long saleId,
            Long saleOrderId,
            String notes,
            List<LineRequest> lines
    ) {
    }

    public record LineRequest(Long productId, String productName, Integer quantity) {
    }

    public record VoidDispatchGuideRequest(String reason) {
    }

    public record AssignDeliveryRequest(Long carrierId, String scope, String medium) {
    }
}
