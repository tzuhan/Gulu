package mlmodule;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 * Created by kantei on 2014/01/08.
 */
public class ArffManager {

    private static final String arffTag = ArffManager.class.getName();

    public void createArff(Instances dataSet) {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataSet);
        String state = Environment.getExternalStorageState();
        try {
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File arffFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/drinksTrainData.arff");
                saver.setFile(arffFile);
                saver.setDestination(arffFile);
                saver.writeBatch();
            }
            else {
                Log.d(arffTag, "cannot save arff");
            }
        }
        catch(Exception e) {
            Log.d(arffTag,Log.getStackTraceString(e));
        }
    }

    public Instances loadArff() {
        ArffLoader loader = new ArffLoader();
        String state = Environment.getExternalStorageState();
        try {
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File arffFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data.arff");
                loader.setFile(arffFile);
                return loader.getDataSet();
            }
            else {
                Log.d(arffTag, "cannot save arff");
                return null;
            }
        }
        catch(Exception e) {
            Log.d(arffTag,Log.getStackTraceString(e));
            return null;
        }
    }

}
