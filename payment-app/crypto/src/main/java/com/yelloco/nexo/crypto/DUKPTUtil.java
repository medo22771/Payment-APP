package com.yelloco.nexo.crypto;

/**
 * Created on 27/09/2016.
 */

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DUKPTUtil {
    // When AND'ed to a 10 byte KSN, zeroes all the 21 bits of the transaction
    // counter
    private static final byte[] KSN_MASK =       StringUtil.hexStringToBytes("FFFFFFFFFFFFFFE00000");
    // When AND'ed to a 10 byte KSN, zeroes all 59 most significative bits,
    // preserving only the 21 bits of the transaction counter
    private static final byte[] TRANSACTION_COUNTER_MASK = StringUtil.hexStringToBytes("000000000000001FFFFF");
    // Used for deriving IPEK and future keys
    private static final byte[] BDK_MASK =       StringUtil.hexStringToBytes("C0C0C0C000000000C0C0C0C000000000");
    private static final byte[] PIN_ENCRYPTION_VARIANT_CONSTANT =   StringUtil.hexStringToBytes("00000000000000FF");
    private static final byte[] SHIFTR =        StringUtil.hexStringToBytes("0000000000100000");
    private static final byte[] KEY_MASK =      StringUtil.hexStringToBytes("C0C0C0C000000000C0C0C0C000000000");
    private static final byte[] REG_8_MASK =    StringUtil.hexStringToBytes("0000000000000000FFFFFFFFFFE00000");
    private static final byte[] LS_16_MASK =    StringUtil.hexStringToBytes("0000000000000000FFFFFFFFFFFFFFFF");
    private static final byte[] MS_16_MASK =    StringUtil.hexStringToBytes("FFFFFFFFFFFFFFFF0000000000000000");
    private static final byte[] PEK_MASK =      StringUtil.hexStringToBytes("00000000000000FF00000000000000FF");

    private static final byte[] DEK_REQ_MASK =  StringUtil.hexStringToBytes("0000000000FF00000000000000FF0000"); // Data Encryption Request Mask
    private static final byte[] DEK_RESP_MASK = StringUtil.hexStringToBytes("000000FF00000000000000FF00000000"); // Data Encryption Response Mask
    private static final byte[] MAC_REQ_MASK =  StringUtil.hexStringToBytes("000000000000FF00000000000000FF00"); // HMAC Request Mask
    private static final byte[] MAC_RESP_MASK = StringUtil.hexStringToBytes("00000000FF00000000000000FF000000"); // HMAC Response Mask
    private static final byte[] KSN_ENC_KEY_MASK = StringUtil.hexStringToBytes("0000000000FFFFFFFFFF");

    /**
     * Generates an IPEK
     *
     * @param KSN
     *            10 bytes array (if your SNK has less than 10 bytes, pad it
     *            with 0xFF bytes to the left).
     * @param BDK
     *            24 bytes array. It's a triple-key (mandatory for TDES), and
     *            each key has 8 bytes. In DUKPT, double-keys are uses, so
     *            K1 = K3 (ex. K1 = 01 23 45 67 89 AB CD EF, K2 = FE DC BA 98 76 54 32 10,
     *            K3 = K1 =  01 23 45 67 89 AB CD EF)
     * @return a 16 byte IPEK for a specific device (the one associated with the
     *         serial key number in KSN), containing both the serial number and
     *         the ID of the associated BDK The BDK format is usually like
     *         follows: FF FF | BDK_ID[6] | TRSM_SN[5] | COUNTER[5] Note that
     *         the rightmost bit of TRSM_ID must not be used, for it belongs to
     *         the COUNTER. So the bytes of TRSM_SN must always form a multiple
     *         of 2 value
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     */
    public static byte[] generateIPEK(byte[] KSN, byte[] BDK)
            throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        // 1) Copy the entire key serial number, including the 21-bit encryption counter, right-justified into a 10-byte register. If the key serial
        // number is less than 10 bytes, pad to the left with hex "FF" bytes.

        // 2) Set the 21 least-significant bits of this 10-byte register to zero.
        byte[] masked_KSN = ByteArrayUtil.and(KSN, KSN_MASK);

        // 3) Take the 8 most-significant bytes of this 10-byte register, and encrypt/decrypt/encrypt these 8 bytes using the double-length
        // derivation key, per the TECB mode of Reference 2.
        byte[] eigth_byte_masked_KSN = new byte[8];
        for (int i = 0; i < 8; i++) {
            eigth_byte_masked_KSN[i] = masked_KSN[i];
        }

        byte[] IPEK_left = DESCryptoUtil.tdesEncrypt(eigth_byte_masked_KSN, BDK, null);

        // 4) Use the cipher text produced by Step 3 as the left half of the
        // Initial Key.
        byte[] ipek = new byte[16];
        for (int i = 0; i < 8; i++) {
            ipek[i] = IPEK_left[i];
        }

        // 5) Take the 8 most-significant bytes from the 10-byte register of step 2 and encrypt/decrypt/encrypt these 8 bytes using as the key the
        // double-length derivation key XORed with hexadecimal C0C0 C0C0 0000 0000 C0C0 C0C0 0000 0000, per the TECB mode of Reference 2.
        byte[] masked_derivation_key = ByteArrayUtil.xor(BDK, BDK_MASK);
        byte[] IPEK_right = DESCryptoUtil.tdesEncrypt(eigth_byte_masked_KSN, masked_derivation_key, null);

        // 6) Use the cipher text produced by Step 5 as the right half of the Initial Key.
        for (int i = 0; i < 8; i++) {
            ipek[i + 8] = IPEK_right[i];
        }

        return ipek;
    }

    /**
     * @param ksn ten byte array, which 2 leftmost bytes value is 0xFF (ex. FF FF 98 76 54 32 10 E0 12 34)
     * @return the ksn with it's last 21 bits set to 0. (ex. FF FF 98 76 54 32 10 E0 00 00)
     */
    public static byte[] ksnWithZeroedTransactionCounter(byte[] ksn) {
        return ByteArrayUtil.and(ksn, KSN_MASK);
    }

    /**
     * @param ksn ten byte array, which 2 leftmost bytes value is 0xFF (ex. FF FF 98 76 54 32 10 E0 12 34)
     * @return the value of the ksnl's last 21 bits, right justified and padded to left with zeroes, as a 8 byte array (ex. 00 00 00 00 00 00 00 00 12 34)
     */
    public static byte[] extractTransactionCounterFromKSN(byte[] ksn) {
        return ByteArrayUtil.subArray(ByteArrayUtil.and(ksn, TRANSACTION_COUNTER_MASK), 2, 9);
    }

    public static byte[] calculateKsn(byte[] derivationId, byte[] encryptedKey) {
        //return new BigInteger(derivationId).shiftLeft(40).or(new BigInteger(encryptedKey)).toByteArray();
        return ByteArrayUtil.join(derivationId, encryptedKey);
    }

    public static byte[] calculateKsnDerivationId(byte[] ksn) {
        return new BigInteger(ksn).shiftRight(40).toByteArray();
    }

    public static byte[] calculateKsnEncryptedKey(byte[] ksn) {
        //BigInteger encryptedKey = (new BigInteger(ksn)).and(new BigInteger(KSN_ENC_KEY_MASK));
        byte[] maskedKsn = ByteArrayUtil.and(ksn, KSN_ENC_KEY_MASK);
        byte[] encryptedKey = ByteArrayUtil.subArray(maskedKsn, (ksn.length / 2), maskedKsn.length - 1);
        return encryptedKey;
    }

    public static byte[] calculateKsn(String issuerIdHex, String merchantIdHex, String groupIdHex, String deviceIdHex, String transactionCounterHex) {
        return calculateKsn(new BigInteger(issuerIdHex, 16).toByteArray(), new BigInteger(merchantIdHex, 16).toByteArray(),
                new BigInteger(groupIdHex, 16).toByteArray(), new BigInteger(deviceIdHex, 16).toByteArray(),
                new BigInteger(transactionCounterHex, 16).toByteArray());
    }

    public static byte[] calculateKsn(byte[] issuerId, byte[] merchantId, byte[] groupId, byte[] deviceId, byte[] transactionCounter) {
        BigInteger derivationId = new BigInteger(issuerId);
        derivationId = derivationId.shiftLeft(8).or(new BigInteger(merchantId));
        derivationId = derivationId.shiftLeft(8).or(new BigInteger(groupId));

        BigInteger encKey = new BigInteger(deviceId);
        encKey = encKey.shiftLeft(21).or(new BigInteger(transactionCounter));

        return calculateKsn(derivationId.toByteArray(), encKey.toByteArray());
    }

    /**
     * Given a Base Derivation Key and a KSN, derives Session Key that matches the encryption counter (21 rightmost bits of the KSN)
     * @param ksn ten byte array, which 2 leftmost bytes value is 0xFF (ex. FF FF 98 76 54 32 10 E0 12 34)
     * @param bdk 16 bytes array (double-length key)
     * @return
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static byte[] deriveKey(byte[] ksn, byte[] bdk)
            throws GeneralSecurityException {
        System.out.println("============================== Key derivation ============================");
        System.out.println("KSN: " + StringUtil.toHexString(ksn));
        System.out.println("BDK: " + StringUtil.toHexString(bdk));

        // 4) Store the Key Serial Number, as received, in the externally
        // initiated command, into the Key Serial Number Register.
        // 5) Clear the encryption counter (21st right-most bits of KSNR
        byte[] r3 = DUKPTUtil.extractTransactionCounterFromKSN(ksn);
        System.out.println("Reg3: " + StringUtil.toHexString(r3));
        byte[] r8 = ByteArrayUtil.subArray(
                DUKPTUtil.ksnWithZeroedTransactionCounter(ksn), 2, 9);
        System.out.println("Reg8: " + StringUtil.toHexString(r8));
        byte[] shiftr = new byte[SHIFTR.length];
        System.arraycopy(SHIFTR, 0, shiftr, 0, SHIFTR.length);
        System.out.println("SHIFTR: " + StringUtil.toHexString(shiftr));

        byte[] crypto_register_1;
        byte[] curKey = bdk;

        curKey = DUKPTUtil.generateIPEK(ksn, curKey);
        System.out.println("Curr Key: " + StringUtil.toHexString(curKey));

        BigInteger intShiftr = new BigInteger(shiftr);
        BigInteger zero = new BigInteger("0");
        int i = 0;
        System.out.println("+++++++++++++++++ Looping +++++++++++++++++");
        while (intShiftr.compareTo(zero) == 1) {
            i++;
            System.out.println("--------------------- Round " + i);
            System.out.println("\tShiftR " + StringUtil.toHexString(shiftr));
            System.out.println("\tR3 " + StringUtil.toHexString(r3));
            byte[] temp = ByteArrayUtil.and(shiftr, r3);
            System.out.println("\tTemp " + StringUtil.toHexString(temp));
            //BigInteger intTemp = new BigInteger(temp);
            long intTemp = ByteArrayUtil.bytesToLong(temp);

            if (intTemp > 0) {
                r8 = ByteArrayUtil.or(r8, shiftr);
                System.out.println("\tReg8 " + StringUtil.toHexString(r8));
                // crypto_register_1 =
                // ByteArrayUtil.or(ByteArrayUtil.createSubArray(DUKPTUtil.ksnWithZeroedTransactionCounter(ksn),
                // 2, 9)/*crypto_register_1*/, shiftr);

                // 1) Crypto Register-1 XORed with the right half of the Key
                // Register goes to Crypto Register-2.
                byte[] crypto_register_2 = ByteArrayUtil.xor(
                        r8/* crypto_register_1 */,
                        ByteArrayUtil.subArray(curKey, 8, 15));
                System.out.println("\tCrypro register 2 (1st)" + StringUtil.toHexString(crypto_register_2));

                // 2) Crypto Register-2 DEA-encrypted using, as the key, the
                // left half of the Key Register goes to Crypto Register-2.
                crypto_register_2 = DESCryptoUtil.desEncrypt(crypto_register_2,
                        ByteArrayUtil.subArray(curKey, 0, 7), null);
                System.out.println("\tCrypro register 2 (2nd)" + StringUtil.toHexString(crypto_register_2));

                // 3) Crypto Register-2 XORed with the right half of the Key
                // Register goes to Crypto Register-2.
                crypto_register_2 = ByteArrayUtil.xor(crypto_register_2,
                        ByteArrayUtil.subArray(curKey, 8, 15));
                System.out.println("\tCrypro register 2 (3rd)" + StringUtil.toHexString(crypto_register_2));

                // 4) XOR the Key Register with hexadecimal C0C0 C0C0 0000 0000
                // C0C0 C0C0 0000 0000.
                curKey = ByteArrayUtil.xor(curKey, BDK_MASK);
                System.out.println("\tCurkey (1st) " + StringUtil.toHexString(curKey));

                // 5) Crypto Register-1 XORed with the right half of the Key
                // Register goes to Crypto Register-1.
                crypto_register_1 = ByteArrayUtil.xor(
                        r8/* crypto_register_1 */,
                        ByteArrayUtil.subArray(curKey, 8, 15));
                System.out.println("\tCrypro register 1 (1st)" + StringUtil.toHexString(crypto_register_1));

                // 6) Crypto Register-1 DEA-encrypted using, as the key, the
                // left half of the Key Register goes to Crypto Register-1.
                crypto_register_1 = DESCryptoUtil.desEncrypt(crypto_register_1,
                        ByteArrayUtil.subArray(curKey, 0, 7), null);
                System.out.println("\tCrypro register 1 (2nd)" + StringUtil.toHexString(crypto_register_1));

                // 7) Crypto Register-1 XORed with the right half of the Key
                // Register goes to Crypto Register-1.
                crypto_register_1 = ByteArrayUtil.xor(crypto_register_1,
                        ByteArrayUtil.subArray(curKey, 8, 15));
                System.out.println("\tCrypro register 1 (3rd)" + StringUtil.toHexString(crypto_register_1));

                curKey = ByteArrayUtil.join(crypto_register_1,
                        crypto_register_2);
                System.out.println("\tCurkey (2nd) " + StringUtil.toHexString(curKey));
            }

            shiftr = ByteArrayUtil.shiftRight(shiftr, 1);
            //System.out.println("\tSHIFTR: " + StringUtil.toHexString(shiftr));
            intShiftr = new BigInteger(shiftr);
        }

        System.out.println("Derived Key " + StringUtil.toHexString(curKey));
        return curKey;
    }

    /**
     *
     * @param derivedKey result of {@link #deriveKey(byte[], byte[])} to generate the key used to encrypt
     * card track info.
     * @return 16 byte array key that should be passed as the second parameter of {@link DESCryptoUtil#tdesDecrypt(byte[], byte[], byte[])}
     */
    public static byte[] calculatePinEncryptionKey(byte[] derivedKey) {
        byte[] derivedKeyL = ByteArrayUtil.subArray(derivedKey, 0, 7);
        byte[] derivedKeyR = ByteArrayUtil.subArray(derivedKey, 8, 15);

        // 1 - derivedKey_L XOR pin_variant_constant = pin_key_L
        byte[] pin_key_L = ByteArrayUtil.xor(derivedKeyL, PIN_ENCRYPTION_VARIANT_CONSTANT);

        // 2 - derivedKey_R XOR pin_variant_constant_R = pin_key_R
        byte[] pin_key_R = ByteArrayUtil.xor(derivedKeyR, PIN_ENCRYPTION_VARIANT_CONSTANT);

        return ByteArrayUtil.setParityBits(ByteArrayUtil.join(pin_key_L, pin_key_R), true);
    }

    public static byte[] decryptTrack1(byte[] track1, byte[] KSN, byte[] BDK) {
        try {
            byte[] derivedKey = deriveKey(KSN, BDK);
            byte[] pinKey = calculatePinEncryptionKey(derivedKey);
            byte[] decryptedInfo = DESCryptoUtil.tdesDecrypt(track1, pinKey, null);
            return decryptedInfo;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.flush();
            return null;
        }
    }

    public static byte[] encryptRegister(byte[] curKey, byte[] reg8) throws GeneralSecurityException {
        byte[] curkeyMs16 = ByteArrayUtil.and(curKey, MS_16_MASK);
        byte[] rightShiftedCurkeyMs16 = ByteArrayUtil.shiftRight(curkeyMs16, 64);
        byte[] curkeyLs16 = ByteArrayUtil.and(curKey, MS_16_MASK);
        byte[] xoredCurkeyLs16 = ByteArrayUtil.xor(curkeyLs16, reg8);
        byte[] encCurkeyMs16 = DESCryptoUtil.desEncrypt(rightShiftedCurkeyMs16, xoredCurkeyLs16, null);
        return ByteArrayUtil.xor(curkeyLs16, encCurkeyMs16);
    }

    public static byte[] createSessionKey(byte[] ksn, byte[] bdk) throws GeneralSecurityException {
        return ByteArrayUtil.xor( deriveKey(ksn, bdk), PEK_MASK);
    }

    public static byte[] calculateKey(byte[] key, byte[] ksn) throws GeneralSecurityException {
        byte[] temp = ByteArrayUtil.xor(key, KEY_MASK);
        byte[] left = encryptRegister(temp, ksn);
        byte[] right = encryptRegister(key, ksn);
        return ByteArrayUtil.join(left, right);
    }

    public static byte[] calculateDataEncryptionKey(byte[] ksn, byte[] bdk, boolean request) throws GeneralSecurityException {
        byte[] derivedKey = deriveKey(ksn, bdk);
        String derivedKeyHex = StringUtil.toHexString(derivedKey);
        byte[] reqMask = request ? DEK_REQ_MASK : DEK_RESP_MASK;
        byte[] currKey = ByteArrayUtil.xor(derivedKey, reqMask);

        byte[] leftBlock = ByteArrayUtil.shiftRight(currKey, 64);
        //leftBlock = new BigInteger(leftBlock).toByteArray();
        leftBlock = ByteArrayUtil.subArray(leftBlock, leftBlock.length - 8, leftBlock.length - 1);

        byte[] rightBlock = ByteArrayUtil.and(currKey, LS_16_MASK);
        //rightBlock = new BigInteger(rightBlock).toByteArray();
        rightBlock = ByteArrayUtil.subArray(rightBlock, rightBlock.length - 8, rightBlock.length - 1);

        byte[] keyLeft = DESCryptoUtil.tdesEncrypt(leftBlock, currKey, null);
        byte[] keyRight = DESCryptoUtil.tdesEncrypt(rightBlock, currKey, null);

        return ByteArrayUtil.setParityBits(ByteArrayUtil.join(keyLeft, keyRight), true);
    }

    public static byte[] calculateHmacKey(byte[] ksn, byte[] bdk, boolean request) throws GeneralSecurityException {
        byte[] derivedKey = deriveKey(ksn, bdk);
        byte[] reqMask = request ? MAC_REQ_MASK : MAC_RESP_MASK;
        byte[] mackKey = ByteArrayUtil.xor(derivedKey, reqMask);
        return ByteArrayUtil.setParityBits(mackKey, true);
    }

    public static byte[] padDataPKCS7(byte[] data, int blockSize) {
        int remainder = data.length % blockSize;
        int paddingLength = blockSize - remainder;
        byte[] paddedData = Arrays.copyOf(data, data.length + paddingLength);

        int paddingEndPosition = paddedData.length;
        int paddingStartPosition = paddedData.length - (paddingLength);
        for (int i = paddingEndPosition - 1; i > paddingStartPosition - 1; i--) {
            paddedData[i] = (byte) (paddingLength);
        }
        return paddedData;
    }

    public static byte[] trimPaddingPKCS7(byte[] paddedData) {
        int paddingLength = paddedData[paddedData.length - 1];
        byte[] dataWithoutPadding = Arrays.copyOf(paddedData, paddedData.length - paddingLength);
        return dataWithoutPadding;
    }

    public static byte[] addNull80Padding(byte[] data) {
        int additional = 8 - (data.length % 8);
        byte[] padded = Arrays.copyOf(data, data.length + additional);
        padded[data.length] = (byte) 0x80;
        return padded;
    }

    public static byte[] trimNull80Padding(byte[] data) {
        int i = data.length - 1;
        while (data[i] != (byte) 0x80)
            i--;
        return Arrays.copyOf(data, i);
    }

    public static byte[] calculateHashMacSha256(byte[] message, byte[] key) throws GeneralSecurityException {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
        hmacSha256.init(keySpec);
        return hmacSha256.doFinal(message);
    }
}
