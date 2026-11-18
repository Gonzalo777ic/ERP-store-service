package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchGuideTest {

    private static final Instant DRAFTED_AT = Instant.parse("2026-11-18T15:00:00Z");
    private static final Instant ISSUED_AT = Instant.parse("2026-11-18T16:00:00Z");

    @Test
    void issueAssignsNumberAndKeepsLines() {
        DispatchGuide guide = draftWithLine();

        guide.issue("t001", "00000001", ISSUED_AT);

        assertEquals(DispatchGuideStatus.ISSUED, guide.getStatus());
        assertEquals("T001-00000001", guide.documentNumber());
        assertEquals(ISSUED_AT, guide.getIssuedAt());
        assertEquals(1, guide.getLines().size());
        assertEquals(DeliveryAssignmentStatus.UNASSIGNED, guide.getAssignmentStatus());
    }

    @Test
    void issueRejectsGuideWithoutLines() {
        DispatchGuide guide = draft();

        DomainException error = assertThrows(DomainException.class, () -> guide.issue("T001", "00000001", ISSUED_AT));

        assertTrue(error.getMessage().contains("líneas"));
        assertEquals(DispatchGuideStatus.DRAFT, guide.getStatus());
        assertNull(guide.documentNumber());
    }

    @Test
    void issueRejectsSecondEmission() {
        DispatchGuide guide = draftWithLine();
        guide.issue("T001", "00000001", ISSUED_AT);

        assertThrows(DomainException.class, () -> guide.issue("T001", "00000002", ISSUED_AT));
        assertEquals("T001-00000001", guide.documentNumber());
    }

    @Test
    void assignDeliveryRequiresIssuedGuideAndCarrier() {
        DispatchGuide draft = draftWithLine();
        assertThrows(DomainException.class, () -> assign(draft, 4L));

        DispatchGuide guide = draftWithLine();
        guide.issue("T001", "00000001", ISSUED_AT);
        assertThrows(DomainException.class, () -> assign(guide, null));

        assign(guide, 4L);

        assertEquals(DeliveryAssignmentStatus.ASSIGNED, guide.getAssignmentStatus());
        assertEquals(4L, guide.getCarrierId());
        assertEquals("DRV-14", guide.getCarrierCode());
        assertEquals("Ana Ruiz", guide.getCarrierName());
        assertEquals("T001-00000001/4", guide.getAssignmentRef());
        assertEquals(DeliveryScope.TOTAL, guide.getDeliveryScope());
    }

    @Test
    void voidKeepsTheGuideAndBlocksAssignment() {
        DispatchGuide guide = draftWithLine();
        guide.issue("T001", "00000001", ISSUED_AT);
        assign(guide, 4L);

        guide.voidGuide("Destinatario ausente", Instant.parse("2026-11-18T18:00:00Z"));

        assertEquals(DispatchGuideStatus.VOIDED, guide.getStatus());
        assertEquals(1, guide.getLines().size());
        assertEquals("Ana Ruiz", guide.getCarrierName());
        assertThrows(DomainException.class, () -> assign(guide, 8L));
    }

    @Test
    void lineQuantityMustBePositive() {
        DispatchGuide guide = draft();

        assertThrows(DomainException.class, () -> guide.addLine(1L, "Arroz", 0));
        assertTrue(guide.getLines().isEmpty());
    }

    private static void assign(DispatchGuide guide, Long carrierId) {
        guide.assignDelivery(carrierId, "Ana Ruiz", "DRV-14", DeliveryScope.TOTAL, "Moto ABC-123", ISSUED_AT);
    }

    private static DispatchGuide draftWithLine() {
        DispatchGuide guide = draft();
        guide.addLine(10L, "Arroz extra", 4);
        return guide;
    }

    private static DispatchGuide draft() {
        return DispatchGuide.draft(
                "Vale ERP",
                "Bodega Norte",
                "20123456789",
                TransferReason.SALE,
                "Almacén central",
                "Av. Las Flores 123",
                15L,
                22L,
                null,
                DRAFTED_AT
        );
    }
}
