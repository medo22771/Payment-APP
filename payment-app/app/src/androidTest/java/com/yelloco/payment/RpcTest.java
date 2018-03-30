package com.yelloco.payment;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import com.yelloco.payment.rpc.RpcChannel;
import com.yelloco.payment.utils.Utils;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RpcTest {

    private static final String TAG = RpcTest.class.getSimpleName();

    @BeforeClass
    public static void setUpClass() {
        Log.v(TAG, "Set up class");
        try {
            RpcChannel.getInstance().init(InstrumentationRegistry.getTargetContext());
        } catch (IOException e) {
            Log.e(TAG, "Failed to init RPC channel. ", e);
        }
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        Log.v(TAG, "Tear down class");
        RpcChannel.getInstance().destroy();
    }

    @Test
    public void testGetHwInfo() throws Exception {
        String hwInfo = RpcChannel.getInstance().getHwInfo();
        Assert.assertTrue("Failed to acquire HW info", hwInfo != null && hwInfo.length() > 0);
        Log.i(TAG, "HW info: " + hwInfo);
    }

    @Test
    public void testGetDateTime() throws Exception {
        String dateTime = RpcChannel.getInstance().getDateTime();
        Assert.assertTrue("Failed to acquire dateTime info", dateTime != null && dateTime.length
                () > 0);
        Log.i(TAG, "Date-time: " + dateTime);
    }

    @Test
    public void testGetDukptMac() throws Exception {
        byte[] message = "Hello".getBytes();
        MessageDigest hmacSha256 = MessageDigest.getInstance("SHA-256");
        hmacSha256.reset();
        byte[] hash = hmacSha256.digest(message);
        byte[] mac = RpcChannel.getInstance().getDukptMac(true, RpcChannel.MacAlgo.RETAIL_CBC,
                hash);
        Log.i(TAG, "mac: " + Arrays.toString(mac));
        Log.i(TAG, "mac hex: " + Utils.bytesToHex(mac));
    }
}
