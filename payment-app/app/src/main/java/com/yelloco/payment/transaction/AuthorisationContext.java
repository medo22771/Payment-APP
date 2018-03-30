package com.yelloco.payment.transaction;

/**
 * Nexo acquirer message flow requires overall authorisation context to determine the final
 * flow and the parameters to be used in the messages.
 * The structure of the context follows chapter 5.2.1. "Elements Impacting the Message Flow" in
 * Nexo Card Payments Message Usage Guide 2.0.
 */
public class AuthorisationContext {

    /**
     * In order to set value follow 5.2.1.1 Authorisation Type
     */
    public AuthorisationType authorisationType;
    /**
     * In order to set value follow 5.2.1.2 Authorisation Result
     */
    public AuthorizationResult authorizationResult;
    /**
     * After an online or offline authorisation, the transaction can terminate with incident.
     * Transaction.FailureReason has then one of the IncidentAfterAuth values.
     * In order to set value follow 5.2.1.3 Incident after Authorisation
     */
    public IncidentAfterAuth incidentAfterAuthorization;
    /**
     * In order to set value follow 5.2.1.4 Merchant Forced Acceptance
     */
    public boolean merchantForcedAcceptance;
    /**
     * If should the financial capture take place or not. Should be set by TMS.
     * In order to set value follow 5.2.1.5 Capture Type
     */
    public CaptureType captureType;
    /**
     * In order to set value follow 5.2.1.6 Completion Exchange
     */
    public CompletionExchange completionExchange;

    public enum AuthorisationType {
        ONLINE,
        OFFLINE
    }

    public enum AuthorizationResult {
        /** Approved online by the Acquirer or offline at the POI */
        APPROVED,
        /** Declined online by the Acquirer or offline at the POI */
        DECLINED,
        /**
         * 1/ When the Acceptor has tried to send an AcceptorAuthorisationRequest message to the
         * Acquirer without receiving a response in time (online authorisation only):
         * - Transaction.FailureReason contains one of the values UnableToSend, TimeOut or
          TooLateResponse,
         * - Transaction.Reversal flag is set to True.
         * 2/ When the AcceptorAuthorisationResponse contains ResponseToAuthorisation/Response
         * with the value “TechnicalError”:
         */
        NO_ACCEPTABLE_RESPONSE,
        FAILURE
    }

    public enum IncidentAfterAuth {

        NO_INCIDENT,
        /** transaction cancelled by the Cardholder */
        CUSTOMER_CANCEL,
        /** payment transaction finally declined by the card */
        CARD_DECLINED,
        /**  malfunction of the card or of the card reader */
         MALFUNCTION,
        /**
         * POI or Sale unable to complete transaction after the authorisation (e.g.
         * written signature invalid, risk too high for the Acceptor)
         */
        UNABLE_TO_COMPLETE
    }

    public enum CaptureType {
        /**
         * The authorisation exchange, when the configuration parameter
         * OnlineTransaction.FinancialCapture has the value Authorisation.
         */
        AUTHORIZATION,
        /**
         * The completion exchange, when the configuration parameter
         * OnlineTransaction.FinancialCapture (if the authorisation was online) or
         * OfflineTransaction.FinancialCapture (if the authorisation was offline) has the value Completion.
         */
        COMPLETION,
        /**
         * A batch transfer, when the configuration parameter OnlineTransaction.FinancialCapture (if the
         * authorisation was online) or OfflineTransaction.FinancialCapture (if the authorisation was
         * offline) has the value Batch
         */
        BATCH
    }

    public enum CompletionExchange {
        /**
         * a Completion advice is never sent to the Acquirer (for offline authorisation only)
         */
        NONE,
        /**
         * a Completion advice is sent after the Authorisation exchange if the
         * CompletionRequired flag of the AcceptorAuthorisationResponse message is set to True (for
         * online authorisation only)
         */
        ON_DEMAND
    }
}