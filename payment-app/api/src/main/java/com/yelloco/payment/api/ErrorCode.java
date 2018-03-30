package com.yelloco.payment.api;

/**
 * Type of error received from the payment transaction.
 * Type is {@link ErrorCode#NO_ERROR} when {@link PaymentResult} is different than ERROR.
 */
public enum ErrorCode {

    /**
     * No error
     */
    NO_ERROR,
    /**
     * Unknown error
     */
    UNKNOWN_ERROR
}
