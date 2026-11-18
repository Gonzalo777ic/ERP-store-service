package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchDomainTest {

    private static final Duration INTERVAL = Duration.ofMinutes(5);
    private static final Instant T0 = Instant.parse("2026-11-18T15:00:00Z");

    @Test
    void routeGoesFromAssignedToOnTheWayToDelivered() {
        CarrierRoute route = route();
        route.start(T0);
        route.deliver(T0.plusSeconds(60));
        assertEquals(RouteStatus.ENTREGADO, route.getStatus());
    }

    @Test
    void rejectsLocationUntilTheRouteIsOnTheWay() {
        CarrierRoute route = route();
        DomainException error = assertThrows(DomainException.class,
                () -> route.acceptFix(GeoPoint.of(BigDecimal.ZERO, BigDecimal.ZERO), T0, INTERVAL));
        assertEquals(CarrierRoute.NOT_IN_PROGRESS, error.code());
    }

    @Test
    void acceptsFirstFixAndRejectsAnotherInsideTheInterval() throws Exception {
        CarrierRoute route = route();
        route.start(T0);
        stampId(route);
        route.acceptFix(GeoPoint.of(BigDecimal.ONE, BigDecimal.ONE), T0, INTERVAL);
        DomainException error = assertThrows(DomainException.class, () -> route.acceptFix(
                GeoPoint.of(new BigDecimal("1.1"), new BigDecimal("1.1")),
                T0.plus(INTERVAL).minusSeconds(1),
                INTERVAL));
        assertEquals(CarrierRoute.TOO_SOON, error.code());
        route.acceptFix(GeoPoint.of(new BigDecimal("1.2"), new BigDecimal("1.2")), T0.plus(INTERVAL), INTERVAL);
        assertEquals(T0.plus(INTERVAL), route.getLastFixAt());
    }

    @Test
    void rejectsLocationAfterDelivery() throws Exception {
        CarrierRoute route = route();
        route.start(T0);
        stampId(route);
        route.acceptFix(GeoPoint.of(BigDecimal.ZERO, BigDecimal.ZERO), T0, INTERVAL);
        route.deliver(T0.plus(INTERVAL));
        DomainException error = assertThrows(DomainException.class, () -> route.acceptFix(
                GeoPoint.of(BigDecimal.ONE, BigDecimal.ONE),
                T0.plus(INTERVAL).plus(INTERVAL),
                INTERVAL));
        assertEquals(CarrierRoute.NOT_IN_PROGRESS, error.code());
        assertEquals(T0, route.getLastFixAt());
    }

    @Test
    void coordinateKeepsFixedScaleAndRange() {
        GeoPoint point = GeoPoint.of(new BigDecimal("1.23456789"), new BigDecimal("-77.0427934"));
        assertEquals(new BigDecimal("1.234568"), point.latitude());
        assertEquals(GeoPoint.SCALE, point.longitude().scale());
        assertThrows(DomainException.class, () -> GeoPoint.of(new BigDecimal("91"), BigDecimal.ZERO));
    }

    @Test
    void ranksRecentCarriersByDistanceAndDropsStaleFixes() {
        GeoPoint origin = GeoPoint.of(BigDecimal.ZERO, BigDecimal.ZERO);
        Instant now = T0.plus(Duration.ofMinutes(40));
        List<NearbyRanking.Ranked> ranked = NearbyRanking.rank(origin, List.of(
                new NearbyRanking.Fix(2L, "LEJOS", "Lejos", GeoPoint.of(BigDecimal.ONE, BigDecimal.ZERO), now.minusSeconds(60)),
                new NearbyRanking.Fix(1L, "CERCA", "Cerca", GeoPoint.of(new BigDecimal("0.01"), BigDecimal.ZERO), now),
                new NearbyRanking.Fix(3L, "VIEJO", "Viejo", GeoPoint.of(BigDecimal.ZERO, BigDecimal.ZERO), now.minus(Duration.ofMinutes(31)))
        ), Duration.ofMinutes(30), now);
        assertEquals(List.of(1L, 2L), ranked.stream().map(NearbyRanking.Ranked::carrierId).toList());
        assertTrue(ranked.get(0).distanceMeters() < ranked.get(1).distanceMeters());
    }

    private static CarrierRoute route() {
        return CarrierRoute.open(
                "asg-1",
                OrderRefKind.CUSTOMER_ORDER,
                9L,
                4L,
                DeliveryScope.PARCIAL,
                "Dos sacos",
                "Jr. Lima 100",
                GeoPoint.of(new BigDecimal("-12.046374"), new BigDecimal("-77.042793")),
                T0);
    }

    private static void stampId(CarrierRoute route) throws Exception {
        var id = CarrierRoute.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(route, 15L);
    }
}
