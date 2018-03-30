package com.yelloco.payment.fragment;

import static com.yelloco.payment.fragment.ReceiptFragment.IMAGE_SIGNATURE;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.yelloco.payment.R;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.data.SaveTransactionTask;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class SignatureFragment extends BaseFragment implements SignaturePad.OnSignedListener {

    private static final String TAG = "SignatureFragment";

    private SignaturePad mSignaturePad;

    private Bitmap mCapturedSignature;
    private TransactionContext mTransactionContext;

    public SignatureFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sign, container, false);
        TextView signAgreementText = (TextView) view.findViewById(R.id.text_signature_agree);
        Button confirmBtn = (Button) view.findViewById(R.id.btn_confirm_sign);

        mTransactionContext = transactionProvider.getTransactionContext();

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCapturedSignature != null) {
                    saveAndProceed();
                }
            }
        });

        mSignaturePad = (SignaturePad) view.findViewById(R.id.signature_pad);

        BigDecimal amount = mTransactionContext.getAmount();
        amount = amount.setScale(2, RoundingMode.DOWN);

        signAgreementText.setText(getString(R.string.label_signature_agreement,
                mTransactionContext.getCurrency().getAlphabeticCode(),
                amount.toString()));

        mSignaturePad.setOnSignedListener(this);

        return view;
    }

    private void saveAndProceed() {
            new SaveTransactionTask(mTransactionContext, mCapturedSignature, getActivity()).execute();

        Bundle bundle = getArguments();
        bundle.putParcelable(IMAGE_SIGNATURE, mCapturedSignature);

        BaseFragment fragment = BaseFragment.newInstance(ReceiptFragment.class, bundle);
        getFragmentManager().beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    @Override
    public void onStartSigning() {

    }

    @Override
    public void onSigned() {
        Log.d(TAG, "Signed");
        mCapturedSignature = mSignaturePad.getTransparentSignatureBitmap(true);
    }

    @Override
    public void onClear() {
        Log.d(TAG, "Signature cleared");
        mCapturedSignature = null;
    }

}
