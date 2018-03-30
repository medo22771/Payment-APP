package com.yelloco.payment.host;

import static com.yelloco.payment.utils.TlvUtils.listTags;

import android.content.Context;
import android.util.Log;

import com.alcineo.transaction.events.OnlineRequestEvent;
import com.alcineo.transaction.events.OnlineReversalEvent;
import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.message.acquirer.DocumentAuthResp;
import com.yelloco.nexo.message.acquirer.DocumentCancelReq;
import com.yelloco.nexo.message.acquirer.DocumentComplAdvice;
import com.yelloco.nexo.message.acquirer.DocumentComplAdviceResp;
import com.yelloco.nexo.message.acquirer.PaymentCard6;
import com.yelloco.nexo.message.acquirer.Response1Code;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.gateway.Gateway;
import com.yelloco.payment.gateway.SafechargeGateway;
import com.yelloco.payment.nexo.NexoProcessor;
import com.yelloco.payment.nexo.NexoTransactionHelper;
import com.yelloco.payment.safecharge.SafechargeProcessor;
import com.yelloco.payment.safecharge.model.response.SafeChargeResponse;
import com.yelloco.payment.transaction.AuthorisationContext;
import com.yelloco.payment.transaction.LoadedTransactionContext;
import com.yelloco.payment.transaction.SharedPreferencesTransactionReferencePersistence;
import com.yelloco.payment.transaction.TransactionReferencePersistence;

