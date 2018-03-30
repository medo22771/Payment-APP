package com.yelloco.payment.transaction;

import com.alcineo.transaction.TransactionManager;
import com.alcineo.transaction.TransactionType;
import com.yelloco.payment.data.tagstore.TagReader;
import com.yelloco.payment.utils.YelloCurrency;

import java.math.BigDecimal;
import java.util.Date;

public interface LoadedTransactionContext {

    Date getTransactionDateAndTime();

    TransactionReferencePersistence getTransactionReferencePersistence();

    TagReader getTagStore();

    String getCategoryCode();

    TransactionIdentification getTransactionToCancel();

    TransactionType getTransactionType();

    BigDecimal getAmount();

    YelloCurrency getCurrency();

    TransactionResult getTransactionResult();

    TransactionManager.TransactionStatus getTransactionStatus();

    String getReceipt();

    BigDecimal getIncreasedAmount();

    BigDecimal getAmountOther();
}