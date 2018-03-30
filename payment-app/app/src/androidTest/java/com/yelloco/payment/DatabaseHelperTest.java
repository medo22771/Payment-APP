package com.yelloco.payment;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;
import com.yelloco.payment.data.db.DatabaseHelper;
import org.junit.Assert;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class DatabaseHelperTest extends AndroidTestCase {
    private static final String TAG = "Databasetest";

    private DatabaseHelper db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new DatabaseHelper(context);
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        super.tearDown();
    }

    public void testDatabaseName() {
        Log.d(TAG, db.getDatabaseName());
        Assert.assertTrue(db.getDatabaseName().equals("payment.db"));
    }

}
