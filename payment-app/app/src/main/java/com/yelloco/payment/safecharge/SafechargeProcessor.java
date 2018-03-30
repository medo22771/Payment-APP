package com.yelloco.payment.safecharge;

import android.util.Log;
import com.yelloco.nexo.crypto.DESCryptoUtil;
import com.yelloco.nexo.crypto.DUKPTUtil;
import com.yelloco.nexo.crypto.StringUtil;
import com.yelloco.nexo.message.acquirer.AttendanceContext1Code;
import com.yelloco.nexo.message.acquirer.AuthenticationMethod2Code;
import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.message.acquirer.MessageFunction1Code;
import com.yelloco.nexo.message.acquirer.PlainCardData1;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.safecharge.model.response.SafeChargeResponse;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.jibx.runtime.JiBXException;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.transform.RegistryMatcher;

/**
 * Created by sylchoquet on 13/11/17.
 */

public class SafechargeProcessor {

    // sg_TransType

    private static final String TRANSACTION_TYPE_SALE = "Sale";
    private static final String TRANSACTION_TYPE_AUTH = "Auth";
    private static final String TRANSACTION_TYPE_SETTLE = "Settle";
    private static final String TRANSACTION_TYPE_CREDIT = "Credit";

    // sg_SuppressAuth

    private static final String SUPPRESS_AUTH_FALSE = "0";
    private static final String SUPPRESS_AUTH_TRUE = "1";

    // sg_POSTerminalAttendance

    private static final String POS_TERMINAL_ATTENDANCE_UNATTENDED = "0";
    private static final String POS_TERMINAL_ATTENDANCE_ATTENDED = "1";

    // sg_POSCVMethod

    private static final String POS_CV_METHOD_NOT_AUTHENTICATED = "0";
    private static final String POS_CV_METHOD_PIN = "1";
    private static final String POS_CV_METHOD_ELECTRONIC_SIGNATURE = "2";
    private static final String POS_CV_METHOD_MANUAL_SIGNATURE = "5";
    private static final String POS_CV_METHOD_OTHER_VERIFICATION = "6";
    private static final String POS_CV_METHOD_UNKNOWN = "9";
    private static final String POS_CV_METHOD_OTHER_SYSTEMATIC_VERIFICATION = "S";

    // sg_POSCVEntity

    private static final String POS_CV_ENTITY_NOT_AUTHENTICATED = "0";
    private static final String POS_CV_ENTITY_OFFLINE_CHIP = "1";
    private static final String POS_CV_ENTITY_CARD_ACCEPTANCE_DEVICE = "2";
    private static final String POS_CV_ENTITY_AUTHORIZING_AGENT_ONLINE_PIN = "3";
    private static final String POS_CV_ENTITY_ACCEPTOR_SIGNATURE = "4";
    private static final String POS_CV_ENTITY_OTHER = "5";
    private static final String POS_CV_ENTITY_UNKNOWN = "9";

    // sg_Channel

    private static final String CHANNEL_ECOMMERCE = "1";
    private static final String CHANNEL_MOTO = "2";
    private static final String CHANNEL_CARD_PRESENT = "3";

    // sg_POSEntryMode

    private static final String POS_ENTRY_MODE_MANUALLY_ENTERED = "1";
    private static final String POS_ENTRY_MODE_MAGNETIC_STRIPE = "2";
    private static final String POS_ENTRY_MODE_ICC_READ = "3";
    private static final String POS_ENTRY_MODE_CONTACTLESS_ICC = "5";
    private static final String POS_ENTRY_MODE_CONTACTLESS_MAGSTRIPE = "6";

    // sg_POSOutputCapability

    private static final String POS_OUTPUT_CAPABILITY_UNKNOWN = "0";
    private static final String POS_OUTPUT_CAPABILITY_NONE = "1";
    private static final String POS_OUTPUT_CAPABILITY_PRINTING_ONLY = "2";
    private static final String POS_OUTPUT_CAPABILITY_DISPLAY = "3";
    private static final String POS_OUTPUT_CAPABILITY_PRINTING_AND_DISPLAY = "4";

    public static final byte[] BDK = StringUtil.hexStringToBytes("37233E890B0104E9BC943D0E45EAE5A7");
    public static final byte[] KSN = StringUtil.hexStringToBytes("398725A501E290200017");

