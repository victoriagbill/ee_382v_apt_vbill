import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This class contains a stock's complete historical quote data available in the database.
 * 
 * @author      Adnan Aziz
 * @author      Salim Amirdache
 */

public class Quotes {

    private List<Quote> quotes = null;
    private Series<Date> date;
    private Series<Double> open;
    private Series<Double> low;
    private Series<Double> high;
    private Series<Double> close;
    private Series<Long> volume;
    private Series<Double> adjClose;
    private String symbol;

    /**
     * Returns the trading day's date for the specified trading day index  
     */
    public Date date(int index) {
        if (index < 0)
            index = 0;

        if (index > quotes.size() - 1)
            index = quotes.size() - 1;

        return quotes.get(index).getDate();
    }
    
    /**
     * Returns the trading day's opening price for the specified trading day index.  
     */
    public double open(int index) {
        if (index < 0)
            index = 0;

        if (index > quotes.size() - 1)
            index = quotes.size() - 1;

        return quotes.get(index).getOpen();
    }

    /**
     * Returns the trading day's lowest price for the specified trading day index.  
     */
    public double low(int index) {
        if (index < 0)
            index = 0;

        if (index > quotes.size() - 1)
            index = quotes.size() - 1;

        return quotes.get(index).getLow();
    }

    /**
     * Returns the trading day's highest price for the specified trading day index.  
     */
    public double high(int index) {
        if (index < 0)
            index = 0;

        if (index > quotes.size() - 1)
            index = quotes.size() - 1;
        
        return quotes.get(index).getHigh();
    }

    /**
     * Returns the trading day's closing price for the specified trading day index.  
     */
    public double close(int index) {
        if (index < 0)
            index = 0;

        if (index > quotes.size() - 1)
            index = quotes.size() - 1;

        return quotes.get(index).getClose();
    }

    /**
     * Returns the trading day's volume for the specified trading day index.  
     */
    public long volume(int index) {
        if (index < 0)
            index = 0;

        if (index > quotes.size() - 1)
            index = quotes.size() - 1;
        
        return quotes.get(index).getVolume();
    }

    /**
     * Returns the trading day's adjusted closing price for the specified trading day index.  
     */
    public double adjClose(int index) {
        if (index < 0)
            index = 0;

        if (index > quotes.size() - 1)
            index = quotes.size() - 1;
        
        return quotes.get(index).getAdjClose();
    }
    
    /**
     * Returns a Series class of all the Date's of the stock.
     */
    public Series<Date> dateSeries() {
        return date;
    }
    
    /**
     * Returns a Series class of all the Opening prices of the stock.  
     */
    public Series<Double> openSeries() {
        return open;
    }
    
    /**
     * Returns a Series class of all the low's of the stock.
     */
    public Series<Double> lowSeries() {
        return low;
    }

    /**
     * Returns a Series class of all the high's of the stock.
     */
    public Series<Double> highSeries() {
        return high;
    }

    /**
     * Returns a Series class of all the volume's of the stock.
     */
    public Series<Long> volumeSeries() {
        return volume;
    }

    /**
     * Returns a Series class of all the closing price's of the stock.
     */
    public Series<Double> closeSeries() {
        return close;
    }

    /**
     * Returns a Series class of all the adjusted closing price's of the stock.
     */
    public Series<Double> adjCloseSeries() {
        return adjClose;
    }
    
    /**
     * Returns a Quote class for the specified trading day index.
     */
    public Quote getQuote(int index) {
        return quotes.get(index);
    }

    /**
     * Returns the number of trading days available.
     */
    public int size() {
        return quotes.size();
    }

    /**
     * Returns the stock symbol for the Quotes class.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the number of Quotes (trading days) available.
     */
    public int quotescount() {
        return quotes.size();
    }

    /**
     * Creates and populates the Quotes class with data for the specified stock symbol.
     */
    public Quotes(String symbol) throws Exception {
    
        List<QuoteRecord> qrs = Global.symbolToQuotesMap.get(symbol);

        this.symbol = symbol;
        this.quotes = new ArrayList<Quote>();
        
        if (qrs != null) {
            
            int numberOfBars   = qrs.size();
            Date [] date       = new Date[numberOfBars];
            Double [] open     = new Double[numberOfBars];
            Double [] high     = new Double[numberOfBars];
            Double [] low      = new Double[numberOfBars];
            Double [] close    = new Double[numberOfBars];
            Long [] volume     = new Long[numberOfBars];
            Double [] adjClose = new Double[numberOfBars];
            
            for (int i = 0; i < numberOfBars; i++) {
                
                Quote bar = new Quote(qrs.get(i).date, 
                                  qrs.get(i).open, 
                                  qrs.get(i).high, 
                                  qrs.get(i).low, 
                                  qrs.get(i).close, 
                                  qrs.get(i).volume, 
                                  qrs.get(i).adjClose);

                // Add Bar to the list of bars
                quotes.add(bar);
                
                // Add element data to arrays
                date[i] = qrs.get(i).date;
                open[i] = qrs.get(i).open;
                high[i] = qrs.get(i).high;
                low[i] = qrs.get(i).low;
                close[i] = qrs.get(i).close;
                volume[i] = qrs.get(i).volume;
                adjClose[i] = qrs.get(i).adjClose;
            }

            this.date = new Series<Date>(date);
            this.open = new Series<Double>(open);
            this.low = new Series<Double>(low);
            this.high = new Series<Double>(high);
            this.close = new Series<Double>(close);
            this.volume = new Series<Long>(volume);
            this.adjClose = new Series<Double>(adjClose);
        }
    }
}
