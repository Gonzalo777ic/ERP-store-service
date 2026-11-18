package com.valeerp.dispatch;

/**
 * Puerto que consume la asignación de delivery. No es la guía.
 */
public interface CarrierDesk {

    DispatchViews.RouteView accept(AssignmentNotice notice);
}
