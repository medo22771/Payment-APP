package com.yelloco.payment.nexo;

import static android.content.ContentValues.TAG;

import static com.yelloco.nexo.crypto.StringUtil.toHexString;
import static com.yelloco.payment.nexo.NexoHelper.CV_RULE_OFFLINE_PIN;
import static com.yelloco.payment.nexo.NexoHelper.CV_RULE_ONLINE_PIN;
import static com.yelloco.payment.nexo.NexoHelper.CV_RULE_SIGNATURE;
import static com.yelloco.payment.nexo.NexoHelper.KSN;
import static com.yelloco.payment.nexo.NexoHelper.SCHEME_DATA_DELIMITER_CHAR;
import static com.yelloco.payment.nexo.NexoHelper.convertPaymentCard;
import static com.yelloco.payment.nexo.NexoHelper.createIcc;
import static com.yelloco.payment.nexo.NexoHelper.decideOnTransactionType;
import static com.yelloco.payment.utils.TlvTagEnum.CH_NAME;
import static com.yelloco.payment.utils.TlvTagEnum.CVM_RESULTS;
import static com.yelloco.payment.utils.TlvTagEnum.ENCRYPT_ALGO;
import static com.yelloco.payment.utils.TlvTagEnum.INITIALIZATION_VECTOR;
import static com.yelloco.payment.utils.TlvTagEnum.PIN_DATA;
import static com.yelloco.payment.utils.TlvTagEnum.PLAIN_CARD_DATA;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yelloco.nexo.crypto.DUKPTUtil;
import com.yelloco.nexo.crypto.StringUtil;
import com.yelloco.nexo.message.acquirer.AcceptorAuthorisationRequestV02;
import com.yelloco.nexo.message.acquirer.Acquirer2;
import com.yelloco.nexo.message.acquirer.AddressVerification1;
import com.yelloco.nexo.message.acquirer.Algorithm2Code;
import com.yelloco.nexo.message.acquirer.Algorithm6Code;
import com.yelloco.nexo.message.acquirer.AlgorithmIdentification2;
import com.yelloco.nexo.message.acquirer.AlgorithmIdentification6;
import com.yelloco.nexo.message.acquirer.AttendanceContext1Code;
import com.yelloco.nexo.message.acquirer.AuthenticationMethod2Code;
import com.yelloco.nexo.message.acquirer.CardDataReading1Code;
import com.yelloco.nexo.message.acquirer.CardPaymentContext1;
import com.yelloco.nexo.message.acquirer.CardPaymentContext2;
import com.yelloco.nexo.message.acquirer.CardPaymentEnvironment12;
import com.yelloco.nexo.message.acquirer.CardPaymentEnvironment18;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction11;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction13;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction15;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction16;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction17;
import com.yelloco.nexo.message.acquirer.CardPaymentTransactionDetails1;
import com.yelloco.nexo.message.acquirer.CardPaymentTransactionDetails3;
import com.yelloco.nexo.message.acquirer.CardPaymentTransactionDetails5;
import com.yelloco.nexo.message.acquirer.CardPaymentTransactionDetails7;
import com.yelloco.nexo.message.acquirer.Cardholder3;
import com.yelloco.nexo.message.acquirer.CardholderAuthentication3;
import com.yelloco.nexo.message.acquirer.CardholderVerificationCapability1Code;
import com.yelloco.nexo.message.acquirer.ContentInformationType5;
import com.yelloco.nexo.message.acquirer.ContentType1Code;
import com.yelloco.nexo.message.acquirer.DisplayCapabilities1;
import com.yelloco.nexo.message.acquirer.EncryptedContent2;
import com.yelloco.nexo.message.acquirer.EnvelopedData2;
import com.yelloco.nexo.message.acquirer.GenericIdentification32;
import com.yelloco.nexo.message.acquirer.Header1;
import com.yelloco.nexo.message.acquirer.Header2;
import com.yelloco.nexo.message.acquirer.KEK2;
import com.yelloco.nexo.message.acquirer.KEKIdentifier1;
import com.yelloco.nexo.message.acquirer.MessageFunction1Code;
import com.yelloco.nexo.message.acquirer.OnLinePIN2;
import com.yelloco.nexo.message.acquirer.Organisation8;
import com.yelloco.nexo.message.acquirer.PINFormat2Code;
import com.yelloco.nexo.message.acquirer.POIComponentType3Code;
import com.yelloco.nexo.message.acquirer.Parameter1;
import com.yelloco.nexo.message.acquirer.PaymentCard5;
import com.yelloco.nexo.message.acquirer.PaymentCard6;
import com.yelloco.nexo.message.acquirer.PaymentContext1;
import com.yelloco.nexo.message.acquirer.PaymentContext2;
import com.yelloco.nexo.message.acquirer.PointOfInteraction2;
import com.yelloco.nexo.message.acquirer.PointOfInteractionCapabilities1;
import com.yelloco.nexo.message.acquirer.PointOfInteractionComponent3;
import com.yelloco.nexo.message.acquirer.PointOfInteractionComponentIdentification1;
import com.yelloco.nexo.message.acquirer.PointOfInteractionComponentStatus1;
import com.yelloco.nexo.message.acquirer.Recipient2Choice;
import com.yelloco.nexo.message.acquirer.TransactionIdentifier1;
import com.yelloco.nexo.message.acquirer.UserInterface2Code;
import com.yelloco.payment.NexoAcquirerPreferences;
import com.yelloco.payment.data.tagstore.TagReader;
import com.yelloco.payment.transaction.AuthorisationContext;
import com.yelloco.payment.transaction.LoadedTransactionContext;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.transaction.TransactionIdentification;
import com.yelloco.payment.transaction.TransactionReferencePersistence;

