package com.yelloco.payment.transaction;

import static com.yelloco.nexo.crypto.StringUtil.toHexString;
import static com.yelloco.payment.utils.TlvTagEnum.PAN;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alcineo.connection.dispatcher.DispatcherService;
import com.alcineo.transaction.TransactionManager;
import com.alcineo.transaction.TransactionType;
import com.yelloco.payment.PaymentFramework;
import com.yelloco.payment.TransactionPreferences;
import com.yelloco.payment.api.CardType;
import com.yelloco.payment.api.ErrorCode;
import com.yelloco.payment.api.PaymentRequest;
import com.yelloco.payment.api.PaymentResponse;
import com.yelloco.payment.api.PaymentResult;
import com.yelloco.payment.utils.RequestType;
import com.yelloco.payment.utils.YelloCurrency;

import java.math.BigDecimal;

public class TransactionHelper {

    private static final String TAG = TransactionHelper.class.getSimpleName();

    private SharedPreferences mPreferences;

    public TransactionHelper(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    /**
     * Prepares the transaction context which is specific to one transaction.
     */
    // TODO some properties should be set by TMS, temporary hardcode is done until TMS is ready
    public TransactionContext initializeTransactionContext(PaymentRequest request) {
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setForceOnline(TransactionPreferences.FORCE_ONLINE.getValue(mPreferences));
        transactionContext.setCategoryCode("01");

        if (request == null) {
            Log.v(TAG, "Starting transaction from GUI");
            transactionContext.setRequestType(RequestType.YELLO_PAY_UI);
            transactionContext.setCurrency(YelloCurrency.EUR);
            transactionContext.setTransactionType(decideTransactionType());
        } else {
            Log.v(TAG, "Starting transaction from YelloPayAPI");
            transactionContext.setRequestType(RequestType.YELLO_PAY_API);
            transactionContext.setAmount(request.getAmount());
            transactionContext.setAmountOther(request.getCashBack());
            transactionContext.setIncreasedAmount(request.getTip());
            transactionContext.setCurrency(YelloCurrency.getByCurrencyAlphabeticCode(
                    request.getCurrency().getCurrencyCode()));
            transactionContext.setTransactionType(request.getCashBack().compareTo
                    (new BigDecimal("0")) == 0 ? TransactionType.PURCHASE : TransactionType.CASHBACK);
            transactionContext.setEmail(request.getEmail());
            transactionContext.setSms(request.getSms());
            transactionContext.setBasketData(request.getBasketData());
        }

        return transactionContext;
    }

    /**
     * Method is responsible to map kernel transaction types to payment application settings.
     *
     * @return
     */
    private TransactionType decideTransactionType() {
        if (TransactionPreferences.PAYMENT.getValue(mPreferences) ||
                TransactionPreferences.INCREASED_AMOUNT.getValue(mPreferences)) {
            if (TransactionPreferences.CASHBACK.getValue(mPreferences)) {
                Log.v(TAG, "Transaction type CASHBACK");
                return TransactionType.CASHBACK;
            } else {
                Log.v(TAG, "Transaction type PURCHASE");
                return TransactionType.PURCHASE;
            }
        } else if (TransactionPreferences.CASHBACK.getValue(mPreferences)) {
            Log.v(TAG, "Transaction type CASH");
            return TransactionType.CASH;
        } else if (TransactionPreferences.REFUND.getValue(mPreferences)) {
            Log.v(TAG, "Transaction type REFUND");
            return TransactionType.REFUND;
        }
        throw new IllegalStateException("Transaction type configuration not implemented: " +
                TransactionPreferences.dumpValues(mPreferences));
    }

    @NonNull
    public PaymentResponse createPaymentResponse(LoadedTransactionContext transactionContext) {
        PaymentResponse response = new PaymentResponse();
        response.setErrorCode(ErrorCode.NO_ERROR);
        switch (transactionContext.getTransactionStatus()) {
            case TIMEOUT:
                response.setResult(PaymentResult.TIMEOUT);
                break;
            case ERROR:
                response.setResult(PaymentResult.ERROR);
                response.setErrorCode(ErrorCode.UNKNOWN_ERROR);
                break;
            case FINISHED:
                //TODO instead of parsing receipt we should take from TLVs which require EMV logic
                // but is precise and should never change
                String receipt = transactionContext.getReceipt();
                //check whether receipt is not empty otherwise something was wrong
                if (receipt == null || receipt.equals("")) {
                    response.setResult(PaymentResult.ERROR);
                    response.setErrorCode(ErrorCode.UNKNOWN_ERROR);
                }
                //look for result
                if (transactionContext.getReceipt().contains("APPROVED")) {
                    response.setResult(PaymentResult.APPROVED);
                } else if (transactionContext.getReceipt().contains("DECLINED")) {
                    response.setResult(PaymentResult.DECLINED);
                } else {
                    response.setResult(PaymentResult.ERROR);
                    response.setErrorCode(ErrorCode.UNKNOWN_ERROR);
                }
                //look for card type
                if (receipt.contains("Chip"))
                    response.setCardType(CardType.CONTACT_CHIP);
                else if (receipt.contains("VSDC") || receipt.contains("MSD"))
                    response.setCardType(CardType.CONTACTLESS);
                else if (receipt.contains("Magnetic"))
                    response.setCardType(CardType.MAGNETIC_STRIPE);
                //look for PAN
                try {
                    response.setMaskedPAN(toHexString(transactionContext.getTagStore().getTag(PAN.getTag())));
                } catch (Exception e) {
                    Log.e(TAG, "PAN could not be extracted from TLVs", e);
                }
        }
        response.setReceipt(transactionContext.getReceipt());
        return response;
    }

    @NonNull
    public TransactionManager createTransactionManager(LoadedTransactionContext transactionContext) {
        DispatcherService service = PaymentFramework.getInstance().getDispatcherService();
        final TransactionManager transactionManager =
                new TransactionManager(service.getDispatcher(), service);
        BigDecimal amount = transactionContext.getAmount().add(transactionContext.getIncreasedAmount());
        Log.i(TAG, "Transaction context: " + transactionContext.toString());
        transactionManager.setAmount(amount);
        transactionManager.setCategoryCode(transactionContext.getCategoryCode());
        transactionManager.setType(transactionContext.getTransactionType());
        transactionManager.setCurrencyCode(transactionContext.getCurrency().getNumericCode().toString());
        transactionManager.setAmountOther(transactionContext.getAmountOther());
        transactionManager.setMerchantData("TestMerchantData");

        return transactionManager;
    }
}