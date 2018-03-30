package com.yelloco.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.yelloco.nexo.crypto.DUKPTUtil;
import java.util.Arrays;
import org.junit.Test;

public class CryptoTest {

    private enum PaddingType {
        PKCS7_PADDING,
        NULL_80_PADDING
    }

    @Test
    public void testPKCS7() throws Exception {
        padAndTrimData(PaddingType.PKCS7_PADDING);
    }

    @Test
    public void testPaddingNull80() throws Exception {
        padAndTrimData(PaddingType.NULL_80_PADDING);
    }

    private void padAndTrimData (PaddingType paddingType){
        for (int i = 1; i < 50; i++) {
            byte[] data = new byte[i];
            Arrays.fill(data, (byte)i);
            System.out.println("Non-Padded data length: " + data.length);
            System.out.println("Non-Padded data: " + Arrays.toString(data));
            byte[] padded = null;
            switch (paddingType) {
                case PKCS7_PADDING:
                     padded = DUKPTUtil.padDataPKCS7(data, 8);
                     break;
                case NULL_80_PADDING:
                     padded = DUKPTUtil.addNull80Padding(data);
                     break;
            }
            assertTrue("Padded data length is not multiple 8 bytes.", padded.length % 8 == 0);
            System.out.println("Padded data length: " + padded.length);
            System.out.println("Padded data: " + Arrays.toString(padded));
            byte[] trimmed = null;
            switch (paddingType) {
                case PKCS7_PADDING:
                    trimmed = DUKPTUtil.trimPaddingPKCS7(padded);
                    break;
                case NULL_80_PADDING:
                    trimmed = DUKPTUtil.trimNull80Padding(padded);
                    break;
            }
            assertEquals("Trimmed data size is not equal to original data", trimmed.length, data.length);
            assertTrue("Trimmed array is different from original.", Arrays.equals(trimmed, data));
            System.out.println("Trimmed data length: " + trimmed.length);
            System.out.println("Trimmed data: " + Arrays.toString(trimmed));
            System.out.println("-----------------------------------------------------");
        }
    }


    @Test
    public void testPanConversion() throws Exception {
        String pan = "5413330089020094";
        String[] panSplit = pan.split("");
        System.out.println(Arrays.toString(panSplit));
        byte[] panFormatted = new byte[8];
        for (int i = 4; i < panSplit.length; i++) {
            System.out.println(panSplit[i]);
        }

    }
}