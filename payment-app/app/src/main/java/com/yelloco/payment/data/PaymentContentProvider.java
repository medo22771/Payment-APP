package com.yelloco.payment.data;

import static com.yelloco.payment.data.db.DbConstants.ID;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.yelloco.payment.data.db.DatabaseHelper;
import com.yelloco.payment.data.db.table.Transaction;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class PaymentContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.yelloco.payment.contentprovider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri CONTENT_URI_TRANSACTION = Uri.parse(CONTENT_URI + "/transaction");

    private static final int CODE_URI_TRANSACTION = 1;
    private static final int CODE_URI_TRANSACTION_ID = 2;

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = "PaymentContentProvider";

    static {
        matcher.addURI(AUTHORITY, CONTENT_URI_TRANSACTION.getPath().substring(1), CODE_URI_TRANSACTION);
        matcher.addURI(AUTHORITY, CONTENT_URI_TRANSACTION.getPath().substring(1) + "/#", CODE_URI_TRANSACTION_ID);
    }

    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(this.getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        Log.d(TAG, "DATABASE - Query for: " + uri.toString());

        String limit = null;

        int uriType = matcher.match(uri);
        switch (uriType) {
            case CODE_URI_TRANSACTION_ID:
                if (selection != null)
                    selection = selection + "_ID = " + uri.getLastPathSegment();
                else
                    selection = ID + " = " + uri.getLastPathSegment();
            case CODE_URI_TRANSACTION:
                queryBuilder.setTables(Transaction.TABLE_TRANSACTION);
                break;
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        Cursor cursor;
        if (limit != null) {
            cursor = queryBuilder.query(db, projection, selection, selectionArgs,
                    null, null, sortOrder, limit);
        } else
            cursor = queryBuilder.query(db, projection, selection, selectionArgs,
                    null, null, sortOrder);


        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        Log.d(TAG, "Loaded from database: " + cursor.getCount());

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(TAG, "DATABASE - Insert for: " + uri.toString());
        int uriType = matcher.match(uri);
        SQLiteDatabase sqlDB = mDatabaseHelper.getWritableDatabase();
        Uri ret = null;
        long id = 0;
        switch (uriType) {
            case CODE_URI_TRANSACTION:
                id = sqlDB.insert(Transaction.TABLE_TRANSACTION, ID, values);
                ret = ContentUris.withAppendedId(CONTENT_URI_TRANSACTION, id);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "DATABASE - Delete for: " + uri.toString());
        int uriType = matcher.match(uri);
        SQLiteDatabase sqlDB = mDatabaseHelper.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case CODE_URI_TRANSACTION:
                rowsDeleted = sqlDB.delete(Transaction.TABLE_TRANSACTION, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        Log.d(TAG, "DATABASE - Update for: " + uri.toString());
        int uriType = matcher.match(uri);
        SQLiteDatabase sqlDB = mDatabaseHelper.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType) {
            case CODE_URI_TRANSACTION_ID:
                if (selection != null)
                    selection = selection + "_ID = " + uri.getLastPathSegment();
                else
                    selection = ID + " = " + uri.getLastPathSegment();
            case CODE_URI_TRANSACTION:
                rowsUpdated = sqlDB.update(Transaction.TABLE_TRANSACTION, values, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
