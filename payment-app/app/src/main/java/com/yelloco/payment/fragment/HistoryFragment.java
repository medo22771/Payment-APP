package com.yelloco.payment.fragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.yelloco.payment.BaseActivity;
import com.yelloco.payment.R;
import com.yelloco.payment.data.PaymentContentProvider;
import com.yelloco.payment.host.HostManager;
import com.yelloco.payment.list.TransactionsAdapter;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class HistoryFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final String TAG = "HistoryFragment";

    private static final int TRANSACTIONS_LOADER = 1;

    private TransactionsAdapter mAdapter;
    private HostManager mHostManager;

    private TextView mCancelButton;

    public HistoryFragment() {
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

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mAdapter = new TransactionsAdapter(getContext(), null, 0);
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(TRANSACTIONS_LOADER, null, this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setOnItemClickListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showLoading(true);
        CursorLoader cursorLoader;
        switch (id) {
            case TRANSACTIONS_LOADER:
                Uri uri = PaymentContentProvider.CONTENT_URI_TRANSACTION;
                cursorLoader = new CursorLoader(getActivity(), uri, null, null, null, null);
                return cursorLoader;
            default:
                Log.w(TAG, "Unknown loader");
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        showLoading(false);
        switch (loader.getId()) {
            case TRANSACTIONS_LOADER:
                Log.d(TAG, "Loaded from database: " + data.getCount());
                mAdapter.changeCursor(data);
                break;
            default:
                Log.w(TAG, "Unknown loader");
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        showLoading(false);
        switch (loader.getId()) {
            case TRANSACTIONS_LOADER:
                mAdapter.changeCursor(null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TransactionDetailDialogFragment dialog = TransactionDetailDialogFragment.newInstance(id);
        dialog.setHostManager(this.mHostManager);
        dialog.show(getFragmentManager(), "transaction_detail");
    }

    private void showLoading(boolean show){
        ((BaseActivity)getActivity()).showLoading(show);
    }

    public void setHostManager(HostManager hostManager) {
        this.mHostManager = hostManager;
    }
}