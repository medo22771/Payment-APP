package com.yelloco.payment.data;

import static com.yelloco.payment.data.db.DbConstants.AMOUNT;
import static com.yelloco.payment.data.db.DbConstants.AUTH_CODE;
import static com.yelloco.payment.data.db.DbConstants.CANCELLATION_FLAG;
import static com.yelloco.payment.data.db.DbConstants.CARD_NUMBER;
import static com.yelloco.payment.data.db.DbConstants.PROTOCOL;
import static com.yelloco.payment.data.db.DbConstants.RECEIPT;
import static com.yelloco.payment.data.db.DbConstants.STATUS;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_DATETIME;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_REFERENCE;
import static com.yelloco.payment.data.db.DbConstants.TYPE;
import static com.yelloco.payment.transaction.TransactionResult.APPROVED;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import android.content.ContentValues;

import com.yelloco.payment.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class SaveTransactionTaskTest extends TransactionTestHelper {

    private SaveTransactionTask saveTransactionTask;

    @Before
    public void setUp() throws Exception {
        saveTransactionTask = new SaveTransactionTask(transactionContext, null, mMainActivity);
    }

    @Test
    public void contentValuesShouldNotBeNull() {
        ContentValues v = new ContentValues();

        assertNotNull(v);
    }

    @Test
    public void canContentValuesCreatedFromGivenContext() {
        ContentValues contentValues = saveTransactionTask.createContentValues();

        assertNotNull(contentValues);
        assertEquals(contentValues.get(CARD_NUMBER), MASKED_PAN);
        assertEquals(contentValues.get(TYPE), PURCHASE_TX_TYPE);
        assertEquals(contentValues.get(STATUS), APPROVED.getResult());
        assertEquals(contentValues.get(AUTH_CODE), "00");
        assertEquals(contentValues.get(AMOUNT), EXPECTED_AMOUNT);
        assertNotNull(contentValues.get(TRANSACTION_DATETIME));
        assertEquals(contentValues.get(CANCELLATION_FLAG), false);
        assertEquals(contentValues.get(PROTOCOL), "epas");
        assertEquals(contentValues.get(RECEIPT), SOME_COOL_TEXT);
    }

    @Test
    public void isCurrentTxReferenceValueStoredWhenPersistenceProvided() {
        ContentValues contentValues = saveTransactionTask.createContentValues();

        assertThat(contentValues.get(TRANSACTION_REFERENCE).toString(),
                is(equalTo(String.valueOf(CURRENT_REFERENCE_VALUE))));
    }
}