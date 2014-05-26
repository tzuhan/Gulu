package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by kantei on 2014/01/01.
 */
public class GoalFeaturesListFragment extends ListFragment {

    static final String[] features = {
            "Regular Mode",
            "Fitness Mode",
            "Pregnancy",
            "Diabetes",
            "Chronic and disease",
            "Hypertension",
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, features));
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goal_features_list, container, false);
    }

    public void onListItemClick(ListView parent, View v,
                                int position, long id) {
        /*
        Toast.makeText(getActivity(),
                "You have selected " + features[position],
                Toast.LENGTH_SHORT).show();
        */


        //may be start an Activity to show

    }




}
