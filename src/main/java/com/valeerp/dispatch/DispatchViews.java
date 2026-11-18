package com.valeerp.dispatch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class DispatchViews {

    private DispatchViews() {
    }

    public record CarrierView(
            Long id,
            String code,
            String displayName,
            String identitySubject,
            String phone,
            BigDecimal latitude,
            BigDecimal longitude,
            Instant lastFixAt
    ) {
        public static CarrierView from(Carrier carrier) {
            GeoPoint fix = carrier.getLastFix();
            return new CarrierView(
                    carrier.getId(),
                    carrier.getCode(),
                    carrier.getDisplayName(),
                    carrier.getIdentitySubject(),
                    carrier.getPhone(),
                    fix == null ? null : fix.latitude(),
                    fix == null ? null : fix.longitude(),
                    carrier.getLastFixAt()
            );
        }
    }

    public record RouteView(
            Long id,
            Long carrierId,
            String assignmentRef,
            String orderKind,
            Long orderId,
            String scope,
            String summary,
            String destinationLabel,
            BigDecimal latitude,
            BigDecimal longitude,
            String status,
            Instant assignedAt,
            Instant startedAt,
            Instant deliveredAt,
            Instant lastFixAt
    ) {
        public static RouteView from(CarrierRoute route) {
            GeoPoint destination = route.getDestination();
            return new RouteView(
                    route.getId(),
                    route.getCarrierId(),
                    route.getAssignmentRef(),
                    route.getOrderKind().name(),
                    route.getOrderId(),
                    route.getScope().name(),
                    route.getSummary(),
                    route.getDestinationLabel(),
                    destination == null ? null : destination.latitude(),
                    destination == null ? null : destination.longitude(),
                    route.getStatus().name(),
                    route.getAssignedAt(),
                    route.getStartedAt(),
                    route.getDeliveredAt(),
                    route.getLastFixAt()
            );
        }
    }

    public record InboxView(Long carrierId, String carrierCode, String carrierName, List<RouteView> routes) {
    }

    public record TrackingPolicyView(long intervalSeconds, long freshnessSeconds) {
    }

    public record OriginView(
            String orderKind,
            Long orderId,
            String label,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
    }

    public record NearbyCarrierView(
            Long carrierId,
            String code,
            String displayName,
            long distanceMeters,
            Instant lastFixAt,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        static NearbyCarrierView from(NearbyRanking.Ranked ranked) {
            return new NearbyCarrierView(
                    ranked.carrierId(),
                    ranked.code(),
                    ranked.displayName(),
                    ranked.distanceMeters(),
                    ranked.recordedAt(),
                    ranked.point().latitude(),
                    ranked.point().longitude()
            );
        }
    }

    public record NearbyResponse(OriginView origin, List<NearbyCarrierView> carriers) {
    }

    public record DepartureView(
            String orderKind,
            Long orderId,
            String label,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        static DepartureView from(OrderDeparture departure) {
            GeoPoint point = departure.point();
            return new DepartureView(
                    departure.ref().kind().name(),
                    departure.ref().id(),
                    departure.label(),
                    point == null ? null : point.latitude(),
                    point == null ? null : point.longitude()
            );
        }
    }
}
