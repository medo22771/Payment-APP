package com.yelloco.payment.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.blackcat.currencyedittext.CurrencyEditText;
import com.yelloco.payment.R;
import com.yelloco.payment.transaction.TransactionContext;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

/**
 * Fragment responsible for transaction initiation.
 * It provides UI to enter the transaction amount by user. After submitting it has to gather
 * transaction configuration parameters use them together with amount to initiate the transaction
 * in payment framework.
 */
public class AmountFragment extends BaseFragment {

    private static final String TAG = AmountFragment.class.getSimpleName();
    private static final String EDIT_TEXT_CURRENCY = "EDIT_TEXT_CURRENCY";
    private static final BigDecimal AMOUNT_DIVISION = new BigDecimal("100");
    public static final String TRANSACTION_TYPE = "TRANSACTION_TYPE";
    public static final String INDEX = "INDEX";
    // Amount fragment is reused for different type of amounts, all can be used in 1 transaction
    public static final int TYPE_PAYMENT = 0;
    public static final int TYPE_CASHBACK = 1;
    public static final int TYPE_INCREASED_AMOUNT = 2;
    public static final int TYPE_REFUND = 3;

    private AmountEnteredListener amountEnteredListener;
    private CurrencyEditText editTextCurrency;

    public AmountFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amount, container, false);
        AppCompatTextView textView = (AppCompatTextView) view.findViewById(R.id.text_type);
        textView.setText(getTransactionTypeMessageId());
        editTextCurrency = (CurrencyEditText) view.findViewById(R.id.edit_amount);
        // TODO if we support more currencies it must be configured (TMS ?)
        // Seems like without setting Locale the currency will remain in dollars
        editTextCurrency.setCurrency(Currency.getInstance(
                transactionProvider.getTransactionContext().getCurrency().getAlphabeticCode()),
                Locale.FRANCE);
        String initialAmount = savedInstanceState != null ?
                savedInstanceState.getString(EDIT_TEXT_CURRENCY, "0") : getPreEnteredAmount();
        editTextCurrency.setText(initialAmount);
        prepareActionButtons(view);
        prepareNumberButtons(view);
        return view;
    }

    private int getTransactionTypeMessageId() {
        int type = getArguments().getInt(TRANSACTION_TYPE);
        switch (type) {
            case TYPE_PAYMENT:
                return R.string.msg_payment;
            case TYPE_CASHBACK:
                return R.string.msg_cashback;
            case TYPE_INCREASED_AMOUNT:
                return R.string.msg_increased_amount;
            case TYPE_REFUND:
                return R.string.msg_refund;
            default:
                throw new RuntimeException("Unknown amount fragment type: " + type);
        }

    }

    private String getPreEnteredAmount() {
        final TransactionContext transactionContext = transactionProvider.getTransactionContext();
        BigDecimal amount = null;
        switch (getArguments().getInt(TRANSACTION_TYPE)) {
            case TYPE_PAYMENT:
                amount = transactionContext.getAmount();
                break;
            case TYPE_CASHBACK:
                amount = transactionContext.getAmountOther();
                break;
            case TYPE_INCREASED_AMOUNT:
                amount = transactionContext.getIncreasedAmount();
                break;
            case TYPE_REFUND:
                amount = transactionContext.getAmount();
                break;
        }
        return ((amount == null) ?  "0" : amount.toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EDIT_TEXT_CURRENCY, editTextCurrency.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public interface AmountEnteredListener {
        void onAmountEntered(int amountType);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            amountEnteredListener = (AmountEnteredListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + AmountEnteredListener.class.getSimpleName());
        }
    }

    private void prepareActionButtons(View view) {
        final TransactionContext transactionContext = transactionProvider.getTransactionContext();
        view.findViewById(R.id.bt_confirmAmount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = getArguments().getInt(TRANSACTION_TYPE);
                switch (type) {
                    case TYPE_PAYMENT:
                    case TYPE_REFUND:
                        transactionContext.setAmount(new BigDecimal(editTextCurrency.getRawValue
                                ()).divide(AMOUNT_DIVISION));
                        break;
                    case TYPE_CASHBACK:
                        transactionContext.setAmountOther(new BigDecimal(editTextCurrency
                                .getRawValue()).divide(AMOUNT_DIVISION));
                        break;
                    case TYPE_INCREASED_AMOUNT:
                        transactionContext.setIncreasedAmount(new BigDecimal(editTextCurrency
                                .getRawValue()).divide(AMOUNT_DIVISION));
                        break;
                    default:
                        throw new RuntimeException("Unknown amount fragment type: " + type);
                }
                amountEnteredListener.onAmountEntered(getArguments().getInt(INDEX));
            }
        });
        view.findViewById(R.id.Modifier).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cutAmount = Long.toString(editTextCurrency.getRawValue()/10);
                editTextCurrency.setText(cutAmount);
            }
        });
        view.findViewById(R.id.btn_go_back_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextCurrency.setText("0");
            }
        });
    }

    private void prepareNumberButtons(View view) {
        view.findViewById(R.id.Button0).setOnClickListener(setDigit("0"));
        view.findViewById(R.id.Button1).setOnClickListener(setDigit("1"));
        view.findViewById(R.id.Button2).setOnClickListener(setDigit("2"));
        view.findViewById(R.id.Button3).setOnClickListener(setDigit("3"));
        view.findViewById(R.id.Button4).setOnClickListener(setDigit("4"));
        view.findViewById(R.id.Button5).setOnClickListener(setDigit("5"));
        view.findViewById(R.id.Button6).setOnClickListener(setDigit("6"));
        view.findViewById(R.id.Button7).setOnClickListener(setDigit("7"));
        view.findViewById(R.id.Button8).setOnClickListener(setDigit("8"));
        view.findViewById(R.id.Button9).setOnClickListener(setDigit("9"));
    }

    private View.OnClickListener setDigit(final String digit) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextCurrency.append(digit);
            }
        };
    }
}
