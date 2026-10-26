package com.example.android_vehicle_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHandler manages the SQLite database ("RideRent.db") for the application.
 * It creates, updates, and provides helper methods to query and manipulate the three primary tables:
 * 1. users: Stores customer and admin accounts, personal info, and credentials.
 * 2. vehicles: Stores vehicle inventory, specs, status, and daily rental pricing.
 * 3. bookings: Stores rental transactions, dates, total costs, payment statuses, and reviews.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RideRent.db";
    private static final int DATABASE_VERSION = 6;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_VEHICLES = "vehicles";
    public static final String TABLE_BOOKINGS = "bookings";

    // Column Names - users
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_DOB = "dob";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_LICENSE = "driving_license_num";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";

    // Column Names - vehicles
    public static final String COLUMN_VEHICLE_ID = "vehicle_id";
    public static final String COLUMN_VEHICLE_NAME = "vehicle_name";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_VEHICLE_NUM = "vehicle_num";
    public static final String COLUMN_FUEL = "fuel";
    public static final String COLUMN_SEATING_CAPACITY = "seating_capacity";
    public static final String COLUMN_PRICE_PER_DAY = "price_per_day";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_VEHICLE_STATUS = "vehicle_status";

    // Column Names - bookings
    public static final String COLUMN_BOOKING_ID = "booking_id";
    public static final String COLUMN_BOOKING_USER_ID = "user_id";
    public static final String COLUMN_BOOKING_VEHICLE_ID = "vehicle_id";
    public static final String COLUMN_PICKUP_DATE = "pickup_date";
    public static final String COLUMN_DROP_DATE = "drop_date";
    public static final String COLUMN_TOTAL_DAYS = "total_days";
    public static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    public static final String COLUMN_BOOKING_STATUS = "booking_status";
    public static final String COLUMN_PAYMENT_STATUS = "payment_status";
    public static final String COLUMN_PAYMENT_TIME = "payment_time";
    public static final String COLUMN_FEEDBACK = "feedback";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_LATE_FINE = "late_fine";

    /**
     * Constructs the SQLiteOpenHelper with the database name and version.
     * @param context App context.
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Configures database connection properties before table creation or migration.
     * Enables foreign key constraints to maintain relational integrity between tables.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Executes table creation SQL statements when the database is first initialized.
     * Seeds initial default admin users, customer accounts, vehicles, and sample bookings.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table with constraints (email, phone, license, username are unique)
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRST_NAME + " TEXT, "
                + COLUMN_LAST_NAME + " TEXT, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_DOB + " TEXT, "
                + COLUMN_PHONE + " TEXT(10) UNIQUE, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_LICENSE + " TEXT(20) UNIQUE, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_ROLE + " TEXT"
                + ");";

        // Create vehicles table to store vehicle inventory and availability
        String CREATE_VEHICLES_TABLE = "CREATE TABLE " + TABLE_VEHICLES + " ("
                + COLUMN_VEHICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_VEHICLE_NAME + " TEXT, "
                + COLUMN_BRAND + " TEXT, "
                + COLUMN_MODEL + " TEXT, "
                + COLUMN_VEHICLE_NUM + " TEXT, "
                + COLUMN_FUEL + " TEXT, "
                + COLUMN_SEATING_CAPACITY + " INTEGER, "
                + COLUMN_PRICE_PER_DAY + " REAL, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_VEHICLE_STATUS + " TEXT"
                + ");";

        // Create bookings table with foreign key relations linking users and vehicles
        String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + " ("
                + COLUMN_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_BOOKING_USER_ID + " INTEGER, "
                + COLUMN_BOOKING_VEHICLE_ID + " INTEGER, "
                + COLUMN_PICKUP_DATE + " TEXT, "
                + COLUMN_DROP_DATE + " TEXT, "
                + COLUMN_TOTAL_DAYS + " INTEGER, "
                + COLUMN_TOTAL_AMOUNT + " REAL, "
                + COLUMN_BOOKING_STATUS + " TEXT, "
                + COLUMN_PAYMENT_STATUS + " TEXT, "
                + COLUMN_PAYMENT_TIME + " TEXT, "
                + COLUMN_FEEDBACK + " TEXT, "
                + COLUMN_RATING + " REAL, "
                + COLUMN_LATE_FINE + " REAL DEFAULT 0.0, "
                + "FOREIGN KEY(" + COLUMN_BOOKING_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), "
                + "FOREIGN KEY(" + COLUMN_BOOKING_VEHICLE_ID + ") REFERENCES " + TABLE_VEHICLES + "(" + COLUMN_VEHICLE_ID + ")"
                + ");";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_VEHICLES_TABLE);
        db.execSQL(CREATE_BOOKINGS_TABLE);

        // Populate initial default data into the database
        seedInitialData(db);
    }

    /**
     * Handles database migration when DATABASE_VERSION is incremented.
     * Drops older tables and calls onCreate to rebuild fresh schema and seed data.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Seeds default accounts (admins, customers), vehicles, and sample bookings upon initialization.
     */
    private void seedInitialData(SQLiteDatabase db) {
        // Seed Admin 1 (admin1 / admin1)
        ContentValues admin1 = new ContentValues();
        admin1.put(COLUMN_FIRST_NAME, "Admin");
        admin1.put(COLUMN_LAST_NAME, "One");
        admin1.put(COLUMN_EMAIL, "admin1@riderent.com");
        admin1.put(COLUMN_DOB, "01 / 01 / 1990");
        admin1.put(COLUMN_PHONE, "9999999991");
        admin1.put(COLUMN_ADDRESS, "Headquarters, Bangalore");
        admin1.put(COLUMN_LICENSE, "ADMIN0001");
        admin1.put(COLUMN_USERNAME, "admin1");
        admin1.put(COLUMN_PASSWORD, "admin1");
        admin1.put(COLUMN_ROLE, "admin");
        db.insert(TABLE_USERS, null, admin1);

        // Seed Admin 2 (admin2 / admin2)
        ContentValues admin2 = new ContentValues();
        admin2.put(COLUMN_FIRST_NAME, "Admin");
        admin2.put(COLUMN_LAST_NAME, "Two");
        admin2.put(COLUMN_EMAIL, "admin2@riderent.com");
        admin2.put(COLUMN_DOB, "02 / 02 / 1991");
        admin2.put(COLUMN_PHONE, "9999999992");
        admin2.put(COLUMN_ADDRESS, "Branch Office, Bangalore");
        admin2.put(COLUMN_LICENSE, "ADMIN0002");
        admin2.put(COLUMN_USERNAME, "admin2");
        admin2.put(COLUMN_PASSWORD, "admin2");
        admin2.put(COLUMN_ROLE, "admin");
        db.insert(TABLE_USERS, null, admin2);

        // Seed Customer 1 (ananya18 / ananya_18)
        ContentValues cust1 = new ContentValues();
        cust1.put(COLUMN_FIRST_NAME, "Ananya");
        cust1.put(COLUMN_LAST_NAME, "Sharma");
        cust1.put(COLUMN_EMAIL, "ananya@email.com");
        cust1.put(COLUMN_DOB, "12 / 05 / 2000");
        cust1.put(COLUMN_PHONE, "9876543210");
        cust1.put(COLUMN_ADDRESS, "MG Road, Bangalore");
        cust1.put(COLUMN_LICENSE, "KA01 20230099880");
        cust1.put(COLUMN_USERNAME, "ananya18");
        cust1.put(COLUMN_PASSWORD, "ananya_18");
        cust1.put(COLUMN_ROLE, "customer");
        long cust1Id = db.insert(TABLE_USERS, null, cust1);

        // Seed Customer 2 (pranav2020 / pranav@20)
        ContentValues cust2 = new ContentValues();
        cust2.put(COLUMN_FIRST_NAME, "Pranav");
        cust2.put(COLUMN_LAST_NAME, "Rao");
        cust2.put(COLUMN_EMAIL, "pranav@email.com");
        cust2.put(COLUMN_DOB, "20 / 10 / 1999");
        cust2.put(COLUMN_PHONE, "9876543211");
        cust2.put(COLUMN_ADDRESS, "Koramangala, Bangalore");
        cust2.put(COLUMN_LICENSE, "KA01 20230099881");
        cust2.put(COLUMN_USERNAME, "pranav2020");
        cust2.put(COLUMN_PASSWORD, "pranav@20");
        cust2.put(COLUMN_ROLE, "customer");
        long cust2Id = db.insert(TABLE_USERS, null, cust2);

        // Seed Customer 3 (rosie45 / rosie_45)
        ContentValues cust3 = new ContentValues();
        cust3.put(COLUMN_FIRST_NAME, "Rosie");
        cust3.put(COLUMN_LAST_NAME, "DSouza");
        cust3.put(COLUMN_EMAIL, "rosie@email.com");
        cust3.put(COLUMN_DOB, "05 / 07 / 2001");
        cust3.put(COLUMN_PHONE, "9876543212");
        cust3.put(COLUMN_ADDRESS, "Indiranagar, Bangalore");
        cust3.put(COLUMN_LICENSE, "KA01 20230099882");
        cust3.put(COLUMN_USERNAME, "rosie45");
        cust3.put(COLUMN_PASSWORD, "rosie_45");
        cust3.put(COLUMN_ROLE, "customer");
        long cust3Id = db.insert(TABLE_USERS, null, cust3);

        // Seed Sample Vehicles
        ContentValues vehicle1 = new ContentValues();
        vehicle1.put(COLUMN_VEHICLE_NAME, "Honda City");
        vehicle1.put(COLUMN_BRAND, "Honda");
        vehicle1.put(COLUMN_MODEL, "City 2023");
        vehicle1.put(COLUMN_VEHICLE_NUM, "KA-01-AB-1234");
        vehicle1.put(COLUMN_FUEL, "petrol");
        vehicle1.put(COLUMN_SEATING_CAPACITY, 5);
        vehicle1.put(COLUMN_PRICE_PER_DAY, 1800.00);
        vehicle1.put(COLUMN_DESCRIPTION, "Comfortable 5-seater sedan for city and highway rides");
        vehicle1.put(COLUMN_VEHICLE_STATUS, "available");
        long vehicle1Id = db.insert(TABLE_VEHICLES, null, vehicle1);

        ContentValues vehicle2 = new ContentValues();
        vehicle2.put(COLUMN_VEHICLE_NAME, "Royal Enfield Classic 350");
        vehicle2.put(COLUMN_BRAND, "Royal Enfield");
        vehicle2.put(COLUMN_MODEL, "Classic 350");
        vehicle2.put(COLUMN_VEHICLE_NUM, "KA-05-XY-5678");
        vehicle2.put(COLUMN_FUEL, "petrol");
        vehicle2.put(COLUMN_SEATING_CAPACITY, 2);
        vehicle2.put(COLUMN_PRICE_PER_DAY, 700.00);
        vehicle2.put(COLUMN_DESCRIPTION, "Powerful cruiser motorcycle for long journeys");
        vehicle2.put(COLUMN_VEHICLE_STATUS, "available");
        long vehicle2Id = db.insert(TABLE_VEHICLES, null, vehicle2);

        ContentValues vehicle3 = new ContentValues();
        vehicle3.put(COLUMN_VEHICLE_NAME, "Mahindra Thar");
        vehicle3.put(COLUMN_BRAND, "Mahindra");
        vehicle3.put(COLUMN_MODEL, "Thar LX 4x4");
        vehicle3.put(COLUMN_VEHICLE_NUM, "KA-03-CD-9012");
        vehicle3.put(COLUMN_FUEL, "diesel");
        vehicle3.put(COLUMN_SEATING_CAPACITY, 4);
        vehicle3.put(COLUMN_PRICE_PER_DAY, 3200.00);
        vehicle3.put(COLUMN_DESCRIPTION, "4x4 Off-road SUV for outdoor adventures");
        vehicle3.put(COLUMN_VEHICLE_STATUS, "available");
        long vehicle3Id = db.insert(TABLE_VEHICLES, null, vehicle3);

        // Seed Sample Bookings for Customers
        ContentValues booking1 = new ContentValues();
        booking1.put(COLUMN_BOOKING_USER_ID, cust1Id);
        booking1.put(COLUMN_BOOKING_VEHICLE_ID, vehicle1Id);
        booking1.put(COLUMN_PICKUP_DATE, "2026-10-15");
        booking1.put(COLUMN_DROP_DATE, "2026-10-18");
        booking1.put(COLUMN_TOTAL_DAYS, 4);
        booking1.put(COLUMN_TOTAL_AMOUNT, 7200.00);
        booking1.put(COLUMN_BOOKING_STATUS, "completed");
        booking1.put(COLUMN_PAYMENT_STATUS, "success");
        booking1.put(COLUMN_PAYMENT_TIME, "11:00:00");
        booking1.put(COLUMN_FEEDBACK, "");
        booking1.put(COLUMN_RATING, 0.0);
        booking1.put(COLUMN_LATE_FINE, 0.0);
        db.insert(TABLE_BOOKINGS, null, booking1);

        ContentValues booking2 = new ContentValues();
        booking2.put(COLUMN_BOOKING_USER_ID, cust2Id);
        booking2.put(COLUMN_BOOKING_VEHICLE_ID, vehicle2Id);
        booking2.put(COLUMN_PICKUP_DATE, "2026-10-20");
        booking2.put(COLUMN_DROP_DATE, "2026-10-22");
        booking2.put(COLUMN_TOTAL_DAYS, 3);
        booking2.put(COLUMN_TOTAL_AMOUNT, 2100.00);
        booking2.put(COLUMN_BOOKING_STATUS, "ongoing");
        booking2.put(COLUMN_PAYMENT_STATUS, "success");
        booking2.put(COLUMN_PAYMENT_TIME, "14:30:00");
        booking2.put(COLUMN_FEEDBACK, "");
        booking2.put(COLUMN_RATING, 0.0);
        booking2.put(COLUMN_LATE_FINE, 0.0);
        db.insert(TABLE_BOOKINGS, null, booking2);

        ContentValues booking3 = new ContentValues();
        booking3.put(COLUMN_BOOKING_USER_ID, cust3Id);
        booking3.put(COLUMN_BOOKING_VEHICLE_ID, vehicle3Id);
        booking3.put(COLUMN_PICKUP_DATE, "2026-10-28");
        booking3.put(COLUMN_DROP_DATE, "2026-10-31");
        booking3.put(COLUMN_TOTAL_DAYS, 4);
        booking3.put(COLUMN_TOTAL_AMOUNT, 12800.00);
        booking3.put(COLUMN_BOOKING_STATUS, "upcoming");
        booking3.put(COLUMN_PAYMENT_STATUS, "success");
        booking3.put(COLUMN_PAYMENT_TIME, "09:15:00");
        booking3.put(COLUMN_FEEDBACK, "");
        booking3.put(COLUMN_RATING, 0.0);
        booking3.put(COLUMN_LATE_FINE, 0.0);
        db.insert(TABLE_BOOKINGS, null, booking3);
    }

    /**
     * Checks if credentials match a record in the users table with the specified role.
     * @param username Entered username.
     * @param password Entered password.
     * @param expectedRole Required role ('customer' or 'admin').
     * @return True if credentials match, false otherwise.
     */
    public boolean checkUserCredentials(String username, String password, String expectedRole) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ? AND " + COLUMN_ROLE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password, expectedRole});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Checks if a given username already exists in the users table.
     * @param username Username to check.
     * @return True if username exists, false otherwise.
     */
    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Checks if a given email address already exists in the users table.
     * @param email Email to check.
     * @return True if email exists, false otherwise.
     */
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    /**
     * Inserts a new customer account record into the users table.
     * @return True if insert succeeded, false otherwise.
     */
    public boolean registerUser(String firstName, String lastName, String email, String dob,
                                String phone, String address, String license, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DOB, dob);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_LICENSE, license);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, "customer");

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Fetches a user record Cursor by username.
     * @param username Username to search for.
     * @return Cursor containing user data.
     */
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        return db.rawQuery(query, new String[]{username});
    }

    /**
     * Queries all vehicles where vehicle_status is 'available'.
     * @return Cursor containing available vehicles.
     */
    public Cursor getAvailableVehicles() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VEHICLES + " WHERE " + COLUMN_VEHICLE_STATUS + " = 'available'";
        return db.rawQuery(query, null);
    }

    /**
     * Queries all bookings for a specific customer joined with vehicle info.
     * @param username Customer username.
     * @return Cursor containing joined booking and vehicle data.
     */
    public Cursor getUserBookings(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, v." + COLUMN_VEHICLE_NAME + ", v." + COLUMN_BRAND + ", v." + COLUMN_MODEL
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_USERS + " u ON b." + COLUMN_BOOKING_USER_ID + " = u." + COLUMN_USER_ID
                + " JOIN " + TABLE_VEHICLES + " v ON b." + COLUMN_BOOKING_VEHICLE_ID + " = v." + COLUMN_VEHICLE_ID
                + " WHERE u." + COLUMN_USERNAME + " = ?";
        return db.rawQuery(query, new String[]{username});
    }

    /**
     * Fetches user_id for a given username.
     * @param username Username to query.
     * @return Primary key user_id integer, or -1 if not found.
     */
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        int userId = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(COLUMN_USER_ID);
                if (idIdx != -1) {
                    userId = cursor.getInt(idIdx);
                }
            }
            cursor.close();
        }
        return userId;
    }

    /**
     * Inserts a new booking record into the bookings table.
     * @return True if insert succeeded, false otherwise.
     */
    public boolean addBooking(int userId, int vehicleId, String pickupDate, String dropDate,
                              int totalDays, double totalAmount, String bookingStatus,
                              String paymentStatus, String paymentTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_USER_ID, userId);
        values.put(COLUMN_BOOKING_VEHICLE_ID, vehicleId);
        values.put(COLUMN_PICKUP_DATE, pickupDate);
        values.put(COLUMN_DROP_DATE, dropDate);
        values.put(COLUMN_TOTAL_DAYS, totalDays);
        values.put(COLUMN_TOTAL_AMOUNT, totalAmount);
        values.put(COLUMN_BOOKING_STATUS, bookingStatus);
        values.put(COLUMN_PAYMENT_STATUS, paymentStatus);
        values.put(COLUMN_PAYMENT_TIME, paymentTime);

        long result = db.insert(TABLE_BOOKINGS, null, values);
        return result != -1;
    }

    /**
     * Updates user profile fields for a specified username.
     * @return True if update succeeded, false otherwise.
     */
    public boolean updateUserProfile(String username, String firstName, String lastName, String email,
                                     String phone, String address, String license) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_LICENSE, license);

        int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        return rows > 0;
    }

    // --- ADMIN METHODS --- //

    /**
     * Queries all bookings joined with user details and vehicle details for Admin view.
     * @return Cursor containing full combined booking records.
     */
    public Cursor getAllAdminRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, u." + COLUMN_USERNAME + ", u." + COLUMN_FIRST_NAME + ", u." + COLUMN_LAST_NAME + ", v." + COLUMN_VEHICLE_NAME + ", v." + COLUMN_BRAND + ", v." + COLUMN_MODEL + ", v." + COLUMN_VEHICLE_NUM + ", v." + COLUMN_PRICE_PER_DAY
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_USERS + " u ON b." + COLUMN_BOOKING_USER_ID + " = u." + COLUMN_USER_ID
                + " JOIN " + TABLE_VEHICLES + " v ON b." + COLUMN_BOOKING_VEHICLE_ID + " = v." + COLUMN_VEHICLE_ID
                + " ORDER BY b." + COLUMN_BOOKING_ID + " DESC";
        return db.rawQuery(query, null);
    }

    /**
     * Queries all vehicles in the database for Admin inventory management.
     * @return Cursor containing all vehicle records.
     */
    public Cursor getAllVehiclesAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VEHICLES;
        return db.rawQuery(query, null);
    }

    /**
     * Inserts a new vehicle record into the vehicles table.
     * @return True if insert succeeded, false otherwise.
     */
    public boolean addVehicle(String name, String brand, String model, String num, String fuel,
                              int capacity, double price, String desc, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VEHICLE_NAME, name);
        values.put(COLUMN_BRAND, brand);
        values.put(COLUMN_MODEL, model);
        values.put(COLUMN_VEHICLE_NUM, num);
        values.put(COLUMN_FUEL, fuel);
        values.put(COLUMN_SEATING_CAPACITY, capacity);
        values.put(COLUMN_PRICE_PER_DAY, price);
        values.put(COLUMN_DESCRIPTION, desc);
        values.put(COLUMN_VEHICLE_STATUS, status);
        long result = db.insert(TABLE_VEHICLES, null, values);
        return result != -1;
    }

    /**
     * Updates an existing vehicle record by vehicle_id.
     * @return True if update succeeded, false otherwise.
     */
    public boolean updateVehicle(int id, String name, String brand, String model, String num, String fuel,
                                 int capacity, double price, String desc, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VEHICLE_NAME, name);
        values.put(COLUMN_BRAND, brand);
        values.put(COLUMN_MODEL, model);
        values.put(COLUMN_VEHICLE_NUM, num);
        values.put(COLUMN_FUEL, fuel);
        values.put(COLUMN_SEATING_CAPACITY, capacity);
        values.put(COLUMN_PRICE_PER_DAY, price);
        values.put(COLUMN_DESCRIPTION, desc);
        values.put(COLUMN_VEHICLE_STATUS, status);

        int rows = db.update(TABLE_VEHICLES, values, COLUMN_VEHICLE_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    /**
     * Deletes a vehicle record by vehicle_id.
     * @return True if delete succeeded, false otherwise.
     */
    public boolean deleteVehicle(int vehicleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_VEHICLES, COLUMN_VEHICLE_ID + " = ?", new String[]{String.valueOf(vehicleId)});
        return rows > 0;
    }

    /**
     * Updates the availability status ('available'/'unavailable') for a vehicle.
     * @return True if update succeeded, false otherwise.
     */
    public boolean updateVehicleStatus(int vehicleId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VEHICLE_STATUS, status);
        int rows = db.update(TABLE_VEHICLES, values, COLUMN_VEHICLE_ID + " = ?", new String[]{String.valueOf(vehicleId)});
        return rows > 0;
    }

    /**
     * Updates late return fine for a specified booking.
     * @return True if update succeeded, false otherwise.
     */
    public boolean updateLateFine(int bookingId, double fineAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATE_FINE, fineAmount);
        int rows = db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    /**
     * Queries all user records in the database.
     * @return Cursor containing user data.
     */
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

    /**
     * Deletes a user account by user_id.
     * @return True if delete succeeded, false otherwise.
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // --- ADMIN DASHBOARD STATS METHODS --- //

    /**
     * Retrieves the total count of vehicles in the database.
     * Used in the Admin Dashboard to display quick statistics.
     * @return Total number of vehicles as an integer.
     */
    public int getTotalVehiclesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VEHICLES, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Retrieves the total count of bookings in the database.
     * Used in the Admin Dashboard to display quick statistics.
     * @return Total number of bookings as an integer.
     */
    public int getTotalBookingsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Deletes a booking record by booking_id.
     * @param bookingId Primary key of booking to delete.
     * @return True if delete succeeded, false otherwise.
     */
    public boolean deleteBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_BOOKINGS, COLUMN_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    /**
     * Updates customer feedback comment and rating for a specified booking.
     * @param bookingId Target booking ID.
     * @param feedback Customer text review comment.
     * @param rating Star rating numerical value.
     * @return True if update succeeded, false otherwise.
     */
    public boolean updateBookingFeedback(int bookingId, String feedback, double rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FEEDBACK, feedback);
        values.put(COLUMN_RATING, rating);
        int rows = db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }
}
