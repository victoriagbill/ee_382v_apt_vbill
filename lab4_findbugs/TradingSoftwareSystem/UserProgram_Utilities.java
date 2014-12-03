import java.util.*;
import java.util.Calendar;

import com.tictactec.ta.lib.*;


/**
 * This class is inherited by the UserProgram class and contains 
 * utlity functions for making trades and positions.
 * 
 * @author      Adnan Aziz
 * @author      Salim Amirdache
 */
public class UserProgram_Utilities extends Quotes {

    private int currentPositionIndex = 0;
    private int positionCount = 0;
    private List<Position> positions = new ArrayList<Position>();
    public List<Transaction> CompletedTransactions = new ArrayList<Transaction>();
    
    /**
     * Buy the specified number of stocks at the closing price of the specified trading day.
     * 
     * @param quoteIndex the trading day closing price to purchase the stock
     * @param numberOfShares the number of stock shares to purchase
     */
    public void buyAtMarket(int quoteIndex, int numberOfShares) {

        Quote quote = getQuote(quoteIndex);

        positions.add(new Position(quote, numberOfShares, positionCount));

        // this should go in position factory
        positionCount++;
    }

    /**
     * Sell the opened position with the specified trading day.
     * 
     * @param quoteIndex the trading day closing price to sell the stock
     * @param pos the position to close
     */
    public void sellAtMarket(int quoteIndex, Position pos) {

        // Remove Opened position
        positions.remove(pos);

        // Selling Quote
        Quote quote = getQuote(quoteIndex);

        // Add to completed transactions
        // Note, positions will contain any open positions at the end of the test session.
        CompletedTransactions.add(new Transaction(pos, quote));
    }

    /**
     * Returns the first open position.
     * 
     */
    public Position getFirstOpenPosition() {
        if (positions.size() > 0)
            return positions.get(0);
        else
            return null;
    }

    /**
     * Returns the next open position. If there are no more open positions, return null.
     * 
     */
    public Position getNextOpenPosition() {
        
        // It would be nice to use an iterator.; however, SellAtMarket
        // may not give you the current position... so we need todo some hacks.
        Position result = null;
        if (currentPositionIndex == positions.size()) {
            
            // reset position index counter
            currentPositionIndex = 0;
        } else {
            
            // can only happen if we remove many positions
            // this is a hack, and we need to solve this problem properly!!!
             if (currentPositionIndex > positions.size() )
                currentPositionIndex = 0;
            
            if (positions.size() > 0)
            {
                result = positions.get(currentPositionIndex);
                currentPositionIndex++;
            }
        }
        return result;
    }

    public UserProgram_Utilities(String symbol) throws Exception {
        super(symbol);
    }


    
    /**
     * Returns a string of transactions completed, and stock price data formatted
     * for the Google Charts APi.
     * 
     */
    // This method generates a string of quotes formatted
    // for google charts. The quotes may be separated by an interval.
    public String getGoogleChartFormattedData(int interval, String symbol) {

        String result = "\nfunction addChartData(data) {" +
            "\ndata.addRows([";

        Calendar cal = Calendar.getInstance();
        
        for (int i = 0; i < quotescount(); i+= interval) {
            cal.setTime(date(i));
            String date = cal.get(Calendar.YEAR) + "," + cal.get(Calendar.MONTH) + "," + cal.get(Calendar.DAY_OF_MONTH);

            result += "\n [new Date(" + date + "), " + close(i) + ", undefined, undefined, undefined ],";
        }

        Double totalProfit = 0.0;
        
        for (int i = 0; i < CompletedTransactions.size(); i++) {

            Transaction t = CompletedTransactions.get(i);

            Quote OpeningQuote = t.getOpeningPositionQuote();
            int NumberOfShares = t.getNumberOfSharesBought();
            double BuyPrice = OpeningQuote.getClose();

            // Output data for stock purchase
            cal.setTime(OpeningQuote.getDate());
            String date = cal.get(Calendar.YEAR) + "," + cal.get(Calendar.MONTH) + "," + cal.get(Calendar.DAY_OF_MONTH);

            result += "\n [new Date(" + date + "), " + BuyPrice + ", 'Buy', 'Bought " + NumberOfShares + " shares.', undefined ],";
            
            // Output date for stock sell
            Quote ClosingQuote = t.getClosingQuote();
            double SellPrice = ClosingQuote.getClose();

            cal.setTime(ClosingQuote.getDate());
            date = cal.get(Calendar.YEAR) + "," + cal.get(Calendar.MONTH) + "," + cal.get(Calendar.DAY_OF_MONTH);

            result += "\n [new Date(" + date + "), " + SellPrice + ", 'Sell', 'Sold " + NumberOfShares + " shares.', null ],";
            
            // Calculate profit
            totalProfit += NumberOfShares * (SellPrice - BuyPrice);
            result += "\n [new Date(" + date + "), undefined, undefined, undefined, " + totalProfit + " ],";
        }

        result = result.substring(0, result.length() - 1);
        result += "\n ]); }";

        return result;
    }
}

