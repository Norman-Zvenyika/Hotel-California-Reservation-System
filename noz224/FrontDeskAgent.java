import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class FrontDeskAgent {
    public static void mainMenu(Connection con, Statement s, Scanner myScanner) {
        
        //get the menu option
        int userOption = printOptions(myScanner);

        //if the customer is not found, let the agent know
        int customerId = getCustomerID(con, myScanner);

        if(customerId<0) {
            System.out.println("\nThe customer was not found\n");
        }
        else {
            //perform the actions according to check out or check in
            switch(userOption) {
                case 1:
                    //check if the customer has a reservation
                    Reservation reservationDetails = getReservation(con, customerId, myScanner, "Check-In");

                    //if rervation is found, pull the room type, and hotelID
                    if(reservationDetails.getReservationID()==-1 || !reservationDetails.getReservationStatus().equals("Confirmed")) {
                        System.out.println("\nThe customer has no confirmed reservation\n");
                    }
                    else{
                        //prompt the user to see if they want to change the roomtype
                        System.out.print("\nDo you want to change your room type?: ");
                        String changeRoomType = CustomerOnlineReservation.getYesOrNoInput(myScanner);
                        if(changeRoomType.equals("Y")) {
                            reservationDetails = CustomerOnlineReservation.changeRoomType(con, myScanner, reservationDetails);
                        }
    
                        if(checkIn(con, reservationDetails, "Occupied", myScanner)) {
                            //update reservation status in the reservation table
                            updateReservationStatus(con, reservationDetails, "Checked-In");
                            System.out.println("\nThe customer has successfully checked into the room.");
                        }
                        else {
                            System.out.println("\nThe check-in has failed");
                        }
                    }
                    break;
                case 2:
                    //check if the customer has checked in "Check-out is the key that we will use in getReservation"
                    reservationDetails = getReservation(con, customerId, myScanner, "Check-Out");

                    //if rervation is found, check if it is a valid reservation
                    if(reservationDetails.getReservationID()==-1 || !reservationDetails.getReservationStatus().equals("Checked-In")) {
                        System.out.println("\nThe customer has no confirmed reservation\n");
                    }
                    else {
                        if(checkOut(con, reservationDetails, "Ready to be cleaned")) {
                            //update reservation status in the reservation table
                            updateReservationStatus(con, reservationDetails, "Checked-Out");
                            System.out.println("\nThe customer has successfully checked out of the room.");
                        }
                        else {
                            System.out.println("\nThe check out has failed");
                        }
                    }
                    break;
            }
        }
    }

    /**
    * Performs check-out by updating the room status and check-out date in the database.
    *
    * @param con Connection object to connect to the database.
    * @param reservation Reservation object representing the reservation being checked out.
    * @param roomStatus String value representing the updated room status after check-out.
    * @return boolean value indicating whether the check-out was successful or not.
    */
    public static boolean checkOut(Connection con, Reservation reservation, String roomStatus) {
        int reservationId = reservation.getReservationID();
        int hotelId;
        int roomId;
        int updateRoomStatusResult;
        // Retrieve the hotelID from the Reservation table using the reservationID
        try (PreparedStatement stmt1 = con.prepareStatement("SELECT hotelID FROM Reservation WHERE reservationID = ?")) {
            stmt1.setInt(1, reservationId);
            try (ResultSet rs1 = stmt1.executeQuery()) {
                if (rs1.next()) {
                    hotelId = rs1.getInt("hotelID");
                } 
                else {
                    return false;
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Retrieve the roomID from the CustomerRoom table using the reservationID
        try (PreparedStatement stmt2 = con.prepareStatement("SELECT roomID FROM CustomerRoom WHERE reservationID = ? AND checkOutDate IS NULL ORDER BY customerROOMID ASC FETCH FIRST 1 ROWS ONLY")) {
            stmt2.setInt(1, reservationId);
            try (ResultSet rs2 = stmt2.executeQuery()) {
                if (rs2.next()) {
                    roomId = rs2.getInt("roomID");
                } 
                else {
                    return false;
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Call the UPDATE_ROOM_STATUS procedure to update the room status
        try (CallableStatement stmt3 = con.prepareCall("{call UPDATE_ROOM_STATUS(?, ?, ?, ?, ?)}")) {
            stmt3.setInt(1, hotelId);
            stmt3.setInt(2, reservation.getRoomTypeID());
            stmt3.setInt(3, roomId);
            stmt3.setString(4, roomStatus);
            stmt3.registerOutParameter(5, Types.INTEGER);
            stmt3.execute();
            updateRoomStatusResult = stmt3.getInt(5);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // If the room status was updated successfully, update the check-out date in the CustomerRoom table
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkOutDate = currentDate.format(formatter);
        java.sql.Date sqlcheckOutDate = CustomerOnlineReservation.convertStringToSqlDate(checkOutDate);
        if (updateRoomStatusResult == 1) {
            try (PreparedStatement stmt4 = con.prepareStatement("UPDATE CustomerRoom SET checkOutDate = ? WHERE reservationID = ?")) {
                stmt4.setDate(1, sqlcheckOutDate );
                stmt4.setInt(2, reservationId);
                int updatedRows = stmt4.executeUpdate();
                if (updatedRows > 0) {
                    // Disable auto-commit
                    con.setAutoCommit(false);
                    con.commit();
                    // Enable auto-commit again
                    con.setAutoCommit(true);
                    return true;
                }
            } 
            catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
    * Performs check-in by identifying a room that matches the reservation's hotel and room type,
    * updating the room status and inserting a new record into the CustomerRoom table with the check-in date.
    *
    * @param con Connection object to connect to the database.
    * @param reservation Reservation object representing the reservation being checked in.
    * @param roomStatus String value representing the updated room status after check-in.
    * @param myScanner Scanner object to read user input.
    * @return boolean value indicating whether the check-in was successful or not.
    */
    public static boolean checkIn(Connection con, Reservation reservation, String roomStatus, Scanner myScanner) {
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
                    else {
                        System.out.println("\nYour room assignment Information: ");
                        System.out.println("Hotel: " + hotelResult.getString("hotelName"));
                    }
                }
            }
    
            // Identify all rooms with a roomTypeID and belong to the specific hotel
            String roomTypeQuery = "SELECT * FROM Room WHERE roomTypeID = ? AND hotelID = ? and roomStatus = ?";
            try (PreparedStatement roomTypeStmt = con.prepareStatement(roomTypeQuery)) {
                roomTypeStmt.setInt(1, roomTypeID);
                roomTypeStmt.setInt(2, hotelID);
                roomTypeStmt.setString(3, "Available"); //look for free rooms
    
                try (ResultSet roomTypeResult = roomTypeStmt.executeQuery()) {
                    if (!roomTypeResult.next()) {
                        System.out.println("\nUnfortunately, there were no free rooms found for the given room type in the specified hotel.");
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
                if(updateResult==1) {
                    //update the customer room table
                    int reservationID = reservation.getReservationID();
                    LocalDate currentDate = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String checkInDate = currentDate.format(formatter);
                    java.sql.Date sqlcheckInDate = CustomerOnlineReservation.convertStringToSqlDate(checkInDate);

                    String insertCustomerRoom = "INSERT INTO CustomerRoom (reservationID, roomID, checkInDate) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = con.prepareStatement(insertCustomerRoom)) {
                        insertStmt.setInt(1, reservationID);
                        insertStmt.setInt(2, roomID);
                        insertStmt.setDate(3, sqlcheckInDate);
                        insertStmt.executeUpdate();
                        return true;
                    }
                }
                else {
                    return false;
                }
            }
        } 
        catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
    * Retrieves the Reservation object for the specified customer ID from the database.
    *
    * @param con the Connection object used to interact with the database
    * @param customerID the ID of the customer for which to retrieve the reservation
    * @return a Reservation object representing the reservation for the specified customer ID,
    *         or null if no reservation exists for the customer ID
    */
    public static Reservation getReservation(Connection con, int customerID, Scanner myScanner, String checkStatus) {
        Reservation reservation = null;
        List<Reservation> reservations = new ArrayList<>();
        String reservationQuery = "";

        //during checking in, we need to retrieve confirmed reservation status belonging to a particular customer ID
        if(checkStatus.equals("Check-In")) {
            reservationQuery = "SELECT * FROM Reservation WHERE customerID = ? AND reservationStatus = 'Confirmed' ORDER BY reservationID ASC";
        }

        //during check-out, we need to retrieve "checked-in" reservation status belonging to a particular customer ID
        else if (checkStatus.equals("Check-Out")) {
            reservationQuery = "SELECT * FROM Reservation WHERE customerID = ? AND reservationStatus = 'Checked-In' ORDER BY reservationID ASC";
        }

        //during cleaning, we need to retrieve "checked-out" so that we can identify the hotel in which the customer lived
        else if (checkStatus.equals("Checked-Out")) {
            reservationQuery = "SELECT * FROM Reservation WHERE customerID = ? AND reservationStatus = 'Checked-Out' ORDER BY reservationID ASC";
        }
        
        //retrieve the relevant records based on whether we are doing a check-in, check-out or cleaning
        try (PreparedStatement pstmt = con.prepareStatement(reservationQuery)) {
            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reservationID = rs.getInt("reservationID");
                    int hotelID = rs.getInt("hotelID");
                    int roomTypeID = rs.getInt("roomTypeID");
                    int numberOfGuests = rs.getInt("numberOfGuests");
                    Date arrivalDate = rs.getDate("arrivalDate");
                    Date departureDate = rs.getDate("departureDate");
                    String reservationStatus = rs.getString("reservationStatus");

                    Reservation tempReservation = new Reservation(reservationID, customerID, hotelID, roomTypeID, numberOfGuests, arrivalDate, departureDate, reservationStatus);
                    reservations.add(tempReservation);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (reservations.isEmpty()) {
            // Return a default Reservation object with negative values for the ID fields to indicate that no reservation was found
            reservation = new Reservation(-1, customerID, -1, -1, -1, null, null, null);
        } 
        else {
            System.out.println("\nAvailable reservations:");
            int index = 1;
            System.out.printf("%-4s %-15s %-10s %-12s %-12s %-15s %-10s%n", "No.", "ReservationID", "HotelID", "RoomTypeID", "ArrivalDate", "DepartureDate", "Status");
            for (Reservation r : reservations) {
                System.out.printf("%-4d %-15d %-10d %-12d %-12s %-15s %-10s%n", index, r.getReservationID(), r.getHotelID(), r.getRoomTypeID(), r.getArrivalDate(), r.getDepartureDate(), r.getReservationStatus());
                index++;
            }

            int reservationIndex;
            boolean validInput;
            do {
                System.out.print("\nPlease choose a reservation by entering the corresponding number (1-" + reservations.size() + "): ");
                validInput = true;
                try {
                    reservationIndex = Integer.parseInt(myScanner.nextLine());
                } 
                catch (Exception e) {
                    System.out.println("Invalid input. Please enter an integer.");
                    validInput = false;
                    reservationIndex = -1;
                }
            } while (!validInput || reservationIndex < 1 || reservationIndex > reservations.size());

            reservation = reservations.get(reservationIndex - 1);
        }
        return reservation;
    }

    /**
    * Updates the reservation status.
    *
    * @param con the Connection object used to interact with the database
    * @param reservation the reservation object of the customer 
    * @param reservationStatus the new status to be set for the reservationStatus
    */
    public static void updateReservationStatus(Connection con, Reservation reservation, String reservationStatus) {
        try (CallableStatement cstmt = con.prepareCall("{call update_reservation_status(?, ?)}")) {
            cstmt.setInt(1, reservation.getReservationID());
            cstmt.setString(2, reservationStatus);
            cstmt.execute();

            // Update the reservation status in the Java object
            reservation.setReservationStatus(reservationStatus);
            System.out.println("\nReservation status has been updated.");
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
    * Gets the customer ID for the given customer details from the Customer table in the database.
    * 
    * @param con the database connection
    * @param myScanner the scanner to read input from the user
    * @return the customer ID, or -1 if the customer is not found
    */
    public static int getCustomerID(Connection con, Scanner myScanner) {
        int customerID = -1;

        System.out.print("\nEnter customer's first name: ");
        String firstName = myScanner.nextLine();
        System.out.print("Enter customer's last name: ");
        String lastName = myScanner.nextLine();

        int membershipID;
        while (true) {
            try {
                System.out.print("Enter customer's membershipID: ");
                membershipID = myScanner.nextInt();
                myScanner.nextLine();

                // Ensure membershipID is greater than or equal to 0
                if (membershipID >= 0) {
                    break;
                } 
                else {
                    System.out.println("Invalid membershipID. Please enter a valid membershipID (greater than or equal to 0).");
                }
            } 
            catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number for the membershipID.");
                myScanner.nextLine(); // Clear the invalid input
            }
        }

        // Query to find the customer based on the given details
        String query = "SELECT customerID FROM Customer WHERE firstName = ? AND lastName = ? AND membershipID = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setInt(3, membershipID);

            ResultSet rs = pstmt.executeQuery();

            // If a customer is found, set the customerID
            if (rs.next()) {
                customerID = rs.getInt("customerID");
            }
        } 
        catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return customerID;
    }

    /**
    * This method prints the front desk agent menu and asks the user to enter an input option.
    * The method validates the user's input and returns it if valid.
    *
    * @param myScanner Scanner object to read input from user.
    * @return An integer representing the user's input option. Returns -1 if the input is invalid.
    */
    public static int printOptions(Scanner myScanner) {
        
        //validate the option
        boolean validOption = false;

        //useroption
        String userOption = "";

        //loop for options
        while(validOption==false){
            //print possible options
            System.out.println("\nFront Desk Agent Main Menu: \n");
            System.out.println( "1. Customer check-in\n" +
                            "2. Customer check-out \n");

            System.out.print("Enter an option (1 or 2): ");
            userOption = myScanner.nextLine();

            //validate the option
            if(userOption.matches("^[1-2]$")) {
                validOption = true;
            }
            else {
                System.out.println("Invalid option.");
            }
        }

        //return the option
        return Integer.parseInt(userOption);
    }
}
