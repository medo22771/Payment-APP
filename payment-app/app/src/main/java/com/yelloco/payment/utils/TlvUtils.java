package com.yelloco.payment.utils;

import static com.alcineo.utils.common.StringUtils.convertBytesToHex;
import static com.yelloco.nexo.crypto.StringUtil.toHexString;
import static com.yelloco.payment.transaction.TransactionResult.DECLINED;
import static com.yelloco.payment.utils.TlvTagEnum.CVM_RESULTS;
import static com.yelloco.payment.utils.TlvTagEnum.TRANSACTION_RESULT;

import android.util.Log;

import com.alcineo.utils.tlv.TlvItem;
import com.yelloco.payment.data.tagstore.EmvTagStore;
import com.yelloco.payment.data.tagstore.TagReader;
import com.yelloco.payment.data.tagstore.TagStore;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class TlvUtils {

    private static final String SPACE = " ";
    private static final String CV_RULE_SIGNATURE = "1E";
    private static final String PAR_BRACKET_LEFT = "[";
    private static final String PAR_BRACKET_RIGHT = "]";
    private static final String COLON = ":";

    public static boolean isSignatureRequired(TagReader tagReader) {

        String transactionResultHex = toHexString(tagReader.getTag(TRANSACTION_RESULT.getTag()));
        if (transactionResultHex != null && transactionResultHex.equals(DECLINED.getCode())) {
            return false;
        }

        String cvmResultsHex = toHexString(tagReader.getTag(CVM_RESULTS.getTag()));
        if (cvmResultsHex != null && cvmResultsHex.startsWith(CV_RULE_SIGNATURE)) {
            Log.d("TlvUtils", "CVM results: " + cvmResultsHex);
            return true;
        }

        return false;
    }

    /**
     * Creates the internal representation of tag store from the Alcineo's tlv items list
     */
    public static TagStore createTagStore(List<TlvItem> tlvItems) {
        TagStore tagStore = new EmvTagStore();

        addItemsRecursively(tlvItems, tagStore);
        return tagStore;
    }

    private static void addItemsRecursively(List<TlvItem> tlvItems, TagStore tagStore) {
        if (tlvItems != null && !tlvItems.isEmpty()) {
            for (TlvItem tlvItem : tlvItems) {
                tagStore.setTag(tlvItem.getTag().toHexString(), tlvItem.getValue());
                addItemsRecursively(tlvItem.getChildren(), tagStore);
            }
        }
    }

    /**
     * Lists all tags from tag store concatenated String value
     */
    public static String listTags(TagReader tagReader) {
        Map<String, byte[]> allTags = tagReader.getAllTags();
        StringBuilder stringBuilder = new StringBuilder();
        if (!allTags.isEmpty()) {
            for (String s : allTags.keySet()) {
                stringBuilder.append(SPACE).append(PAR_BRACKET_LEFT).append(s).append(COLON).append(
                        convertBytesToHex(allTags.get(s))).append(PAR_BRACKET_RIGHT);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Converting length into byte array according to BER specification.
     * @param length length of TLV value in integer type
     * @return length of TLV value in byte[] type according BER spec.
     */
    public static byte[] getTLVLength(int length) {
        if (length <= 127)
            return new byte[]{(byte) length};
        return  length <= 32767 ?
                        ByteBuffer.allocate(3).put((byte) 0x82).putShort((short) length).array() :
                        ByteBuffer.allocate(5).put((byte) 0x84).putInt(length).array();
    }
}