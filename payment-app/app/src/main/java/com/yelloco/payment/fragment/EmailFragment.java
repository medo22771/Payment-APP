package com.yelloco.payment.fragment;

import static com.yelloco.payment.fragment.ReceiptFragment.TEXT_RECEIPT;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.yelloco.payment.R;
import com.yelloco.payment.email.MailSender;
import com.yelloco.payment.utils.Utils;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class EmailFragment extends BaseFragment {

    private EditText mEmailInput;

    private ProgressDialog mProgressDialog;

    public EmailFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_email, container, false);

        mEmailInput = (EditText) view.findViewById(R.id.edit_address);
        String emailFromApi = transactionProvider.getTransactionContext().getEmail();
        if (emailFromApi != null && !emailFromApi.equals("")) {
            mEmailInput.setText(emailFromApi);
        }
        mEmailInput.requestFocus();

        mEmailInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendEmail();
                    return true;
                }

                return false;
            }
        });

        Button mSendBtn = (Button) view.findViewById(R.id.btn_val_email);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        Button mCancelBtn = (Button) view.findViewById(R.id.btn_cancel_email);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                getFragmentManager().popBackStackImmediate();
            }
        });

        return view;
    }

    private void sendEmail() {
        if (!Utils.isValidEmail(mEmailInput.getText().toString())) {
            Utils.showAlert(R.string.receipt_email_wrong_format, getContext());
            return;
        }

        new SendMailTask(mEmailInput.getText().toString()).execute();
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    /**
     * Show progress dialog.
     */
    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity(), R.style.DialogTheme);

            mProgressDialog.setTitle(R.string.dialog_email_progress_title);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getText(R.string.dialog_progress_message));
            mProgressDialog.show();
        }
    }


    /**
     * Hide progress dialog.
     */
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private class SendMailTask extends AsyncTask<Void, Void, Boolean> {

        private String mEmail;

        public SendMailTask(String email) {
            mEmail = email;
        }

        @Override
        protected void onPreExecute() {
            showLoading(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            MailSender sender = new MailSender(getContext());
            return sender.sendMail(getString(R.string.receipt_email_subject),
                    getArguments().getString(TEXT_RECEIPT, ""), mEmail);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showLoading(false);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showLoading(false);
            if (!result) {
                Toast.makeText(getContext(), R.string.receipt_email_fail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.receipt_email_success, Toast.LENGTH_SHORT).show();
            }
            transactionProvider.finishTransaction();
        }
    }

}
