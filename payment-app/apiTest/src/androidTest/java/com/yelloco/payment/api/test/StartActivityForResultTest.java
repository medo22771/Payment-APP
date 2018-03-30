package com.yelloco.payment.api.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.yelloco.payment.api.PaymentRequest;
import com.yelloco.payment.api.PaymentResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class StartActivityForResultTest{

    private static final String TAG = StartActivityForResultTest.class.getSimpleName();

    @Rule
    public IntentsTestRule<YelloPayAPITestActivity> mActivityRule = new IntentsTestRule<>(
            YelloPayAPITestActivity.class);

    @Test
    public void testPayViaIntentMandatory() {
        PaymentResponse response = new PaymentResponse();
        response.setResult(YelloPayAPITestActivity.TEST_RESULT);
        response.setCardType(YelloPayAPITestActivity.TEST_CARD_TYPE);
        response.setErrorCode(YelloPayAPITestActivity.TEST_ERROR_CODE);
        response.setMaskedPAN(YelloPayAPITestActivity.TEST_MASKED_PAN);
        response.setReceipt(YelloPayAPITestActivity.TEST_RECEIPT);
        intending(not(isInternal())).respondWith(
                new Instrumentation.ActivityResult(Activity.RESULT_OK, response.toIntent()));

        onView(withId(R.id.button_pay_mandatory)).perform(click());
        intended(allOf(
                hasAction(equalTo(PaymentRequest.ACTION_PAY)),
                hasExtra("CURRENCY", YelloPayAPITestActivity.TEST_CURRENCY.getCurrencyCode()),
                hasExtra("AMOUNT", YelloPayAPITestActivity.TEST_AMOUNT.toString())
        ));
    }

    @Test
    public void testPayViaIntentOptional() {
        PaymentResponse response = new PaymentResponse();
        response.setResult(YelloPayAPITestActivity.TEST_RESULT);
        response.setCardType(YelloPayAPITestActivity.TEST_CARD_TYPE);
        response.setErrorCode(YelloPayAPITestActivity.TEST_ERROR_CODE);
        response.setMaskedPAN(YelloPayAPITestActivity.TEST_MASKED_PAN);
        response.setReceipt(YelloPayAPITestActivity.TEST_RECEIPT);
        intending(not(isInternal())).respondWith(
                new Instrumentation.ActivityResult(Activity.RESULT_OK, response.toIntent()));

        onView(withId(R.id.button_pay_optional)).perform(click());
        intended(allOf(
                hasAction(equalTo(PaymentRequest.ACTION_PAY)),
                hasExtra("CURRENCY", YelloPayAPITestActivity.TEST_CURRENCY.getCurrencyCode()),
                hasExtra("AMOUNT", YelloPayAPITestActivity.TEST_AMOUNT.toString()),
                hasExtra("CASH_BACK", YelloPayAPITestActivity.TEST_CASH_BACK.toString()),
                hasExtra("TIP", YelloPayAPITestActivity.TEST_TIP.toString()),
                hasExtra("EMAIL", YelloPayAPITestActivity.TEST_EMAIL),
                hasExtra("SMS", YelloPayAPITestActivity.TEST_SMS),
                hasExtra("MERCHANT_ID", YelloPayAPITestActivity.TEST_MERCHANT_ID),
                hasExtra("BASKET_DATA", YelloPayAPITestActivity.TEST_BASKET_DATA)
        ));
    }
}
