package com.yelloco.payment;

import android.content.SharedPreferences;

import java.util.Set;

public class CustomEditor implements SharedPreferences.Editor {

    int storedValue = 0;

    public int getStoredValue() {
        return this.storedValue;
    }

    @Override
    public SharedPreferences.Editor putString(String s, String s1) {
        return null;
    }

    @Override
    public SharedPreferences.Editor putStringSet(String s, Set<String> set) {
        return null;
    }

    @Override
    public SharedPreferences.Editor putInt(String s, int i) {
        this.storedValue = i;
        return null;
    }

    @Override
    public SharedPreferences.Editor putLong(String s, long l) {
        return null;
    }

    @Override
    public SharedPreferences.Editor putFloat(String s, float v) {
        return null;
    }

    @Override
    public SharedPreferences.Editor putBoolean(String s, boolean b) {
        return null;
    }

    @Override
    public SharedPreferences.Editor remove(String s) {
        return null;
    }

    @Override
    public SharedPreferences.Editor clear() {
        return null;
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public void apply() {

    }
}