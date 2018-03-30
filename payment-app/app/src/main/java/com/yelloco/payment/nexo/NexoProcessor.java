package com.yelloco.payment.nexo;

import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.AUTQ;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.CCAQ;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.CCAV;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.CMPV;
import static com.yelloco.payment.nexo.NexoHelper.BDK;
import static com.yelloco.payment.nexo.NexoHelper.KSN;

import android.content.Context;
import android.util.Log;

import com.yelloco.nexo.crypto.DUKPTUtil;
import com.yelloco.nexo.message.acquirer.AcceptorAuthorisationRequest2;
import com.yelloco.nexo.message.acquirer.AcceptorAuthorisationRequestV02;
import com.yelloco.nexo.message.acquirer.AcceptorCancellationAdvice2;
import com.yelloco.nexo.message.acquirer.AcceptorCancellationAdviceV02;
import com.yelloco.nexo.message.acquirer.AcceptorCancellationRequest2;
import com.yelloco.nexo.message.acquirer.AcceptorCancellationRequestV02;
import com.yelloco.nexo.message.acquirer.AcceptorCompletionAdvice2;
import com.yelloco.nexo.message.acquirer.AcceptorCompletionAdviceV02;
import com.yelloco.nexo.message.acquirer.Algorithm2Code;
import com.yelloco.nexo.message.acquirer.Algorithm3Code;
import com.yelloco.nexo.message.acquirer.AlgorithmIdentification2;
import com.yelloco.nexo.message.acquirer.AlgorithmIdentification3;
import com.yelloco.nexo.message.acquirer.AuthenticatedData2;
import com.yelloco.nexo.message.acquirer.CardPaymentEnvironment10;
import com.yelloco.nexo.message.acquirer.CardPaymentEnvironment12;
import com.yelloco.nexo.message.acquirer.CardPaymentEnvironment9;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction13;
import com.yelloco.nexo.message.acquirer.ContentInformationType6;
import com.yelloco.nexo.message.acquirer.ContentType1Code;
import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.message.acquirer.DocumentCancelAdvice;
import com.yelloco.nexo.message.acquirer.DocumentCancelReq;
import com.yelloco.nexo.message.acquirer.DocumentComplAdvice;
import com.yelloco.nexo.message.acquirer.EncapsulatedContent1;
import com.yelloco.nexo.message.acquirer.KEK2;
import com.yelloco.nexo.message.acquirer.KEKIdentifier1;
import com.yelloco.nexo.message.acquirer.PaymentCard6;
import com.yelloco.nexo.message.acquirer.Recipient2Choice;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.transaction.AuthorisationContext;
import com.yelloco.payment.transaction.LoadedTransactionContext;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.transaction.TransactionReferencePersistence;

