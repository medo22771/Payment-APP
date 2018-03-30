package com.yelloco.payment.safecharge.model.response;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 14/12/2016.
 */
@Root
public class EMVTags
{
    @Element(name = "84", required = false)
    private String fieldEMV84;

    @Element(name = "9F33", required = false)
    private String fieldEMV9F33;

    @Element(name = "9F34", required = false)
    private String fieldEMV9F34;

    @Element(name = "9F1A", required = false)
    private String fieldEMV9F1A;

    @Element(name = "82", required = false)
    private String fieldEMV82;

    @Element(name = "9F36", required = false)
    private String fieldEMV9F36;

    @Element(name = "9F10", required = false)
    private String fieldEMV9F10;

    @Element(name = "9F26", required = false)
    private String fieldEMV9F26;

    @Element(name = "9F27", required = false)
    private String fieldEMV9F27;

    @Element(name = "9F37", required = false)
    private String fieldEMV9F37;

    @Element(name = "9F02", required = false)
    private String fieldEMV9F02;

    @Element(name = "95", required = false)
    private String fieldEMV95;

    @Element(name = "9C", required = false)
    private String fieldEMV9C;

    @Element(name = "9A", required = false)
    private String fieldEMV9A;

    @Element(name = "5F2A", required = false)
    private String fieldEMV5F2A;

}
