package com.valeerp.store.complaint;

/**
 * Transiciones que el libro publica. El camino HTTP coincide con {@link #path()}.
 */
public enum ComplaintAction {
    START_ATTENTION("Pasar a atención", "attention", null, null),
    ATTEND("Registrar atención", "resolution", "response", "Respuesta del proveedor"),
    VOID("Anular hoja", "cancellation", "reason", "Motivo de la anulación");

    public static final String ATTENTION_PATH = "attention";
    public static final String RESOLUTION_PATH = "resolution";
    public static final String CANCELLATION_PATH = "cancellation";

    private final String label;
    private final String path;
    private final String textField;
    private final String textLabel;

    ComplaintAction(String label, String path, String textField, String textLabel) {
        this.label = label;
        this.path = path;
        this.textField = textField;
        this.textLabel = textLabel;
    }

    public String label() {
        return label;
    }

    public String path() {
        return path;
    }

    public String textField() {
        return textField;
    }

    public String textLabel() {
        return textLabel;
    }
}
