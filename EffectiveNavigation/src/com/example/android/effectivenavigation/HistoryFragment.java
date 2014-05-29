package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by ggm on 5/30/14.
 */
public class HistoryFragment extends Fragment {

    public static final int[] ids = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5, R.id.button6};

    private Button[] dayButton = new Button[7];

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history_main, container, false);
        for (int i = 0; i < 7; i++) {
            dayButton[i] = (Button) rootView.findViewById(ids[i]);
            dayButton[i].setOnClickListener(dayClickListener);
        }

        return rootView;
    }

    private OnClickListener dayClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            Button button = (Button) view;
            for (int i = 0; i < 7; i ++) {
                if (ids[i] == viewId) {
                    dayButton[i].setBackgroundResource(R.drawable.day_circle_blue);
                } else {
                    dayButton[i].setBackgroundResource(R.drawable.day_circle_grey);
                }
            }
        }
    };

}
