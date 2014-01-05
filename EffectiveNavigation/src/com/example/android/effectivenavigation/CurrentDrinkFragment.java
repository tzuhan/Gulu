package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by kantei on 2014/01/01.
 */
public class CurrentDrinkFragment extends Fragment {

    //private static final String argsPredictedLabelTag = "predictedLabel";
    //private static final String argsPreviousLabelTag = "predictedLabel";
    private static final String fragmentTag = CurrentDrinkFragment.class.getName();
    private static final float emptyBottleThreshold = 10;
    private static final int[] ImagesId = {R.drawable.cola};
    private static final int numOfImages = ImagesId.length;
    private int mPredictedLabel;
    private float mCurrentWeight;
    private float mTotalDeltaWeight;

    public static CurrentDrinkFragment newInstance(int predictedLabel,float currentWeight,float totalDeltaWeight) {
        CurrentDrinkFragment fragment = new CurrentDrinkFragment();
        fragment.mPredictedLabel = predictedLabel;
        fragment.mCurrentWeight = currentWeight;
        fragment.mTotalDeltaWeight = totalDeltaWeight;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(fragmentTag,"onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_current_drink,container,false);
        ImageView drinkImageView = (ImageView)rootView.findViewById(R.id.drinkImageView);
        TextView drinkTextView = (TextView)rootView.findViewById(R.id.drinkTextView);

        //put a picture, drink name, current weight
        if(this.mCurrentWeight > emptyBottleThreshold) {
            if(mPredictedLabel < numOfImages) {
                drinkImageView.setImageResource(ImagesId[mPredictedLabel]);
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(),"set Image out of bound",Toast.LENGTH_SHORT);
                Log.d(fragmentTag,"set Image out of bound");
            }

            if(Math.abs(this.mTotalDeltaWeight - 0) > 1e-6) {
                Toast.makeText(getActivity().getApplicationContext(),"You have drunk " + mTotalDeltaWeight + " ml",Toast.LENGTH_LONG);
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(),"You are drinking a new kind of drink",Toast.LENGTH_LONG);
            }
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(),"No drink in bottle now",Toast.LENGTH_LONG);
        }

        return rootView;
        //return super.onCreateView(inflater, container, savedInstanceState);

    }

}
