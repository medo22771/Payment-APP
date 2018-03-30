package com.yelloco.payment;

import static junit.framework.Assert.assertEquals;

import android.support.test.InstrumentationRegistry;
import android.util.Log;
import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.safecharge.SafechargeProcessor;
import com.yelloco.payment.safecharge.model.response.SafeChargeResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.transform.RegistryMatcher;

/**
 * Created by sylchoquet on 10/11/17.
 */

public class SafechargeTest {

    private static final String NEXO_REQ_CARD6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:caaa.001.001.02\">\n" +
            "   <AccptrAuthstnReq>\n" +
            "      <Hdr>\n" +
            "         <MsgFctn>AUTQ</MsgFctn>\n" +
            "         <PrtcolVrsn>3.0</PrtcolVrsn>\n" +
            "         <XchgId>149</XchgId>\n" +
            "         <CreDtTm>2017-11-16T12:51:57.139Z</CreDtTm>\n" +
            "         <InitgPty>\n" +
            "            <Id>66000001</Id>\n" +
            "         </InitgPty>\n" +
            "         <RcptPty>\n" +
            "            <Id>nexo-acquirer-1</Id>\n" +
            "         </RcptPty>\n" +
            "      </Hdr>\n" +
            "      <AuthstnReq>\n" +
            "         <Envt>\n" +
            "            <POI>\n" +
            "               <Id>\n" +
            "                  <Id>1</Id>\n" +
            "               </Id>\n" +
            "            </POI>\n" +
            "            <Card>\n" +
            "               <PrtctdCardData>\n" +
            "                  <CnttTp>DATA</CnttTp>\n" +
            "                  <EnvlpdData>\n" +
            "                     <Rcpt>\n" +
            "                        <KEK>\n" +
            "                           <KEKId>\n" +
            "                              <KeyId>SpecV1TestKey</KeyId>\n" +
            "                              <KeyVrsn>2010060715</KeyVrsn>\n" +
            "                              <DerivtnId>OYclpQE=</DerivtnId>\n" +
            "                           </KEKId>\n" +
            "                           <KeyNcrptnAlgo>\n" +
            "                              <Algo>DKP9</Algo>\n" +
            "                           </KeyNcrptnAlgo>\n" +
            "                           <NcrptdKey>4pAgABc=</NcrptdKey>\n" +
            "                        </KEK>\n" +
            "                     </Rcpt>\n" +
            "                     <NcrptdCntt>\n" +
            "                        <CnttTp>EVLP</CnttTp>\n" +
            "                        <CnttNcrptnAlgo>\n" +
            "                           <Algo>E3DC</Algo>\n" +
            "                           <Param>\n" +
            "                              <InitlstnVctr>q5UGo7YPemU=</InitlstnVctr>\n" +
            "                           </Param>\n" +
            "                        </CnttNcrptnAlgo>\n" +
            "                        <NcrptdData>QUfP+nPbNJmtMl4VSejAoDSzOnuM4d+mE13qq5/SG/XHmKKhBqeNNDIBcUaHnGyj33mTkewgnMjl0TKK\n" +
            "                                                                 GNuOEmvTV9/vV9WUYz8bR7mnGGNdxiAQBFbH/O2B6VpIOP51YO1c0FGVizB0YZaFpsLLc9vDwtrl\n" +
            "                                                                 zHIJX01kzBi8130IMDY390/YiI3rDRWbKSutYWDiIspwAQjnjgTtJbYv9JaLO9XzSa5C2QKDoGw2\n" +
            "                                                                 whCzVMmE/UmG83v4Nebgq6ml7VLk5qpnwIOcZsMiWdFqMf5zHcI9cwpdqYOYHuyVnX7k6wirg2Wt\n" +
            "                                                                 8Ez5kvYjYpYiMDK9ueVoVQTjxNpXYpRX0ALaO+Dw2aroT2BF3MdZNH2PMfdpDDaZ4jmGC3TCKwHx\n" +
            "                                                                 HRqyFQdcycs=</NcrptdData>\n" +
            "                     </NcrptdCntt>\n" +
            "                  </EnvlpdData>\n" +
            "               </PrtctdCardData>\n" +
            "            </Card>\n" +
            "         </Envt>\n" +
            "         <Cntxt>\n" +
            "            <PmtCntxt>\n" +
            "               <CardDataNtryMd>CICC</CardDataNtryMd>\n" +
            "            </PmtCntxt>\n" +
            "         </Cntxt>\n" +
            "         <Tx>\n" +
            "            <TxCaptr>true</TxCaptr>\n" +
            "            <TxTp>CRDP</TxTp>\n" +
            "            <MrchntCtgyCd>01</MrchntCtgyCd>\n" +
            "            <TxId>\n" +
            "               <TxDtTm>2017-11-16T12:51:57.147Z</TxDtTm>\n" +
            "               <TxRef>Thu Nov 16 12:51:57 GMT+00:00 2017</TxRef>\n" +
            "            </TxId>\n" +
            "            <TxDtls>\n" +
            "               <Ccy>EUR</Ccy>\n" +
            "               <TtlAmt>88.99</TtlAmt>\n" +
            "               <ICCRltdData>mgMXERafAgYAAAAAiJlaCEdhc5ABAQAQnxAHBgEKA6CYBIICfACODgAAAAAAAAAAHgMCAx8AXyQDIhIx\n" +
            "                                                                 XyUDCQcBnwYHoAAAAAMQEJ8HAv8Anw0F8EAAiACfDgUAEAAAAJ8PBfBAAJgAnyYIE+7ZNCUUUj6f\n" +
            "                                                                 JwGAnzYCAIGcAQCfMwPg+MifNAMeAwCfNwTNjfvxnzkBBZ9ABXAA4KABlQUIgAAIAJsC6ADf3wAB\n" +
            "                                                                 QJ8eCDEyMzQ1Njc4nxoCCEBfKgIFUp8BBgAAAAAAAZ8hAxUARlcRR2FzkAEBABDSISIBEUOERIlf\n" +
            "                                                                 IBpWSVNBIEFDUVVJUkVSIFRFU1QgQ0FSRCAwNp8DBgAAAAAAAJ81ASKfCAIAjV80AQFfMAICAV8o\n" +
            "                                                                 AghAUAtWSVNBIENSRURJVJ8SD0NSRURJVE8gREUgVklTQZ8RAQGfQgIIQA==</ICCRltdData>\n" +
            "            </TxDtls>\n" +
            "         </Tx>\n" +
            "      </AuthstnReq>\n" +
            "      <SctyTrlr>\n" +
            "         <CnttTp>AUTH</CnttTp>\n" +
            "         <AuthntcdData>\n" +
            "            <Rcpt>\n" +
            "               <KEK>\n" +
            "                  <KEKId>\n" +
            "                     <KeyId>SpecV1TestKey</KeyId>\n" +
            "                     <KeyVrsn>2010060715</KeyVrsn>\n" +
            "                     <DerivtnId>OYclpQE=</DerivtnId>\n" +
            "                  </KEKId>\n" +
            "                  <KeyNcrptnAlgo>\n" +
            "                     <Algo>DKP9</Algo>\n" +
            "                  </KeyNcrptnAlgo>\n" +
            "                  <NcrptdKey>4pAgABc=</NcrptdKey>\n" +
            "               </KEK>\n" +
            "            </Rcpt>\n" +
            "            <MACAlgo>\n" +
            "               <Algo>MCCS</Algo>\n" +
            "            </MACAlgo>\n" +
            "            <NcpsltdCntt>\n" +
            "               <CnttTp>DATA</CnttTp>\n" +
            "            </NcpsltdCntt>\n" +
            "            <MAC>z+m89rjOMAWCJRBolxAbiHLxFweiB6RHoWUT0sZ3WiA=</MAC>\n" +
            "         </AuthntcdData>\n" +
            "      </SctyTrlr>\n" +
            "   </AccptrAuthstnReq>\n" +
            "</Document>";

