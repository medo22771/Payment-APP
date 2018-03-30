package com.yelloco.payment.rpc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class RpcRequest {

    public static final int RPC_REQUEST_HEADER_SIZE = 16;

    /**
     * RPC protocol version number
     */
    public short version;
    /**
     * Identifier of the target subsystem
     */
    public short target;
    /**
     * Identifier of the operation
     */
    public short code;
    /**
     * Size of the payload. It is not necessary for SPI, but would be useful with other physical medias.
     * Kept for simplicity. Maximum allowed - 1024 bytes
     */
    public short size;
    /**
     * Unique number of the request
     */
    public short seq;
    /**
     * Payload must be 64bit aligned, must be zeroed
     */
    public byte[] pad = {0, 0, 0, 0, 0, 0};
    /**
     * Payload
     */
    public byte[] payload;

    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(RPC_REQUEST_HEADER_SIZE + size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(version);
        buffer.putShort(target);
        buffer.putShort(code);
        buffer.putShort(size);
        buffer.putShort(seq);
        buffer.put(pad, 0, 6);
        if (size > 0)
            buffer.put(payload);
        buffer.flip();
        return buffer;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "version=0x" + Integer.toHexString(version) +
                ", target=0x" + Integer.toHexString(target) +
                ", code=0x" + Integer.toHexString(code) +
                ", size=0x" + Integer.toHexString(size) +
                ", seq=0x" + Integer.toHexString(seq) +
                ", pad=" + Arrays.toString(pad) +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
