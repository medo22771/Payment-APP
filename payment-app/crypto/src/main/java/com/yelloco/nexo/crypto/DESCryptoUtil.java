package com.yelloco.nexo.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created  on 27/09/2016.
 */
public class DESCryptoUtil {
    private static String TRIPLE_DES_TRANSFORMATION = "DESede/CBC/NoPadding";
    private static String ALGORITHM = "DESede";

    public static byte[] tdesEncrypt(byte[] input, byte[] key, byte[] iv) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        if(key.length != 16 && key.length != 24) {
            throw new InvalidKeyException("@ DESCryptoUtil.tdesEncrypt(). Parameter <key> must be 16 or 24 bytes long (bouble/triple key), but was " + key.length + ".");
        }

        if(key.length == 16) {
            key = extendDoubleKeyToTripleKey(key);
        }

        SecretKey keySpec = new SecretKeySpec(key, ALGORITHM);
        Cipher encryptor = Cipher.getInstance(TRIPLE_DES_TRANSFORMATION);

        if(iv == null) {
            iv = new byte[encryptor.getBlockSize()];
        }
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        encryptor.init(Cipher.ENCRYPT_MODE, keySpec, ivParams);

        return encryptor.doFinal(input);
    }

    public static byte[] tdesDecrypt(byte[] input, byte[] key, byte[] iv) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        if(key.length != 16 && key.length != 24) {
            throw new InvalidKeyException("@ DESCryptoUtil.tdesDecrypt(). Parameter <key> must be 16 or 24 bytes long (bouble/triple key), but was " + key.length + ".");
        }

        if(key.length == 16) {
            key = extendDoubleKeyToTripleKey(key);
        }

        SecretKey keySpec = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRIPLE_DES_TRANSFORMATION);

        if(iv == null) {
            iv = new byte[cipher.getBlockSize()];
        }
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams);

        return cipher.doFinal(input);
    }

    public static byte[] desEncrypt(byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        return desTransform(true, input, key, iv);
    }

    public static byte[] desDecrypt(byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        return desTransform(false, input, key, iv);
    }

    public static byte[] desTransform(boolean enc, byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        if(key.length != 8) {
            throw new InvalidKeyException("@ DESCryptoUtil.desEncrypt(). Parameter <key> must be 8 bytes long, but was " + key.length + ".");
        }

        if(input.length != 8) {
            throw new IllegalBlockSizeException("@ DESCryptoUtil.desEncrypt(). Parameter <input> must be 8 bytes long, but was " + input.length + ".");
        }

        Cipher desCipher = Cipher.getInstance("DES/CBC/NoPadding");
        SecretKey keySpec = new SecretKeySpec(key, "DES");

        if(iv == null) {
            iv = new byte[desCipher.getBlockSize()];
        }
        IvParameterSpec ivParams = new IvParameterSpec(iv);

        desCipher.init(enc ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, ivParams);

        return desCipher.doFinal(input);
    }

    public static byte[] aesEncrypt(byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        return aesTransform(true, input, key, iv);
    }

    public static byte[] aesDecrypt(byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        return aesTransform(false, input, key, iv);
    }

    public static byte[] aesTransform(boolean enc, byte[] input, byte[] key, byte[] iv) throws GeneralSecurityException {
        Cipher desCipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKey keySpec = new SecretKeySpec(key, "AES");

        if(iv == null) {
            iv = new byte[desCipher.getBlockSize()];
        }

        IvParameterSpec ivParams = new IvParameterSpec(iv);

        desCipher.init(enc ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, keySpec, ivParams);

        return desCipher.doFinal(input);
    }

    private static byte[] extendDoubleKeyToTripleKey(byte[] doubleKey) {
        byte[] tripleKey = new byte[24];

        for(int i = 0; i < 16; i++) {
            tripleKey[i] = doubleKey[i];
        }

        for(int i = 0, j = 16; j < 24; i++, j++) {
            tripleKey[j] = doubleKey[i];
        }

        return tripleKey;
    }
}

