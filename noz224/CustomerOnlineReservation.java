import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

public class CustomerOnlineReservation {

    // mainMenu
    public static void mainMenu(Connection con, Statement s, Scanner myScanner) {

        //boolean for completing reservation
        boolean reservationComplete = false;

        // Request the arrival date of the reservation
        System.out.print("Enter the arrival date of your reservation (YYYY-MM-DD): ");
        String arrivalDate = getDate(myScanner);

        // Request the departure date of the reservation
        System.out.print("\nEnter the departure date of your reservation (YYYY-MM-DD): ");
        String departureDate = getDate(myScanner);

        // Enforce that the departure date is greater than or equal to the arrival date
        while (LocalDate.parse(departureDate).isBefore(LocalDate.parse(arrivalDate))) {
            System.out.println(
                    "The departure date must be greater than or equal to the arrival date.");
            System.out.print("Enter the arrival date of your reservation (YYYY-MM-DD): ");
            arrivalDate = getDate(myScanner);
            System.out.print("Enter the departure date of your reservation (YYYY-MM-DD): ");
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
                System.out.print("Do you want to try again with different dates? (Y/N): ");
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

                    //complete the reservation
                    reservationComplete  = makeReservation(con,customerID,arrivalDate,departureDate,roomTypeID, myScanner, numberOfGuests, userHotelID); 


                }
                else if(customerStatus==0) {
                    
                    System.out.print("\nYour account was not found. Do you want to create an account with us? (Y/N): ");
                    
                    String createAcc = getYesOrNoInput(myScanner);

                    if(createAcc.equals("Y")) {
                        //request their personal information
                        int newCustomerID = createNewCustomer(con, myScanner);

                        System.out.println("\nYou a now a customer of Hotel California!\n");

                        System.out.println("Here are your customer details: ");
                        Customer customerInfo = getCustomerInformation(con,newCustomerID);
                        System.out.println("First Name: "+ customerInfo.getFirstName());
                        System.out.println("Last Name: "+ customerInfo.getLastName());
                        System.out.println("CustomerID: "+ customerInfo.getCustomerID());
                        System.out.println("MembershipID: "+ customerInfo.getMembershipID());
                        System.out.println("Phone number: "+ customerInfo.getPhoneNumber());
                        System.out.println("");

                        //complete the reservation
                        reservationComplete  = makeReservation(con,newCustomerID,arrivalDate,departureDate,roomTypeID, myScanner, numberOfGuests, userHotelID); 
                    }
                }
            }
        }

        if(reservationComplete) {
            System.out.println("\nThe reservation has been completed succesfully.\n");
        }
        else {
            System.out.println("\nReservation cancelled. Try again.\n");
        }
    }

    /**
    * This function is used to convert an SQL Date object into a String in the format "yyyy-MM-dd".
    *
    * @param date The SQL Date object to be formatted.
    * @return A formatted date string in the format "yyyy-MM-dd".
    */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
    * This function is used to change the room type for a customer's reservation.
    * It requests the necessary information from the user and updates the reservation if a valid room type ID is provided.
    *
    * @param con                The Connection object to the database.
    * @param myScanner          The Scanner object used to receive user input.
    * @param reservationDetails The Reservation object containing the current reservation details.
    * @return An updated Reservation object if the room type is successfully updated; otherwise, the original Reservation object.
    */
    public static Reservation changeRoomType(Connection con, Scanner myScanner, Reservation reservationDetails) {
        int numberOfGuests = 0;
        int roomTypeID  = -1;
        boolean reservationUpdate = false;

        //requests the information again
        while(true) {
            
            //request for the number of guests that the user has. Repate until the user enters a valid integer
            numberOfGuests = getNumberOfGuests(myScanner);

            //request roomTypeID based on the available hotel
            roomTypeID = getRoomTypeID(con, myScanner, reservationDetails.getHotelID(), formatDate(reservationDetails.getArrivalDate()), formatDate(reservationDetails.getDepartureDate()), numberOfGuests);

            //check if the user wants to change the number of guests
            if(roomTypeID!=-4) {
                break;
            }
        }

        //check if the roomTypeID is valid
        if(roomTypeID>=0) {
            
            //get customerID
            int customerID = reservationDetails.getCustomerID();

            //complete the reservation
            reservationUpdate  = updateReservation(con,customerID,formatDate(reservationDetails.getArrivalDate()),formatDate(reservationDetails.getDepartureDate()),roomTypeID, 
            myScanner, numberOfGuests, reservationDetails.getHotelID(), reservationDetails.getReservationID()); 
        }

        //return status if reservation gets updated
        if(reservationUpdate) {
            reservationDetails = FrontDeskAgent.getReservation(con, reservationDetails.getCustomerID());
            System.out.println("\nThe room type has been updated successfully.");
        }
        else {
            System.out.println("\nThe room type was not updated.");
        }
        return reservationDetails;
    }

    /**
    * This function is used to update an existing reservation with new information.
    * It takes necessary parameters, retrieves the previous payment amount, updates the reservation and payment records.
    *
    * @param con              The Connection object to the database.
    * @param customerID       The ID of the customer making the reservation.
    * @param arrivalDate      The arrival date in the format "yyyy-MM-dd".
    * @param departureDate    The departure date in the format "yyyy-MM-dd".
    * @param roomTypeID       The ID of the room type being booked.
    * @param myScanner        The Scanner object used to receive user input.
    * @param numOfGuests      The number of guests for the reservation.
    * @param hotelID          The ID of the hotel where the reservation is made.
    * @param reservationID    The ID of the existing reservation to be updated.
    * @return true if the reservation is successfully updated; otherwise, false.
    */
    public static boolean updateReservation(Connection con,int customerID, String arrivalDate,String departureDate,
                                            int roomTypeID, Scanner myScanner, int numOfGuests, int hotelID, int reservationID) {

        // Get the previous payment amount by retrieving by using the reservationID
        double prevPaymentAmount;
        try (PreparedStatement ps = con.prepareStatement("SELECT amount FROM payment WHERE reservationID = ?")) {
            ps.setInt(1, reservationID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                prevPaymentAmount = rs.getDouble("amount");
            } 
            else {
                throw new SQLException("Payment not found for the given reservationID");
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
                                                
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
        double additionalPayment = 0;

        //get the number of points belonging to the user
        int numOfPoints = getNumberOfPoints(con, customerID);

        //let the user know their number of points available
        System.out.printf("You have %d membership points\n", numOfPoints);
        int numOfPointsUsed = 0;
        
        //only ask if the user wants to use some points that is if they have some already
        if(numOfPoints > 0) {

            //get the number of points the user wants to use
            numOfPointsUsed = getNumberOfPointsUsed(myScanner,numOfPoints);

            if (numOfPointsUsed > 0) {
                
                //fixed equivalent amount for each pooint
                double pointsCost = 0.005;

                //calculate the discount
                double discount = calculateDiscount(numOfPointsUsed, pointsCost);
                System.out.printf("\nThe discount is $%.2f%n\n", discount);

                //deduct discount to reflect the new amount
                bookingPrice = bookingPrice - discount;

            }
        }

        //calculate any possible refund or additional amount
        additionalPayment = bookingPrice - prevPaymentAmount;

        //print the refund or additional amount needed
        if(additionalPayment < 0) {
            System.out.printf("Your refund is $%.2f%n\n", additionalPayment);
        }
        else {
            //display the additinal fee
            System.out.printf("The additional bill is $%.2f%n\n", additionalPayment);
        }

        //update the bookingpirce (it will either increase or decrease based on additional amount)
        bookingPrice = bookingPrice + additionalPayment;

        //format payment date
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String paymentDate = currentDate.format(formatter);
        java.sql.Date sqlPaymentDate = convertStringToSqlDate(paymentDate);

        //set reservation status to complete
        String reservationStatus = "Confirmed";

        //format date
        java.sql.Date sqlArrivalDate = convertStringToSqlDate(arrivalDate);
        java.sql.Date sqlDepartureDate = convertStringToSqlDate(departureDate);

        // update reservation using stored procedure and get reservation ID
        int updatedReservationID = -1;
        try (CallableStatement csReservation = con.prepareCall("{call update_old_reservation(?,?,?,?,?,?,?,?,?)}")) {
            csReservation.setInt(1, reservationID);
            csReservation.setInt(2, customerID);
            csReservation.setInt(3, hotelID);
            csReservation.setInt(4, roomTypeID);
            csReservation.setInt(5, numOfGuests);
            csReservation.setDate(6, sqlArrivalDate);
            csReservation.setDate(7, sqlDepartureDate);
            csReservation.setString(8, reservationStatus);
            csReservation.registerOutParameter(9, Types.INTEGER); // Add an extra OUT parameter for the updated reservation ID
            csReservation.execute();
            updatedReservationID = csReservation.getInt(9);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Update payment using stored procedure and get payment ID
        int paymentID;
        try (CallableStatement csPayment = con.prepareCall("{call update_old_payment(?,?,?,?,?,?)}")) {
            csPayment.setInt(1, customerID);
            csPayment.setInt(2, updatedReservationID); // Use the updatedReservationID obtained from the previous stored procedure call
            csPayment.setDouble(3, bookingPrice);
            csPayment.setInt(4, numOfPointsUsed);
            csPayment.setDate(5, sqlPaymentDate);
            csPayment.registerOutParameter(6, Types.INTEGER);
            csPayment.execute();
            paymentID = csPayment.getInt(6);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // If paymentID > 0, return true, else return false.
        return paymentID > 0;
    }

    /**
    * This function is used to convert a date string in the format "yyyy-MM-dd" to a java.sql.Date object.
    * It is useful for inserting or updating date values in SQL databases.
    *
    * @param dateString The date string to be converted, in the format "yyyy-MM-dd".
    * @return A java.sql.Date object representing the converted date, or null if the conversion fails.
    */
    public static java.sql.Date convertStringToSqlDate(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = format.parse(dateString);
            return new java.sql.Date(parsedDate.getTime());
        } 
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
    * This function is used to create a new reservation for a specific customer, taking into account
    * their membership points and card validity.
    *
    * @param con            The Connection object to access the database.
    * @param customerID     The ID of the customer making the reservation.
    * @param arrivalDate    The arrival date of the reservation in the format "yyyy-MM-dd".
    * @param departureDate  The departure date of the reservation in the format "yyyy-MM-dd".
    * @param roomTypeID     The ID of the room type for the reservation.
    * @param myScanner      The Scanner object for user input.
    * @param numOfGuests    The number of guests for the reservation.
    * @param hotelID        The ID of the hotel where the reservation is being made.
    * @return               A boolean value indicating whether the reservation was successful (true) or not (false).
    */
    public static boolean makeReservation(Connection con,int customerID, String arrivalDate,String departureDate,
                                            int roomTypeID, Scanner myScanner, int numOfGuests, int hotelID) {
                    
        //retrieve card information
        Card customerCardInfor = getcustomerCardInfo(con, customerID);

        //calculate the cost of the customer stay based on the roomType and number of days
        double bookingPrice = getBookingPrice(con, roomTypeID, arrivalDate, departureDate);

        //get the number of points belonging to the user
        int numOfPoints = getNumberOfPoints(con, customerID);

        //let the user know their number of points available
        System.out.printf("You have %d membership points\n", numOfPoints);
        int numOfPointsUsed = 0;
        
        //only ask if the user wants to use some points that is if they have some already
        if(numOfPoints > 0) {

            //get the number of points the user wants to use
            numOfPointsUsed = getNumberOfPointsUsed(myScanner,numOfPoints);

            if (numOfPointsUsed > 0) {
                
                //fixed equivalent amount for each pooint
                double pointsCost = 0.005;

                //calculate the discount
                double discount = calculateDiscount(numOfPointsUsed, pointsCost);
                System.out.printf("\nThe discount is $%.2f%n\n", discount);

                //deduct discount to reflect the new amount
                bookingPrice = bookingPrice - discount;

            }
        }

        //display the price
        System.out.printf("\nYour total bill is $%.2f%n\n", bookingPrice);

        //format payment date
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String paymentDate = currentDate.format(formatter);
        java.sql.Date sqlPaymentDate = convertStringToSqlDate(paymentDate);

        //set reservation status to complete
        String reservationStatus = "Confirmed";

        //format date
        java.sql.Date sqlArrivalDate = convertStringToSqlDate(arrivalDate);
        java.sql.Date sqlDepartureDate = convertStringToSqlDate(departureDate);

        // Insert into reservation using stored procedure and get reservation ID
        int reservationID;
        try (CallableStatement csReservation = con.prepareCall("{call insert_new_reservation(?,?,?,?,?,?,?,?)}")) {
            csReservation.setInt(1, customerID);
            csReservation.setInt(2, hotelID);
            csReservation.setInt(3, roomTypeID);
            csReservation.setInt(4, numOfGuests);
            csReservation.setDate(5, sqlArrivalDate);
            csReservation.setDate(6, sqlDepartureDate);
            csReservation.setString(7, reservationStatus);
            csReservation.registerOutParameter(8, Types.INTEGER);
            csReservation.execute();
            reservationID = csReservation.getInt(8);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Insert into payment using stored procedure and get payment ID
        int paymentID;
        try (CallableStatement csPayment = con.prepareCall("{call insert_new_payment(?,?,?,?,?,?)}")) {
            csPayment.setInt(1, customerID);
            csPayment.setInt(2, reservationID);
            csPayment.setDouble(3, bookingPrice);
            csPayment.setInt(4, numOfPointsUsed);
            csPayment.setDate(5, sqlPaymentDate);
            csPayment.registerOutParameter(6, Types.INTEGER);
            csPayment.execute();
            paymentID = csPayment.getInt(6);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // If paymentID > 0, return true, else return false.
        return paymentID > 0;
    }

    /**
    * Creates a new membership and inserts it into the database, then returns the generated membership ID.
    *
    * @param con The Connection object to access the database.
    * @return The generated membership ID, or -1 if an error occurs.
    */
    public static int createNewMembership(Connection con) {
        int membershipID = -1;

        // Call the stored procedure to insert a new membership with random points (0 to 10)
        String storedProcCall = "{call insert_new_membership(?)}";
        try (CallableStatement cstmt = con.prepareCall(storedProcCall)) {
            
            // Register the output parameter for the generated membership ID
            cstmt.registerOutParameter(1, Types.INTEGER);

            // Execute the stored procedure
            cstmt.executeUpdate();

            // Retrieve the generated membership ID
            membershipID = cstmt.getInt(1);

        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        return membershipID;
    }

    /**
    * Creates a new address and adds it to the database.
    *
    * @param con The Connection object to access the database.
    * @param myScanner The Scanner object to read user inputs.
    * @return The generated address ID, or -1 if an error occurs.
    */
    public static int createNewAddress(Connection con, Scanner myScanner) {
        int addressID = -1;
        // Request the address information from the user
        System.out.print("Enter street: ");
        String street = myScanner.nextLine();

        System.out.print("Enter city: ");
        String city = myScanner.nextLine();

        System.out.print("Enter state: ");
        String state = myScanner.nextLine();

        // Get the postal code (American Zip Code), loop until the user provides the correct information
        System.out.print("Enter postal code (American Zip Code, e.g. 12345 or 12345-6789): ");
        String postalCode = myScanner.nextLine();
        Pattern zipPattern = Pattern.compile("^\\d{5}(-\\d{4})?$");

        while (!zipPattern.matcher(postalCode).matches()) {
            System.out.println("Invalid Zip Code format. Please try again.");
            System.out.print("Enter postal code (American Zip Code, e.g. 12345 or 12345-6789): ");
            postalCode = myScanner.nextLine();
        }

        // Call the stored procedure to insert the address into the table
        String storedProcCall = "{call insert_new_address(?, ?, ?, ?,?)}";
        try (CallableStatement cstmt = con.prepareCall(storedProcCall)) {

            cstmt.setString(1, street);
            cstmt.setString(2, city);
            cstmt.setString(3, state);
            cstmt.setString(4, postalCode);
            cstmt.registerOutParameter(5, Types.INTEGER);
            cstmt.executeUpdate();

            // Retrieve the generated addressID
            addressID = cstmt.getInt(5);

        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        return addressID;
    }

    /**
    * Creates a new card and inserts it into the database.
    *
    * @param con The Connection object to access the database.
    * @param myScanner The Scanner object to read user inputs.
    * @return The generated card ID, or -1 if an error occurs.
    */
    public static int createNewCard(Connection con, Scanner myScanner) {
        int cardID = -1;
    
        // Request card information from the user
        System.out.print("Enter card token: ");
        String cardToken = myScanner.nextLine();
    
        System.out.print("Enter card type: ");
        String cardType = myScanner.nextLine();
    
        // Use the getDate() function to get a valid date from the user
        System.out.print("Enter card expiration date (YYYY-MM-DD): ");
        String expirationDateString = getDate(myScanner);
        LocalDate expirationDate = LocalDate.parse(expirationDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
        // Call the stored procedure to insert the card into the table
        String storedProcCall = "{call insert_new_card(?, ?, ?, ?)}";
        try (CallableStatement cstmt = con.prepareCall(storedProcCall)) {
    
            cstmt.setString(1, cardToken);
            cstmt.setString(2, cardType);
            cstmt.setObject(3, expirationDate);
            cstmt.registerOutParameter(4, Types.INTEGER);
    
            cstmt.executeUpdate();
    
            // Retrieve the generated cardID
            cardID = cstmt.getInt(4);
    
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    
        return cardID;
    }
    
    /**
    * Creates a new customer and inserts it into the database.
    *
    * @param con The Connection object to access the database.
    * @param myScanner The Scanner object to read user inputs.
    * @return The generated customer ID, or -1 if an error occurs.
    */
    public static int createNewCustomer(Connection con, Scanner myScanner) {
        //arbitraruy customerID
        int customerID = -1;

        //create a customer object
        Customer newCustomer = new Customer();

        try {
            
            //insert new member into the frequent flyer program
            int membershipID = createNewMembership(con);
            System.out.println("You have been added to the Frequest Guests, and you were rewarded with some points!\n");

            //insert new address and return the addressID
            System.out.println("Enter your permanent address in the following section: \n");
            int addressID = createNewAddress(con, myScanner);
            System.out.println("Your address has been added successfully.\n");

            //insert new card information and return the cardID
            System.out.println("Enter your credit card/debit card information in the following section: \n");
            int cardID = createNewCard(con, myScanner);
            System.out.println("Your credit card/debit card information has been added successfully.\n");

            // Get the customer first name
            System.out.print("Enter first name: ");
            newCustomer.setFirstName(myScanner.nextLine());

            // Get the customer last name
            System.out.print("Enter last name: ");
            newCustomer.setLastName(myScanner.nextLine());

            // Get the customer phone number, loop until the user provides the correct information
            System.out.print("Enter phone number - US format, e.g. +1(123)-456-7890: ");
            String phoneNumber = myScanner.nextLine();
            Pattern phonePattern = Pattern.compile("^\\+1\\(\\d{3}\\)-\\d{3}-\\d{4}$");

            while (!phonePattern.matcher(phoneNumber).matches()) {
                System.out.println("Invalid phone number format. Please try again.");
                System.out.print("Enter phone number (US format, e.g. +1(123)-456-7890): ");
                phoneNumber = myScanner.nextLine();
            }

            //assign all customer attributes
            newCustomer.setPhoneNumber(phoneNumber);
            newCustomer.setAddressID(addressID);
            newCustomer.setCardID(cardID);
            newCustomer.setMembershipID(membershipID);

            // Call the stored procedure to insert the customer into the database
            String storedProcCall = "{call insert_new_customer(?, ?, ?, ?, ?, ?, ?)}";
            try (CallableStatement cstmt = con.prepareCall(storedProcCall)) {
                cstmt.setString(1, newCustomer.getFirstName());
                cstmt.setString(2, newCustomer.getLastName());
                cstmt.setString(3, newCustomer.getPhoneNumber());
                cstmt.setInt(4, newCustomer.getCardID());
                cstmt.setInt(5, newCustomer.getAddressID());
                cstmt.setInt(6, newCustomer.getMembershipID());
                cstmt.registerOutParameter(7, Types.INTEGER);

                cstmt.executeUpdate();

                // Get the generated customer ID from the stored procedure
                customerID = cstmt.getInt(7);
            } 
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the generated customer ID
        return customerID;
    }

    /**
    * Calculates the discount based on the number of points used and the fixed cost per point.
    *
    * @param numOfPointsUsed The number of points the customer wants to use for the discount.
    * @param pointsCost The fixed cost per point.
    * @return The calculated discount, rounded to two decimal places.
    */
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
                    } 
                    else {
                        System.out.printf("\nInvalid input. Please enter a number (1-%d): ", availablePoints);
                    }
                } 
                catch (NumberFormatException e) {
                    // If the input is not a valid number, inform the user
                    System.out.println("\nInvalid input. Please enter a valid number.");
                }
            }
        }

        // Return the number of points the user wants to use
        return pointsToUse;
    }


    /**
    * Retrieves the number of points belonging to the customer based on their customerID.
    *
    * @param con The database connection.
    * @param customerID The customer's ID.
    * @return The number of points associated with the customer's membership.
    */
    public static int getNumberOfPoints(Connection con, int customerID) {
        int numOfPoints = 0;

        // Query to retrieve the membershipID of the customer based on their customerID
        String query = "SELECT membershipID FROM Customer WHERE customerID = ?";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, customerID);

            try (ResultSet rs = pstmt.executeQuery()) {
                // If a result is found, set the membershipID to the value in the database
                if (rs.next()) {
                    int membershipID = rs.getInt("membershipID");

                    // Query to retrieve the number of points of the customer based on their membershipID
                    String pointsQuery = "SELECT points FROM Membership WHERE membershipID = ?";
                    try (PreparedStatement pointsPstmt = con.prepareStatement(pointsQuery)) {
                        pointsPstmt.setInt(1, membershipID);

                        try (ResultSet pointsRs = pointsPstmt.executeQuery()) {
                            // If a result is found, set the numOfPoints to the value in the database
                            if (pointsRs.next()) {
                                numOfPoints = pointsRs.getInt("points");
                            }
                        }
                    }
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the number of points
        return numOfPoints;
    }

    /**
    * Calculates the booking price for a specified room type and date range.
    *
    * @param con The database connection.
    * @param roomTypeID The ID of the room type.
    * @param arrivalDate The arrival date as a String (YYYY-MM-DD).
    * @param departureDate The departure date as a String (YYYY-MM-DD).
    * @return The total booking price for the specified room type and date range.
    */
    public static double getBookingPrice(Connection con, int roomTypeID, String arrivalDate, String departureDate) {
        double price = 0;
        double defaultRate = 40;

        // Convert the arrivalDate and departureDate to LocalDate
        LocalDate arrivalLocalDate = LocalDate.parse(arrivalDate);
        LocalDate departureLocalDate = LocalDate.parse(departureDate);

        // Extract the room rates from the database based on the roomTypeID and the date range
        String query = "SELECT startDate, endDate, price FROM RoomRate WHERE roomTypeID = ? AND ((startDate BETWEEN ? AND ?) OR (endDate BETWEEN ? AND ?)) ORDER BY startDate ASC";

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, roomTypeID);
            pstmt.setDate(2, java.sql.Date.valueOf(arrivalLocalDate));
            pstmt.setDate(3, java.sql.Date.valueOf(departureLocalDate));
            pstmt.setDate(4, java.sql.Date.valueOf(arrivalLocalDate));
            pstmt.setDate(5, java.sql.Date.valueOf(departureLocalDate));

            try (ResultSet rs = pstmt.executeQuery()) {

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
                    } 
                    else {
                        // Apply the default rate to the remaining days
                        long remainingDays = ChronoUnit.DAYS.between(currentDate, departureLocalDate);
                        price += remainingDays * defaultRate;
                        break;
                    }
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the price
        return price;
    }

    /**
    * Converts a java.util.Date to a java.time.LocalDate.
    *
    * @param date The java.util.Date to be converted.
    * @return The corresponding java.time.LocalDate.
    */
    public static LocalDate convertToLocalDate(Date date) {
        Timestamp timestamp = new Timestamp(date.getTime());
        return timestamp.toLocalDateTime().toLocalDate();
    }

    /**
    * Checks if the expiration date of a card is still valid.
    *
    * @param expirationDate The expiration date of the card.
    * @return true if the expiration date is after the current date, false otherwise.
    */
    public static boolean checkExpirationDateOfCard(Date expirationDate) {
    
        LocalDate expDate = convertToLocalDate(expirationDate);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Check if the expiration date is after the current date
        if (expDate.isAfter(currentDate)) {
            // Card is still valid
            return true;
        } 
        else {
            // Card has expired
            return false;
        }
    }

    /**
    * Retrieves the card information of an existing customer.
    *
    * @param con The connection to the database.
    * @param customerID The ID of the customer.
    * @return A Card object containing the card information of the customer.
    */
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
            } 
            else {
                System.out.println("Card not found for the provided customerID.");
            }
        } 
        catch (SQLException e) {
            System.out.println("Error while retrieving card information: " + e.getMessage());
        }
        return card;
    }

    /**
    * Retrieves the information of an existing customer.
    *
    * @param con The connection to the database.
    * @param customerID The ID of the customer.
    * @return A Customer object containing the information of the customer.
    */
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
            } 
            else {
                System.out.println("Customer not found with the provided customerID.");
            }
        } 
        catch (SQLException e) {
            System.out.println("Error while retrieving customer information: " + e.getMessage());
        }
        return customer;
    }

    /**
    * Prompts the user to enter a customer ID, validating that the input is a non-negative integer.
    * If the user indicates they do not have a customer ID, returns -1.
    *
    * @param myScanner a Scanner object to read user input
    * @return the customer ID entered by the user, or -1 if the user does not have a customer ID
    */
    public static int getCustomerID(Scanner myScanner) {
        int inputCustomerID = -1;

        //ask if they have customer ID
        System.out.print("\nDo you have a customer ID? (Y/N): ");

        String response = getYesOrNoInput(myScanner); 

        if(response.equals("Y")) {
            System.out.print("\nEnter your Customer ID (must be an integer greater than or equal to 0): ");
            while (true) {
                try {
                    inputCustomerID = Integer.parseInt(myScanner.nextLine());
                    if (inputCustomerID >= 0) {
                        break;
                    } 
                    else {
                        System.out.println("Customer ID must be greater than or equal to 0. Please enter a valid Customer ID:");
                    }
                } 
                catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid Customer ID:");
                }
            }
        }

        //return customerID
        return inputCustomerID;
    }

    /**
    * Checks if the customer exists in the database based on the provided customer ID.
    *
    * @param con The database connection
    * @param myScanner The Scanner object for user input
    * @param inputCustomerID The customer ID provided by the user
    * @return Returns 1 if the customer exists in the database, 0 if the customer does not exist, or -100 if there is an error
    */
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
                } 
                else {
                    // The customer is not found in the table
                    return 0;
                }
            } 
            catch (SQLException e) {
                System.out.println(e.getMessage());
                return -100;
            }
        } 
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return -100;
        }
    }
    
    
    /**
    * This function prompts the user to enter the number of guests and validates that the input is a positive integer.
    * @param myScanner A Scanner object used to get input from the user
    * @return The number of guests entered by the user
    */
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
                } 
                else {
                    break;
                }
            } 
            catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a positive integer.");
            }
        }
        // Return the number of guests entered by the user
        return numberOfGuests;
    }

    /**
    * Get the hotelID located with free reservations, located in the city chosen by user
    * 
    * @param con Connection to the database
    * @param myScanner Scanner object for user input
    * @param arrivalDate The arrival date in 'YYYY-MM-DD' format
    * @param departureDate The departure date in 'YYYY-MM-DD' format
    * @return The hotel ID of the selected hotel
    */
    public static int getHotelID(Connection con, Scanner myScanner, String arrivalDate, String departureDate) {

        // Create a map to store city names and a list of hotel IDs and names within each city
        Map<String, List<Entry<Integer, String>>> cityHotelMap = new HashMap<>();

        // query to get the cities with free rooms during a particular period
        String citiesQuery = "WITH reserved_roomtypes AS (" +
        "    SELECT rr.hotelID, rr.roomTypeID" +
        "    FROM Reservation rr" +
        "    WHERE (rr.arrivalDate <= ? AND rr.departureDate >= ?)" +
        ")," +
        "available_rooms AS (" +
        "    SELECT r.roomID, r.roomTypeID, r.hotelID" +
        "    FROM Room r" +
        "    JOIN RoomType rt ON r.roomTypeID = rt.roomTypeID" +
        "    WHERE r.roomStatus = 'Available' AND NOT EXISTS (" +
        "        SELECT * FROM reserved_roomtypes rrt" +
        "        WHERE rrt.hotelID = r.hotelID AND rrt.roomTypeID = r.roomTypeID" +
        "    )" +
        ")" +
        "SELECT DISTINCT h.hotelID, h.hotelName, a.city " + 
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
        System.out.print("\nEnter the hotel ID from the list given above: ");
        AtomicInteger selectedHotelID = new AtomicInteger(myScanner.nextInt());
        myScanner.nextLine(); // Consume newline character

        // Validate the entered hotel ID
        while (hotelList.stream().noneMatch(hotel -> hotel.getKey() == selectedHotelID.get())) {
            System.out.print("Invalid hotel ID. Please enter a hotel ID from the list given above: ");
            selectedHotelID.set(Integer.parseInt(myScanner.nextLine()));
        }

        // Return the selected hotel ID
        return selectedHotelID.get();
    }

    /**
    * Retrieves available room types in a particular hotel during a specified time period based on the number of guests.
    *
    * @param con Connection object to connect to the database.
    * @param myScanner Scanner object to get user input.
    * @param hotelID Integer value representing the hotel ID to get the available room types for.
    * @param arrivalDate String value representing the arrival date in "YYYY-MM-DD" format.
    * @param departureDate String value representing the departure date in "YYYY-MM-DD" format.
    * @param numberOfGuests Integer value representing the number of guests.
    * @return Integer value representing the selected room type ID, -1 if there are no available room types,
    *         -4 if the user wants to change the number of guests, and -1 for any database related errors.
    * @throws SQLException If there is an error executing the SQL statement.
    */
    public static int getRoomTypeID(Connection con, Scanner myScanner, int hotelID, String arrivalDate, String departureDate, int numberOfGuests) {

        Map<Integer, Integer> roomTypeMaxGuests = new HashMap<>();
        Map<Integer, Integer> availableRoomTypeMaxGuests = new HashMap<>();
        Map<Integer, String> roomTypeDescriptions = new HashMap<>();

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

        //set a boolean variable to see if the user gets any room
        boolean freeRoomType = false;

        // Prepare the statement to view the available roomtypes
        try (PreparedStatement pstmt = con.prepareStatement(roomTypesQuery)) {
            // Set the parameters
            pstmt.setString(1, arrivalDate);
            pstmt.setString(2, departureDate);
            pstmt.setInt(3, hotelID);
            pstmt.setInt(4, hotelID);

             // Execute the query and process the results
            boolean freeRoomTypeFound = false;
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    
                    // Get the room type details from the current row
                    int roomTypeID = rs.getInt("roomTypeID");
                    String description = rs.getString("description");
                    int maxGuests = rs.getInt("maxGuests");

                    // Store the room type details
                    roomTypeMaxGuests.put(roomTypeID, maxGuests);
                    roomTypeDescriptions.put(roomTypeID, description);

                    //print the availbale roomtype based on the user number of guests
                    if(numberOfGuests<=maxGuests) {

                        //for printing the heading before showing the available roomtypes
                        if(freeRoomTypeFound==false) {
                            System.out.println("\nThe available roomtypes are : ");
                            freeRoomTypeFound = true;
                        }

                        // Store the maxGuests for each roomTypeID
                        availableRoomTypeMaxGuests.put(roomTypeID, maxGuests);

                        // Print the room type details
                        System.out.println("\nRoom Type ID: " + roomTypeID);
                        System.out.println("Description: " + description);
                        System.out.println("Max Guests: " + maxGuests);
                        System.out.println();
                        freeRoomType = true;
                    }
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

        
        int selectedRoomTypeID = -1;

        //check if we have any rooms available
        if (availableRoomTypeMaxGuests.isEmpty() ||  roomTypeMaxGuests.isEmpty()) {
            System.out.println("\nThere are no available room types to accomodate your all of guests during the specified period.");
            return selectedRoomTypeID;
        }
        
        if(freeRoomType==false) {
            System.out.println("\nThe number of guests exceeds the maximum allowed for the available room types.\n");
            System.out.println("Below is a list of all available roomtypes, but you need to change the number of guests to select any of them.");

            // Print out all available room types for the user to see
            for (Map.Entry<Integer, String> entry : roomTypeDescriptions.entrySet()) {
                int roomTypeID = entry.getKey();
                String description = entry.getValue();
                int maxGuests = roomTypeMaxGuests.get(roomTypeID);

                System.out.println("\nRoom Type ID: " + roomTypeID);
                System.out.println("Description: " + description);
                System.out.println("Max Guests: " + maxGuests);
                System.out.println();
            }

            //prompt the user if they want to change their number of guests
            System.out.print("Do you want to change your number of guests? (Y/N) ");
            String userResponse = getYesOrNoInput(myScanner);

            //if the user says yes, return -4. We will use -4 as a code to allow the user to renter number of guests
            if(userResponse.equals("Y")) {
                return -4;
            }
            else {
                return -1;
            }
        }
        else {
            System.out.print("Please enter a Room Type ID from the list above: ");
            while (true) {
                selectedRoomTypeID = Integer.parseInt(myScanner.nextLine());
                if (!availableRoomTypeMaxGuests.containsKey(selectedRoomTypeID)) {
                    System.out.print("\nInvalid Room Type ID. Please enter a valid Room Type ID from the list above: ");
                }
                else {
                    break;
                } 
            }
        }
        return selectedRoomTypeID;
    }

    /**
    * Prompts the user to enter 'Y' or 'N' and returns the user's input.
    *
    * @param myScanner Scanner object to get user input.
    * @return String value representing the user's input, either 'Y' or 'N'.
    */
    public static String getYesOrNoInput(Scanner myScanner) {
        String userInput;
        while (true) {
            userInput = myScanner.nextLine().trim().toUpperCase();
    
            if (userInput.equals("Y") || userInput.equals("N")) {
                break;
            } 
            else {
                System.out.print("Invalid input. Please enter 'Y' or 'N':");
            }
        }
        return userInput;
    }

    /**
    * Prompts the user to enter a date in "YYYY-MM-DD" format, validates the input, and returns the date as a String.
    *
    * @param myScanner Scanner object to get user input.
    * @return String value representing the validated date in "YYYY-MM-DD" format.
    */
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
                } 
                else {
                    // If the date is valid, set validDate to true to exit the loop
                    validDate = true;
                }
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
}
