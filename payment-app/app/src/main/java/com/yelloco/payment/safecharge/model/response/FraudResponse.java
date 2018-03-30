package com.yelloco.payment.safecharge.model.response;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 14/12/2016.
 */
@Root
public class FraudResponse
{
    @Element(name = "FinalDecision", required = false)
    private String finalDecision;

    @Element(name = "Recommendations", required = false)
    private Recommendations recommendations;

    @Element(name = "Decision", required = false)
    private String Decision;


    @Element(name = "Rule", required = false)
    private Rules Rules;




}
