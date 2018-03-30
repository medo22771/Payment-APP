package com.yelloco.nexo.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created on 29/09/2016.
 */
public class UKPTUtil {
    private static final byte[] MacLcvMask =            StringUtil.hexStringToBytes("00004D000341000000004D0003410000");
    private static final byte[] PinLcvMask =            StringUtil.hexStringToBytes("00215F000341000000215F0003410000");
    private static final byte[] MacRcvMask =            StringUtil.hexStringToBytes("00004D000321000000004D0003210000");
    private static final byte[] PinRcvMask =            StringUtil.hexStringToBytes("00215F000321000000215F0003210000");
    private static final byte[] DataLcvMask =           StringUtil.hexStringToBytes("00007100034100000000710003410000");
    private static final byte[] DataRcvMask =           StringUtil.hexStringToBytes("00007100032100000000710003210000");
    private static final byte[] KeyEncryptLcvMask =     StringUtil.hexStringToBytes("00427D000341000000427D0003410000");
    private static final byte[] KeyEncryptRcvMask =     StringUtil.hexStringToBytes("00427D000321000000427D0003210000");
    private static final byte[] LeftEncryptedKeyMask =  StringUtil.hexStringToBytes("0000000000000000FFFFFFFFFFFFFFFF");

    public static byte[] calculateTDesSessionKey(byte[] masterKey, byte[] encryptedKey) throws GeneralSecurityException {
        byte[] encryptedRightShifted = ByteArrayUtil.shiftRight(encryptedKey, 64);
        byte[] leftPart = ByteArrayUtil.subArray(DESCryptoUtil.tdesDecrypt(encryptedRightShifted, masterKey, null), 8, 15);

        byte[] maskedEncryptedKey = ByteArrayUtil.and(encryptedKey, LeftEncryptedKeyMask);
        byte[] rightPart = ByteArrayUtil.subArray(DESCryptoUtil.tdesDecrypt(maskedEncryptedKey, masterKey, null), 8, 15);

        return ByteArrayUtil.setParityBits(ByteArrayUtil.join(leftPart, rightPart), true);
    }

    public static byte[] calculateAesSessionKey(byte[] masterKey, byte[] encryptedKey) throws GeneralSecurityException {
        return DESCryptoUtil.aesDecrypt(encryptedKey, masterKey, null);
    }

    private static byte[] calculateIbmCcaSessionKey(byte[] masterKey, byte[] encryptedKey, byte[] leftMask, byte[] rightMask) throws BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException {
        byte[] leftKek = ByteArrayUtil.xor(masterKey, leftMask);
        byte[] rightKek = ByteArrayUtil.xor(masterKey, rightMask);

        byte[] encryptedRightShifted = ByteArrayUtil.shiftRight(encryptedKey, 64);
        byte[] temp = DESCryptoUtil.tdesDecrypt(encryptedRightShifted, leftKek, null);
        byte[] leftKey = ByteArrayUtil.subArray(temp, 8, 15);

        byte[] maskedEncryptedKey = ByteArrayUtil.and(encryptedKey, LeftEncryptedKeyMask);
        temp = DESCryptoUtil.tdesDecrypt(maskedEncryptedKey, rightKek, null);
        byte[] rightKey = ByteArrayUtil.subArray(temp, 8, 15);

        return ByteArrayUtil.setParityBits(ByteArrayUtil.join(leftKey, rightKey), true);
    }

    public static byte[] calculateIbmCcaMacSessionKey(byte[] masterKey, byte[] encryptedKey) throws BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {
        return calculateIbmCcaSessionKey(masterKey, encryptedKey, MacLcvMask, MacRcvMask);
    }

    public static byte[] calculateIbmCcaPinSessionKey(byte[] masterKey, byte[] encryptedKey) throws BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {
        return calculateIbmCcaSessionKey(masterKey, encryptedKey, PinLcvMask, PinRcvMask);
    }

    public static byte[] calculateIbmCcaDataSessionKey(byte[] masterKey, byte[] encryptedKey) throws BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {
        return calculateIbmCcaSessionKey(masterKey, encryptedKey, DataLcvMask, DataRcvMask);
    }

    public static byte[] calculateIbmCcaKeyEncryptionSessionKey(byte[] masterKey, byte[] encryptedKey) throws BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {
        return calculateIbmCcaSessionKey(masterKey, encryptedKey, KeyEncryptLcvMask, KeyEncryptRcvMask);
    }
}
