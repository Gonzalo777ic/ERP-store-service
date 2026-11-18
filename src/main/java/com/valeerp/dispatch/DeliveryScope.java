package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

public enum DeliveryScope {
    TOTAL("Total"),
    PARCIAL("Parcial");

    private final String label;

    DeliveryScope(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static DeliveryScope parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El alcance de la entrega es obligatorio");
        }
        try {
            return DeliveryScope.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Alcance de entrega no soportado: " + raw);
        }
    }
}
