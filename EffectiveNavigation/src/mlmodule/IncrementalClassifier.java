package mlmodule;

import android.os.Environment;
import android.util.Log;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class IncrementalClassifier {

    private static final String modelFileName = "NaiveBayes.model";

    //singleton pattern

    private static IncrementalClassifier singleInstance = null;

    public static IncrementalClassifier getInstance(int numFeatures,String[] attNames,String[] labelNames) {
        if(singleInstance == null) {
            synchronized (IncrementalClassifier.class) {
                if(singleInstance == null) {
                    singleInstance = new IncrementalClassifier(numFeatures,attNames,labelNames);
                }
            }
        }
        return singleInstance;
    }

    Attribute[] attArray;
    //List<Instance> instancesList;
    Attribute classAtt;
    Instances trainingDataSet;
    int mNumFeatures;
    String classifierTag = IncrementalClassifier.class.getName();
    NaiveBayesUpdateable mNaiveBayes;
    int mNumAtts;
    //we are trying Updateable classifier so we don't need Instances


    private IncrementalClassifier(int numFeatures,String[] attNames,String[] labelNames) {
        mNumFeatures = numFeatures;
        mNumAtts = numFeatures + 1;
        //initial class Attribute, it would be treated as label
        classAtt = new Attribute("DrinkLabels",Arrays.asList(labelNames));

        //features array(vector)
        attArray = new Attribute[mNumAtts]; //numFeatures + 1(Label)

        for(int dim = 0;dim < numFeatures;dim++) { //dim means dimension
            Attribute current = new Attribute(attNames[dim],dim); //initialize attributes
            attArray[dim] = current;
        }
        attArray[numFeatures] = classAtt;

        ArrayList<Attribute> attList = new ArrayList<Attribute>(Arrays.asList(attArray));
        trainingDataSet = new Instances("DrinksTrainingDataSet", attList, 0);
        trainingDataSet.setClassIndex(numFeatures); //set dataSet's class(label)

        mNaiveBayes = new NaiveBayesUpdateable();
        try {
            mNaiveBayes.buildClassifier(trainingDataSet);
        }
        catch(Exception e) {
            Log.d(classifierTag,"constructor,exception:\n" + Log.getStackTraceString(e));
        }
        Log.d(classifierTag,"initialize done");
    }

    public void addTrainingInstanceAndUpdateClassifier(String[] featureData, int label) {
        if(featureData.length == mNumFeatures) {
            Instance newInstance = new DenseInstance(mNumFeatures + 1); //already existed Instance class
            try {
                trainingDataSet.add(newInstance);
                // if we don't need to collect them and write into an arff file,
                // then we don't actually need to add them into an DataSet
                // But we still need to set this instance to a DataSet
                // due to it needs the label and feature information from dataSet
                newInstance.setDataset(trainingDataSet);
                newInstance.setClassValue((double)label); //set label for training
                for(int dim = 0;dim < mNumFeatures;dim++) {
                    newInstance.setValue(dim,Double.valueOf(featureData[dim])); //set attribute values(feature values)
                }
                mNaiveBayes.updateClassifier(newInstance);
                Log.d(classifierTag,"successfully update without exception");
            }
            catch (Exception e) {
                Log.d(classifierTag,"updating classifier,name:" + e.toString() + ",reason:\n" + Log.getStackTraceString(e));
            }
        }
        else {
            Log.d(classifierTag,"number of values in data isn't correct");
        }
    }

    public void resetClassifier(Instances dataSet) {
        trainingDataSet = dataSet;
        trainingDataSet.setClassIndex(0);
        mNaiveBayes = new NaiveBayesUpdateable();
        try {
            mNaiveBayes.buildClassifier(dataSet);
        }
        catch(Exception e) {
            Log.d(classifierTag,"resetClassifier failed,exception:\n" + Log.getStackTraceString(e));
        }
    }

    public void saveModel() { //save model to flash
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //storage is writable and readable
            File root = Environment.getExternalStorageDirectory();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream(root.getAbsolutePath() + "/" + modelFileName) //put it in root
                );
                oos.writeObject(mNaiveBayes);
                oos.flush();
                oos.close();
                Log.d(classifierTag,"save model successfully without exception");
            }
            catch (Exception e) {
                Log.d(classifierTag,"saveModel failed,exception:" + e.getLocalizedMessage());
            }
        }
    }

    public int predictInstance(String[] featureData) {
        if(featureData.length == mNumFeatures) {
            Instance testInstance = new DenseInstance(mNumFeatures + 1);
            testInstance.setDataset(trainingDataSet);
            for(int dim = 0;dim < mNumFeatures;dim++) {
                testInstance.setValue(dim,Double.valueOf(featureData[dim]));
            }

            try {
                int label = (int)mNaiveBayes.classifyInstance(testInstance);
                Log.d(classifierTag,"successfully predicting without exception");
                return label;
            }
            catch(Exception e) {
                Log.d(classifierTag,Log.getStackTraceString(e));
                return -1;
            }
        }
        else {
            Log.d(classifierTag,"number of features isn't right");
            return -1;
        }
    }

    public void loadModel() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { //readable
            File root = Environment.getExternalStorageDirectory();
            try {
                ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(root.getAbsolutePath() + "/" + modelFileName)
                );
                mNaiveBayes = (NaiveBayesUpdateable) ois.readObject();
                ois.close();
                Log.d(classifierTag,"load model successfully without exception");
            }
            catch (Exception e) {
                Log.d(classifierTag,"loadModel failed,exception:" + e.getLocalizedMessage());
                //if exception happen, we use a new model
                initializeClassifier();
            }
        }
    }

    public void initializeClassifier() {
        mNaiveBayes = new NaiveBayesUpdateable();
        try {
            mNaiveBayes.buildClassifier(trainingDataSet);
        }
        catch(Exception e) {
            Log.d(classifierTag,"loadModel,exception:\n" + Log.getStackTraceString(e));
        }
    }

    public void clearModel() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File modelFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + modelFileName);
            try {
                if(modelFile.exists()) {
                    if(!modelFile.delete()) {
                        Log.d(classifierTag,"warning:model file cannot be deleted");
                    }
                }
                else {
                    Log.d(classifierTag,"warning:model file doesn't exist");
                }
            }
            catch(Exception e) {
                Log.d(classifierTag,"clearModel:" + Log.getStackTraceString(e));
            }
        }
    }

}