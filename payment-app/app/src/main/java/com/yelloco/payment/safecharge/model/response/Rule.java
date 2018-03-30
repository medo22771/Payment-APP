package com.yelloco.payment.safecharge.model.response;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 14/12/2016.
 */
@Root
public class Rule
{
    @Element(name = "ID", required = false)
    private String id;

    @Element(name = "Description", required = false)
    private String Description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
