import java.util.ArrayList;
import java.util.List;

public class Transaction {
    
    private Position OpeningPosition;
    private Quote ClosingQuote;
    
    public Transaction(Position p, Quote q) {
        this.OpeningPosition = p;
        this.ClosingQuote = q;
    }
    
    public Quote getOpeningPositionQuote() {
        return OpeningPosition.quote;
    }

    public int getNumberOfSharesBought() {
        return OpeningPosition.numberOfShares;
    }

    public int getPositionID() {
        return OpeningPosition.positionId;
    }
    
    public Quote getClosingQuote() {
        return ClosingQuote;
    }
}