import org.jibx.runtime.JiBXException;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provide the partial NEXO message components only
 */
public class NexoPartialBuilder {

    private SharedPreferences preferences;
    private NexoTransactionHelper mNexoTransactionHelper;

    public NexoPartialBuilder(SharedPreferences preferences,
            TransactionReferencePersistence txRefPersistence,
            NexoTransactionHelper nexoTransactionHelper) {
        this.preferences = preferences;
        this.mNexoTransactionHelper = nexoTransactionHelper;
        this.mNexoTransactionHelper.setTransactionReferencePersistence(txRefPersistence);
    }

    @NonNull
    private GenericIdentification32 createInitgPtyGenericIdentification32(String id) {
        GenericIdentification32 initgPty = new GenericIdentification32();
        initgPty.setId(
                id == null ? NexoAcquirerPreferences.INIT_PARTY_ID.getString(preferences) : id);
        return initgPty;
    }

    @NonNull
    private GenericIdentification32 createRcptPtyGenericIdentification32() {
        GenericIdentification32 recipientParty = new GenericIdentification32();
        recipientParty.setId(NexoAcquirerPreferences.RCPT_PARTY_ID.getString(preferences));
        return recipientParty;
    }

    @NonNull
    private PaymentContext1 createPaymentContext1() {
        PaymentContext1 pmtCntxt = new PaymentContext1();
        //TODO we need to somehow distinguish entry mode
        pmtCntxt.setCardDataNtryMd(CardDataReading1Code.CICC);

        if (NexoAcquirerPreferences.ATTENDANCE_CONTEXT.getBool(preferences)) {
            pmtCntxt.setAttndncCntxt(AttendanceContext1Code.ATTD);
        } else {
            pmtCntxt.setAttndncCntxt(AttendanceContext1Code.UATT);
        }
        return pmtCntxt;
    }

    @NonNull
    private PaymentContext2 createPaymentContext2() {
        PaymentContext2 pmtCntxt = new PaymentContext2();
        //TODO we need to somehow distinguish entry mode
        pmtCntxt.setCardDataNtryMd(CardDataReading1Code.CICC);

        if (NexoAcquirerPreferences.ATTENDANCE_CONTEXT.getBool(preferences)) {
            pmtCntxt.setAttndncCntxt(AttendanceContext1Code.ATTD);
        } else {
            pmtCntxt.setAttndncCntxt(AttendanceContext1Code.UATT);
        }
        return pmtCntxt;
    }

