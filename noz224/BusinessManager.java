import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BusinessManager {
    
    /**
    * Displays the main menu for the user, and processes the selected option.
    * 
    * @param con        The Connection object to be used for database interactions.
    * @param s          The Statement object to be used for executing SQL queries.
    * @param myScanner  The Scanner object to be used for reading user input.
    */
    public static void mainMenu(Connection con, Statement s, Scanner myScanner) {

        // Request user to choose an option
        int option = printOptions(myScanner);
        
        // Perform the selected operation
        if (option == 1) {

            java.sql.Date startDate;
            java.sql.Date endDate;
            do {
                // Get the start date from the user
                System.out.print("\nEnter the start date (yyyy-mm-dd): ");
                String startDateInput = getDate(myScanner);
                startDate = CustomerOnlineReservation.convertStringToSqlDate(startDateInput);

                // Get the end date from the user
                System.out.print("Enter the end date (yyyy-mm-dd): ");
                String endDateInput = getDate(myScanner);
                endDate = CustomerOnlineReservation.convertStringToSqlDate(endDateInput);

                // Validate that the end date is not earlier than the start date
                if (endDate.before(startDate)) {
                    System.out.println("End date must be equal to or greater than the start date. Please try again.");
                }
            } while (endDate.before(startDate));

            // Call aggregate functions to get occupancy rate and total revenues over the given time period
            getOccupancyRateOverTime(con, new java.sql.Timestamp(startDate.getTime()), new java.sql.Timestamp(endDate.getTime()));
            getTotalRevenuesOverTime(con, new java.sql.Timestamp(startDate.getTime()), new java.sql.Timestamp(endDate.getTime())); 

        } 
        else {
            // Get the manager ID from the user
            int managerID = getManagerID(con, myScanner);

            // Update room rates based on the manager's input
            boolean ratesupdates = setRates(con, managerID, myScanner);

            // Check if the rates were successfully updated and display a message to the user
            if (ratesupdates) {
                System.out.println("\nThe rates have been updated/inserted correctly.");
            } 
            else {
                System.out.println("The rate update failed.");
            }
        }
    }
    

    /**
    * Retrieves and displays the occupancy rate of each hotel between the given start and end dates.
    *
    * @param con        The Connection object to be used for database interactions.
    * @param startDate  The start date of the time period for which the occupancy rate should be calculated.
    * @param endDate    The end date of the time period for which the occupancy rate should be calculated.
    */
    public static void getOccupancyRateOverTime(Connection con, Timestamp startDate, Timestamp endDate) {
        // SQL query to retrieve hotel occupancy rates over the given time period
        String sql = "SELECT h.hotelID, h.hotelName AS name, COUNT(cr.roomID) AS occupied_rooms, COUNT(r.roomID) AS total_rooms, " +
                    "CAST(COUNT(cr.roomID) AS FLOAT) / COUNT(r.roomID) * 100 AS occupancy_rate " +
                    "FROM Hotel h " +
                    "JOIN Room r ON h.hotelID = r.hotelID " +
                    "LEFT JOIN CustomerRoom cr ON r.roomID = cr.roomID AND cr.checkInDate >= ? AND (cr.checkOutDate <= ? OR cr.checkOutDate IS NULL) " +
                    "GROUP BY h.hotelID, h.hotelName " +
                    "ORDER BY h.hotelID";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            // Set the start and end dates as parameters in the prepared statement
            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, endDate);

            // Execute the query and process the results
            ResultSet rs = stmt.executeQuery();

            // Print the header for the occupancy rates table
            System.out.println("\nOccupancy Rates Over Time:");
            System.out.printf("%-10s %-30s %-20s %-20s %-20s%n", "HotelID", "Hotel Name", "Occupied Rooms", "Total Rooms", "Occupancy Rate (%)");

            // Iterate through the result set and print the occupancy rates for each hotel
            while (rs.next()) {
                System.out.printf("%-10d %-30s %-20d %-20d %-20.2f%n",
                        rs.getInt("hotelID"), rs.getString("name"), rs.getInt("occupied_rooms"), rs.getInt("total_rooms"),
                        rs.getDouble("occupancy_rate"));
            }
        } 
        catch (SQLException e) {
            // Handle any exceptions that occur during the execution of the SQL query
            e.printStackTrace();
        }
    }


   /**
    * Retrieves and displays the total revenues of each hotel between the given start and end dates.
    *
    * @param con        The Connection object to be used for database interactions.
    * @param startDate  The start date of the time period for which the total revenues should be calculated.
    * @param endDate    The end date of the time period for which the total revenues should be calculated.
    */
    public static void getTotalRevenuesOverTime(Connection con, Timestamp startDate, Timestamp endDate) {
        // SQL query to retrieve the total revenues for each hotel over the given time period
        String sql = "SELECT h.hotelID, h.hotelName, SUM(p.amount) AS total_revenue " +
                    "FROM Hotel h " +
                    "JOIN Reservation r ON h.hotelID = r.hotelID " +
                    "JOIN Payment p ON r.reservationID = p.reservationID " +
                    "WHERE p.paymentDate >= ? AND p.paymentDate <= ? " +
                    "GROUP BY h.hotelID, h.hotelName " +
                    "ORDER BY h.hotelID";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            // Set the start and end dates as parameters in the prepared statement
            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, endDate);

            // Execute the query and process the results
            ResultSet rs = stmt.executeQuery();

            // Print the header for the total revenues table
            System.out.println("\nTotal Revenues Over Time:");
            System.out.printf("%-10s %-30s %-20s%n", "HotelID", "Hotel Name", "Total Revenue ($)");

            // Iterate through the result set and print the total revenues for each hotel
            while (rs.next()) {
                System.out.printf("%-10d %-30s %-20.2f%n",
                        rs.getInt("hotelID"), rs.getString("hotelName"), rs.getDouble("total_revenue"));
            }
        } 
        catch (SQLException e) {
            // Handle any exceptions that occur during the execution of the SQL query
            e.printStackTrace();
        }
    }


    /**
    * Prompts the user to enter a date and validates the input to ensure it is in the correct format.
    *
    * @param myScanner The Scanner object to be used for reading user input.
    * @return A valid date in the format "yyyy-MM-dd" as a String.
    */
    public static String getDate(Scanner myScanner) {
        // Set the desired date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String date;

        // Keep asking the user for a date input until a valid date is provided
        while (true) {
            try {
                // Read user input and attempt to parse it as a LocalDate
                date = myScanner.nextLine();
                LocalDate parsedDate = LocalDate.parse(date, formatter);
                // If the date is valid, break out of the loop
                break;
            } 
            catch (DateTimeParseException e) {
                // If the input is not in the correct format, show an error message and ask for
                // input again
                System.out.println("Invalid date format. Please enter a valid date in the format YYYY-MM-DD.");
            }
        }

        // Return the valid date as a String
        return date;
    }

    /**
    * Sets room rates based on manager input.
    *
    * @param con       The Connection object to be used for database interactions.
    * @param managerID The manager ID to be used for verifying room rates and room types.
    * @param myScanner The Scanner object to be used for reading user input.
    * @return A boolean value representing whether the rate update was successful.
    */
    public static boolean setRates(Connection con, int managerID, Scanner myScanner) {
        try {
            // Get hotelID from the manager
            int hotelID;
            try (PreparedStatement stmt = con.prepareStatement("SELECT hotelID FROM Manager WHERE managerID = ?")) {
                stmt.setInt(1, managerID);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    return false;
                }
                hotelID = rs.getInt("hotelID");
            }

            int roomRateID;
            int roomTypeID;

            // Get all room types and room rate IDs for the specific hotel
            try (PreparedStatement stmt = con.prepareStatement(
                "SELECT m.managerID, h.hotelID, rt.roomTypeID, rt.description, rr.roomRateID, rr.price, rr.startDate, rr.endDate " +
                "FROM Hotel h, Room r, RoomType rt, RoomRate rr, Manager m " +
                "WHERE h.hotelID = r.hotelID AND r.roomTypeID = rt.roomTypeID AND rt.roomTypeID = rr.roomTypeID AND h.hotelID = m.hotelID AND m.hotelID = ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

                stmt.setInt(1, hotelID);
                ResultSet rs = stmt.executeQuery();
                
                // Print room types and room rates for the specific hotel
                System.out.println("\nRoom Types and Room Rates for your Hotel:");
                System.out.printf("%-10s %-10s %-10s %-20s %-10s %-10s %-12s %-12s%n", "ManagerID", "HotelID", "RoomTypeID", "Description", "RoomRateID", "Price", "Start Date", "End Date");
                while (rs.next()) {
                    System.out.printf("%-10d %-10d %-10d %-20s %-10d %-10.2f %-12tF %-12tF%n",
                            rs.getInt("managerID"), rs.getInt("hotelID"), rs.getInt("roomTypeID"), rs.getString("description"), rs.getInt("roomRateID"),
                            rs.getDouble("price"), rs.getDate("startDate"), rs.getDate("endDate"));
                }

                // Close the first ResultSet and create a new PreparedStatement and ResultSet for validation
                rs.close();
                PreparedStatement stmtValidation = con.prepareStatement(
                    "SELECT m.managerID, h.hotelID, rt.roomTypeID, rt.description, rr.roomRateID, rr.price, rr.startDate, rr.endDate " +
                    "FROM Hotel h, Room r, RoomType rt, RoomRate rr, Manager m " +
                    "WHERE h.hotelID = r.hotelID AND r.roomTypeID = rt.roomTypeID AND rt.roomTypeID = rr.roomTypeID AND h.hotelID = m.hotelID AND m.hotelID = ?");
                stmtValidation.setInt(1, hotelID);
                ResultSet rsValidation = stmtValidation.executeQuery();
                List<Map<String, Object>> rows = resultSetToList(rsValidation);
                rsValidation.close();
                stmtValidation.close();

                // Loop until a valid room rate ID and room type ID are provided
                while (true) {
                    // Get the room rate ID from the manager
                    System.out.print("\nEnter the Room Rate ID: ");
                    roomRateID = getIntegerInput(myScanner);

                    // Get the room type ID from the manager
                    System.out.print("Enter the Room Type ID: ");
                    roomTypeID = getIntegerInput(myScanner);

                    // Check if the rateId and roomtypeID are present in the results returned by the query
                    if (isValidRoomRateIdAndRoomTypeId(rows, roomRateID, roomTypeID)) {
                        break;
                    } 
                    else {
                        System.out.println("Invalid Room Rate ID or Room Type ID. Please try again.");
                    }
                }
            }

            // Get the start date, end date, and price from the manager
            java.sql.Date startDate;
            java.sql.Date endDate;
            do {
                System.out.print("Enter the start date (yyyy-mm-dd): ");
                String startDateInput = CustomerOnlineReservation.getDate(myScanner);
                startDate = CustomerOnlineReservation.convertStringToSqlDate(startDateInput);

                System.out.print("Enter the end date (yyyy-mm-dd): ");
                String endDateInput = CustomerOnlineReservation.getDate(myScanner);
                endDate = CustomerOnlineReservation.convertStringToSqlDate(endDateInput);

                if (endDate.before(startDate)) {
                    System.out.println("End date must be equal to or greater than the start date. Please try again.");
                }
            } while (endDate.before(startDate));

            System.out.print("Enter the price: ");
            double price = getPrice(myScanner);

            // Call the stored procedure to upsert the room rate
            try (CallableStatement stmt = con.prepareCall("{call upsert_room_rate(?, ?, ?, ?, ?, ?, ?)}")) {
                stmt.setInt(1, roomRateID);
                stmt.setInt(2, roomTypeID);
                stmt.setInt(3, managerID);
                stmt.setDate(4, startDate);
                stmt.setDate(5, endDate);
                stmt.setDouble(6, price);
                stmt.registerOutParameter(7, Types.INTEGER);
                stmt.executeUpdate();
                int success = stmt.getInt(7);
                if (success == 1) {
                    return true;
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
    * Converts a ResultSet to a List of Maps.
    *
    * @param rs The ResultSet object to be converted.
    * @return A List of Maps representing the ResultSet, where each map contains column names as keys
    *         and the corresponding values as values.
    * @throws SQLException If a database access error occurs.
    */
    public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        // Create a new ArrayList to store the result
        List<Map<String, Object>> result = new ArrayList<>();

        // Get metadata from the ResultSet
        ResultSetMetaData md = rs.getMetaData();
        // Get the column count from the metadata
        int columns = md.getColumnCount();

        // Iterate through the ResultSet
        while (rs.next()) {
            // Create a new HashMap to store the row data
            Map<String, Object> row = new HashMap<>(columns);
            // Iterate through the columns
            for (int i = 1; i <= columns; ++i) {
                // Add the column name and value to the row map
                row.put(md.getColumnName(i).toUpperCase(), rs.getObject(i));
            }
            // Add the row map to the result list
            result.add(row);
        }

        // Return the result list
        return result;
    }


    /**
    * Checks if the given room rate ID and room type ID are valid based on the provided rows.
    *
    * @param rows A List of Maps representing rows from the ResultSet, where each map contains
    *             column names as keys and the corresponding values as values.
    * @param roomRateID The room rate ID to be checked for validity.
    * @param roomTypeID The room type ID to be checked for validity.
    * @return A boolean value indicating whether the given room rate ID and room type ID are valid.
    */
    public static boolean isValidRoomRateIdAndRoomTypeId(List<Map<String, Object>> rows, int roomRateID, int roomTypeID) {
        boolean validRoomRateId = false;
        boolean validRoomTypeId = false;

        // Iterate through the rows
        for (Map<String, Object> row : rows) {
            // Get the current room rate ID and room type ID
            int currentRoomRateID = ((Number) row.get("ROOMRATEID")).intValue();
            int currentRoomTypeID = ((Number) row.get("ROOMTYPEID")).intValue();

            // Check if the given room rate ID matches the current one
            if (currentRoomRateID == roomRateID) {
                validRoomRateId = true;
            }
            // Check if the given room type ID matches the current one
            if (currentRoomTypeID == roomTypeID) {
                validRoomTypeId = true;
            }

            // If both roomRateID and roomTypeID are valid, we can break the loop
            if (validRoomRateId && validRoomTypeId) {
                break;
            }
        }

        // Return true if both room rate ID and room type ID are valid, otherwise false
        return validRoomRateId && validRoomTypeId;
    }


    /**
    * Gets a price from the user input.
    *
    * @param myScanner The Scanner object used to read user input.
    * @return A double representing the price entered by the user.
    */
    public static double getPrice(Scanner myScanner) {
        double userInput;

        // Keep asking the user for a price input until a valid price is provided
        while (true) {
            try {
                // Read user input and attempt to parse it as a double
                userInput = Double.parseDouble(myScanner.nextLine());
                break;
            } 
            catch (NumberFormatException e) {
                // If the input is not a valid integer or decimal value, show an error message and ask for input again
                System.out.println("Invalid input. Please enter a valid integer or decimal value.");
            }
        }

        // Return the valid price as a double
        return userInput;
    }


    /**
    * Gets an integer input from the user.
    *
    * @param myScanner The Scanner object used to read user input.
    * @return An int representing the integer value entered by the user.
    */
    public static int getIntegerInput(Scanner myScanner) {
        int userInput;

        // Keep asking the user for an integer input until a valid integer is provided
        while (true) {
            try {
                // Read user input and attempt to parse it as an integer
                String inp = myScanner.nextLine();
                userInput = Integer.parseInt(inp);
                break;
            } 
            catch (NumberFormatException e) {
                // If the input is not a valid integer value, show an error message and ask for input again
                System.out.println("Invalid input. Please enter a valid integer value.");
            }
        }

        // Return the valid integer as an int
        return userInput;
    }


    /**
    * Requests the user for a manager ID and validates it.
    *
    * @param con       The Connection object used to connect to the database.
    * @param myScanner The Scanner object used to read user input.
    * @return An Integer representing the valid manager ID entered by the user.
    */
    public static Integer getManagerID(Connection con, Scanner myScanner) {
        Integer managerID = null;
        boolean validInput = false;

        // Keep asking the user for a manager ID until a valid manager ID is provided
        do {
            try {
                System.out.print("\nEnter the Manager ID: ");
                String input = myScanner.nextLine();
                managerID = Integer.parseInt(input);

                // Validate the manager ID by querying the database
                try (PreparedStatement stmt = con.prepareStatement("SELECT managerID FROM Manager WHERE managerID = ?")) {
                    stmt.setInt(1, managerID);
                    ResultSet rs = stmt.executeQuery();

                    // If the manager ID exists in the database, set validInput to true
                    if (rs.next()) {
                        validInput = true;
                    } 
                    else {
                        System.out.println("Invalid manager ID. Please try again.");
                    }
                } 
                catch (SQLException e) {
                    e.printStackTrace();
                }
            } 
            catch (InputMismatchException e) {
                // If the input is not a valid integer value, show an error message and discard invalid input
                System.out.println("Invalid input. Please enter an integer.");
                myScanner.nextLine();
            }
        } while (!validInput);

        // Return the valid manager ID as an Integer
        return managerID;
    }


    /**
    * Prints the main menu options for the business manager and requests input.
    *
    * @param myScanner The Scanner object used to read user input.
    * @return An integer representing the chosen option by the business manager.
    */
    public static int printOptions(Scanner myScanner) {

        // Flag to track whether the user has entered a valid option
        boolean validOption = false;

        // Variable to store the user's option
        String userOption = "";

        // Loop until a valid option is provided
        while (validOption == false) {
            // Print possible options
            System.out.println("\nBusiness Manager Main Menu: \n");
            System.out.println("1. View aggregate data over a period of time\n" +
                            "2. Set rates \n");

            System.out.print("Enter an option (1 or 2): ");
            userOption = myScanner.nextLine();

            // Validate the option
            if (userOption.matches("^[1-2]$")) {
                validOption = true;
            } 
            else {
                System.out.println("Invalid option.");
            }
        }

        // Return the valid option as an integer
        return Integer.parseInt(userOption);
    }
}
