package com.yelloco.payment.transaction;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesTransactionReferencePersistence extends
        TransactionReferencePersistence {

    private static final String KEY_REF = "STAN";
    private final SharedPreferences sharedPref;

    public SharedPreferencesTransactionReferencePersistence(Context context,
            String sharedPrefsFileName, int maximumValue) {
        super(maximumValue);
        sharedPref = context.getSharedPreferences(sharedPrefsFileName, Context.MODE_PRIVATE);
    }

    @Override
    protected int getNewValue() {
        return sharedPref.getInt(KEY_REF, 0) + 1;
    }

    @Override
    public int getCurrentValue() {
        return sharedPref.getInt(KEY_REF, 0);
    }

    @Override
    protected void setValue(int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_REF, value);
        editor.apply();
    }
}