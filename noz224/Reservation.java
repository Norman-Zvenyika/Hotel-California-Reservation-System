import java.sql.Date;

public class Reservation {
    private int reservationID;
    private int customerID;
    private int hotelID;
    private int roomTypeID;
    private int numberOfGuests;
    private Date arrivalDate;
    private Date departureDate;
    private String reservationStatus;

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

    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getHotelID() {
        return hotelID;
    }

    public void setHotelID(int hotelID) {
        this.hotelID = hotelID;
    }

    public int getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(int roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
