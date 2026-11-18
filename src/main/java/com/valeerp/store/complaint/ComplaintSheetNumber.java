package com.valeerp.store.complaint;

import com.valeerp.kernel.DomainException;

/**
 * Correlativo de la hoja. Lo asigna el libro; el canal solo lo muestra.
 */
public final class ComplaintSheetNumber {

    public static final int WIDTH = 6;

    private final long value;

    private ComplaintSheetNumber(long value) {
        this.value = value;
    }

    public static ComplaintSheetNumber of(long value) {
        if (value < 1) {
            throw new DomainException("El número de hoja debe ser positivo");
        }
        return new ComplaintSheetNumber(value);
    }

    public long value() {
        return value;
    }

    public String text() {
        return String.format("%0" + WIDTH + "d", value);
    }
}