import org.jibx.runtime.JiBXException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class HostManagerImpl implements HostManager {

    private static final String TAG = HostManagerImpl.class.getSimpleName();

    private static final int MAX_REF_VALUE = 999999;

    private NexoProcessor mNexoProcessor;
    private Context mContext;
    private Gateway mCurrentGateway;
    private NexoTransactionHelper mNexoTransactionHelper;

    public HostManagerImpl(Context context, NexoTransactionHelper nexoTransactionHelper,
            Gateway gateway) {
        this.mContext = context;
        this.mNexoTransactionHelper = nexoTransactionHelper;
        this.mCurrentGateway = gateway;
        this.mNexoProcessor = new NexoProcessor(mContext,
                new SharedPreferencesTransactionReferencePersistence(context,
                        context.getPackageName(), MAX_REF_VALUE), nexoTransactionHelper);
    }

    @Override
    public void sendTransaction(LoadedTransactionContext transactionContext,
            OnlineRequestEvent onlineRequestEvent) throws IOException {
        DocumentAuthReq document = mNexoProcessor.createAuthorisationRequest(transactionContext);
        mNexoTransactionHelper.setAuthRequest(document.getAccptrAuthstnReq());

        String serializedNexoRequest;
        String serializedNexoResponse = null;
        DocumentAuthResp fullResponse = null;
        try {
            serializedNexoRequest = XmlParser.serialize(DocumentAuthReq.class, document);
            Log.v(TAG, "Nexo request: " + serializedNexoRequest);
            serializedNexoResponse = mCurrentGateway.sendRequest(serializedNexoRequest);

            if (mCurrentGateway instanceof SafechargeGateway) {
                processSafechargeResponse(serializedNexoResponse, onlineRequestEvent);
                return;
            }
            if (serializedNexoResponse == null) {
                Log.e(TAG, "No response received from server.");
                unableToGoOnline(onlineRequestEvent);
                return;
            }

            Log.i(TAG, "Response from server \n" + serializedNexoResponse);
            fullResponse = XmlParser.parseXml(DocumentAuthResp.class, serializedNexoResponse);

            mNexoTransactionHelper.setAuthResponse(fullResponse.getAccptrAuthstnRspn());

            Response1Code responseCode = fullResponse.getAccptrAuthstnRspn().getAuthstnRspn()
                    .getTxRspn().getAuthstnRslt().getRspnToAuthstn().getRspn();
            Log.i(TAG, "Response code from server: " + responseCode);
            switch (responseCode) {
                case APPR:
                    mNexoTransactionHelper.setAuthResult(
                            AuthorisationContext.AuthorizationResult.APPROVED);
                    onlineRequestEvent.sendApproved();
                    return;
                case DECL:
                    mNexoTransactionHelper.setAuthResult(
                            AuthorisationContext.AuthorizationResult.DECLINED);
                    onlineRequestEvent.sendDeclined();
                    return;
                case PART:
                    //TODO until we support partial authorization we need to do reversal
                    // automatically
                    return;
                case TECH:
                    //TODO - check
                    unableToGoOnline(onlineRequestEvent);
                    return;
                default:
            }
        } catch (JiBXException e) {
            throw new RuntimeException("XML parsing error", e);
        }
    }

    @Override
    public void sendReversal(LoadedTransactionContext transactionContext,
            OnlineReversalEvent onlineReversalEvent) throws IOException, GeneralSecurityException {
        Log.d(TAG, "onReversalReceived: " + listTags(transactionContext.getTagStore()));
        try {
            mNexoTransactionHelper.setIncidentAfterAuth(
                    AuthorisationContext.IncidentAfterAuth.CARD_DECLINED);
            PaymentCard6 paymentCard = mNexoProcessor.getNexoPartialBuilder().createPaymentCard6(
                    transactionContext.getTagStore());
            DocumentComplAdvice document = mNexoProcessor.createCompletionAdvice(paymentCard,
                    mNexoTransactionHelper.getAuthRequest(),
                    mNexoTransactionHelper.getAuthResult());
            mNexoTransactionHelper.setComplAdvice(document.getAccptrCmpltnAdvc());
            String serializedNexoRequest = XmlParser.serialize(DocumentComplAdvice.class, document);
            Log.v(TAG, "Nexo request: " + serializedNexoRequest);
            String serializedNexoResponse = mCurrentGateway.sendRequest(serializedNexoRequest);

            if (serializedNexoResponse == null) {
                Log.e(TAG, "No response received from server.");
                return;
            }

            Log.i(TAG, "Response from server \n" + serializedNexoResponse);
            DocumentComplAdviceResp fullResponse =
                    XmlParser.parseXml(DocumentComplAdviceResp.class, serializedNexoResponse);
            mNexoTransactionHelper.setComplAdviceResponse(fullResponse.getAccptrCmpltnAdvcRspn());

            Response1Code responseCode = fullResponse.getAccptrCmpltnAdvcRspn().getCmpltnAdvcRspn
                    ().getTx().getRspn();
            Log.i(TAG, "Response code from server on reversal: " + responseCode);
            if (responseCode != Response1Code.APPR) {
                //TODO For other responses, an error resolution process has to be performed which is
                // out of scope of the nexo acquirer protocol guide.
            }
        } catch (JiBXException e) {
            Log.e(TAG, "Failed to parse NEXO response from server.", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to perform reversal.", e);
        }
    }

    @Override
    public void sendCancelTransaction(LoadedTransactionContext transactionContext) {
        String serializedNexoResponse = null;
        try {
            DocumentCancelReq documentCancelReq = mNexoProcessor.createCancellationRequest(
                    transactionContext);
            String serializedNexoRequest = XmlParser.serialize(DocumentCancelReq.class,
                    documentCancelReq);
            Log.v(TAG, "Nexo cancellation request: " + serializedNexoRequest);
            serializedNexoResponse = mCurrentGateway.sendRequest(serializedNexoRequest);
        } catch (JiBXException | IOException e) {
            Log.e(TAG, "Error during sending the cancellation message");
        }

        Log.v(TAG, "Nexo cancellation response: " + serializedNexoResponse);
    }

    @Override
    public TransactionReferencePersistence getTransactionReference() {
        return mNexoTransactionHelper.getTransactionReferencePersistence();
    }

    private void processSafechargeResponse(String response,
            OnlineRequestEvent onlineRequestEvent) {
        try {
            if (response != null) {
                Log.d("Response", response);
                SafeChargeResponse sc = SafechargeProcessor.convertResponse(response);
                if (sc != null) {
                    if (sc.getStatus().equals("APPROVED")) {
                        onlineRequestEvent.sendApproved();
                        Log.d(TAG, "onOnlineRequest: sendApproved");
                    } else if (sc.getStatus().equals("DECLINED")) {
                        onlineRequestEvent.sendDeclined();
                        Log.d(TAG, "onOnlineRequest: sendDeclined");
                    } else {
                        onlineRequestEvent.sendDeclined();
                        Log.d(TAG, "onOnlineRequest: sendDeclined");
                    }
                }
            } else {
                onlineRequestEvent.sendUnableToGoOnline();
                Log.d(TAG, "onOnlineRequest: sendUnableToGoOnline");
            }
        } catch (IOException e) {
            Log.i(TAG, "Failed to confirm online request: ", e);
        }
    }

    private void unableToGoOnline(OnlineRequestEvent onlineRequestEvent) {
        try {
            mNexoTransactionHelper.setAuthResult(
                    AuthorisationContext.AuthorizationResult.NO_ACCEPTABLE_RESPONSE);
            onlineRequestEvent.sendUnableToGoOnline();
        } catch (IOException e1) {
            Log.e(TAG, "Failed to confirm online request.", e1);
        }
    }
}