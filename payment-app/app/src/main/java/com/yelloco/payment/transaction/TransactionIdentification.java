package com.yelloco.payment.transaction;

import java.util.Date;

/**
 * This class holds the data for the transaction identification. At least contains the minimum
 * data to store the transaction to be cancelled.
 */
public class TransactionIdentification {

    private Date transactionDate;

    private int transactionReference;

    public TransactionIdentification(Date transactionDate, int transactionReference) {
        this.transactionDate = transactionDate;
        this.transactionReference = transactionReference;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(int transactionReference) {
        this.transactionReference = transactionReference;
    }
}