    @NonNull
    Cardholder3 createCardholder3(TagReader tagReader) {
        Cardholder3 cardholder = new Cardholder3();

        cardholder.setAuthntcnList(createCardholderAuthentication3List(tagReader));

        byte[] chNameValue = tagReader.getTag(CH_NAME.getTag());
        if (chNameValue != null) {
            cardholder.setNm(StringUtil.toString(chNameValue));
        }

        AddressVerification1 address = createAddressVerification1();

        cardholder.setAdrVrfctn(address);
        return cardholder;
    }

    @NonNull
    private List<CardholderAuthentication3> createCardholderAuthentication3List(
            TagReader tagReader) {
        List<CardholderAuthentication3> cardholderAuthentication3List = new ArrayList<>();
        CardholderAuthentication3 auth = createCardholderAuthentication3(tagReader);
        cardholderAuthentication3List.add(auth);
        return cardholderAuthentication3List;
    }

    @NonNull
    private AddressVerification1 createAddressVerification1() {
        AddressVerification1 address = new AddressVerification1();
        address.setAdrDgts(NexoAcquirerPreferences.ADDRESS_DIGITS.getString(preferences));
        address.setPstlCdDgts(NexoAcquirerPreferences.POSTAL_CODE_DIGITS.getString(preferences));
        return address;
    }

