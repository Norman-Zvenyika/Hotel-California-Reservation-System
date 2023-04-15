public class Customer {
    
    // Customer variables
    private int customerID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private int cardID;
    private int addressID;
    private int membershipID;
    
    // Default constructor
    public Customer() {

    }

    // Constructor with parameters
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
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public void setAddressID(int addressID) {
        this.addressID = addressID;   
    }

    public void setMembershipID(int membershipID){
        this.membershipID = membershipID;
    }

    // Getters
    public int getCustomerID() {
        return customerID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getCardID() {
        return cardID;
    }

    public int getAddressID() {
        return addressID;
    }

    public int getMembershipID() {
        return membershipID;
    }
}
