import java.util.Collections;
import java.util.List;  
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;

import java.net.URL;

import java.sql.*;
import com.csvreader.*;  

/**
 * This is a class for manipulating a SQLite database that's populated with
 * historical stock quote data pulled in from Yahoo Finance.
 * 
 * The database schema is as follows
 * <ul>
 * <li> There's a table called listofsymbols - which has one symbol per row
 * <li> For each symbol in listofsymbols, there's a table, which has 
 *      a number of rows - each row corresponds to data for a particular day.
 * </ul>
 * Later we may add more time series: bonds, interest rates, Google trends data.
 * <p>
 * 
 * @author      Adnan Aziz
 * @author      Salim Amirdache
 */


public class SymbolsDatabase {

  // TODO: make the start date more flexible
  private static final String DefaultStartDate = "2010-08-01";
  // private static final String[] SYMBOLS = { "INTC", "AAPL", "YHOO" };
  private static final String[] SYMBOLS = Symbols.SYMBOLS;

 /**
  * Returns a Connection object for the database called "stocks.db"
  * that lives in the current directory.
  * <p>
  *
  * @return A good connection to "stock.db"
  */

  public static Connection initialize() {
    Connection conn = null;
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (Exception e) {
      System.out.println("failed class.forname");
      System.out.println(e.getStackTrace() );
    }
    try {
      conn = DriverManager.getConnection("jdbc:sqlite:stocks.db");
    } catch (Exception e) {
      System.out.println("failed conn");
      File f = new File("stocks.db");
      if ( !f.exists() ) {
        System.out.println("stocks.db doesnt exist");
        System.out.println("create the database from scratch, see the TSS webapge for instructions");
      } else {
        System.out.println("stocks.db does exist, but conn failed, corrupt stocks.db?");
        System.out.println( e.getStackTrace() );
      }
    }
    return conn;
  }

 /**
  * Returns a URL that can be used for downloading historical
  * stock data in CSV format from Yahoo Finance.
  * 
  * @param  symbol A quote symbol, e.g., "XOM"
  * @param  startDate, the first date in the range, inclusive
  * @param  endDate, the last date in the range, inclusive
  * @return a URL that's backed up by a CSV
  */

  public static URL YahooCsvDownloadUrl( String symbol, Date startDate, Date endDate ) {

    Calendar startCal = new GregorianCalendar();
    startCal.setTime(startDate);

    Calendar endCal = new GregorianCalendar();
    endCal.setTime(endDate);

    int startMonthInt = startCal.get(Calendar.MONTH);
    int startDateInt = startCal.get(Calendar.DATE);
    int startYearInt = startCal.get(Calendar.YEAR);

    int endMonthInt = endCal.get(Calendar.MONTH);
    int endDateInt = endCal.get(Calendar.DATE);
    int endYearInt = endCal.get(Calendar.YEAR);

    // to get range of dates to update for:
    // from http://www.etraderzone.com/free-scripts/47-historical-quotes-yahoo.html
    String url = "http://ichart.finance.yahoo.com/table.csv?s=" 
            + symbol 
            + "&a=" + startMonthInt 
            + "&b=" + startDateInt
            + "&c=" + startYearInt
            + "&d=" + endMonthInt 
            + "&e=" + endDateInt
            + "&f=" + endYearInt;
    URL csvUrl  = null;
    try { 
       csvUrl = new URL(url);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("URL construction failure: " + symbol );
    }
    return csvUrl;
  }

 /** 
  * Returns URL for data starting from startDate to today, just
  * wraps YahooCsvDownloadUrl(symbol, startDate, today);
  */

  public static URL YahooCsvDownloadUrl(String symbol, Date startDate) {
    Date today = new Date();
    return YahooCsvDownloadUrl(symbol, startDate, today);
  }

 /** 
  * Returns URL for data starting from the default starting day to today, 
  * just wraps YahooCsvDownloadUrl(symbol, defaultStartDay, today);
  */

