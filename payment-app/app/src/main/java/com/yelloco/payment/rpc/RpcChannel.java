package com.yelloco.payment.rpc;

import android.content.Context;
import android.util.Log;
import com.yelloco.payment.R;
import com.yelloco.payment.utils.Utils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * RPC channel is another socket connection along with WLINK connection in order
 * to communicate with K81 (related to the API not supported by WLINK).
 * It includes initialization of socket, writing and reading according
 * to provided RPC request and provides the RPC response back. Particular API functions should be
 * implemented to be called from outside as it is done with HW INFO request.
 */
public class RpcChannel {

    private static final String TAG = RpcChannel.class.getSimpleName();

    private static final RpcChannel INSTANCE = new RpcChannel();

    private SocketChannel socketChannel;

    // we do not need version for config command
    private static final short VERSION = 0x0;
    // target set to Secure component
    private static final short TARGET = 0x2;
    // rpc request to get HW information
    private static final short HW_INFO_REQUEST = 0x1;
    // rpc request to get date-time information
    private static final short DATE_TIME_REQUEST = 0x5;
    // calculate MAC over the data sent as parameter
    private static final short RPC_DUKPT_MAC = 0x32;

    /**
     * More MAC algos can be supported according to NEXO
     */
    public enum MacAlgo {
        RETAIL_CBC
    }

    private RpcChannel() {
    }

    public static RpcChannel getInstance() {
        return INSTANCE;
    }

    public void init(Context context) throws IOException {
        Log.v(TAG, "Initializing RPC channel.");
        socketChannel = SocketChannel.open();
        socketChannel.socket().setSoTimeout(100);
        final String address = context.getString(R.string.id_wlink_ip);
        final int port = Integer.parseInt(context.getString(R.string.id_wlink_port));
        Log.i(TAG, "Using address: " + address + " and port: " + port);
        socketChannel.socket().connect(new InetSocketAddress(address, port), 1000);
    }

    /**
     * De-initialization of RPC channel and releasing resources.
     */
    public void destroy() {
        Log.v(TAG, "Destroying RPC channel.");
        if (socketChannel != null) {
            try {
                socketChannel.socket().close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close socket: ", e);
            }
        }
    }

    private RpcResponse sendRequest(RpcRequest request) throws IOException {
        Log.v(TAG, request.toString());

        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        socketChannel.write(request.toByteBuffer());
        buffer.clear();
        buffer.rewind();

        int read;
        do {
            read = socketChannel.read(buffer);
            Log.i(TAG, "Read: " + read);
        } while (read > 0);

        buffer.rewind();
        RpcResponse response = new RpcResponse();
        response.toRpcResponse(buffer);

        if (response.status < 0)
            throw new IOException("Failed to send rpc command, status received: " +
                    response.status);
        Log.i(TAG, response.toString());
        return response;
    }

    /**
     * Provides date and time from K81
     * @return date and time in K81 specific format
     * @throws IOException
     */
    public String getDateTime() throws IOException {
        RpcRequest request = new RpcRequest();
        request.code = DATE_TIME_REQUEST;
        request.target = TARGET;
        request.version = VERSION;
        RpcResponse response = sendRequest(request);
        return Utils.bytesToHex(response.payload);
    }

    /**
     * Provides info about particular K81 components
     * @return K81 components information in K81 specific format
     * @throws IOException
     */
    public String getHwInfo() throws IOException {
        RpcRequest request = new RpcRequest();
        request.code = HW_INFO_REQUEST;
        request.target = TARGET;
        request.version = VERSION;
        RpcResponse response = sendRequest(request);
        return new String(response.payload);
    }

    /**
     * Get MAC calculated on data provided. DUKPT MAC key is used which is different for request
     * or response.
     * @param isRequest true if the MAC should be calculated with key dedicated to request.
     *                  False if it should be response MAC key.
     * @param algo MAC algorithm
     * @param data data which are used for MAC calculation
     * @return MAC
     * @throws IOException
     */
    public byte[] getDukptMac(boolean isRequest, MacAlgo algo, byte[] data) throws IOException {
        RpcRequest request = new RpcRequest();
        request.code = RPC_DUKPT_MAC;
        request.target = TARGET;
        request.version = VERSION;
        request.size = (short) (data.length + 2);
        byte[] payload = new byte[request.size];

        payload[0] = isRequest ? (byte) 0x1 : (byte) 0x2;
        switch (algo) {
            case RETAIL_CBC:
                payload[1] = (byte) 0x1;
                break;

        }
        System.arraycopy(data, 0, payload, 2, data.length);
        request.payload = payload;

        RpcResponse response = sendRequest(request);
        return response.payload;
    }
}
