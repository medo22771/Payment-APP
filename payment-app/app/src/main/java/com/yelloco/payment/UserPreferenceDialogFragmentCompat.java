package com.yelloco.payment;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;

/**
 * Created by sylchoquet on 20/09/17.
 */

public class UserPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private EditText mEditTextLogin;
    private EditText mEditTextPassword;

    public UserPreferenceDialogFragmentCompat() {
    }

    public static UserPreferenceDialogFragmentCompat newInstance(String key) {
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        UserPreferenceDialogFragmentCompat fragment = new UserPreferenceDialogFragmentCompat();
        fragment.setArguments(args);
        return fragment;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEditTextLogin = (EditText) view.findViewById(R.id.edt_login);
        mEditTextPassword = (EditText) view.findViewById(R.id.edt_password);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DialogPreference preference = getPreference();
            if (preference instanceof UserPreference) {
                UserPreference userPreference =
                        ((UserPreference) preference);
                userPreference.saveUserToDatabase(mEditTextLogin.getText().toString(), mEditTextPassword.getText().toString());
            }
        }
    }
}
