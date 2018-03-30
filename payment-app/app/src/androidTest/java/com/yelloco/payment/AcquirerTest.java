package com.yelloco.payment;

import static com.yelloco.payment.nexo.NexoHelper.BDK;
import static com.yelloco.payment.nexo.NexoHelper.KSN;
import static com.yelloco.payment.nexo.NexoHelper.convertPaymentCard;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.alcineo.transaction.TransactionType;
import com.yelloco.nexo.crypto.DESCryptoUtil;
import com.yelloco.nexo.crypto.DUKPTUtil;
import com.yelloco.nexo.message.acquirer.Algorithm2Code;
import com.yelloco.nexo.message.acquirer.Algorithm6Code;
import com.yelloco.nexo.message.acquirer.AlgorithmIdentification2;
import com.yelloco.nexo.message.acquirer.AlgorithmIdentification6;
import com.yelloco.nexo.message.acquirer.ContentInformationType5;
import com.yelloco.nexo.message.acquirer.ContentType1Code;
import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.message.acquirer.DocumentAuthResp;
import com.yelloco.nexo.message.acquirer.DocumentComplAdvice;
import com.yelloco.nexo.message.acquirer.EncryptedContent2;
import com.yelloco.nexo.message.acquirer.EnvelopedData2;
import com.yelloco.nexo.message.acquirer.KEK2;
import com.yelloco.nexo.message.acquirer.KEKIdentifier1;
import com.yelloco.nexo.message.acquirer.Parameter1;
import com.yelloco.nexo.message.acquirer.PaymentCard5;
import com.yelloco.nexo.message.acquirer.PaymentCard6;
import com.yelloco.nexo.message.acquirer.PlainCardData1;
import com.yelloco.nexo.message.acquirer.Recipient2Choice;
import com.yelloco.nexo.message.acquirer.Response1Code;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.gateway.Gateway;
import com.yelloco.payment.gateway.YelloGateway;
import com.yelloco.payment.nexo.NexoProcessor;
import com.yelloco.payment.nexo.NexoTransactionHelper;
import com.yelloco.payment.tcp.TcpClientUtil;
import com.yelloco.payment.transaction.SharedPreferencesTransactionReferencePersistence;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.utils.YelloCurrency;

import junit.framework.Assert;

