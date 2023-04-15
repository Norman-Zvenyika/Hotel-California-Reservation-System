import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map.Entry;

public class CustomerOnlineReservation {

    // mainMenu
    public static void mainMenu(Connection con, Statement s, Scanner myScanner) {

        // Request the arrival date of the reservation
        System.out.println("Enter the arrival date of your reservation (YYYY-MM-DD)");
        String arrivalDate = getDate(myScanner);

        // Request the departure date of the reservation
        System.out.println("Enter the departure date of your reservation (YYYY-MM-DD)");
        String departureDate = getDate(myScanner);

        // Enforce that the departure date is greater than or equal to the arrival date
        while (LocalDate.parse(departureDate).isBefore(LocalDate.parse(arrivalDate))) {
            System.out.println(
                    "The departure date must be greater than or equal to the arrival date. Please enter a new departure date (YYYY-MM-DD):");
            System.out.println("Enter the arrival date of your reservation (YYYY-MM-DD)");
            arrivalDate = getDate(myScanner);
            System.out.println("Enter the departure date of your reservation (YYYY-MM-DD)");
            departureDate = getDate(myScanner);
        }

        //variables for getting the hotelID
        int userHotelID;
        boolean tryAgain;
        boolean validHotelID = false;

        do {
            // Get the hotelID where there are free rooms during the given date range
            userHotelID = getHotelID(con, myScanner, arrivalDate, departureDate);

            // Check if the hotelID is valid
            if (userHotelID == -4) {
                System.out.println("There are no available hotel rooms during the specified period.");
                System.out.println("Do you want to try again with different dates? (Y/N)");
                String userResponse = getYesOrNoInput(myScanner);

                if ("Y".equalsIgnoreCase(userResponse)) {
                    tryAgain = true;
                    System.out.println("Enter the arrival date of your reservation (YYYY-MM-DD):");
                    arrivalDate = getDate(myScanner);
                    System.out.println("Enter the departure date of your reservation (YYYY-MM-DD):");
                    departureDate = getDate(myScanner);
                } else {
                    tryAgain = false;
                }
            } else {
                tryAgain = false;
                validHotelID = true;
            }
        } while (tryAgain);

        // display the hotel and available room types
        if(validHotelID) {
            
            //variables for number of guests and roomTypeID
            int numberOfGuests = 0;
            int roomTypeID  = -1;

            while(true) {
                
                //request for the number of guests that the user has. Repate until the user enters a valid integer
                numberOfGuests = getNumberOfGuests(myScanner);

                //request roomTypeID based on the available hotel
                roomTypeID = getRoomTypeID(con, myScanner, userHotelID, arrivalDate, departureDate, numberOfGuests);

                //check if the user wants to change the number of guests
                if(roomTypeID!=-4) {
                    break;
                }
            }

            //check if the roomTypeID is valid
            if(roomTypeID>=0) {

                //request customer ID
                System.out.println("Enter customerID: ");
                
                //arbitrary customerID
                int customerID = getCustomerID(myScanner);

                //check if the customerID is in the database
                int customerStatus = checkCustomerStatus(con, myScanner, customerID);
                if(customerStatus==1) {
                    
                    //print status of the customer
                    System.out.println("You account was found.");

                    //retrieve the customer information

                    //calculate their bills

                    //get points

                    //ask if they want to use their points

                    //the trigger should automatically deduct 

                    //enter the information in necessary tables


                }
                else if(customerStatus==0) {
                    System.out.println("You account was  not found. Let us create a new account for you.");

                    //request their personal information

                    //ask for frequent membership

                    //calculate their bills

                    //get points

                    //ask if they want to use their points

                    //the trigger should automatically deduct 

                    //enter the information in necessary tables
                }
            }
        }
    }

