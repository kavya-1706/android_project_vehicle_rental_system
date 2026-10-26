package com.example.android_vehicle_project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * BookingUtils is a helper class providing static utility functions for:
 * 1. Dynamic date-based status calculation ('upcoming', 'ongoing', 'completed').
 * 2. 2-day prior booking cancellation eligibility checks.
 * 3. System push notification dispatching for booking reminders (pickup or drop-off tomorrow).
 */
public class BookingUtils {

    // Notification channel identifier
    private static final String CHANNEL_ID = "ride_rent_notifications";
    // Date parser matching SQLite date format (yyyy-MM-dd)
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Calculates booking status dynamically based on current date relative to pickup and drop dates.
     * Rules:
     * - today < pickup_date -> "upcoming"
     * - pickup_date <= today <= drop_date -> "ongoing"
     * - today > drop_date -> "completed"
     *
     * @param pickupDateStr Pick-up date string in yyyy-MM-dd format.
     * @param dropDateStr Drop-off date string in yyyy-MM-dd format.
     * @return Status string: "upcoming", "ongoing", or "completed".
     */
    public static String calculateStatus(String pickupDateStr, String dropDateStr) {
        try {
            // Get current date and reset time to midnight (00:00:00) for exact day comparison
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Parse pickup date and reset time
            Calendar pickup = Calendar.getInstance();
            if (dateFormat.parse(pickupDateStr) != null) {
                pickup.setTime(dateFormat.parse(pickupDateStr));
            }
            pickup.set(Calendar.HOUR_OF_DAY, 0);
            pickup.set(Calendar.MINUTE, 0);
            pickup.set(Calendar.SECOND, 0);
            pickup.set(Calendar.MILLISECOND, 0);

            // Parse drop date and reset time
            Calendar drop = Calendar.getInstance();
            if (dateFormat.parse(dropDateStr) != null) {
                drop.setTime(dateFormat.parse(dropDateStr));
            }
            drop.set(Calendar.HOUR_OF_DAY, 0);
            drop.set(Calendar.MINUTE, 0);
            drop.set(Calendar.SECOND, 0);
            drop.set(Calendar.MILLISECOND, 0);

            // Evaluate position of today relative to pickup and drop dates
            if (today.before(pickup)) {
                return "upcoming";
            } else if (!today.after(drop)) {
                return "ongoing";
            } else {
                return "completed";
            }
        } catch (Exception e) {
            // Fallback to completed in case of parsing exceptions
            return "completed";
        }
    }

    /**
     * Checks if a booking is eligible for cancellation by the customer.
     * Rule: Can be cancelled ONLY IF there is at least 2 days left before booking starts (pickup_date - today >= 2 days).
     *
     * @param pickupDateStr Pick-up date string in yyyy-MM-dd format.
     * @return True if remaining days >= 2, false otherwise.
     */
    public static boolean canCancelBooking(String pickupDateStr) {
        try {
            // Reset today's time to midnight
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Reset pickup date time to midnight
            Calendar pickup = Calendar.getInstance();
            if (dateFormat.parse(pickupDateStr) != null) {
                pickup.setTime(dateFormat.parse(pickupDateStr));
            }
            pickup.set(Calendar.HOUR_OF_DAY, 0);
            pickup.set(Calendar.MINUTE, 0);
            pickup.set(Calendar.SECOND, 0);
            pickup.set(Calendar.MILLISECOND, 0);

            // Calculate difference in days
            long diffInMillis = pickup.getTimeInMillis() - today.getTimeInMillis();
            long diffDays = diffInMillis / (1000 * 60 * 60 * 24);

            return diffDays >= 2;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Queries customer bookings and dispatches Android status bar system notifications for:
     * 1. Pickup date is tomorrow ("Tomorrow is your upcoming booking for [Vehicle]!")
     * 2. Drop-off date is tomorrow ("Tomorrow is your drop-off last date for [Vehicle]!")
     *
     * @param context App context.
     * @param username Customer username.
     */
    public static void checkBookingReminders(Context context, String username) {
        if (context == null || username == null) return;

        DatabaseHandler dbHandler = new DatabaseHandler(context);
        Cursor cursor = dbHandler.getUserBookings(username);

        if (cursor != null) {
            // Get today's midnight date
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Calculate tomorrow's date
            Calendar tomorrow = (Calendar) today.clone();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);

            while (cursor.moveToNext()) {
                int vehicleNameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_VEHICLE_NAME);
                int pickupIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PICKUP_DATE);
                int dropIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_DROP_DATE);

                String vehicleName = vehicleNameIdx != -1 ? cursor.getString(vehicleNameIdx) : "Vehicle";
                String pickupDateStr = pickupIdx != -1 ? cursor.getString(pickupIdx) : "";
                String dropDateStr = dropIdx != -1 ? cursor.getString(dropIdx) : "";

                try {
                    // Check if pickup is tomorrow
                    if (!pickupDateStr.isEmpty()) {
                        Calendar pickup = Calendar.getInstance();
                        if (dateFormat.parse(pickupDateStr) != null) {
                            pickup.setTime(dateFormat.parse(pickupDateStr));
                        }
                        pickup.set(Calendar.HOUR_OF_DAY, 0);
                        pickup.set(Calendar.MINUTE, 0);
                        pickup.set(Calendar.SECOND, 0);
                        pickup.set(Calendar.MILLISECOND, 0);

                        if (pickup.equals(tomorrow)) {
                            sendNotification(context, "Upcoming Booking Reminder",
                                    "Tomorrow is your upcoming booking for " + vehicleName + "!");
                        }
                    }

                    // Check if drop-off is tomorrow
                    if (!dropDateStr.isEmpty()) {
                        Calendar drop = Calendar.getInstance();
                        if (dateFormat.parse(dropDateStr) != null) {
                            drop.setTime(dateFormat.parse(dropDateStr));
                        }
                        drop.set(Calendar.HOUR_OF_DAY, 0);
                        drop.set(Calendar.MINUTE, 0);
                        drop.set(Calendar.SECOND, 0);
                        drop.set(Calendar.MILLISECOND, 0);

                        if (drop.equals(tomorrow)) {
                            sendNotification(context, "Drop-off Last Date Reminder",
                                    "Tomorrow is your drop-off last date for " + vehicleName + "!");
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            cursor.close();
        }
    }

    /**
     * Constructs and posts a notification to the Android system notification bar.
     *
     * @param context App context.
     * @param title Notification title.
     * @param message Notification body text.
     */
    private static void sendNotification(Context context, String title, String message) {
        // Ensure Notification Channel exists
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException ignored) {
        }
    }

    /**
     * Creates a high-priority Notification Channel on Android 8.0+ (API 26+).
     *
     * @param context App context.
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "RideRent Booking Reminders";
            String description = "Notifications for upcoming bookings and drop-off dates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
