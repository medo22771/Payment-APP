package com.yelloco.payment.safecharge.model.response;

/**
 * Created by Fatoumata on 13/12/2016.
 */

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 13/12/2016.
 */

@Root(name = "ReasonCodes")
public class ReasonCodes
{
    @Element(name = "Reason", required = false)
    private Reason reason;

    public Reason getReason ()
    {
        return reason;
    }

    public void setReason (Reason Reason)
    {
        this.reason = Reason;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Reason = "+reason+"]";
    }
}
