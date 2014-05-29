package com.example.android.effectivenavigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import com.example.android.effectivenavigation.HealthConditionInfo;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by kantei on 2014/05/25.
 */
public class HealthConditionFragment extends Fragment{
    private final static String conditionNameKey = "conditionName";
    private final static String intakesGoalKey = "intakesGoal";
    private final static String toShowKey = "toShow";
    private float[] defaultValue = new float[HealthConditionInfo.ingredients.length];
    private boolean[] toShow = new boolean[HealthConditionInfo.ingredients.length];
    private static final String TAG = "test";
    private MainActivity mainActivity = HealthConditionFragment.this.;

    public static HealthConditionFragment newInstance(HealthConditionInfo info) {
        HealthConditionFragment fragment = new HealthConditionFragment();
        Bundle args = new Bundle();
        args.putString(conditionNameKey, info.conditionName);
        args.putFloatArray(intakesGoalKey, info.intakesGoal);
        args.putBooleanArray(toShowKey, info.toShow);
        for(int i=0;i<HealthConditionInfo.ingredients.length;i++) {
            fragment.toShow[i] = info.toShow[i];
            fragment.defaultValue[i] = info.intakesGoal[i];

        }

        fragment.setArguments(args);

        return fragment;
    }


    int[] toPercentage(Float[] values, float[] base) {
        int numValues = values.length;
        int []turnIntoValues = new int[numValues];
        for(int i=0;i<numValues;i++) {
            turnIntoValues[i] = (int)(values[i].floatValue()/base[i]*100);
        }
        return turnIntoValues;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //save setting to sharedPreference
        SharedPreferences sp = getActivity().getSharedPreferences("settings.txt", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        JSONArray toShowArray = new JSONArray();
        for (boolean show : toShow) {
            toShowArray.put(show);
        }

        JSONArray defaultValueArray = new JSONArray();
        for (float value : defaultValue) {
            defaultValueArray.put(Float.valueOf(value));
        }

        editor.putString("toShow", toShowArray.toString());
        editor.putString("defaultValue", defaultValueArray.toString());
        editor.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_health_condition,container,false);
        ListView intakesList = (ListView) rootView.findViewById(R.id.intakesListView);
        ImageView prev = (ImageView) rootView.findViewById(R.id.back_to_goal_select);
        TextView conditionTitle = (TextView) rootView.findViewById(R.id.health_condition);
        conditionTitle.setText(getArguments().getString(conditionNameKey));

        int numIngredients = HealthConditionInfo.ingredients.length;

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.changeCurrentFragment(mainActivity.thirdTabIndex,GoalFeaturesListFragment.newInstance(mainActivity));
            }
        });

        //restore saved Intent
        SharedPreferences sp = getActivity().getSharedPreferences("settings.txt", Context.MODE_PRIVATE);

        try {


            JSONArray toShowArray = new JSONArray(sp.getString("toShow", "[\"true\", \"true\", \"true\". \"true\". \"true\"]"));
            JSONArray defaultValueArray = new JSONArray(sp.getString("defaultValue", "[]"));
            Log.d(TAG, sp.getString("toShow", "[\"true\", \"true\", \"true\". \"true\". \"true\"]"));


            if(toShowArray.length()!=0 || defaultValueArray.length()!=0) {
                for (int i = 0; i < toShowArray.length(); i++) {
                    toShow[i] = toShowArray.getBoolean(i);
                }

                for (int i = 0; i < defaultValueArray.length(); i++) {
                    defaultValue[i] = (float) defaultValueArray.getDouble(i);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }




        ArrayList<Float> defaultValuesToShow = new ArrayList<Float>();
        ArrayList<String> ingredientsNameToShow = new ArrayList<String>();

        for(int i = 0;i < numIngredients;i++) {
            if(toShow[i] == true) {
                defaultValuesToShow.add(defaultValue[i]);
                ingredientsNameToShow.add(HealthConditionInfo.ingredients[i]);
            }
        }
        String[] ingredientsNameToShowStringArray = new String[ingredientsNameToShow.size()];
        ingredientsNameToShowStringArray = ingredientsNameToShow.toArray(ingredientsNameToShowStringArray);

        Float[] defaultValuesToShowFloatArray = new Float[defaultValuesToShow.size()];
        defaultValuesToShowFloatArray = defaultValuesToShow.toArray(defaultValuesToShowFloatArray);

        final IntakesListArrayAdapter adapter = new IntakesListArrayAdapter(
                this.getActivity(),
                ingredientsNameToShowStringArray,
                toPercentage(defaultValuesToShowFloatArray,HealthConditionInfo.suggestionValues)
        );

        intakesList.setAdapter(adapter);

        /*
        intakesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
            }

        });
        */

        return rootView;
    }

    private class IntakesListArrayAdapter extends ArrayAdapter<String>{
        private final Context context;
        private String[] ingredientNames;
        private int[] defaultGoalPercentage;

        public IntakesListArrayAdapter(Context context, String[] ingredientNames, int[] defaultGoalPercentage) {
            super(context, R.layout.fragment_health_condition_intakes_row, ingredientNames);
            this.context = context;
            this.ingredientNames = ingredientNames;
            this.defaultGoalPercentage = defaultGoalPercentage;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.fragment_health_condition_intakes_row, parent, false);
            final int final_pos = position;
            TextView ingredientName = (TextView) rowView.findViewById(R.id.hc_ingredient_name);
            final TextView seekBarValue = (TextView) rowView.findViewById(R.id.hc_seekBarValue);
            ingredientName.setText(ingredientNames[position]);
            seekBarValue.setText(String.valueOf(HealthConditionFragment.this.defaultValue[position]));

            SeekBar goalBar = (SeekBar) rowView.findViewById(R.id.hc_goal_bar);
            goalBar.setProgress(defaultGoalPercentage[position]);
            //goalBar.setProgressDrawable();
            //goalBar.setThumb(writeOnDrawable(R.drawable.pin,"QQ"));
            goalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                @Override
                public void onProgressChanged(SeekBar goalBar, int progress, boolean fromUser){
                    seekBarValue.setText(String.valueOf(Math.round(HealthConditionInfo.suggestionValues[final_pos] * progress)/100f));
                    //save to defaultValue
                    HealthConditionFragment.this.defaultValue[final_pos] = HealthConditionInfo.suggestionValues[final_pos] * progress/100f;
                    defaultGoalPercentage[final_pos] = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar goalBar){
                    //
                }
                public void onStopTrackingTouch(SeekBar goalBar){

                }

            });

            return rowView;
        }

        public BitmapDrawable writeOnDrawable(int drawableId, String text){

            Resources resources = getResources();
            Bitmap bm = BitmapFactory.decodeResource(resources, drawableId).copy(Bitmap.Config.ARGB_8888, true);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setTextSize(20);

            Canvas canvas = new Canvas(bm);
            canvas.drawText(text, 0, bm.getHeight()/2, paint);

            return new BitmapDrawable(resources, bm);
        }

    }
}