  public static URL YahooCsvDownloadUrl(String symbol) {
    Date today = new Date();
    Date startDate = null;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    try {
      startDate = formatter.parse(DefaultStartDate);
    } catch (Exception e) {
      System.out.println("Bad formatting for DefaultStartDate:" + DefaultStartDate);
      e.printStackTrace();
    }
    return YahooCsvDownloadUrl(symbol, startDate, today);
  }

 /**
  * Build a string of column names from the header of the CSV
  *
  * @param  header  Array of strings from first 
  *                 line from CSV returned by Yahoo Finance
  * @return      These strings concatenated and seperated by a comma
  */

  public static String getColNames( String [] headers ) {
    String ColumnNames = "";           
    for (int j = 0; j < headers.length; j++) {
      ColumnNames += "'" + headers[j] + "',";
    }
    // substring(a,b) returns [a,b)
    return ColumnNames.substring(0, ColumnNames.length() - 1);
  }

 /**
  * Build a string of question marks from the header of the CSV
  *
  * @param  header  Array of strings from first 
  *                 line from CSV returned by Yahoo Finance
  * @return      A string of the for "?,?,?", num question marks 
  *              = header array length
  */

  public static String getQueryQuestionMarks( String [] headers ) {
    String QueryQuestionMarks = "";           
    for (int j = 0; j < headers.length; j++) {
      QueryQuestionMarks += "?,";
    }
    // substring(a,b) returns [a,b)
    return QueryQuestionMarks.substring(0, QueryQuestionMarks.length() - 1);
  }
 
 /**
  * Compute a date object from a string
  *
  * @param  dateString - assumed to be of the form "2010-12-31"
  * @return correspoding java.util.Date object  
  */
  static private Date DateFromString(String dateString) {
    Date result = null;
    try {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      result = formatter.parse(dateString);
    } catch (Exception e) {
      System.out.println("Parse error:" + dateString);
      e.printStackTrace();
    }
    return result;
  }

 /**
  * Main method - used for testing
  *
  * @param  args  later use for seeing what calls to make using reflection 
  */
 
