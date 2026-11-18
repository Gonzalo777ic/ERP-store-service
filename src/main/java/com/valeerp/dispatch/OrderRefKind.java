package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Qué pedido nombra la asignación, sin copiar su documento.
 */
public enum OrderRefKind {
    CUSTOMER_ORDER,
    SALE_ORDER,
    DISPATCH_GUIDE;

    public static OrderRefKind parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de pedido es obligatorio");
        }
        try {
            return OrderRefKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de pedido no soportado: " + raw);
        }
    }
}
