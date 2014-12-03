import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class UserProgram_TestClassifier extends UserProgram_Utilities {

    UserProgram_TestClassifier(String symbol) throws Exception {
        super(symbol);
    }

    void run() {        

        int count = quotescount();
        int trainingCount = (count * 66) / 100;

        try {
            WekaClassifier c = new WekaClassifier();
            String[] columnTitles = {"high", "low", "5-day slope"};
            String[] classData = new String[count];
            
            // Build class data, 5-day lookahead (slope)
            for (int i = 0; i < count; i++) {
                if (close(i) < close(i+5)) {
                    classData[i] = "true";
                } else {
                    classData[i] = "false";
                }
            }
            Series<String> classDataSeries = new Series<String>(classData);

            Series[] data = new Series[3];
            data[0] = highSeries();
            data[1] = lowSeries();
            data[2] = classDataSeries;

            // Build classifier with training set
            c.BuildClassifier("weka.classifiers.functions.Logistic", data, columnTitles, 0, trainingCount);

            // Test the classifier using the test set
            String[] result = c.Classify(data, columnTitles, trainingCount, count);

            c.PrintInformation();

            // Trade using the classified result.
            for (int j = 0,i = trainingCount; i < count; j++, i++) {
            //for (int j = trainingCount, i = trainingCount; i < count; j++, i++) {
                for( Position pos = getFirstOpenPosition(); pos != null; pos = getNextOpenPosition() )
                {
                   if (result[j].equals("false") && (pos.quote.getClose() > close(i)) ) {
                      sellAtMarket(i, pos);    
                   }
                }

                if (result[j].equals("true"))
                {
                    buyAtMarket(i, 1000);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

