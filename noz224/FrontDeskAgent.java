import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;

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

            //check if the customer has a reservation
            Reservation reservationDetails = getReservation(con, customerId);

            //if rervation is found, pull the room type, and hotelID
            if(reservationDetails.getReservationID()==-1 || !reservationDetails.getReservationStatus().equals("Confirmed")) {
                System.out.println("\nThe customer has no confirmed reservation\n");
            }
            else {
                //perform the actions according to check out or check in
                switch(userOption) {
                    case 1:
                        //prompt the user to see if they want to change the roomtype
                        System.out.print("\nDo you want to change your room type?: ");
                        String changeRoomType = CustomerOnlineReservation.getYesOrNoInput(myScanner);
                        if(changeRoomType.equals("Y")) {
                            reservationDetails = CustomerOnlineReservation.changeRoomType(con, myScanner, reservationDetails);
                        }
    
                        if(checkIn(con, reservationDetails, "Occupied", myScanner)) {
                            System.out.println("\nThe customer has successfully checked into the room.");
                        }
                        else {
                            System.out.println("\nThe check-in has failed");
                        }
                        break;
                    case 2:
                        if(checkOut(con, reservationDetails, "Ready to be cleaned")) {
                            System.out.println("\nThe customer has successfully checked out of the room.");
                        }
                        else {
                            System.out.println("\nThe check out has failed");
                        }
                        break;
                }
            } 
        }
    }

    //function to perform check-out
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
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Retrieve the roomID from the CustomerRoom table using the reservationID
        try (PreparedStatement stmt2 = con.prepareStatement("SELECT roomID FROM CustomerRoom WHERE reservationID = ? AND checkOutDate IS NULL")) {
            stmt2.setInt(1, reservationId);
            try (ResultSet rs2 = stmt2.executeQuery()) {
                if (rs2.next()) {
                    roomId = rs2.getInt("roomID");
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
                    // Enable auto-commit again (if required)
                    con.setAutoCommit(true);
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    //function to perform check in
    public static boolean checkIn(Connection con, Reservation reservation, String roomStatus, Scanner myScanner) {
        try {
            // Get the hotel ID and roomTypeID from the reservation object
            int hotelID = reservation.getHotelID();
            int roomTypeID = reservation.getRoomTypeID();
            System.out.println("Room type is :" + roomTypeID);
            int roomID;
    
            // Identify the hotel from the hotel table
            String hotelQuery = "SELECT * FROM Hotel WHERE hotelID = ?";
            try (PreparedStatement hotelStmt = con.prepareStatement(hotelQuery)) {
                hotelStmt.setInt(1, hotelID);
    
                try (ResultSet hotelResult = hotelStmt.executeQuery()) {
                    if (!hotelResult.next()) {
                        System.out.println("Hotel not found.");
                        return false;
                    } else {
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
                        System.out.println("No rooms found for the given room type in the specified hotel.");
                        return false;
                    } else {
                        roomID = roomTypeResult.getInt("roomID");
                        int roomNumber = roomTypeResult.getInt("roomNumber");
                        System.out.println("Room ID: " + roomID);
                        System.out.println("Room Number: " + roomNumber);
                    }
                }
            }
            System.out.println("Pass 10");

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
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    //function to get core reservation details of the customer
    public static Reservation getReservation(Connection con, int customerID) {
        Reservation reservation = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
    
        try {
            String sql = "SELECT * FROM Reservation WHERE customerID = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, customerID);
            rs = pstmt.executeQuery();
    
            if (rs.next()) {
                int reservationID = rs.getInt("reservationID");
                int hotelID = rs.getInt("hotelID");
                int roomTypeID = rs.getInt("roomTypeID");
                int numberOfGuests = rs.getInt("numberOfGuests");
                Date arrivalDate = rs.getDate("arrivalDate");
                Date departureDate = rs.getDate("departureDate");
                String reservationStatus = rs.getString("reservationStatus");
    
                reservation = new Reservation(reservationID, customerID, hotelID, roomTypeID, numberOfGuests, arrivalDate, departureDate, reservationStatus);
            } else {
                reservation = new Reservation(-1, customerID, -1, -1, -1, null, null, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
        return reservation;
    }
    

    //function to get customerID
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
                } else {
                    System.out.println("Invalid membershipID. Please enter a valid membershipID (greater than or equal to 0).");
                }
            } catch (InputMismatchException e) {
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
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return customerID;
    }

    //print the menu and assk the front desk agent to enter the input
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
