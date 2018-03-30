package com.yelloco.payment.safecharge.model.response;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 20/01/2017.
 */
@Root(name ="AmountInfo" )
public class AmountInfo
{
    @Element(name = "RequestedAmount", required = false)
    private String requestedAmount;

    @Element(name = "RequestedCurrency", required = false)
    private String requestedCurrency;

    @Element(name = "ProcessedAmount", required = false)
    private String processedAmount;

    @Element(name = "ProcessedCurrency", required = false)
    private String processedCurrency;

    @Element(name = "CardProduct", required = false)
    private String CardProduct;


    public String getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(String requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public String getRequestedCurrency() {
        return requestedCurrency;
    }

    public void setRequestedCurrency(String requestedCurrency) {
        this.requestedCurrency = requestedCurrency;
    }

    public String getProcessedCurrency() {
        return processedCurrency;
    }

    public void setProcessedCurrency(String processedCurrency) {
        this.processedCurrency = processedCurrency;
    }

    public String getCardProduct() {
        return CardProduct;
    }

    public void setCardProduct(String cardProduct) {
        CardProduct = cardProduct;
    }

    public String getProcessedAmount() {
        return processedAmount;
    }

    public void setProcessedAmount(String processedAmount) {
        this.processedAmount = processedAmount;
    }
}
