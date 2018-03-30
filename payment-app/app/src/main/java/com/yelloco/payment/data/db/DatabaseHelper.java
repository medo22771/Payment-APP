package com.yelloco.payment.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.yelloco.payment.data.UserDAO;
import com.yelloco.payment.data.db.table.Transaction;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "payment.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating database");
        Transaction.onCreate(db);
        UserDAO.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Transaction.onUpgrade(db, oldVersion, newVersion);
        UserDAO.onUpgrade(db, oldVersion, newVersion);
    }
}
