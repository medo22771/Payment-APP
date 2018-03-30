package com.yelloco.payment;

import static com.yelloco.nexo.crypto.StringUtil.toHexString;
import static com.yelloco.payment.utils.TlvTagEnum.ENCRYPT_ALGO;
import static com.yelloco.payment.utils.TlvTagEnum.INITIALIZATION_VECTOR;
import static com.yelloco.payment.utils.TlvTagEnum.PAN;
import static com.yelloco.payment.utils.TlvTagEnum.PIN_DATA;
import static com.yelloco.payment.utils.TlvTagEnum.PLAIN_CARD_DATA;
import static com.yelloco.payment.utils.TlvUtils.createTagStore;
import static com.yelloco.payment.utils.TlvUtils.listTags;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.alcineo.administrative.SendFileExecutor;
import com.alcineo.administrative.commands.GetConfList;
import com.alcineo.administrative.commands.GetDateTime;
import com.alcineo.connection.dispatcher.DispatcherListener;
import com.alcineo.connection.dispatcher.DispatcherService;
import com.alcineo.connection.executor.AbstractExecutor;
import com.alcineo.connection.executor.CommandExecutor;
import com.alcineo.transaction.TransactionListener;
import com.alcineo.transaction.TransactionManager;
import com.alcineo.transaction.TransactionType;
import com.alcineo.transaction.commands.DisplayLed;
import com.alcineo.transaction.events.DekRequestEvent;
import com.alcineo.transaction.events.DelayedAuthorizationEvent;
import com.alcineo.transaction.events.DisplayMenuRequestEvent;
import com.alcineo.transaction.events.DisplayTextRequestEvent;
import com.alcineo.transaction.events.GetAmountRequestEvent;
import com.alcineo.transaction.events.NotifyKernelIdEvent;
import com.alcineo.transaction.events.OnlineRequestEvent;
import com.alcineo.transaction.events.OnlineReversalEvent;
import com.alcineo.transaction.events.OutcomeReceivedEvent;
import com.alcineo.transaction.events.PinRequestEvent;
import com.alcineo.transaction.events.ReceiptDataEvent;
import com.alcineo.transaction.events.TransactionFinishedEvent;
import com.alcineo.transaction.events.TransactionStartedEvent;
import com.google.common.util.concurrent.Service;
import com.yelloco.nexo.crypto.ByteArrayUtil;
import com.yelloco.nexo.crypto.DESCryptoUtil;
import com.yelloco.nexo.crypto.DUKPTUtil;
import com.yelloco.nexo.crypto.StringUtil;
import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.message.acquirer.PaymentCard5;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.data.tagstore.TagStore;
import com.yelloco.payment.nexo.NexoProcessor;
import com.yelloco.payment.nexo.NexoTransactionHelper;
import com.yelloco.payment.rpc.RpcChannel;
import com.yelloco.payment.safecharge.SafechargeProcessor;
import com.yelloco.payment.safecharge.model.response.SafeChargeResponse;
import com.yelloco.payment.tcp.TcpClientUtil;
import com.yelloco.payment.transaction.SharedPreferencesTransactionReferencePersistence;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.utils.ConfigFileType;
import com.yelloco.payment.utils.Utils;
import com.yelloco.payment.utils.YelloCurrency;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.channels.AsynchronousCloseException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RunWith(AndroidJUnit4.class)
public class CommandTest implements TransactionListener {

    private static final String TAG = CommandTest.class.getSimpleName();

    public static final byte[] BDK = StringUtil.hexStringToBytes("37233E890B0104E9BC943D0E45EAE5A7");
    public static final byte[] KSN = StringUtil.hexStringToBytes("398725A501E290200017");

    private static final Service.Listener testServiceListener = new Service.Listener() {
        @Override
        public void failed(Service.State from, final Throwable failure) {
            // AsynchronousCloseException is standard closing from our side
            if (failure instanceof AsynchronousCloseException)
                return;
            Log.e(TAG, "Service failed: ", failure);
            assertTrue("Error in connection to payment framework", false);
        }
    };

    private static final Object LOCK = new Object();
    //jpg format is used to avoid the problems with de/compression of assets
    private static final java.lang.String LANGUAGES_FILE = "languages.jpg";

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final int MAX_REF_VALUE = 666;

