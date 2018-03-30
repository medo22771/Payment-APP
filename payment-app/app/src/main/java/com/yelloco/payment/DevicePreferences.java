package com.yelloco.payment;

import android.content.SharedPreferences;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public enum DevicePreferences {

    EMAIL_HOST("EMAIL_HOST"),
    EMAIL_ADDRESS("EMAIL_ADDRESS"),
    EMAIL_PASSWORD("EMAIL_PASSWORD"),
    EMAIL_PORT("EMAIL_PORT");

    public final String preferenceKey;

    DevicePreferences(String preferenceKey) {
        this.preferenceKey = preferenceKey;
    }

    public String getValue(SharedPreferences prefs) {
        return prefs.getString(this.preferenceKey, "");
    }

    public static String dumpValues(SharedPreferences prefs) {
        StringBuilder builder = new StringBuilder("DevicePreferences: ");
        for (DevicePreferences pref : DevicePreferences.values()) {
            builder.append("\n  ");
            builder.append(pref.preferenceKey);
            builder.append(" : ");
            builder.append(prefs.getBoolean(pref.preferenceKey, false));
        }
        return builder.toString();
    }

}
