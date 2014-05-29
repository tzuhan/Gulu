package com.example.android.effectivenavigation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import com.example.android.effectivenavigation.HealthConditionInfo;

/**
 * Created by kantei on 2014/05/25.
 */
public class HealthConditionFragment extends Fragment{
    private final static String conditionNameKey = "conditionName";
    private final static String intakesGoalKey = "intakesGoal";
    private final static String toShowKey = "toShow";
    public float[] defaultValue = new float[HealthConditionInfo.ingredients.length];
    public boolean[] toShow = new boolean[HealthConditionInfo.ingredients.length];
    public static HealthConditionFragment newInstance(HealthConditionInfo info) {
        HealthConditionFragment fragment = new HealthConditionFragment();
        Bundle args = new Bundle();
        args.putString(conditionNameKey, info.conditionName);
        args.putFloatArray(intakesGoalKey, info.intakesGoal);
        args.putBooleanArray(toShowKey, info.toShow);
        for(int i=0;i<HealthConditionInfo.ingredients.length;i++)
            fragment.defaultValue[i] = info.intakesGoal[i];

        fragment.setArguments(args);

        return fragment;
    }
    private Bundle savedState = null;


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
        defaultValue = savedState.getFloatArray("ingredientsValue");
        toShow = savedState.getBooleanArray("ingredientsToShow");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_health_condition,container,false);
        TextView conditionTitle = (TextView) rootView.findViewById(R.id.health_condition);
        ListView intakesList = (ListView) rootView.findViewById(R.id.intakesListView);
        conditionTitle.setText(getArguments().getString(conditionNameKey));

        int numIngredients = HealthConditionInfo.ingredients.length;

        //restore saved Intent
        if(savedState == null){
            //get defaultValue first
            defaultValue = getArguments().getFloatArray(intakesGoalKey);
            toShow = getArguments().getBooleanArray(toShowKey);

            //save default value into bundle
            savedState = new Bundle();
            savedState.putBooleanArray("ingredientsToShow", toShow);
            savedState.putFloatArray("ingredientsValue", defaultValue);
        }
        else if(savedState != null){
            //restore pre-stored bundle back
            defaultValue = savedState.getFloatArray("ingredientsValue");
            toShow = savedState.getBooleanArray("ingredientsToShow");

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