  public static void main( String[] args ) {

    if ( ( args.length != 1 ) || 
           !(args[0].equals( "create" ) || args[0].equals("update") ) ) {
      System.out.println("Bad argument, must be one of create or update");
    }
    
    try {
      if (args[0].equals( "create") ) {
        createDatabase(SYMBOLS);
      } else if ( args[0].equals("update") ) {
       makeDatabaseUpToDate();
      }
      // createDatabase(SP500.SYMBOLS, 1500);
      // updateListOfSymbols(SP500.SYMBOLS);
      // updateListOfSymbols(SYMBOLS);
      // System.out.println("Starting GetDatabaseAsMap:" + new Date());
      // Map<String,List<QuoteRecord>> quotes = GetDatabaseAsMap();
      // System.out.println("Starting SerializeSymbolToQuotes:" + new Date());
      // SerializeSymbolToQuotes( quotes, "/tmp/quotes.ser" );
      // System.out.println("Starting DeserializeSymbolToQuotes:" + new Date());
      // quotes = DeserializeSymbolToQuotes( "/tmp/quotes.ser" );
      // System.out.println("Done DeserializeSymbolToQuotes:" + new Date());
      // CheckSymbolToQuotesMap(Global.symbolToQuotesMap);
      // addSymbol("MSFT");
      // deleteSymbol("YHOO");
      // addSymbol("YHOO");
      // deleteDateForSymbol("YHOO","2010-04-12" );
      // makeUpToDate("YHOO");
      // makeUpToDate("INTC");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Makes the all the entries in the database for up to date.
   * <p>
   * See also makeUpToDate( String s )
   */
   static void makeDatabaseUpToDate() throws Exception {
     for ( String s : SYMBOLS ) {
       makeUpToDate( s );
     }
   }

  /**
   * Makes the entries in the database for a given symbol up to date.
   * <p>
   * This method as it currently is written is wasteful,
   * since it does not attempt to see if the date range has
   * any actual trading days - e.g., may try to pull in a range
   * of a Sat and a Sunday.
   * 
   *
   * @param  symbol - the symbol we are trying to bring up to date
   */

  static void makeUpToDate( String symbol ) throws Exception {
    Connection conn = initialize();
    Statement stat = conn.createStatement();
    ResultSet rs = stat.executeQuery("select * from " + symbol + ";");

    Date today = new Date();
    Date latestDateFoundSoFar = null;
    Date dateBeingProcessed = null;
    while (rs.next()) {
      String dateString = rs.getString("Date");
      dateBeingProcessed = DateFromString(dateString);
      System.out.println("processing:" + dateBeingProcessed);
      if ( ( latestDateFoundSoFar == null ) || (latestDateFoundSoFar.compareTo(dateBeingProcessed) < 0 ) ) {
        latestDateFoundSoFar = dateBeingProcessed;
        System.out.println("\tupdated latestDateFoundSoFar:" + dateBeingProcessed);
      }
    }
    if ( latestDateFoundSoFar.compareTo( today ) < 0 ) {
      Calendar cal = new GregorianCalendar();
      cal.setTime(latestDateFoundSoFar);

      Date succLatestDateFoundSoFar = nextDate(latestDateFoundSoFar);
      if ( DaysAreEqual( succLatestDateFoundSoFar, today) ) {
        return;
      }
      updateSymbol(symbol, succLatestDateFoundSoFar, today);
    }
  }

 /**
  * Computes the next day from the current day.
  * <p>
  * 
  * @param  aDate - date whose succesor we want
  */

  static Date nextDate(Date aDate) {
    // add the number of milliseconds in a day to aDate
    long nextDateLong = aDate.getTime() + 1000L*3600L*24L;
    return new Date(nextDateLong);
  }

 /**
  * Checks if two Date objects represent the same day.
  * <p>
  * @param  aDate - first date being checked 
  * @param  bDate - second date being checked
  *
  */

  static boolean DaysAreEqual(Date aDate, Date bDate) {
    Calendar aCal = Calendar.getInstance();
    aCal.setTime(aDate);
    Calendar bCal = Calendar.getInstance();
    bCal.setTime(bDate);
    boolean result = aCal.get(Calendar.YEAR) == bCal.get(Calendar.YEAR)
                       && aCal.get(Calendar.MONTH) == bCal.get(Calendar.MONTH)
                       && aCal.get(Calendar.DAY_OF_MONTH) == bCal.get(Calendar.DAY_OF_MONTH);
    return result;
  }

 /**
  * Deletes record for corresponding date for symbol. Used for debugging
  * the makeUpToDate method. Note that the makeUpToDate won't catch
  * missing days before the last day in the table.
  * 
  * @param  symbol - symbol for which we are to delete a record
  * @param  date - the date for which we want to delete
  */

  static void deleteDateForSymbol(String symbol, String date) throws Exception {
    Connection conn = initialize();
    Statement removeSymbol = conn.createStatement();
    String command = "DELETE FROM " + symbol + " WHERE ( Date = '" + date + "' ) ;";
    removeSymbol.executeUpdate(command);
    conn.close();
    return;
  }


 /**
  * Remove a symbol from the listofsymbols table and drop the
  * table for the symbol.
  *
  * @param  symbol - symbol to delete from database
  */

  static void deleteSymbol( String symbol ) throws Exception {
    Connection conn = initialize();
    Statement removeSymbol = conn.createStatement();
    removeSymbol.executeUpdate("DELETE FROM listofsymbols WHERE symbolname = "
        + "'" + symbol + "'" +  ";");

    Statement stat = conn.createStatement();
    stat.executeUpdate("drop table if exists " + symbol + ";");
    conn.close();

    return;
  }

  /**
   * Add a symbol into the database. Deletes corresponding table if it already exists.
   *
   * @param  symbol - symbol to add
   */

  static void addSymbol(String symbol) throws Exception {
    Connection conn = initialize();
    // TODO: there will be two calls to initialize, 
    // one above, one in deleteSymbol, check that's not a problem
    deleteSymbol(symbol);

    PreparedStatement tmpprep = conn.prepareStatement( "INSERT INTO listofsymbols VALUES (?);");
    tmpprep.setString(1, symbol );
    tmpprep.addBatch();
 
    // TODO: salim, i'm not clear on what these commands do 
    conn.setAutoCommit(false);
    tmpprep.executeBatch();
    conn.setAutoCommit(true);

    updateSymbol(symbol, DateFromString(DefaultStartDate), new Date() );
    // TODO - updateSymbol is also closing this, check it that's a problem
    conn.close();
  }

 /**
  * Updates the table of quotes for this symbol. Assumes that 
  * the listofsymbols has been updated, but the table itself
  * may not exist. Takes a date range, including both start
  * and end days.
  * <p>
  * Yahoo Finance returns an error message rather than an empty
  * CSV if the start and end dates are today. The caller
  * is responsible for checking that the call range is acceptable.
  *
  * @param  symbol - symbol to update
  * @param  startDate - beginning of range to add to 
  * @param  endDate - end of range to add to 
  */

  static void updateSymbol(String symbol, Date startDate, Date endDate ) throws Exception {
    System.out.println("Trying to update:" + symbol);
    Connection conn = initialize();
    Statement stat = conn.createStatement();
    URL data = YahooCsvDownloadUrl(symbol, startDate, endDate);
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(data.openStream()));
    } catch (java.io.FileNotFoundException e) {
      System.out.println("Symbol not found:" + symbol);
      e.printStackTrace();
      return;
    }
    CsvReader reader = new CsvReader(in);
    reader.readHeaders();
    String[] headers = reader.getHeaders();
    stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + symbol + " (" + getColNames(headers) + ");");
    String statement = 
            "INSERT INTO " 
             + symbol 
             + " ("
             + getColNames(headers)
             + ") VALUES (" 
             + getQueryQuestionMarks(headers)
             + ");";

