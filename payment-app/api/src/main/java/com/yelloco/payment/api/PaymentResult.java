package com.yelloco.payment.api;

/**
 * Result of the payment transaction.
 */
public enum PaymentResult {

    /**
     * Transaction was approved.
     */
    APPROVED,
    /**
     * Transaction was declined.
     */
    DECLINED,
    /**
     * Transaction was cancelled by user.
     */
    CANCELLED,
    /**
     * There was some error during transaction, check for {@link ErrorCode}
     */
    ERROR,
    /**
     * Transaction finished with timeout.
     */
    TIMEOUT
}
