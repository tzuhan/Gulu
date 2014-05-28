package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kantei on 2014/01/01.
 */
public class CurrentDrinkFragment extends Fragment {

    private static final String fragmentTag = CurrentDrinkFragment.class.getName();
    //private static final float emptyBottleThreshold = 10;
    private static final int[] ImagesId = { //R.drawable.d0,
                                            //R.drawable.d1,
                                            //R.drawable.d2,
                                            //R.drawable.d3,
                                            //R.drawable.d4,
                                            //R.drawable.d5,
                                            R.drawable.d6,
                                            R.drawable.d7};
                                            //R.drawable.d8};
    private static final int numOfImages = ImagesId.length;
    private int mToShowLabel;
    /*
    private float mTotalDeltaV;
    */
    private ImageView drinkImageView;
    private MainActivity mMainActivity;

    public static CurrentDrinkFragment newInstance(MainActivity mainActivity,int toShowLabel) {
        CurrentDrinkFragment fragment = new CurrentDrinkFragment();
        fragment.mToShowLabel = toShowLabel;
        /*
        fragment.mTotalDeltaV = totalDeltaV;
        */
        fragment.mMainActivity = mainActivity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(fragmentTag,"onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_current_drink,container,false);
        drinkImageView = (ImageView)rootView.findViewById(R.id.drinkImageView);
        TextView drinkTextView = (TextView)rootView.findViewById(R.id.drinkTextView);

        if(mToShowLabel < numOfImages) {
            drinkImageView.setImageResource(ImagesId[mToShowLabel]);
            drinkImageView.setDrawingCacheEnabled(true);
        }
        else {
            Log.d(fragmentTag,"set Image out of bound");
        }

        if(mToShowLabel < DrinksInformation.drinks_list.length) {
            drinkTextView.setText(DrinksInformation.drinks_list[mToShowLabel]);
        }
        else {
            drinkTextView.setText("unrecognized drink");
        }

        return rootView;

    }

}