import org.jibx.runtime.JiBXException;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class NexoProcessor {
    private static final String TAG = NexoProcessor.class.getSimpleName();

    private NexoPartialBuilder nexoPartialBuilder;

    public NexoProcessor(Context context,
            TransactionReferencePersistence transactionReferencePersistence,
            NexoTransactionHelper nexoTransactionHelper) {
        this.nexoPartialBuilder = new NexoPartialBuilder(
                context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE),
                transactionReferencePersistence, nexoTransactionHelper);
    }

    public AcceptorAuthorisationRequest2 createAuthorisationRequestRequest(
            LoadedTransactionContext transactionContext) {

        AcceptorAuthorisationRequest2 request = new AcceptorAuthorisationRequest2();

        // Environment
        CardPaymentEnvironment9 env = new CardPaymentEnvironment9();

        // Acquirer
        env.setAcqrr(nexoPartialBuilder.createAcquirer2());

        //// Merchant
        env.setMrchnt(nexoPartialBuilder.createOrganisation8());

        //// POI
        env.setPOI(nexoPartialBuilder.createPointOfInteraction2(null));

        //// Card
        try {
            env.setCard(nexoPartialBuilder.createPaymentCard5(transactionContext.getTagStore()));
        } catch (JiBXException | GeneralSecurityException | UnsupportedEncodingException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        // Cardholder
        env.setCrdhldr(nexoPartialBuilder.createCardholder3(transactionContext.getTagStore()));

        // Transaction
        request.setEnvt(env);
        request.setCntxt(nexoPartialBuilder.createCardPaymentContext1());
        request.setTx(
                nexoPartialBuilder.createCardPaymentTransaction11(transactionContext));

        return request;
    }

    /**
     * To be replaced by K81 function
     *
     * @return ContentInformationType6 trailer
     */
    public static ContentInformationType6 createTrailer(byte[] messageBody) {

        ContentInformationType6 sctyTrlr = new ContentInformationType6();
        sctyTrlr.setCnttTp(ContentType1Code.AUTH);
        AuthenticatedData2 authntcdData = new AuthenticatedData2();
        Recipient2Choice rcptz = new Recipient2Choice();
        KEK2 item2 = new KEK2();
        KEKIdentifier1 kekId = new KEKIdentifier1();
        kekId.setKeyId("SpecV1TestKey");
        kekId.setKeyVrsn("2010060715");
        //instead of ByteArrayWrapper we use directly byte[]
        kekId.setDerivtnId(DUKPTUtil.calculateKsnDerivationId(KSN));
        item2.setKEKId(kekId);
        AlgorithmIdentification2 keyNcrptnAlgo = new AlgorithmIdentification2();
        keyNcrptnAlgo.setAlgo(Algorithm2Code.DK_P9);
        item2.setKeyNcrptnAlgo(keyNcrptnAlgo);
        //instead of ByteArrayWrapper we use directly byte[]
        item2.setNcrptdKey(DUKPTUtil.calculateKsnEncryptedKey(KSN));
        rcptz.setKEK(item2);
        authntcdData.getRcptList().add(rcptz);
        AlgorithmIdentification3 macAlgo = new AlgorithmIdentification3();
        macAlgo.setAlgo(Algorithm3Code.MCCS);
        authntcdData.setMACAlgo(macAlgo);
        EncapsulatedContent1 ncpsltdCntt = new EncapsulatedContent1();
        ncpsltdCntt.setCnttTp(ContentType1Code.DATA);
        authntcdData.setNcpsltdCntt(ncpsltdCntt);
        //instead of ByteArrayWrapper we use directly byte[]
        try {
            authntcdData.setMAC(DUKPTUtil.calculateHashMacSha256(messageBody, DUKPTUtil
                    .calculateHmacKey(KSN, BDK, true)));
        } catch (GeneralSecurityException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        sctyTrlr.getAuthntcdDataList().add(authntcdData);
        return sctyTrlr;
    }

    public DocumentAuthReq createAuthorisationRequest(LoadedTransactionContext transactionContext) {
        DocumentAuthReq doc = new DocumentAuthReq();

        AcceptorAuthorisationRequestV02 req = new AcceptorAuthorisationRequestV02();

        req.setHdr(nexoPartialBuilder.createHeader1(AUTQ));
        AcceptorAuthorisationRequest2 request2 = createAuthorisationRequestRequest(
                transactionContext);
        req.setAuthstnReq(request2);

        byte[] messageBody = new byte[0];
        try {
            messageBody = XmlParser.serialize(AcceptorAuthorisationRequest2.class,
                    request2).getBytes();
        } catch (JiBXException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        req.setSctyTrlr(createTrailer(messageBody));
        doc.setAccptrAuthstnReq(req);
        return doc;
    }

    /**
     * Create the entire AcceptorCompletionAdvice request
     */
    public DocumentComplAdvice createCompletionAdvice(PaymentCard6 card,
            AcceptorAuthorisationRequestV02 authRequest,
            AuthorisationContext.AuthorizationResult authorizationResult) {
        AcceptorCompletionAdviceV02 completionAdviceV02 = new AcceptorCompletionAdviceV02();
        completionAdviceV02.setHdr(
                nexoPartialBuilder.createHeader2(authRequest.getHdr().getInitgPty().getId(), CMPV));

        AcceptorCompletionAdvice2 body = createCompletionAdviceBody(card, authRequest,
                authorizationResult);
        completionAdviceV02.setCmpltnAdvc(body);

        byte[] messageBody = new byte[0];
        try {
            messageBody = XmlParser.serialize(AcceptorCompletionAdvice2.class, body).getBytes();
        } catch (JiBXException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        completionAdviceV02.setSctyTrlr(createTrailer(messageBody));

        DocumentComplAdvice document = new DocumentComplAdvice();
        document.setAccptrCmpltnAdvc(completionAdviceV02);

        return document;
    }

    private AcceptorCompletionAdvice2 createCompletionAdviceBody(PaymentCard6 card,
            AcceptorAuthorisationRequestV02 authRequest,
            AuthorisationContext.AuthorizationResult authorizationResult) {

        AcceptorCompletionAdvice2 body = new AcceptorCompletionAdvice2();

        // Environment
        CardPaymentEnvironment10 environment = new CardPaymentEnvironment10();
        environment.setPOI(nexoPartialBuilder.createPointOfInteraction2(
                authRequest.getAuthstnReq().getEnvt().getPOI().getId().getId()));
        environment.setCard(card);

        body.setEnvt(environment);

        // Transaction
        CardPaymentTransaction13 transaction = nexoPartialBuilder.createCardPaymentTransaction13(
                authRequest, authorizationResult);

        body.setTx(transaction);
        return body;
    }

    public DocumentCancelReq createCancellationRequest(LoadedTransactionContext transactionContext) throws
            JiBXException {
        AcceptorCancellationRequestV02 request = new AcceptorCancellationRequestV02();
        request.setHdr(nexoPartialBuilder.createHeader1(CCAQ));
        AcceptorCancellationRequest2 body = createCancellationRequestBody(transactionContext);
        request.setCxlReq(body);

        byte[] messageBody = XmlParser.serialize(AcceptorCancellationRequest2.class,
                body).getBytes();

        request.setSctyTrlr(createTrailer(messageBody));
        DocumentCancelReq document = new DocumentCancelReq();
        document.setAccptrCxlReq(request);
        return document;
    }

    private AcceptorCancellationRequest2 createCancellationRequestBody(
            LoadedTransactionContext transactionContext) {
        AcceptorCancellationRequest2 acceptorCancellationRequest =
                new AcceptorCancellationRequest2();
        CardPaymentEnvironment12 env = nexoPartialBuilder.createCardPaymentEnvironment12(
                transactionContext);

        acceptorCancellationRequest.setEnvt(env);
        acceptorCancellationRequest.setCntxt(nexoPartialBuilder.createCardPaymentContext1());
        acceptorCancellationRequest.setTx(
                nexoPartialBuilder.createCardPaymentTransaction15(transactionContext));

        return acceptorCancellationRequest;
    }

    public NexoPartialBuilder getNexoPartialBuilder() {
        return nexoPartialBuilder;
    }

    public DocumentCancelAdvice createCancellationAdvice(TransactionContext transactionContext,
            AcceptorCancellationRequestV02 acceptorCancellationRequest,
            AuthorisationContext.AuthorizationResult authorizationResult)
            throws JiBXException {
        AcceptorCancellationAdviceV02 advice = new AcceptorCancellationAdviceV02();
        advice.setHdr(nexoPartialBuilder.createHeader2(
                acceptorCancellationRequest.getHdr().getInitgPty().getId(), CCAV));
        AcceptorCancellationAdvice2 body = createCancellationAdviceBody(transactionContext,
                acceptorCancellationRequest, authorizationResult);
        advice.setCxlAdvc(body);

        byte[] messageBody = XmlParser.serialize(AcceptorCancellationAdvice2.class,
                body).getBytes();

        advice.setSctyTrlr(createTrailer(messageBody));
        DocumentCancelAdvice document = new DocumentCancelAdvice();
        document.setAccptrCxlAdvc(advice);
        return document;
    }

    private AcceptorCancellationAdvice2 createCancellationAdviceBody(
            TransactionContext transactionContext,
            AcceptorCancellationRequestV02 acceptorCancellationRequest,
            AuthorisationContext.AuthorizationResult authorizationResult) {
        AcceptorCancellationAdvice2 body = new AcceptorCancellationAdvice2();
        body.setEnvt(nexoPartialBuilder.createCardPaymentEnvironment18(
                transactionContext,
                acceptorCancellationRequest.getCxlReq().getEnvt().getPOI().getId().getId()));
        body.setCntxt(nexoPartialBuilder.createCardPaymentContext2());
        body.setTx(nexoPartialBuilder.createCardPaymentTransaction16(transactionContext,
                authorizationResult));
        return body;
    }
}