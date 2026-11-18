package com.valeerp.dispatch;

/**
 * Estado de la asignación de delivery sobre la guía. No hay maestro de repartidores.
 */
public enum DeliveryAssignmentStatus {
    UNASSIGNED("Sin asignar"),
    ASSIGNED("Asignado");

    private final String label;

    DeliveryAssignmentStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isAssigned() {
        return this == ASSIGNED;
    }
}
