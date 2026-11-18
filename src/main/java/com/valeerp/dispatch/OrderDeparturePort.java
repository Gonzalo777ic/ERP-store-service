package com.valeerp.dispatch;

/**
 * Puerto hacia el pedido. La guía de remisión no se modela aquí.
 */
public interface OrderDeparturePort {

    OrderDeparture load(OrderRef ref);
}
