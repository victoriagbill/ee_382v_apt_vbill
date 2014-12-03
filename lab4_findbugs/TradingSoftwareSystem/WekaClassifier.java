import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
import weka.core.*;


/**
 * This class provides access to the Weka classification and regression anaylysis tools.  
 *
 * WEKA: http://www.cs.waikato.ac.nz/ml/weka/
 * 
 * @author      Adnan Aziz
 * @author      Salim Amirdache
 */

public class WekaClassifier {

    private Classifier m_classifier;
    private double m_LastRunPercentCorrect;
    
    /**
     * Build the specified classifier with the provided input data range. 
     *
     * @param classifierName The Weka Classifier name to use
     * @param in_data An array of Series to classify
     * @param columnTitles The titles of each Series in the in_date array
     * @param start The starting index for the training set
     * @param end The last index for the training set
     */
    public void BuildClassifier(String classifierName, Series[] in_data, String[] columnTitles, int start, int end)
    throws Exception
    {
        int size = end - start;

        // Convert all the training_data to strings
        // If a series is nominal, we would like the ArrayLoader to figure it out.
        // One day, we can have a Series[] -> Instances converter.
        String[][] training_data = new String[size][in_data.length];

        for (int i = 0, k = start; i < size; k++, i++) {
            for (int j = 0; j < in_data.length; j++) {
                training_data[i][j]  = in_data[j].get(k).toString();
            }
        }

        String[] options = {"-R", "1.0E-8", "-M", "-1"};
        
        ArrayLoader l1 = new ArrayLoader();
        l1.setSource(training_data, columnTitles);
        Instances training = l1.getDataSet();
        training.setClassIndex(training.numAttributes() - 1);
        m_classifier = Classifier.forName(classifierName, options);
        m_classifier.buildClassifier(training);
    }

    /**
     * Classify the data range provided. 
     *
     * @param in_data An array of Series to classify
     * @param columnTitles The titles of each Series in the in_data array
     * @param start The starting index for the test set
     * @param end The last index for the test set
     * @return Returns an array of strings that contain the classification for the test data.
     */
    public String[] Classify(Series[] in_data, String[] columnTitles, int start, int end)
    throws Exception
    {
        int size = end - start;
        
        String[] predictions = new String[size];
        String[][] test_data = new String[size][in_data.length];

        for (int i = 0, k = start; i < size; k++, i++) {
            for (int j = 0; j < in_data.length; j++) {
                test_data[i][j]  = in_data[j].get(k).toString();
            }
        }

        ArrayLoader l2 = new ArrayLoader();
        l2.setSource(test_data, columnTitles);
        Instances test = l2.getDataSet();
        test.setClassIndex(test.numAttributes() - 1);

        int CorrectCount = 0;
                
        for (int i = 0; i < test.numInstances(); i++) {

            double pred = m_classifier.classifyInstance(test.instance(i));
            // double[] distribution = classifier.distributionForInstance(test.instance(0));
            
            // String actual = test.instance(0).toString(test.classIndex());
            predictions[i] = test.classAttribute().value((int) pred);
            
            if (pred == test.instance(i).classValue())
            {
                CorrectCount++;
            }
        }

        m_LastRunPercentCorrect = ((double) CorrectCount / (double) test.numInstances()) * 100.0;
        
        return predictions;
    }

    /**
     * Print information about the built classifier. 
     *
     */
    public void PrintInformation() {

        String result =  m_classifier.toString();
        
        System.out.println(result);
        System.out.println("\nClassifier correct percentage: " +  m_LastRunPercentCorrect);
    }

    /**
     * If the classifier is sourcable, print the source code that implements the build classifier. 
     *
     */
    public void PrintSource() {
        // check if toSource method exists first!
    }

    /**
     * Enable or Disable printing of debug information during the building of the classifier. 
     *
     */    
    public void setDebugOutput(Boolean value) {
        m_classifier.setDebug(value);
    }
}
