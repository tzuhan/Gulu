package com.example.android.effectivenavigation;

/**
 * Created by kantei on 2014/01/05.
 */
public class DrinksInformation {

    public static final int[] ImagesId = {R.drawable.cola};
    public static final int numOfImages = ImagesId.length;
    public static final String[] drinks_list = {"Black-Tea",
                                                "Green-Tea",
                                                "Milk-Tea",
                                                "Orange-Juice",
                                                "Vegetable-Juice",
                                                "Orange-Soda",
                                                "Coffee",
                                                "Cola",
                                                "Grape-Juice"};
    public static final int NUM_DATA_VALUES = 7;
    public static final int NUM_FEATURE_VALUES = NUM_DATA_VALUES - 1; //exclude force sensor value
}
