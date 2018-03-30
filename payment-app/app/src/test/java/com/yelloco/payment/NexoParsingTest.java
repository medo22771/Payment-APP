package com.yelloco.payment;

import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.message.acquirer.DocumentAuthResp;
import com.yelloco.nexo.message.acquirer.PlainCardData1;
import com.yelloco.nexo.message.retailer.AmountsReqType;
import com.yelloco.nexo.message.retailer.ErrorConditionEnumeration;
import com.yelloco.nexo.message.retailer.MessageCategoryEnumeration;
import com.yelloco.nexo.message.retailer.MessageClassEnumeration;
import com.yelloco.nexo.message.retailer.MessageHeaderType;
import com.yelloco.nexo.message.retailer.MessageTypeEnumeration;
import com.yelloco.nexo.message.retailer.PaymentDataType;
import com.yelloco.nexo.message.retailer.PaymentRequestType;
import com.yelloco.nexo.message.retailer.PaymentTransactionType;
import com.yelloco.nexo.message.retailer.PaymentTypeEnumeration;
import com.yelloco.nexo.message.retailer.SaleDataType;
import com.yelloco.nexo.message.retailer.SaleToPOIRequest;
import com.yelloco.nexo.message.retailer.SaleToPOIResponse;
import com.yelloco.nexo.message.retailer.TransactionIdentificationType;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.utils.YelloCurrency;

import junit.framework.Assert;

