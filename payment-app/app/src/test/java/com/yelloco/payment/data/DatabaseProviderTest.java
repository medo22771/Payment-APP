package com.yelloco.payment.data;

import static com.yelloco.payment.data.PaymentContentProvider.AUTHORITY;
import static com.yelloco.payment.data.PaymentContentProvider.CONTENT_URI_TRANSACTION;
import static com.yelloco.payment.data.db.DbConstants.AMOUNT;
import static com.yelloco.payment.data.db.DbConstants.AUTH_CODE;
import static com.yelloco.payment.data.db.DbConstants.CANCELLATION_FLAG;
import static com.yelloco.payment.data.db.DbConstants.CARD_NUMBER;
import static com.yelloco.payment.data.db.DbConstants.ID;
import static com.yelloco.payment.data.db.DbConstants.STATUS;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_DATETIME;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_REFERENCE;
import static com.yelloco.payment.data.db.DbConstants.TYPE;
import static com.yelloco.payment.transaction.TransactionResult.APPROVED;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static java.lang.String.valueOf;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.test.ProviderTestCase2;

import com.yelloco.payment.BuildConfig;
import com.yelloco.payment.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class DatabaseProviderTest extends ProviderTestCase2<PaymentContentProvider> {

    public static final String BEST_TRANSACTION_REFERENCE_NUMBER_EVER = "666";
    private ContentResolver mContentResolver;

    public DatabaseProviderTest() {
        super(PaymentContentProvider.class, AUTHORITY);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MainActivity mainActivity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .resume()
                .get();
        mContentResolver = mainActivity.getContentResolver();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testInsert() {
        assertTrue(insertCorrect() > -1);
    }

    @Test
    public void testInsertMissedRequired() {
        ContentValues cv = createPartialContentValues("1234********6789");

        long id = ContentUris.parseId(
                mContentResolver.insert(CONTENT_URI_TRANSACTION, cv));

        assertFalse(id > -1);
    }

    @Test
    public void testDelete() {
        insertCorrect();

        int deleted = mContentResolver.delete(CONTENT_URI_TRANSACTION, null,
                null);

        assertTrue(deleted == 1);
    }

    @Test
    public void testQueryAll() {
        insertCorrect();
        insertCorrect();

        Cursor cursor = mContentResolver.query(CONTENT_URI_TRANSACTION,
                null, null, null, null);

        assertThat(cursor.getCount(), is(2));
    }

    @Test
    public void testQueryByID() {
        insertCorrect();

        // Save second transaction with different card number
        String cardNumber = "9876********4321";
        long id = insertCorrect(createContentValues(cardNumber));

        // Query transaction with ID = 2
        Cursor cursor = null;
        if (id != -1) {
            cursor = mContentResolver.query(CONTENT_URI_TRANSACTION,
                    null, ID + "=?", new String[]{valueOf(id)}, null);
        }

        assertThat(cursor.getCount(), is(1));
        assertTrue(cardNumber.equals(getColumnValue(cursor, CARD_NUMBER)));
    }

    @Test
    public void canTransactionReferenceBeLoadedWithOtherDataWhenStoredWithContext() {
        long transactionId = insertCorrect();

        Cursor cursor = null;
        if (transactionId != -1) {
            cursor = mContentResolver.query(CONTENT_URI_TRANSACTION,
                    null, ID + "=?", new String[]{valueOf(transactionId)}, null);
        }

        assertThat(BEST_TRANSACTION_REFERENCE_NUMBER_EVER,
                is(equalTo(getColumnValue(cursor, TRANSACTION_REFERENCE))));
    }

    private String getColumnValue(Cursor cursor, String columnIndex) {
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(columnIndex));
    }

    private long insertCorrect() {
        return insertCorrect(null);
    }

    private long insertCorrect(ContentValues contentValues) {
        if (contentValues == null) {
            contentValues = createContentValues("1234********6789");
        }
        return ContentUris.parseId(
                mContentResolver.insert(CONTENT_URI_TRANSACTION, contentValues));
    }

    @NonNull
    private ContentValues createPartialContentValues(String value2) {
        ContentValues cv = new ContentValues();
        cv.put(AMOUNT, "000000010");
        cv.put(CARD_NUMBER, value2);
        return cv;
    }

    @NonNull
    private ContentValues createContentValues(String cardNumber) {
        ContentValues cv = createPartialContentValues(cardNumber);
        cv.put(TYPE, "Auth");
        cv.put(TRANSACTION_DATETIME, new Date().toString());
        cv.put(STATUS, APPROVED.getResult());
        cv.put(AUTH_CODE, "00");
        cv.put(CANCELLATION_FLAG, false);
        cv.put(TRANSACTION_REFERENCE, BEST_TRANSACTION_REFERENCE_NUMBER_EVER);
        return cv;
    }
}