    private TransactionManager transactionManager;
    private TransactionContext transactionContext;
    private DispatcherService dispatcherService;

    private boolean transactionStarted = false;
    private boolean transactionFinished = false;

    private boolean fileUploaded = false;
    private int fileUploadResult;

    private List<GetConfList.UploadableFile> uploadableFiles;
    private Date date;
    private StringBuilder receipt;
    private AuthChannel authChannel = AuthChannel.APPROVED_SIMUL;

    private enum AuthChannel {
        YELLO_GATEWAY,
        SAFECHARGE,
        YELLO_NEXO_TEST_SERVER,
        APPROVED_SIMUL,
        TEST_DECRYPTION_PIN,
        TEST_DECRYPTION_PLAIN_CARD
    }

    @BeforeClass
    public static void setUpClass() {
        Log.v(TAG, "Set up class");
        try {
            PaymentFramework.getInstance().init(testServiceListener, InstrumentationRegistry.getTargetContext());
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize payment framework. ", e);
        }
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        Log.v(TAG, "Tear down class");
        PaymentFramework.getInstance().destroy();
    }

    @Before
    public void setUp() throws Exception {
        Log.v(TAG, "Set up instance");
        dispatcherService = PaymentFramework.getInstance().getDispatcherService();
        if (dispatcherService == null)
            throw new Exception("Payment framework was not initialized");
        transactionManager = new TransactionManager(dispatcherService.getDispatcher(), dispatcherService);
    }

    @After
    public void tearDown() throws Exception {
        Log.v(TAG, "Tear down");
    }

    @Test
    public void testCancelTransaction() throws Exception {
        synchronized (LOCK) {
            Log.i(TAG, "Starting transaction");
            startTransaction();
            LOCK.wait(1000);
            assertTrue("Failed to start transaction.", transactionStarted);
        }
        synchronized (LOCK) {
            Log.i(TAG, "Cancelling transaction");
            transactionManager.cancel();
            LOCK.wait(1000);
            assertTrue("Failed to cancel transaction.", transactionFinished);
        }
    }

    @Test
    public void testFinishTransaction() throws Exception {
        performTransaction();
    }

    @Test
    public void testFinishTransactionGateway() throws Exception {
        authChannel = AuthChannel.YELLO_GATEWAY;
        performTransaction();
    }

    @Test
    public void testFinishTransactionNexoServer() throws Exception {
        authChannel = AuthChannel.YELLO_NEXO_TEST_SERVER;
        performTransaction();
    }

    @Test
    public void testWlinkWithRpcdParallel() throws Exception {
        RpcChannel.getInstance().init(InstrumentationRegistry.getTargetContext());
        String time = RpcChannel.getInstance().getDateTime();
        Assert.assertNotNull("Failed to get time from rpc - K81.", time);
        Log.i(TAG, "Time: " + time);
    }

    @Test
    public void testDecryptionPin() throws Exception {
        authChannel = AuthChannel.TEST_DECRYPTION_PIN;
        performTransaction();
    }

    @Test
    public void testDecryptionPlainCard() throws Exception {
        authChannel = AuthChannel.TEST_DECRYPTION_PLAIN_CARD;
        performTransaction();
    }

    private void performTransaction() throws Exception {
        synchronized (LOCK) {
            Log.i(TAG, "Starting transaction");
            startTransaction();
            LOCK.wait();
            assertTrue("Failed to finish transaction.", transactionFinished);
        }
    }

    @Test
    public void testGetConfList() throws Exception {
        CommandExecutor<GetConfList, List<GetConfList.UploadableFile>> executor = GetConfList
                .getExecutor(dispatcherService.getDispatcher(), dispatcherService);
        executor.setResultListener(new AbstractExecutor.ResultListener<List<GetConfList.UploadableFile>>() {
            @Override
            public void onResult(List<GetConfList.UploadableFile> uploadableFiles) {
                synchronized (LOCK) {
                    CommandTest.this.uploadableFiles = uploadableFiles;
                    LOCK.notifyAll();
                }
            }
        });
        synchronized (LOCK) {
            executor.execute();
            LOCK.wait(1000);
        }
        assertNotNull("Failed to acquire the config list.", uploadableFiles);
        for (GetConfList.UploadableFile file : uploadableFiles) {
            Log.i(TAG, "id: " + file.id + ", name: " + file.name);
        }
    }

