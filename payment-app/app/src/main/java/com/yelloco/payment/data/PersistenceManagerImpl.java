package com.yelloco.payment.data;

import static com.yelloco.payment.data.PaymentContentProvider.CONTENT_URI_TRANSACTION;
import static com.yelloco.payment.data.db.DbConstants.ID;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_DATETIME;

import static java.lang.String.valueOf;

import android.content.ContentResolver;
import android.database.Cursor;

import com.yelloco.payment.transaction.LoadedTransactionContext;

import java.util.ArrayList;
import java.util.List;

public class PersistenceManagerImpl implements PersistenceManager {

    private ContentResolver mContentResolver;
    private PersistenceConverter mPersistenceConverter;

    public PersistenceManagerImpl(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
        this.mPersistenceConverter = new PersistenceConverter();
    }

    @Override
    public LoadedTransactionContext getLastTransaction() {
        Cursor cursor = mContentResolver.query(CONTENT_URI_TRANSACTION, null, null, null,
                TRANSACTION_DATETIME + " DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return this.mPersistenceConverter.convertCursorToContext(cursor);
    }

    @Override
    public LoadedTransactionContext getTransaction(String transactionId) {
        Cursor cursor = mContentResolver.query(CONTENT_URI_TRANSACTION, null, ID + "=?",
                new String[]{valueOf(transactionId)}, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return this.mPersistenceConverter.convertCursorToContext(cursor);
    }

    @Override
    public List<LoadedTransactionContext> getAll() {
        Cursor cursor = mContentResolver.query(CONTENT_URI_TRANSACTION, null, null, null, null);
        List<LoadedTransactionContext> mArrayList = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mArrayList.add(this.mPersistenceConverter.convertCursorToContext(cursor));
                cursor.moveToNext();
            }
        }
        return mArrayList;
    }
}