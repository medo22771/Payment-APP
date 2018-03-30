package com.yelloco.payment.host;

import com.alcineo.transaction.events.OnlineRequestEvent;
import com.alcineo.transaction.events.OnlineReversalEvent;
import com.yelloco.payment.transaction.LoadedTransactionContext;
import com.yelloco.payment.transaction.TransactionReferencePersistence;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface HostManager {

    /**
     * Sends the transaction online. Particular HostManager implementations must handle the specific
     * online behaviour.
     * @param transactionContext
     */
    void sendTransaction(LoadedTransactionContext transactionContext, OnlineRequestEvent onlineRequestEvent) throws IOException;

    /**
     * Sends the common reversal message specifically to the concrete HostManager implementation
     * @param transactionContext
     * @param onlineReversalEvent
     * @throws IOException
     */
    void sendReversal(LoadedTransactionContext transactionContext, OnlineReversalEvent onlineReversalEvent)
            throws IOException, GeneralSecurityException;

    /**
     * Sends the cancellation message to the host
     * @param transactionContext
     */
    void sendCancelTransaction(LoadedTransactionContext transactionContext);

    /**
     * Returns the reference on the current transaction to be further used by other facilities
     * @return
     */
    TransactionReferencePersistence getTransactionReference();
}