package com.yelloco.payment.utils;

import com.alcineo.utils.common.StringUtils;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public enum TlvTagEnum {

    TRACK_2_DATA("57"),
    PAN("5A"),
    SERVICE_CODE("5F30"),
    CARD_SEQUENCE_NUMBER("5F34"),
    TRANSACTION_STATUS("9B"),
    AMOUNT_AUTHORISED("9F02"),
    TRANSACTION_TIME("9F21"),
    APP_EXPIRATION_DATE("5F24"),
    TRANSACTION_RESULT("CF"),
    CH_NAME("5F20"),
    PIN_DATA("99"),

    APPLICATION_CRYPTO("9F26"),
    CRYPTOGRAM_INFO("9F27"),
    ISSUER_APP_DATA("9F10"),
    UNPREDICTABLE_NB("9F37"),
    APPLICATION_TRANSACTION_COUNTER("9F36"),
    TERMINAL_VERIFICATION_RESULTS("95"),
    TRANSACTION_DATE("9A"),
    TRANSACTION_TYPE("9C"),
    AMOUNT_AUTHORIZED("9F02"),
    TRANSACTION_CURRENCY_CODE("5F2A"),
    APPLICATION_INTERCHANGE_PROFILE("82"),
    TERMINAL_COUNTRY_CODE("9F1A"),
    CVM_RESULTS("9F34"),
    TERMINAL_CAPABILITIES("9F33"),

    DEDICATED_FILE("84"),

    PLAIN_CARD_DATA("DFDE00"),
    ENCRYPT_ALGO("DFDE01"),
    INITIALIZATION_VECTOR("DFDE02"),
    ;

    private String mTag;

    public static TlvTagEnum fromBytes(byte[] bytes) throws RuntimeException{
        for (TlvTagEnum tag : TlvTagEnum.values()) {
            if (StringUtils.convertBytesToHex(bytes).equalsIgnoreCase(tag.mTag)) {
                return tag;
            }
        }
        throw new RuntimeException("TLV Tag not recognised");
    }

    public static TlvTagEnum fromString(String tag) {
        for (TlvTagEnum b : TlvTagEnum.values()) {
            if (b.mTag.equalsIgnoreCase(tag)) {
                return b;
            }
        }
        return null;
    }

    TlvTagEnum(String tag) {
        mTag = tag;
    }

    public String getTag() {
        return mTag;
    }
}