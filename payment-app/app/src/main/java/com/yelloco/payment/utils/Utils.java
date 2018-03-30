package com.yelloco.payment.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class Utils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static boolean isValidEmail(String input) {
        boolean result = true;
        if (input.isEmpty())
            return false;

        try {
            InternetAddress emailAddr = new InternetAddress(input);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Show alert dialog with message.
     *
     * @param messageID String resource ID of Message for dialog.
     */
    public static void showAlert(int messageID, Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setTitle(android.R.string.dialog_alert_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(messageID).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String getHashFromString(String str){
        String result;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(str.getBytes());
            result = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }
}
