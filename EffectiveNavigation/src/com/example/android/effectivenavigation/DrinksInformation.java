package com.example.android.effectivenavigation;

import org.json.JSONArray;

/**
 * Created by kantei on 2014/01/05.
 */
public class DrinksInformation {

    public static final String[] drinks_list = {
                                                "Black-Tea",
                                                "Green-Tea",
                                                "Milk-Tea",
                                                "Orange-Juice",
                                                "Vegetable-Juice",
                                                "Fanta",
                                                "Coffee",
                                                "Cola"
                                                };
                                                //"Grape-Juice"};
    public static final int numOfDrinks = drinks_list.length;
    public static final int NUM_DATA_VALUES = 4;
    public static final int NUM_FEATURE_VALUES = NUM_DATA_VALUES - 1; //exclude force sensor value

    public static final String drinksIngredientsJSONStr = "[ {" +
            "        \"category\": \"Black-Tea\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"36.8\"," +
            "        \"sugars\":   \"9.2\"," +
            "        \"protein\":  \"0.0\"," +
            "        \"fat\":      \"0.0\"," +
            "        \"caffeine\": \"20.0\"," +
            "        \"sodium\":   \"10.0\"" +
            "}" +
            "," +
            "{" +
            "        \"category\": \"Green-Tea\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"27.2\"," +
            "        \"sugars\":   \"6.8\"," +
            "        \"protein\":  \"0.0\"," +
            "        \"fat\":      \"0.0\"," +
            "        \"caffeine\": \"20.0\"," +
            "        \"sodium\":   \"11.0\"" +
            "}" +
            "," +
            "{" +
            "        \"category\": \"Milk-Tea\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"36.6\"," +
            "        \"sugars\":   \"8.3\"," +
            "        \"protein\":  \"0.4\"," +
            "        \"fat\":      \"0.2\"," +
            "        \"caffeine\": \"20.0\"," +
            "        \"sodium\":   \"11.0\"" +
            "}" +
            "," +
            "{" +
            "        \"category\": \"Orange-Juice\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"47.32\"," +
            "        \"sugars\":   \"10.68\"," +
            "        \"protein\":  \"0.61\"," +
            "        \"fat\":      \"0.0\"," +
            "        \"caffeine\": \"0.0\"," +
            "        \"sodium\":   \"4.76\"" +
            "}" +
            "," +
            "{" +
            "        \"category\": \"Vegetable-Juice\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"37.2\"," +
            "        \"sugars\":   \"9.3\"," +
            "        \"protein\":  \"0.0\"," +
            "        \"fat\":      \"0.0\"," +
            "        \"caffeine\": \"0.0\"," +
            "        \"sodium\":   \"30.0\"" +
            "}" +
            "," +
            "{" +
            "        \"category\": \"Fanta\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"48.09\"," +
            "        \"sugars\":   \"11.79\"," +
            "        \"protein\":  \"0.0\"," +
            "        \"fat\":      \"0.0\"," +
            "        \"caffeine\": \"0.0\"," +
            "        \"sodium\":   \"4.0\"" +
            "}" +
            "," +
            "{" +
            "        \"category\": \"Coffee\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"4.0\"," +
            "        \"sugars\":   \"0.0\"," +
            "        \"protein\":  \"0.3\"," +
            "        \"fat\":      \"0.0\"," +
            "        \"caffeine\": \"135.0\"," +
            "        \"sodium\":   \"0.2\"" +
            "}" +
            "," +
            "{" +
            "        \"category\": \"Cola\"," +
            "        \"water\":    \"100.0\"," +
            "        \"calories\": \"42.13\"," +
            "        \"sugars\":   \"10.6\", " +
            "        \"protein\":  \"0.0\", " +
            "        \"fat\":      \"0.0\"," +
            "        \"caffeine\": \"9.58\"," +
            "        \"sodium\":   \"5.96\"" +
            "} ]";

}
