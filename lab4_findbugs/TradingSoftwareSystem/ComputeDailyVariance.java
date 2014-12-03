import java.util.Map;
import java.util.List;
import java.util.Date;

public class ComputeDailyVariance {

  private static double truncate(double x){
    if ( x > 0 ) {
       return Math.floor(x * 100)/100;
    } else {
      return Math.ceil(x * 100)/100;
    }
  }

  public static void main(String [] args) {
    try {     

      for (Map.Entry<String,List<QuoteRecord>> entry : Global.symbolToQuotesMap.entrySet() ) {
         String symbol = entry.getKey(); 
         List<QuoteRecord> quotes = entry.getValue();
         System.out.println("Processing " + symbol );
         QuoteRecord prior = null;
         for ( QuoteRecord q : quotes ) {
           if ( prior != null ) {
             long plong = prior.date.getTime();
             long qlong = q.date.getTime();
             if ( ( (qlong - plong) - 24 * 60 * 60 * 1000 ) > 100000 ) {
             } else {
               double qVar = (qlong - plong)/qlong;
               System.out.println( "Variance: " + truncate( qVar ) );
               if (Math.abs(qVar) > 0.5) {
                 System.out.println("Outlier: " + symbol + " " + truncate(qVar) + 
                                            " " + new Date( qlong ) +  ":" + new Date( plong ) );
                 System.out.println("Outlier: " + symbol + " " + q.close + " " + prior.close );
               }
             }
           }
           prior = q;
         }
      }
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
}
