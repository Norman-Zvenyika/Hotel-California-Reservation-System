import java.sql.Date;

/**
 * Represents a Card with cardID, cardToken, cardType, and expirationDate.
 */
public class Card {

    // Card variables
    private int cardID;
    private String cardToken;
    private String cardType;
    private Date expirationDate;

    /**
     * Default constructor.
     */
    public Card() {
        
    }

    /**
     * Constructor with parameters.
     *
     * @param cardID        The unique ID of the card.
     * @param cardToken     The token representing the card information.
     * @param cardType      The type of the card (e.g., Visa, Mastercard).
     * @param expirationDate The expiration date of the card.
     */
    public Card(int cardID, String cardToken, String cardType, Date expirationDate) {
        this.cardID = cardID;
        this.cardToken = cardToken;
        this.cardType = cardType;
        this.expirationDate = expirationDate;
    }

    // Setters

    /**
     * Sets the card ID.
     *
     * @param cardID The unique ID of the card.
     */
    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    /**
     * Sets the card token.
     *
     * @param cardToken The token representing the card information.
     */
    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    /**
     * Sets the card type.
     *
     * @param cardType The type of the card (e.g., Visa, Mastercard).
     */
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    /**
     * Sets the expiration date of the card.
     *
     * @param date The expiration date of the card.
     */
    public void setExpirationDate(Date date) {
        this.expirationDate = date;
    }

    // Getters

    /**
     * Returns the card ID.
     *
     * @return The unique ID of the card.
     */
    public int getCardID() {
        return cardID;
    }

    /**
     * Returns the card token.
     *
     * @return The token representing the card information.
     */
    public String getCardToken() {
        return cardToken;
    }

    /**
     * Returns the card type.
     *
     * @return The type of the card (e.g., Visa, Mastercard).
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * Returns the expiration date of the card.
     *
     * @return The expiration date of the card.
     */
    public Date getExpirationDate() {
        return expirationDate;
    }
}


