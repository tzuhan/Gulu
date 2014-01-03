package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by kantei on 2014/01/01.
 */
public class WeekListFragment extends ListFragment {

    static final String[] weekDays = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, weekDays));
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week_list, container, false);
    }

    public void onListItemClick(ListView parent, View v,
                                int position, long id)
    {
        /*
        Toast.makeText(getActivity(),
                "You have selected " + weekDays[position],
                Toast.LENGTH_SHORT).show();
        */

        //may be start an Activity to show

    }




}
