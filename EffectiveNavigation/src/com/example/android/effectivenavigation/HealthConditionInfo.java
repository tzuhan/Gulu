package com.example.android.effectivenavigation;

import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kantei on 2014/05/25.
 */
public class HealthConditionInfo {
    public final static String[] ingredients = {
        "Water",
        "Calories",
        "Sugar",
        "Caffeine",
        "Sodium",
    };
    public static final Map<String, String> unit;
    static {
        Map<String, String> tempMap = new HashMap<String, String>();
        tempMap.put("Water", "ml");
        tempMap.put("Calories", "Kcal");
        tempMap.put("Sugar","g");
        tempMap.put("Caffeine","mg");
        tempMap.put("Sodium","mg");
        unit = Collections.unmodifiableMap(tempMap);

    };
    public final static float increasePercentage = 0.1f;
    public final static float[] suggestionValues = {
        3274f,
        2800f,
        150f,
        400f,
        4300f
    };
    public final static float[] maxLimits = {
        (suggestionValues[0]*(1+increasePercentage)),
        (suggestionValues[1]*(1+increasePercentage)),
        (suggestionValues[2]*(1+increasePercentage)),
        (suggestionValues[3]*(1+increasePercentage)),
        (suggestionValues[4]*(1+increasePercentage))
    };

    public final static float data [][]  = {
        {
            //regular mode
            2000f,
            2500f,
            90f,
            350f,
            3200f
        },
        {
            //athlete mode, one hour workout
            3274f,
            2800f,
            150f,
            400f,
            3700f
        },
        {
            //pregnancy
            2500f,
            2200f,
            60f,
            200f,
            2300f
        },
        {
            //diabetes
            2000f,
            2500f,
            60f,
            350f,
            2300f
        },
        {
            //renal
            2000f,
            2500f,
            90f,
            350f,
            1500f
        },
        {
            //hypertension
            2000f,
            2500f,
            90f,
            350f,
            1500f
        }
    };

    public String conditionName;
    public float[] intakesGoal = new float[ingredients.length];
    public boolean[] toShow = new boolean[ingredients.length];


    public HealthConditionInfo(){
        //send back regular mode
        //set all ingredient visible

    }

    public HealthConditionInfo(int position, String name){
        //reset suggestionValues
        for(int i = 0; i< ingredients.length; i++) {
            intakesGoal[i] = data[position][i];
            toShow[i] = true;
        }
        conditionName = name;

    }

}
