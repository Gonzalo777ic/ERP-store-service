package com.valeerp.dispatch;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Ordena transportistas con ubicación vigente. La pantalla no calcula la distancia.
 */
public final class NearbyRanking {

    private NearbyRanking() {
    }

    public record Fix(long carrierId, String code, String displayName, GeoPoint point, Instant recordedAt) {
    }

    public record Ranked(long carrierId, String code, String displayName, long distanceMeters,
                         Instant recordedAt, GeoPoint point) {
    }

    public static List<Ranked> rank(GeoPoint origin, List<Fix> fixes, Duration freshness, Instant now) {
        if (origin == null || freshness == null || now == null) {
            throw new IllegalArgumentException("La cercanía exige origen, vigencia y reloj");
        }
        Instant cutoff = now.minus(freshness);
        return fixes.stream()
                .filter(fix -> fix.point() != null && fix.recordedAt() != null && !fix.recordedAt().isBefore(cutoff))
                .map(fix -> new Ranked(
                        fix.carrierId(),
                        fix.code(),
                        fix.displayName(),
                        GeoDistance.meters(origin, fix.point()),
                        fix.recordedAt(),
                        fix.point()))
                .sorted(Comparator.comparingLong(Ranked::distanceMeters))
                .toList();
    }
}
