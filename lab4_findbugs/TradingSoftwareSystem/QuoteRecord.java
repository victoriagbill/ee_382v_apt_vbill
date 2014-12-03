import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.io.Serializable;

public class QuoteRecord implements Comparable<QuoteRecord>, Serializable {
  static final SimpleDateFormat ymd = new SimpleDateFormat("yyyyy-MM-dd");
  // 'Date','Open','High','Low','Close','Volume','Adj Close'
  Date date;
  double open;
  double high;
  double low;
  double close;
  long volume;
  double adjClose;

  QuoteRecord(String date, String open, String high, String low, String close, String volume, String adjClose) {
    this.open = new Double(open);
    this.high = new Double(high);
    this.low = new Double(low);
    this.close = new Double(close);
    this.volume = new Long(volume);
    this.adjClose = new Double(adjClose);

    try {
      this.date = ymd.parse(date);
    } catch (Exception e) {
      System.out.println("Bad formatting for entry:" + date);
      e.printStackTrace();
    }
  }

  static QuoteRecord QuoteRecordFromString(String recordString) {
    String noQuotes = recordString.replaceAll("'", "");
    String [] fields = noQuotes.split(",");
    return new QuoteRecord(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
  }

  public int compareTo(QuoteRecord q) {
    return new Long( date.getTime() ).compareTo(q.date.getTime());
  }

  public String toString() {

    String dateString = ymd.format(date);

    String result = dateString
                      + "," + open 
                      + "," + high 
                      + "," + low 
                      + "," + close 
                      + "," + new Long(volume).toString()
                      + "," + adjClose;
    return result;
  }

  public static void main(String [] args) {
    QuoteRecord t0 = QuoteRecordFromString("'2010-01-11','1.0','2.0','3.0','4.0','100','1.5'");
    System.out.println(t0.toString());
    QuoteRecord t1 = QuoteRecordFromString("'2010-04-12','34.21','34.33','34.04','34.21','2957500','34.21'");
    System.out.println(t1.toString());
    QuoteRecord t2 = new QuoteRecord("2010-04-12","34.21","34.33","34.04","34.21","2957500","34.21");
    System.out.println(t2.toString());

    System.out.println("Compare of t0 with t1:" + t0.compareTo(t1));
    System.out.println("Compare of t1 with t2:" + t1.compareTo(t2));
    System.out.println("Compare of t2 with t1:" + t1.compareTo(t2));
  }
 }