    @NonNull
    private CardholderAuthentication3 createCardholderAuthentication3(TagReader tagReader) {
        CardholderAuthentication3 auth = new CardholderAuthentication3();

        String cvmResults = toHexString(tagReader.getTag(CVM_RESULTS.getTag()));

        if (cvmResults != null) {
            if (cvmResults.startsWith(CV_RULE_ONLINE_PIN)) {
                auth.setAuthntcnMtd(AuthenticationMethod2Code.NPIN);
            } else if (cvmResults.startsWith(CV_RULE_SIGNATURE)) {
                auth.setAuthntcnMtd(AuthenticationMethod2Code.CPSG);
            } else if (cvmResults.startsWith(CV_RULE_OFFLINE_PIN)) {
                auth.setAuthntcnMtd(AuthenticationMethod2Code.FPIN);
            } else {
                auth.setAuthntcnMtd(AuthenticationMethod2Code.UKNW);
            }
        }

        if (auth.getAuthntcnMtd().equals(AuthenticationMethod2Code.NPIN)) {
            try {
                auth.setCrdhldrOnLinePIN(createOnlinePin(tagReader));
            } catch (UnsupportedEncodingException | GeneralSecurityException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
        return auth;
    }

    @NonNull
    private OnLinePIN2 createOnlinePin(TagReader tagReader)
            throws UnsupportedEncodingException, GeneralSecurityException {
        OnLinePIN2 onlinePin = new OnLinePIN2();

        ContentInformationType5 encryptedPinBlock = new ContentInformationType5();

        // Content Type
        encryptedPinBlock.setCnttTp(ContentType1Code.EVLP);

        // Enveloped Data
        EnvelopedData2 envelopedData = new EnvelopedData2();

        //// Recipient

        Recipient2Choice rcpt1 = new Recipient2Choice();
        KEK2 item = new KEK2();

        KEKIdentifier1 kekIda = new KEKIdentifier1();
        kekIda.setKeyId("SpecV1TestKey");
        kekIda.setKeyVrsn("2010060715");
        //instead of ByteArrayWrapper we use directly byte[]
        kekIda.setDerivtnId(
                DUKPTUtil.calculateKsnDerivationId(KSN)); //TODO: check if function is OK
        item.setKEKId(kekIda);
        AlgorithmIdentification2 keyNcrptnAlgoa = new AlgorithmIdentification2();
        keyNcrptnAlgoa.setAlgo(Algorithm2Code.DK_P9);
        item.setKeyNcrptnAlgo(keyNcrptnAlgoa);
        item.setNcrptdKey(DUKPTUtil.calculateKsnEncryptedKey(KSN));
        rcpt1.setKEK(item);
        envelopedData.getRcptList().add(rcpt1);

        //// EncryptedContent

        EncryptedContent2 encryptedContent = new EncryptedContent2();

        ////// Content Type

        encryptedContent.setCnttTp(ContentType1Code.DATA);

        ////// EncryptedData
        String pin = toHexString(tagReader.getTag(PIN_DATA.getTag()));
        Log.d("pin", pin);

        byte[] encryptedData = tagReader.getTag(PIN_DATA.getTag());

        if (encryptedData != null) {
            encryptedContent.setNcrptdData(encryptedData);
        }

        envelopedData.setNcrptdCntt(encryptedContent);

        encryptedPinBlock.setEnvlpdData(envelopedData);
        onlinePin.setNcrptdPINBlck(encryptedPinBlock);
        onlinePin.setPINFrmt(PINFormat2Code.IS_O0);

        ////// ContentEncryptionAlgorithm

        AlgorithmIdentification6 algo = new AlgorithmIdentification6();
        algo.setAlgo(Algorithm6Code.E3_DC);
        encryptedContent.setCnttNcrptnAlgo(algo);

        return onlinePin;
    }

    @NonNull
    private PointOfInteractionComponent3 createPointOfInteractionComponent3() {
        //Here can be particular component types, every with its identification, serial etc...
        PointOfInteractionComponent3 poiComp = new PointOfInteractionComponent3();
        //Terminal Component
        poiComp.setTp(POIComponentType3Code.TERM);
        PointOfInteractionComponentStatus1 status = new PointOfInteractionComponentStatus1();
        status.setVrsnNb(
                NexoAcquirerPreferences.TERMINAL_MODEL.getString(preferences));
        poiComp.setSts(status);
        PointOfInteractionComponentIdentification1 compId =
                createPointOfInteractionComponentIdentification1();
        poiComp.setId(compId);
        return poiComp;
    }

    @NonNull
    PointOfInteraction2 createPointOfInteraction2(String id) {
        PointOfInteraction2 poi = new PointOfInteraction2();
        GenericIdentification32 poiId = new GenericIdentification32();
        poiId.setId(id == null ? NexoAcquirerPreferences.POI_ID.getString(preferences) : id);
        poi.setId(poiId);

        if (id == null) {
            PointOfInteractionCapabilities1 capabilities = createPointOfInteractionCapabilities1();
            poi.setCpblties(capabilities);
            List<PointOfInteractionComponent3> compList = new ArrayList<>();
            compList.add(createPointOfInteractionComponent3());
            poi.setCmpntList(compList);
        }

        return poi;
    }

    @NonNull
    Organisation8 createOrganisation8() {
        Organisation8 merchant = new Organisation8();

        merchant.setCmonNm(NexoAcquirerPreferences.MERCHANT_NAME.getString(preferences));
        merchant.setAdr(NexoAcquirerPreferences.MERCHANT_ADDR.getString(preferences));
        merchant.setCtryCd(NexoAcquirerPreferences.MERCHANT_COUNTRY_CODE.getString(preferences));

        //custom data in generic String field can hold more key-value parameters delimited
        merchant.setSchmeData(
                NexoAcquirerPreferences.MERCHANT_CITY.getString(preferences)
                        + SCHEME_DATA_DELIMITER_CHAR
                        +
                        NexoAcquirerPreferences.MERCHANT_ZIP.getString(preferences)
                        + SCHEME_DATA_DELIMITER_CHAR +
                        NexoAcquirerPreferences.MERCHANT_PHONE.getString(preferences)
                        + SCHEME_DATA_DELIMITER_CHAR +
                        NexoAcquirerPreferences.MERCHANT_SUB_ID.getString(preferences));
        return merchant;
    }

    @NonNull
    CardPaymentEnvironment12 createCardPaymentEnvironment12(
            LoadedTransactionContext transactionContext) {
        CardPaymentEnvironment12 env = new CardPaymentEnvironment12();

        env.setAcqrr(createAcquirer2());
        env.setMrchnt(createOrganisation8());
        env.setPOI(createPointOfInteraction2(null));

        //// Card
        try {
            env.setCard(createPaymentCard6(transactionContext.getTagStore()));
        } catch (JiBXException | GeneralSecurityException | UnsupportedEncodingException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return env;
    }

    @NonNull
    CardPaymentEnvironment18 createCardPaymentEnvironment18(
            TransactionContext transactionContext, String poiId) {
        CardPaymentEnvironment18 env = new CardPaymentEnvironment18();

        env.setAcqrrId(createAcquirer2());
        env.setMrchnt(createOrganisation8());
        env.setPOI(createPointOfInteraction2(poiId));

        //// Card
        try {
            env.setCard(createPaymentCard6(transactionContext.getTagStore()));
        } catch (JiBXException | GeneralSecurityException | UnsupportedEncodingException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return env;
    }

    @NonNull
    CardPaymentTransaction11 createCardPaymentTransaction11(LoadedTransactionContext transactionContext) {
        CardPaymentTransaction11 tx = new CardPaymentTransaction11();
        tx.setTxCaptr(mNexoTransactionHelper.isFinancialCaptureRequiredForAuthRequest());
        //TODO Change
        tx.setTxTp(decideOnTransactionType(transactionContext));
        tx.setMrchntCtgyCd(transactionContext.getCategoryCode());
        tx.setTxId(createTransactionIdentifier1(transactionContext.getTransactionDateAndTime(),
                String.valueOf(
                        mNexoTransactionHelper.getTransactionReferencePersistence().getAndIncrementRef())));
        tx.setTxDtls(createCardPaymentTransactionDetails1(transactionContext));
        return tx;
    }

    @NonNull
    public CardPaymentTransaction13 createCardPaymentTransaction13(
            AcceptorAuthorisationRequestV02 authRequest,
            AuthorisationContext.AuthorizationResult authorizationResult) {
        CardPaymentTransaction13 transaction = new CardPaymentTransaction13();

        transaction.setTxCaptr(mNexoTransactionHelper.isFinancialCaptureRequiredForCompletion());

        transaction.setTxTp(authRequest.getAuthstnReq().getTx().getTxTp());
        transaction.setMrchntCtgyCd(authRequest.getAuthstnReq().getTx().getMrchntCtgyCd());

        transaction.setTxId(createTransactionIdentifier1(
                authRequest.getAuthstnReq().getTx().getTxId().getTxDtTm(),
                authRequest.getAuthstnReq().getTx().getTxId().getTxRef()));

        //Transaction success is related to wider context not only to AcceptorAuthorizationResponse
        //e.g. when offline acceptance is done - there is no AcceptorAuthorizationResponse available
        transaction.setTxSucss(
                authorizationResult == AuthorisationContext.AuthorizationResult.APPROVED);

        //TODO failure list must be provided if TransactionSuccess is False, or Reversal is True or
        // MerchantOverride is True (see section 5).
//        transaction.setFailrRsnList();

        transaction.setTxDtls(createCardPaymentTransactionDetails3(authRequest));
        return transaction;
    }

    @NonNull
    CardPaymentTransaction15 createCardPaymentTransaction15(
            LoadedTransactionContext transactionContext) {
        CardPaymentTransaction15 transaction = new CardPaymentTransaction15();
        transaction.setTxCaptr(mNexoTransactionHelper.isFinancialCaptureRequiredForAuthRequest());

        transaction.setMrchntCtgyCd(transactionContext.getCategoryCode());
        transaction.setTxId(
                createTransactionIdentifier1(transactionContext.getTransactionDateAndTime(),
                        String.valueOf(
                                mNexoTransactionHelper.getTransactionReferencePersistence().getAndIncrementRef())));
        transaction.setOrgnlTx(createCardPaymentTransaction17(transactionContext));
        transaction.setTxDtls(createCardPaymentTransactionDetails5(transactionContext));
        return transaction;
    }

    @NonNull
    CardPaymentTransaction16 createCardPaymentTransaction16(
            TransactionContext transactionContext,
            AuthorisationContext.AuthorizationResult authorizationResult) {
        CardPaymentTransaction16 transaction = new CardPaymentTransaction16();
        transaction.setMrchntCtgyCd(transactionContext.getCategoryCode());
        transaction.setTxId(
                createTransactionIdentifier1(transactionContext.getTransactionDateAndTime(),
                        String.valueOf(
                                mNexoTransactionHelper.getTransactionReferencePersistence().getAndIncrementRef())));
        transaction.setOrgnlTx(createCardPaymentTransaction17(transactionContext));
        transaction.setTxSucss(authorizationResult == AuthorisationContext
                .AuthorizationResult.APPROVED);
        transaction.setTxDtls(createCardPaymentTransactionDetails7(transactionContext));
        return transaction;
    }

    @NonNull
    CardPaymentContext1 createCardPaymentContext1() {
        CardPaymentContext1 ctx = new CardPaymentContext1();
        ctx.setPmtCntxt(createPaymentContext1());
        return ctx;
    }

    @NonNull
    CardPaymentContext2 createCardPaymentContext2() {
        CardPaymentContext2 ctx = new CardPaymentContext2();
        ctx.setPmtCntxt(createPaymentContext2());
        return ctx;
    }

    @NonNull
    private CardPaymentTransactionDetails3 createCardPaymentTransactionDetails3(
            AcceptorAuthorisationRequestV02 authRequest) {
        CardPaymentTransactionDetails3 txDetails = new CardPaymentTransactionDetails3();
        txDetails.setCcy(authRequest.getAuthstnReq().getTx().getTxDtls().getCcy());
        txDetails.setTtlAmt(authRequest.getAuthstnReq().getTx().getTxDtls().getTtlAmt());
        return txDetails;
    }

    @NonNull
    private CardPaymentTransactionDetails5 createCardPaymentTransactionDetails5(
            LoadedTransactionContext transactionContext) {
        CardPaymentTransactionDetails5 txDtls = new CardPaymentTransactionDetails5();
        txDtls.setCcy(transactionContext.getCurrency().getAlphabeticCode());
        txDtls.setTtlAmt(transactionContext.getAmount());
        //Transaction authorisation deadline to complete the related payment
        txDtls.setVldtyDt(new Date());
        txDtls.setICCRltdData(createIcc(transactionContext.getTagStore()));
        return txDtls;
    }

    @NonNull
    private CardPaymentTransactionDetails7 createCardPaymentTransactionDetails7(
            TransactionContext transactionContext) {
        CardPaymentTransactionDetails7 txDtls = new CardPaymentTransactionDetails7();
        txDtls.setCcy(transactionContext.getCurrency().getAlphabeticCode());
        txDtls.setTtlAmt(transactionContext.getAmount());
        //Transaction authorisation deadline to complete the related payment
        Date transactionDate = transactionContext.getTransactionToCancel().getTransactionDate();
        txDtls.setVldtyDt(transactionDate == null ? transactionContext.getTransactionDateAndTime()
                : transactionDate);
        txDtls.setICCRltdData(createIcc(transactionContext.getTagStore()));
        return txDtls;
    }

    @NonNull
    private CardPaymentTransaction17 createCardPaymentTransaction17(
            LoadedTransactionContext transactionContext) {
        CardPaymentTransaction17 cardPaymentTransaction17 = new CardPaymentTransaction17();
        TransactionIdentification transactionToCancel = transactionContext.getTransactionToCancel();
        TransactionIdentifier1 originalTxId = createTransactionIdentifier1(
                transactionToCancel.getTransactionDate(),
                String.valueOf(transactionToCancel.getTransactionReference()));
        cardPaymentTransaction17.setTxId(originalTxId);
        cardPaymentTransaction17.setTxTp(decideOnTransactionType(transactionContext));
        return cardPaymentTransaction17;
    }

    @NonNull
    TransactionIdentifier1 createTransactionIdentifier1(Date txDtTm, String txRef) {
        TransactionIdentifier1 transactionIdentifier1 = new TransactionIdentifier1();
        transactionIdentifier1.setTxDtTm(txDtTm);
        transactionIdentifier1.setTxRef(
                txRef == null ? String.valueOf(
                        mNexoTransactionHelper.getTransactionReferencePersistence().getAndIncrementRef())
                        : txRef);
        return transactionIdentifier1;
    }

    @NonNull
    private PointOfInteractionComponentIdentification1
    createPointOfInteractionComponentIdentification1() {
        PointOfInteractionComponentIdentification1 compId = new
                PointOfInteractionComponentIdentification1();
        compId.setPrvdrId(NexoAcquirerPreferences.PROVIDER_ID.getString(preferences) +
                SCHEME_DATA_DELIMITER_CHAR +
                NexoAcquirerPreferences.MAC_ADDRESS.getString(preferences));
        compId.setSrlNb(
                NexoAcquirerPreferences.SERIAL.getString(preferences) + SCHEME_DATA_DELIMITER_CHAR +
                        NexoAcquirerPreferences.IMEI.getString(preferences));

        compId.setId(NexoAcquirerPreferences.IP_ADDRESS.getString(preferences));
        return compId;
    }

    @NonNull
    private PointOfInteractionCapabilities1 createPointOfInteractionCapabilities1() {
        PointOfInteractionCapabilities1 capabilities = new PointOfInteractionCapabilities1();

        List<CardDataReading1Code> capList = new ArrayList<>();
        capList.add(CardDataReading1Code.CICC);
        capList.add(CardDataReading1Code.MGST);
        capList.add(CardDataReading1Code.ECTL);
        capabilities.setCardRdngCpbltyList(capList);

        List<CardholderVerificationCapability1Code> cardHolderCapList = new ArrayList<>();
        cardHolderCapList.add(CardholderVerificationCapability1Code.NPIN);
        cardHolderCapList.add(CardholderVerificationCapability1Code.MNSG);
        capabilities.setCrdhldrVrfctnCpbltyList(cardHolderCapList);

        List<DisplayCapabilities1> displayCapList = new ArrayList<>();
        DisplayCapabilities1 displayCap = new DisplayCapabilities1();
        displayCap.setDispTp(UserInterface2Code.CDSP);
        displayCap.setLineWidth(NexoAcquirerPreferences.DISPLAY_CAP_LINE_WIDTH.getString
                (preferences));
        displayCap.setNbOfLines(NexoAcquirerPreferences.DISPLAY_CAP_LINE_NB.getString(preferences));
        displayCapList.add(displayCap);
        capabilities.setDispCpbltyList(displayCapList);
        return capabilities;
    }

    @NonNull
    Acquirer2 createAcquirer2() {
        Acquirer2 acquirer = new Acquirer2();
        GenericIdentification32 acquirerId = new GenericIdentification32();
        acquirerId.setId(
                NexoAcquirerPreferences.SAFECHARGE_CLIENT_LOGIN_ID.getString(preferences) +
                        SCHEME_DATA_DELIMITER_CHAR +
                        NexoAcquirerPreferences.SAFECHARGE_CLIENT_PASSWORD.getString(preferences)
                        + SCHEME_DATA_DELIMITER_CHAR +
                        NexoAcquirerPreferences.SAFECHARGE_WEBSITE.getString(preferences) +
                        SCHEME_DATA_DELIMITER_CHAR +
                        NexoAcquirerPreferences.SAFECHARGE_USER_ID.getString(preferences)
        );
        acquirer.setId(acquirerId);
        acquirer.setParamsVrsn(
                NexoAcquirerPreferences.SAFECHARGE_VERSION.getString(preferences) +
                        SCHEME_DATA_DELIMITER_CHAR +
                        NexoAcquirerPreferences.SAFECHARGE_RESPONSE_FORMAT.getString(preferences)
        );
        return acquirer;
    }

    @NonNull
    public PaymentCard5 createPaymentCard5(TagReader tagReader) throws JiBXException,
            GeneralSecurityException, UnsupportedEncodingException {
        PaymentCard5 card = new PaymentCard5();
        card.setPrtctdCardData(createProtectedCardData(tagReader));
        return card;
    }

    @NonNull
    private ContentInformationType5 createProtectedCardData(TagReader tagReader) throws
            JiBXException, UnsupportedEncodingException, GeneralSecurityException {

        byte[] encryptedData = tagReader.getTag(PLAIN_CARD_DATA.getTag());
        byte[] iv = tagReader.getTag(INITIALIZATION_VECTOR.getTag());
        byte[] encryptAlgo = tagReader.getTag(ENCRYPT_ALGO.getTag());

        if (encryptedData == null || iv == null || encryptAlgo == null) {
            throw new JiBXException("Failed to create NEXO message. One of the following values " +
                    "were not received from kernel: encrypted data, initialization vector or " +
                    "encryption algorithm from K81.");
        }

        ContentInformationType5 protectedCardData = new ContentInformationType5();
        protectedCardData.setCnttTp(ContentType1Code.EVLP);
        EnvelopedData2 envlpdData = new EnvelopedData2();
        EncryptedContent2 ncrptdCntt = new EncryptedContent2();
        ncrptdCntt.setCnttTp(ContentType1Code.DATA);
        AlgorithmIdentification6 cnttNcrptnAlgo = new AlgorithmIdentification6();
        cnttNcrptnAlgo.setAlgo(Algorithm6Code.convert(new String(encryptAlgo)));
        Parameter1 param = new Parameter1();
        param.setInitlstnVctr(iv);
        cnttNcrptnAlgo.setParam(param);
        ncrptdCntt.setCnttNcrptnAlgo(cnttNcrptnAlgo);
        ncrptdCntt.setNcrptdData(encryptedData);
        envlpdData.setNcrptdCntt(ncrptdCntt);
        Recipient2Choice rcpt1 = new Recipient2Choice();
        KEK2 item = new KEK2();

        KEKIdentifier1 kekIda = new KEKIdentifier1();
        //TODO: name should be set by TMS?
        kekIda.setKeyId("SpecV1TestKey");
        kekIda.setKeyVrsn("2010060715");
        //TODO: replace also KSN for the one from K81
        kekIda.setDerivtnId(DUKPTUtil.calculateKsnDerivationId(KSN));
        item.setKEKId(kekIda);
        AlgorithmIdentification2 keyNcrptnAlgoa = new AlgorithmIdentification2();
        keyNcrptnAlgoa.setAlgo(Algorithm2Code.DK_P9);
        item.setKeyNcrptnAlgo(keyNcrptnAlgoa);
        item.setNcrptdKey(DUKPTUtil.calculateKsnEncryptedKey(KSN));
        rcpt1.setKEK(item);
        envlpdData.getRcptList().add(rcpt1);
        protectedCardData.setEnvlpdData(envlpdData);
        return protectedCardData;
    }

    @NonNull
    public PaymentCard6 createPaymentCard6(TagReader tagReader) throws JiBXException,
            GeneralSecurityException, UnsupportedEncodingException {
        PaymentCard5 paymentCard5 = createPaymentCard5(tagReader);
        return convertPaymentCard(paymentCard5);
    }

    @NonNull
    private CardPaymentTransactionDetails1 createCardPaymentTransactionDetails1(
            LoadedTransactionContext transactionContext) {
        CardPaymentTransactionDetails1 txDtls = new CardPaymentTransactionDetails1();
        txDtls.setCcy(transactionContext.getCurrency().getAlphabeticCode());
        txDtls.setTtlAmt(transactionContext.getAmount());
        txDtls.setICCRltdData(createIcc(transactionContext.getTagStore()));
        return txDtls;
    }

    @NonNull
    Header1 createHeader1(MessageFunction1Code messageFunction1Code) {
        Header1 header = new Header1();

        header.setMsgFctn(mNexoTransactionHelper.decideMessageFunction(messageFunction1Code));
        header.setPrtcolVrsn(NexoAcquirerPreferences.PROTOCOL_VERSION.getString(preferences));
        header.setXchgId(NexoAcquirerPreferences.EXCHANGE_ID.getString(preferences));
        header.setCreDtTm(new Date());

        header.setInitgPty(createInitgPtyGenericIdentification32(null));
        header.setRcptPty(createRcptPtyGenericIdentification32());
        return header;
    }

    @NonNull
    public Header2 createHeader2(String initiatingPartyId,
            MessageFunction1Code messageFunction1Code) {
        Header2 header = new Header2();

        header.setMsgFctn(mNexoTransactionHelper.decideMessageFunction(messageFunction1Code));
        header.setPrtcolVrsn(NexoAcquirerPreferences.PROTOCOL_VERSION.getString(preferences));
        header.setXchgId(NexoAcquirerPreferences.EXCHANGE_ID.getString(preferences));
        header.setCreDtTm(new Date());

        header.setInitgPty(createInitgPtyGenericIdentification32(initiatingPartyId));
        return header;
    }
}