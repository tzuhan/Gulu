package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Random;

/**
 * Created by ggm on 5/30/14.
 */
public class HistoryFragment extends Fragment {

    public static final int[] dayButtonIds = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5, R.id.button6};

    public static final int[] liquidIds = new int[]{R.id.liquid1, R.id.liquid2, R.id.liquid3};

    private Button[] dayButton = new Button[dayButtonIds.length];
    private LinearLayout summaryBars;

    private View[] liquids = new View[liquidIds.length];

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
            dayButton[i] = (Button) rootView.findViewById(dayButtonIds[i]);
            dayButton[i].setOnClickListener(dayClickListener);
        }

        for (int i = 0; i < liquidIds.length; i++) {
            liquids[i] = rootView.findViewById(liquidIds[i]);
        }

        summaryBars = (LinearLayout) rootView.findViewById(R.id.summary);
        genSummary();
        genLiquids();
        return rootView;
    }

    private void genSummary() {
        summaryBars.removeAllViews();
        Random r = new Random();
        for (int i = 0; i < 24; i++) {
            View view = new View(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, r.nextInt(100));
            params.gravity = Gravity.BOTTOM;
            params.weight = 1;
            params.setMargins(0, 0, 10, 0);
            view.setLayoutParams(params);
            view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            summaryBars.addView(view);
        }
    }

    private void genLiquids() {
        Random r = new Random();
        for (int i = 0; i < liquids.length; i++) {
            ViewGroup.LayoutParams params = liquids[i].getLayoutParams();
            params.width = 50 + r.nextInt(150);
        }

    }

    private OnClickListener dayClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            Button button = (Button) view;
            genSummary();
            genLiquids();
            for (int i = 0; i < 7; i++) {
                if (dayButtonIds[i] == viewId) {
                    dayButton[i].setBackgroundResource(R.drawable.day_circle_blue);
                } else {
                    dayButton[i].setBackgroundResource(R.drawable.day_circle_grey);
                }
            }
        }
    };

}
