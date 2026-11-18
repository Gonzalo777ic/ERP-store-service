package com.valeerp.dispatch;

/**
 * Motivo de traslado de la guía de remisión.
 */
public enum TransferReason {
    SALE("Venta"),
    PURCHASE("Compra"),
    RETURN("Devolución"),
    INTERNAL_TRANSFER("Traslado entre establecimientos"),
    CONSIGNMENT("Consignación"),
    OTHER("Otros");

    private final String label;

    TransferReason(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
