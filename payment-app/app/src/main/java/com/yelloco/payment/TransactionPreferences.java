package com.yelloco.payment;

import android.content.SharedPreferences;

public enum TransactionPreferences {

    AUTH_ONLY("AUTH_ONLY"),
    CASHBACK("CASHBACK"),
    DEFERRED_PAYMENT("DEFERRED_PAYMENT"),
    FORCE_ONLINE("FORCE_ONLINE"),
    INCREASED_AMOUNT("INCREASED_AMOUNT"),
    PAYMENT("PAYMENT"),
    REFUND("REFUND");

    public final String preferenceKey;

    TransactionPreferences(String preferenceKey) {
        this.preferenceKey = preferenceKey;
    }

    public boolean getValue(SharedPreferences prefs) {
        return prefs.getBoolean(this.preferenceKey, false);
    }

    public void setValue(SharedPreferences prefs, boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(this.preferenceKey, value);
        editor.apply();
    }

    public static String dumpValues(SharedPreferences prefs) {
        StringBuilder builder = new StringBuilder("TransactionPreferences: ");
        for (TransactionPreferences pref: TransactionPreferences.values()) {
            builder.append("\n  ");
            builder.append(pref.preferenceKey);
            builder.append(" : ");
            builder.append(prefs.getBoolean(pref.preferenceKey, false));
        }
        return builder.toString();
    }

    public static boolean isInitialized(SharedPreferences prefs) {
        for (TransactionPreferences pref: TransactionPreferences.values()) {
            if (prefs.getBoolean(pref.preferenceKey, false))
                return true;
        }
        return false;
    }
}
