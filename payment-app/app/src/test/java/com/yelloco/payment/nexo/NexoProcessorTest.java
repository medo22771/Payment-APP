package com.yelloco.payment.nexo;

import static com.yelloco.payment.utils.TlvTagEnum.ENCRYPT_ALGO;
import static com.yelloco.payment.utils.TlvTagEnum.INITIALIZATION_VECTOR;
import static com.yelloco.payment.utils.TlvTagEnum.PLAIN_CARD_DATA;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import android.content.Context;
import android.content.SharedPreferences;

import com.alcineo.transaction.TransactionType;
import com.alcineo.utils.tlv.TlvException;
import com.yelloco.nexo.crypto.DESCryptoUtil;
import com.yelloco.nexo.crypto.DUKPTUtil;
import com.yelloco.nexo.message.acquirer.Algorithm6Code;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction15;
import com.yelloco.nexo.message.acquirer.CardPaymentTransaction16;
import com.yelloco.nexo.message.acquirer.DocumentCancelAdvice;
import com.yelloco.nexo.message.acquirer.DocumentCancelReq;
import com.yelloco.nexo.message.acquirer.PlainCardData1;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.CustomEditor;
import com.yelloco.payment.data.tagstore.EmvTagStore;
import com.yelloco.payment.data.tagstore.TagStore;
import com.yelloco.payment.transaction.SharedPreferencesTransactionReferencePersistence;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.transaction.TransactionIdentification;
import com.yelloco.payment.utils.YelloCurrency;

