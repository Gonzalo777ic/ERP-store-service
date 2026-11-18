package com.valeerp.dispatch;

/**
 * Distancia geodésica. El seno exige un double interno; el resultado es metros enteros.
 */
public final class GeoDistance {

    /** Radio medio terrestre usado solo para ordenar cercanía. */
    public static final double EARTH_RADIUS_METERS = 6_371_000d;

    private GeoDistance() {
    }

    public static long meters(GeoPoint from, GeoPoint to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("La distancia exige dos puntos");
        }
        double lat1 = Math.toRadians(from.latitude().doubleValue());
        double lat2 = Math.toRadians(to.latitude().doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(to.longitude().subtract(from.longitude()).doubleValue());
        double haversine = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double arc = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return Math.round(EARTH_RADIUS_METERS * arc);
    }
}
