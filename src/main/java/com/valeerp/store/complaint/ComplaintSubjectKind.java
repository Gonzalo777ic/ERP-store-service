package com.valeerp.store.complaint;

import com.valeerp.kernel.DomainException;

/**
 * Bien o servicio contratado, como lo distingue la hoja de reclamación.
 */
public enum ComplaintSubjectKind {
    GOOD("Bien", "Producto adquirido."),
    SERVICE("Servicio", "Servicio contratado.");

    private final String label;
    private final String description;

    ComplaintSubjectKind(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public static ComplaintSubjectKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El bien o servicio es obligatorio");
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Bien o servicio no soportado");
        }
    }
}
