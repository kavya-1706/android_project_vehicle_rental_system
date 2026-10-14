package com.example.android_vehicle_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RideRent.db";
    private static final int DATABASE_VERSION = 1;

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
                + "FOREIGN KEY(" + COLUMN_BOOKING_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), "
                + "FOREIGN KEY(" + COLUMN_BOOKING_VEHICLE_ID + ") REFERENCES " + TABLE_VEHICLES + "(" + COLUMN_VEHICLE_ID + ")"
                + ");";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_VEHICLES_TABLE);
        db.execSQL(CREATE_BOOKINGS_TABLE);

        // Seed default users and sample vehicles
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

        // Seed Customer user (abcd / abcd)
        ContentValues customerValues = new ContentValues();
        customerValues.put(COLUMN_FIRST_NAME, "Priya");
        customerValues.put(COLUMN_LAST_NAME, "Sharma");
        customerValues.put(COLUMN_EMAIL, "priya@email.com");
        customerValues.put(COLUMN_DOB, "15 / 08 / 1998");
        customerValues.put(COLUMN_PHONE, "9876543210");
        customerValues.put(COLUMN_ADDRESS, "MG Road, Bangalore");
        customerValues.put(COLUMN_LICENSE, "KA01 20230099887");
        customerValues.put(COLUMN_USERNAME, "abcd");
        customerValues.put(COLUMN_PASSWORD, "abcd");
        customerValues.put(COLUMN_ROLE, "customer");
        db.insert(TABLE_USERS, null, customerValues);

        // Also seed legacy demo user (priya / 123456) for backward compatibility
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
        db.insert(TABLE_USERS, null, priyaValues);

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
        db.insert(TABLE_VEHICLES, null, vehicle1);

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
        db.insert(TABLE_VEHICLES, null, vehicle2);

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
}
