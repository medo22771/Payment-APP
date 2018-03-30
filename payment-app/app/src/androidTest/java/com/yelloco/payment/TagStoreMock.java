package com.yelloco.payment;

import com.yelloco.payment.data.tagstore.EmvTagStore;
import com.yelloco.payment.data.tagstore.TagStore;
import com.yelloco.payment.utils.TlvTagEnum;
import com.yelloco.payment.utils.Utils;

public class TagStoreMock {

    public final TagStore DEFAULT = new EmvTagStore();

    private TagStoreMock() {
        DEFAULT.setTag(TlvTagEnum.APPLICATION_CRYPTO.getTag(),"4E51D7A9FDD374CD");
        DEFAULT.setTag(TlvTagEnum.CRYPTOGRAM_INFO.getTag(), "80");
        DEFAULT.setTag(TlvTagEnum.ISSUER_APP_DATA.getTag(),
                "0210A00000000000000000000000000000FF");
        DEFAULT.setTag(TlvTagEnum.UNPREDICTABLE_NB.getTag(),"69FF4323");
        DEFAULT.setTag(TlvTagEnum.APPLICATION_TRANSACTION_COUNTER.getTag(),"39");
        DEFAULT.setTag(TlvTagEnum.TERMINAL_VERIFICATION_RESULTS.getTag(), TVR1);
        DEFAULT.setTag(TlvTagEnum.TRANSACTION_DATE.getTag(),"700101");
        DEFAULT.setTag(TlvTagEnum.TRANSACTION_TYPE.getTag(), "00");
        DEFAULT.setTag(TlvTagEnum.AMOUNT_AUTHORIZED.getTag(),"000000000024");
        DEFAULT.setTag(TlvTagEnum.TRANSACTION_CURRENCY_CODE.getTag(),"0978");
        DEFAULT.setTag(TlvTagEnum.APPLICATION_INTERCHANGE_PROFILE.getTag(),"3000");
        DEFAULT.setTag(TlvTagEnum.TERMINAL_COUNTRY_CODE.getTag(),"0840");
        DEFAULT.setTag(TlvTagEnum.CVM_RESULTS.getTag(), "420300");
        DEFAULT.setTag(TlvTagEnum.TERMINAL_CAPABILITIES.getTag(),"E0F8C8");
//        DEFAULT.setTag(TlvTagEnum.DEDICATED_FILE.getTag(), "");

        DEFAULT.setTag(TlvTagEnum.CH_NAME.getTag(),"456C6F6E204D75736B");
        DEFAULT.setTag(TlvTagEnum.PIN_DATA.getTag(),"43BA55B656926DAE");
        DEFAULT.setTag(TlvTagEnum.PLAIN_CARD_DATA.getTag(),
                "556BFEB55CF7B3A2470B1ACFDD88497B98C8C8E1A54F1B04E9C485616496AE431D024CEE46E9F32A53D16A159545B5BFC770D7A376950446F2D1EA3B394C48A89057E692EBD18564CA8198FF88E3870E98F25C8916D958CC83C1EE6B10A0C3A8D3872949B4C90C1442B1BCC205D5078EEEB6BD494605232C6B980A7C1255A22361C2142A242B3ED5FB377F7658CE922112009AE1E65EE04CA61651F3A8E1A3933736A7520131F8D29675A026CA1101E0A7A4B5619691F830D3035F6B80111CE6B1A84667E66C02CA6996D57B1235AA70FCE3410A6D81819F0CF0B797ACF89CF9B2C8DCE2EC779E195E83D6A9C10F0E757587B4E36C2ECB06");
        DEFAULT.setTag(TlvTagEnum.INITIALIZATION_VECTOR.getTag(),"C021089472D6C04D");
        DEFAULT.setTag(TlvTagEnum.ENCRYPT_ALGO.getTag(),"45334443");
    }

    /**
     * TVR with no bit set
     */
    public static final String TVR1 = "0000000000";

    /**
     * 0000040000 (Byte 3 Bit 3) Online PIN entered
     */
    public static final String TVR2 = "0000040000";

    /**
     * PIN encrypted by test keys
     */
    public static final String PIN_ENCRYPTED = "43BA55B656926DAE";

    public static TagStore getTagStoreNoPin() {
        return new TagStoreMock().DEFAULT;
    }

    public static TagStore getTagStorePinOnline() {
        TagStoreMock mock = new TagStoreMock();
        mock.DEFAULT.setTag(TlvTagEnum.TERMINAL_VERIFICATION_RESULTS.getTag(), TVR2);
        mock.DEFAULT.setTag(TlvTagEnum.PIN_DATA.getTag(), PIN_ENCRYPTED);
        return mock.DEFAULT;
    }
}
