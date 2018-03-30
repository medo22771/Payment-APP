package com.yelloco.payment.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.yelloco.payment.R;
import com.yelloco.payment.utils.DisplayMessageEnum;

/**
 * Fragment responsible for displaying messages received from payment framework
 */
public class MessageFragment extends BaseFragment {

    public static final String TEXT = "TEXT";

    private LinearLayout mLinearLayoutProcessing;

    public MessageFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_message, container, false);
        TextView textView = (TextView) view.findViewById(R.id.message);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String text = getArguments().getString("TEXT", "");
            //Temporary solution to display a space between amount and currency
            text = text.replaceFirst("(?<=[^0-9.])(?=[0-9])", " ");
            if(text.contains(DisplayMessageEnum.PROCESSING.english) || text.contains(DisplayMessageEnum.PROCESSING.french)){
                mLinearLayoutProcessing = (LinearLayout) view.findViewById(R.id.ll);
                mLinearLayoutProcessing.setVisibility(View.VISIBLE);
                mLinearLayoutProcessing.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.pulse));
            }
            textView.setText(text);
        }
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.message_layout);
        if (simulationView != null)
            layout.addView(simulationView);
        return view;
    }

}
