package com.yelloco.payment.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.yelloco.payment.data.db.DatabaseHelper;
import com.yelloco.payment.data.db.model.User;
import com.yelloco.payment.utils.Utils;

/**
 * Created by sylchoquet on 15/09/17.
 */

public class UserDAO {

    public static final String TABLE_USER = "userTable";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";


    private static final String DATABASE_CREATE = "create table "
            + TABLE_USER
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_USERNAME + " text not null, "
            + COLUMN_PASSWORD + " text not null "
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
    }

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        database.close();
        dbHelper.close();
    }

    public long insert(User user) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, Utils.getHashFromString(user.getPassword()));
        return database.insert(TABLE_USER, null, values);
    }

    public boolean exists(String username, String password) {
        password = Utils.getHashFromString(password);
        Cursor cursor = database.query(
                TABLE_USER,
                new String[]{COLUMN_USERNAME, COLUMN_PASSWORD},
                "username = ? AND password = ?",
                new String[]{username, password},
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUsername(cursor.getString(0));
        user.setPassword(cursor.getString(1));
        return user;
    }
}
