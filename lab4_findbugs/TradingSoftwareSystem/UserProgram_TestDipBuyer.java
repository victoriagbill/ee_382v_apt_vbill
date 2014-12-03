
class UserProgram_TestDipBuyer extends UserProgram_Utilities {

    UserProgram_TestDipBuyer(String symbol) throws Exception {
        super(symbol);

    }

    void run() {

for (int i = 0; i < quotescount(); i++) {

   for( Position pos = getFirstOpenPosition(); pos != null; pos = getNextOpenPosition() )
   {
      // If today's closing price is 2% more than yesterday's closing price,
      // sell the stock at the high if profit can be made.
      if ((close(i) > (close(i-1) * 1.02)) && (close(i) > pos.quote.getClose()) ) {
         sellAtMarket(i, pos);
      }
   }

   // If today's closing price is less than 2% of yesterday's closing price,
   // buy the stock at the dip.
   if (close(i) < (close(i-1) * 0.98)) {
       buyAtMarket(i, 1000);
   }
}

    }
}

