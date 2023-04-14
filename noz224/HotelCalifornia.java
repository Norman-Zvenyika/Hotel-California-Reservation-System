import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.*;
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
                ) 
            {
                //printout status connection
                System.out.println("Connection successfully made.\n");
                System.out.println("Welcome "+userName+"!");
                System.out.println();

                //interfaces

                //close the scanner and statement
                myScanner.close();
                con.close();
                break;
            }

            //catch the password error
            catch (SQLException ex) {
                if (ex.getErrorCode() == 1017) {
                    System.out.println("Incorrect password. Please try again.");
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

    //request user name and password from the suer
    public static String[] getCredentials(Scanner myScanner) {
        
        //create an array for username and password
        String [] userCredentials = new String [2];

        //request for username
        System.out.print("Enter Oracle username: ");
        userCredentials[0] = myScanner.next();
     
        //request password
        System.out.print("Enter Oracle user password: ");
        userCredentials[1] = myScanner.next();

        //return user credentials
        return userCredentials;
    }
}
