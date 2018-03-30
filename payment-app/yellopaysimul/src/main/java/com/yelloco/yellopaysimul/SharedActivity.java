package com.yelloco.yellopaysimul;

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

public class SharedActivity extends AppCompatActivity {

    private TextView mTextView;
    private Button mButton;

    private static final String MASKED_PAN = "1234XXXXXXXX7890";
    private static final String RECEIPT = "TEST RECEIPT CONTENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared);
        mTextView = (TextView) findViewById(R.id.tv);
        mButton = (Button) findViewById(R.id.btn);

        Intent data = getIntent();

        if (data.getAction().equals("com.yelloco.payment.api.PAY")) {
            PaymentRequest request = PaymentRequest.fromIntent(data);

            mTextView.setText("Received the "+request.toString());
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PaymentResponse response = new PaymentResponse();
                    response.setCardType(CardType.CONTACT_CHIP);
                    response.setResult(PaymentResult.APPROVED);
                    response.setErrorCode(ErrorCode.NO_ERROR);
                    response.setMaskedPAN(MASKED_PAN);
                    response.setReceipt(RECEIPT);
                    setResult(RESULT_OK, response.toIntent());
                    finish();
                }
            });
        }
    }
}
