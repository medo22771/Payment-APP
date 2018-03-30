package com.yelloco.payment.transaction;

import static com.yelloco.payment.transaction.TransactionResult.getByCode;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_RESULT;
import static com.yelloco.payment.utils.Utils.bytesToHex;
import static com.yelloco.payment.utils.YelloCurrency.getByCurrencyNumericCode;

import android.os.Parcel;
import android.os.Parcelable;

import com.alcineo.transaction.TransactionManager;
import com.alcineo.transaction.TransactionType;
import com.alcineo.transaction.events.NotifyKernelIdEvent;
import com.alcineo.transaction.events.TransactionFinishedEvent;
import com.yelloco.payment.data.tagstore.TagReader;
import com.yelloco.payment.data.tagstore.TagStore;
import com.yelloco.payment.utils.RequestType;
import com.yelloco.payment.utils.YelloCurrency;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Context should store data specific for one transaction
 */
public class TransactionContext implements Parcelable, LoadedTransactionContext {

    /**
     * One and only transaction date and time
     */
    private Date date;
    /**
     * Where the request comes from. We need to handle request from application UI differently
     * than request from another android application via YelloPayAPI
     */
    private RequestType requestType;
    /**
     * Transaction amount (mandatory)
     */
    private BigDecimal amount = new BigDecimal("0");
    /**
     * The amount associated with a cash back transaction type (optional).
     */
    private BigDecimal amountOther = new BigDecimal("0");
    /**
     * If balance reading is required as a configuration option (according to payment scheme)
     * then these controls let you set Balance Read Before Gen AC and Balance Read After Gen AC.
     */
    private int balanceBefore;
    private int balanceAfter;
    private boolean balanceIsSet = false;
    /**
     * Transaction type specific for MasterCard
     */
    private String categoryCode;

    private YelloCurrency currency;
    /**
     * If not checked, in case a new presentation of the card is required, terminates the
     * transaction. This feature is not implemented into all products. Please check the
     * ICS related to payment kernel.
     */
    private boolean enableEntryPoint;
    /**
     * Transaction type specific for MasterCard
     */
    private boolean forceOnline;
    /**
     * Tip which can be included as additional amount. It is not used for EMV kernel - there is just
     * sum of the amount and increasedAmount used
     */
    private BigDecimal increasedAmount = new BigDecimal("0");
    /**
     * Indicates the type of financial transaction, represented by the first two digits of [ISO
     * 8583:1993] Processing Code. Please refer to standard for more information about Transaction Type.
     */
    private TransactionType transactionType;
    /**
     * Receipt of transaction in String format.
     */
    private StringBuilder receipt;

    /**
     * Transaction status received from L2.
     * It is null until
     * {@link com.alcineo.transaction.TransactionListener#onTransactionFinished(TransactionManager.TransactionStatus, TransactionFinishedEvent)} is received
     */
    private TransactionManager.TransactionStatus transactionStatus;

    /**
     * TLV items received from L2.
     * It is null until
     * {@link com.alcineo.transaction.TransactionListener#onTransactionFinished(TransactionManager.TransactionStatus, TransactionFinishedEvent)} is received
     */
    private String tlvs;

    /**
     * Storage for the tags received from L2 kernel or from a host
     */
    private TagStore tagStore;

    /**
     * Pre-filled email address where receipt should be sent to.
     */
    private String email;

    /**
     * Pre-filled sms number where receipt should be sent to.
     */
    private String sms;

    /**
     * ID of the kernel which is used for the transaction. Should be received in
     * {@link com.alcineo.transaction.TransactionListener#onNotifyKernelIdReceived(NotifyKernelIdEvent)}
     * KernelId can be used to determine TLVs which might be specific for particular contactless kernels
     */
    private int kernelId;

    /**
     * Transaction basket data;
     */
    private String basketData;

    /**
     * Holds the identification of the transaction to be cancelled
     */
    private TransactionIdentification transactionToCancel;

    /**
     * Persistence for the transaction reference
     */
    private TransactionReferencePersistence transactionReferencePersistence;

    /**
     * Holds the result of a transaction
     */
    private TransactionResult transactionResult;

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountOther() {
        return amountOther;
    }

    public void setAmountOther(BigDecimal amountOther) {
        this.amountOther = amountOther;
    }

