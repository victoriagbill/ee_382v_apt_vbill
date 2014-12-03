


public class Position {

    Quote quote;

    int numberOfShares;
    int positionId;
    
    public Position(Quote quote, int numberOfShares, int id) {
        this.quote = quote;

        this.numberOfShares = numberOfShares;
        this.positionId = id;
    }
    
    // SKAFIX: should be a factory, and generate id's
}