    @Test
    public void testGetDate() throws Exception {
        CommandExecutor<GetDateTime, Date> executor = GetDateTime.getExecutor(dispatcherService.getDispatcher(), dispatcherService);
        executor.setResultListener(new AbstractExecutor.ResultListener<Date>() {
            @Override
            public void onResult(Date date) {
                Log.i(TAG, "Date received: " + date.toString());
                synchronized (LOCK) {
                    CommandTest.this.date = date;
                    LOCK.notifyAll();
                }
            }
        });
        executor.setErrorListener(new AbstractExecutor.ErrorListener() {
            @Override
            public void onError(Exception e) {
                Log.i(TAG, "Error received: ", e);
            }
        });
        synchronized (LOCK) {
            executor.execute();
            LOCK.wait(1000);
        }
        assertNotNull("Failed to acquire date.", date);
        Log.i(TAG, "date: " + date);
    }

    @Test
    public void testLoadLanguages() throws Exception {
        testGetConfList();
        if (uploadableFiles == null)
            throw new IllegalStateException("Cannot load the file without config list");
        byte id = ConfigFileType.LANGUAGES.getId(uploadableFiles);
        AssetFileDescriptor fd = InstrumentationRegistry.getContext().getResources().getAssets().openFd(LANGUAGES_FILE);
        SendFileExecutor executor = new SendFileExecutor(dispatcherService
                .getDispatcher(), dispatcherService, id, (int) fd.getLength(), fd.createInputStream());
        executor.setErrorListener(new AbstractExecutor.ErrorListener() {
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to upload file: ", e);
            }
        });
        executor.setProgressListener(new AbstractExecutor.ProgressListener() {
            @Override
            public void onProgressChanged(double progress) {
                Log.i(TAG, "Progress: " + Math.round(progress*100) + "%");
            }
        });
        executor.setResultListener(new AbstractExecutor.ResultListener<Integer>() {
            @Override
            public void onResult(Integer result) {
                Log.i(TAG, "File upload result: " + result);
                synchronized (LOCK) {
                    fileUploaded = true;
                    LOCK.notifyAll();
                }
            }
        });
        synchronized (LOCK) {
            executor.execute();
            LOCK.wait(10000);
        }
        assertTrue("File upload timeout.", fileUploaded);
        assertFalse("File upload result error.", fileUploadResult < 0);
    }

    @Test
    public void testLEDs() throws InterruptedException {
        dispatcherService.getDispatcher().addCommandListener(DisplayLed.class,
                new DispatcherListener<DisplayLed>() {

            @Override
            public void handle(DisplayLed displayLed) {
                Log.v(TAG, "Display LED event.");
                Log.v(TAG, "Leds: " + Arrays.toString(displayLed.getLeds()));
                Log.v(TAG, "Led status: " + displayLed.getLedStatus());
                Log.v(TAG, "Mode: " + displayLed.getMode());
                Log.v(TAG, "BlinkOffDurationMs: " + displayLed.getBlinkOffDurationMs());
                Log.v(TAG, "BlinkOnDurationMs: " + displayLed.getBlinkOnDurationMs());
            }
        });
        synchronized (LOCK) {
            startTransaction();
            LOCK.wait(10000);
        }
    }

    private void startTransaction() {
        receipt = new StringBuilder();
        transactionContext = new TransactionContext();
        transactionContext.setAmount(new BigDecimal(10));
        transactionContext.setAmountOther(new BigDecimal(10));
        transactionContext.setCategoryCode("01");
        transactionContext.setTransactionType(TransactionType.PURCHASE);
        transactionContext.setCurrency(YelloCurrency.EUR);

        transactionManager.setAmount(transactionContext.getAmount());
        transactionManager.setCategoryCode(transactionContext.getCategoryCode());
        transactionManager.setType(transactionContext.getTransactionType());
        transactionManager.setCurrencyCode(transactionContext.getCurrency().getNumericCode().toString());
        transactionManager.setAmountOther(transactionContext.getAmountOther());
        transactionManager.setMerchantData("TestMerchantData");

        transactionManager.addListener(this);
        try {
            transactionManager.start(20, TimeUnit.SECONDS);
        } catch (IOException e) {
            Log.i(TAG, "Failed to start payment transaction: ", e);
        }
    }

    @Override
    public void onTransactionStarted(TransactionStartedEvent transactionStartedEvent) {
        Log.i(TAG, "onTransactionStarted");
    }

    @Override
    public void onTransactionFinished(TransactionManager.TransactionStatus transactionStatus,
                                      TransactionFinishedEvent transactionFinishedEvent) {
        Log.i(TAG, "onTransactionFinished: " + transactionStatus.name());
        Log.i(TAG, receipt.toString());
        synchronized (LOCK) {
            transactionFinished = true;
            LOCK.notifyAll();
        }
    }

    @Override
    public void onOutcomeReceived(OutcomeReceivedEvent outcomeReceivedEvent) {
        Log.i(TAG, "onOutcomeReceived: " + outcomeReceivedEvent.getOutcomeType());
    }

    @Override
    public void onDisplayTextRequested(DisplayTextRequestEvent displayTextRequestEvent) {
        Log.i(TAG, "onDisplayTextRequested: " + displayTextRequestEvent.getText());
    }

    @Override
    public void onDisplayMenuRequested(DisplayMenuRequestEvent displayMenuRequestEvent) {
        Log.i(TAG, "onDisplayMenuRequested");
        try {
            displayMenuRequestEvent.sendSelection(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPinRequested(PinRequestEvent pinRequestEvent) {
        Log.i(TAG, "onPinRequested");
        try {
            pinRequestEvent.sendPin("1234");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOnlineRequested(final OnlineRequestEvent onlineRequestEvent) {
        Log.i(TAG, "onOnlineRequested");
        TagStore tagStore = createTagStore(onlineRequestEvent.getTlvItems());
        if (tagStore.hasTags()) {
            Log.i(TAG, "Received online TAGs: " + listTags(tagStore));
        } else {
            Log.i(TAG, "No online TAGs received.");
        }
        if (onlineRequestEvent.getResponse() != null) {
            Log.i(TAG, "Raw byte array from online event (HEX): \n" + Utils.bytesToHex
                    (onlineRequestEvent.getData()));
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                switch (authChannel) {
                    case APPROVED_SIMUL:
                        try {
                            onlineRequestEvent.sendApproved();
                        } catch (IOException e) {
                            Log.i(TAG, "Failed to send approved to EMV kernel: ", e);
                        }
                        break;
                    case YELLO_GATEWAY:
                        authorizationRequestGateway(onlineRequestEvent);
                        break;
                    case YELLO_NEXO_TEST_SERVER:
                        authorizationRequest(onlineRequestEvent);
                        break;
                    case TEST_DECRYPTION_PIN:
                        decryptPin(onlineRequestEvent);
                        try {
                            onlineRequestEvent.sendApproved();
                        } catch (IOException e) {
                            Log.i(TAG, "Failed to send approved to EMV kernel: ", e);
                        }
                        break;
                    case TEST_DECRYPTION_PLAIN_CARD:
                        Log.i(TAG, "Decrypting plaincard");
                        decryptPlainCard(onlineRequestEvent);
                        try {
                            onlineRequestEvent.sendApproved();
                        } catch (IOException e) {
                            Log.i(TAG, "Failed to send approved to EMV kernel: ", e);
                        }
                        break;
                }

            }
        });
    }

    private void authorizationRequest(final OnlineRequestEvent onlineRequestEvent) {
        try {
            transactionContext.createOrUpdateContext(createTagStore(onlineRequestEvent.getTlvItems()));
            Context targetContext = InstrumentationRegistry.getTargetContext();
            NexoTransactionHelper nexoTransactionHelper = new NexoTransactionHelper(
                    targetContext.getSharedPreferences(targetContext.getPackageName(),
                            Context.MODE_PRIVATE));
            NexoProcessor nexoProcessor = new NexoProcessor(targetContext,
                    new SharedPreferencesTransactionReferencePersistence(targetContext,
                            targetContext.getPackageName(),
                            MAX_REF_VALUE), nexoTransactionHelper);

            PaymentCard5 paymentCard = nexoProcessor.getNexoPartialBuilder().createPaymentCard5(
                    transactionContext.getTagStore());
            DocumentAuthReq document = nexoProcessor.createAuthorisationRequest(transactionContext);
            String serialized = XmlParser.serialize(DocumentAuthReq.class, document);
            Socket socket = new Socket(TcpClientUtil.SERVER_ADDRESS, TcpClientUtil.SERVER_PORT);
            TcpClientUtil.sendMessage(serialized, socket);
            String response = TcpClientUtil.readMessage(socket);
            Log.i(TAG, "Response from server \n" + response);
            onlineRequestEvent.sendApproved();
        } catch (Exception e) {
            Log.e(TAG, "Failed to process online authorization request: ", e);
        }
    }

    private void authorizationRequestGateway(OnlineRequestEvent onlineRequestEvent) {
        try {
            Context targetContext = InstrumentationRegistry.getTargetContext();
            NexoTransactionHelper nexoTransactionHelper = new NexoTransactionHelper(
                    targetContext.getSharedPreferences(targetContext.getPackageName(),
                            Context.MODE_PRIVATE));
            NexoProcessor nexoProcessor = new NexoProcessor(targetContext,
                    new SharedPreferencesTransactionReferencePersistence(targetContext,
                            targetContext.getPackageName(),
                            MAX_REF_VALUE), nexoTransactionHelper);

            transactionContext.createOrUpdateContext(
                    createTagStore(onlineRequestEvent.getTlvItems()));
            PaymentCard5 paymentCard = nexoProcessor.getNexoPartialBuilder().createPaymentCard5(
                    transactionContext.getTagStore());
            DocumentAuthReq document = nexoProcessor.createAuthorisationRequest(transactionContext);

            String serialized = XmlParser.serialize(DocumentAuthReq.class, document);
            Log.d("Nexo request", serialized);

            //RequestBody body = SafechargeProcessor.nexoAuthToSafecharge(document);

            OkHttpClient client = new OkHttpClient();

            String url = "http://41.38.100.254:9002/Gateway/services/1/1";
            //url += URLEncoder.encode(serialized, "utf-8");
            //url+=URLEncoder.encode(serialized,"utf-8");
            Log.d("url", url);

            FormBody.Builder body = new FormBody.Builder();

            body.add("p1", "1");
            body.add("p2", "1");
            body.add("p3", URLEncoder.encode(serialized, "utf-8"));

            Request req = new Request.Builder().url(url).post(body.build()).build();

            Response response = client.newCall(req).execute();

            if(response != null) {
                String responseStr = response.body().string();
                Log.d("Message from gateway",response.message());
                Log.d("Response from gateway", responseStr);
                SafeChargeResponse sc = SafechargeProcessor.convertResponse(responseStr);

                if(sc != null) {
                    if(sc.getStatus().equals("APPROVED")) {
                        onlineRequestEvent.sendApproved();
                        Log.d(TAG, "onOnlineRequest: sendApproved");
                    }
                    else if(sc.getStatus().equals("DECLINED")) {
                        onlineRequestEvent.sendDeclined();
                        Log.d(TAG, "onOnlineRequest: sendDeclined");
                    }
                    else {
                        onlineRequestEvent.sendDeclined();
                        Log.d(TAG, "onOnlineRequest: sendDeclined");
                    }
                }
            } else {
                onlineRequestEvent.sendUnableToGoOnline();
                Log.d(TAG, "onOnlineRequest: sendUnableToGoOnline");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decryptPin(OnlineRequestEvent onlineRequestEvent) {
        TagStore tagStore = createTagStore(onlineRequestEvent.getTlvItems());
        String hexPinData = toHexString(tagStore.getTag(PIN_DATA.getTag()));
        Log.i(TAG, "PinData 0x " + hexPinData);
        byte[] encryptedData = tagStore.getTag(PIN_DATA.getTag());
        if (encryptedData == null) {
            Log.e(TAG, "encryptedData is null.");
            return;
        }
        byte[] pan = tagStore.getTag(PAN.getTag());
        if (pan == null) {
            Log.e(TAG, "pan is null.");
            return;
        }
        Log.i(TAG, "PAN 0x " + new String(pan));
        byte[] decrypted = new byte[0];
        try {
            decrypted = DESCryptoUtil.tdesDecrypt(encryptedData, DUKPTUtil
                    .createSessionKey(KSN, BDK), null);
        } catch (GeneralSecurityException e) {

        }
        if (decrypted == null) {
            Log.e(TAG, "decrypted is null.");
            return;
        }
        byte[] panForXor = tagStore.getTag(PAN.getTag());
        Log.i(TAG, "Length of pan array: " + panForXor.length);
        panForXor[0] = 0;
        panForXor[1] = 0;

        byte[] pin = ByteArrayUtil.xor(decrypted, panForXor);
        String pinString = Utils.bytesToHex(pin);
        Log.i(TAG, "Decrypted PIN block: " + pinString);

        int length = Integer.parseInt(new String(new byte[] {(byte)pinString.charAt(1)}));
        Log.i(TAG, "PIN: " + pinString.substring(2,  length + 2));
    }

    private void decryptPlainCard(OnlineRequestEvent onlineRequestEvent) {
        try {
            TagStore tagStore = createTagStore(onlineRequestEvent.getTlvItems());
            byte[] encryptedData = tagStore.getTag(PLAIN_CARD_DATA.getTag());

            if (encryptedData == null) {
                Log.e(TAG, "encryptedData is null.");
                return;
            }
            String encryptedDataHex = toHexString(tagStore.getTag(PLAIN_CARD_DATA.getTag()));
            Log.i(TAG, "encryptedData 0x: " + encryptedDataHex);

            byte[] encryptAlgo = tagStore.getTag(ENCRYPT_ALGO.getTag());
            if (encryptAlgo == null) {
                Log.e(TAG, "encryptAlgo is null.");
                return;
            }
            String encryptAlgoString = new String(encryptAlgo);
            Log.i(TAG, "encryptAlgo String: " + encryptAlgoString);

            byte[] iv = tagStore.getTag(INITIALIZATION_VECTOR.getTag());
            if (iv == null) {
                Log.e(TAG, "iv is null.");
                return;
            }
            String ivHex = toHexString(tagStore.getTag(INITIALIZATION_VECTOR.getTag()));
            Log.i(TAG, "iv String: " + ivHex);

            byte[] decrypted = new byte[0];
            try {
                decrypted = DESCryptoUtil.tdesDecrypt(encryptedData, DUKPTUtil
                        .calculateDataEncryptionKey(KSN, BDK, true), iv);
            } catch (GeneralSecurityException e) {
                Log.e(TAG, "Decrypting error: ", e);
            }
            if (decrypted == null || decrypted.length == 0) {
                Log.e(TAG, "Failed to decrypt data.");
                return;
            }
            decrypted = DUKPTUtil.trimNull80Padding(decrypted);
            Log.i(TAG, "PlainCardData string:\n" + new String(decrypted));
        } catch (Exception e) {
            Log.e(TAG, "Failed to decrypt data: ", e);
        }
    }

    @Override
    public void onDelayedAuthorizationReceived(DelayedAuthorizationEvent
                                                           delayedAuthorizationEvent) {
        Log.i(TAG, "onDelayedAuthorizationReceived");
    }

    @Override
    public void onReceiptDataReceived(ReceiptDataEvent receiptDataEvent) {
        Log.i(TAG, "onReceiptDataReceived");
        receipt.append(receiptDataEvent.getReceiptLine());
    }

    @Override
    public void onNotifyKernelIdReceived(NotifyKernelIdEvent notifyKernelIdEvent) {
        Log.i(TAG, "onNotifyKernelIdReceived");
    }

    @Override
    public void onGetAmountRequested(GetAmountRequestEvent getAmountRequestEvent) {
        Log.i(TAG, "onGetAmountRequested");
        try {
            getAmountRequestEvent.sendAmounts(transactionContext.getAmount(), transactionContext
                    .getAmountOther());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReversalReceived(OnlineReversalEvent onlineReversalEvent) {
        Log.i(TAG, "onReversalReceived");
    }

    @Override
    public void onDekRequest(DekRequestEvent dekRequestEvent) {
        Log.i(TAG, "onDekRequest");
    }
}