    public int getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(int balanceBefore) {
        this.balanceIsSet = true;
        this.balanceBefore = balanceBefore;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(int balanceAfter) {
        this.balanceIsSet = true;
        this.balanceAfter = balanceAfter;
    }

    public boolean isBalanceSet() {
        return balanceIsSet;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    @Override
    public YelloCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(YelloCurrency currency) {
        this.currency = currency;
    }

    public boolean getEnableEntryPoint() {
        return enableEntryPoint;
    }

    public void setEnableEntryPoint(boolean enableEntryPoint) {
        this.enableEntryPoint = enableEntryPoint;
    }

    public boolean isForceOnline() {
        return forceOnline;
    }

    public void setForceOnline(boolean forceOnline) {
        this.forceOnline = forceOnline;
    }

    public BigDecimal getIncreasedAmount() {
        return increasedAmount;
    }

    public void setIncreasedAmount(BigDecimal increasedAmount) {
        this.increasedAmount = increasedAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void insertToReceipt(String input, int position) {
        receipt.insert(position, input);
    }

    public void addToReceipt(String input) {
        receipt.append(input);
    }

    public String getReceipt() {
        return receipt.toString();
    }

    public TransactionManager.TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionManager.TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public TagReader getTagStore() {
        return tagStore;
    }

    public void createOrUpdateContext(TagStore tagStore) {
        updateTagStoreValues(tagStore);
        updateContextData(tagStore);
    }

    private void updateContextData(TagStore tagStore) {
        if (tagStore != null) {
            byte[] resultValue = tagStore.getTag(TRANSACTION_RESULT.getTag());
            if (resultValue != null) {
                this.transactionResult = getByCode(bytesToHex(resultValue));
            }
        }
    }

    private void updateTagStoreValues(TagStore tagStore) {
        if (this.tagStore != null) {
            if (tagStore.hasTags()) {
                for (String tag : tagStore.getAllTags().keySet()) {
                    this.tagStore.setTag(tag, tagStore.getAllTags().get(tag));
                }
            }
        } else {
            this.tagStore = tagStore;
        }
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

    public int getKernelId() {
        return kernelId;
    }

    public void setKernelId(int kernelId) {
        this.kernelId = kernelId;
    }

    public String getBasketData() {
        return basketData;
    }

    public void setBasketData(String basketData) {
        this.basketData = basketData;
    }

    public TransactionIdentification getTransactionToCancel() {
        return transactionToCancel;
    }

    public void setTransactionToCancel(
            TransactionIdentification transactionToCancel) {
        this.transactionToCancel = transactionToCancel;
    }

    public TransactionReferencePersistence getTransactionReferencePersistence() {
        return transactionReferencePersistence;
    }

    public void setTransactionReferencePersistence(
            TransactionReferencePersistence transactionReferencePersistence) {
        this.transactionReferencePersistence = transactionReferencePersistence;
    }

    public Date getTransactionDateAndTime() {
        return date;
    }

    public void setTransactionDateAndTime(Date date) {
        this.date = date;
    }

    public TransactionResult getTransactionResult() {
        return transactionResult;
    }

    public void setTransactionResult(TransactionResult transactionResult) {
        this.transactionResult = transactionResult;
    }

    @Override
    public String toString() {
        return "TransactionContext{" +
                "requestType=" + requestType +
                ", amount=" + amount +
                ", amountOther=" + amountOther +
                ", balanceBefore=" + balanceBefore +
                ", balanceAfter=" + balanceAfter +
                ", balanceIsSet=" + balanceIsSet +
                ", categoryCode='" + categoryCode + '\'' +
                ", currency=" + currency +
                ", enableEntryPoint=" + enableEntryPoint +
                ", forceOnline=" + forceOnline +
                ", increasedAmount=" + increasedAmount +
                ", transactionType=" + transactionType +
                ", receipt=" + receipt +
                ", transactionStatus=" + transactionStatus +
                ", email='" + email + '\'' +
                ", sms='" + sms + '\'' +
                ", kernelId='" + kernelId + '\'' +
                ", basketData='" + basketData + '\'' +
                '}';
    }

    public TransactionContext() {
        receipt = new StringBuilder();
        date = Calendar.getInstance().getTime();
    }

    protected TransactionContext(Parcel in) {
        amount = new BigDecimal(in.readString());
        amountOther = new BigDecimal(in.readString());
        balanceBefore = in.readInt();
        balanceAfter = in.readInt();
        balanceIsSet = in.readByte() != 0x00;
        categoryCode = in.readString();
        currency = getByCurrencyNumericCode(in.readString());
        enableEntryPoint = in.readByte() != 0x00;
        forceOnline = in.readByte() != 0x00;
        increasedAmount = new BigDecimal(in.readString());
        transactionType = (TransactionType) in.readValue(TransactionType.class.getClassLoader());
        receipt = new StringBuilder(in.readString());
        transactionStatus = (TransactionManager.TransactionStatus) in.readValue
                (TransactionManager.TransactionStatus.class.getClassLoader());
        tlvs = in.readString();
        email = in.readString();
        sms = in.readString();
        kernelId = in.readInt();
        basketData = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(amount.toString());
        dest.writeString(amountOther.toString());
        dest.writeInt(balanceBefore);
        dest.writeInt(balanceAfter);
        dest.writeByte((byte) (balanceIsSet ? 0x01 : 0x00));
        dest.writeString(categoryCode);
        dest.writeValue(currency);
        dest.writeByte((byte) (enableEntryPoint ? 0x01 : 0x00));
        dest.writeByte((byte) (forceOnline ? 0x01 : 0x00));
        dest.writeString(increasedAmount.toString());
        dest.writeValue(transactionType);
        dest.writeString(receipt.toString());
        dest.writeValue(transactionStatus);
        dest.writeString(tlvs);
        dest.writeString(email);
        dest.writeString(sms);
        dest.writeInt(kernelId);
        dest.writeString(basketData);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TransactionContext> CREATOR = new Parcelable.Creator<TransactionContext>() {
        @Override
        public TransactionContext createFromParcel(Parcel in) {
            return new TransactionContext(in);
        }

        @Override
        public TransactionContext[] newArray(int size) {
            return new TransactionContext[size];
        }
    };
}
