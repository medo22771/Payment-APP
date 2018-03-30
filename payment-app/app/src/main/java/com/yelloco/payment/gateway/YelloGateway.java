package com.yelloco.payment.gateway;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YelloGateway implements Gateway {
    private static final String TAG = YelloGateway.class.getSimpleName();

    private final String url;

    public YelloGateway(String url) {
        this.url = url;
    }

    @Override
    public String sendRequest(String serializedNexoRequest) throws IOException {
        String serializedNexoResponse = "";
        OkHttpClient client = new OkHttpClient();

        Log.d("url", url);

        FormBody.Builder body = new FormBody.Builder();

        body.add("p1", "1");
        body.add("p2", "1");

        try {
            body.add("p3", URLEncoder.encode(serializedNexoRequest, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to encode message: ", e);
            return serializedNexoResponse;
        }

        Request req = new Request.Builder().url(url).post(body.build()).build();

        Response response = client.newCall(req).execute();
        if (response != null) {
            serializedNexoResponse = response.body().string();
            Log.d(TAG, "Response from yello gateway \n" + serializedNexoResponse);
        }
        return serializedNexoResponse;
    }
}
