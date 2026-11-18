package com.valeerp.dispatch;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;

/**
 * Ficha del transportista en despacho. La credencial sigue en identidad:
 * aquí solo se guarda el subject.
 */
@Entity
@Table(name = "carriers")
public class Carrier extends SoftDeletable {

    public static final int CODE_MAX = 40;
    public static final int NAME_MAX = 160;
    public static final int SUBJECT_MAX = 180;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = CODE_MAX)
    private String code;

    @Column(name = "display_name", nullable = false, length = NAME_MAX)
    private String displayName;

    @Column(name = "identity_subject", nullable = false, length = SUBJECT_MAX)
    private String identitySubject;

    @Column(length = 40)
    private String phone;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "last_latitude", precision = 9, scale = GeoPoint.SCALE)),
            @AttributeOverride(name = "longitude", column = @Column(name = "last_longitude", precision = 9, scale = GeoPoint.SCALE))
    })
    private GeoPoint lastFix;

    @Column(name = "last_fix_at")
    private Instant lastFixAt;

    protected Carrier() {
    }

    public static Carrier register(String code, String displayName, String identitySubject, String phone) {
        Carrier carrier = new Carrier();
        carrier.code = normalizeCode(code);
        carrier.displayName = requireName(displayName);
        carrier.identitySubject = requireSubject(identitySubject);
        carrier.phone = blankToNull(phone);
        return carrier;
    }

    public void noteFix(GeoPoint point, Instant when) {
        if (point == null || when == null) {
            throw new DomainException("La ubicación reciente del transportista es obligatoria");
        }
        if (isRetired()) {
            throw new DomainException("El transportista está dado de baja");
        }
        this.lastFix = point;
        this.lastFixAt = when;
    }

    public void archive(Actor actor, Instant when) {
        retire(actor, when);
    }

    public boolean sessionMatches(String subject) {
        return identitySubject != null && subject != null && identitySubject.equals(subject);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIdentitySubject() {
        return identitySubject;
    }

    public String getPhone() {
        return phone;
    }

    public GeoPoint getLastFix() {
        return lastFix;
    }

    public Instant getLastFixAt() {
        return lastFixAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Carrier carrier)) {
            return false;
        }
        return id != null && id.equals(carrier.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código del transportista es obligatorio");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > CODE_MAX) {
            throw new DomainException("El código del transportista excede la longitud permitida");
        }
        return normalized;
    }

    private static String requireName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new DomainException("El nombre del transportista es obligatorio");
        }
        String trimmed = displayName.trim();
        if (trimmed.length() > NAME_MAX) {
            throw new DomainException("El nombre del transportista excede la longitud permitida");
        }
        return trimmed;
    }

    private static String requireSubject(String identitySubject) {
        if (identitySubject == null || identitySubject.isBlank()) {
            throw new DomainException("La ficha exige el identificador de identidad del transportista");
        }
        String trimmed = identitySubject.trim();
        if (trimmed.length() > SUBJECT_MAX) {
            throw new DomainException("El identificador de identidad excede la longitud permitida");
        }
        return trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 40) {
            throw new DomainException("El teléfono excede la longitud permitida");
        }
        return trimmed;
    }
}
