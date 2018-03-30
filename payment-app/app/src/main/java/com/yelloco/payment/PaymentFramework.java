package com.yelloco.payment;

import static com.alcineo.administrative.commands.DispatcherUtil.registerAdministrativeCommands;
import static com.alcineo.transaction.commands.DispatcherUtil.registerTransactionCommands;

import android.content.Context;
import android.util.Log;
import com.alcineo.connection.TransportByteChannel;
import com.alcineo.connection.dispatcher.DispatcherService;
import com.alcineo.connection.protocols.WlinkProtocol;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.yelloco.payment.rpc.RpcRequest;
import com.yelloco.payment.rpc.RpcResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PaymentFramework {

    private static final String TAG = PaymentFramework.class.getSimpleName();

    private static final PaymentFramework INSTANCE = new PaymentFramework();

    private DispatcherService dispatcherService;
    private SocketChannel socketChannel;
    private TransportByteChannel transport;

    // we do not need version for config command
    private static final short VERSION = 0x0;
    // target set to RPC daemon
    private static final short TARGET = 0x3;
    // rpc instruction set to wlink mode
    private static final short CODE = 0x12;
    // timeout in wlink configuration
    private static final int TIMEOUT_MS = 3000;

    public PaymentFramework()
    {
    }

    public static PaymentFramework getInstance() {
        return INSTANCE;
    }

    /**
     * Initialization of payment framework to be ready for payment transactions processing.
     * It requires the connection to wlink which is communication bridge with K81 secure element.
     * K81 includes EMV kernel responsible for the main EMV payment logic.
     * Transaction events are provided asynchronously via transaction listener registered in
     * dispatcher service.
     *
     * @throws IOException
     */
    protected void init(Service.Listener serviceListener, Context context) throws IOException {
        Log.v(TAG, "Initializing payment dispatcher.");
        socketChannel = SocketChannel.open();
        socketChannel.socket().setSoTimeout(100);
        boolean isSimulation = Boolean.parseBoolean(context.getString(R.string.id_simulation));
        int idWlinkIP = isSimulation ? R.string.id_wlink_ip_simulator : R.string.id_wlink_ip;
        int idWlinkPort = isSimulation ? R.string.id_wlink_port_simulator : R.string.id_wlink_port;
        final String address =  context.getString(idWlinkIP);
        final int port = Integer.parseInt(context.getString(idWlinkPort));
        Log.i(TAG, "Using address: " + address + " and port: " + port);
        socketChannel.socket().connect(new InetSocketAddress(address, port), 1000);
        if (!isSimulation) {
            configureWlink(socketChannel);
        }
        transport = new TransportByteChannel(
                socketChannel, socketChannel, new WlinkProtocol());

        // Starts a background thread which reads from the specified transport and dispatchCommand
        // received commands. This service is used by the transaction manager to execute
        // a transaction and receive events from the terminal.
        dispatcherService = new DispatcherService(transport);
        dispatcherService.startAsync();
        dispatcherService.awaitRunning();

        // Registering listener for disconnection notifications :
        dispatcherService.addListener(serviceListener, MoreExecutors.directExecutor());

        // Following register methods must be called after we setup the dispatcher.
        registerAdministrativeCommands(dispatcherService.getDispatcher());
        registerTransactionCommands(dispatcherService.getDispatcher());
    }

    /**
     * De-initialization of payment framework and releasing resources.
     */
    protected void destroy()
    {
        Log.v(TAG, "Destroying payment dispatcher");
        if (dispatcherService != null) {
            dispatcherService.stopAsync();
            dispatcherService = null;
        }
        if (transport != null)
        {
            try {
                transport.close();
                transport = null;
            } catch (IOException e) {
                Log.e(TAG, "Failed to close framework dispatcher: ", e);
            }
        }
        if (socketChannel != null) {
            try {
                socketChannel.socket().close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close socket: ", e);
            }
        }
    }

    public DispatcherService getDispatcherService() {
        return dispatcherService;
    }

    private void configureWlink(SocketChannel socketChannel) throws IOException {
        Log.i(TAG, "Configuring socket for wlink mode: " + " \nCODE: 0x" + Integer.toHexString(CODE) + "\nTARGET: 0x" +
                Integer.toHexString(TARGET) + "\nVERSION: 0x" + Integer.toHexString(VERSION));
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        RpcRequest commandRequest = new RpcRequest();
        commandRequest.code = CODE;
        commandRequest.target = TARGET;
        commandRequest.version = VERSION;
        Log.v(TAG, "commandResponse: " + commandRequest.toString());
        socketChannel.write(commandRequest.toByteBuffer());
        buffer.clear();
        buffer.limit(RpcResponse.RPC_RESPONSE_HEADER_SIZE);
        long startTime = System.currentTimeMillis();
        while (buffer.hasRemaining()) {
            if (System.currentTimeMillis() - startTime > TIMEOUT_MS)
                throw new IOException("Timeout in wlink configuration response.");
            socketChannel.read(buffer);
        }
        buffer.flip();
        RpcResponse commandResponse = new RpcResponse();
        commandResponse.toRpcResponse(buffer);
        Log.v(TAG, "commandResponse: " + commandResponse.toString());
        if (commandResponse.status < 0)
            throw new IOException("Failed to configure wlink, status received: " + commandResponse.status);
    }
}
