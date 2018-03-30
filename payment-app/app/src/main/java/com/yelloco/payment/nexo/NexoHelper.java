package com.yelloco.payment.nexo;

import static com.yelloco.payment.utils.TlvTagEnum.AMOUNT_AUTHORIZED;
import static com.yelloco.payment.utils.TlvTagEnum.APPLICATION_CRYPTO;
import static com.yelloco.payment.utils.TlvTagEnum.APPLICATION_INTERCHANGE_PROFILE;
import static com.yelloco.payment.utils.TlvTagEnum.APPLICATION_TRANSACTION_COUNTER;
import static com.yelloco.payment.utils.TlvTagEnum.CRYPTOGRAM_INFO;
import static com.yelloco.payment.utils.TlvTagEnum.CVM_RESULTS;
import static com.yelloco.payment.utils.TlvTagEnum.DEDICATED_FILE;
import static com.yelloco.payment.utils.TlvTagEnum.ISSUER_APP_DATA;
import static com.yelloco.payment.utils.TlvTagEnum.TERMINAL_CAPABILITIES;
import static com.yelloco.payment.utils.TlvTagEnum.TERMINAL_COUNTRY_CODE;
import static com.yelloco.payment.utils.TlvTagEnum.TERMINAL_VERIFICATION_RESULTS;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_CURRENCY_CODE;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_DATE;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_TYPE;
import static com.yelloco.payment.utils.TlvTagEnum.UNPREDICTABLE_NB;
import static com.yelloco.payment.utils.Utils.bytesToHex;
import static com.yelloco.payment.utils.Utils.hexStringToByteArray;

import android.util.Log;

import com.yelloco.nexo.crypto.StringUtil;
import com.yelloco.nexo.message.acquirer.CardPaymentServiceType4Code;
import com.yelloco.nexo.message.acquirer.PaymentCard5;
import com.yelloco.nexo.message.acquirer.PaymentCard6;
import com.yelloco.payment.data.tagstore.TagReader;
import com.yelloco.payment.transaction.LoadedTransactionContext;
import com.yelloco.payment.utils.TlvUtils;

import org.apache.commons.codec.binary.Base64;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Provides the static helper methods to improve the building process of NEXO messages
 */
public class NexoHelper {

    public static final byte[] BDK = StringUtil.hexStringToBytes(
            "37233E890B0104E9BC943D0E45EAE5A7");
    public static final byte[] KSN = StringUtil.hexStringToBytes("398725A501E290200017");

    static final String SCHEME_DATA_DELIMITER_CHAR = ",";
    static final String CV_RULE_ONLINE_PIN = "42";
    static final String CV_RULE_SIGNATURE = "1E";
    static final String CV_RULE_OFFLINE_PIN = "01";

    public static final List<String> TLVS = Arrays.asList(APPLICATION_CRYPTO.getTag(),
            CRYPTOGRAM_INFO.getTag(),
            ISSUER_APP_DATA.getTag(),
            UNPREDICTABLE_NB.getTag(),
            APPLICATION_TRANSACTION_COUNTER.getTag(),
            TERMINAL_VERIFICATION_RESULTS.getTag(),
            TRANSACTION_DATE.getTag(),
            TRANSACTION_TYPE.getTag(),
            AMOUNT_AUTHORIZED.getTag(),
            TRANSACTION_CURRENCY_CODE.getTag(),
            APPLICATION_INTERCHANGE_PROFILE.getTag(),
            TERMINAL_COUNTRY_CODE.getTag(),
            CVM_RESULTS.getTag(),
            TERMINAL_CAPABILITIES.getTag(),
            DEDICATED_FILE.getTag());

    public static byte[] createIcc(TagReader tagReader) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        for (String tag : tagReader.getAllTags().keySet()) {
            if (TLVS.contains(tag)) {
                Log.i("NexoProcessor", "TAG-HEX:\n" + tag);
                buffer.put(hexStringToByteArray(tag));
                buffer.put(TlvUtils.getTLVLength(tagReader.getTag(tag).length));
                buffer.put(tagReader.getTag(tag));
            }
        }
        byte[] finalArray = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(finalArray);
        Log.i("NexoProcessor", "FinalHexString:\n" + bytesToHex(buffer.array()));
        return Base64.encodeBase64(finalArray);
    }

    public static CardPaymentServiceType4Code decideOnTransactionType(
            LoadedTransactionContext transactionContext) {
        switch (transactionContext.getTransactionType()) {
            case PURCHASE:
                return CardPaymentServiceType4Code.CRDP;
            case CASHBACK:
                return CardPaymentServiceType4Code.CSHB;
            case REFUND:
                return CardPaymentServiceType4Code.RFND;
        }
        throw new RuntimeException("Transaction type not implemented for online authorization");
    }

    public static PaymentCard6 convertPaymentCard(PaymentCard5 card5) {
        PaymentCard6 card6 = new PaymentCard6();
        card6.setPrtctdCardData(card5.getPrtctdCardData());
        card6.setAddtlCardData(card5.getAddtlCardData());
        card6.setCardBrnd(card5.getCardBrnd());
        card6.setCardCtryCd(card5.getCardCtryCd());
        card6.setCardPdctPrfl(card5.getCardPdctPrfl());
        return card6;
    }
}