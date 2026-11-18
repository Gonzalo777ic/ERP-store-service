package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

/**
 * Recorrido de una entrega asignada. Libro: no se oculta ni se borra.
 */
@Entity
@Table(name = "carrier_routes", indexes = {
        @Index(name = "carrier_routes_carrier_idx", columnList = "carrier_id")
})
public class CarrierRoute {

    public static final String NOT_IN_PROGRESS = "ROUTE_NOT_IN_PROGRESS";
    public static final String TOO_SOON = "LOCATION_TOO_SOON";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carrier_id", nullable = false)
    private Long carrierId;

    @Column(name = "assignment_ref", nullable = false, unique = true, length = AssignmentNotice.REF_MAX)
    private String assignmentRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_kind", nullable = false, length = 32)
    private OrderRefKind orderKind;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeliveryScope scope;

    @Column(nullable = false, length = AssignmentNotice.SUMMARY_MAX)
    private String summary;

    @Column(name = "destination_label", length = 240)
    private String destinationLabel;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude", precision = 9, scale = GeoPoint.SCALE)),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude", precision = 9, scale = GeoPoint.SCALE))
    })
    private GeoPoint destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RouteStatus status;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "last_fix_at")
    private Instant lastFixAt;

    protected CarrierRoute() {
    }

    public static CarrierRoute open(String assignmentRef, OrderRefKind orderKind, Long orderId, Long carrierId,
                                    DeliveryScope scope, String summary, String destinationLabel,
                                    GeoPoint destination, Instant when) {
        if (when == null) {
            throw new DomainException("La asignación exige fecha");
        }
        CarrierRoute route = new CarrierRoute();
        route.assignmentRef = assignmentRef;
        route.orderKind = orderKind;
        route.orderId = orderId;
        route.carrierId = carrierId;
        route.scope = scope;
        route.summary = summary == null ? defaultSummary(scope, orderId) : summary;
        route.destinationLabel = clip(destinationLabel, 240);
        route.destination = destination;
        route.status = RouteStatus.ASIGNADO;
        route.assignedAt = when;
        return route;
    }

    public void start(Instant when) {
        if (status != RouteStatus.ASIGNADO) {
            throw new DomainException("El recorrido ya no está pendiente de inicio");
        }
        if (when == null) {
            throw new DomainException("El inicio del recorrido exige fecha");
        }
        this.status = RouteStatus.EN_CAMINO;
        this.startedAt = when;
    }

    public void deliver(Instant when) {
        if (status != RouteStatus.EN_CAMINO) {
            throw new DomainException("Solo un recorrido en camino se puede marcar entregado");
        }
        if (when == null) {
            throw new DomainException("La entrega exige fecha");
        }
        this.status = RouteStatus.ENTREGADO;
        this.deliveredAt = when;
    }

    /**
     * Acepta un punto solo con el recorrido en curso y respetando el intervalo de política.
     */
    public RouteLocation acceptFix(GeoPoint point, Instant when, Duration minimumGap) {
        if (point == null || when == null) {
            throw new DomainException("La ubicación es obligatoria");
        }
        if (status != RouteStatus.EN_CAMINO) {
            throw new DomainException(NOT_IN_PROGRESS, "La ubicación solo se registra con el recorrido en curso");
        }
        if (minimumGap == null || minimumGap.isZero() || minimumGap.isNegative()) {
            throw new DomainException("El intervalo de ubicación no es válido");
        }
        if (lastFixAt != null && when.isBefore(lastFixAt.plus(minimumGap))) {
            throw new DomainException(TOO_SOON, "Todavía no corresponde registrar otra ubicación");
        }
        if (id == null) {
            throw new DomainException("El recorrido debe estar guardado antes de registrar ubicación");
        }
        this.lastFixAt = when;
        return RouteLocation.capture(id, point, when);
    }

    public Long getId() {
        return id;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public String getAssignmentRef() {
        return assignmentRef;
    }

    public OrderRefKind getOrderKind() {
        return orderKind;
    }

    public Long getOrderId() {
        return orderId;
    }

    public DeliveryScope getScope() {
        return scope;
    }

    public String getSummary() {
        return summary;
    }

    public String getDestinationLabel() {
        return destinationLabel;
    }

    public GeoPoint getDestination() {
        return destination;
    }

    public RouteStatus getStatus() {
        return status;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getLastFixAt() {
        return lastFixAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CarrierRoute route)) {
            return false;
        }
        return id != null && id.equals(route.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private static String defaultSummary(DeliveryScope scope, Long orderId) {
        String reach = scope == DeliveryScope.PARCIAL ? "Entrega parcial" : "Entrega total";
        return reach + " del pedido " + orderId;
    }
}
