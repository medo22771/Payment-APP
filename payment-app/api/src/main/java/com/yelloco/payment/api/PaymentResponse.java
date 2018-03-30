package com.yelloco.payment.api;

import android.content.Intent;

/**
 * Payment response from YelloPay transaction.
 * At first android intent is received in android onActivityResult callback after
 * payment transaction is done. PaymentResponse object can be then obtained by
 * {@link PaymentResponse#fromIntent(Intent)}
 */
public class PaymentResponse {

    private static final String RESULT = "RESULT";
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String CARD_TYPE = "CARD_TYPE";
    private static final String MASKED_PAN = "MASKED_PAN";
    private static final String RECEIPT = "RECEIPT";

    private PaymentResult result;
    private ErrorCode errorCode;
    private CardType cardType;
    private String maskedPAN;
    private String receipt;

    /**
     * Extracts the PaymentResponse from android intent received as result of payment transaction
     * @param intent android intent containing all payment response data from the transaction
     * @return PaymentResponse
     */
    public static PaymentResponse fromIntent(Intent intent) {
        PaymentResponse response = new PaymentResponse();
        response.result = (PaymentResult) intent.getSerializableExtra(RESULT);
        response.errorCode = (ErrorCode) intent.getSerializableExtra(ERROR_CODE);
        response.cardType = (CardType) intent.getSerializableExtra(CARD_TYPE);
        response.maskedPAN = intent.getStringExtra(MASKED_PAN);
        response.receipt = intent.getStringExtra(RECEIPT);
        return response;
    }

    /**
     * Converts this object into android intent
     * @return android intent containing all payment response values
     */
    public Intent toIntent() {
        Intent intent = new Intent();
        intent.putExtra(RESULT, result);
        intent.putExtra(ERROR_CODE, errorCode);
        intent.putExtra(CARD_TYPE, cardType);
        intent.putExtra(MASKED_PAN, maskedPAN);
        intent.putExtra(RECEIPT, receipt);
        return intent;
    }

    public PaymentResult getResult() {
        return result;
    }

    public void setResult(PaymentResult result) {
        this.result = result;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getMaskedPAN() {
        return maskedPAN;
    }

    public void setMaskedPAN(String maskedPAN) {
        this.maskedPAN = maskedPAN;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "result=" + result +
                ", errorCode=" + errorCode +
                ", cardType=" + cardType +
                ", maskedPAN='" + maskedPAN + '\'' +
                ", receipt='" + receipt + '\'' +
                '}';
    }
}