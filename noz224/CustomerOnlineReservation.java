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
import java.util.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CustomerOnlineReservation {

    // mainMenu
    public static void mainMenu(Connection con, Statement s, Scanner myScanner) {

        // Request the arrival date of the reservation
        System.out.print("Enter the arrival date of your reservation (YYYY-MM-DD): ");
        String arrivalDate = getDate(myScanner);

        // Request the departure date of the reservation
        System.out.print("\nEnter the departure date of your reservation (YYYY-MM-DD): ");
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
                
                //get customerID
                int customerID = getCustomerID(myScanner);

                //check if the customerID is in the database
                int customerStatus = checkCustomerStatus(con, myScanner, customerID);
                
                //if found, retriee their info, else we create a new account for them
                if(customerStatus==1) {
                    
                    //print status of the customer
                    System.out.println("\nYour account was found.\n");

                    //retrieve the customer information
                    Customer customerInfor = getCustomerInformation(con, customerID);
                    
                    //retrieve card information
                    Card customerCardInfor = getcustomerCardInfo(con, customerID);

                    //check if the card has not expired
                    boolean validCard = false;
                    while(validCard==false) {
                        validCard = checkExpirationDateOfCard(customerCardInfor.getExpirationDate());
                        
                        if(validCard==false) {
                            System.out.println("\nYour card is not valid. It expired on "+ customerCardInfor.getExpirationDate());
                            System.out.println("\nUse a valid card : ");
                            //customerCardInfor = createNewCard(myScanner);
                        }
                    }

                    //calculate the cost of the customer stay based on the roomType and number of days
                    double bookingPrice = getBookingPrice(con, roomTypeID, arrivalDate, departureDate);

                    //get the number of points belonging to the user
                    int numOfPoints = getNumberOfPoints(con, customerID);

                    //get the number of points the user wants to use
                    int numOfPointsUsed = getNumberOfPointsUsed(myScanner,numOfPoints);

                    if (numOfPointsUsed > 0) {
                        
                        //fixed equivalent amount for each pooint
                        double pointsCost = 0.005;

                        //calculate the discount
                        double discount = calculateDiscount(numOfPointsUsed, pointsCost);
                        System.out.printf("\nThe discount is $%.2f%n\n", discount);

                        //deduct discount to reflect the new amount
                        bookingPrice = bookingPrice - discount;

                    }

                    //display the price
                    System.out.printf("\nYour total bill is $%.2f%n\n", bookingPrice);

                    //update the records

                    //ask if they want to use their points

                    //the trigger should automatically deduct 

                    //enter the information in necessary tables


                }
                else if(customerStatus==0) {
                    System.out.println("You account was  not found. Let us create a new account for you.");

                    // //request their personal information
                    // Customer newCustomer = createNewCustomer(con, myScanner);

                    // //request their card infrmation
                    // Card newCsutmerCadInfor = createNewCard(con, myScanner);

                    //calculate their bills

                    //get points

                    //ask if they want to use their points

                    //the trigger should automatically deduct 

                    //enter the information in necessary tables
                }
            }
        }
    }

    //function to calcluate the discount
    public static double calculateDiscount(int numOfPointsUsed, double pointsCost) {
        // Calculate the discount based on the number of points used and the fixed cost per point
        double discount = numOfPointsUsed * pointsCost;
        
        // Return the discount rounded to two decimal places
        return Math.round(discount * 100.0) / 100.0;
    }

    // Function that returns the number of points the user wants to use
    public static int getNumberOfPointsUsed(Scanner myScanner, int availablePoints) {
        int pointsToUse = 0;

        // Ask the user if they want to use any of their points
        System.out.print("\nDo you want to use any of your points to pay the bill? (Y/N): ");
        String userInput = getYesOrNoInput(myScanner);

        // If the user wants to use their points
        if (userInput.equals("Y")) {
            // Keep prompting the user until a valid number of points is entered
            while (true) {
                System.out.printf("\nHow many points do you want to use? (1-%d): ", availablePoints);
                try {
                    // Parse the user input as an integer
                    pointsToUse = Integer.parseInt(myScanner.nextLine());

                    // Check if the entered number is between 1 and the available points
                    if (pointsToUse >= 1 && pointsToUse <= availablePoints) {
                        break;
                    } else {
                        System.out.printf("\nInvalid input. Please enter a number between 1 and %d.%n inclusive", availablePoints);
                    }
                } catch (NumberFormatException e) {
                    // If the input is not a valid number, inform the user
                    System.out.println("\nInvalid input. Please enter a valid number.");
                }
            }
        }

        // Return the number of points the user wants to use
        return pointsToUse;
    }


    //function to get the number of points belonging to the user
    public static int getNumberOfPoints(Connection con, int customerID) {
        int numOfPoints = 0;

        // Query to retrieve the membershipID of the customer based on their customerID
        String query = "SELECT membershipID FROM Customer WHERE customerID = ?";

        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, customerID);

            ResultSet rs = pstmt.executeQuery();

            // If a result is found, set the membershipID to the value in the database
            if (rs.next()) {
                int membershipID = rs.getInt("membershipID");

                // Query to retrieve the number of points of the customer based on their membershipID
                String pointsQuery = "SELECT points FROM Membership WHERE membershipID = ?";
                pstmt = con.prepareStatement(pointsQuery);
                pstmt.setInt(1, membershipID);

                ResultSet pointsRs = pstmt.executeQuery();

                // If a result is found, set the numOfPoints to the value in the database
                if (pointsRs.next()) {
                    numOfPoints = pointsRs.getInt("points");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the number of points
        return numOfPoints;
    }

    //function to calculate booking price
    public static double getBookingPrice(Connection con, int roomTypeID, String arrivalDate, String departureDate) {
        double price = 0;
        double defaultRate = 40;

        // Convert the arrivalDate and departureDate to LocalDate
        LocalDate arrivalLocalDate = LocalDate.parse(arrivalDate);
        LocalDate departureLocalDate = LocalDate.parse(departureDate);

        // Extract the room rates from the database based on the roomTypeID and the date range
        String query = "SELECT startDate, endDate, price FROM RoomRate WHERE roomTypeID = ? AND ((startDate BETWEEN ? AND ?) OR (endDate BETWEEN ? AND ?)) ORDER BY startDate ASC";

        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, roomTypeID);
            pstmt.setDate(2, java.sql.Date.valueOf(arrivalLocalDate));
            pstmt.setDate(3, java.sql.Date.valueOf(departureLocalDate));
            pstmt.setDate(4, java.sql.Date.valueOf(arrivalLocalDate));
            pstmt.setDate(5, java.sql.Date.valueOf(departureLocalDate));

            ResultSet rs = pstmt.executeQuery();

            LocalDate currentDate = arrivalLocalDate;

            while (currentDate.isBefore(departureLocalDate)) {
                if (rs.next()) {
                    LocalDate startDate = rs.getDate("startDate").toLocalDate();
                    LocalDate endDate = rs.getDate("endDate").toLocalDate();
                    double roomCost = rs.getDouble("price");

                    // Apply the default rate to the days before the startDate of this room rate
                    long daysBeforeRate = ChronoUnit.DAYS.between(currentDate, startDate);
                    price += daysBeforeRate * defaultRate;

                    // Calculate the intersection between the current date range and the date range of this room rate
                    LocalDate intersectionStart = startDate.isAfter(currentDate) ? startDate : currentDate;
                    LocalDate intersectionEnd = endDate.isBefore(departureLocalDate) ? endDate : departureLocalDate;

                    // Calculate the number of days in the intersection and update the price
                    long numberOfDays = ChronoUnit.DAYS.between(intersectionStart, intersectionEnd) + 1;
                    price += roomCost * numberOfDays;

                    // Update the currentDate to the day after the endDate of this room rate
                    currentDate = endDate.plusDays(1);
                } else {
                    // Apply the default rate to the remaining days
                    long remainingDays = ChronoUnit.DAYS.between(currentDate, departureLocalDate);
                    price += remainingDays * defaultRate;
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the price
        return price;
    }

    //function to convert date to local date
    public static LocalDate convertToLocalDate(Date date) {
        Timestamp timestamp = new Timestamp(date.getTime());
        return timestamp.toLocalDateTime().toLocalDate();
    }

    //function to check the experiation date of the card
    public static boolean checkExpirationDateOfCard(Date expirationDate) {
    
        LocalDate expDate = convertToLocalDate(expirationDate);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Check if the expiration date is after the current date
        if (expDate.isAfter(currentDate)) {
            // Card is still valid
            return true;
        } else {
            // Card has expired
            return false;
        }
    }

    //function to retrieve the existing customer card information
    public static Card getcustomerCardInfo(Connection con, int customerID) {
        Card card = new Card();
        String query = "SELECT CreditCard.* FROM CreditCard JOIN Customer ON CreditCard.cardID = Customer.cardID WHERE Customer.customerID = ?";
    
        // Using a try-with-resources statement to automatically close the PreparedStatement when finished
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, customerID); // Set the customerID parameter in the query
            ResultSet rs = stmt.executeQuery(); // Execute the query
    
            // If a card is found for the given customer ID, populate the Card object with the data
            if (rs.next()) {
                card.setCardID(rs.getInt("cardID"));
                card.setCardToken(rs.getString("cardToken"));
                card.setCardType(rs.getString("cardType"));
                card.setExpirationDate(rs.getDate("expirationDate"));
            } else {
                System.out.println("Card not found for the provided customerID.");
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving card information: " + e.getMessage());
        }
        return card;
    }

    //function to retrieve the existing customer information
    public static Customer getCustomerInformation(Connection con, int customerID) {
        Customer customer = new Customer();
        String query = "SELECT * FROM Customer WHERE customerID = ?";
    
        // Using a try-with-resources statement to automatically close the PreparedStatement when finished
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, customerID); // Set the customerID parameter in the query
            ResultSet rs = stmt.executeQuery(); // Execute the query
    
            // If a customer is found with the given ID, populate the Customer object with the data
            if (rs.next()) {
                customer.setCustomerID(rs.getInt("customerID"));
                customer.setFirstName(rs.getString("firstName"));
                customer.setLastName(rs.getString("lastName"));
                customer.setPhoneNumber(rs.getString("phoneNumber"));
                customer.setCardID(rs.getInt("cardID"));
                customer.setAddressID(rs.getInt("addressID"));
                customer.setMembershipID(rs.getInt("membershipID"));
            } else {
                System.out.println("Customer not found with the provided customerID.");
            }
        } catch (SQLException e) {
            System.out.println("Error while retrieving customer information: " + e.getMessage());
        }
        return customer;
    }

    //get customerID
    public static int getCustomerID(Scanner myScanner) {
        int inputCustomerID;
    
        System.out.print("\nEnter your Customer ID (must be an integer greater than or equal to 0): ");
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
            System.out.print("\nEnter the number of guests: ");
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
                System.out.println("\nThe available roomtypes are : ");
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


        System.out.print("Please enter a Room Type ID from the list above, ensuring that the number of guests does not exceed the maximum for the room type: ");

        int selectedRoomTypeID = -1;
        while (true) {
            selectedRoomTypeID = Integer.parseInt(myScanner.nextLine());
    
            if (!roomTypeMaxGuests.containsKey(selectedRoomTypeID)) {
                System.out.print("\nInvalid Room Type ID. Please enter a valid Room Type ID from the list above: ");
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
