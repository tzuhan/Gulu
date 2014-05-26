package com.example.android.effectivenavigation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import database.DBHelper;
import database.DrinkRecord;
import database.DrinkRecordDataSource;

/**
 * Created by kantei on 2014/01/01.
 */
public class DayDrinkFragment extends Fragment {

    private static final String fragmentTag = DayDrinkFragment.class.getName();
    private int mWeekDay = -1;
    private List<DrinkRecord> records = null;
    private DrinkRecordDataSource mSource = null;
    private DrinkRecordAdapter mAdapter;
    private static final String weekArgKey = "weekDay";

    private Context mContext;
    public static DayDrinkFragment newInstance(int weekDay) {
        DayDrinkFragment fragment = new DayDrinkFragment();
        Bundle args = new Bundle();
        args.putInt(weekArgKey,weekDay);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeekDay = getArguments().getInt(weekArgKey);
        try {
            records = mSource.getWeekDayRecords(mWeekDay);
        }
        catch(Exception e) {
            records = null;
            Log.d(fragmentTag,Log.getStackTraceString(e));
        }
        mContext = getActivity().getApplicationContext();
        mSource = new DrinkRecordDataSource(mContext);
        try{
            mSource.openDB();
        }
        catch (Exception e) {
            Log.d(fragmentTag,Log.getStackTraceString(e));
        }

    }

    private class DrinkRecordAdapter extends ArrayAdapter<DrinkRecord>{
        private final Context context;
        private final DrinkRecord[] drinkRecords;

        public DrinkRecordAdapter(Context context, DrinkRecord[] drinkRecords) {
            super(context, R.layout.fragment_health_condition_intakes_row, drinkRecords);
            this.context = context;
            this.drinkRecords = drinkRecords;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.fragment_day_drink_row, parent, false);

            TextView drinkName = (TextView) rowView.findViewById(R.id.list_drink_text);
            drinkName.setText(drinkRecords[position].getDrink());

            TextView endTime = (TextView) rowView.findViewById(R.id.list_end_time_text);
            endTime.setText(drinkRecords[position].getEndTime());

            TextView volume = (TextView) rowView.findViewById(R.id.list_volume_text);
            volume.setText(new Float(drinkRecords[position].getVolume()).toString());

            return rowView;
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_day_drink, container, false);
        if(records != null) {
            ((ListView) rootView.findViewById(R.id.day_drink_listView)).setAdapter(new DrinkRecordAdapter(this.getActivity(), (DrinkRecord[]) records.toArray()));
        }
        return rootView;
    }

}
