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

public class BookingUtils {

    private static final String CHANNEL_ID = "ride_rent_notifications";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Calculates booking status dynamically based on current date relative to pickup and drop dates.
     * Rule:
     * - today < pickup_date -> "upcoming"
     * - pickup_date <= today <= drop_date -> "ongoing"
     * - today > drop_date -> "completed"
     */
    public static String calculateStatus(String pickupDateStr, String dropDateStr) {
        try {
            Calendar today = Calendar.getInstance();
            // Reset time part for accurate day comparison
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar pickup = Calendar.getInstance();
            pickup.setTime(dateFormat.parse(pickupDateStr));
            pickup.set(Calendar.HOUR_OF_DAY, 0);
            pickup.set(Calendar.MINUTE, 0);
            pickup.set(Calendar.SECOND, 0);
            pickup.set(Calendar.MILLISECOND, 0);

            Calendar drop = Calendar.getInstance();
            drop.setTime(dateFormat.parse(dropDateStr));
            drop.set(Calendar.HOUR_OF_DAY, 0);
            drop.set(Calendar.MINUTE, 0);
            drop.set(Calendar.SECOND, 0);
            drop.set(Calendar.MILLISECOND, 0);

            if (today.before(pickup)) {
                return "upcoming";
            } else if (!today.after(drop)) {
                return "ongoing";
            } else {
                return "completed";
            }
        } catch (Exception e) {
            return "completed";
        }
    }

    /**
     * Checks if a booking can be cancelled.
     * Rule: Can be cancelled ONLY IF there is at least 2 days left before booking starts (pickup_date - today >= 2 days).
     */
    public static boolean canCancelBooking(String pickupDateStr) {
        try {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar pickup = Calendar.getInstance();
            pickup.setTime(dateFormat.parse(pickupDateStr));
            pickup.set(Calendar.HOUR_OF_DAY, 0);
            pickup.set(Calendar.MINUTE, 0);
            pickup.set(Calendar.SECOND, 0);
            pickup.set(Calendar.MILLISECOND, 0);

            long diffInMillis = pickup.getTimeInMillis() - today.getTimeInMillis();
            long diffDays = diffInMillis / (1000 * 60 * 60 * 24);

            return diffDays >= 2;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks user bookings and sends system push notifications for:
     * 1. Pickup date is tomorrow ("tomorrow is upcoming booking")
     * 2. Drop date is tomorrow ("tomorrow is drop off last date")
     */
    public static void checkBookingReminders(Context context, String username) {
        if (context == null || username == null) return;

        DatabaseHandler dbHandler = new DatabaseHandler(context);
        Cursor cursor = dbHandler.getUserBookings(username);

        if (cursor != null) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

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
                    if (!pickupDateStr.isEmpty()) {
                        Calendar pickup = Calendar.getInstance();
                        pickup.setTime(dateFormat.parse(pickupDateStr));
                        pickup.set(Calendar.HOUR_OF_DAY, 0);
                        pickup.set(Calendar.MINUTE, 0);
                        pickup.set(Calendar.SECOND, 0);
                        pickup.set(Calendar.MILLISECOND, 0);

                        if (pickup.equals(tomorrow)) {
                            sendNotification(context, "Upcoming Booking Reminder",
                                    "Tomorrow is your upcoming booking for " + vehicleName + "!");
                        }
                    }

                    if (!dropDateStr.isEmpty()) {
                        Calendar drop = Calendar.getInstance();
                        drop.setTime(dateFormat.parse(dropDateStr));
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

    private static void sendNotification(Context context, String title, String message) {
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
