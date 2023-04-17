/**
 * Represents a Customer with customerID, firstName, lastName, phoneNumber, cardID, addressID, and membershipID.
 */
public class Customer {

    // Customer variables
    private int customerID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private int cardID;
    private int addressID;
    private int membershipID;

    /**
     * Default constructor.
     */
    public Customer() {
        
    }

    /**
     * Constructor with parameters.
     *
     * @param customerID    The unique ID of the customer.
     * @param firstName     The first name of the customer.
     * @param lastName      The last name of the customer.
     * @param phoneNumber   The phone number of the customer.
     * @param cardID        The card ID associated with the customer.
     * @param addressID     The address ID associated with the customer.
     * @param membershipID  The membership ID associated with the customer.
     */
    public Customer(int customerID, String firstName, String lastName, String phoneNumber, int cardID, int addressID, int membershipID) {
        this.customerID = customerID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.cardID = cardID;
        this.addressID = addressID;
        this.membershipID = membershipID;
    }

    // Setters

    /**
     * Sets the customer ID.
     *
     * @param customerID The unique ID of the customer.
     */
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    /**
     * Sets the first name of the customer.
     *
     * @param firstName The first name of the customer.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the last name of the customer.
     *
     * @param lastName The last name of the customer.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the phone number of the customer.
     *
     * @param phoneNumber The phone number of the customer.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Sets the card ID associated with the customer.
     *
     * @param cardID The card ID associated with the customer.
     */
    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    /**
     * Sets the address ID associated with the customer.
     *
     * @param addressID The address ID associated with the customer.
     */
    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    /**
     * Sets the membership ID associated with the customer.
     *
     * @param membershipID The membership ID associated with the customer.
     */
    public void setMembershipID(int membershipID) {
        this.membershipID = membershipID;
    }

    // Getters

    /**
     * Returns the customer ID.
     *
     * @return The unique ID of the customer.
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * Returns the first name of the customer.
     *
     * @return The first name of the customer.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name of the customer.
     *
     * @return The last name of the customer.
     */
    public String getLastName() {
        return lastName;
    }

        /**
     * Returns the phone number of the customer.
     *
     * @return The phone number of the customer.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns the card ID associated with the customer.
     *
     * @return The card ID associated with the customer.
     */
    public int getCardID() {
        return cardID;
    }

    /**
     * Returns the address ID associated with the customer.
     *
     * @return The address ID associated with the customer.
     */
    public int getAddressID() {
        return addressID;
    }

    /**
     * Returns the membership ID associated with the customer.
     *
     * @return The membership ID associated with the customer.
     */
    public int getMembershipID() {
        return membershipID;
    }
}



