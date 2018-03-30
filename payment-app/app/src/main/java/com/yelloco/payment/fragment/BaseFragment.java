package com.yelloco.payment.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import com.yelloco.payment.BaseActivity;
import com.yelloco.payment.MainActivity;
import com.yelloco.payment.transaction.TransactionContext;

/**
 * Base fragment for all payment fragments
 */
public abstract class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    // Temporary solution to provide additional elements for simulation (until real functionality is available)
    View simulationView;
    // Dispatcher to be used for calling all payment framework commands
    CommandDispatcher commandDispatcher;
    // Transaction context specific for current transaction
    TransactionProvider transactionProvider;

    public static BaseFragment newInstance(Class<? extends BaseFragment> clazz) {
        return newInstance(clazz, null);
    }

    public static BaseFragment newInstance(Class<? extends BaseFragment> clazz, Bundle bundle) {
        try {
            BaseFragment fragment = clazz.newInstance();
            if (bundle != null)
                fragment.setArguments(bundle);
            return fragment;
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            Log.e(TAG, "Failed to create new instance of " + clazz.getSimpleName() + ": ", e);
            return null;
        }
    }

    public void setSimulationView(View simulationView) {
        this.simulationView = simulationView;
    }

    public interface CommandDispatcher {
        void dispatchCommand(Runnable command, Runnable failure);
        void dispatchCommand(Runnable command);
    }

    public interface TransactionProvider {
        TransactionContext getTransactionContext();
        void finishTransaction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            commandDispatcher = (CommandDispatcher) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + CommandDispatcher.class.getSimpleName());
        }
        try {
            transactionProvider = (TransactionProvider) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + TransactionProvider.class.getSimpleName());
        }
    }

    public void showLoading(boolean show){
        if(getActivity() instanceof BaseActivity){
            ((BaseActivity) getActivity()).showLoading(show);
        }
    }

    public void showProcessingFragment(){
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchFragment(new ProcessingFragment(), false);
        }
    }
}
