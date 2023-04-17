import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BusinessManager {
    public static void mainMenu(Connection con, Statement s, Scanner myScanner) {

        //request ooption
        int option = printOptions(myScanner);
        
        //perform the operation
        if (option==1) {

        }
        else {
            //get the managerID
            int managerID = getManagerID(con, myScanner);

            //update rates function
            boolean ratesupdates = setRates(con,managerID,myScanner);

            //check if operations done
            if(ratesupdates) {
                System.out.println("\nThe rates has been updated/inserted correctly.");
            }
            else {
                System.out.println("The rate update failed.");
            }

        }

    }

    //function to set rate
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
                    } else {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i).toUpperCase(), rs.getObject(i));
            }
            result.add(row);
        }
        
        return result;
    }

    public static boolean isValidRoomRateIdAndRoomTypeId(List<Map<String, Object>> rows, int roomRateID, int roomTypeID) {
        boolean validRoomRateId = false;
        boolean validRoomTypeId = false;
    
        for (Map<String, Object> row : rows) {
            int currentRoomRateID = ((Number) row.get("ROOMRATEID")).intValue();
            int currentRoomTypeID = ((Number) row.get("ROOMTYPEID")).intValue();
    
            if (currentRoomRateID == roomRateID) {
                validRoomRateId = true;
            }
            if (currentRoomTypeID == roomTypeID) {
                validRoomTypeId = true;
            }
    
            // If both roomRateID and roomTypeID are valid, we can break the loop
            if (validRoomRateId && validRoomTypeId) {
                break;
            }
        }
    
        return validRoomRateId && validRoomTypeId;
    }

    //function to get new price
    public static double getPrice(Scanner myScanner) {
        double userInput;
        while (true) {
            try {
                userInput = Double.parseDouble(myScanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer or decimal value.");
            }
        }
        return userInput;
    }

    //function to get integer input from the user
    public static int getIntegerInput(Scanner myScanner) {
        int userInput;
        while (true) {
            try {
                String inp = myScanner.nextLine();
                userInput = Integer.parseInt(inp);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer value.");
            }
        }
        return userInput;
    }

    //function to request for managerID
    public static Integer getManagerID(Connection con, Scanner myScanner) {
        Integer managerID = null;
        boolean validInput = false;

        do {
            try {
                System.out.print("\nEnter the Manager ID: ");
                String input = myScanner.nextLine();
                managerID = Integer.parseInt(input);

                try (PreparedStatement stmt = con.prepareStatement("SELECT managerID FROM Manager WHERE managerID = ?")) {
                    stmt.setInt(1, managerID);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        validInput = true;
                    } else {
                        System.out.println("Invalid manager ID. Please try again.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer.");
                myScanner.nextLine(); // Discard invalid input
            }
        } while (!validInput);

        return managerID;
    }

    //print the menu and ask the business manager to enter the input
    public static int printOptions(Scanner myScanner) {
        
        //validate the option
        boolean validOption = false;

        //useroption
        String userOption = "";

        //loop for options
        while(validOption==false){
            //print possible options
            System.out.println("\nBusiness Manager Main Menu: \n");
            System.out.println( "1. View aggregate data over a period of time\n" +
                            "2. Set rates \n");

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
