package com.example.android.effectivenavigation;

import android.content.Context;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by kantei on 2014/01/01.
 */
public class CurrentDrinkFragment extends Fragment {

    private static final String fragmentTag = CurrentDrinkFragment.class.getName();
    //private static final float emptyBottleThreshold = 10;

    private JSONArray drinksIngredient = null;
    private SharedPreferences sp = null;

    private static final int[] ImagesId = {
            R.drawable.black_tea,
            R.drawable.green_tea,
            R.drawable.milk_tea,
            R.drawable.orange_juice,
            R.drawable.vegetable_juice,
            R.drawable.fanta,
            R.drawable.coffee,
            R.drawable.cola
    };

    private static final int[] smallAmountImageIds = {
            R.drawable.water0,
            R.drawable.calories0,
            R.drawable.suger0,
            R.drawable.caffeine0,
            R.drawable.sodium0,
    };

    private static final int[] mediumAmountImageIds = {
            R.drawable.water1,
            R.drawable.calories1,
            R.drawable.suger1,
            R.drawable.caffeine1,
            R.drawable.sodium1

    };

    private static final int[] largeAmountImageIds = {
            R.drawable.water2,
            R.drawable.calories2,
            R.drawable.suger2,
            R.drawable.caffeine2,
            R.drawable.sodium2,
    };

    private static final int caloriesIndex = 0; //in ImageView ,TextView ,ImageIds
    private static final int sugarsIndex = 1;
    private static final int caffeineIndex = 2;
    private static final int sodiumIndex = 3;
    private static final int waterIndex = 4;

    private static final double lowToMedThresPercetage = 0.7;
    private static final double MedToHighThresPercetage = 0.7;

    /*
    private static final int waterIndexInHealthInfo = 0;
    private static final int caloriesIndexInHealthInfo = 1;
    private static final int sugarIndexInHealthInfo = 2;
    private static final int caffIndexInHealthInfo = 3;
    private static final int sodiumIndexInHealthInfo = 4;
    */
    private static final int[] indicesInImageIds = {
            waterIndex,
            caloriesIndex,
            sugarsIndex,
            caffeineIndex,
            sodiumIndex
    };

    private static final String caloriesField = "calories";
    private static final String sugarsField = "sugars";
    private static final String caffeineField = "caffeine";
    private static final String sodiumField = "sodium";
    private static final String waterField = "water";

    private static final int noDrinkId = R.drawable.no_drink;

    private static final int numOfImages = ImagesId.length;

    public int mToShowLabel;
    public float mTotalVolume;
    public boolean mIsLoading = false;

    //private ImageView mDrinkImage;

    private MainActivity mMainActivity;
    ImageView[] ingredientImageViews;
    TextView[] ingredientTextViews;
    int numIngredients;
    int numDrinks = DrinksInformation.drinks_list.length;

    public static CurrentDrinkFragment newInstance(MainActivity mainActivity, int toShowLabel, float totalVolume) {
        CurrentDrinkFragment fragment = new CurrentDrinkFragment();
        fragment.mToShowLabel = toShowLabel;

        fragment.mTotalVolume = totalVolume;

        fragment.mMainActivity = mainActivity;
        return fragment;
    }

    final int toShow = 0;
    final int toHide = 1;