    //get customerID
    public static int getCustomerID(Scanner myScanner) {
        int inputCustomerID;
    
        System.out.println("Please enter your Customer ID (must be an integer greater than or equal to 0):");
        while (true) {
            try {
                inputCustomerID = Integer.parseInt(myScanner.nextLine());
                if (inputCustomerID >= 0) {
                    break;
                } else {
                    System.out.println("Customer ID must be greater than or equal to 0. Please enter a valid Customer ID:");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid Customer ID:");
            }
        }
    
        return inputCustomerID;
    }

    // check if the customer is new or old
    public static int checkCustomerStatus(Connection con, Scanner myScanner, int inputCustomerID) {
    
        // Check if the customer exists in the table using the provided customerID
        String checkCustomerQuery = "SELECT * FROM Customer WHERE customerID = ?";
        try (PreparedStatement pstmt = con.prepareStatement(checkCustomerQuery)) {
            pstmt.setInt(1, inputCustomerID);
    
            // Execute the query and process the results
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // The customer is found in the table
                    return 1;
                } else {
                    // The customer is not found in the table
                    return 0;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return -100;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -100;
        }
    }
    
    

    //function to get the number of guests
    public static int getNumberOfGuests(Scanner myScanner) {
        int numberOfGuests;
        while (true) {
            // Prompt user to enter the number of guests
            System.out.print("\nEnter the number of guests:");
            String input = myScanner.nextLine();
            try {
                numberOfGuests = Integer.parseInt(input);
                // Check if the number of guests is a positive integer
                if (numberOfGuests <= 0) {
                    System.out.println("\nNumber of guests must be a positive integer. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a positive integer.");
            }
        }
        // Return the number of guests entered by the user
        return numberOfGuests;
    }

    // Get the hotelID located with free reservations, located in the city chosen by user
    public static int getHotelID(Connection con, Scanner myScanner, String arrivalDate, String departureDate) {

        // Create a map to store city names and a list of hotel IDs and names within each city
        Map<String, List<Entry<Integer, String>>> cityHotelMap = new HashMap<>();

        // query to get the cities with free rooms during a particular period
        String citiesQuery = "WITH reserved_roomtypes AS (" +
        "    SELECT rr.roomTypeID" +
        "    FROM Reservation rr" +
        "    WHERE (rr.arrivalDate <= ? AND rr.departureDate >= ?)" +
        ")," +
        "available_rooms AS (" +
        "    SELECT r.roomID, r.roomTypeID, r.hotelID" +
        "    FROM Room r" +
        "    JOIN RoomType rt ON r.roomTypeID = rt.roomTypeID" +
        "    WHERE r.roomStatus = 'Available' AND r.roomTypeID NOT IN (SELECT * FROM reserved_roomtypes)" +
        ")" +
        "SELECT h.hotelID, h.hotelName, a.city " + 
        "FROM available_rooms ar " + 
        "JOIN Hotel h ON ar.hotelID = h.hotelID " +
        "JOIN Address a ON h.addressID = a.addressID " +
        "ORDER BY h.hotelName";

        //prepare a statement to get all cities of hotels with free rooms during a particular period
        try (PreparedStatement pstmt = con.prepareStatement(citiesQuery)) {

            // Set arrivalDate and departureDate in the prepared statement
            pstmt.setDate(1, java.sql.Date.valueOf(arrivalDate));
            pstmt.setDate(2, java.sql.Date.valueOf(departureDate));
        
            // Execute the query and process the results
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Get city, hotelID, and hotelName from the current row
                    String cityName = rs.getString("city");
                    int hotelID = rs.getInt("hotelID");
                    String hotelName = rs.getString("hotelName");
        
                    // If city not in cityHotelMap, create a new list and add it
                    if (!cityHotelMap.containsKey(cityName)) {
                        cityHotelMap.put(cityName, new ArrayList<>());
                    }
                    // Add hotel ID and name to the list for this city
                    cityHotelMap.get(cityName).add(new SimpleEntry<>(hotelID, hotelName));
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
                return 0;
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }

        // Check if there are no available hotel rooms during the specified period
        if (cityHotelMap.isEmpty()) {
            System.out.println("There are no available hotel rooms during the specified period.");
            return -4;
        }

        // Display the list of cities with available rooms
        System.out.print("\nThe cities with available rooms during your reservation period are: \n");
        for (String cityName : cityHotelMap.keySet()) {
            System.out.println(cityName);
        }

        // Prompt user to enter a city and validate their input
        System.out.print("\nEnter the name of the city from the given list: ");
        String city = myScanner.nextLine();
        while (!cityHotelMap.containsKey(city)) {
            System.out.println("Invalid city. Please enter a city from the given list: ");
            city = myScanner.nextLine();
        }

        // Get the list of hotels for the selected city
        List<Entry<Integer, String>> hotelList = cityHotelMap.get(city);

        // Display the list of hotels in the selected city and prompt user to select a hotel
        System.out.println("\nAvailable hotels in " + city + ":");
        for (Entry<Integer, String> hotel : hotelList) {
            System.out.println("Hotel ID: " + hotel.getKey() + ", Hotel Name: " + hotel.getValue());
        }

        //request hotelID from the user
        System.out.print("\nEnter the hotel ID from the list: ");
        AtomicInteger selectedHotelID = new AtomicInteger(myScanner.nextInt());
        myScanner.nextLine(); // Consume newline character

        // Validate the entered hotel ID
        while (hotelList.stream().noneMatch(hotel -> hotel.getKey() == selectedHotelID.get())) {
            System.out.print("Invalid hotel ID. Please enter a hotel ID from the list: ");
            selectedHotelID.set(Integer.parseInt(myScanner.nextLine()));
        }

        // Return the selected hotel ID
        return selectedHotelID.get();
    }

    // get room types available in a particular hotel
    public static int getRoomTypeID(Connection con, Scanner myScanner, int hotelID, String arrivalDate, String departureDate, int numberOfGuests) {

        Map<Integer, Integer> roomTypeMaxGuests = new HashMap<>();

        // display all room types available in the hotel during the particular time period
        String roomTypesQuery = "WITH reserved_roomtypes AS (" +
        "    SELECT rr.roomTypeID" +
        "    FROM Reservation rr" +
        "    WHERE (rr.arrivalDate <= TO_DATE(?, 'YYYY-MM-DD') AND rr.departureDate >= TO_DATE(?, 'YYYY-MM-DD')) AND rr.hotelID = ?" +
        ")," +
        "available_rooms AS (" +
        "    SELECT r.roomID, r.roomTypeID, r.hotelID, rt.description, rt.maxGuests" +
        "    FROM Room r" +
        "    JOIN RoomType rt ON r.roomTypeID = rt.roomTypeID" +
        "    WHERE r.roomStatus = 'Available' AND r.roomTypeID NOT IN (SELECT * FROM reserved_roomtypes) AND r.hotelID = ?" +
        ")" + 
        "SELECT DISTINCT ar.roomTypeID, ar.description, ar.maxGuests " +
        "FROM available_rooms ar " +
        "ORDER BY ar.roomTypeID";

        // Prepare the statement to view the available roomtypes
        try (PreparedStatement pstmt = con.prepareStatement(roomTypesQuery)) {
            // Set the parameters
            pstmt.setString(1, arrivalDate);
            pstmt.setString(2, departureDate);
            pstmt.setInt(3, hotelID);
            pstmt.setInt(4, hotelID);

             // Execute the query and process the results
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Get the room type details from the current row
                    int roomTypeID = rs.getInt("roomTypeID");
                    String description = rs.getString("description");
                    int maxGuests = rs.getInt("maxGuests");

                    // Store the maxGuests for each roomTypeID
                    roomTypeMaxGuests.put(roomTypeID, maxGuests);

                    // Print the room type details
                    System.out.println("\nRoom Type ID: " + roomTypeID);
                    System.out.println("Description: " + description);
                    System.out.println("Max Guests: " + maxGuests);
                    System.out.println();
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }


        System.out.print("Please enter a Room Type ID from the list above, ensuring that the number of guests does not exceed the maximum for the room type:");

        int selectedRoomTypeID = -1;
        while (true) {
            selectedRoomTypeID = Integer.parseInt(myScanner.nextLine());
    
            if (!roomTypeMaxGuests.containsKey(selectedRoomTypeID)) {
                System.out.println("\nInvalid Room Type ID. Please enter a valid Room Type ID from the list above:");
            } else if (numberOfGuests > roomTypeMaxGuests.get(selectedRoomTypeID)) {
                System.out.println("The number of guests exceeds the maximum allowed for the selected room type. Please enter a different Room Type ID:");
                System.out.print("Do you want to change your number of guests? ");
                String userResponse = getYesOrNoInput(myScanner);
                if(userResponse.equals("Y")) {
                    return -4;
                }
                else {
                    break;
                }
            } else {
                break;
            }
        }
    
        return selectedRoomTypeID;
    }

    //function for getting yes or no input from the user
    public static String getYesOrNoInput(Scanner myScanner) {
        String userInput;
        while (true) {
            userInput = myScanner.nextLine().trim().toUpperCase();
    
            if (userInput.equals("Y") || userInput.equals("N")) {
                break;
            } else {
                System.out.println("Invalid input. Please enter 'Y' or 'N':");
            }
        }
        return userInput;
    }

    // create a new customer
    public static boolean createNewCustomer() {
        // request the information of a customer
        // add the customer to the list of customers
        return true;
    }

    // make a reservation for a specific customer
    public static boolean makeReservation() {

        // fetch customer information

        // check whether they have points or not

        // make the payment

        // intiate a reservation

        // display whether the reervation was successful or not
        return true;
    }

    // get the number of points the particular customer has
    public static int getNumberOfPoints() {

        // get the customer information

        // get the number of points

        // return the number of points
        return 0;
    }

    // calculate the quivalent of the amount of points
    public static double getAmountEquivalentToPoints() {
        // calculate the amount based on the number of points * fixed rate

        // return the amount
        return 0;
    }

    // function to get date
    public static String getDate(Scanner myScanner) {
        // set date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // get current date
        LocalDate currentDate = LocalDate.now();

        String date = "";
        boolean validDate = false;

        // Keep asking the user for a date input until a valid date is provided
        while (!validDate) {

            try {
                // Read user input and attempt to parse it as a LocalDate
                date = myScanner.nextLine();
                LocalDate parsedDate = LocalDate.parse(date, formatter);

                // Check if the parsed date is before the current date
                if (parsedDate.isBefore(currentDate)) {
                    System.out.println(
                            "The entered date is in the past. Please enter a date greater than or equal to the current date.");
                } else {
                    // If the date is valid, set validDate to true to exit the loop
                    validDate = true;
                }
            } catch (DateTimeParseException e) {
                // If the input is not in the correct format, show an error message and ask for
                // input again
                System.out.println("Invalid date format. Please enter a valid date in the format YYYY-MM-DD.");
            }
        }

        // Return the valid date as a String
        return date;
    }
}

