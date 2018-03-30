package com.yelloco.payment.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.alcineo.transaction.events.DisplayMenuRequestEvent;
import com.yelloco.payment.R;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Fragment responsible for displaying menu options received from payment framework.
 * User option choice is provided back payment framework.
 */
public class MenuFragment extends BaseFragment {

    private static final String TAG = MenuFragment.class.getSimpleName();

    public static final String MENU = "MENU";
    public static final String TITLE = "TITLE";

    private DisplayMenuRequestEvent event;


    public MenuFragment() {
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

        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        ListView listView = (ListView) view.findViewById(R.id.menu);
        TextView textView = (TextView) view.findViewById(R.id.message);
        Bundle bundle = getArguments();
        if (bundle != null) {
            ArrayList<String> list = bundle.getStringArrayList(MENU);
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(this.getContext(), R.layout.menu_item, R.id.item, list);
            listView.setAdapter(adapter);
            textView.setText(getArguments().getString(TITLE, ""));
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int positionOfItem = position;
                commandDispatcher.dispatchCommand(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            event.sendSelection(positionOfItem);
                            showProcessingFragment();
                        } catch (IOException e) {
                            Log.i(TAG, "Failed to send menu option selected: ", e);
                        }
                    }
                });
            }
        });
        return view;
    }

    public void setMenuEvent(DisplayMenuRequestEvent event) {
        this.event = event;
    }
}
