package com.example.android.effectivenavigation;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import java.util.List;

import database.DBHelper;
import database.DrinkRecord;
import database.DrinkRecordDataSource;

/**
 * Created by kantei on 2014/01/01.
 */
public class DayDrinkFragment extends ListFragment {

    private static final String fragmentTag = DayDrinkFragment.class.getName();
    private int mWeekDay;
    private List<DrinkRecord> records = null;
    private DrinkRecordDataSource mSource = null;
    private SimpleCursorAdapter mAdapter;
    private static final int[] mLayoutIds = {R.id.list_id_text,
                                             R.id.list_drink_text,
                                             R.id.list_week_day_text,
                                             R.id.list_end_time_text,
                                             R.id.list_volume_text};
    private Context mContext;
    public static DayDrinkFragment newInstance(int weekDay) {
        DayDrinkFragment fragment = new DayDrinkFragment(weekDay);
        return fragment;
    }

    private DayDrinkFragment(int weekDay) {
        mWeekDay = weekDay;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //records = mSource.getWeekDayRecords(mWeekDay);
        mContext = getActivity().getApplicationContext();
        mSource = new DrinkRecordDataSource(mContext);
        try{
            mSource.openDB();
        }
        catch (Exception e) {
            Log.d(fragmentTag,Log.getStackTraceString(e));
        }
        mAdapter = new SimpleCursorAdapter(mContext ,
                R.layout.fragment_day_drink_row ,
                mSource.getWeekDayCursor(mWeekDay) ,
                DBHelper.allColumns,
                mLayoutIds ,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        setListAdapter(mAdapter);

    }

    /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_day_drink, container, false);
        ((ListView)rootView.findViewById(R.id.day_drink_listView)).setAdapter(mAdapter);
        return rootView;
    }
    */
}
