import java.util.List;
import java.util.Map;
import com.tictactec.ta.lib.*;

class Global {
    static final Map<String,List<QuoteRecord>> symbolToQuotesMap = SymbolsDatabase.GetDatabaseAsMap();
    
    static Core talib = new Core();
}
