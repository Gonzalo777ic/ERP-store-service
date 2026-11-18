package com.valeerp.dispatch;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Libro de ubicaciones del recorrido. No se da de baja ni se borra.
 */
@Entity
@Table(name = "route_locations", indexes = {
        @Index(name = "route_locations_route_idx", columnList = "route_id")
})
public class RouteLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Embedded
    private GeoPoint point;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected RouteLocation() {
    }

    static RouteLocation capture(Long routeId, GeoPoint point, Instant recordedAt) {
        RouteLocation location = new RouteLocation();
        location.routeId = routeId;
        location.point = point;
        location.recordedAt = recordedAt;
        return location;
    }

    public Long getId() {
        return id;
    }

    public Long getRouteId() {
        return routeId;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RouteLocation location)) {
            return false;
        }
        return id != null && id.equals(location.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
