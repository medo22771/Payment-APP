package com.yelloco.payment;

import android.content.SharedPreferences;

/**
 * Created by sylchoquet on 25/10/17.
 */

public enum NexoAcquirerPreferences {

    /**
     * Header values
     */

    PROTOCOL_VERSION("PROTOCOL_VERSION", String.class, "2.0"),
    EXCHANGE_ID("EXCHANGE_ID", String.class, "149"),
    INIT_PARTY_ID("INIT_PARTY_ID", String.class, "66000001"),
    RCPT_PARTY_ID("RCPT_PARTY_ID", String.class, "nexo-acquirer-1"),

    // POI

    POI_ID("POI_ID", String.class, "1"),
    IP_ADDRESS("IP_ADDRESS", String.class, "192.168.0.1"),

    //CARDHOLDER

    ADDRESS_DIGITS("ADDRESS_DIGITS", String.class, "112"),
    POSTAL_CODE_DIGITS("POSTAL_CODE_DIGITS",String.class,"75002"),

    // MERCHANT

    MERCHANT_NAME("MERCHANT_NAME", String.class, "Darty"),
    MERCHANT_ADDR("MERCHANT_ADDR", String.class, "112 Rue Marchand"),
    MERCHANT_COUNTRY_CODE("MERCHANT_COUNTRY_CODE", String.class, "FRA"),
    MERCHANT_PHONE("MERCHANT_PHONE", String.class, "0123456789"),
    MERCHANT_CITY("MERCHANT_CITY", String.class, "Paris"),
    MERCHANT_ZIP("MERCHANT_ZIP", String.class, "75002"),
    MERCHANT_SUB_ID("MERCHANT_SUB_ID", String.class, "0"),

    TERMINAL_MODEL("TERMINAL_MODEL", String.class, "1234"),
    PROVIDER_ID("PROVIDER_ID", String.class, "PROVIDER_1"),
    MAC_ADDRESS("MAC_ADDRESS", String.class, "8c8404564c01"),
    SERIAL("SERIAL", String.class, "1546879141544874"),
    IMEI("IMEI", String.class, "FERS65AEGUNRKJMZ"),

    DISPLAY_CAP_LINE_WIDTH("DISPLAY_CAP_LINE_WIDTH", String.class, "10"),
    DISPLAY_CAP_LINE_NB("DISPLAY_CAP_LINE_NB", String.class, "10"),

    ATTENDANCE_CONTEXT("ATTENDANCE_CONTEXT", Boolean.class, true),

    //CLIENT (SAFECHARGE SPECIFIC)
    SAFECHARGE_CLIENT_LOGIN_ID("CLIENT_LOGIN_ID", String.class, "0"),
    SAFECHARGE_CLIENT_PASSWORD("CLIENT_PASSWORD", String.class, "0"),
    SAFECHARGE_WEBSITE("WEBSITE", String.class, "0"),
    SAFECHARGE_USER_ID("USER_ID", String.class, "0"),
    SAFECHARGE_VERSION("SAFECHARGE_VERSION", String.class, "4.0.6"),
    SAFECHARGE_RESPONSE_FORMAT("USER_ID", String.class, "4"),
    ;

    private String preferenceKey;
    private Class type;
    private Object defaultValue;

    NexoAcquirerPreferences(String preferenceKey, Class type, Object defaultValue) {
        this.preferenceKey = preferenceKey;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getString(SharedPreferences prefs) {
        if (!type.getName().equals(String.class.getName()))
            throw new RuntimeException("Incompatible type of preference");
        return prefs.getString(preferenceKey, (String) defaultValue);
    }

    public Boolean getBool(SharedPreferences prefs) {
        if (!type.getName().equals(Boolean.class.getName()))
            throw new RuntimeException("Incompatible type of preference");
        return prefs.getBoolean(preferenceKey, (Boolean) defaultValue);
    }

    public <T> void setValue(SharedPreferences prefs, T value) {
        if (type.getSimpleName().equals(String.class.getSimpleName())) {
            prefs.edit().putString(preferenceKey, (String) value).apply();
        } else if (type.getSimpleName().equals(Boolean.class.getSimpleName())) {
            prefs.edit().putBoolean(preferenceKey, (Boolean) value).apply();
        } else
            throw new RuntimeException("Setter for this type is not implemented");

    }

    public String getName() {
        return preferenceKey;
    }
}
