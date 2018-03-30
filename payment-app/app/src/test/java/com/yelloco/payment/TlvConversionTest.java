package com.yelloco.payment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.alcineo.utils.tlv.TlvItem;
import com.alcineo.utils.tlv.TlvParser;
import com.yelloco.payment.utils.TlvUtils;
import com.yelloco.payment.utils.Utils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TlvConversionTest {

    private static final String EXAMPLE_TLVS_HEX =
            "9A031709079F02060000000010005A0849405218005140369F100706010A0360240482023C008E10000" +
            "0000000000000020142031E031F025F24031511305F25031211019F0607A00000000310109F0702FF00" +
            "9F0D05B8408488009F0E0500100000009F0F05B8689C98009F260859C774AE89EAD3EE9F2701409F360" +
            "203859C01009F3303E0F8C89F34034203009F370406EEB9039F3901059F40057000E0A0019505084004" +
            "00009B02E8009908041231DE7FFAEBFCDFDF0001379F1E0831323334353637389F1A0208405F2A02055" +
            "29F01060000000000019F210313522257134940521800514036D15112019622569584401F5F201A4D52" +
            "2054455354313320514320323039313620202020202020209F03060000000000009F3501224F07A0000" +
            "0000310109F0802008C5F3401015F300202015F28020036500B5669736120437265646974";

    @Test
    public void convertTlvs() throws Exception {
        List<TlvItem> items = TlvParser.decode(Utils.hexStringToByteArray(EXAMPLE_TLVS_HEX), 0);
        assertNotNull("Failed to parse TLVs.", items);
        printTlvItems(items);
    }

    private void printTlvItems(List<TlvItem> items) {
        if (items.size() == 0) {
            System.out.println("TLV items list is empty");
        }
        for (TlvItem item : items) {
            System.out.println(itemToString(item));
            for (TlvItem subItem : item.getChildren()) {
                System.out.println("-> " + itemToString(subItem));
            }
        }
    }

    private String itemToString(TlvItem item) {
        return "TAG(" + item.getTag().toHexString() + "), " + "LENGTH(" + item.getLength()
                .getValue() + "), VALUE(" + item.getValueHex() + ")";
    }

    @Test
    public void convertTlvLength() {
        for (int length = 1; length < 80000; length++) {
            byte[] lengthBytes = TlvUtils.getTLVLength(length);
            if (length < 0x80) {
                assertTrue(lengthBytes[0] < 0x80);
            } else {
                assertTrue((lengthBytes[0] & 0x80) == 0x80);
                byte[] lengthCheck = new byte[lengthBytes[0] & 0x7F];
                System.arraycopy(lengthBytes, 1, lengthCheck, 0, lengthCheck.length);
                ByteBuffer buffer = ByteBuffer.wrap(lengthCheck);
                buffer.rewind();
                int lengthAcquired = (lengthCheck.length < 3) ? buffer.getShort() : buffer.getInt();
                assertTrue(length == lengthAcquired);
            }
        }
    }
}