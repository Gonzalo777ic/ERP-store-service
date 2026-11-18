package com.valeerp.store.complaint;

import com.valeerp.kernel.DomainException;

/**
 * Reclamo o queja, según el libro de reclamaciones del establecimiento.
 */
public enum ComplaintKind {
    RECLAMO(
            "Reclamo",
            "Disconformidad relacionada con los productos o servicios."
    ),
    QUEJA(
            "Queja",
            "Disconformidad no relacionada con los productos o servicios, o malestar o descontento respecto de la atención al público."
    );

    private final String label;
    private final String description;

    ComplaintKind(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public static ComplaintKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de reclamo es obligatorio");
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de reclamo no soportado");
        }
    }
}
