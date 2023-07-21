#### Database Systems, Algorithms, and Applications Final Project
#### Student name: Norman Zvenyika
<br/>


# **HotelCalifornia Program**

This README file provides an overview of the HotelCalifornia project. The program offers a hotel reservation system with four main user interfaces: Customer Online Reservation, Front Desk Agent, Housekeeping, and Business Manager.

<br/>

## **File Structure**

### 1. DataGeneration
   * Data
      * Includes generated insert statements for populating the database
      * Data was generated using Python's Faker Library
   * Script
      * Generates insert statements for the database
      * Source code available in the notebook

### 2. Noz224
   * Java files
      * Source code files for running the program

### 3. Other
   * PL/SQL
      * Contains PL/SQL scripts for creating the database, inserting records, and updating records in the database
   * ER Diagram
      * Contains the Entity-Relationship Diagram for the Hotel California database

### 4. Makefile
   * Commands for running the program

### 5. Manifest.txt
   * Specify the configuration settings for the JAR file that is created when the application is compiled

### 6. README.txt
   * Describes the program and its functionalities

<br/>

## **Program Interfaces**

### 1. **Customer Online Reservation**
   *  This program allows users to make online reservations at Hotel California. The reservation process consists of several steps, as described below:

      #### A. Reservation Dates

      - The program starts by requesting the user to enter their arrival and departure dates.

      #### B. City Selection

      - Based on the provided dates, the program will display cities with available rooms and prompt the user to pick a city.

      #### C. Hotel Selection

      - Based on the chosen city, the program will display available hotels and prompt the user to enter the hotel ID.

      #### D. Number of Guests

      - The user is prompted to enter the number of guests.

      #### E. Room Type Selection

      - The program displays room type ID, description, and max guests each room can accommodate, and the user is prompted to provide the room type ID.
      - Constraints are set up to ensure that the user's number of guests does not exceed the maximum number of guests allowed in each room type.

      #### F. Customer ID

      - The program then prompts the user if they have a customer ID.
      - If Yes, the program will request their customer ID.
      - If No, the program will ask if the customer is interested in creating a new account with Hotel California.
         - If the user enters yes, the program will create a new profile for the customer.
            - To create a new profile, the program will request customer details (personal information and their card information).
            - The program is set to create a unique frequent member account for the new user.
            - Each new member is randomly awarded points between 0-10 just for creating an account with the hotel.
            - If the account is created successfully, the customer information is displayed at the end.
         - If the user enters no, the reservation is terminated.

      #### G. Membership Points

      - With the customer ID, the program will ask if the user wants to use their membership points to pay for the bill.
      - If yes, each point is equivalent to $0.005, and the discount value is calculated based on the number of points used.
      - If not, the program will simply display the total bill to the user.

      #### H. Booking Price Calculation

      - Initial booking price is calculated by multiplying the set room rate for a specific room type if found in the room rate table else a default flat value of $40 per day is used.
      - The final booking price = initial booking price – discount price.

### 2. **Front Desk Agent**

   *  The front desk agent provides two options for customers:

      ####  A. Check-In

      - The program requests the customer's first name, last name, and membership ID.
      - Possible scenarios after entering this information:
         - If the information does not match any records in the customer reservation, the program will display "customer not found."
         - If the information is found, but the reservation status is not "Confirmed," the program will notify the user that they have no active reservation.
            - if the customer has more than 1 reservation "Confirmed", the program will display all confirmed reservations and the customer is asked to select a single reservation that can be checked-in.
         - If the customer information is found and the reservation record has "Confirmed" status:
            - The program will ask if the user wants to change their room type.
            - If No:
               - The customer is checked into a room of the specific room type in a specific hotel based on the user reservation records.
               - The reservation status is changed to "Checked-In" so that we know that the customer came to the hotel and utilized their reservation.
            - If Yes:
               - The program will display all room types with free rooms in the particular hotel.
                  - The program can display "no room available" if there are no free rooms of any room type available at the hotel.
               - The customer is asked to select the room type again, with previous constraints still applied (e.g., the maximum number of room type guests should not be less than the user's number of guests).
               - Based on the selected room type, the program calculates the booking.
               - The program will find the difference between the original booking price and the new booking price:
                  - If the difference is positive, the customer needs to pay an additional amount, which will be displayed to the user.
                  - If the difference is negative, the customer will get a refund, which will be displayed to the user.
               - The program will then update the reservation record and the payment record of the customer accordingly.
               - Unfortunately, the dates of reservation cannot be changed according to the implementation of this project.
            - The room status of the specific room is then changed to "Occupied."

      #### B. Check-Out

         - The program requests the customer's first name, last name, and membership ID.
         - Possible scenarios following:
            - If the customer information is found, the customer is checked out, and the room status will be set to "Ready to be cleaned."
            - If the customer information does not match any records, the system will display "customer not found."

### 3. **Housekeeping**
   * The program requests the Customer's first name, last name, and membership ID
   * Based on this information, the program will identify the room in which the user was accommodated and the room status is set to "Available" for the next use.

### 4. **Business Manager**
   *  The program will prompt the manager with the following options:

      #### A. View Aggregate Data Over a Period of Time

      -  The program asks the user for the start and end dates.
      -  Based on the provided dates, the program will display:
         - The occupancy rate of each hotel for the given time period.
         - The total revenue from each hotel.
         - Reservation statuses over time.

      #### B. Set Rates

      -  The program will ask for the manager ID to identify the hotel to which the room type belongs.
      -  The program prompts the manager if they want to change an existing room rate or set a new room rate for a specific room type.
      -  The PL/SQL procedure is set to update a record if there is any overlap between two room rates during a particular period:
         - For example, if there is a room rate between 2023-08-19 and 2023-08-20 for a specific room type and the manager enters a room rate for dates 2023-08-19 and 2023-08-24, this new room rate will override the existing room rate. Thus, the new room rate reflected in the database will become the new one.
      -  A new room rate ID is created only if there is no overlap between any dates.

<br/>

## **Future Work (not implemented)**
   -  In a real-world scenario, it is recommended to add a password column to the customer table to ensure that customers cannot access each other's information. This enhances data security and privacy by requiring customers to provide a valid password before accessing their reservation details.

   - The customer online reservation system should provide customers with the flexibility to modify their departure dates, allowing them to either extend or shorten their stay as needed. This feature would improve the user experience by giving customers more control over their reservations, making the system more convenient and user-friendly.

<br/>

## **Compilation**
* You can compile and launch HotelCalifornia program by using the makefile, using the `make run` command.

<br/>

## **Data Generation**

* All the data was generated using the Python Faker library to generate insert statements for inserting data into the database. The script and the data are in the `DataGeneration` folder.