package com.valeerp.dispatch;

/**
 * Serie y límites de texto de la guía de remisión.
 * La política de ubicación del transportista vive en {@link DispatchPolicy}.
 */
public final class DispatchGuidePolicy {

    public static final String GUIDE_SERIES = "T001";

    public static final int GUIDE_NUMBER_WIDTH = 8;

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;

    public static final int SERIES_MAX_LENGTH = 8;

    public static final int NUMBER_MAX_LENGTH = 20;

    public static final int PARTY_NAME_MAX_LENGTH = 200;

    public static final int DOCUMENT_MAX_LENGTH = 20;

    public static final int ADDRESS_MAX_LENGTH = 300;

    public static final int NOTE_MAX_LENGTH = 1000;

    public static final int VOID_REASON_MAX_LENGTH = 500;

    public static final int MEDIUM_MAX_LENGTH = 120;

    public static final int PRODUCT_NAME_MAX_LENGTH = 200;

    private DispatchGuidePolicy() {
    }
}
