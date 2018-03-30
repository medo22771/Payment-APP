package com.yelloco.payment.data.db.table;

import static com.yelloco.payment.data.db.DbConstants.AMOUNT;
import static com.yelloco.payment.data.db.DbConstants.AUTH_CODE;
import static com.yelloco.payment.data.db.DbConstants.CANCELLATION_FLAG;
import static com.yelloco.payment.data.db.DbConstants.CARD_NUMBER;
import static com.yelloco.payment.data.db.DbConstants.DB_CURRENCY_CODE;
import static com.yelloco.payment.data.db.DbConstants.DB_ENCRYPT_ALGO;
import static com.yelloco.payment.data.db.DbConstants.DB_INITIALIZATION_VECTOR;
import static com.yelloco.payment.data.db.DbConstants.ID;
import static com.yelloco.payment.data.db.DbConstants.PROTOCOL;
import static com.yelloco.payment.data.db.DbConstants.PSP_TRANSACTION_ID;
import static com.yelloco.payment.data.db.DbConstants.RECEIPT;
import static com.yelloco.payment.data.db.DbConstants.RECEIPT_SIGNATURE_FILE;
import static com.yelloco.payment.data.db.DbConstants.STATUS;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_DATETIME;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_REFERENCE;
import static com.yelloco.payment.data.db.DbConstants.TYPE;

import android.database.sqlite.SQLiteDatabase;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class Transaction {

    public static final String TABLE_TRANSACTION = "transactionTable";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_TRANSACTION
            + "("
            + ID + " integer primary key autoincrement,"
            + TYPE + " text not null, "
            + STATUS + " text not null, "
            + TRANSACTION_DATETIME + " datetime not null, "
            + AMOUNT + " text, "
            + CARD_NUMBER + " text not null, "
            + CANCELLATION_FLAG + " bit  null ,"
            + PSP_TRANSACTION_ID + "  text ,"
            + AUTH_CODE + "  text ,"
            + PROTOCOL + "  text ,"
            + RECEIPT + " text ,"
            + RECEIPT_SIGNATURE_FILE + " text ,"
            + TRANSACTION_REFERENCE + " text ,"
            + DB_INITIALIZATION_VECTOR + " text ,"
            + DB_ENCRYPT_ALGO + " text ,"
            + DB_CURRENCY_CODE + " text "
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
    }
}
