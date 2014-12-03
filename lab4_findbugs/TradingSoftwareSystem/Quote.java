import java.util.Date;


/**
 * This class encapsulates a single day's stock quote information.
 * 
 * @author      Adnan Aziz
 * @author      Salim Amirdache
 */

public class Quote {

    private Date date;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private double adjClose;

    /**
     * Constructor for the Quote class.
     */
    public Quote(Date date, double open, double high, double low, double close, long volume, double adjClose) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.adjClose = adjClose;
    }
    
    /**
     * Returns the Quote's Date 
     *
     * @return Returns the quote's trading Date 
     */
    public Date getDate() {
        return date;
    }    

    /**
     * Returns the Quote's opening trading day price 
     *
     * @return Returns a double with the stock's opening price for the trading day 
     */
    public double getOpen() {
        return open;
    }

    /**
     * Returns the Quote's trading day high 
     *
     * @return Returns a double with the highest stock price for the trading day 
     */
    public double getHigh() {
        return high;
    }

    /**
     * Returns the Quote's trading day low 
     *
     * @return Returns a double with the lowest stock price for the trading day 
     */
    public double getLow() {
        return low;
    }
    
    /**
     * Returns the Quote's closing trading day price 
     *
     * @return Returns a double with the stock's closing price for the trading day 
     */
    public double getClose() {
        return close;
    }
    
    /**
     * Returns the Quote's adjusted close trading day price 
     *
     * @return Returns a double with the stock's closing price adjusted for dividends and splits 
     */
    public double getAdjClose() {
        return adjClose;
    }
    
    /**
     * Returns the Quote's trading day volume 
     *
     * @return Returns a long with the volume for the trading day 
     */
    public long getVolume() {
        return volume;
    }
} 