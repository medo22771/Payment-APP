package com.yelloco.payment.nexo;

import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.FAUQ;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.FCMV;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.FRVA;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.RVRA;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorisationType.OFFLINE;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorisationType.ONLINE;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult.APPROVED;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult.DECLINED;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult.FAILURE;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult
        .NO_ACCEPTABLE_RESPONSE;
import static com.yelloco.payment.transaction.AuthorisationContext.CaptureType.AUTHORIZATION;
import static com.yelloco.payment.transaction.AuthorisationContext.CaptureType.COMPLETION;
import static com.yelloco.payment.transaction.AuthorisationContext.IncidentAfterAuth.NO_INCIDENT;

import android.content.SharedPreferences;

import com.yelloco.nexo.message.acquirer.AcceptorAuthorisationRequestV02;
import com.yelloco.nexo.message.acquirer.AcceptorAuthorisationResponseV02;
import com.yelloco.nexo.message.acquirer.AcceptorCancellationRequestV02;
import com.yelloco.nexo.message.acquirer.AcceptorCompletionAdviceResponseV02;
import com.yelloco.nexo.message.acquirer.AcceptorCompletionAdviceV02;
import com.yelloco.nexo.message.acquirer.MessageFunction1Code;
import com.yelloco.payment.TransactionPreferences;
import com.yelloco.payment.transaction.AuthorisationContext;
import com.yelloco.payment.transaction.TransactionReferencePersistence;

public class NexoTransactionHelper {

    private AuthorisationContext authorisationContext;
    private SharedPreferences mPreferences;
    private TransactionReferencePersistence transactionReferencePersistence;

    private AcceptorAuthorisationRequestV02 authRequest;
    private AcceptorAuthorisationResponseV02 authResponse;
    private AcceptorCompletionAdviceV02 complAdvice;
    private AcceptorCompletionAdviceResponseV02 complAdviceResponse;
    private AcceptorCancellationRequestV02 acceptorCancellationRequest;

    public NexoTransactionHelper(SharedPreferences sharedPreferences) {
        this.mPreferences = sharedPreferences;
        createAuthTransactionState();
    }

    private AuthorisationContext createAuthTransactionState() {
        this.authorisationContext = new AuthorisationContext();
        //TODO For now we implement online-only authorization, offline needs another scenarios
        // and messages
        // offline enabling via TMS or UI ?
        authorisationContext.authorisationType = ONLINE;
        // TODO provide the possibility to set via TMS
        authorisationContext.captureType = !TransactionPreferences.AUTH_ONLY.getValue(mPreferences)
                ? COMPLETION : AUTHORIZATION;
        // TODO provide the possibility to set via TMS
        authorisationContext.merchantForcedAcceptance = false;
        //No authorization request was sent so far
        if (authResponse == null) {
            return authorisationContext;
        }

        return authorisationContext;
    }

    public NexoTransactionHelper(AuthorisationContext.AuthorisationType authorisationType,
            AuthorisationContext.AuthorizationResult authorizationResult,
            AuthorisationContext.IncidentAfterAuth incidentAfterAuth, boolean merchantForced,
            AuthorisationContext.CaptureType captureType,
            AuthorisationContext.CompletionExchange completionExchange) {

        this.authorisationContext = new AuthorisationContext();
        this.authorisationContext.authorisationType = authorisationType;
        this.authorisationContext.authorizationResult = authorizationResult;
        this.authorisationContext.incidentAfterAuthorization = incidentAfterAuth;
        this.authorisationContext.merchantForcedAcceptance = merchantForced;
        this.authorisationContext.captureType = captureType;
        this.authorisationContext.completionExchange = completionExchange;
    }

    MessageFunction1Code decideMessageFunction(MessageFunction1Code messageFunction1Code) {
        switch (messageFunction1Code) {
            case AUTQ:
                if (isFinancialCaptureRequiredForAuthRequest()) {
                    return FAUQ;
                }
                break;
            case CMPV:
                if (isFinancialCaptureRequiredForCompletion()) {
                    if (isReversalForCompletionRequired()) {
                        return FRVA;
                    }
                    return FCMV;
                } else {
                    if (isReversalForCompletionRequired()) {
                        return RVRA;
                    }
                }
            default:
                return messageFunction1Code;
        }
        return messageFunction1Code;
    }

    boolean isFinancialCaptureRequiredForAuthRequest() {
        if (authorisationContext.authorisationType == ONLINE) {
            if (authorisationContext.authorizationResult == APPROVED
                    || authorisationContext.authorizationResult == DECLINED
                    || authorisationContext.authorizationResult == NO_ACCEPTABLE_RESPONSE) {
                return authorisationContext.captureType == AUTHORIZATION;
            }
        }
        return false;
    }

