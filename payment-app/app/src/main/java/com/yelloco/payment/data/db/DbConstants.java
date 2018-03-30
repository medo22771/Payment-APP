package com.yelloco.payment.data.db;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class DbConstants {
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static final String ID = "_id";
    public static final String TYPE = "type";
    public static final String STATUS = "status";
    public static final String TRANSACTION_DATETIME = "transaction_datetime";
    public static final String AMOUNT = "amount";
    public static final String CARD_NUMBER = "transaction_card_number";
    public static final String CANCELLATION_FLAG = "transaction_cancellation_flag";
    public static final String PSP_TRANSACTION_ID = "psp_transaction_id";
    public static final String AUTH_CODE = "auth_code";
    public static final String PROTOCOL = "protocol";
    public static final String RECEIPT = "receipt";
    public static final String RECEIPT_SIGNATURE_FILE = "receipt_signature_file";
    public static final String TRANSACTION_REFERENCE = "transaction_reference";
    public static final String DB_INITIALIZATION_VECTOR = "initialization_vector";
    public static final String DB_ENCRYPT_ALGO = "encrypt_algo";
    public static final String DB_CURRENCY_CODE = "currency_code";
}