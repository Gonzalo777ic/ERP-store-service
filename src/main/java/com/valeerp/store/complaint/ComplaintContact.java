package com.valeerp.store.complaint;

import com.valeerp.kernel.DomainException;

/**
 * Límites y formato de los datos que la hoja pide al consumidor.
 */
final class ComplaintContact {

    static final int NAME_MAX = 200;
    static final int ADDRESS_MAX = 300;
    static final int EMAIL_MAX = 160;
    static final int PHONE_MAX = 30;
    static final int SUBJECT_MAX = 500;
    static final int REFERENCE_MAX = 80;
    static final int DETAIL_MAX = 2000;
    static final int RESPONSE_MAX = 2000;

    private static final int PHONE_DIGITS_MIN = 6;
    private static final int PHONE_DIGITS_MAX = 15;

    private ComplaintContact() {
    }

    static String required(String raw, String label, int max) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new DomainException(label + " es obligatorio");
        }
        String value = raw.trim();
        if (value.length() > max) {
            throw new DomainException(label + " admite como máximo " + max + " caracteres");
        }
        return value;
    }

    static String optional(String raw, String label, int max) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim();
        if (value.length() > max) {
            throw new DomainException(label + " admite como máximo " + max + " caracteres");
        }
        return value;
    }

    static String email(String raw) {
        String email = required(raw, "El correo", EMAIL_MAX).toLowerCase();
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        if (at <= 0 || at != email.lastIndexOf('@') || dot < at + 2 || dot == email.length() - 1) {
            throw new DomainException("El correo no es válido");
        }
        return email;
    }

    static String phone(String raw) {
        String phone = required(raw, "El teléfono", PHONE_MAX);
        int digits = 0;
        for (int i = 0; i < phone.length(); i++) {
            char character = phone.charAt(i);
            if (character >= '0' && character <= '9') {
                digits++;
            }
        }
        if (digits < PHONE_DIGITS_MIN || digits > PHONE_DIGITS_MAX) {
            throw new DomainException("El teléfono no es válido");
        }
        return phone;
    }
}