import org.jibx.runtime.JiBXException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class AcquirerTest {

    private static final String TAG = AcquirerTest.class.getSimpleName();
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
    private static final int MAXIMUM_REF_VALUE = 666;

    @Test
    public void testNexoRequestFromXml() throws IOException, TimeoutException, JiBXException {
        Socket client = new Socket(TcpClientUtil.SERVER_ADDRESS, TcpClientUtil.SERVER_PORT);

        Log.i(TAG, "Request: " + AUTHORIZATION_REQUEST);
        DocumentAuthReq doc = XmlParser.parseXml(DocumentAuthReq.class,AUTHORIZATION_REQUEST);
        TcpClientUtil.sendMessage(XmlParser.serialize(DocumentAuthReq.class,doc),client);

        String response = TcpClientUtil.readMessage(client);
        Log.i(TAG, "Response from server: " + response);
        DocumentAuthResp fullResponse =
                XmlParser.parseXml(DocumentAuthResp.class, response);

        Response1Code responseCode = fullResponse.getAccptrAuthstnRspn().getAuthstnRspn()
                .getTxRspn().getAuthstnRslt().getRspnToAuthstn().getRspn();
        Log.i(TAG, "Response code from server: " + responseCode);
    }

    @Test
    public void testnexoRequestConstructed() throws JiBXException, GeneralSecurityException,
            TimeoutException, IOException {
        TransactionContext transactionContext = createTestTransactionContext();
        transactionContext.createOrUpdateContext(TagStoreMock.getTagStoreNoPin());
        Context targetContext = InstrumentationRegistry.getTargetContext();
        NexoTransactionHelper nexoTransactionHelper = new NexoTransactionHelper(
                targetContext.getSharedPreferences(targetContext.getPackageName(),
                        Context.MODE_PRIVATE));
        NexoProcessor nexoProcessor = new NexoProcessor(targetContext,
                new SharedPreferencesTransactionReferencePersistence(
                        targetContext,
                        targetContext.getPackageName(),
                        MAXIMUM_REF_VALUE), nexoTransactionHelper);
        DocumentAuthReq document = nexoProcessor.createAuthorisationRequest(transactionContext);
        Socket server = new Socket(TcpClientUtil.SERVER_ADDRESS, TcpClientUtil.SERVER_PORT);
        TcpClientUtil.sendMessage(XmlParser.serialize(DocumentAuthReq.class, document), server);
        String response = TcpClientUtil.readMessage(server);
        System.out.println(response);
    }

    @Test
    public void testYelloGatewayRequestResponse() throws Exception {
        TransactionContext transactionContext = createTestTransactionContext();
        transactionContext.createOrUpdateContext(TagStoreMock.getTagStoreNoPin());
        Context targetContext = InstrumentationRegistry.getTargetContext();
        NexoTransactionHelper nexoTransactionHelper = new NexoTransactionHelper(
                targetContext.getSharedPreferences(targetContext.getPackageName(),
                        Context.MODE_PRIVATE));
        NexoProcessor nexoProcessor = new NexoProcessor(targetContext,
                new SharedPreferencesTransactionReferencePersistence(
                        targetContext, targetContext.getPackageName(),
                        MAXIMUM_REF_VALUE), nexoTransactionHelper);
        DocumentAuthReq document = nexoProcessor.createAuthorisationRequest(transactionContext);
        Gateway gateway = new YelloGateway(targetContext.getString(
                R.string.gateway_yello_test_url));
        String response = gateway.sendRequest(XmlParser.serialize(DocumentAuthReq.class, document));
        Log.i(TAG, "Response: " + response);
    }

    @Test
    public void testNexoCompletionConstructed() throws JiBXException, GeneralSecurityException,
            TimeoutException, IOException, InterruptedException {
        DocumentAuthReq document;
        String response;
        Socket server = null;
        try {
            TransactionContext transactionContext = createTestTransactionContext();
            transactionContext.createOrUpdateContext(TagStoreMock.getTagStoreNoPin());
            Context targetContext = InstrumentationRegistry.getTargetContext();
            NexoTransactionHelper nexoTransactionHelper = new NexoTransactionHelper(
                    targetContext.getSharedPreferences(targetContext.getPackageName(),
                            Context.MODE_PRIVATE));
            NexoProcessor nexoProcessor = new NexoProcessor(
                    targetContext,
                    new SharedPreferencesTransactionReferencePersistence(
                            targetContext,
                            targetContext.getPackageName(),
                            MAXIMUM_REF_VALUE), nexoTransactionHelper);
            document = nexoProcessor.createAuthorisationRequest(transactionContext);
            nexoTransactionHelper.setAuthRequest(document.getAccptrAuthstnReq());
            String authRequestString = XmlParser.serialize(DocumentAuthReq.class, document);
            Log.i(TAG, "AuthRequest:\n" + authRequestString);

            server = new Socket(TcpClientUtil.SERVER_ADDRESS, TcpClientUtil.SERVER_PORT);
            server.setSoTimeout(5000);
            TcpClientUtil.sendMessage(authRequestString, server);
            Thread.sleep(1000);
            response = TcpClientUtil.readMessage(server);
            Log.i(TAG, "AuthResponse:\n" + response);

            server.close();
            server = new Socket(TcpClientUtil.SERVER_ADDRESS, TcpClientUtil.SERVER_PORT);
            server.setSoTimeout(5000);

            DocumentAuthResp documentAuthResp = XmlParser.parseXml(DocumentAuthResp.class, response);
            nexoTransactionHelper.setAuthResponse(documentAuthResp.getAccptrAuthstnRspn());

            Thread.sleep(1000);
            DocumentComplAdvice documentComplAdvice = nexoProcessor.createCompletionAdvice(
                    getTestCardData6(), nexoTransactionHelper.getAuthRequest(),
                    nexoTransactionHelper.getAuthResult());
            String completionAdviceString = XmlParser.serialize(DocumentComplAdvice.class,
                    documentComplAdvice);
            Log.i(TAG, "CompletionRequest:\n" + completionAdviceString);

            TcpClientUtil.sendMessage(completionAdviceString, server);
            Thread.sleep(1000);
            response = TcpClientUtil.readMessage(server);
            Assert.assertNotNull("Failed to get CompletionAdviseResponse from server", response);
            Log.i(TAG, "CompletionResponse:\n" + response);
        } finally {
            if (server != null)
                server.close();
        }
    }

    private TransactionContext createTestTransactionContext() {
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setCurrency(YelloCurrency.EUR);
        transactionContext.setAmount(new BigDecimal("10.30"));
        transactionContext.setAmountOther(new BigDecimal("5"));
        transactionContext.setTransactionType(TransactionType.PURCHASE);
        transactionContext.setCategoryCode("01");
        return transactionContext;
    }

    private PaymentCard6 getTestCardData6() throws JiBXException, GeneralSecurityException,
            UnsupportedEncodingException {
        PaymentCard5 card = getTestCardData5();
        return convertPaymentCard(card);
    }


    public PaymentCard5 getTestCardData5() throws JiBXException, UnsupportedEncodingException,
            GeneralSecurityException {
        PaymentCard5 card = new PaymentCard5();

        PlainCardData1 plainCardData1 = new PlainCardData1();
        plainCardData1.setPAN("123456789");
        plainCardData1.setXpryDt("20171212");
        String cardXml = XmlParser.serialize(PlainCardData1.class, plainCardData1);
        byte[] cardData = DUKPTUtil.addNull80Padding(cardXml.getBytes("UTF-8"));
        byte[] iv = new byte[8];
        byte[] encryptedData = DESCryptoUtil.tdesEncrypt(cardData, DUKPTUtil.calculateDataEncryptionKey(
                KSN, BDK, true), iv);

        ContentInformationType5 protectedCardData = new ContentInformationType5();
        protectedCardData.setCnttTp(ContentType1Code.EVLP);
        EnvelopedData2 envlpdData = new EnvelopedData2();
        EncryptedContent2 ncrptdCntt = new EncryptedContent2();
        ncrptdCntt.setCnttTp(ContentType1Code.DATA);
        AlgorithmIdentification6 cnttNcrptnAlgo = new AlgorithmIdentification6();
        cnttNcrptnAlgo.setAlgo(Algorithm6Code.E3_DC);
        Parameter1 param = new Parameter1();
        //instead of ByteArrayWrapper we use directly byte[]
        param.setInitlstnVctr(iv);
        cnttNcrptnAlgo.setParam(param);
        ncrptdCntt.setCnttNcrptnAlgo(cnttNcrptnAlgo);
        //instead of ByteArrayWrapper we use directly byte[]
        ncrptdCntt.setNcrptdData(encryptedData);
        envlpdData.setNcrptdCntt(ncrptdCntt);
        Recipient2Choice rcpt1 = new Recipient2Choice();
        KEK2 item = new KEK2();

        KEKIdentifier1 kekIda = new KEKIdentifier1();
        kekIda.setKeyId("SpecV1TestKey");
        kekIda.setKeyVrsn("2010060715");
        //instead of ByteArrayWrapper we use directly byte[]
        kekIda.setDerivtnId(DUKPTUtil.calculateKsnDerivationId(KSN)); //TODO: check if function is OK
        item.setKEKId(kekIda);
        AlgorithmIdentification2 keyNcrptnAlgoa = new AlgorithmIdentification2();
        keyNcrptnAlgoa.setAlgo(Algorithm2Code.DK_P9);
        item.setKeyNcrptnAlgo(keyNcrptnAlgoa);
        item.setNcrptdKey(DUKPTUtil.calculateKsnEncryptedKey(KSN));
        rcpt1.setKEK(item);
        envlpdData.getRcptList().add(rcpt1);
        protectedCardData.setEnvlpdData(envlpdData);
        card.setPrtctdCardData(protectedCardData);
        return card;
    }

    @Test
    public void dumpDefaultTagStore() {
        Log.i(TAG, TagStoreMock.getTagStorePinOnline().toString());
    }
}