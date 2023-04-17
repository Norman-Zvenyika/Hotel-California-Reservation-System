/**
 * Reservation class represents a reservation in the hotel management system.
 */
public class Reservation {
    // Reservation instance variables
    private int reservationID;
    private int customerID;
    private int hotelID;
    private int roomTypeID;
    private int numberOfGuests;
    private Date arrivalDate;
    private Date departureDate;
    private String reservationStatus;

    /**
     * Constructor with parameters for the Reservation class.
     *
     * @param reservationID    the unique identifier for the reservation
     * @param customerID       the unique identifier for the customer who made the reservation
     * @param hotelID          the unique identifier for the hotel where the reservation is made
     * @param roomTypeID       the unique identifier for the type of room reserved
     * @param numberOfGuests   the total number of guests for this reservation
     * @param arrivalDate      the date when the guests will arrive at the hotel
     * @param departureDate    the date when the guests will depart from the hotel
     * @param reservationStatus the current status of the reservation (e.g., confirmed, canceled, etc.)
     */
    public Reservation(int reservationID, int customerID, int hotelID, int roomTypeID, int numberOfGuests, Date arrivalDate, Date departureDate, String reservationStatus) {
        this.reservationID = reservationID;
        this.customerID = customerID;
        this.hotelID = hotelID;
        this.roomTypeID = roomTypeID;
        this.numberOfGuests = numberOfGuests;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.reservationStatus = reservationStatus;
    }

    // Getters and setters for the Reservation class with Javadoc comments

    /**
     * Gets the reservation ID.
     *
     * @return the reservation ID
     */
    public int getReservationID() {
        return reservationID;
    }

    /**
     * Sets the reservation ID.
     *
     * @param reservationID the new reservation ID
     */
    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    /**
     * Gets the customer ID.
     *
     * @return the customer ID
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * Sets the customer ID.
     *
     * @param customerID the new customer ID
     */
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    /**
     * Gets the hotel ID.
     *
     * @return the hotel ID
     */
    public int getHotelID() {
        return hotelID;
    }

    /**
     * Sets the hotel ID.
     *
     * @param hotelID the new hotel ID
     */
    public void setHotelID(int hotelID) {
        this.hotelID = hotelID;
    }

    /**
     * Gets the room type ID.
     *
     * @return the room type ID
     */
    public int getRoomTypeID() {
        return roomTypeID;
    }

    /**
     * Sets the room type ID.
     *
     * @param roomTypeID the new room type ID
     */
    public void setRoomTypeID(int roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    /**
     * Gets the number of guests.
     *
     * @return the number of guests
     */
    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    /**
    * Sets the number of guests.
    *
    * @param numberOfGuests the new number of guests
    */
    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    /**
    * Gets the arrival date.
    *
    * @return the arrival date
    */
    public Date getArrivalDate() {
        return arrivalDate;
    }

    /**
    * Sets the arrival date.
    *
    * @param arrivalDate the new arrival date
    */
    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    /**
    * Gets the departure date.
    *
    * @return the departure date
    */
    public Date getDepartureDate() {
        return departureDate;
    }

    /**
    * Sets the departure date.
    *
    * @param departureDate the new departure date
    */
    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    /**
    * Gets the reservation status.
    *
    * @return the reservation status
    */
    public String getReservationStatus() {
        return reservationStatus;
    }

    /**
    * Sets the reservation status.
    *
    * @param reservationStatus the new reservation status
    */
    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
