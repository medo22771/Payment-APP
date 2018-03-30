package com.yelloco.payment.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.yelloco.payment.R;

/**
 * Processing fragment is used for cases when whole screen is displayed by K81 (e.g. PinPad screen)
 */
public class ProcessingFragment extends BaseFragment {

    private RelativeLayout mRelativeLayout;
    private TextView mTextViewTitle;
    private LinearLayout mLinearLayout;
    private ImageView mImageViewLogo;
    private ImageView mImageViewText;

    public ProcessingFragment() {
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
        View view = inflater.inflate(R.layout.fragment_process, container, false);

        mRelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_processing);
        mLinearLayout = (LinearLayout)view.findViewById(R.id.ll);
        mTextViewTitle = (TextView) view.findViewById(R.id.tv_title);

        mImageViewLogo = (ImageView) view.findViewById(R.id.iv_processing_icon);
        mImageViewText = (ImageView) view.findViewById(R.id.iv_processing_text);

        mLinearLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.pulse));

        if (simulationView != null)
            mRelativeLayout.addView(simulationView);
        return view;
    }
}
