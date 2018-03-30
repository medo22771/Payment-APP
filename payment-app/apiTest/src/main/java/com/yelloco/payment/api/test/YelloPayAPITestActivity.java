package com.yelloco.payment.api.test;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yelloco.payment.api.CardType;
import com.yelloco.payment.api.ErrorCode;
import com.yelloco.payment.api.PaymentRequest;
import com.yelloco.payment.api.PaymentResponse;
import com.yelloco.payment.api.PaymentResult;

import junit.framework.Assert;

import java.math.BigDecimal;
import java.util.Currency;

public class YelloPayAPITestActivity extends AppCompatActivity {

    private static final String TAG = YelloPayAPITestActivity.class.getSimpleName();

    public static final BigDecimal TEST_AMOUNT = new BigDecimal("58.50");
    public static final BigDecimal TEST_CASH_BACK = new BigDecimal("99.99");
    public static final BigDecimal TEST_TIP = new BigDecimal("1.1");
    public static final Currency TEST_CURRENCY = Currency.getInstance("CZK");
    public static final String TEST_EMAIL = "merchant1@shop.fr";
    public static final String TEST_SMS = "00111222333444";
    public static final String TEST_MERCHANT_ID = "YELLOCO";
    public static final String TEST_BASKET_DATA = "Lorem ipsum dolor sit amet, consectetur elit.";

    public static final PaymentResult TEST_RESULT = PaymentResult.APPROVED;
    public static final ErrorCode TEST_ERROR_CODE = ErrorCode.NO_ERROR;
    public static final CardType TEST_CARD_TYPE = CardType.CONTACT_CHIP;
    public static final String TEST_MASKED_PAN = "1234XXXXXXXX7890";
    public static final String TEST_RECEIPT = "TEST RECEIPT CONTENT";

    private TextView tvCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yello_pay_api_test);
        Button buttonPayMandatory = (Button) findViewById(R.id.button_pay_mandatory);
        Button buttonPayOptional = (Button) findViewById(R.id.button_pay_optional);
        tvCallback = (TextView) findViewById(R.id.tv_callback);
        buttonPayMandatory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = createPaymentRequest().toIntent();
                PaymentRequest.fromIntent(intent); // test vice versa process for any exception
                intent.setAction(PaymentRequest.ACTION_PAY);
                startActivityForResult(intent, PaymentRequest.PAYMENT_REQUEST_CODE);
            }
        });

        buttonPayOptional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentRequest request = createPaymentRequest();
                request.setCashBack(TEST_CASH_BACK);
                request.setTip(TEST_TIP);
                request.setEmail(TEST_EMAIL);
                request.setSms(TEST_SMS);
                request.setMerchantId(TEST_MERCHANT_ID);
                request.setBasketData(TEST_BASKET_DATA);
                Intent intent = request.toIntent();
                PaymentRequest.fromIntent(intent); // test vice versa process for any exception
                intent.setAction(PaymentRequest.ACTION_PAY);
                startActivityForResult(intent, PaymentRequest.PAYMENT_REQUEST_CODE);
            }
        });
    }

    private PaymentRequest createPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setCurrency(TEST_CURRENCY);
        request.setAmount(TEST_AMOUNT);
        return request;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PaymentResponse response = PaymentResponse.fromIntent(data);
        Assert.assertEquals("Expected requestCode does not match requestCode received",
                PaymentRequest.PAYMENT_REQUEST_CODE, requestCode);
        Assert.assertEquals("Expected intent result code does not match result received",
                Activity.RESULT_OK, resultCode);
        Assert.assertEquals("Expected payment result does not match result received",
                TEST_RESULT, response.getResult());
        Assert.assertEquals("Expected error code does not match result",
                TEST_ERROR_CODE, response.getErrorCode());
        Assert.assertEquals("Expected card type does not match result",
                TEST_CARD_TYPE, response.getCardType());
        Assert.assertEquals("Expected masked PAN does not match result",
                TEST_MASKED_PAN, response.getMaskedPAN());
        Assert.assertEquals("Expected receipt does not match result",
                TEST_RECEIPT, response.getReceipt());
        tvCallback.setText("Received the " + response.toString());
    }
}
