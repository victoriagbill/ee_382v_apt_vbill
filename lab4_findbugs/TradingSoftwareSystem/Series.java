import java.util.ArrayList;
import com.tictactec.ta.lib.*;


/**
 * This class provides a generic container for an array of values of the same type. 
 * 
 * Internally, the values are stored in an ArrayList.
 * The Series technical analysis methods use the TA-lib technical analysis functions.
 * 
 * TA-lib: http://www.ta-lib.org/
 * 
 * @author      Adnan Aziz
 * @author      Salim Amirdache
 */

public class Series<T> {

    ArrayList<T> values;

    Series() {
    }

    /**
     * Constructor for the Series class. An ArrayList of type T is 
     * populated.
     *
     * @param  in a generic array of values 
     */
    public Series(T in[]) {
        values = new ArrayList<T>(in.length);

        for (int i = 0; i < in.length; i++) {
            values.add(in[i]);
        }
    }

    /**
     * Returns the Series value specified by the index. 
     *
     * @param  index position of value to be returned
     * @return Returns the value specified by the index 
     */
    public T get(int index) {
        return values.get(index);
    }

    /**
     * Returns the size of the Series. 
     *
     * @return Returns the size of the Series 
     */
    public int size() {
        return values.size();
    }

    
    
    /**
     * Returns a Series of type Double that contains the Simple Moving Averages 
     * of the contained Series data using the specified period. 
     *
     * @param period the number of previous values to average
     * @return Returns a Series of type Double.
     */
    public Series<Double> SMA(int period) {

        Double[] inDouble = new Double[values.size()];
        double[] outdouble = new double[values.size()];
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();

        // Convert ArrayList to array of Doubles
        inDouble = values.toArray(inDouble);
        
        // Convert array of Double to double
        double[] indouble = new double[values.size()];
        for (int i = 0; i < inDouble.length; i++) {
            indouble[i] = inDouble[i].doubleValue();
        }
        
        // Only takes in double not Double!
        RetCode ret = Global.talib.sma(0, indouble.length - 1, indouble, period, outBegIdx, outNBElement, outdouble);
        if(ret != RetCode.Success) {
            return null;
        }
        
        // Convert array of double to Double
        Double[] outDouble = new Double[outdouble.length];
        
        // Fill invalid values
        for (int i = 0; i < outBegIdx.value; i++) {
            outDouble[i] = 0.0;
        }
        
        // Fill the valid values
        for (int i = outBegIdx.value, j = 0; i < outdouble.length; j++, i++) {
            outDouble[i] = Double.valueOf(outdouble[j]);
        }

        Series<Double> results = new Series<Double>(outDouble);

        return results;
    }

    /**
     * Returns a new Series of the Exponential Moving Average of the Series. 
     *
     * @param period the number of values to average
     * @return Returns a Series of type Double. 
     */
    public Series<Double> EMA(int period) {

        Double[] inDouble = new Double[values.size()];
        double[] outdouble = new double[values.size()];
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();

        // Convert ArrayList to array of Doubles
        inDouble = values.toArray(inDouble);
        
        // Convert array of Double to double
        double[] indouble = new double[values.size()];
        for (int i = 0; i < inDouble.length; i++) {
            indouble[i] = inDouble[i].doubleValue();
        }
        
        // Only takes in double not Double!
        RetCode ret = Global.talib.ema(0, indouble.length - 1, indouble, period, outBegIdx, outNBElement, outdouble);
        if(ret != RetCode.Success) {
            return null;
        }
        
        // Convert array of double to Double
        Double[] outDouble = new Double[outdouble.length];
        
        // Fill invalid values
        for (int i = 0; i < outBegIdx.value; i++) {
            outDouble[i] = 0.0;
        }
        
        // Fill the valid values
        for (int i = outBegIdx.value, j = 0; i < outdouble.length; j++, i++) {
            outDouble[i] = Double.valueOf(outdouble[j]);
        }

        Series<Double> results = new Series<Double>(outDouble);

        return results;
    }
}