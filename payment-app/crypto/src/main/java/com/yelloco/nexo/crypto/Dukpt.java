package com.yelloco.nexo.crypto;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Dukpt {
    public enum SymmetricAlgo {
        TripleDES, DES
    }
    private static final BigInteger REG_3_MASK = new BigInteger(1, Hex.decode("1FFFFF"));
    private static final BigInteger SHIFT_REG_MASK = new BigInteger(1, Hex.decode("100000"));
    private static final BigInteger REG_8_MASK = new BigInteger(1, Hex.decode("FFFFFFFFFFE00000"));
    private static final BigInteger LS_16_MASK = new BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFF"));
    private static final BigInteger MS_16_MASK = new BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFF0000000000000000"));
    private static final String KEY_MASK_HEX = "C0C0C0C000000000C0C0C0C000000000";
    private static final BigInteger KEY_MASK = new BigInteger(1, Hex.decode(KEY_MASK_HEX));
    private static final BigInteger PEK_MASK = new BigInteger(1, Hex.decode("FF00000000000000FF"));
    private static final BigInteger KSN_MASK = new BigInteger(1, Hex.decode("FFFFFFFFFFFFFFE00000"));

    private static final BigInteger DEK_REQ_MASK = new BigInteger(1, Hex.decode("FF00000000000000FF0000")); // Data Encryption Request Mask
    private static final BigInteger DEK_RESP_MASK = new BigInteger(1, Hex.decode("FF00000000000000FF00000000")); // Data Encryption Response Mask
    private static final BigInteger MAC_REQ_MASK = new BigInteger(1, Hex.decode("FF00000000000000FF00")); // HMAC Request Mask
    private static final BigInteger MAC_RESP_MASK = new BigInteger(1, Hex.decode("FF00000000000000FF000000")); // HMAC Response Mask

    private static final BigInteger KSN_ENC_KEY_MASK = new BigInteger(1, Hex.decode("0000000000FFFFFFFFFF"));

    private static final String BOUNCY_CASTLE_PROVIDER = "SC";

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public static BigInteger createBdk(BigInteger key1, BigInteger key2)
    {
        return key1.xor(key2);
    }

    public static BigInteger transform(SymmetricAlgo algo, boolean encrypt, BigInteger key, BigInteger message, byte[] iv)
            throws GeneralSecurityException {

        return transform(algo, encrypt, key.toByteArray(), message.toByteArray(), iv);
    }

    public static BigInteger transform(SymmetricAlgo algo, boolean encrypt, byte[] key, byte[] message, byte[] iv)
            throws GeneralSecurityException {

        String keyType;
        String keyAlgo;
        int maxKeyLength;
        if(algo == SymmetricAlgo.TripleDES) {
            keyType = "DESede";
            keyAlgo = "DESede/CBC/ZeroBytePadding";
            maxKeyLength = DESedeKeySpec.DES_EDE_KEY_LEN;
        } else {
            keyType = "DES";
            keyAlgo = "DES/CBC/ZeroBytePadding";
            maxKeyLength = DESKeySpec.DES_KEY_LEN;
        }

        //byte[] keyBytes = padKeyToLength(key.toByteArray(), maxKeyLength);
        //byte[] keyBytes = key.toByteArray();
        int multValue = getNearestWholeMultiple(key.length, 8);
        int paddLength = Math.max(0, multValue - key.length);
        byte[] paddedKey = new byte[paddLength + key.length];
        System.arraycopy(key, 0, paddedKey, paddLength, key.length);

        System.out.println("Crypto key: " + Hex.toHexString(paddedKey));

        SecretKeySpec keySpec = new SecretKeySpec(paddedKey, keyType);

        Cipher cipher = Cipher.getInstance(keyAlgo, BOUNCY_CASTLE_PROVIDER);

        if(iv == null)
            iv = new byte[cipher.getBlockSize()];

        IvParameterSpec ivParams = new IvParameterSpec(iv);
        SecretKey secKey = SecretKeyFactory.getInstance(keyType).generateSecret(keySpec);
        cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secKey, ivParams);

        //byte[] messageBytes = message.toByteArray();
        multValue = getNearestWholeMultiple(message.length, 8);
        paddLength = Math.max(0, multValue - message.length);
        byte[] paddedMessage = new byte[paddLength + message.length];
        System.arraycopy(message, 0, paddedMessage, paddLength, message.length);

        return new BigInteger(cipher.doFinal(paddedMessage));
    }

    /**
     * 3DES Encrypt the data
     *
     * @param key
     *            Key to encrypt with
     * @param data
     *            Data to be encrypted
     * @return Hex string of encrypted data
     */
    public static BigInteger tripleDesEncrypt(BigInteger key, BigInteger data) {
        //checkNotNull(key);
        //checkNotNull(data);
        //int len = data.length();
        //checkArgument(len == 16 || len == 32 || len == 48, "Invalid data for 3DES Encrypt");

        try {

            byte[] masterKey = key.toByteArray();
            byte[] desKey = new byte[24];
            System.arraycopy(masterKey, 0, desKey, 0, 16);
            System.arraycopy(masterKey, 0, desKey, 16, 8);

            DESedeKeySpec keySpec = new DESedeKeySpec(desKey);

            SecretKey secretKey = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);

            Cipher ecipher = Cipher.getInstance("DESede/CBC/ZeroBytePadding", BOUNCY_CASTLE_PROVIDER);
            ecipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedData = ecipher.doFinal(data.toByteArray());
            return new BigInteger(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] padKeyToLength(byte[] key, int len) {
        byte[] newKey = new byte[len];
        System.arraycopy(key, 0, newKey, 0, Math.min(key.length, len));
        return newKey;
    }

    public static BigInteger createIpek(BigInteger ksn, BigInteger bdk) throws GeneralSecurityException {
        //System.out.println("BDK Length: " + bdk.bitLength() / 8);
        //System.out.println("Key mask length: " + KEY_MASK.bitLength() / 8);
        BigInteger ksnAndKsnMask = ksn.and(KSN_MASK);
        //BigInteger bdkXorKeyMask = bdk.xor(KEY_MASK);
        BigInteger bdkXorKeyMask = bdk.xor(KEY_MASK);
        BigInteger ksnAndKeyMaskRightShifted16 = ksnAndKsnMask.shiftRight(16);
        System.out.println("ksnAndKeyMaskRightShifted16: " + ksnAndKeyMaskRightShifted16.toString(16));
        System.out.println("bdkXorKeyMask: " + bdkXorKeyMask.toString(16));

        BigInteger left = transform(SymmetricAlgo.TripleDES, true, bdk, ksnAndKeyMaskRightShifted16, null).shiftLeft(64);
        System.out.println("Left part: " + left.toString(16));
        BigInteger right = transform(SymmetricAlgo.TripleDES, true, bdkXorKeyMask, ksnAndKeyMaskRightShifted16, null);
        System.out.println("Right part: " + left.toString(16));
        return left.or(right);
        //return tripleDesEncrypt(bdk, ksn.and(KSN_MASK).shiftRight(16)).shiftLeft(64)
        //        .or(tripleDesEncrypt(bdk.xor(KEY_MASK), ksn.and(KSN_MASK).shiftRight(16)));
    }

    public static byte[] xor(byte[] a, byte[] b) {
        byte[] xor = new byte[Math.min(a.length, b.length)];
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < xor.length; i++) {
            xor[i] = (byte) (((int) a[i]) ^ ((int) b[i]));
        }

        byte[] result = new byte[len];
        System.arraycopy(xor, 0, result, (len - xor.length), xor.length);
        return result;
    }

    public static byte[] and(byte[] a, byte[] b) {
        byte[] xor = new byte[Math.min(a.length, b.length)];
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < xor.length; i++) {
            xor[i] = (byte) (((int) a[i]) & ((int) b[i]));
        }

        byte[] result = new byte[len];
        System.arraycopy(xor, 0, result, (len - xor.length), xor.length);
        return result;
    }

    public static byte[] or(byte[] a, byte[] b) {
        byte[] xor = new byte[Math.min(a.length, b.length)];
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < xor.length; i++) {
            xor[i] = (byte) (((int) a[i]) | ((int) b[i]));
        }

        byte[] result = new byte[len];
        if(a.length > b.length) {
            System.arraycopy(a, 0, result, 0, (len - xor.length));
        } else {
            System.arraycopy(b, 0, result, 0, (len - xor.length));
        }
        System.arraycopy(xor, 0, result, (len - xor.length), xor.length);
        return result;
    }

    public static byte[] shiftRight(byte[] bytes, int length) {
        BigInteger bigInt = new BigInteger(1, bytes);
        bigInt.shiftRight(length);
        return bigInt.toByteArray();
    }

    public static BigInteger deriveKey(BigInteger ipek, BigInteger ksn) throws GeneralSecurityException {
        BigInteger ksnReg = ksn.and(LS_16_MASK).and(REG_8_MASK);
        BigInteger curKey = ipek;
        for(BigInteger shiftReg = SHIFT_REG_MASK; shiftReg.intValue() > 0; shiftReg = shiftReg.shiftRight(1)) {
            if(shiftReg.and(ksn).and(REG_3_MASK).intValue() > 0) {
                BigInteger tmp = ksnReg.or(shiftReg);
                curKey = generateKey(curKey, tmp);
                ksnReg = tmp;
            }
        }
        return curKey;
    }

    public static BigInteger generateKey(BigInteger key, BigInteger ksn) throws GeneralSecurityException {
        return encryptRegister(key.xor(KEY_MASK), ksn).shiftLeft(64)
                .or(encryptRegister(key, ksn));
    }

    public static BigInteger encryptRegister(BigInteger curKey, BigInteger reg8) throws GeneralSecurityException {
        return curKey.and(LS_16_MASK).xor(transform(SymmetricAlgo.DES, true, curKey.and(MS_16_MASK).shiftRight(64),
                curKey.and(LS_16_MASK).xor(reg8), null));
    }

    private static int getNearestWholeMultiple(double input, int x) {
        double output = Math.round(input / x);
        if(output == 0 && input > 0)
            output += 1;
        output *= x;
        return (int)output;
    }

    public static byte[] encrypt(String bdk, String ksn, byte[] track) throws GeneralSecurityException {
        BigInteger ipek = createIpek(new BigInteger(bdk, 16), new BigInteger(ksn, 16));
        BigInteger sessionKey = createSessionKey(ipek, new BigInteger(ksn, 16));
        return transform(SymmetricAlgo.TripleDES, true, sessionKey, new BigInteger(track), null).toByteArray();
    }

    public static BigInteger createSessionKey(BigInteger ipek, BigInteger ksn) throws GeneralSecurityException {
        return deriveKey(ipek, ksn).xor(PEK_MASK);
    }

    public static byte[] decrypt(String bdk, String ksn, byte[] track) throws GeneralSecurityException {
        BigInteger ipek = createIpek(new BigInteger(bdk, 16), new BigInteger(ksn, 16));
        BigInteger sessionKey = createSessionKey(ipek, new BigInteger(ksn, 16));
        return transform(SymmetricAlgo.TripleDES, false, sessionKey, new BigInteger(track), null).toByteArray();
    }

    public static BigInteger generateKsn(BigInteger issuerId, BigInteger merchantId, BigInteger groupId, BigInteger deviceId, BigInteger transactionCounter) {
        BigInteger derivationId = issuerId;
        derivationId = derivationId.shiftLeft(8).or(merchantId);
        derivationId = derivationId.shiftLeft(8).or(groupId);

        BigInteger encKey = deviceId;
        encKey = encKey.shiftLeft(21).or(transactionCounter);

        return generateKsn(derivationId, encKey);
    }

    private static byte setParityBit(byte b, boolean odd) {
        b = (byte)(b & ~1);

        int onBits = (b & 0x01) + ((b & 0x02) >> 1) + ((b & 0x04) >> 2) + ((b & 0x08) >> 3) + ((b & 0x10) >> 4) + ((b & 0x20) >> 5) +
                ((b & 0x40) >> 6) + ((b & 0x80) >> 7);

        return (byte)(b | ((onBits + (odd ? 1 : 0)) % 2));
    }

    private static byte[] setParityBites(byte[] bytes, boolean odd) {
        byte[] tmp = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            tmp[i] = setParityBit(bytes[i], odd);
        }
        return tmp;
    }

    public static BigInteger generateKsn(BigInteger derivationId, BigInteger encryptedKey) {
        return derivationId.shiftLeft(40).or(encryptedKey);
    }

    public static BigInteger generateKsn(String issuerIdHex, String merchantIdHex, String groupIdHex, String deviceIdHex, String transactionCounterHex) {
        return generateKsn(new BigInteger(issuerIdHex, 16), new BigInteger(merchantIdHex, 16),
                new BigInteger(groupIdHex, 16), new BigInteger(deviceIdHex, 16),
                new BigInteger(transactionCounterHex, 16));
    }

    public static BigInteger generateDataEncryptionKey(BigInteger bdk, BigInteger ksn, boolean requet) throws GeneralSecurityException {
        BigInteger tik = createIpek(ksn, bdk);
        BigInteger derivedKey = deriveKey(tik, ksn);

        BigInteger reqMask = requet ? DEK_REQ_MASK : DEK_RESP_MASK;
        BigInteger currKey = derivedKey.xor(reqMask);

        BigInteger leftBlock = currKey.shiftRight(64);
        BigInteger rightBlock = currKey.and(LS_16_MASK);

        BigInteger key = transform(SymmetricAlgo.TripleDES, true, currKey, leftBlock, null).shiftLeft(64)
                .or(transform(SymmetricAlgo.TripleDES, true, currKey, rightBlock, null));

        return new BigInteger(setParityBites(key.toByteArray(), true));
    }

    public static BigInteger generateHmacKey(BigInteger bdk, BigInteger ksn, boolean request) throws GeneralSecurityException {
        BigInteger tik = createIpek(ksn, bdk);
        BigInteger derivedKey = deriveKey(tik, ksn);

        BigInteger reqMask = request ? MAC_REQ_MASK : MAC_RESP_MASK;
        BigInteger macKey = derivedKey.xor(reqMask);

        BigInteger macKeyWithParity = new BigInteger(setParityBites(macKey.toByteArray(), true));
        return macKeyWithParity;
    }

    public static BigInteger getKsnDerivationId(BigInteger ksn) {
        return ksn.shiftRight(40);
    }

    public static BigInteger getKsnEncryptionKey(BigInteger ksn) {
        return ksn.and(KSN_ENC_KEY_MASK);
    }

    public static BigInteger generatePinEncryptionKey(BigInteger bdk, BigInteger ksn) throws GeneralSecurityException {
        BigInteger tik = createIpek(ksn, bdk);
        BigInteger derivedKey = deriveKey(tik, ksn);
        BigInteger pek = derivedKey.xor(PEK_MASK);
        byte[] pekBytes = pek.toByteArray();
        BigInteger pekWithParity = new BigInteger(setParityBites(pekBytes, true));
        return pekWithParity;
    }

    public static byte[] padData(byte[] data) {
        byte[] tmp = new byte[data.length + 1];
        System.arraycopy(data, 0, tmp, 0, data.length);
        tmp[tmp.length - 1] = (byte)0x80;
        if (tmp.length % 8 == 0)
            return data;

        int padLength = 8 - (tmp.length % 8);
        byte[] paddedData = new byte[tmp.length + padLength];

        System.arraycopy(tmp, 0, paddedData, 0, tmp.length);

        return paddedData;
    }

    public static byte[] computeRetailCbcMacSha256(byte[] message, byte[] key) throws GeneralSecurityException
    {
        MessageDigest hmacSha256 = MessageDigest.getInstance("SHA-256");
        hmacSha256.reset();
        byte[] hash = hmacSha256.digest(message);

        byte[] d = padData(hash);
        int length = d.length / 8;

        BigInteger k = new BigInteger(key);
        BigInteger kl = k.shiftRight((key.length / 2) * 8);

        byte[] c = new byte[8];

        for (int i = 0; i < length - 1; i++)
        {
            byte[] tmp = new byte[8];
            System.arraycopy(d, i * 8, tmp, 0, 8);

            BigInteger ctmp = new BigInteger(tmp).xor(new BigInteger(c));
            c = transform(SymmetricAlgo.DES, true, kl, ctmp, null).toByteArray();
        }

        byte[] dn = new byte[8];
        System.arraycopy(d, (length - 1) * 8, dn, 0, dn.length);

        BigInteger t = new BigInteger(dn).xor(new BigInteger(c));

        byte[] mac = transform(SymmetricAlgo.TripleDES, true, new BigInteger(key), t, null).toByteArray();

        return mac;
    }

    public static BigInteger GenerateRandomBytes(int length)
    {
        if (length <= 0)
        {
            return BigInteger.ZERO;
        }

        Random rnd = new Random();
        byte[] b = new byte[length];
        rnd.nextBytes(b);

        return new BigInteger(b);
    }
}
