package com.valeerp.dispatch;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Coordenada geográfica. Escala fija; no es un texto libre ni un double persistido.
 */
@Embeddable
public class GeoPoint {

    public static final int SCALE = 6;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");

    @Column(precision = 9, scale = SCALE)
    private BigDecimal latitude;

    @Column(precision = 9, scale = SCALE)
    private BigDecimal longitude;

    protected GeoPoint() {
    }

    private GeoPoint(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static GeoPoint of(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new DomainException("La ubicación exige latitud y longitud");
        }
        BigDecimal lat = latitude.setScale(SCALE, ROUNDING);
        BigDecimal lon = longitude.setScale(SCALE, ROUNDING);
        if (lat.compareTo(MIN_LATITUDE) < 0 || lat.compareTo(MAX_LATITUDE) > 0) {
            throw new DomainException("La latitud está fuera de rango");
        }
        if (lon.compareTo(MIN_LONGITUDE) < 0 || lon.compareTo(MAX_LONGITUDE) > 0) {
            throw new DomainException("La longitud está fuera de rango");
        }
        return new GeoPoint(lat, lon);
    }

    public BigDecimal latitude() {
        return latitude;
    }

    public BigDecimal longitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GeoPoint point)) {
            return false;
        }
        return latitude.compareTo(point.latitude) == 0 && longitude.compareTo(point.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return latitude.stripTrailingZeros().hashCode() * 31 + longitude.stripTrailingZeros().hashCode();
    }
}
