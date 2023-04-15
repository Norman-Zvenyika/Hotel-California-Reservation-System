import java.sql.Date;

public class Card {
    
    // Card variables
    private int cardID;
    private String cardToken;
    private String cardType;
    private Date expirationDate;

    // Default constructor
    public Card() {

    }

    // Constructor with parameters
    public Card(int cardID, String cardToken, String cardType, Date expirationDate) {
        this.cardID = cardID;
        this.cardToken = cardToken;
        this.cardType = cardType;
        this.expirationDate = expirationDate;
    }

    // Setters
    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setExpirationDate(Date date) {
        this.expirationDate = date;
    }

    // Getters
    public int getCardID() {
        return cardID;
    }

    public String getCardToken() {
        return cardToken;
    }

    public String getCardType() {
        return cardType;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }
}