    public static RequestBody nexoAuthToSafecharge(DocumentAuthReq authReq) {

        SimpleDateFormat sdftime = new SimpleDateFormat("HHmmss", Locale.getDefault());
        SimpleDateFormat sdfdate = new SimpleDateFormat("yyMMdd", Locale.getDefault());

        PlainCardData1 cardData = decryptCard(authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCard().getPrtctdCardData().getEnvlpdData().getNcrptdCntt().getNcrptdData(),
                authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCard().getPrtctdCardData().getEnvlpdData().getNcrptdCntt().getCnttNcrptnAlgo().getParam().getInitlstnVctr());

        String icc = new String(authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxDtls().getICCRltdData());
        Log.i("SafeCharge", "ICC: " + icc);

        // Default Auth request

        FormBody.Builder body = new FormBody.Builder()
                .add("sg_CardNumber", cardData.getPAN())
                //TODO: Implement in Nexo and separate first and last name
                //.add("sg_FirstName", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCrdhldr().getNm())
                //.add("sg_LastName", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCrdhldr().getNm())
                .add("sg_FirstName", "YelloFirstName")
                .add("sg_LastName", "YelloLastName")

                //TODO: Implement in Nexo
                //.add("sg_Zip", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCrdhldr().getAdrVrfctn().getPstlCdDgts())
                .add("sg_Zip", "75002")

                .add("sg_ExpMonth", cardData.getXpryDt().substring(2, 4))
                .add("sg_ExpYear", cardData.getXpryDt().substring(0, 2))

                .add("sg_Amount", authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxDtls().getTtlAmt().toPlainString())
                //.add("sg_Address", "Street test")
                .add("sg_City", "testCity")
                .add("sg_Country", "FR")
                .add("sg_Phone", "12345678910")
                .add("sg_Email", "test@yelloco.com")
                .add("sg_VendorID", "31111")
                .add("sg_Website", "31111")
                .add("sg_TransType", determineTransactionType(authReq.getAccptrAuthstnReq().getHdr().getMsgFctn()))
                //.add("sg_AuthType", authType)
                .add("sg_IPAddress", "78.193.202.123")
                .add("sg_ClientPassword", "Fxf7ibpbqa")
                .add("sg_ClientLoginID", "YelloTestTRX")
                .add("sg_ClientUniqueID", "test")
                .add("sg_ResponseFormat", "4")
                .add("sg_Currency", authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxDtls().getCcy())
                .add("sg_Version", "4.0.6")
                .add("sg_NameOnCard", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCrdhldr().getNm())
                //.add("sg_NameOnCard", "Test Test")
                .add("sg_ClientID", "11111")
                .add("sg_UserID", "d1107ada11ef")
                .add("sg_TerminalID", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getPOI().getId().getId())

                .add("sg_POSTrackData", cardData.getTrckDataList().get(0).getTrckVal())
                .add("sg_POSTrackType", cardData.getTrckDataList().get(0).getTrckNb())
                //.add("sg_POSICC", "9F02060000000150009F03060000000000009F1A020250950500000080005F2A0209789A031511029C01009F37046923AE3D9F3501259F450200009F34031F00029B02E8004F07A00000000410109F080200029F2608E4502CFE00CA50D99F21031322119F410404130000000000115F3401038A0200009F150211229F160F3132333435363738000000000000009F3901059F1C08454D5677656220359F2005079360805F9F010611223344556657115413000000000011D2512601079360805F9F0D05FC50A000009F0E0500000000009F0F05F870A498005F300206015F280200569F0702FF00500A4D4153544552434152449F120A4D6173746572436172648407A00000000410109F1101019F42020978")
                //.add("sg_POSICC", "mgNwAQafAgYAAAAAAAVaCEdhc5ABAQEZnxAHBgEKA6AAAIICXACODgAAAAAAAAAAHgMCAx8AXyQDIhIxXyUDCQcBnwYHoAAAAAMQEJ8HAv8Anw0F8EAAiACfDgUAEAAAAJ8PBfBAAJgAnyYIE7lgBKETuKSfJwGAnzYCAAGcAQCfMwPg+MifNAMeAwCfNwQ+zYo7nzkBBZ9ABXAA4KABlQVCoAAAAJsC6ADf3wABMZ8eCDEyMzQ1Njc4nxoCCEBfKgIFUp8BBgAAAAAAAZ8hAwAhGFcRR2FzkAEBARnSISIBF1iSiIlfIBpWSVNBIEFDUVVJUkVSIFRFU1QgQ0FSRCAwMZ8DBgAAAAAAAJ81ASKfCAIAll80AQFfMAICAV8oAghAUAtWSVNBIENSRURJVJ8SD0NSRURJVE8gREUgVklTQZ8RAQGfQgIIQAAAAAA=")
                //.add("sg_POSICC", getPosiccBase64(authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxDtls().getICCRltdData()))
                .addEncoded("sg_POSICC", icc)
                // TODO: PIN in nexo
                //.add("sg_POSPINData", StringUtils.convertBytesToHex(authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCrdhldr().getAuthntcnList().get(0).getCrdhldrOnLinePIN().getNcrptdPINBlck().getEnvlpdData().getNcrptdCntt().getNcrptdData()))
                .add("sg_POSPINData", "0000")
                //.add("sg_POSEntryMode", authReq.getAccptrAuthstnReq().getAuthstnReq().getCntxt().getPmtCntxt().getCardDataNtryMd().name())
                .add("sg_POSEntryMode", POS_ENTRY_MODE_ICC_READ)
                .add("sg_POSTerminalCapability", "11111")

                //TODO: Check
                //.add("sg_POSTerminalAttendance", determineAttendanceContext(authReq.getAccptrAuthstnReq().getAuthstnReq().getCntxt().getPmtCntxt().getAttndncCntxt()))
                .add("sg_POSTerminalAttendance", POS_TERMINAL_ATTENDANCE_UNATTENDED)
                //.add("sg_POSOfflineResCode", "") not  mandatory for offline trx
                .add("sg_POSLocalTime", sdftime.format(authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxId().getTxDtTm())) //terminal's local time in hhmmss format
                .add("sg_POSLocalDate", sdfdate.format(authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxId().getTxDtTm()))
                //TODO: Implement in Nexo
                .add("sg_POSCVMethod", getMethod(authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCrdhldr().getAuthntcnList().get(0).getAuthntcnMtd()))
                //.add("sg_POSCVMethod", POS_CV_METHOD_ELECTRONIC_SIGNATURE)

                .add("sg_POSCVEntity", getEntity(authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getCrdhldr().getAuthntcnList().get(0).getAuthntcnMtd()))
                //.add("sg_POSCVEntity", POS_CV_ENTITY_CARD_ACCEPTANCE_DEVICE)

                //.add("sg_AutoReversal", "")
                //.add("sg_AutoReversalAmount", "")
                //.add("sg_AutoReversalCurrency", "")
                .add("sg_Channel", CHANNEL_CARD_PRESENT)

                //TODO: Remove hardcoded
                .add("sg_POSTerminalCity", "Paris")
                //.add("sg_POSTerminalAddress", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getMrchnt().getAdr())
                //.add("sg_POSTerminalCountry", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getMrchnt().getCtryCd())
                //.add("sg_POSTerminalZip", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getMrchnt().getSchmeData())
                //.add("sg_POSTerminalState", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getMrchnt().getSchmeData())

                .add("sg_POSTerminalAddress", "112 Rue du sentier")
                .add("sg_POSTerminalCountry", "FR")
                .add("sg_POSTerminalZip", "75002")
                //.add("sg_POSTerminalState", "")

                .add("sg_POSTerminalModel", "yelloXPadTest")
                .add("sg_POSTerminalManufacturer", "yello")
                //TODO: Remove hardcoded
                .add("sg_POSTerminalMACAddress", "8c8404564c01")
                // .add("sg_POSTerminalKernel", "EMVCTL1 EMVCTL2 EMVCTLL1 paywave expresspay")
                //TODO: Remove hardcoded
                .add("sg_POSTerminalIMEI", "FERS65AEGUNRKJMZ")
                // .add("sg_CCToken", "")
                //.add("sg_Rebill", "")
                //.add("sg_TemplateID", "")
                //.add("sg_SharedToken", "")
                .add("sg_Ship_Country", "FR")
                //.add("sg_Ship_State", "")
                .add("sg_Ship_City", "Caen")
                .add("sg_Ship_Address", "test ship adress")
                .add("sg_Ship_Zip", "14000")
                //.add("sg_ApiType", "")
                // .add("sg_PARes", "")
                .add("sg_POSOutputCapability", POS_OUTPUT_CAPABILITY_DISPLAY)

                //TODO: Replace by merchant data
                //.add("sg_MerchantPhoneNumber", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getMrchnt().getSchmeData())
                //.add("sg_MerchantName", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getMrchnt().getCmonNm())
                //.add("sg_PFSubMerchantId", authReq.getAccptrAuthstnReq().getAuthstnReq().getEnvt().getPOI().getGrpId())

                .add("sg_MerchantPhoneNumber", "0123456789")
                .add("sg_MerchantName", "Merchant Name")
                .add("sg_PFSubMerchantId", "0123")

                //TODO: CHECK
                //.add("sg_ExpectedFulfillmentCount", authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxTp().equals(CardPaymentServiceType4Code.CRDP) ? "1" :
                //authReq.getAccptrAuthstnReq().getAuthstnReq().getTx().getTxDtls().getDtldAmtList().get(0).getVal().toPlainString())

                .add("sg_ExpectedFulfillmentCount",
                    determineFulfillmentCount(authReq.getAccptrAuthstnReq().getHdr().getMsgFctn()))

                //.add("sg_PFSubMerchantId", "YelloTest")
                ;

        // Sale mandatory fields

        if(determineTransactionType(authReq.getAccptrAuthstnReq().getHdr().getMsgFctn()).equals(TRANSACTION_TYPE_SALE)){
            body.add("sg_SuppressAuth", determineSuppressAuth(authReq.getAccptrAuthstnReq().getHdr().getMsgFctn()));
        }

        // Optional parameters

        if(!cardData.getCardSeqNb().equals("")){
            body.add("sg_POSCardSequenceNum", cardData.getCardSeqNb());
        }

        return body.build();
    }

