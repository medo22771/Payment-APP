package com.yelloco.payment.data.tagstore;

import static com.yelloco.nexo.crypto.StringUtil.hexStringToBytes;
import static com.yelloco.nexo.crypto.StringUtil.toHexString;
import static com.yelloco.payment.utils.TlvTagEnum.APPLICATION_CRYPTO;
import static com.yelloco.payment.utils.TlvTagEnum.APPLICATION_INTERCHANGE_PROFILE;
import static com.yelloco.payment.utils.TlvTagEnum.CH_NAME;
import static com.yelloco.payment.utils.TlvTagEnum.CRYPTOGRAM_INFO;
import static com.yelloco.payment.utils.TlvTagEnum.PAN;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.yelloco.payment.nexo.NexoHelper;
import com.yelloco.payment.nexo.NexoProcessor;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class TagStoreTest {

    public static final String EXPECTED_ONLINE_DATA =
            "9A037001019F02060000000000249F10120210A00000000000000000000000000000FF820230009F26084E51D7A9FDD374CD9F2701809F360200279C01009F3303E0F8C89F34034203009F370469FF4323950508A02400009F1A0208405F2A020978";

    private TagStore tagStore;

    @Before
    public void setUp() throws Exception {
        tagStore = new EmvTagStore();
    }

    @Test
    public void canTagsBeStoredWhenRequested() {
        tagStore.setTag("9F02", new byte[]{0});
        tagStore.setTag("9F03", new byte[]{0});

        assertTrue(tagStore.getAllTags().size() == 2);
    }

    @Test
    public void canParticularTagBeReadWhenRequested() {
        tagStore.setTag("9A", new byte[]{1, 2, 3, 4});

        byte[] tagValue = tagStore.getTag("9A");

        assertTrue(Arrays.equals(tagValue, new byte[]{1, 2, 3, 4}));
    }

    @Test
    public void canParticularTagBeReadByEnumWhenRequested() {
        tagStore.setTag(APPLICATION_CRYPTO.getTag(), new byte[]{9, 8, 7, 6});

        byte[] tagValue = tagStore.getTag(APPLICATION_CRYPTO.getTag());

        assertTrue(Arrays.equals(tagValue, new byte[]{9, 8, 7, 6}));
    }

    @Test
    public void canTheTagStoreBeEmptinessBeExcludedWhenThereIsOneTagOnly() {
        tagStore.setTag(APPLICATION_CRYPTO.getTag(), new byte[]{9, 8, 7, 6});

        assertTrue(tagStore.hasTags());
    }

    @Test
    public void canTheTagStoreBeEmptinessBeVerifiedWhenThereIsNoTag() {
        assertFalse(tagStore.hasTags());
    }

    @Test
    public void canMultipleTagsBeObtainedWhenOneIsValidAndTwoNot() {
        tagStore.setTag(APPLICATION_INTERCHANGE_PROFILE.getTag(), new byte[]{9});
        tagStore.setTag(CH_NAME.getTag(), new byte[]{9, 8});
        tagStore.setTag(PAN.getTag(), new byte[]{0x2, 0x6, 12, 33});

        Map<String, byte[]> tags = tagStore.getTags(
                new String[]{APPLICATION_INTERCHANGE_PROFILE.getTag(),
                        CRYPTOGRAM_INFO.getTag(), PAN.getTag()});

        assertTrue(tags.size() == 2);
        assertFalse(tags.containsKey(CRYPTOGRAM_INFO));
    }

    @Test
    public void areIccDataCreatedProperlyWhenTagStoreWithTagsIsGiven() {
        createOnlineData();

        byte[] onlineData = Base64.decodeBase64(NexoHelper.createIcc(tagStore));

        assertEquals(EXPECTED_ONLINE_DATA, toHexString(onlineData));
    }

    private void createOnlineData() {
        tagStore.setTag("9A", hexStringToBytes("700101"));
        tagStore.setTag("9F02", hexStringToBytes("000000000024"));
        tagStore.setTag("9F10", hexStringToBytes("0210A00000000000000000000000000000FF"));
        tagStore.setTag("82", hexStringToBytes("3000"));
        tagStore.setTag("9F26", hexStringToBytes("4E51D7A9FDD374CD"));
        tagStore.setTag("9F27", hexStringToBytes("80"));
        tagStore.setTag("9F36", hexStringToBytes("0027"));
        tagStore.setTag("9C", hexStringToBytes("00"));
        tagStore.setTag("9F33", hexStringToBytes("E0F8C8"));
        tagStore.setTag("9F34", hexStringToBytes("420300"));
        tagStore.setTag("9F37", hexStringToBytes("69FF4323"));
        tagStore.setTag("95", hexStringToBytes("08A0240000"));
        tagStore.setTag("9F1A", hexStringToBytes("0840"));
        tagStore.setTag("5F2A", hexStringToBytes("0978"));
    }
}