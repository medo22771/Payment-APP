package com.yelloco.payment.safecharge.model.response;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 14/12/2016.
 */
@Root
public class Rules
{
    @ElementList(name = "Rule", required = false,  inline=true)
    private List<Rule> rule;

    public List<Rule> getRule() {
        return rule;
    }

    public void setRule(List<Rule> rule) {
        this.rule = rule;
    }
}