    PreparedStatement prep = conn.prepareStatement(statement);
  
    while (reader.readRecord()) {
      for (int j = 0; j < headers.length; j++) {
        String str = reader.get(headers[j]);
            prep.setString(j+1, str);
      }
      // TODO: salim, what's the point of these calls?
      prep.addBatch();
      conn.setAutoCommit(false);
      prep.executeBatch();
      conn.setAutoCommit(true);
    }
    reader.close();
    in.close();
    conn.close();
  }

 /**
  * Prints out the records for each symbol.
  * <p>
  * Can also verify the state of the database with 
  * csh> sqlite3 stocks.bd ".dump"
  *
  * @param  symbols - list of symbols to dump data for
  */

  static Map<String,List<QuoteRecord>> dumpSymbols( List<String> symbols ) throws Exception {
    Connection conn = initialize();
    Map<String,List<QuoteRecord>> symbolToQuotesMap = 
        new TreeMap<String,List<QuoteRecord>>();
    for( int i = 0; i < symbols.size(); i++ ) {
      String symbolname = symbols.get(i);
      List<QuoteRecord> symbolToQuotes = new ArrayList<QuoteRecord>();
      symbolToQuotesMap.put(symbolname, symbolToQuotes);
      Statement stat = conn.createStatement();
      try {
        ResultSet rs = stat.executeQuery("SELECT * FROM " + symbolname + ";");
        // 'Date','Open','High','Low','Close','Volume','Adj Close'
        while (rs.next()) {
          String date = rs.getString("Date");
          String open = rs.getString("Open");
          String high = rs.getString("High");
          String low = rs.getString("Low");
          String close = rs.getString("Close");
          String volume = rs.getString("Volume");
          String adjClose = rs.getString("Adj Close");
          QuoteRecord qr = new QuoteRecord(date, open, high, low, close, volume, adjClose);
          symbolToQuotes.add(qr);
          // System.out.println(symbolname + ":open," + open + ":date," + date);
        }
        // TODO: why/when do we need to close this?
        rs.close();
      } catch ( java.sql.SQLException e ) {
        // it can happen that symbols in Symbols.java go away over time
        // so we need to ignore them
      }
    }
    return symbolToQuotesMap;
  }

 /**
  * Sorts quotes according to dates in ascending order.
  * Checks to see if a symbolToQuotesMap has duplicate dates;
  * if so it prints a warning to stdout.
  * <p>
  * Does not update the database itself.
  *
  * @param symbolToQuotesMap - maps Strings to List<QuoteRecord>s
  */

  static void SortSymbolToQuotesMap(Map<String,List<QuoteRecord>> symbolToQuotesMap) {
    for (Map.Entry<String,List<QuoteRecord>> entry : symbolToQuotesMap.entrySet() ) {
      // System.out.println("Key:" + entry.getKey() );
      List<QuoteRecord> quotes = entry.getValue();
      Collections.sort(quotes);
      QuoteRecord prior = null;
      for (QuoteRecord aQuote : quotes) {
        if ( (prior != null) && (prior.date.compareTo(aQuote.date) == 0) ) {
          System.out.println("WARNING duplicate:" + entry.getKey() + ":" + prior);
          System.out.println("WARNING duplicate:" + entry.getKey() + ":" + aQuote);
        }
        prior = aQuote;
      }
    }
  }


 /**
  * Print out the state of a symbolToQuotesMap. 
  * <p>
  * We do not make any assumptions about the quotes list - could be unsorted,
  * and may have repetitions.
  *
  * @param symbolToQuotesMap - maps Strings to List<QuoteRecord>s
  */

  static void CheckSymbolToQuotesMap(Map<String,List<QuoteRecord>> symbolToQuotesMap) {
    for (Map.Entry<String,List<QuoteRecord>> entry : symbolToQuotesMap.entrySet() ) {
      List<QuoteRecord> quotes = entry.getValue();
       for (QuoteRecord aQuote : quotes) {
         System.out.println(entry.getKey() + ":" +  aQuote.toString() );
       }
    }
  }

 /**
  * Return a mock of the map, useful for debugging.
  * 
  */

  public static Map<String,List<QuoteRecord>> GetMockDatabaseAsMap() {
    Map<String,List<QuoteRecord>> result = 
        new TreeMap<String,List<QuoteRecord>>();
    String s1 = "AAPL";
    String s2 = "XOM";
    List<QuoteRecord>  s1Quotes = new ArrayList<QuoteRecord>();
    result.put(s1,s1Quotes);
    s1Quotes.add(QuoteRecord.QuoteRecordFromString("'2010-04-01','1.0','2.0','0.5','1.1','123','1.5324'"));
    s1Quotes.add(QuoteRecord.QuoteRecordFromString("'2010-04-02','1.2','2.2','0.5','1.5','123','1.523'"));
    s1Quotes.add(QuoteRecord.QuoteRecordFromString("'2010-04-03','1.1','2.1','2.5','1.0','123','1.51'"));
    List<QuoteRecord>  s2Quotes = new ArrayList<QuoteRecord>();
    result.put(s2,s2Quotes);
    s2Quotes.add(QuoteRecord.QuoteRecordFromString("'2008-05-01','4.0','2.0','0.5','4.1','123','4.5324'"));
    s2Quotes.add(QuoteRecord.QuoteRecordFromString("'2008-05-02','4.2','2.2','0.5','4.5','423','4.523'"));
    s2Quotes.add(QuoteRecord.QuoteRecordFromString("'2008-05-06','4.1','2.1','2.5','4.0','123','4.51'"));
    return result;
  }

 /**
  * Serialize the map-view of the database
  * <p>
  * @param symbolToQuotesMap - the map to be serialized
  * @param - the name of the file we'll be writing to
  * @see DeserializeSymbolToQuotes
  *
  */

  public static void SerializeSymbolToQuotes(
      Map<String,List<QuoteRecord>> symbolToQuotesMap,
      String filename) {
    try {
      // ObjectOutputStream out = new ObjectOutputStream( new StringWriter( ) );
      ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( filename ) );
      out.writeObject( symbolToQuotesMap );
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

 /**
  * Deserialize the map-view of the database
  * <p>
  * @see SerializeSymbolToQuotes
  *
  */

  public static Map<String,List<QuoteRecord>> DeserializeSymbolToQuotes(
      String filename) {
    Map<String,List<QuoteRecord>> symbolToQuotesMap = null;
    try {
      // ObjectOutputStream out = new ObjectOutputStream( new StringWriter( ) );
      ObjectInputStream in = new ObjectInputStream( new FileInputStream( filename ) );
      symbolToQuotesMap = (Map<String,List<QuoteRecord>>) in.readObject();
      in.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return symbolToQuotesMap;
  }

 /**
  * Get the database as a map from symbols (e.g., "XOM") to
  * a list of QuoteRecords. These records are sorted in ascending
  * order by date.  
  * <p>
  * The procedure does not guarantee duplicate quote records have
  * been removed, but it does draw attention to their existence.
  * (Such records can enter the database when performing tests/updates.)
  *
  * @see  QuoteRecord, CheckSymbolToQuotesMap, SortSymbolToQuotesMap
  */
 
  public static Map<String,List<QuoteRecord>> GetDatabaseAsMap() {
    Map<String,List<QuoteRecord>> result = null;

    List<String> symbols = new ArrayList<String>();
    Connection conn = null;
    try {
      conn = initialize();
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select * from listofsymbols;");
      while (rs.next()) {
        symbols.add( rs.getString("symbolname") );
      }
      rs.close();
      result = dumpSymbols(symbols);
      conn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    SortSymbolToQuotesMap(result);
    // CheckSymbolToQuotesMap( result );
    return result;
  }

 /**
  * Drop the listofsymbols and recreate - this is a one-off
  * method, I was adding SP symbols in sets, but this
  * table was repeatedly deleted in the process.
  *
  * @see createDatabase
  */

 public static void updateListOfSymbols(String [] symbolArray) throws Exception {
    Connection conn = initialize();
    Statement stat = conn.createStatement();
    try {
      stat.executeUpdate("DROP TABLE IF EXISTS " + "listofsymbols" + ";");
      stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + "listofsymbols" + " (" + "'symbolname'" + ");" );
    } catch (Exception e) {
      System.out.println("failed stat.executeUpdate");
      System.out.println( e.getMessage() );          
    }
    for (int i = 0; i < symbolArray.length; i++) {
      stat.executeUpdate( "INSERT INTO listofsymbols VALUES ('" + symbolArray[i] + "');");
    }
    conn.close();
 }


 /**
  * Creates the database, deleting data that existed previously.
  * <p>
  * Won't actually remove symbol's tables if they aren't in the 
  * set SYMBOLS.
  *
  * @param pauseDuration - this is how long we sleep (in ms) for before
  * hitting  yahoo again
  * @see SYMBOLS
  */

  public static void createDatabase(String [] symbolArray, int pauseDuration) {
    try {
      Connection conn = initialize();
      Statement stat = conn.createStatement();
      try {
        stat.executeUpdate("DROP TABLE IF EXISTS " + "listofsymbols" + ";");
        stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + "listofsymbols" + " (" + "'symbolname'" + ");" );
      } catch (Exception e) {
        System.out.println("failed stat.executeUpdate");
        System.out.println( e.getMessage() );          
      }

      for (int i = 0; i < symbolArray.length; i++) {
        addSymbol( symbolArray[i] );
        if (pauseDuration != 0) {
          Thread.sleep(pauseDuration);
        }
      }
      conn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

 /*
  * Creates the database.
  * <p>
  * @see createDatabase(int pauseDuration)
  */

  public static void createDatabase(String [] symbolArray) {
    createDatabase(symbolArray, 0);
  }
}
