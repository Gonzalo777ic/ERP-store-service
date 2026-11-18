package com.valeerp.dispatch;

/**
 * Ciclo de la guía. La anulación es un estado de reverso: el documento sigue en el listado.
 */
public enum DispatchGuideStatus {
    DRAFT("Borrador"),
    ISSUED("Emitida"),
    VOIDED("Anulada");

    private final String label;

    DispatchGuideStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isIssued() {
        return this == ISSUED;
    }
}
