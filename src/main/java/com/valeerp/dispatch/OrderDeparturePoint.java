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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Punto de partida guardado para un pedido. Se reemplaza; no se borra por HTTP.
 */
@Entity
@Table(name = "order_departure_points", uniqueConstraints = {
        @UniqueConstraint(name = "order_departure_ref_uk", columnNames = {"order_kind", "order_id"})
})
public class OrderDeparturePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_kind", nullable = false, length = 32)
    private OrderRefKind orderKind;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(length = 240)
    private String label;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude", precision = 9, scale = GeoPoint.SCALE)),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude", precision = 9, scale = GeoPoint.SCALE))
    })
    private GeoPoint point;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderDeparturePoint() {
    }

    public static OrderDeparturePoint place(OrderRefKind kind, Long orderId, String label, GeoPoint point, Instant when) {
        if (kind == null || orderId == null || point == null || when == null) {
            throw new DomainException("El punto de partida del pedido es obligatorio");
        }
        OrderDeparturePoint row = new OrderDeparturePoint();
        row.orderKind = kind;
        row.orderId = orderId;
        row.label = blankToNull(label);
        row.point = point;
        row.updatedAt = when;
        return row;
    }

    public void relocate(String label, GeoPoint point, Instant when) {
        if (point == null || when == null) {
            throw new DomainException("El punto de partida del pedido es obligatorio");
        }
        String nextLabel = blankToNull(label);
        if (nextLabel != null) {
            this.label = nextLabel;
        }
        this.point = point;
        this.updatedAt = when;
    }

    public Long getId() {
        return id;
    }

    public OrderRefKind getOrderKind() {
        return orderKind;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getLabel() {
        return label;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 240) {
            throw new DomainException("La dirección excede la longitud permitida");
        }
        return trimmed;
    }
}
