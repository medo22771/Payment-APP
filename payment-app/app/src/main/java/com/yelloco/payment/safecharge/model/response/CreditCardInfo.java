package com.yelloco.payment.safecharge.model.response;

/**
 * Created by Fatoumata on 13/12/2016.
 */

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Fatoumata on 13/12/2016.
 */

@Root(name ="CreditCardInfo" )
public class CreditCardInfo
{
    @Element(name = "IsPrepaid", required = false)
    private String isPrepaid;

    @Element(name = "CardType", required = false)
    private String cardType;

    @Element(name = "CardProgram", required = false)
    private String cardProgram;

    @Element(name = "CardProduct", required = false)
    private String CardProduct;


    public String getIsPrepaid ()
    {
        return isPrepaid;
    }

    public void setIsPrepaid (String IsPrepaid)
    {
        this.isPrepaid = IsPrepaid;
    }

    public String getCardType ()
    {
        return cardType;
    }

    public void setCardType (String CardType)
    {
        this.cardType = CardType;
    }

    public String getCardProgram ()
    {
        return cardProgram;
    }

    public void setCardProgram (String CardProgram)
    {
        this.cardProgram = CardProgram;
    }

    public String getCardProduct ()
    {
        return CardProduct;
    }

    public void setCardProduct (String CardProduct)
    {
        this.CardProduct = CardProduct;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [IsPrepaid = "+isPrepaid+", CardType = "+cardType+", CardProgram = "+cardProgram+", CardProduct = "+CardProduct+"]";
    }
}
