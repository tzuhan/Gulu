package com.example.android.effectivenavigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
            R.drawable.orange_juice,
            R.drawable.coffee};
    //R.drawable.d8};

    private static final int noDrinkId = R.drawable.no_drink;

    private static final int numOfImages = ImagesId.length;
    public int mToShowLabel;
    public float mTotalVolume;
    public boolean mIsLoading = false;

    //private ImageView mDrinkImage;

    private MainActivity mMainActivity;

    public static CurrentDrinkFragment newInstance(MainActivity mainActivity, int toShowLabel, float totalVolume) {
        CurrentDrinkFragment fragment = new CurrentDrinkFragment();
        fragment.mToShowLabel = toShowLabel;

        fragment.mTotalVolume = totalVolume;

        fragment.mMainActivity = mainActivity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(fragmentTag, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_current_drink2, container, false);
        ImageView mDrinkImage = (ImageView) rootView.findViewById(R.id.drinkImageView);
        TextView drankVolumeText = (TextView) rootView.findViewById(R.id.drank_volume);
        final Button detectDrinkButton = (Button) rootView.findViewById(R.id.detectDrinkButton);
        final ProgressBar dataLoadingProgressBar = (ProgressBar)rootView.findViewById(R.id.dataLoadingProgressBar);

        if(!mIsLoading) {
            dataLoadingProgressBar.setVisibility(View.GONE);
        }

        if (mToShowLabel == -1) { //no drink
            mDrinkImage.setImageResource(noDrinkId);
            mDrinkImage.setDrawingCacheEnabled(true);

            drankVolumeText.setVisibility(View.INVISIBLE);

            mMainActivity.timeToDetectDrink = false;
            detectDrinkButton.setVisibility(View.VISIBLE);
            detectDrinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainActivity.timeToDetectDrink = true;
                    dataLoadingProgressBar.setVisibility(View.VISIBLE);
                    mIsLoading = true;
                    detectDrinkButton.setVisibility(View.GONE);

                    if(mMainActivity.automaticMode) {
                        mMainActivity.enableBTandStartToDiscover();
                    }
                }
            });
        } else {
            if (mToShowLabel < numOfImages) {
                mDrinkImage.setImageResource(ImagesId[mToShowLabel]);
                mDrinkImage.setDrawingCacheEnabled(true);
            } else {
                Log.d(fragmentTag, "set Image out of bound");
            }

            drankVolumeText.setVisibility(View.VISIBLE);
            drankVolumeText.setText(((int)mTotalVolume) + " ml");

            detectDrinkButton.setVisibility(View.GONE);

        }

        return rootView;

    }

}
