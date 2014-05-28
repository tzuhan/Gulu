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


    public static HealthConditionFragment newInstance(HealthConditionInfo info) {
        HealthConditionFragment fragment = new HealthConditionFragment();
        Bundle args = new Bundle();
        args.putString(conditionNameKey, info.conditionName);
        args.putFloatArray(intakesGoalKey, info.intakesGoal);
        args.putBooleanArray(toShowKey, info.toShow);

        fragment.setArguments(args);

        return fragment;
    }

    int[] toPercentage(Float[] values,float base) {
        int numValues = values.length;
        int []turnIntoValues = new int[numValues];
        for(int i=0;i<numValues;i++) {
            turnIntoValues[i] = (int)(values[i].floatValue()/base*100);
        }
        return turnIntoValues;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_health_condition,container,false);
        TextView conditionTitle = (TextView) rootView.findViewById(R.id.health_condition);
        ListView intakesList = (ListView) rootView.findViewById(R.id.intakesListView);

        conditionTitle.setText(getArguments().getString(conditionNameKey));

        int numIngredients = HealthConditionInfo.ingredients.length;
        float[] defaultValues = getArguments().getFloatArray(intakesGoalKey);
        boolean[] toShow = getArguments().getBooleanArray(toShowKey);

        ArrayList<Float> defaultValuesToShow = new ArrayList<Float>();
        ArrayList<String> ingredientsNameToShow = new ArrayList<String>();

        for(int i = 0;i < numIngredients;i++) {
            if(toShow[i] == true) {
                defaultValuesToShow.add(defaultValues[i]);
                ingredientsNameToShow.add(HealthConditionInfo.ingredients[i]);
            }
        }

        final IntakesListArrayAdapter adapter = new IntakesListArrayAdapter(
                this.getActivity(),
                (String[])ingredientsNameToShow.toArray(),
                toPercentage((Float[])defaultValuesToShow.toArray(),1000f)
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

            TextView ingredientName = (TextView) rowView.findViewById(R.id.hc_ingredient_name);
            ingredientName.setText(ingredientNames[position]);

            SeekBar goalBar = (SeekBar) rowView.findViewById(R.id.hc_goal_bar);
            goalBar.setProgress(defaultGoalPercentage[position]);
            //goalBar.setProgressDrawable();
            //goalBar.setThumb(writeOnDrawable(R.drawable.pin,"QQ"));

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