    boolean isFinancialCaptureRequiredForCompletion() {
        if (authorisationContext.authorisationType == ONLINE) {
            if (authorisationContext.authorizationResult == APPROVED) {
                if (isCompletedWithoutIncident()) {
                    return authorisationContext.captureType == COMPLETION;
                } else {
                    if (!authorisationContext.merchantForcedAcceptance) {
                        return authorisationContext.captureType == AUTHORIZATION;
                    } else {
                        return authorisationContext.captureType == AUTHORIZATION
                                || authorisationContext.captureType == COMPLETION;
                    }
                }
            }
            if (authorisationContext.authorizationResult == DECLINED) {
                if (authorisationContext.merchantForcedAcceptance) {
                    return authorisationContext.captureType == AUTHORIZATION
                            || authorisationContext.captureType == COMPLETION;
                }
            }
            if (authorisationContext.authorizationResult == NO_ACCEPTABLE_RESPONSE) {
                if (!authorisationContext.merchantForcedAcceptance) {
                    return authorisationContext.captureType == AUTHORIZATION;
                } else {
                    return authorisationContext.captureType == AUTHORIZATION
                            || authorisationContext.captureType == COMPLETION;
                }
            }
        }
        if (authorisationContext.authorisationType == OFFLINE) {
            if (authorisationContext.authorizationResult == APPROVED) {
                return authorisationContext.captureType == COMPLETION;
            }
            if (authorisationContext.authorizationResult == DECLINED
                    || authorisationContext.authorizationResult == FAILURE) {
                if (authorisationContext.merchantForcedAcceptance) {
                    return authorisationContext.captureType == COMPLETION;
                }
            }
        }
        return false;
    }

    boolean isReversalForCompletionRequired() {
        if (authorisationContext.authorisationType == ONLINE) {
            if (authorisationContext.authorizationResult == APPROVED) {
                if (!isCompletedWithoutIncident()
                        && !authorisationContext.merchantForcedAcceptance) {
                    return true;
                }
            }
            if (authorisationContext.authorizationResult == NO_ACCEPTABLE_RESPONSE) {
                if (!authorisationContext.merchantForcedAcceptance) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isCompletedWithoutIncident() {
        return authorisationContext.incidentAfterAuthorization == NO_INCIDENT;
    }

    public AuthorisationContext.AuthorizationResult getAuthResult() {
        return authorisationContext.authorizationResult;
    }

    public void setAuthResult(AuthorisationContext.AuthorizationResult authResult) {
        authorisationContext.authorizationResult = authResult;
    }

    public void setIncidentAfterAuth(AuthorisationContext.IncidentAfterAuth incident) {
        authorisationContext.incidentAfterAuthorization = incident;
    }

    public void setAuthorizationType(AuthorisationContext.AuthorisationType authorizationType) {
        authorisationContext.authorisationType = authorizationType;
    }

    public void setMerchantForced(boolean merchantForced) {
        authorisationContext.merchantForcedAcceptance = merchantForced;
    }

    public void setCaptureType(AuthorisationContext.CaptureType captureType) {
        authorisationContext.captureType = captureType;
    }

    public void setCompletionExchange(AuthorisationContext.CompletionExchange completionExchange) {
        authorisationContext.completionExchange = completionExchange;
    }

    public AcceptorAuthorisationRequestV02 getAuthRequest() {
        return authRequest;
    }

    public void setAuthRequest(AcceptorAuthorisationRequestV02 authRequest) {
        this.authRequest = authRequest;
    }

    public AcceptorAuthorisationResponseV02 getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(
            AcceptorAuthorisationResponseV02 authResponse) {
        this.authResponse = authResponse;
    }

    public AcceptorCompletionAdviceV02 getComplAdvice() {
        return complAdvice;
    }

    public void setComplAdvice(AcceptorCompletionAdviceV02 complAdvice) {
        this.complAdvice = complAdvice;
    }

    public AcceptorCompletionAdviceResponseV02 getComplAdviceResponse() {
        return complAdviceResponse;
    }

    public void setComplAdviceResponse(
            AcceptorCompletionAdviceResponseV02 complAdviceResponse) {
        this.complAdviceResponse = complAdviceResponse;
    }

    public AcceptorCancellationRequestV02 getAcceptorCancellationRequest() {
        return acceptorCancellationRequest;
    }

    public void setAcceptorCancellationRequest(
            AcceptorCancellationRequestV02 acceptorCancellationRequest) {
        this.acceptorCancellationRequest = acceptorCancellationRequest;
    }

    public TransactionReferencePersistence getTransactionReferencePersistence() {
        return transactionReferencePersistence;
    }

    public void setTransactionReferencePersistence(
            TransactionReferencePersistence transactionReferencePersistence) {
        this.transactionReferencePersistence = transactionReferencePersistence;
    }
}