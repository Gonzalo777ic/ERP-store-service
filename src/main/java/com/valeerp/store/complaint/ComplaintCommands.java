package com.valeerp.store.complaint;

/**
 * Cuerpo HTTP del libro. El id de cliente no viaja aquí: sale de la sesión, si existe.
 */
public final class ComplaintCommands {

    private ComplaintCommands() {
    }

    public record File(
            String fullName,
            String documentType,
            String documentNumber,
            String address,
            String phone,
            String email,
            boolean minor,
            String guardianName,
            String guardianDocumentType,
            String guardianDocumentNumber,
            String subjectKind,
            String subjectDescription,
            String claimedAmount,
            String orderReference,
            String kind,
            String detail,
            String consumerRequest
    ) {
    }

    public record Response(String response) {
    }

    public record Cancellation(String reason) {
    }
}
