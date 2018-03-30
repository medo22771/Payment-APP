package com.yelloco.payment.fragment;

import static com.yelloco.payment.fragment.ReceiptFragment.IMAGE_SIGNATURE;
import static com.yelloco.payment.transaction.TransactionResult.APPROVED;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.blackcat.currencyedittext.CurrencyEditText;
import com.yelloco.payment.MainActivity;
import com.yelloco.payment.R;
import com.yelloco.payment.data.PaymentContentProvider;
import com.yelloco.payment.data.PersistenceManager;
import com.yelloco.payment.data.PersistenceManagerImpl;
import com.yelloco.payment.data.db.DbConstants;
import com.yelloco.payment.host.HostManager;
import com.yelloco.payment.transaction.LoadedTransactionContext;
import com.yelloco.payment.transaction.TransactionResult;

import java.math.BigDecimal;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class TransactionDetailDialogFragment extends DialogFragment {

    private DrawerLayout mDrawer;
    private MainActivity MainActObj;

    private static final String KEY_ID = "key_id";

    private long mTransID = 150;

    private CurrencyEditText mAmount;
    private ImageView mStatus;
    private TextView mCancelBtn;
    private TextView mRefundBtn;
    private TextView mReceiptBtn;
    private TextView mNotesBtn;
    private TextView mGenQRCBtn;

    private String mReceipt;
    private Bitmap mSignature;

    private TransactionDetailListener mListener;
    private PersistenceManager mPersistenceManager;
    private HostManager mHostManager;

    //private TransCodeDelivery ConnectorObj;

    public interface TransactionDetailListener {

        void onRefundTransaction(BigDecimal amount);
    }

//    public interface TransCodeDelivery
//    {
//        void fromTDetailDialogToMain(long code);
//    }

    private class GetTransactionTask extends AsyncTask<Void, Void, Void> {

        String amount;
        String status;
        String notes;

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver resolver = getContext().getContentResolver();

            String uri = PaymentContentProvider.CONTENT_URI_TRANSACTION
                    .toString()
                    + "/" + mTransID;

            Cursor transaction = resolver.query(Uri.parse(uri), null, null, null, null);
            transaction.moveToFirst();

            String strAmount = transaction.getString(transaction.getColumnIndex(DbConstants.AMOUNT));
            amount = String.valueOf(Double.parseDouble(strAmount) / 100.00);
            status = transaction.getString(transaction.getColumnIndex(DbConstants.STATUS));
            mReceipt = transaction.getString(transaction.getColumnIndex(DbConstants.RECEIPT));
            String signaturePath = transaction.getString(transaction.getColumnIndex(DbConstants.RECEIPT_SIGNATURE_FILE));
            // TODO: add get notes

            if (signaturePath != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                mSignature = BitmapFactory.decodeFile(signaturePath, options);
            }

            transaction.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            TransactionResult transactionResult = TransactionResult.getByResult(status);
            if (transactionResult != null && transactionResult == APPROVED) {
                mStatus.setImageDrawable(getContext().getDrawable(R.drawable.button_ok_bis));
                mStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                mStatus.setImageDrawable(getContext().getDrawable(R.drawable.button_cancel_bis));
                mStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            mAmount.append(amount);
        }

    }

    static TransactionDetailDialogFragment newInstance(long id) {
        TransactionDetailDialogFragment fragment = new TransactionDetailDialogFragment();

        Bundle b = new Bundle();
        b.putLong(KEY_ID, id);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActObj = new MainActivity();

        Bundle b = getArguments();
        mTransID = b.getLong(KEY_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_transaction_detail, container, false);

        mAmount = (CurrencyEditText) v.findViewById(R.id.amount);
        mStatus = (ImageView) v.findViewById(R.id.status);
        mNotesBtn = (TextView) v.findViewById(R.id.button_notes);
        mReceiptBtn = (TextView) v.findViewById(R.id.button_receipt);
        mRefundBtn = (TextView) v.findViewById(R.id.button_refund);
        mCancelBtn = (TextView) v.findViewById(R.id.button_cancel);
        mGenQRCBtn = (TextView) v.findViewById(R.id.button_QRCode);

        mNotesBtn.setOnClickListener(notImplementedClick);

        final LoadedTransactionContext lastTransaction = mPersistenceManager.getTransaction(
                String.valueOf(mTransID));
        TransactionResult transactionResult = lastTransaction.getTransactionResult();
        if (transactionResult != null && transactionResult == APPROVED) {
            mCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHostManager.sendCancelTransaction(lastTransaction);
                }
            });
        } else {
            mCancelBtn.setVisibility(View.INVISIBLE);
        }

        mReceiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceiptFragment fragment = new ReceiptFragment();

                Bundle b = new Bundle();
                b.putString(ReceiptFragment.TEXT_RECEIPT, mReceipt);
                b.putBoolean(ReceiptFragment.FLAG_HISTORY_TRANSACTION, true);
                b.putParcelable(IMAGE_SIGNATURE, mSignature);
                fragment.setArguments(b);

                getFragmentManager().beginTransaction()
                        .replace(R.id.main_container, fragment)
                        .addToBackStack(null)
                        .commit();
                dismiss();
            }
        });

        mRefundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRefundTransaction(new BigDecimal(mAmount.getRawValue()));
                dismiss();
            }
        });

        v.findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mGenQRCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGenQRCBtn_fn();
            }
        });

        return v;
    }

    public void mGenQRCBtn_fn()
    {
        mTransID = 150;
        if(mTransID != 0)
        {
            SharedPreferences TransactionIDPref = getActivity().getSharedPreferences("TransactionIDDB", Context.MODE_PRIVATE);
            SharedPreferences.Editor QREditor = TransactionIDPref.edit();
            QREditor.putString("QRCode", "" + mTransID);
            QREditor.apply();

            QRCodeGenFragment nextFrag = new QRCodeGenFragment();
            MainActObj.switchFragment(nextFrag, false);
//            mDrawer.closeDrawer(GravityCompat.START);

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TransactionDetailListener) {
            mListener = (TransactionDetailListener) context;
            mPersistenceManager = new PersistenceManagerImpl(context.getContentResolver());
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        try
        {
            Log.i("Error", "TransactionDetailDialogFragment0001");
            //ConnectorObj = (TransCodeDelivery) context;
        }
        catch(Exception e)
        {}


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private View.OnClickListener notImplementedClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getContext(), "Feature not implemented yet", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        new GetTransactionTask().execute();
    }

    public void setHostManager(HostManager hostManager) {
        this.mHostManager = hostManager;
    }

}
