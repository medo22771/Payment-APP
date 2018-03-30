package com.yelloco.payment.api;

import android.content.Intent;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Payment request for YelloPay transaction.
 * It has to be initialized, particular payment values set, transformed into android intent by
 * {@link PaymentRequest#toIntent()} method and used to startActivityForResult.
 */
public class PaymentRequest {

    public static final int PAYMENT_REQUEST_CODE = 1;

    public static final String ACTION_PAY = "com.yelloco.payment.api.PAY";

    private static final String AMOUNT = "AMOUNT";
    private static final String CASH_BACK = "CASH_BACK";
    private static final String TIP = "TIP";
    private static final String CURRENCY = "CURRENCY";
    private static final String EMAIL = "EMAIL";
    private static final String SMS = "SMS";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String BASKET_DATA = "BASKET_DATA";

    private BigDecimal amount = new BigDecimal("0.0");
    private BigDecimal cashBack = new BigDecimal("0.0");
    private BigDecimal tip = new BigDecimal("0.0");
    private Currency currency;
    private String email;
    private String sms;
    private String merchantId;
    private String basketData;

    /**
     * Extracts the PaymentRequest from android intent.
     * @param intent android intent including the payment request
     * @return PaymentRequest
     */
    public static PaymentRequest fromIntent(Intent intent) {
        PaymentRequest request = new PaymentRequest();
        request.amount = new BigDecimal(intent.getStringExtra(AMOUNT));
        request.cashBack = new BigDecimal(intent.getStringExtra(CASH_BACK));
        request.tip = new BigDecimal(intent.getStringExtra(TIP));
        request.currency = Currency.getInstance(intent.getStringExtra(CURRENCY));
        request.email = intent.getStringExtra(EMAIL);
        request.sms = intent.getStringExtra(SMS);
        request.merchantId = intent.getStringExtra(MERCHANT_ID);
        request.basketData = intent.getStringExtra(BASKET_DATA);
        return request;
    }

    /**
     * Provides the intent of this PaymentRequest object which can be then used to start
     * YelloPay activity.
     * Amount and currency are mandatory and must be set before calling this method.
     * @return android intent containing all payment request data previously injected
     */
    public Intent toIntent() {
        if (amount.compareTo(new BigDecimal("0.0")) == 0 || currency == null) {
            throw new IllegalStateException("Amount and currency must be set");
        }
        Intent intent = new Intent(ACTION_PAY);
        intent.putExtra(AMOUNT, amount.toString());
        intent.putExtra(CASH_BACK, cashBack.toString());
        intent.putExtra(TIP, tip.toString());
        intent.putExtra(CURRENCY, currency.getCurrencyCode());
        intent.putExtra(EMAIL, email);
        intent.putExtra(SMS, sms);
        intent.putExtra(MERCHANT_ID, merchantId);
        intent.putExtra(BASKET_DATA, basketData);
        return intent;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getCashBack() {
        return cashBack;
    }

    public void setCashBack(BigDecimal cashBack) {
        this.cashBack = cashBack;
    }

    public BigDecimal getTip() {
        return tip;
    }

    public void setTip(BigDecimal tip) {
        this.tip = tip;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getBasketData() {
        return basketData;
    }

    public void setBasketData(String basketData) {
        this.basketData = basketData;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", cashBack=" + cashBack +
                ", tip=" + tip +
                ", currency=" + currency +
                ", email='" + email + '\'' +
                ", sms='" + sms + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", basket_data=" + basketData + '\'' +
                '}';
    }
}