import org.jibx.runtime.JiBXException;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NexoParsingTest {

    private static final String TEST_REQUEST_SIMPLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<SaleToPOIRequest><MessageHeader MessageClass=\"Service\" " +
            "MessageCategory=\"Payment\" " +
            "MessageType=\"Request\" ServiceID=\"642\" SaleID=\"SaleTermA\" " +
            "POIID=\"POITerm1\"/><PaymentRequest><SaleData " +
            "CustomerOrderReq=\"\"><SaleTransactionID " +
            "TransactionID=\"579\" TimeStamp=\"2017-10-04T12:00:22.118Z\"/>" +
            "</SaleData><PaymentTransaction><AmountsReq Currency=\"EUR\" " +
            "RequestedAmount=\"104.11\" TipAmount=\"1.5\"/></PaymentTransaction><PaymentData " +
            "PaymentType=\"Normal\"/></PaymentRequest></SaleToPOIRequest>";

    private static final String TEST_RESPONSE_SIMPLE = "<?xml version=\"1.0\" " +
            "encoding=\"UTF-8\"?><SaleToPOIResponse xmlns:xsi=\"http://www.w3" +
            ".org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"nexoSaleToPOIMessages" +
            ".xsd\"><MessageHeader MessageClass=\"Service\" MessageCategory=\"Payment\" " +
            "MessageType=\"Response\" ServiceID=\"642\" SaleID=\"SaleTermA\" " +
            "POIID=\"POITerm1\"/><PaymentResponse><Response " +
            "Result=\"Success\"/><SaleData><SaleTransactionID TransactionID=\"579\" " +
            "TimeStamp=\"2009-03-10T23:08:42.4+01:00\"/></SaleData><POIData " +
            "POIReconciliationID=\"200903101\"><POITransactionID TransactionID=\"481\" " +
            "TimeStamp=\"2009-03-10T23:08:42.4+01:00\"/></POIData><PaymentResult " +
            "PaymentType=\"Normal\"><PaymentInstrumentData " +
            "PaymentInstrumentType=\"Card\"><CardData PaymentBrand=\"CardPlus\" " +
            "EntryMode=\"MagStripe\"></CardData></PaymentInstrumentData><AmountsResp " +
            "AuthorizedAmount=\"105.61\"/><PaymentAcquirerData AcquirerID=\"400012\" " +
            "MerchantID=\"mer77-130209\" " +
            "AcquirerPOIID=\"963276433\"><ApprovalCode>8347</ApprovalCode></PaymentAcquirerData" +
            "></PaymentResult></PaymentResponse></SaleToPOIResponse>";

    @Test
    public void serializeAndValidateFromJava() throws Exception {

        TransactionIdentificationType transactionId = new TransactionIdentificationType();
        transactionId.setTimeStamp(new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS").parse
                ("2017-10-04T14:00:22.118"));
        transactionId.setTransactionID("579");

        SaleDataType saleData = new SaleDataType();
        saleData.setSaleTransactionID(transactionId);

        AmountsReqType amount = new AmountsReqType();
        amount.setRequestedAmount(new BigDecimal("104.11"));
        amount.setTipAmount(new BigDecimal("1.5"));
        amount.setCurrency(YelloCurrency.EUR.getAlphabeticCode());

        PaymentTransactionType transaction = new PaymentTransactionType();
        transaction.setAmountsReq(amount);

        PaymentDataType paymentData = new PaymentDataType();
        paymentData.setPaymentType(PaymentTypeEnumeration.NORMAL);

        PaymentRequestType paymentRequestType = new PaymentRequestType();
        paymentRequestType.setSaleData(saleData);
        paymentRequestType.setPaymentTransaction(transaction);
        paymentRequestType.setPaymentData(paymentData);

        MessageHeaderType messageHeader = new MessageHeaderType();
        messageHeader.setMessageClass(MessageClassEnumeration.SERVICE);
        messageHeader.setMessageCategory(MessageCategoryEnumeration.PAYMENT);
        messageHeader.setMessageType(MessageTypeEnumeration.REQUEST);
        messageHeader.setSaleID("SaleTermA");
        messageHeader.setPOIID("POITerm1");
        messageHeader.setServiceID("642");

        SaleToPOIRequest request = new SaleToPOIRequest();
        request.setMessageHeader(messageHeader);
        request.setPaymentRequest(paymentRequestType);

        String serialized = XmlParser.serialize(SaleToPOIRequest.class, request);
        System.out.println(serialized);

        Assert.assertEquals(TEST_REQUEST_SIMPLE, serialized);
    }


    @Test
    public void parseXml() throws JiBXException {
        SaleToPOIResponse response = XmlParser.parseXml(SaleToPOIResponse.class,
                TEST_RESPONSE_SIMPLE);
        String saleTrxId = response.getPaymentResponse().getSaleData().getSaleTransactionID()
                .getTransactionID();
        Date timestamp = response.getPaymentResponse().getSaleData().getSaleTransactionID()
                .getTimeStamp();
        BigDecimal authorizedAmount = response.getPaymentResponse().getPaymentResult()
                .getAmountsResp().getAuthorizedAmount();

        System.out.println("Payment response received with transaction ID: " + saleTrxId);
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Amount: " + authorizedAmount);
        switch (response.getPaymentResponse().getResponse().getResult()) {
            case SUCCESS:
                System.out.println("Approved");
                break;
            case FAILURE:
                ErrorConditionEnumeration error = response.getPaymentResponse().getResponse()
                        .getErrorCondition();
                System.out.println("Declined");
                System.out.println("Reason: " + error);
        }
        Assert.assertEquals("Authorized amount differs.", new BigDecimal("105.61").toString(),
                authorizedAmount.toString());
    }

    private static final String AUTHORIZATION_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<Document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns=\"urn:iso:std:iso:20022:tech:xsd:caaa.001.001.02\">" +
                    " <AccptrAuthstnReq>" +
                    "<Hdr>" +
                    "<MsgFctn>AUTQ</MsgFctn>" +
                    "<PrtcolVrsn>2.0</PrtcolVrsn>" +
                    "<XchgId>149</XchgId>" +
                    "<CreDtTm>2013-08-11T17:22:54.13+01:00</CreDtTm>" +
                    "<InitgPty>" +
                    "<Id>66000001</Id>" +
                    "<Tp>OPOI</Tp>" +
                    "<Issr>ACQR</Issr>" +
                    "</InitgPty>" +
                    "<RcptPty>" +
                    "<Id>nexo-acquirer-1</Id>" +
                    "<Tp>ACQR</Tp>" +
                    "</RcptPty>" +
                    "</Hdr>" +
                    "<AuthstnReq>" +
                    "<Envt>" +
                    "<Acqrr>" +
                    "<Id>" +
                    "<Id>9287351</Id>" +
                    "<Tp>ACQR</Tp>" +
                    "</Id>" +
                    "<ParamsVrsn>2013-08-07 08:00:00</ParamsVrsn>" +
                    "</Acqrr>" +
                    "<Mrchnt>" +
                    "<Id>" +
                    "<Id>nexoMER001</Id>" +
                    "</Id>" +
                    "<CmonNm>nexoOrg Merchant 1</CmonNm>" +
                    "<LctnCtgy>FIXD</LctnCtgy>" +
                    "<CtryCd>056</CtryCd>" +
                    "</Mrchnt>" +
                    "<POI>" +
                    "<Id>" +
                    "<Id>1</Id>" +
                    "<Issr>ACQR</Issr>" +
                    "</Id>" +
                    "<SysNm>POI01</SysNm>" +
                    "<Cpblties>" +
                    "<CardRdngCpblties>CICC</CardRdngCpblties>" +
                    "<CardRdngCpblties>MGST</CardRdngCpblties>" +
                    "<CardRdngCpblties>PHYS</CardRdngCpblties>" +
                    "<CrdhldrVrfctnCpblties>MNSG</CrdhldrVrfctnCpblties>" +
                    "<CrdhldrVrfctnCpblties>FCPN</CrdhldrVrfctnCpblties>" +
                    "<CrdhldrVrfctnCpblties>FEPN</CrdhldrVrfctnCpblties>" +
                    "<CrdhldrVrfctnCpblties>NPIN</CrdhldrVrfctnCpblties>" +
                    "<OnLineCpblties>SMON</OnLineCpblties>" +
                    "<DispCpblties>" +
                    "<DispTp>CDSP</DispTp>" +
                    "<NbOfLines>2</NbOfLines>" +
                    "<LineWidth>20</LineWidth>" +
                    "</DispCpblties>" +
                    "<DispCpblties>" +
                    "<DispTp>MDSP</DispTp>" +
                    "<NbOfLines>4</NbOfLines>" +
                    "<LineWidth>40</LineWidth>" +
                    "</DispCpblties>" +
                    "<PrtLineWidth>48</PrtLineWidth>" +
                    "</Cpblties>" +
                    "<Cmpnt>" +
                    "<Tp>TERM</Tp>" +
                    "<Id>" +
                    "<ItmNb>1</ItmNb>" +
                    "<PrvdrId>nexoVendor001</PrvdrId>" +
                    "<Id>SmartPOI-8539</Id>" +
                    "<SrlNb>7825410759</SrlNb>" +
                    "</Id>" +
                    "</Cmpnt>" +
                    "<Cmpnt>" +
                    "<Tp>APLI</Tp>" +
                    "<Id>" +
                    "<ItmNb>1</ItmNb>" +
                    "<PrvdrId>nexoVendor001</PrvdrId>" +
                    "</Id>" +
                    "<Sts>" +
                    "<VrsnNb>1.0</VrsnNb>" +
                    "</Sts>" +
                    "<StdCmplc>" +
                    "<Id>SEPA-FAST</Id>" +
                    "<Vrsn>3.0</Vrsn>" +
                    "<Issr>CIR</Issr>" +
                    "</StdCmplc>" +
                    "</Cmpnt>" +
                    "</POI>" +
                    "<Card>" +
                    "<PrtctdCardData>" +
                    "<CnttTp>EVLP</CnttTp>" +
                    "<EnvlpdData>" +
                    "<Rcpt>" +
                    "<KEK>" +
                    "<KEKId>" +
                    "<KeyId>SpecV1TestKey</KeyId>" +
                    "<KeyVrsn>2010060715</KeyVrsn>" +
                    "<DerivtnId>OYclpQE=</DerivtnId>" +
                    "</KEKId>" +
                    "<KeyNcrptnAlgo>" +
                    "<Algo>DKP9</Algo>" +
                    "</KeyNcrptnAlgo>" +
                    "<NcrptdKey>4pAgABc=</NcrptdKey>" +
                    "</KEK>" +
                    "</Rcpt>" +
                    "<NcrptdCntt>" +
                    "<CnttTp>DATA</CnttTp>" +
                    "<CnttNcrptnAlgo>" +
                    "<Algo>E3DC</Algo>" +
                    "<Param>" +
                    "<InitlstnVctr>onu0bRwwbgk=</InitlstnVctr>" +
                    "</Param>" +
                    "</CnttNcrptnAlgo>" +
                    "<NcrptdData>" +
                    "y4VI8vNjPE3pcY4L8YXodPla1Avuab/P6vt8V8EJmF++uqdgRdQvD" +
                    "i6PR9JkMbQtf98wo/khtlxmmN7OCMmkRIOBRnO/3gXSauq3zqzfxC" +
                    "SZFbPfaXOUBoYQYTCpz+oPcy2NPoengXxL3w/4KdOvXfowFPtK7BX" +
                    "4t1ynN4btsgJWay9Oz5h0uq/jgyk/Umcl/9BYtDGbVj/zrks9J1Gg" +
                    "gCCmSBKKMPPO2f5b3KWkA578iLsmvs+s9TlcxjmKHl5FdpiuCDHuU" +
                    "4PZgzw1s74Wa9Kdmb4plSYo9I17HAI79zs=" +
                    "</NcrptdData>" +
                    "</NcrptdCntt>" +
                    "</EnvlpdData>" +
                    "</PrtctdCardData>" +
                    "<CardCtryCd>056</CardCtryCd>" +
                    "<CardPdctPrfl>0003</CardPdctPrfl>" +
                    "<CardBrnd>TestCard</CardBrnd>" +
                    "</Card>" +
                    "<Crdhldr>" +
                    "<Lang>fr</Lang>" +
                    "<Authntcn>" +
                    "<AuthntcnMtd>FPIN</AuthntcnMtd>" +
                    "<AuthntcnNtty>ICCD</AuthntcnNtty>" +
                    "</Authntcn>" +
                    "</Crdhldr>" +
                    "</Envt>" +
                    "<Cntxt>" +
                    "<PmtCntxt>" +
                    "<AttndncCntxt>ATTD</AttndncCntxt>" +
                    "<AttndntLang>fr</AttndntLang>" +
                    "<CardDataNtryMd>CICC</CardDataNtryMd>" +
                    "</PmtCntxt>" +
                    "<SaleCntxt>" +
                    "<SaleId>ST06</SaleId>" +
                    "<SaleRefNb>S78-T06-0029</SaleRefNb>" +
                    "<SaleRcncltnId>S78-8469</SaleRcncltnId>" +
                    "</SaleCntxt>" +
                    "</Cntxt>" +
                    "<Tx>" +
                    "<TxCaptr>false</TxCaptr>" +
                    "<TxTp>CRDP</TxTp>" +
                    "<MrchntCtgyCd>5411</MrchntCtgyCd>" +
                    "<TxId>" +
                    "<TxDtTm>2013-08-11T17:22:04.51+01:00</TxDtTm>" +
                    "<TxRef>002949</TxRef>" +
                    "</TxId>" +
                    "<RcncltnId>8469</RcncltnId>" +
                    "<TxDtls>" +
                    "<Ccy>EUR</Ccy>" +
                    "<TtlAmt>43.14</TtlAmt>" +
                    "<OnLineRsn>ICCF</OnLineRsn>" +
                    "<ICCRltdData>" +
                    "XyoCCXhfNAEAggJ8AIQHoAAAAAk1EJUFAAAAgACaAxEEEZwBAJ8CBgAAAABDFJ" +
                    "8JAgACnxASAhCngAMEAACfIQAAAAAAAAD/nxoCAlCfJgguPqsXzuxQDJ8nAYCf" +
                    "MwNgoECfNANEAwKfNQEinzYCABafNwS8demzn0AFoACQ8AGfTAifIbAuDurEVA" +
                    "==" +
                    "</ICCRltdData>" +
                    "</TxDtls>" +
                    "</Tx>" +
                    "</AuthstnReq>" +
                    "<SctyTrlr>" +
                    "<CnttTp>AUTH</CnttTp>" +
                    "<AuthntcdData>" +
                    "<Rcpt>" +
                    "<KEK>" +
                    "<KEKId>" +
                    "<KeyId>SpecV1TestKey</KeyId>" +
                    "<KeyVrsn>2010060715</KeyVrsn>" +
                    "<DerivtnId>OYclpQE=</DerivtnId>" +
                    "</KEKId>" +
                    "<KeyNcrptnAlgo>" +
                    "<Algo>DKP9</Algo>" +
                    "</KeyNcrptnAlgo>" +
                    "<NcrptdKey>4pAgABc=</NcrptdKey>" +
                    "</KEK>" +
                    "</Rcpt>" +
                    "<MACAlgo>" +
                    "<Algo>MCCS</Algo>" +
                    "</MACAlgo>" +
                    "<NcpsltdCntt>" +
                    "<CnttTp>DATA</CnttTp>" +
                    "</NcpsltdCntt>" +
                    "<MAC>qhsKRSGiXt8=</MAC>" +
                    "</AuthntcdData>" +
                    "</SctyTrlr>" +
                    " </AccptrAuthstnReq>" +
                    "</Document>";

    private static final String AUTHORIZATION_RESPONSE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<Document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns=\"urn:iso:std:iso:20022:tech:xsd:caaa.002.001.02\">" +
                    " <AccptrAuthstnRspn>" +
                    "<Hdr>" +
                    "<MsgFctn>AUTQ</MsgFctn>" +
                    "<PrtcolVrsn>2.0</PrtcolVrsn>" +
                    "<XchgId>149</XchgId>" +
                    "<CreDtTm>2013-08-11T17:22:55.11+01:00</CreDtTm>" +
                    "<InitgPty>" +
                    "<Id>66000001</Id>" +
                    "<Tp>OPOI</Tp>" +
                    "<Issr>ACQR</Issr>" +
                    "</InitgPty>" +
                    "<RcptPty>" +
                    "<Id>nexo-acquirer-1</Id>" +
                    "<Tp>ACQR</Tp>" +
                    "</RcptPty>" +
                    "</Hdr>" +
                    "<AuthstnRspn>" +
                    "<Envt>" +
                    "<AcqrrId>" +
                    "<Id>9287351</Id>" +
                    "<Tp>ACQR</Tp>" +
                    "</AcqrrId>" +
                    "<MrchntId>" +
                    "<Id>nexoMER001</Id>" +
                    "</MrchntId>" +
                    "<POIId>" +
                    "<Id>1</Id>" +
                    "<Issr>ACQR</Issr>" +
                    "</POIId>" +
                    "<PrtctdCardData>" +
                    "<CnttTp>EVLP</CnttTp>" +
                    "<EnvlpdData>" +
                    "<Rcpt>" +
                    "<KEK>" +
                    "<KEKId>" +
                    "<KeyId>SpecV1TestKey</KeyId>" +
                    "<KeyVrsn>2010060715</KeyVrsn>" +
                    "<DerivtnId>OYclpQE=</DerivtnId>" +
                    "</KEKId>" +
                    "<KeyNcrptnAlgo>" +
                    "<Algo>DKP9</Algo>" +
                    "</KeyNcrptnAlgo>" +
                    "<NcrptdKey>4pAgABc=</NcrptdKey>" +
                    "</KEK>" +
                    "</Rcpt>" +
                    "<NcrptdCntt>" +
                    "<CnttTp>DATA</CnttTp>" +
                    "<CnttNcrptnAlgo>" +
                    "<Algo>E3DC</Algo>" +
                    "<Param>" +
                    "<InitlstnVctr>onu0bRwwbgk=</InitlstnVctr>" +
                    "</Param>" +
                    "</CnttNcrptnAlgo>" +
                    "<NcrptdData>" +
                    "CAagOqDDQiVw/J5uM3vWxfRy1hzoxccje0xums6+s/EmqMK5" +
                    "+7xfxcx3y+FNTw1+5sU7Tu98xgODcLDaU2Nxmb7jyYe9mDlN" +
                    "/Q1/NJHPTbtLkh981WSeWnjFqB2YGXmkNKmnvxBT+LB2H+n+" +
                    "1hVpwA==" +
                    "</NcrptdData>" +
                    "</NcrptdCntt>" +
                    "</EnvlpdData>" +
                    "</PrtctdCardData>" +
                    "</Envt>" +
                    "<Tx>" +
                    "<TxId>" +
                    "<TxDtTm>2013-08-11T17:22:04.51+01:00</TxDtTm>" +
                    "<TxRef>002949</TxRef>" +
                    "</TxId>" +
                    "<TxDtls>" +
                    "<Ccy>EUR</Ccy>" +
                    "<TtlAmt>43.14</TtlAmt>" +
                    "<ICCRltdData>kQp05S/FnWreKgASigIwMA==</ICCRltdData>" +
                    "</TxDtls>" +
                    "</Tx>" +
                    "<TxRspn>" +
                    "<AuthstnRslt>" +
                    "<AuthstnNtty>" +
                    "<Tp>CISS</Tp>" +
                    "</AuthstnNtty>" +
                    "<RspnToAuthstn>" +
                    "<Rspn>APPR</Rspn>" +
                    "</RspnToAuthstn>" +
                    "<AuthstnCd>032983</AuthstnCd>" +
                    "<CmpltnReqrd>true</CmpltnReqrd>" +
                    "</AuthstnRslt>" +
                    "</TxRspn>" +
                    "</AuthstnRspn>" +
                    "<SctyTrlr>" +
                    "<CnttTp>AUTH</CnttTp>" +
                    "<AuthntcdData>" +
                    "<Rcpt>" +
                    "<KEK>" +
                    "<KEKId>" +
                    "<KeyId>SpecV1TestKey</KeyId>" +
                    "<KeyVrsn>2010060715</KeyVrsn>" +
                    "<DerivtnId>OYclpQE=</DerivtnId>" +
                    "</KEKId>" +
                    "<KeyNcrptnAlgo>" +
                    "<Algo>DKP9</Algo>" +
                    "</KeyNcrptnAlgo>" +
                    "<NcrptdKey>4pAgABc=</NcrptdKey>" +
                    "</KEK>" +
                    "</Rcpt>" +
                    "<MACAlgo>" +
                    "<Algo>MCCS</Algo>" +
                    "</MACAlgo>" +
                    "<NcpsltdCntt>" +
                    "<CnttTp>DATA</CnttTp>" +
                    "</NcpsltdCntt>" +
                    "<MAC>lSd3SfK140g=</MAC>" +
                    "</AuthntcdData>" +
                    "</SctyTrlr>" +
                    " </AccptrAuthstnRspn>" +
                    "</Document>";
    @Test
    public void parseAcquirerRequest() throws JiBXException {

        DocumentAuthReq request =
                XmlParser.parseXml(DocumentAuthReq.class, AUTHORIZATION_REQUEST);
        System.out.println(request.getAccptrAuthstnReq().toString());
    }

    @Test
    public void parseAcquirerResponse() throws JiBXException {
        DocumentAuthResp response =
                XmlParser.parseXml(DocumentAuthResp.class, AUTHORIZATION_RESPONSE);
        System.out.println(response.getAccptrAuthstnRspn().getAuthstnRspn().getTxRspn()
                .getAuthstnRslt().getRspnToAuthstn().getRspn());
        String backToXml = XmlParser.serialize(DocumentAuthResp.class, response);
        System.out.println(backToXml);
        Assert.assertNotSame("Requests do not equal.", AUTHORIZATION_RESPONSE,
                AUTHORIZATION_REQUEST);
    }

    @Test
    public void plainCardDataSerialize() throws JiBXException {
        PlainCardData1 plainCardData1 = new PlainCardData1();
        plainCardData1.setPAN("123");
        plainCardData1.setXpryDt("321321");
        String serialized = XmlParser.serialize(PlainCardData1.class, plainCardData1);
        System.out.println(serialized);
    }

    private static final String PLAIN_CARD_DATA = "<PlainCardData " +
            "xmlns=\"urn:iso:std:iso:20022:tech:xsd:caaa.001.001.02\"><PAN>123</PAN><XpryDt" +
            ">321321</XpryDt></PlainCardData>";

    @Test
    public void plainCardDataParse() throws JiBXException {
        PlainCardData1 plainCardData1 = XmlParser.parseXml(PlainCardData1.class, PLAIN_CARD_DATA);
        System.out.println(plainCardData1);
    }
}