import org.jibx.runtime.JiBXException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class NexoProcessorTest {

    private static final String RANDOM_TEXT = "foobar";
    private static final int MAX_REF_VALUE = 666;
    private NexoProcessor classUnderTest;

    @Spy
    private SharedPreferences preferences;
    private CustomEditor customEditor;
    private NexoTransactionHelper nexoTransactionHelper;

    @Mock
    private Context context;

    private TagStore tagStore;

    @Before
    public void beforeTests() {
        initMocks(this);
        when(preferences.getString(anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[1];
            }
        });
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(preferences);
        customEditor = new CustomEditor();
        when(preferences.edit()).thenReturn(customEditor);
        when(context.getPackageName()).thenReturn(RANDOM_TEXT);
        nexoTransactionHelper = new NexoTransactionHelper(preferences);
        classUnderTest = new NexoProcessor(context,
                new SharedPreferencesTransactionReferencePersistence(context,
                        context.getPackageName(),
                        MAX_REF_VALUE), nexoTransactionHelper);
        tagStore = new EmvTagStore();

        byte[] iv = new byte[8];
        byte[] encryptedData = getCardData(iv);

        tagStore.setTag(PLAIN_CARD_DATA.getTag(), encryptedData);
        tagStore.setTag(INITIALIZATION_VECTOR.getTag(), iv);
        tagStore.setTag(ENCRYPT_ALGO.getTag(), Algorithm6Code.E3_DC.xmlValue().getBytes());
    }

    private byte[] getCardData(byte[] iv) {
        PlainCardData1 plainCardData1 = new PlainCardData1();
        plainCardData1.setPAN("123456789");
        plainCardData1.setXpryDt("20171212");
        try {
            String cardXml = XmlParser.serialize(PlainCardData1.class, plainCardData1);
            byte[] cardData = DUKPTUtil.addNull80Padding(cardXml.getBytes("UTF-8"));
            return DESCryptoUtil.tdesEncrypt(cardData, DUKPTUtil.calculateDataEncryptionKey(
                    NexoHelper.KSN, NexoHelper.BDK, true), iv);
        } catch (JiBXException | UnsupportedEncodingException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return new byte[]{0};
    }

    @Test
    public void doesMockingSharedPreferencesWorkEvenWhenDefaultValueIsSpecified() throws
            JiBXException {
        boolean success = preferences.getString("a", RANDOM_TEXT).equals(RANDOM_TEXT);

        assertThat("Checking that SharedPreferenceEntry.getString... returns something", success,
                is(true));
    }

    @Test
    public void canCancellationMessageBeCreatedWhenNothingIsGiven() throws JiBXException,
            TlvException {
        DocumentCancelReq documentCancelReq = classUnderTest.createCancellationRequest(
                createTestTransactionContext());

        assertNotNull(documentCancelReq);
    }

    @Test
    public void doesTransactionContextMatchWhenCancelMessageIsCreatedFromIt() throws JiBXException,
            TlvException {
        TransactionContext testTransactionContext = createTestTransactionContext();
        DocumentCancelReq documentCancelReq = classUnderTest.createCancellationRequest(
                testTransactionContext);

        assertTrue(
                contextCorrespondsToNexoCancelMessage(testTransactionContext, documentCancelReq));
    }

    @Test
    public void serializeWholeCancelMessage() throws JiBXException,
            TlvException {
        TransactionContext testTransactionContext = createTestTransactionContext();
        DocumentCancelReq documentCancelReq = classUnderTest.createCancellationRequest(
                testTransactionContext);
        String xml = XmlParser.serialize(DocumentCancelReq.class, documentCancelReq);

        assertThat(xml, is(not(emptyString())));
    }

    @Test
    public void canCancellationAdviceMessageBeCreatedWhenNothingIsGiven() throws JiBXException,
            TlvException {
        TransactionContext testTransactionContext = createContextAndCancelRequest();
        DocumentCancelAdvice documentCancelAdvice = classUnderTest.createCancellationAdvice(
                testTransactionContext, nexoTransactionHelper.getAcceptorCancellationRequest(),
                nexoTransactionHelper.getAuthResult());

        assertNotNull(documentCancelAdvice);
    }

    @Test
    public void doesTransactionContextMatchWhenCancelAdviceMessageIsCreatedFromIt()
            throws JiBXException,
            TlvException {
        TransactionContext testTransactionContext = createContextAndCancelRequest();
        DocumentCancelAdvice documentCancelAdvice = classUnderTest.createCancellationAdvice(
                testTransactionContext, nexoTransactionHelper.getAcceptorCancellationRequest(),
                nexoTransactionHelper.getAuthResult());


        assertTrue(contextCorrespondsToNexoCancelAdviceMessage(testTransactionContext,
                documentCancelAdvice));
    }

    @Test
    public void serializeWholeCancelAdviceMessage() throws JiBXException,
            TlvException {
        TransactionContext testTransactionContext = createContextAndCancelRequest();
        DocumentCancelAdvice documentCancelAdvice = classUnderTest.createCancellationAdvice(
                testTransactionContext, nexoTransactionHelper.getAcceptorCancellationRequest(),
                nexoTransactionHelper.getAuthResult());
        String xml = XmlParser.serialize(DocumentCancelAdvice.class, documentCancelAdvice);

        assertThat(xml, is(not(emptyString())));
    }

    private TransactionContext createContextAndCancelRequest() throws JiBXException {
        TransactionContext testTransactionContext = createTestTransactionContext();
        nexoTransactionHelper = new NexoTransactionHelper(preferences);
        nexoTransactionHelper.setAcceptorCancellationRequest(
                classUnderTest.createCancellationRequest(
                        testTransactionContext).getAccptrCxlReq());
        return testTransactionContext;
    }

    private boolean contextCorrespondsToNexoCancelMessage(TransactionContext transactionContext,
            DocumentCancelReq documentCancelReq) {
        CardPaymentTransaction15 messageTransaction = documentCancelReq.getAccptrCxlReq()
                .getCxlReq().getTx();
        if (!messageTransaction.getMrchntCtgyCd().equals(transactionContext.getCategoryCode())) {
            return false;
        }
        if (!messageTransaction.getTxDtls().getCcy().equals(transactionContext.getCurrency()
                .getAlphabeticCode())) {
            return false;
        }
        if (!messageTransaction.getTxDtls().getTtlAmt().equals(transactionContext.getAmount())) {
            return false;
        }
        if (!messageTransaction.getTxDtls().getTtlAmt().equals(transactionContext.getAmount())) {
            return false;
        }
        return messageTransaction.getOrgnlTx().getTxId().getTxRef().equals(String.valueOf(
                transactionContext.getTransactionToCancel().getTransactionReference()));
    }

    private boolean contextCorrespondsToNexoCancelAdviceMessage(
            TransactionContext transactionContext,
            DocumentCancelAdvice documentCancelAdvice) {
        CardPaymentTransaction16 messageTransaction =
                documentCancelAdvice.getAccptrCxlAdvc().getCxlAdvc().getTx();

        if (!messageTransaction.getMrchntCtgyCd().equals(transactionContext.getCategoryCode())) {
            return false;
        }
        if (!messageTransaction.getTxDtls().getCcy().equals(transactionContext.getCurrency()
                .getAlphabeticCode())) {
            return false;
        }
        if (!messageTransaction.getTxDtls().getTtlAmt().equals(transactionContext.getAmount())) {
            return false;
        }
        if (!messageTransaction.getTxDtls().getTtlAmt().equals(transactionContext.getAmount())) {
            return false;
        }
        return messageTransaction.getOrgnlTx().getTxId().getTxRef().equals(String.valueOf(
                transactionContext.getTransactionToCancel().getTransactionReference()));
    }

    private TransactionContext createTestTransactionContext() {
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setCurrency(YelloCurrency.EUR);
        transactionContext.setAmount(new BigDecimal("10.30"));
        transactionContext.setAmountOther(new BigDecimal("5"));
        transactionContext.setTransactionType(TransactionType.PURCHASE);
        transactionContext.setCategoryCode("01");
        transactionContext.createOrUpdateContext(tagStore);
        transactionContext.setTransactionToCancel(new TransactionIdentification(new Date(), 666));
        return transactionContext;
    }
}