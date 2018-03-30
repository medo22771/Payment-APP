package com.yelloco.payment.utils.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by BACE on 29/09/2016.
 */
public class SourceSansProBold extends android.support.v7.widget.AppCompatTextView
{
    public SourceSansProBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Bold.ttf"));
    }
}
