package com.yelloco.payment.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;
import com.yelloco.payment.MainActivity;
import com.yelloco.payment.R;

/**
 * Fragment responsible for displaying menu options received from payment framework.
 * User option choice is provided back payment framework.
 */
public class ReceiptFragment extends BaseFragment {

    private static final String TAG = ReceiptFragment.class.getSimpleName();

    public static final String TEXT_RESULT = "TEXT_RESULT";
    public static final String TEXT_RECEIPT = "TEXT_RECEIPT";
    public static final String IMAGE_SIGNATURE = "IMAGE_SIGNATURE";
    public static final String FLAG_HISTORY_TRANSACTION = "FLAG_TRANSACTION_HISTORY";
    private static final String KEY_ID = "key_id";
    private long TransID;

    private Button btnNoReceipt;
    private Button btnSMS;
    private Button btnEmail;
    private Button btnPrint;
    private Button btnGenQRC;

    private String receipt;

    private boolean mIsHistoryTransaction;

    public ReceiptFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        TransID = b.getLong(KEY_ID);
        //TransID = 150;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_receipt, container, false);
        TextView textResult = (TextView) view.findViewById(R.id.text_result);
        ImageView imageSignature = (ImageView) view.findViewById(R.id.image_signature);
        receipt = getArguments().getString(TEXT_RECEIPT, "");

        Bundle bundle = getArguments();
        if (bundle != null) {
            textResult.setText(getArguments().getString(TEXT_RESULT, ""));
        }
        TextView textReceipt = (TextView) view.findViewById(R.id.text_receipt);
        if (bundle != null) {
            textReceipt.setText(receipt);
        }
        if (bundle != null) {
            mIsHistoryTransaction = getArguments().getBoolean(FLAG_HISTORY_TRANSACTION, false);
        }
        if (bundle != null) {
            imageSignature.setImageBitmap((Bitmap)getArguments().getParcelable(IMAGE_SIGNATURE));
        }

        prepareButtons(view);
        return view;
    }

    private void prepareButtons(View view)
    {
        btnEmail = (Button) view.findViewById(R.id.btn_email);
        btnNoReceipt = (Button) view.findViewById(R.id.btn_noreceipt);
        btnSMS = (Button) view.findViewById(R.id.btn_sms);
        btnPrint = (Button) view.findViewById(R.id.btn_print);
        btnGenQRC = (Button) view.findViewById(R.id.btn_EReceiptBtn);

        //TODO implement particular functionalities, for now we only initialize new transaction
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionProvider.finishTransaction();
            }
        };
        if (mIsHistoryTransaction) {
            btnNoReceipt.setText(R.string.button_back);
            btnNoReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        } else {
            btnNoReceipt.setOnClickListener(listener);
        }
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailFragment fragment = new EmailFragment();
                fragment.setArguments(getArguments());

                getFragmentManager().beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        btnSMS.setOnClickListener(listener);

        if(((MainActivity)getActivity()).getPrinterManager().isConnect()) {
            btnPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        ((MainActivity)getActivity()).getPrinterManager().sendText(receipt);
                    } catch (PrinterException e) {
                        Log.e(TAG,e.getLocalizedMessage());
                        Toast.makeText(getActivity(),getString(R.string.printer_error_connect) +
                                "printer: " + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            btnPrint.setVisibility(View.GONE);
        }

        btnGenQRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenQRCImage();
            }
        });
    }

    public void GenQRCImage()
    {
        if(TransID != 0)
        {
            SharedPreferences TransactionIDPref = getActivity().getSharedPreferences("TransactionIDDB", Context.MODE_PRIVATE);
            SharedPreferences.Editor QREditor = TransactionIDPref.edit();
            QREditor.putString("QRCode", "" + TransID);
            QREditor.apply();

            QRCodeGenFragment nextFrag = new QRCodeGenFragment();
            MainActivity.switchFragment(nextFrag, false);

//-------------------------------------
//            QRCodeGenFragment nextFrag= new QRCodeGenFragment();
//            getActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.main_container, nextFrag)
//                    .addToBackStack(null)
//                    .commit();
//-------------------------------------
            //If The Above Part Doesn't Work Use This
//            QRCodeGenFragment fragment2=new QRCodeGenFragment();
//            FragmentManager fragmentManager=getActivity().getFragmentManager();
//            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.main_container,fragment2);
//            fragmentTransaction.commit();
//-------------------------------------
            //ConnectorObj.fromTDetailDialogToMain(mTransID);
        }
    }
}
