package com.yelloco.payment.data;

import com.yelloco.payment.transaction.LoadedTransactionContext;

import java.util.List;

public interface PersistenceManager {

    /**
     * Loads the last transaction. Usually for cancellation purposes.
     * @return
     */
    LoadedTransactionContext getLastTransaction();

    /**
     * Loads selected transaction into the context
     * @param transactionId
     * @return
     */
    LoadedTransactionContext getTransaction(String transactionId);

    /**
     * Load all stored transactions
     * @return
     */
    List<LoadedTransactionContext> getAll();
}