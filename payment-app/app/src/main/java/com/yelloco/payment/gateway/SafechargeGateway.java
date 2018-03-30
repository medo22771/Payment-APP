package com.yelloco.payment.gateway;

import android.util.Log;
import android.util.Xml;

import com.yelloco.nexo.message.acquirer.DocumentAuthReq;
import com.yelloco.nexo.process.XmlParser;
import com.yelloco.payment.safecharge.SafechargeProcessor;

import org.jibx.runtime.JiBXException;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class SafechargeGateway implements Gateway {

    private static final String TAG = SafechargeGateway.class.getSimpleName();

    private final String mUrl;

    public SafechargeGateway(String url) {
        this.mUrl = url;
    }

    @Override
    public String sendRequest(String request) throws IOException {
        DocumentAuthReq documentAuthReq = null;
        try {
            XmlParser.parseXml(DocumentAuthReq.class, request);
        } catch (JiBXException e) {
            Log.i(TAG, "Failed to parse NEXO request in order to transform to safecharge.");
            return null;
        }
        RequestBody body = SafechargeProcessor.nexoAuthToSafecharge(documentAuthReq);
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(mUrl).post(body).build();

        final Buffer buffer = new Buffer();
        body.writeTo(buffer);
        String reqStr = buffer.readUtf8();

        Log.d("Request", reqStr);

        Response response = client.newCall(req).execute();
        return (response != null) ? response.body().string() : null;
    }
}
