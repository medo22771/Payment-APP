package com.yelloco.payment.data;

import static junit.framework.TestCase.assertNotNull;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import static java.lang.String.valueOf;

import com.yelloco.payment.BuildConfig;
import com.yelloco.payment.MainActivity;
import com.yelloco.payment.transaction.LoadedTransactionContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class PersistenceManagerTest extends TransactionTestHelper {

    private PersistenceManager mPersistenceManager;
    private SaveTransactionTask saveTransactionTask;

    @Before
    public void setUp() throws Exception {
        MainActivity mainActivity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .resume()
                .get();
        mPersistenceManager = new PersistenceManagerImpl(mContentResolver);

        saveTransactionTask = new SaveTransactionTask(transactionContext, null, mainActivity);
    }

    @Test
    public void canLoadAllTransactionsBeLoadedWhenPreviouslySaved() {
        storeContentValues(saveTransactionTask.createContentValues());
        storeContentValues(saveTransactionTask.createContentValues());

        assertThat(mPersistenceManager.getAll().size(), is(equalTo(2)));
    }

    @Test
    public void canSpecificTransactionBeLoadedByIdWhenPreviouslySaved() {
        long id = storeContentValues(saveTransactionTask.createContentValues());

        assertThat(mPersistenceManager.getTransaction(valueOf(id)), is(notNullValue()));
    }

    @Test
    public void canSpecificTransactionBeLoadedByIdWhenMultipleTransactionsPreviouslySaved() {
        storeContentValues(saveTransactionTask.createContentValues());
        long id = storeContentValues(saveTransactionTask.createContentValues());
        storeContentValues(saveTransactionTask.createContentValues());

        assertThat(mPersistenceManager.getTransaction(valueOf(id)), is(notNullValue()));
    }

    @Test
    public void canLoadLastTransactionBeLoadedWhenPreviouslySaved() {
        storeContentValues(saveTransactionTask.createContentValues());

        LoadedTransactionContext loadedTransactionContext =
                mPersistenceManager.getLastTransaction();

        assertNotNull(loadedTransactionContext);
    }

    @Test
    public void isLastTransactionLoadedWithHighestDateWhenPreviouslySaved() {
        storeContentValues(saveTransactionTask.createContentValues());
        shiftDay(-1);
        storeContentValues(saveTransactionTask.createContentValues());

        Date highestDate = getHighestDate(mPersistenceManager.getAll());
        Date actualDate = mPersistenceManager.getLastTransaction().getTransactionDateAndTime();

        assertThat(actualDate, is(equalTo(highestDate)));
    }

    private Date getHighestDate(List<LoadedTransactionContext> loadedTransactionContextList) {
        Date highestDate = loadedTransactionContextList.get(0).getTransactionDateAndTime();
        for (LoadedTransactionContext loadedTransactionContext : loadedTransactionContextList) {
            Date transactionDateAndTime = loadedTransactionContext.getTransactionDateAndTime();
            if (transactionDateAndTime.after(highestDate)) {
                highestDate = transactionDateAndTime;
            }
        }
        return highestDate;
    }

    private void shiftDay(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, amount);
        transactionContext.setTransactionDateAndTime(calendar.getTime());
    }
}