    private void hideOrShow(int flag) {
        if(flag == toHide) {
            for(int i=0;i<numIngredients;i++) {
                ingredientImageViews[i].setVisibility(View.INVISIBLE);
                ingredientTextViews[i].setVisibility(View.INVISIBLE);
            }
        }
        else if (flag == toShow) {
            for(int i=0;i<numIngredients;i++) {
                ingredientImageViews[i].setVisibility(View.VISIBLE);
                ingredientTextViews[i].setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(drinksIngredient == null) {
            try {
                drinksIngredient = new JSONArray(DrinksInformation.drinksIngredientsJSONStr);
            } catch(Exception e) {
                Log.d(fragmentTag,e.getLocalizedMessage());
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_current_drink2, container, false);
        ImageView mDrinkImage = (ImageView) rootView.findViewById(R.id.drinkImageView);
        ingredientImageViews = new ImageView[]{
                (ImageView) rootView.findViewById(R.id.CaloriesImageView),
                (ImageView) rootView.findViewById(R.id.SugarImageView),
                (ImageView) rootView.findViewById(R.id.CaffImageView),
                (ImageView) rootView.findViewById(R.id.SodiumImageView),
                (ImageView) rootView.findViewById(R.id.WaterImageView)
        };

        ingredientTextViews = new TextView[]{
                (TextView) rootView.findViewById(R.id.CaloriesTextView),
                (TextView) rootView.findViewById(R.id.SugarTextView),
                (TextView) rootView.findViewById(R.id.CaffTextView),
                (TextView) rootView.findViewById(R.id.SodiumTextView),
                (TextView) rootView.findViewById(R.id.WaterTextView)
        };

        numIngredients = ingredientImageViews.length;

        TextView drankVolumeText = (TextView) rootView.findViewById(R.id.drank_volume);
        final Button detectDrinkButton = (Button) rootView.findViewById(R.id.detectDrinkButton);
        final ProgressBar dataLoadingProgressBar = (ProgressBar)rootView.findViewById(R.id.dataLoadingProgressBar);

        if(!mIsLoading) {
            dataLoadingProgressBar.setVisibility(View.GONE);
        }
        if(mMainActivity.automaticMode) {
            if (mTotalVolume == 0) {
                mToShowLabel = -1;
            }
        }

        if (mToShowLabel == -1) { //no drink
            mDrinkImage.setImageResource(noDrinkId);
            mDrinkImage.setDrawingCacheEnabled(true);

            drankVolumeText.setVisibility(View.INVISIBLE);

            mMainActivity.timeToDetectDrink = false;
            detectDrinkButton.setVisibility(View.VISIBLE);
            hideOrShow(toHide); //hide ingredients' textView and imageView

            detectDrinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainActivity.timeToDetectDrink = true;
                    dataLoadingProgressBar.setVisibility(View.VISIBLE);
                    mIsLoading = true;
                    detectDrinkButton.setVisibility(View.GONE);

                    if(mMainActivity.automaticMode) {
                        mMainActivity.callTempThreadForHelp(mMainActivity.toEnableBTAndStartToDiscover);
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
            hideOrShow(toShow);

            //get ingredients
            if(mToShowLabel < numDrinks) {
                try {
                    JSONObject singleDrinkIngredients = drinksIngredient.getJSONObject(mToShowLabel);
                    double[] ingredientValues = {
                            (singleDrinkIngredients.getDouble(waterField) * mTotalVolume / 100),
                            (singleDrinkIngredients.getDouble(caloriesField) * mTotalVolume / 100),
                            (singleDrinkIngredients.getDouble(sugarsField) * mTotalVolume / 100),
                            (singleDrinkIngredients.getDouble(caffeineField) * mTotalVolume / 100),
                            (singleDrinkIngredients.getDouble(sodiumField) * mTotalVolume / 100)
                    };

                    for(int i = 0;i < numIngredients;i++) {
                        ingredientTextViews[indicesInImageIds[i]].setText(String.valueOf((int)ingredientValues[i]));
                    }

                    sp = getActivity().getSharedPreferences(GoalFeaturesListFragment.features[0]+"settings.txt", Context.MODE_PRIVATE);
                    String defaultValueJSONStr = sp.getString("defaultValue", "");
                    if(defaultValueJSONStr.length() > 0) {
                        JSONArray defaultValueArray = new JSONArray(defaultValueJSONStr);
                        for(int i = 0;i < numIngredients;i++) {
                            double ratio = ingredientValues[i]/defaultValueArray.getDouble(i);
                            if(ratio <= lowToMedThresPercetage) {
                                ingredientImageViews[indicesInImageIds[i]].setImageResource(smallAmountImageIds[i]);
                            }
                            else if(ratio <= MedToHighThresPercetage) {
                                ingredientImageViews[indicesInImageIds[i]].setImageResource(mediumAmountImageIds[i]);
                            }
                            else {
                                ingredientImageViews[indicesInImageIds[i]].setImageResource(largeAmountImageIds[i]);
                            }

                        }
                    }
                    else {
                        for(int i = 0;i < numIngredients;i++) {
                            double ratio = ingredientValues[i]/HealthConditionInfo.maxLimits[i];
                            if(ratio <= lowToMedThresPercetage) {
                                ingredientImageViews[indicesInImageIds[i]].setImageResource(smallAmountImageIds[i]);
                            }
                            else if(ratio <= MedToHighThresPercetage) {
                                ingredientImageViews[indicesInImageIds[i]].setImageResource(mediumAmountImageIds[i]);
                            }
                            else {
                                ingredientImageViews[indicesInImageIds[i]].setImageResource(largeAmountImageIds[i]);
                            }

                        }

                    }
                }
                catch (Exception e) {
                    Log.d(fragmentTag,e.getLocalizedMessage());
                }


            }

        }

        return rootView;

    }

}
