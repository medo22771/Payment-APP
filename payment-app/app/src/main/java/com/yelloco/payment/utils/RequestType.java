package com.yelloco.payment.utils;

public enum RequestType {
    /**
     * Request coming from another android application as an intent via YelloPayAPI
     */
    YELLO_PAY_API,
    /**
     * Request coming from UI of this YelloPay application
     */
    YELLO_PAY_UI
}
