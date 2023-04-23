# HotelCalifornia Program

This README file provides an overview of the HotelCalifornia project, explaining its purpose, key components, and file structure. The program offers a hotel reservation system with four main user interfaces: Customer Online Reservation, Front Desk Agent, Housekeeping, and Business Manager.

## File Structure

### 1. DataGeneration
   * Data
      * Contains the generated insert statements for inserting data into the database
   * Script
      * Responsible for generating the insert statements

### 2. Noz224
   * Java files
      * Source code files for running the program

### 3. Other
   * PL/SQL
      * Contains PL/SQL scripts for creating the database, inserting records, and updating records in the database

### 4. Makefile
   * Commands
      * For running the program

### 5. Manifest.txt
   * Specify the configuration settings for the JAR file that is created when the application is compiled

### 6. README.txt
   * Describes the program and its functionalities

## Program Interfaces

### 1. **Customer Online Reservation**
   * For making reservations
   * The program starts by requesting the arrival Date and Departure Date
   * Based on the dates provided, the program will display cities with available rooms. The user is prompted to pick a city
   * Based on the city chosen, the program will display available hotels in that particular city. The user simply needs to enter the hotel ID
   * After picking the ID, the user is prompted to enter the number of guests
   * The program then displays room types ID, description, and max guests each room can accommodate. The user simply needs to provide the room type ID
   * Constraints are set up to ensure that the user's number of guests does not exceed the maximum number of guests allowed
   * The program then prompts the user if they have a customer ID
      * If Yes, the program will request their customer ID
      * If No, the program will ask if the customer is interested in creating a new account with the Hotel California
         * If the user enters yes, the program will create a new profile for the customer
         * If not, the reservation is terminated
   * With the customer ID, the program will ask if the user wants to use their membership points to pay for the bill
      * If yes, each point is equivalent to $0.005, and the discount value is calculated based on the number of points used
      * If not, the program will simply display the total bill to the user
   * If payment fails at any time due to unforeseen reason, the reservation status is immediately set to cancelled

### 2. **Front Desk Agent**
   * The front desk agent displays two options:
      * Check-In
         * The program requests the Customer's first name, last name, and membership ID
         * Based on this information, the program will identify the room in which the user was located and the room is set to available for use
      * Check-Out
         * The program requests the Customer's first name, last name, and membership ID
         * Based on this information, the program will identify the room in which the user was located and the room is set to available for use

### 3. **Housekeeping**
   * The program requests the Customer's first name, last name, and membership ID
   * Based on this information, the program will identify the room in which the user was located and the room is set to available for use

### 4. **Business Manager**
   * The program will prompt the manager with options:
      * View-aggregate data over a period of time
         * The program asks the user for the dates
         * Based on the dates, the program will display the occupancy rate of each hotel for a given time period, the total revenue from each hotel, and reservation statuses over time
      * Set rates
         * The program will ask for the manager ID to identify the hotel to which the room type belongs
         * The program prompts the manager if they want to change an existing or set a new room rate for a specific room type
         * The PL/SQL procedure is set to update a record if there is any overlap between two room rates during a particular period
         * A new room rate ID is created only if there is no overlap between any dates

## Compilation
* You can compile and launch HotelCalifornia program by using the makefile, using the `make run` command.

## Data Generation

* All the data was generated using the Python Faker library to generate insert statements for inserting data into the database. The script and the data are in the `DataGeneration` folder.