package com.valeerp.store.complaint;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un solo correlativo del libro. La hoja no se numera en el canal.
 */
@Entity
@Table(name = "complaint_sheet_counters")
public class ComplaintSheetCounter {

    public static final long ID = 1L;

    @Id
    private Long id;

    @Column(name = "last_value", nullable = false)
    private long lastValue;

    protected ComplaintSheetCounter() {
    }

    public static ComplaintSheetCounter start() {
        ComplaintSheetCounter counter = new ComplaintSheetCounter();
        counter.id = ID;
        counter.lastValue = 0L;
        return counter;
    }

    public long next() {
        lastValue = lastValue + 1;
        return lastValue;
    }
}
