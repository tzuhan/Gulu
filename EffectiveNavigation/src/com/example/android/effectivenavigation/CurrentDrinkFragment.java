package com.example.android.effectivenavigation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LruCache;
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

    private static final String fragmentTag = CurrentDrinkFragment.class.getName();
    private static final float emptyBottleThreshold = 10;
    private static final int[] ImagesId = { R.drawable.d0,
                                            R.drawable.d1,
                                            R.drawable.d2,
                                            R.drawable.d3,
                                            R.drawable.d4,
                                            R.drawable.d5,
                                            R.drawable.d6,
                                            R.drawable.d7,
                                            R.drawable.d8};
    private static final int numOfImages = ImagesId.length;
    private int mPredictedLabel;
    private float mCurrentWeight;
    private float mTotalDeltaWeight;
    private ImageView drinkImageView;
    private MainActivity mMainActivity;
    /*
    private enum UIEventId {
        PutImage,
        NoEvent
    }

    private UIEventId uiEventId = UIEventId.NoEvent;

    private void addBitmapToImageCache(int key,Bitmap bitmap) {
        if(mMainActivity.mImageCache.get(key) == null) {
            mMainActivity.mImageCache.put(key,bitmap);
        }
    }

    private Bitmap getBitmapFromImageCache(int key) {
        return mMainActivity.mImageCache.get(key);
    }

    private void loadImage(int id,ImageView imageView) {
        final Bitmap bitmap = getBitmapFromImageCache(id);
        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        else {
            //do asynchronous loading;
            mMainActivity.mIOThreadHandler.post(loadImageAsynchronously);
            //and restart this function in UI thread
        }
    }

    private Runnable loadImageAsynchronously = new Runnable() {
        @Override
        public void run() {
            Bitmap bitmap = BitmapFactory.decodeResource(mMainActivity.getResources(),ImagesId[mPredictedLabel]);
            mMainActivity.mImageCache.put(ImagesId[mPredictedLabel], bitmap);
            uiEventId = UIEventId.PutImage;
            mMainActivity.getUIHandler().post(UIEvent);
        }
    };

    private Runnable UIEvent = new Runnable() {
        @Override
        public void run() {
            if(uiEventId == UIEventId.PutImage) {
                loadImage(ImagesId[mPredictedLabel],drinkImageView);
            }
        }
    };
    */

    public static CurrentDrinkFragment newInstance(MainActivity mainActivity,int predictedLabel,float currentWeight,float totalDeltaWeight) {
        CurrentDrinkFragment fragment = new CurrentDrinkFragment();
        fragment.mPredictedLabel = predictedLabel;
        fragment.mCurrentWeight = currentWeight;
        fragment.mTotalDeltaWeight = totalDeltaWeight;
        fragment.mMainActivity = mainActivity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(fragmentTag,"onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_current_drink,container,false);
        drinkImageView = (ImageView)rootView.findViewById(R.id.drinkImageView);
        TextView drinkTextView = (TextView)rootView.findViewById(R.id.drinkTextView);

        if(mPredictedLabel < numOfImages) {
            //loadImage(ImagesId[mPredictedLabel],drinkImageView);
            drinkImageView.setImageResource(ImagesId[mPredictedLabel]);
            drinkImageView.setDrawingCacheEnabled(true);
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(),"set Image out of bound",Toast.LENGTH_SHORT);
            Log.d(fragmentTag,"set Image out of bound");
        }

        if(mPredictedLabel < DrinksInformation.drinks_list.length) {
            drinkTextView.setText(DrinksInformation.drinks_list[mPredictedLabel]);
        }

        if(Math.abs(this.mTotalDeltaWeight - 0) > 1e-6) {
            Toast.makeText(getActivity().getApplicationContext(),"You have drunk " + mTotalDeltaWeight + " ml",Toast.LENGTH_LONG);
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(),"You are drinking a new kind of drink",Toast.LENGTH_LONG);
        }

        return rootView;
        //return super.onCreateView(inflater, container, savedInstanceState);

    }

}
