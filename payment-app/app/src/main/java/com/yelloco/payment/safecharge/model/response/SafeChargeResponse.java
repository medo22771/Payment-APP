package com.yelloco.payment.safecharge.model.response;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 12/12/2016.
 */

@Root(name = "Response")
public class SafeChargeResponse
{
    @Element(name = "IssuerBankCountry", required = false)
    protected String issuerBankCountry;

    @Element(name = "Token", required = false)
    protected String token;

    @Element(name = "ReasonCodes", required = false)
    protected ReasonCodes reasonCodes;

    @Element(name = "AGVCode", required = false)
    protected String agvCode;

    @Element(name = "AVSCode", required = false)
    protected String avsCode;

    @Element(name = "ClientUniqueID", required = false)
    protected String clientUniqueID;

    @Element(name = "Status", required = false)
    protected String status;

    @Element(name = "TransactionID", required = true)
    protected String transactionID;

    @Element(name = "CVV2Reply", required = false)
    protected String CVV2Reply;

    @Element(name = "AuthCode", required = false)
    protected String authCode;

    @Element(name = "AcquirerID", required = false)
    protected String acquirerID;

    @Element(name = "ErrCode", required = false)
    protected String errCode;

    @Element(name = "UniqueCC", required = false)
    protected String uniqueCC;

    @Element(name = "ClientLoginID", required = false)
    protected String clientLoginID;

    @Element(name = "CustomData", required = false)
    protected String customData;

    @Element(name = "IssuerBankName", required = false)
    protected String IssuerBankName;

    @Element(name = "Reference", required = false)
    protected String reference;

    @Element(name = "CreditCardInfo", required = false)
    protected CreditCardInfo creditCardInfo;

    @Element(name = "Version", required = false)
    protected String version;

    @Element(name = "ExErrCode", required = false)
    protected String exErrCode;

    @Element(name = "AGVError", required = false)
    protected String agvError;

    @Element(name = "CustomData2", required = false)
    protected String customData2;

    @Element(name = "RRN", required = false)
    protected String rrn;

    @Element(name = "ICC", required = false)
    protected String icc;

    @Element(name = "CardProgram", required = false)
    protected String cardProgram;

    @Element(name = "CardProduct", required = false)
    protected String cardProduct;

    @Element(name = "FinalDecision", required = false)
    protected String finalDecision;

    @Element(name = "Recommendations", required = false)
    protected Recommendations recommendations;

    @Element(name = "Decision", required = false)
    protected String decision;

    @Element(name = "Score", required = false)
    protected String score;

    @Element(name = "CardType", required = false)
    protected String cardType;

    @Element(name="IsPartialApproval", required = false)
    protected String IsPartialApproval;

    @Element(name = "FraudResponse", required = false)
    protected FraudResponse fraudResponse;

    @Element(name = "AmountInfo", required = false)
    protected AmountInfo amountInfo;

    @Element(name = "CVVReply", required = false)
    protected String cvvReply;



    public FraudResponse getFraudResponse() {
        return fraudResponse;
    }

    public void setFraudResponse(FraudResponse fraudResponse) {
        this.fraudResponse = fraudResponse;
    }

    public String getIssuerBankCountry ()
    {
        return issuerBankCountry;
    }

    public void setIssuerBankCountry (String IssuerBankCountry)
    {
        this.issuerBankCountry = IssuerBankCountry;
    }

    public String getToken ()
    {
        return token;
    }

    public void setToken (String Token)
    {
        this.token = Token;
    }

    public ReasonCodes getReasonCodes ()
    {
        return reasonCodes;
    }

    public void setReasonCodes (ReasonCodes ReasonCodes)
    {
        this.reasonCodes = ReasonCodes;
    }

    public String getAGVCode ()
    {
        return agvCode;
    }

    public void setAGVCode (String AGVCode)
    {
        this.agvCode = AGVCode;
    }

    public String getAVSCode ()
    {
        return avsCode;
    }

    public void setAVSCode (String AVSCode)
    {
        this.avsCode = AVSCode;
    }

    public String getClientUniqueID ()
    {
        return clientUniqueID;
    }

    public void setClientUniqueID (String ClientUniqueID)
    {
        this.clientUniqueID = ClientUniqueID;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String Status)
    {
        this.status = Status;
    }

    public String getTransactionID ()
    {
        return transactionID;
    }

    public void setTransactionID (String TransactionID)
    {
        this.transactionID = TransactionID;
    }

    public String getCVV2Reply ()
    {
        return CVV2Reply;
    }

