package com.yelloco.payment.rpc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class RpcResponse {

    public static final int RPC_RESPONSE_HEADER_SIZE = 16;

    /**
     * The version of the protocol
     */
    public short version;
    /**
     * Identifier of the source subsystem
     */
    public short source;
    /**
     * Identifier of the operation to which the request is generated
     */
    public short code;
    /**
     * The amount of data (raw, including all overheads) ready to be transferred in the next transaction.
     * Must be 0 if there are no data to transfer
     */
    public short size;
    /**
     * Unique number of the request
     */
    public short seq;
    /**
     * Status of the response
     */
    public int status;
    /**
     * The end of the structure must be 64bit aligned, must be always zeroed
     * Result code of pending RPC call
     */
    public byte[] pad = { 0, 0 };

    public byte[] payload;

    public void toRpcResponse(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        version = buffer.getShort();
        source = buffer.getShort();
        code = buffer.getShort();
        size = buffer.getShort();
        seq = buffer.getShort();
        buffer.put(pad, 0, 2);
        status = buffer.getInt();
        payload = new byte[size];
        buffer.get(payload);
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "version=0x" + Integer.toHexString(version) +
                ", source=0x" + Integer.toHexString(source) +
                ", code=0x" + Integer.toHexString(code) +
                ", size=" + size +
                ", seq=0x" + Integer.toHexString(seq) +
                ", status=0x" + Integer.toHexString(status) +
                ", pad=" + Arrays.toString(pad) +
                ", payload=" + Arrays.toString(payload) +
                ", payloadASCII=" + new String(payload) +
                '}';
    }
}
