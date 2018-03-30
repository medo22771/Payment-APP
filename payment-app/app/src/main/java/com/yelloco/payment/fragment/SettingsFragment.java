package com.yelloco.payment.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yelloco.payment.DevicePreferences;
import com.yelloco.payment.R;
import com.yelloco.payment.UserPreference;
import com.yelloco.payment.UserPreferenceDialogFragmentCompat;
import com.yelloco.payment.utils.Utils;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.device_preferences, rootKey);

        initSummary(getPreferenceScreen());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setEmailVerification();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference pref) {
        if (pref == null) return;

        if (pref instanceof EditTextPreference) {
            Log.d("SettingsFragment", "Pref: " + pref.getKey());
            EditTextPreference listPref = (EditTextPreference) pref;
            if (listPref.getText() != null && listPref.getKey().equals(DevicePreferences.EMAIL_PASSWORD.preferenceKey)) {
                listPref.setSummary(listPref.getText().replaceAll(".", "*"));
            } else {
                listPref.setSummary(listPref.getText());
            }

        }

    }

    private void setEmailVerification() {
        Preference pref = findPreference(DevicePreferences.EMAIL_ADDRESS.preferenceKey);

        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("SettingsFragment", "On preference change");
                if (!Utils.isValidEmail((String) newValue)) {
                    Utils.showAlert(R.string.receipt_email_wrong_format, getContext());
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof UserPreference) {
            dialogFragment = UserPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