    @Test
    public void safechargeStatic() throws Exception {

        SimpleDateFormat sdftime = new SimpleDateFormat("HHmmss");
        SimpleDateFormat sdfdate = new SimpleDateFormat("yyMMdd");

        double trxAmount = 10.0;
        String pan = "4761739001010010";
        String expDate = "221231";
        String expMonth = "";
        String expYear = "";
        String trackData = "";
        String posIccBase64 = "";
        String authType = "SALE";

        expMonth = expDate.substring(2, 4);
        expYear = expDate.substring(0, 2);

        Date date = new Date();

        OkHttpClient client = new OkHttpClient();


        RequestBody body = new FormBody.Builder()
                .add("sg_CardNumber", pan)
                .add("sg_FirstName", "Test")
                .add("sg_LastName", "Test")
                .add("sg_Zip", "75002")
                .add("sg_ExpMonth", expMonth)
                .add("sg_ExpYear", expYear)
                .add("sg_Amount", Double.toString(trxAmount))
                .add("sg_Address", "Street test")
                .add("sg_City", "testCity")
                .add("sg_Country", "250")
                .add("sg_Phone", "12345678910")
                .add("sg_Email", "test@yelloco.com")
                .add("sg_VendorID", "31111")
                .add("sg_Website", "31111")
                .add("sg_TransType", "Auth")
                // .add("sg_AuthType", authType)
                .add("sg_IPAddress", "78.193.202.123")
                .add("sg_ClientPassword", "Fxf7ibpbqa")
                .add("sg_ClientLoginID", "YelloTestTRX")
                .add("sg_ClientUniqueID", "test")
                .add("sg_ResponseFormat", "4")
                .add("sg_Currency", "EUR")
                .add("sg_Version", "4.0.6")
                .add("sg_NameOnCard", "TEST CARD")
                .add("sg_ClientID", "11111")
                .add("sg_UserID", "d1107ada11ef")
                .add("sg_TerminalID", "5123")
                .add("sg_POSTrackData", trackData)
                .add("sg_POSTrackType", "2")
                // .add("sg_POSICC", "9F02060000000150009F03060000000000009F1A020250950500000080005F2A0209789A031511029C01009F37046923AE3D9F3501259F450200009F34031F00029B02E8004F07A00000000410109F080200029F2608E4502CFE00CA50D99F21031322119F410404130000000000115F3401038A0200009F150211229F160F3132333435363738000000000000009F3901059F1C08454D5677656220359F2005079360805F9F010611223344556657115413000000000011D2512601079360805F9F0D05FC50A000009F0E0500000000009F0F05F870A498005F300206015F280200569F0702FF00500A4D4153544552434152449F120A4D6173746572436172648407A00000000410109F1101019F42020978")
                // .add("sg_POSICC", "9F02060000000010009F03069F1A0295055F9F1A020840950542880460005F2A0209789A031701189C01009F3704BB85055C9F34034203009B02E8004F07A00000000410109F08028D009F2608B0EFCCE06A65DFA89F210316071782025C009F3602015D9F2701809F1E0831323334353637389F100706010A03A0B9009F3303E0F8C85F25031409125F24031703315A0840617306677604178A0230309F160F3132333435363738000000000000009F3901059F010600000000000157114061730667760417D1703226106760799F0D05F068BC88009F0E0500100000009F0F05F068BC88005F300202265F280208549F0702FF00500C5669736120507265706169649F120C5669736120507265706169648407A00000000310109F1101019F42020952")
                .add("sg_POSICC", posIccBase64)
                .add("sg_POSPINData", "0000")
                .add("sg_POSEntryMode", "3")
                .add("sg_POSTerminalCapability", "11111")
                .add("sg_POSTerminalAttendance", "0")
                //.add("sg_POSCardSequenceNum", "") NOT MANDATORY BECAUSE TAG 5F34 NOT INCLUDE IN SG_ICC
                //.add("sg_POSOfflineResCode", "") not  mandatory for offline trx
                .add("sg_POSLocalTime", sdftime.format(date).toString()) //terminal's local time in hhmmss format
                .add("sg_POSLocalDate", sdfdate.format(date).toString())
                .add("sg_POSCVMethod", "5")
                .add("sg_POSCVEntity", "4")
                //.add("sg_AutoReversal", "")
                //.add("sg_AutoReversalAmount", "")
                //.add("sg_AutoReversalCurrency", "")
                .add("sg_Channel", "3")
                .add("sg_SuppressAuth", "0")
                .add("sg_POSTerminalCity", "Paris")
                .add("sg_POSTerminalAddress", "100 Main St.")
                .add("sg_POSTerminalCountry", "FR")
                .add("sg_POSTerminalZip", "75002")
                //.add("sg_POSTerminalState", "")
                .add("sg_POSTerminalModel", "yelloXPadTest")
                .add("sg_POSTerminalManufacturer", "yello")
                .add("sg_POSTerminalMACAddress", "8c8404564c01")
                // .add("sg_POSTerminalKernel", "EMVCTL1 EMVCTL2 EMVCTLL1 paywave expresspay")
                .add("sg_POSTerminalIMEI", "FERS65AEGUNRKJMZ")
                // .add("sg_CCToken", "")
                //.add("sg_Rebill", "")
                //.add("sg_TemplateID", "")
                //.add("sg_SharedToken", "")
                .add("sg_Ship_Country", "FR")
                //.add("sg_Ship_State", "")
                .add("sg_Ship_City", "Caen")
                .add("sg_Ship_Address", "test ship address")
                .add("sg_Ship_Zip", "14000")
                //.add("sg_ApiType", "")
                // .add("sg_PARes", "")
                .add("sg_POSOutputCapability", "4")

                //.add("sg_MerchantName", "YelloTest")
                //.add("sg_PFSubMerchantId", "YelloTest")
                //post.setEntity(new UrlEncodedFormEntity(urlParameters));
                .build();

        Request request = new Request.Builder()
                .url(InstrumentationRegistry.getTargetContext().getString(
                        R.string.gateway_safecharge_test_url))
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        RegistryMatcher registryMatcher = new RegistryMatcher();
        Serializer serializer = new Persister(registryMatcher, new Format(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));

        SafeChargeResponse resp = serializer.read(SafeChargeResponse.class, response.body().string());

        Log.d("Response", resp.toString());

        assertEquals(resp.getErrCode(), "0");
    }

    @Test
    public void safeChargeDynamic() throws Exception {

        DocumentAuthReq documentAuthReq = XmlParser.parseXml(DocumentAuthReq.class, NEXO_REQ_CARD6);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(InstrumentationRegistry.getTargetContext().getString(
                        R.string.gateway_safecharge_test_url))
                .post(SafechargeProcessor.nexoAuthToSafecharge(documentAuthReq))
                .build();

        Response response = client.newCall(request).execute();

        RegistryMatcher registryMatcher = new RegistryMatcher();
        Serializer serializer = new Persister(registryMatcher, new Format(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));

        SafeChargeResponse resp = serializer.read(SafeChargeResponse.class, response.body().string());
        assertEquals(resp.getErrCode(), "0");
    }


}