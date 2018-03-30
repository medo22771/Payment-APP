package com.yelloco.payment.data;

import static com.yelloco.nexo.crypto.StringUtil.hexStringToBytes;
import static com.yelloco.payment.data.PaymentContentProvider.CONTENT_URI_TRANSACTION;
import static com.yelloco.payment.transaction.TransactionResult.APPROVED;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;

import com.yelloco.payment.MainActivity;
import com.yelloco.payment.data.tagstore.EmvTagStore;
import com.yelloco.payment.data.tagstore.TagStore;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.transaction.TransactionReferencePersistence;

import org.robolectric.Robolectric;

abstract class TransactionTestHelper {

    static final String SOME_COOL_TEXT = "some cool_text";
    static final String EXPECTED_PAN = "5413330089020011";
    static final String MASKED_PAN = "5413****20011";
    static final String EXPECTED_AMOUNT = "000000000024";
    static final String PURCHASE_TX_TYPE = "00";
    static final int MAXIMUM_VALUE = 666;
    static final int CURRENT_REFERENCE_VALUE = 50;

    TagStore tagStore;
    TransactionContext transactionContext;
    ContentResolver mContentResolver;
    MainActivity mMainActivity;

    public TransactionTestHelper() {
        mMainActivity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .resume()
                .get();
        mContentResolver = mMainActivity.getContentResolver();

        tagStore = new EmvTagStore();
        createDataToSave();
        createContext();
    }

    void createContext() {
        transactionContext = new TransactionContext();
        transactionContext.createOrUpdateContext(tagStore);
        transactionContext.addToReceipt(SOME_COOL_TEXT);
        transactionContext.setTransactionReferencePersistence(
                new FakeTransactionReferencePersistence(MAXIMUM_VALUE));
    }

    void createDataToSave() {
        tagStore.setTag("9F02", hexStringToBytes(EXPECTED_AMOUNT));
        tagStore.setTag("9F26", hexStringToBytes("4E51D7A9FDD374CD"));
        tagStore.setTag("9F27", hexStringToBytes("80"));
        tagStore.setTag("9F36", hexStringToBytes("0027"));
        tagStore.setTag("9C", hexStringToBytes(PURCHASE_TX_TYPE));
        tagStore.setTag("5A", hexStringToBytes(EXPECTED_PAN));
        tagStore.setTag("9F34", hexStringToBytes("420300"));
        tagStore.setTag("CF", hexStringToBytes(APPROVED.getCode()));
        tagStore.setTag("95", hexStringToBytes("08A0240000"));
        tagStore.setTag("9F1A", hexStringToBytes("0840"));
        tagStore.setTag("5F2A", hexStringToBytes("0978"));
    }

    long storeContentValues(ContentValues contentValues) {
        return ContentUris.parseId(
                mContentResolver.insert(CONTENT_URI_TRANSACTION, contentValues));
    }

    class FakeTransactionReferencePersistence extends TransactionReferencePersistence {

        FakeTransactionReferencePersistence(int maximumValue) {
            super(maximumValue);
        }

        @Override
        protected int getNewValue() {
            return 0;
        }

        @Override
        public int getCurrentValue() {
            return CURRENT_REFERENCE_VALUE;
        }

        @Override
        protected void setValue(int value) {

        }
    }
}