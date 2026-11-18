package com.valeerp.dispatch;

/**
 * Dirección ya guardada del pedido. El punto puede faltar si nadie lo georreferenció.
 */
public record OrderDeparture(OrderRef ref, String label, GeoPoint point) {
}
