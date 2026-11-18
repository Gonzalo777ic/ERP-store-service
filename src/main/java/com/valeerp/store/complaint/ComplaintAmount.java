package com.valeerp.store.complaint;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;

/**
 * Monto reclamado opcional. Si el consumidor lo informa, queda en Money de escala 2.
 */
final class ComplaintAmount {

    private ComplaintAmount() {
    }

    static Money optional(String raw, String currency) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainException("La moneda del libro no está configurada");
        }
        BigDecimal parsed;
        try {
            parsed = new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            throw new DomainException("El monto reclamado no es válido");
        }
        if (parsed.scale() > Money.SCALE) {
            throw new DomainException("El monto reclamado admite como máximo dos decimales");
        }
        Money money = Money.of(parsed, currency.trim().toUpperCase());
        if (money.isNegative()) {
            throw new DomainException("El monto reclamado no puede ser negativo");
        }
        return money;
    }
}
