package com.yelloco.payment.safecharge.model.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 13/12/2016.
 */

@Root
public class Reason
{
    @Attribute(name = "code", required = false)
    private String code;

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [code = "+code+"]";
    }
}