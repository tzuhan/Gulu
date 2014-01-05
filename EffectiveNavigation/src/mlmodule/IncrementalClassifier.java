package mlmodule;

import android.os.Environment;
import android.util.Log;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.classifiers.bayes.NaiveBayesUpdateable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * This example trains NaiveBayes incrementally on data obtained
 * from the ArffLoader.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class IncrementalClassifier {

  /**
   * Expects an ARFF file as first argument (class attribute is assumed
   * to be the last attribute).
   *
   * @param args        the commandline arguments
   * @throws Exception  if something goes wrong
   */

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
    int mNumFeatures;
    String classifierTag = IncrementalClassifier.class.getName();
    NaiveBayesUpdateable mNaiveBayes;
    //we are trying Updateable classifier so we don't need Instances

    private IncrementalClassifier(int numFeatures,String[] attNames,String[] labelNames) {
        mNumFeatures = numFeatures;

        //initial class Attribute, it would be treated as label
        classAtt = new Attribute("DrinkLabels",Arrays.asList(labelNames));

        //features array(vector)
        attArray = new Attribute[numFeatures];

        for(int dim = 0;dim < numFeatures;dim++) { //dim means dimension
            Attribute current = new Attribute(attNames[dim],dim); //initialize attributes
        }

        mNaiveBayes = new NaiveBayesUpdateable();
        //instancesList = new ArrayList<Instance>();
    }

    public void addTrainingInstanceAndUpdateClassifier(String[] featureData, String label) {
        if(featureData.length == mNumFeatures) {
            Instance newInstance = new DenseInstance(mNumFeatures); //already existed Instance class
            newInstance.setClassValue(label); //set label for training
            for(int dim = 0;dim < mNumFeatures;dim++) {
                newInstance.setValue(dim,Double.valueOf(featureData[dim])); //set attribute values(feature values)
            }
            try{
                mNaiveBayes.updateClassifier(newInstance);
            }
            catch (Exception e) {
                Log.d(classifierTag,"exception happened when updating classifier:" + e.getLocalizedMessage());
            }
            //instancesList.add(newInstance);
        }
        else {
            Log.d(classifierTag,"number of values in data isn't correct");
        }
    }

    public void saveModel() { //save model to flash
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) { //writable and readable
            File root = Environment.getExternalStorageDirectory();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream(root.getAbsolutePath() + "/" + modelFileName) //put it in root
                );
                oos.writeObject(mNaiveBayes);
                oos.flush();
                oos.close();
            }
            catch (Exception e) {
                Log.d(classifierTag,"saveModel failed,exception:" + e.getLocalizedMessage());
            }
        }
    }

    public void loadModel() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            File root = Environment.getExternalStorageDirectory();
            try {
                ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(root.getAbsolutePath() + "/" + modelFileName)
                );
                mNaiveBayes = (NaiveBayesUpdateable) ois.readObject();
                ois.close();
            }
            catch (Exception e) {
                Log.d(classifierTag,"loadModel failed,exception:" + e.getLocalizedMessage());
            }
        }
    }

    /* currently we do training incrementally
    public void doTraining() {

        ArrayList<Attribute> attList = new ArrayList<Attribute>(Arrays.asList(attArray));
        Instances newDataSet = new Instances("DrinksDataSet",attList, instancesList.size());
        newDataSet.setClass(classAtt); //set dataSet's class(label)
        for(Instance instance : instancesList) { //add instance into dataSet
            newDataSet.add(instance);
        }
        //put data into training algorithm
    }
    */
  /*
  public static void main(String[] args) throws Exception {
    // load data
    ArffLoader loader = new ArffLoader();
    loader.setFile(new File("./drinks.arff"));
    Instances structure = loader.getStructure();
    structure.setClassIndex(structure.numAttributes() - 1);

    // train NaiveBayes
    NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
    nb.buildClassifier(structure);
    Instance current;
    while ((current = loader.getNextInstance(structure)) != null)
      nb.updateClassifier(current);

    // output generated model
    System.out.println(nb);
  }
  */
}