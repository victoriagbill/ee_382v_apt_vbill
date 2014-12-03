import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;

public class SystemRun {

  private static void WriteStringToFile(String filename, String output) {
      try {
          File f=new File(filename);
          if(!f.exists())
          {
              f.createNewFile();
          }
          else
          {
              f.delete();
              f.createNewFile();
          }
          FileOutputStream fop=new FileOutputStream(f);

          fop.write(output.getBytes());
          fop.flush();
          fop.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
    
  private static String GetJSArrayofSymbols() {
      String result = "var SymbolData = [";

      try {
          for (Map.Entry<String,List<QuoteRecord>> entry : Global.symbolToQuotesMap.entrySet() ) {
              String symbol = entry.getKey();
              
              result += "\"" + symbol + "\", ";
              
          }
          result = result.substring(0, result.length() - 2);
          result += "];";

      } catch (Exception e) {
          e.printStackTrace();
      }  
      
      return result;
  }

  public static void main(String [] args) {

    try {
        /*
        * Run the user program on every symbol in the database. 
        */
        for (Map.Entry<String,List<QuoteRecord>> entry : Global.symbolToQuotesMap.entrySet() ) {

           String symbol = entry.getKey(); 
           // String symbol = "INTC";
           // System.out.println("processing " + symbol );

           UserProgram p= new UserProgram(symbol);

           p.run();

           String result = p.getGoogleChartFormattedData(1, symbol );
           // System.out.println(result );

           WriteStringToFile("htdocs/tmp/" + symbol + ".js", result);
        }

        WriteStringToFile("htdocs/SymbolData.js", GetJSArrayofSymbols());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
