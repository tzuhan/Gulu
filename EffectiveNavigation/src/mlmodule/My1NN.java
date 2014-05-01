package mlmodule;

import com.example.android.effectivenavigation.DrinksInformation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kantei on 2014/01/08.
 */
public class My1NN {

    private static final int[][] dataMatrix = {
        {27,161,18,87,19,125}, //Black Tea
        {32,178,35,158,32,176}, //Green Tea
        {2,26,0,4,0,12}, //Milk Tea
        {11,78,2,14,6,56}, //Orange Juice
        //{25,150,17,85,19,123}, //Orange Juice
        {23,144,15,77,14,103}, //Vegetable juice

        {30,173,18,90,21,131}, //Orange Soda
        {20,134,4,14,8,65}, //Coffee

        {17,115,7,39,9,75}, //Cola
        {4,54,1,6,0,0} //Grape Juice
    };

    private int numOfDrinks = DrinksInformation.drinks_list.length;

    public int predictInstance(String[] featureData) {
        Map<Integer,Float> indexDistanceMap = new HashMap<Integer,Float>(numOfDrinks);
        for(int index = 0;index < numOfDrinks;index++) {
            float distance = 0;
            for(int dim = 0;dim < DrinksInformation.NUM_FEATURE_VALUES;dim++) {
                float difference = Float.valueOf(featureData[dim]) - dataMatrix[index][dim];
                distance += difference*difference;
            }
            indexDistanceMap.put(index,distance);
        }

        ValueComparator bvc = new ValueComparator(indexDistanceMap);
        TreeMap<Integer,Float> sorted_map = new TreeMap<Integer,Float>(bvc);
        sorted_map.putAll(indexDistanceMap);
        return sorted_map.firstKey();
    }

    private class ValueComparator implements Comparator<Integer> {

        Map<Integer,Float> base;
        public ValueComparator(Map<Integer,Float> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(Integer a, Integer b) {
            if (base.get(a) <= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
