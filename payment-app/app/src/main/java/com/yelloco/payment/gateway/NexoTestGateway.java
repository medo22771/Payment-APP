package com.yelloco.payment.gateway;

import android.util.Log;

import com.yelloco.payment.tcp.TcpClientUtil;

import java.io.IOException;
import java.net.Socket;

public class NexoTestGateway implements Gateway {

    private static final String TAG = NexoTestGateway.class.getSimpleName();

    private final String mAddress;
    private final int mPort;
    private final int mTimeoutMs;

    public NexoTestGateway(String address, int port, int timeoutMs) {
        mAddress = address;
        mPort = port;
        mTimeoutMs = timeoutMs;
    }

    @Override
    public String sendRequest(String request) throws IOException {
        Socket socket = null;
        String response;
        try {
            socket = new Socket(mAddress, mPort);
            socket.setSoTimeout(mTimeoutMs);
            TcpClientUtil.sendMessage(request, socket);
            response = TcpClientUtil.readMessage(socket);
            return response;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close socket: ", e);
                }
            }
        }
    }
}
