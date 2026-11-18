package com.valeerp.dispatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * Intervalo de ubicación y vigencia de «reciente». Una sola política, leída de configuración.
 */
@Component
public class DispatchPolicy {

    private final Duration locationInterval;
    private final Duration locationFreshness;

    public DispatchPolicy(
            @Value("${valeerp.dispatch.location-interval}") String locationInterval,
            @Value("${valeerp.dispatch.location-freshness}") String locationFreshness) {
        this.locationInterval = parse(locationInterval, "valeerp.dispatch.location-interval");
        this.locationFreshness = parse(locationFreshness, "valeerp.dispatch.location-freshness");
    }

    public Duration locationInterval() {
        return locationInterval;
    }

    public Duration locationFreshness() {
        return locationFreshness;
    }

    private static Duration parse(String raw, String key) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Falta " + key);
        }
        try {
            Duration duration = Duration.parse(raw.trim());
            if (duration.isZero() || duration.isNegative()) {
                throw new IllegalStateException(key + " debe ser una duración positiva");
            }
            return duration;
        } catch (DateTimeParseException ex) {
            throw new IllegalStateException("Duración inválida en " + key, ex);
        }
    }
}
