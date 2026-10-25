package com.example.android_vehicle_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RideRent.db";
    private static final int DATABASE_VERSION = 3;

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

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
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

        // Create vehicles table
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

        // Create bookings table
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

        // Seed default users, vehicles, and bookings
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Seed Admin user
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_FIRST_NAME, "Admin");
        adminValues.put(COLUMN_LAST_NAME, "System");
        adminValues.put(COLUMN_EMAIL, "admin@riderent.com");
        adminValues.put(COLUMN_DOB, "01 / 01 / 1990");
        adminValues.put(COLUMN_PHONE, "9999999999");
        adminValues.put(COLUMN_ADDRESS, "Headquarters, Bangalore");
        adminValues.put(COLUMN_LICENSE, "ADMIN000111222");
        adminValues.put(COLUMN_USERNAME, "admin");
        adminValues.put(COLUMN_PASSWORD, "admin");
        adminValues.put(COLUMN_ROLE, "admin");
        db.insert(TABLE_USERS, null, adminValues);



        // Seed Customer user (priya / 123456)
        ContentValues priyaValues = new ContentValues();
        priyaValues.put(COLUMN_FIRST_NAME, "Priya");
        priyaValues.put(COLUMN_LAST_NAME, "Sharma");
        priyaValues.put(COLUMN_EMAIL, "priya.sharma@email.com");
        priyaValues.put(COLUMN_DOB, "15 / 08 / 1998");
        priyaValues.put(COLUMN_PHONE, "9876543211");
        priyaValues.put(COLUMN_ADDRESS, "MG Road, Bangalore");
        priyaValues.put(COLUMN_LICENSE, "KA01 20230099888");
        priyaValues.put(COLUMN_USERNAME, "priya");
        priyaValues.put(COLUMN_PASSWORD, "123456");
        priyaValues.put(COLUMN_ROLE, "customer");
        long priyaUserId = db.insert(TABLE_USERS, null, priyaValues);

        // Seed Customer user (k / k)
        ContentValues kValues = new ContentValues();
        kValues.put(COLUMN_FIRST_NAME, "K");
        kValues.put(COLUMN_LAST_NAME, "Kumar");
        kValues.put(COLUMN_EMAIL, "k.kumar@email.com");
        kValues.put(COLUMN_DOB, "01 / 01 / 2000");
        kValues.put(COLUMN_PHONE, "9876543212");
        kValues.put(COLUMN_ADDRESS, "Indiranagar, Bangalore");
        kValues.put(COLUMN_LICENSE, "KA01 20230099889");
        kValues.put(COLUMN_USERNAME, "k");
        kValues.put(COLUMN_PASSWORD, "k");
        kValues.put(COLUMN_ROLE, "customer");
        long kUserId = db.insert(TABLE_USERS, null, kValues);

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
        db.insert(TABLE_VEHICLES, null, vehicle3);

        // Seed Sample Booking for user "priya"
        ContentValues priyaBooking = new ContentValues();
        priyaBooking.put(COLUMN_BOOKING_USER_ID, priyaUserId);
        priyaBooking.put(COLUMN_BOOKING_VEHICLE_ID, vehicle2Id);
        priyaBooking.put(COLUMN_PICKUP_DATE, "2026-10-15");
        priyaBooking.put(COLUMN_DROP_DATE, "2026-10-17");
        priyaBooking.put(COLUMN_TOTAL_DAYS, 2);
        priyaBooking.put(COLUMN_TOTAL_AMOUNT, 1400.00);
        priyaBooking.put(COLUMN_BOOKING_STATUS, "ongoing");
        priyaBooking.put(COLUMN_PAYMENT_STATUS, "success");
        priyaBooking.put(COLUMN_PAYMENT_TIME, "14:15:00");
        priyaBooking.put(COLUMN_FEEDBACK, "Great car, very clean!");
        priyaBooking.put(COLUMN_RATING, 4.5);
        priyaBooking.put(COLUMN_LATE_FINE, 0.0);
        db.insert(TABLE_BOOKINGS, null, priyaBooking);

        // Seed Sample Booking for user "k"
        ContentValues kBooking = new ContentValues();
        kBooking.put(COLUMN_BOOKING_USER_ID, kUserId);
        kBooking.put(COLUMN_BOOKING_VEHICLE_ID, vehicle1Id);
        kBooking.put(COLUMN_PICKUP_DATE, "2026-10-20");
        kBooking.put(COLUMN_DROP_DATE, "2026-10-23");
        kBooking.put(COLUMN_TOTAL_DAYS, 3);
        kBooking.put(COLUMN_TOTAL_AMOUNT, 5400.00);
        kBooking.put(COLUMN_BOOKING_STATUS, "upcoming");
        kBooking.put(COLUMN_PAYMENT_STATUS, "success");
        kBooking.put(COLUMN_PAYMENT_TIME, "10:30:00");
        kBooking.put(COLUMN_FEEDBACK, "");
        kBooking.put(COLUMN_RATING, 0.0);
        kBooking.put(COLUMN_LATE_FINE, 0.0);
        db.insert(TABLE_BOOKINGS, null, kBooking);
    }

    // Helper: Authenticate user credentials with specific role
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

    // Helper: Check if username exists
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

    // Helper: Check if email exists
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

    // Helper: Register new customer
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

    // Helper: Fetch user profile cursor by username
    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        return db.rawQuery(query, new String[]{username});
    }

    // Helper: Get all available vehicles
    public Cursor getAvailableVehicles() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VEHICLES + " WHERE " + COLUMN_VEHICLE_STATUS + " = 'available'";
        return db.rawQuery(query, null);
    }

    // Helper: Get all bookings for a user
    public Cursor getUserBookings(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, v." + COLUMN_VEHICLE_NAME + ", v." + COLUMN_BRAND + ", v." + COLUMN_MODEL
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_USERS + " u ON b." + COLUMN_BOOKING_USER_ID + " = u." + COLUMN_USER_ID
                + " JOIN " + TABLE_VEHICLES + " v ON b." + COLUMN_BOOKING_VEHICLE_ID + " = v." + COLUMN_VEHICLE_ID
                + " WHERE u." + COLUMN_USERNAME + " = ?";
        return db.rawQuery(query, new String[]{username});
    }

    // Helper: Get user ID by username
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

    // Helper: Add a new booking
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

    // Helper: Update user profile details
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

    // Get all bookings (includes payments, feedback, users, vehicles)
    public Cursor getAllAdminRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, u." + COLUMN_USERNAME + ", u." + COLUMN_FIRST_NAME + ", u." + COLUMN_LAST_NAME + ", v." + COLUMN_VEHICLE_NAME + ", v." + COLUMN_BRAND + ", v." + COLUMN_MODEL + ", v." + COLUMN_VEHICLE_NUM + ", v." + COLUMN_PRICE_PER_DAY
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_USERS + " u ON b." + COLUMN_BOOKING_USER_ID + " = u." + COLUMN_USER_ID
                + " JOIN " + TABLE_VEHICLES + " v ON b." + COLUMN_BOOKING_VEHICLE_ID + " = v." + COLUMN_VEHICLE_ID
                + " ORDER BY b." + COLUMN_BOOKING_ID + " DESC";
        return db.rawQuery(query, null);
    }

    // Get all vehicles (Admin view)
    public Cursor getAllVehiclesAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VEHICLES;
        return db.rawQuery(query, null);
    }

    // Add new vehicle
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

    // Update vehicle completely
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

    // Delete vehicle
    public boolean deleteVehicle(int vehicleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_VEHICLES, COLUMN_VEHICLE_ID + " = ?", new String[]{String.valueOf(vehicleId)});
        return rows > 0;
    }

    // Update vehicle status
    public boolean updateVehicleStatus(int vehicleId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VEHICLE_STATUS, status);
        int rows = db.update(TABLE_VEHICLES, values, COLUMN_VEHICLE_ID + " = ?", new String[]{String.valueOf(vehicleId)});
        return rows > 0;
    }

    // Update late return fine
    public boolean updateLateFine(int bookingId, double fineAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATE_FINE, fineAmount);
        int rows = db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    // Get all users
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

    // Delete a user
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
}


