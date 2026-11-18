package com.valeerp.store.complaint;

/**
 * Estados de la hoja. La anulación permanece en el libro: no se oculta ni se borra.
 */
public enum ComplaintStatus {
    RECEIVED("Recibido", "La hoja ingresó al libro."),
    IN_ATTENTION("En atención", "El establecimiento está revisando la hoja."),
    ATTENDED("Atendido", "El establecimiento registró su respuesta."),
    VOIDED("Anulado", "La hoja queda en el libro con estado anulado.");

    private final String label;
    private final String description;

    ComplaintStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public boolean acceptsAttention() {
        return this == RECEIVED || this == IN_ATTENTION;
    }
}