    public void setCVV2Reply (String CVV2Reply)
    {
        this.CVV2Reply = CVV2Reply;
    }

    public String getAuthCode ()
    {
        return authCode;
    }

    public void setAuthCode (String AuthCode)
    {
        this.authCode = AuthCode;
    }

    public String getAcquirerID ()
    {
        return acquirerID;
    }

    public void setAcquirerID (String AcquirerID)
    {
        this.acquirerID = AcquirerID;
    }

    public String getErrCode ()
    {
        return errCode;
    }

    public void setErrCode (String ErrCode)
    {
        this.errCode = ErrCode;
    }

    public String getUniqueCC ()
    {
        return uniqueCC;
    }

    public void setUniqueCC (String UniqueCC)
    {
        this.uniqueCC = UniqueCC;
    }

    public String getClientLoginID ()
    {
        return clientLoginID;
    }

    public void setClientLoginID (String ClientLoginID)
    {
        this.clientLoginID = ClientLoginID;
    }

    public String getCustomData ()
    {
        return customData;
    }

    public void setCustomData (String CustomData)
    {
        this.customData = CustomData;
    }

    public String getIssuerBankName ()
    {
        return IssuerBankName;
    }

    public void setIssuerBankName (String IssuerBankName)
    {
        this.IssuerBankName = IssuerBankName;
    }

    public String getReference ()
    {
        return reference;
    }

    public void setReference (String Reference)
    {
        this.reference = Reference;
    }

    public CreditCardInfo getCreditCardInfo ()
    {
        return creditCardInfo;
    }

    public void setCreditCardInfo (CreditCardInfo CreditCardInfo)
    {
        this.creditCardInfo = CreditCardInfo;
    }

    public String getVersion ()
    {
        return version;
    }

    public void setVersion (String Version)
    {
        this.version = Version;
    }

    public String getExErrCode ()
    {
        return exErrCode;
    }

    public void setExErrCode (String ExErrCode)
    {
        this.exErrCode = ExErrCode;
    }

    public String getAGVError ()
    {
        return agvError;
    }

    public void setAGVError (String AGVError)
    {
        this.agvError = AGVError;
    }

    public String getCustomData2 ()
    {
        return customData2;
    }

    public void setCustomData2 (String CustomData2)
    {
        this.customData2 = CustomData2;
    }

    public String getAgvCode() {
        return agvCode;
    }

    public void setAgvCode(String agvCode) {
        this.agvCode = agvCode;
    }

    public String getAvsCode() {
        return avsCode;
    }

    public void setAvsCode(String avsCode) {
        this.avsCode = avsCode;
    }

    public String getAgvError() {
        return agvError;
    }

    public void setAgvError(String agvError) {
        this.agvError = agvError;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getIcc() {
        return icc;
    }

    public void setIcc(String icc) {
        this.icc = icc;
    }

    public String getCardProgram() {
        return cardProgram;
    }

    public void setCardProgram(String cardProgram) {
        this.cardProgram = cardProgram;
    }

    public String getCardProduct() {
        return cardProduct;
    }

    public void setCardProduct(String cardProduct) {
        this.cardProduct = cardProduct;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public Recommendations getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(Recommendations recommendations) {
        this.recommendations = recommendations;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getIsPartialApproval() {
        return IsPartialApproval;
    }

    public void setIsPartialApproval(String isPartialApproval) {
        IsPartialApproval = isPartialApproval;
    }

    public AmountInfo getAmountInfo() {
        return amountInfo;
    }

    public void setAmountInfo(AmountInfo amountInfo) {
        this.amountInfo = amountInfo;
    }

    public String getCvvReply() {
        return cvvReply;
    }

    public void setCvvReply(String cvvReply) {
        this.cvvReply = cvvReply;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [IssuerBankCountry = "+issuerBankCountry+", Token = "+token+", ReasonCodes = " + reasonCodes+", AGVCode = "+ agvCode+", AVSCode = "+avsCode+", ClientUniqueID = "+clientUniqueID+", Status = "+status+", TransactionID = "+transactionID+", CVV2Reply = "+CVV2Reply+", AuthCode = "+authCode+", AcquirerID = "+acquirerID+", ErrCode = "+errCode+", UniqueCC = "+uniqueCC+", ClientLoginID = "+clientLoginID+", CustomData = "+customData+", IssuerBankName = "+IssuerBankName+", Reference = "+reference+", CreditCardInfo = "+creditCardInfo+", Version = "+version+", ExErrCode = "+exErrCode+", AGVError = "+agvError+", CustomData2 = "+customData2+"]";
    }
}
