package com.valeerp.store.complaint;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.partner.DocumentFormatPolicy;
import com.valeerp.partner.IdentityDocument;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintSheetTest {

    private static final Instant FILED_AT = Instant.parse("2026-11-18T15:00:00Z");
    private static final DocumentFormatPolicy DOCUMENTS = DocumentFormatPolicy.withDefaults();

    @Test
    void formatsTheSheetNumberWithoutAskingTheChannel() {
        assertEquals("000001", ComplaintSheetNumber.of(1).text());
        assertEquals("000042", ComplaintSheetNumber.of(42).text());
        assertThrows(DomainException.class, () -> ComplaintSheetNumber.of(0));
    }

    @Test
    void publishedPathsMatchTheHttpContract() {
        assertEquals(ComplaintAction.ATTENTION_PATH, ComplaintAction.START_ATTENTION.path());
        assertEquals(ComplaintAction.RESOLUTION_PATH, ComplaintAction.ATTEND.path());
        assertEquals(ComplaintAction.CANCELLATION_PATH, ComplaintAction.VOID.path());
    }

    @Test
    void opensAReceivedSheetAndKeepsTheConsumerData() {
        ComplaintSheet sheet = ComplaintSheet.open(ComplaintSheetNumber.of(7), filing(false, null, null), FILED_AT);

        assertEquals("000007", sheet.sheetNumber());
        assertEquals(ComplaintStatus.RECEIVED, sheet.status());
        assertEquals("Ana Perez", sheet.fullName());
        assertEquals("ana@example.com", sheet.email());
        assertEquals("DNI", sheet.documentType());
        assertEquals("12345678", sheet.documentNumber());
        assertEquals("RECLAMO", sheet.kind().name());
        assertEquals(new BigDecimal("45.50"), sheet.claimedAmount());
        assertEquals("PEN", sheet.currency());
        assertNull(sheet.guardianName());
        assertEquals(3, sheet.availableActions().size());
    }

    @Test
    void requiresGuardianWhenTheConsumerIsAMinor() {
        DomainException error = assertThrows(DomainException.class,
                () -> ComplaintSheet.open(ComplaintSheetNumber.of(1), filing(true, null, null), FILED_AT));
        assertTrue(error.getMessage().contains("apoderado"));
    }

    @Test
    void attendsAndThenRefusesAnotherTransition() {
        ComplaintSheet sheet = ComplaintSheet.open(ComplaintSheetNumber.of(1), filing(false, null, null), FILED_AT);
        Actor clerk = Actor.of("asesor-1");
        Instant later = FILED_AT.plusSeconds(60);

        sheet.startAttention(clerk, later);
        assertEquals(ComplaintStatus.IN_ATTENTION, sheet.status());

        sheet.attend(clerk, later.plusSeconds(60), "Se repone el producto.");
        assertEquals(ComplaintStatus.ATTENDED, sheet.status());
        assertEquals("Se repone el producto.", sheet.providerResponse());
        assertTrue(sheet.availableActions().isEmpty());
        assertThrows(DomainException.class, () -> sheet.cancel(clerk, later, "Ya no"));
    }

    @Test
    void cancellationStaysInTheBookAsAStatus() {
        ComplaintSheet sheet = ComplaintSheet.open(ComplaintSheetNumber.of(2), filing(false, null, null), FILED_AT);
        sheet.cancel(Actor.of("asesor-2"), FILED_AT.plusSeconds(10), "Hoja duplicada");

        assertEquals(ComplaintStatus.VOIDED, sheet.status());
        assertEquals("Hoja duplicada", sheet.voidReason());
        assertTrue(sheet.availableActions().isEmpty());
    }

    @Test
    void rejectsANegativeClaimedAmount() {
        assertThrows(DomainException.class, () -> ComplaintAmount.optional("-1.00", "PEN"));
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThrows(DomainException.class, () -> ComplaintAmount.optional("10.555", "PEN"));
        assertEquals(new BigDecimal("10.50"), ComplaintAmount.optional("10.5", "PEN").amount());
    }

    private static ComplaintFiling filing(boolean minor, String guardianName, IdentityDocument guardian) {
        IdentityDocument document = DOCUMENTS.require("DNI", "12345678");
        return new ComplaintFiling(
                9L,
                "Ana Perez",
                document,
                "Av. Los Álamos 120, Lima",
                "999888777",
                "Ana@Example.com",
                minor,
                guardianName,
                guardian,
                ComplaintSubjectKind.GOOD,
                "Licuadora modelo L-2",
                Money.of(new BigDecimal("45.50"), "PEN"),
                "B001-15",
                ComplaintKind.RECLAMO,
                "El equipo no enciende.",
                "Reposición del bien o devolución del monto."
        );
    }
}
