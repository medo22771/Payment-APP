package com.yelloco.payment.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Created by Fatoumata on 19/02/2016.
 */
public class TcpClientUtil
{
    public static final String SERVER_ADDRESS = "50.3.68.225";
    public static final int SERVER_PORT = 5051;

    public static String readMessage(Socket clientSocket) throws IOException {
        DataInputStream reader = new DataInputStream(clientSocket.getInputStream());
        byte[] lengthBytes = new byte[4];
        reader.readFully(lengthBytes);
        byte[] data = new byte[fromNetworkOrderedBytes(lengthBytes)];
        reader.readFully(data);
        return new String(data);
    }

    public static void sendMessage(String message, Socket socket) throws IOException {
        byte[] messageBytes = message.getBytes(Charset.forName("UTF8"));
        int msgLength = messageBytes.length;

        DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
        writer.write(getNetworkOrderedBytes(msgLength));
        writer.write(messageBytes);
        writer.flush();
    }

    private static byte[] getNetworkOrderedBytes(int i) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(i);
        bb.flip();
        return bb.array();
    }

    private static int fromNetworkOrderedBytes(byte[] bytes) {
        final int intLen = Integer.SIZE / Byte.SIZE;
        ByteBuffer bb = ByteBuffer.allocate(intLen);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(bytes, 0, intLen);
        bb.flip();

        return bb.getInt();
    }

    @SuppressWarnings("unused")
    private static byte[] getBytes(int i) {
        final int intLen = Integer.SIZE / Byte.SIZE;
        ByteBuffer bb = ByteBuffer.allocate(intLen);
        bb.putInt(i);
        return bb.array();
    }

    @SuppressWarnings("unused")
    private static int fromBytes(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}