    /**
     * Get the sg_POSCVEntity from the nexo message
     * @param authntcnMtd
     * @return
     */
    private static String getEntity(AuthenticationMethod2Code authntcnMtd) {
        switch (authntcnMtd) {
            case FPIN:
                return POS_CV_ENTITY_OFFLINE_CHIP;
            case NPIN:
                return POS_CV_ENTITY_AUTHORIZING_AGENT_ONLINE_PIN;
            case CPSG:
                return POS_CV_ENTITY_ACCEPTOR_SIGNATURE;
            default:
                return POS_CV_ENTITY_UNKNOWN;
        }
    }

    /**
     * Get the sg_POSCVMethod from the nexo message
     * @param authntcnMtd
     * @return
     */
    private static String getMethod(AuthenticationMethod2Code authntcnMtd) {
        switch (authntcnMtd) {
            case NPIN:
            case FPIN:
                return POS_CV_METHOD_PIN;
            case CPSG:
                return POS_CV_METHOD_MANUAL_SIGNATURE;
            default:
                return POS_CV_METHOD_UNKNOWN;
        }
    }

    /**
     * Decrypt the card
     *
     * @param ncrptdData encrypted card data
     * @param iv         initialisation vector
     * @return the decrypted card
     */
    private static PlainCardData1 decryptCard(byte[] ncrptdData, byte[] iv) {

        PlainCardData1 cardData = null;

        try {
            byte[] decrypted = DUKPTUtil.trimNull80Padding(DESCryptoUtil.tdesDecrypt(ncrptdData, DUKPTUtil.calculateDataEncryptionKey(KSN, BDK, true), iv));

            String decryptedString = new String(decrypted);
            cardData = XmlParser.parseXml(PlainCardData1.class, decryptedString);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (JiBXException e) {
            e.printStackTrace();
        }
        return cardData;

    }

    /**
     * Determine the SuppressAuth from the Nexo message
     *
     * @param type
     * @return
     */
    //TODO: Update with future scenarios
    private static String determineSuppressAuth(MessageFunction1Code type) {

        switch(type) {
            case FAUQ:
            case FCMV:
            case FRVA:
                return SUPPRESS_AUTH_TRUE;
            case AUTQ:
            case CMPV:
            case RVRA:
                return SUPPRESS_AUTH_FALSE;
            default:
                throw new RuntimeException("Message function code not implemented");
        }
    }

    /**
     * Determine the transaction type from the nexo Message
     *
     * @param type
     * @return
     */
    //TODO: Update with future scenarios
    private static String determineTransactionType(MessageFunction1Code type) {
        switch (type) {
            case FAUQ:
                return TRANSACTION_TYPE_SALE;
            case AUTQ:
                return TRANSACTION_TYPE_AUTH;
            case CMPV:
            case FCMV:
                return TRANSACTION_TYPE_SETTLE;
            case RVRA:
            case FRVA:
                return TRANSACTION_TYPE_CREDIT;
            default:
                throw new RuntimeException("Message function code not implemented");
        }
    }

    /**
     * Determine the AttendanceContext from the nexo Message
     *
     * @param attendanceContext1Code
     * @return
     */
    public static String determineAttendanceContext(AttendanceContext1Code attendanceContext1Code) {

        switch(attendanceContext1Code) {
            case UATT:
                return POS_TERMINAL_ATTENDANCE_UNATTENDED;
            case ATTD:
                return POS_TERMINAL_ATTENDANCE_ATTENDED;
            default:
                throw new RuntimeException("Attendance context not implemented");
        }
    }

    /**
     * Determine the FulfillmentCount
     * @param type
     * @return
     */
    // TODO: Update with batch, cancel and other scenarios
    private static String determineFulfillmentCount(MessageFunction1Code type){
        switch (type) {
            case FAUQ:
            case AUTQ:
                return "1";
            default:
                throw new RuntimeException("Message function code not implemented");
        }
    }

    /**
     * Convert the String response from SafeCharge to POJO
     *
     * @param response
     * @return
     */
    public static SafeChargeResponse convertResponse(String response) {
        RegistryMatcher registryMatcher = new RegistryMatcher();
        Serializer serializer = new Persister(registryMatcher, new Format(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));

        SafeChargeResponse sc = null;

        try {
            sc = serializer.read(SafeChargeResponse.class, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sc;
    }


}
