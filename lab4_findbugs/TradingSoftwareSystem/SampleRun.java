import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class Pair<T,U> {
  public final T fst;
  public final U snd;
  public Pair(T fst, U snd) {
    this.fst = fst;
    this.snd = snd;
  }
}

class Purchase {
  Pair<QuoteRecord,Integer> p;
  Purchase(QuoteRecord q, Integer price) {
    this.p = new Pair<QuoteRecord,Integer>(q,price);
  }
  public QuoteRecord qr() { return p.fst; }
  public Integer amount() { return p.snd; }
}


public class SampleRun {
  static final Map<String,List<QuoteRecord>> symbolToQuotesMap = AdnanDatabase.GetDatabaseAsMap();
  static Map<String,List<Purchase>> buys = new HashMap<String,List<Purchase>>();
  public static void main(String [] args) {
    run();
    evaluate();
  }

  static void run() {
    for (Map.Entry<String,List<QuoteRecord>> entry : symbolToQuotesMap.entrySet() ) {
      List<Purchase> purchases = new ArrayList<Purchase>();
      buys.put(entry.getKey(), purchases);

      List<QuoteRecord> quotes = entry.getValue();
      Double initialPrice = null;
      for (QuoteRecord aQuote : quotes) {
        if (initialPrice == null) {
          initialPrice = aQuote.open;
        }
        if (aQuote.close < initialPrice * 0.95) {
          Purchase aPurchase = new Purchase(aQuote, 100);
          purchases.add(aPurchase);
        }
      }
    }
  }

  static void evaluate() {
    Double totalProfit = 0.0;
    for (Map.Entry<String,List<Purchase>> entry : buys.entrySet() ) {
      Double thisSymbolProfit = 0.0;
      // TODO: is there a getlast method?
      List<QuoteRecord> l = symbolToQuotesMap.get(entry.getKey());
      QuoteRecord latest = l.get(l.size() - 1);
      for (Purchase aPurchase : entry.getValue()) {
        System.out.println(entry.getKey() + ":" + aPurchase.amount() + ":" + latest.close);
        totalProfit += 100*(latest.close - aPurchase.qr().close);
        thisSymbolProfit += aPurchase.amount()*(latest.close - aPurchase.qr().close);
      }
      System.out.println("Total profit for " + entry.getKey() + ":" + thisSymbolProfit);
    }
    System.out.println("Total profit:" + totalProfit);
  }
}
