package com.yelloco.payment.data;

import static com.yelloco.payment.data.PaymentContentProvider.CONTENT_URI_TRANSACTION;
import static com.yelloco.payment.data.db.DbConstants.AMOUNT;
import static com.yelloco.payment.data.db.DbConstants.AUTH_CODE;
import static com.yelloco.payment.data.db.DbConstants.CANCELLATION_FLAG;
import static com.yelloco.payment.data.db.DbConstants.CARD_NUMBER;
import static com.yelloco.payment.data.db.DbConstants.DB_CURRENCY_CODE;
import static com.yelloco.payment.data.db.DbConstants.DB_ENCRYPT_ALGO;
import static com.yelloco.payment.data.db.DbConstants.DB_INITIALIZATION_VECTOR;
import static com.yelloco.payment.data.db.DbConstants.PROTOCOL;
import static com.yelloco.payment.data.db.DbConstants.RECEIPT;
import static com.yelloco.payment.data.db.DbConstants.RECEIPT_SIGNATURE_FILE;
import static com.yelloco.payment.data.db.DbConstants.SIMPLE_DATE_FORMAT;
import static com.yelloco.payment.data.db.DbConstants.STATUS;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_DATETIME;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_REFERENCE;
import static com.yelloco.payment.data.db.DbConstants.TYPE;
import static com.yelloco.payment.utils.TlvTagEnum.AMOUNT_AUTHORISED;
import static com.yelloco.payment.utils.TlvTagEnum.ENCRYPT_ALGO;
import static com.yelloco.payment.utils.TlvTagEnum.INITIALIZATION_VECTOR;
import static com.yelloco.payment.utils.TlvTagEnum.PAN;
import static com.yelloco.payment.utils.TlvTagEnum.PLAIN_CARD_DATA;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_CURRENCY_CODE;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_TYPE;
import static com.yelloco.payment.utils.Utils.bytesToHex;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yelloco.payment.BaseActivity;
import com.yelloco.payment.data.tagstore.TagReader;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.utils.TlvTagEnum;
import com.yelloco.payment.utils.TlvUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class SaveTransactionTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "SaveTransactionTask";

    private static final Bitmap.CompressFormat SIGNATURE_FILE_FORMAT = Bitmap.CompressFormat.PNG;

    private Context mContext;
    private TransactionContext mTransactionContext;
    private Bitmap mSignature;

    public SaveTransactionTask(TransactionContext transactionContext, Bitmap signature, Context context) {
        mContext = context;
        mTransactionContext = transactionContext;
        mSignature = signature;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(mContext instanceof BaseActivity){
            ((BaseActivity)mContext).showLoading(true);
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(mContext instanceof BaseActivity){
            ((BaseActivity)mContext).showLoading(false);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        ContentResolver resolver = mContext.getContentResolver();

        ContentValues cv = createContentValues();
        Uri inserted = resolver.insert(CONTENT_URI_TRANSACTION, cv);

        // Store signature to file and add it's path to the transaction record in the database
        if (mSignature != null) {
            String fileName = "receipt_"
                    + inserted.getLastPathSegment()
                    + "." + SIGNATURE_FILE_FORMAT.name();
            String signFilePath = saveFile(mSignature, fileName);
            if (signFilePath != null) {
                cv.put(RECEIPT_SIGNATURE_FILE, signFilePath);
                resolver.update(inserted, cv, null, null);
            }
        }

        return true;
    }

    @NonNull
    public ContentValues createContentValues() {
        ContentValues cv = new ContentValues();

        TagReader tagReader = mTransactionContext.getTagStore();
        Log.d(TAG, "TLV: " + TlvUtils.listTags(tagReader));
        Map<String, byte[]> requiredTags = tagReader.getTags(
                new String[]{PAN.getTag(), TRANSACTION_TYPE.getTag(), AMOUNT_AUTHORISED.getTag(),
                        TRANSACTION_CURRENCY_CODE.getTag(), PLAIN_CARD_DATA.getTag(),
                        INITIALIZATION_VECTOR.getTag(), ENCRYPT_ALGO.getTag()});
        Log.d(TAG, "TLV items: " + requiredTags.size());

        for (String tag : requiredTags.keySet()) {
            String hexValue = bytesToHex(requiredTags.get(tag));
            switch (TlvTagEnum.fromString(tag)) {
                case PAN:
                    Log.d(TAG, "PAN: " + hexValue);
                    StringBuilder panBuilder = new StringBuilder(hexValue);
                    panBuilder.replace(4, 11, "****");
                    cv.put(CARD_NUMBER, panBuilder.toString());
                    break;
                case TRANSACTION_TYPE:
                    Log.d(TAG, "TRANSACTION TYPE: " + hexValue);
                    cv.put(TYPE, hexValue);
                    break;
                case AMOUNT_AUTHORISED:
                    Log.d(TAG, "AMOUNT_AUTHORISED: " + hexValue);
                    cv.put(AMOUNT, hexValue);
                    break;
                case TRANSACTION_CURRENCY_CODE:
                    Log.d(TAG, "CURRENCY CODE: " + hexValue);
                    cv.put(DB_CURRENCY_CODE, hexValue);
                    break;
                case INITIALIZATION_VECTOR:
                    Log.d(TAG, "INITIALIZATION VECTOR: " + hexValue);
                    cv.put(DB_INITIALIZATION_VECTOR, hexValue);
                    break;
                case ENCRYPT_ALGO:
                    Log.d(TAG, "ENCRYPT ALGO: " + hexValue);
                    cv.put(DB_ENCRYPT_ALGO, hexValue);
                    break;
            }
        }

        if (mTransactionContext.getTransactionResult() != null) {
            cv.put(STATUS, mTransactionContext.getTransactionResult().getResult());
        }
        cv.put(AUTH_CODE, "00");
        cv.put(TRANSACTION_DATETIME, getDateString());
        cv.put(CANCELLATION_FLAG, false);
        cv.put(PROTOCOL, "epas");
        cv.put(RECEIPT, mTransactionContext.getReceipt());
        cv.put(TRANSACTION_REFERENCE,
                mTransactionContext.getTransactionReferencePersistence().getCurrentValue());
        return cv;
    }

    private String getDateString() {
        Date d = mTransactionContext.getTransactionDateAndTime();
        return SIMPLE_DATE_FORMAT.format(d);
    }

    private String saveFile(Bitmap bitmap, String name) {
        FileOutputStream out = null;
        try {
            File file = new File(mContext.getFilesDir(), "receipt_signatures/" + name);
            if (!file.exists())
                file.getParentFile().mkdirs();

            out = new FileOutputStream(file);
            bitmap.compress(SIGNATURE_FILE_FORMAT, 100, out);
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception: ", e);
            }
        }
        return null;
    }
}
