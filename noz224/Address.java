/**
 * Represents an address in the database.
 */
public class Address {
    private int addressID;
    private String street;
    private String city;
    private String state;
    private String zipCode;

    //address constructor
    public Address() {

    }

    /**
     * Gets the address ID.
     * @return the addressID
     */
    public int getAddressID() {
        return addressID;
    }

    /**
     * Sets the address ID.
     * @param addressID the addressID to set
     */
    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    /**
     * Gets the street.
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street.
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets the city.
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the state.
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the zip code.
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Sets the zip code.
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}

