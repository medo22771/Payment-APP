package com.yelloco.payment.safecharge.model.response;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 14/12/2016.
 */

@Root
public class Recommendations
{
    @ElementList(name = "Recommendation", required = false, inline = true)
    private List<String> Recommendation;

    public List<String> getRecommendation() {
        return Recommendation;
    }

    public void setRecommendation(List<String> recommendation) {
        Recommendation = recommendation;
    }
}
