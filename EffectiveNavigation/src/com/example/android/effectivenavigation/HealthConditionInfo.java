package com.example.android.effectivenavigation;

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
    public final static float increasePercentage = 0.3f;
    public final static float[] suggestionValues = {
            1500f,
            2500f,
            90f,
            400f,
            2.3f,
    };
    public final static float[] maxLimits = {
            (suggestionValues[0]*(1+increasePercentage)),
            (suggestionValues[1]*(1+increasePercentage)),
            (suggestionValues[2]*(1+increasePercentage)),
            (suggestionValues[3]*(1+increasePercentage)),
            (suggestionValues[4]*(1+increasePercentage))
    };

    public String conditionName;
    public float[] intakesGoal = new float[ingredients.length];
    public boolean[] toShow = new boolean[ingredients.length];

}
