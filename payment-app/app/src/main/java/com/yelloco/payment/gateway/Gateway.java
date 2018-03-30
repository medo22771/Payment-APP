package com.yelloco.payment.gateway;

import java.io.IOException;

public interface Gateway {

    /**
     * Sends serialized request to the gateway and provides serialized response from gateway.
     * @param request serialized request to be sent
     * @return serialized response
     * @throws IOException
     */
    String sendRequest (String request) throws IOException;
}
