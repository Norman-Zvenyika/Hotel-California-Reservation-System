import java.util.Scanner;
import java.sql.*;

public class HotelCalifornia {
    public static void main(String[] arg) {

        //scanner object
        Scanner myScanner = new Scanner (System.in);

        //variable to ensure successful login
        boolean login = false;

        //number of attempts
        int numOfAttempts = 0;
            
        //request for username and password until the user enters something
        while(login==false) {
            
            //request user information
            String [] userCredentials = getCredentials(myScanner);

            //get username
            String userName = userCredentials[0];

            //get password
            String passWord = userCredentials[1];
        
            //estabilish connection
            try (
                Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", userName, passWord);
                Statement s = con.createStatement();
                ) 
            {
                //printout status connection
                System.out.println("\nWelcome to Hotel California "+userName+"!\n");

                //continue loop to provide options until the user terminates the program
                boolean exit = false;
                while(exit==false) {
                    
                    //display and get the user option
                    int option = printOptions(myScanner);

                    switch(option) {
                        case 1:
                            System.out.println("\nCustomer Online Reservation Access Interface\n");
                            CustomerOnlineReservation.mainMenu(con,s, myScanner);
                            break;
                        case 2:
                            System.out.println("\nFront Desk Agent Interface\n");
                            FrontDeskAgent.mainMenu(con, s, myScanner);
                            break;
                        case 3:
                            System.out.println("\nHousekeeping Interface\n");
                            Housekeeping.mainMenu(con, s, myScanner);
                            break;
                        case 4:
                            System.out.println("\nBusiness Manager Interface\n");
                            BusinessManager.mainMenu(con, s, myScanner);
                            break;
                        case 5:
                            System.out.println("\nProgram closing.......\n");
                            exit = true;
                            break;
                    }
                }

                //close the scanner and statement
                myScanner.close();
                con.close();
                break;
            }

            //catch the password error
            catch (SQLException ex) {
                if (ex.getErrorCode() == 1017) {
                    System.out.println("Incorrect username/ password. Please try again.");
                } else {
                    System.out.println(ex.getMessage());
                    break;
                }
            }

            //increment the number of attempts
            numOfAttempts += 1;

            //check if the number of attempts is above 3. If 3, terminate 
            if (numOfAttempts >=3) {
                System.out.println("Maximum number of password attempts reached.");
                break;
            }
        }

        System.out.println();
        System.out.println("Thank you for choosing Hotel California. Goodbye!");
    }

    /**
    * Requests the user's Oracle username and password.
    *
    * @param myScanner the Scanner object used for reading input
    * @return an array containing the user's Oracle username and password
    */
    public static String[] getCredentials(Scanner myScanner) {
        
        // Create an array for the username and password
        String[] userCredentials = new String[2];

        // Request the username
        System.out.print("\nEnter your Oracle username: ");
        userCredentials[0] = myScanner.nextLine();
    
        // Request the password
        System.out.print("Enter your Oracle user password: ");
        userCredentials[1] = myScanner.nextLine();

        // Return user credentials
        return userCredentials;
    }
    

   /**
    * Prints the main menu options for the Hotel California application and
    * validates the user's input.
    *
    * @param myScanner the Scanner object used for reading input
    * @return an integer representing the user's valid menu option
    */
    public static int printOptions(Scanner myScanner) {
        
        // Validate the option
        boolean validOption = false;

        // User option
        String userOption = "";

        // Loop for options
        while (!validOption) {
            // Print possible options
            System.out.println("\nHotel California Main Menu: \n");
            System.out.println("1. Customer online reservation access\n" +
                            "2. Front-desk agent\n" +
                            "3. Housekeeping\n" +
                            "4. Business manager\n" +
                            "5. Exit \n");

            System.out.print("Enter an option (1-5): ");
            userOption = myScanner.nextLine();

            // Validate the option
            if (userOption.matches("^[1-5]$")) {
                validOption = true;
            } 
            else {
                System.out.println("Invalid option.");
            }
        }

        // Return the option
        return Integer.parseInt(userOption);
    }

}
