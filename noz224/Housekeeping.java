import java.sql.*;
import java.util.Scanner;

/**
 * Housekeeping class responsible for handling housekeeping operations such as
 * cleaning rooms for customers with active reservations.
 */
public class Housekeeping {

    /**
     * The main menu for housekeeping operations.
     * 
     * @param con      the database connection
     * @param s        the statement object
     * @param myScanner the scanner for user input
     */
    public static void mainMenu(Connection con, Statement s, Scanner myScanner) {

        // If the customer is not found, let the agent know
        int customerId = FrontDeskAgent.getCustomerID(con, myScanner);

        if (customerId < 0) {
            System.out.println("\nThe customer was not found\n");
        } 
        else {

            // Check if the customer has a reservation
            Reservation reservationDetails = FrontDeskAgent.getReservation(con, customerId);

            // If reservation is found, pull the room type, and hotelID
            if (reservationDetails.getReservationID() == -1) {
                System.out.println("\nThe customer has no active reservation\n");
            } 
            else {
                // Perform the actions according to check out or check in
                if (cleanRoom(con, reservationDetails, "Available")) {
                    System.out.println("\nRoom cleaning completed.");
                }
            }
        }
    }

    /**
     * Clean the room and update its status in the database.
     * 
     * @param con        the database connection
     * @param reservation the reservation object
     * @param roomStatus  the new room status
     * @return true if the room is cleaned and updated, false otherwise
     */
    public static boolean cleanRoom(Connection con, Reservation reservation, String roomStatus) {
        try {
            // Get the hotel ID and roomTypeID from the reservation object
            int hotelID = reservation.getHotelID();
            int roomTypeID = reservation.getRoomTypeID();
            int roomID;

            // Identify the hotel from the hotel table
            String hotelQuery = "SELECT * FROM Hotel WHERE hotelID = ?";
            try (PreparedStatement hotelStmt = con.prepareStatement(hotelQuery)) {
                hotelStmt.setInt(1, hotelID);

                try (ResultSet hotelResult = hotelStmt.executeQuery()) {
                    if (!hotelResult.next()) {
                        System.out.println("Hotel not found.");
                        return false;
                    }
                }
            }

            // Identify all rooms with a roomTypeID and belong to the specific hotel
            String roomTypeQuery = "SELECT * FROM Room WHERE roomTypeID = ? AND hotelID = ? and roomStatus = ?";
            try (PreparedStatement roomTypeStmt = con.prepareStatement(roomTypeQuery)) {
                roomTypeStmt.setInt(1, roomTypeID);
                roomTypeStmt.setInt(2, hotelID);
                roomTypeStmt.setString(3, "Ready to be cleaned");

                try (ResultSet roomTypeResult = roomTypeStmt.executeQuery()) {
                    if (!roomTypeResult.next()) {
                        System.out.println("\nThe room is clean and available for next use.");
                        return false;
                    } 
                    else {
                        roomID = roomTypeResult.getInt("roomID");
                        int roomNumber = roomTypeResult.getInt("roomNumber");
                        System.out.println("Room ID: " + roomID);
                        System.out.println("Room Number: " + roomNumber);
                    }
                }
            }

            // Create a CallableStatement to call the stored procedure
            String storedProcedureCall = "{call UPDATE_ROOM_STATUS(?, ?, ?, ?, ?)}";
            try (CallableStatement cstmt = con.prepareCall(storedProcedureCall)) {
                // Set the input parameters
                cstmt.setInt(1, hotelID);
                cstmt.setInt(2, roomTypeID);
                cstmt.setInt(3, roomID);
                cstmt.setString(4, roomStatus);

                // Register the output parameter
                cstmt.registerOutParameter(5, Types.INTEGER);

                // Execute the stored procedure
                cstmt.execute();

                // Retrieve the output parameter value (room status update result)
                int updateResult = cstmt.getInt(5);

                // Check the update result and return true or false accordingly
                if (updateResult == 1) {
                    return true;
                } 
                else {
                    return false;
                }
            }
        } 
        catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("The room has not been cleaned due to a system problem");
            return false;
        }
    }
}

