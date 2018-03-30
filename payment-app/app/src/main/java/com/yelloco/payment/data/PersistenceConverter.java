package com.yelloco.payment.data;

import static com.yelloco.payment.data.db.DbConstants.AMOUNT;
import static com.yelloco.payment.data.db.DbConstants.DB_CURRENCY_CODE;
import static com.yelloco.payment.data.db.DbConstants.DB_ENCRYPT_ALGO;
import static com.yelloco.payment.data.db.DbConstants.DB_INITIALIZATION_VECTOR;
import static com.yelloco.payment.data.db.DbConstants.SIMPLE_DATE_FORMAT;
import static com.yelloco.payment.data.db.DbConstants.STATUS;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_DATETIME;
import static com.yelloco.payment.data.db.DbConstants.TRANSACTION_REFERENCE;
import static com.yelloco.payment.data.db.DbConstants.TYPE;
import static com.yelloco.payment.utils.TlvTagEnum.AMOUNT_AUTHORISED;
import static com.yelloco.payment.utils.TlvTagEnum.ENCRYPT_ALGO;
import static com.yelloco.payment.utils.TlvTagEnum.INITIALIZATION_VECTOR;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_RESULT;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_TYPE;

import android.database.Cursor;
import android.util.Log;

import com.alcineo.transaction.TransactionType;
import com.yelloco.payment.data.tagstore.EmvTagStore;
import com.yelloco.payment.data.tagstore.TagStore;
import com.yelloco.payment.transaction.LoadedTransactionContext;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.transaction.TransactionIdentification;
import com.yelloco.payment.transaction.TransactionResult;
import com.yelloco.payment.utils.TlvTagEnum;
import com.yelloco.payment.utils.YelloCurrency;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PersistenceConverter {

    private static final String TAG = "PersistenceManagerImpl";

    public LoadedTransactionContext convertCursorToContext(Cursor cursor) {
        TransactionContext transactionContext = null;

        if (cursor != null && cursor.getCount() > 0) {
            transactionContext = new TransactionContext();
            Map<String, Integer> columnsMap = convertCursorToMap(cursor);

            String stringDate = cursor.getString(columnsMap.get(TRANSACTION_DATETIME));
            Date transactionDate = null;
            try {
                transactionDate = SIMPLE_DATE_FORMAT.parse(stringDate);
            } catch (ParseException e) {
                Log.e(TAG, "Cannot convert database date: " + stringDate);
            }
            transactionContext.setTransactionDateAndTime(transactionDate);
            transactionContext.setTransactionType(TransactionType.PURCHASE);

            TagStore tagStore = new EmvTagStore();

            setTagStoreValue(TRANSACTION_TYPE, cursor.getString(columnsMap.get(TYPE)), tagStore);
            setTagStoreValue(INITIALIZATION_VECTOR,
                    cursor.getString(columnsMap.get(DB_INITIALIZATION_VECTOR)), tagStore);
            setTagStoreValue(ENCRYPT_ALGO, cursor.getString(columnsMap.get(DB_ENCRYPT_ALGO)),
                    tagStore);
            String stringAmount = cursor.getString(columnsMap.get(AMOUNT));
            BigDecimal amount = BigDecimal.valueOf(Long.valueOf(stringAmount));
            transactionContext.setAmount(amount);
            setTagStoreValue(AMOUNT_AUTHORISED, cursor.getString(columnsMap.get(AMOUNT)),
                    tagStore);
            TransactionResult savedResult = TransactionResult.getByResult(
                    cursor.getString(columnsMap.get(STATUS)));
            if (savedResult != null) {
                setTagStoreValue(TRANSACTION_RESULT, savedResult.getCode(), tagStore);
                transactionContext.setTransactionResult(savedResult);
            }
            String stringCurrencyCode = cursor.getString(columnsMap.get(DB_CURRENCY_CODE));
            if (stringCurrencyCode == null) {
                transactionContext.setCurrency(YelloCurrency.EUR);
            } else {
                transactionContext.setCurrency(
                        YelloCurrency.getByCurrencyNumericCode(stringCurrencyCode));
            }

            transactionContext.createOrUpdateContext(tagStore);

            String transactionReferenceString = cursor.getString(
                    columnsMap.get(TRANSACTION_REFERENCE));
            if (transactionReferenceString != null) {
                TransactionIdentification transactionIdentification = new TransactionIdentification(
                        transactionDate, Integer.valueOf(transactionReferenceString));
                transactionContext.setTransactionToCancel(transactionIdentification);
            }
        }

        return transactionContext;
    }

    private void setTagStoreValue(TlvTagEnum tlvTagEnum, String hexValue, TagStore tagStore) {
        if (hexValue == null || hexValue.isEmpty()) {
            return;
        }
        tagStore.setTag(tlvTagEnum.getTag(), hexValue);
    }

    private Map<String, Integer> convertCursorToMap(Cursor cursor) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            map.put(cursor.getColumnName(i), i);
        }
        return map;
    }
}