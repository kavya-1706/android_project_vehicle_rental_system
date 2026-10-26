package com.example.android_vehicle_project;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * PastBookingsActivity displays all booking history for the logged-in customer.
 * Features:
 * 1. Dynamic date-based status evaluation ('upcoming', 'ongoing', 'completed') via BookingUtils.
 * 2. 2-day prior booking cancellation eligibility enforcement.
 * 3. Written feedback submission for completed bookings.
 */
public class PastBookingsActivity extends AppCompatActivity {

    // UI View Components
    private String username;
    private LinearLayout llBookingsContainer;
    private TextView tvNoBookings;
    private DatabaseHandler dbHandler;

    /**
     * Initializes activity views, extracts username extra, and loads customer booking cards.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_bookings);

        // Setup toolbar with back arrow navigation
        Toolbar toolbar = findViewById(R.id.toolbarPastBookings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind layout views
        llBookingsContainer = findViewById(R.id.llBookingsContainer);
        tvNoBookings = findViewById(R.id.tvNoBookings);

        // Extract customer username intent extra
        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = "priya";
        }

        dbHandler = new DatabaseHandler(this);
        loadCustomerBookings();
    }

    /**
     * Queries customer bookings from SQLite, computes dynamic statuses, and populates item_booking.xml cards.
     */
    private void loadCustomerBookings() {
        llBookingsContainer.removeAllViews();
        Cursor cursor = dbHandler.getUserBookings(username);

        if (cursor != null && cursor.getCount() > 0) {
            tvNoBookings.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(this);

            while (cursor.moveToNext()) {
                // Read database columns
                int bookingIdIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_BOOKING_ID);
                int vehicleNameIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_VEHICLE_NAME);
                int brandIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_BRAND);
                int modelIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_MODEL);
                int pickupIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PICKUP_DATE);
                int dropIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_DROP_DATE);
                int daysIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_DAYS);
                int amountIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_TOTAL_AMOUNT);
                int paymentIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_STATUS);
                int payTimeIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_PAYMENT_TIME);
                int feedbackIdx = cursor.getColumnIndex(DatabaseHandler.COLUMN_FEEDBACK);

                int bookingId = bookingIdIdx != -1 ? cursor.getInt(bookingIdIdx) : -1;
                String vehicleName = vehicleNameIdx != -1 ? cursor.getString(vehicleNameIdx) : "Vehicle";
                String brand = brandIdx != -1 ? cursor.getString(brandIdx) : "";
                String model = modelIdx != -1 ? cursor.getString(modelIdx) : "";
                String pickupDate = pickupIdx != -1 ? cursor.getString(pickupIdx) : "";
                String dropDate = dropIdx != -1 ? cursor.getString(dropIdx) : "";
                int totalDays = daysIdx != -1 ? cursor.getInt(daysIdx) : 1;
                double totalAmount = amountIdx != -1 ? cursor.getDouble(amountIdx) : 0.0;

                // Calculate dynamic status based on current date
                String bookingStatus = BookingUtils.calculateStatus(pickupDate, dropDate);
                String paymentStatus = paymentIdx != -1 ? cursor.getString(paymentIdx) : "success";
                String paymentTime = payTimeIdx != -1 ? cursor.getString(payTimeIdx) : "";
                String feedback = feedbackIdx != -1 ? cursor.getString(feedbackIdx) : "";

                // Inflate item card
                View cardView = inflater.inflate(R.layout.item_booking, llBookingsContainer, false);

                TextView tvVehicleName = cardView.findViewById(R.id.tvBookingVehicleName);
                TextView tvStatus = cardView.findViewById(R.id.tvBookingStatus);
                TextView tvBrand = cardView.findViewById(R.id.tvBookingVehicleBrand);
                TextView tvDates = cardView.findViewById(R.id.tvBookingDates);
                TextView tvAmount = cardView.findViewById(R.id.tvBookingTotalAmount);
                TextView tvPayment = cardView.findViewById(R.id.tvPaymentStatus);
                TextView tvBookingFeedback = cardView.findViewById(R.id.tvBookingFeedback);
                LinearLayout llInlineFeedbackContainer = cardView.findViewById(R.id.llInlineFeedbackContainer);
                android.widget.EditText etInlineFeedback = cardView.findViewById(R.id.etInlineFeedback);
                android.widget.Button btnSubmitInlineFeedback = cardView.findViewById(R.id.btnSubmitInlineFeedback);
                android.widget.Button btnCancelBooking = cardView.findViewById(R.id.btnCancelBooking);

                // Populate item views
                tvVehicleName.setText(vehicleName);
                tvBrand.setText(brand + " " + model);
                tvDates.setText(pickupDate + " to " + dropDate + " (" + totalDays + " days)");
                tvAmount.setText(String.format("₹%,.0f", totalAmount));
                tvStatus.setText(bookingStatus.toUpperCase());
                tvPayment.setText("Payment Status: " + paymentStatus.toUpperCase() + (paymentTime.isEmpty() ? "" : " at " + paymentTime));

                // Handle Completed Booking Feedback section
                if (bookingStatus.equalsIgnoreCase("completed")) {
                    if (feedback != null && !feedback.trim().isEmpty()) {
                        // Display submitted feedback comment
                        tvBookingFeedback.setVisibility(View.VISIBLE);
                        tvBookingFeedback.setText("Feedback: \"" + feedback + "\"");
                        llInlineFeedbackContainer.setVisibility(View.GONE);
                    } else {
                        // Show inline feedback input text box
                        tvBookingFeedback.setVisibility(View.GONE);
                        llInlineFeedbackContainer.setVisibility(View.VISIBLE);

                        btnSubmitInlineFeedback.setOnClickListener(v -> {
                            String comment = etInlineFeedback != null ? etInlineFeedback.getText().toString().trim() : "";
                            if (comment.isEmpty()) {
                                android.widget.Toast.makeText(this, "Please write your feedback before submitting", android.widget.Toast.LENGTH_SHORT).show();
                                return;
                            }

                            boolean updated = dbHandler.updateBookingFeedback(bookingId, comment, 5.0);
                            if (updated) {
                                android.widget.Toast.makeText(this, "Feedback submitted successfully!", android.widget.Toast.LENGTH_SHORT).show();
                                loadCustomerBookings();
                            } else {
                                android.widget.Toast.makeText(this, "Failed to submit feedback", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    tvBookingFeedback.setVisibility(View.GONE);
                    llInlineFeedbackContainer.setVisibility(View.GONE);
                }

                // Handle Booking Cancellation (Allowed ONLY if UPCOMING and >= 2 days remaining before pickup)
                if (bookingStatus.equals("upcoming") && BookingUtils.canCancelBooking(pickupDate)) {
                    btnCancelBooking.setVisibility(View.VISIBLE);
                    btnCancelBooking.setOnClickListener(v -> {
                        androidx.appcompat.app.AlertDialog cancelDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Cancel Booking")
                                .setMessage("Are you sure you want to cancel this booking?")
                                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                                    boolean deleted = dbHandler.deleteBooking(bookingId);
                                    if (deleted) {
                                        android.widget.Toast.makeText(this, "Booking cancelled successfully", android.widget.Toast.LENGTH_SHORT).show();
                                        loadCustomerBookings();
                                    } else {
                                        android.widget.Toast.makeText(this, "Failed to cancel booking", android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .create();
                        cancelDialog.show();
                        if (cancelDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null) {
                            cancelDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.WHITE);
                        }
                        if (cancelDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
                            cancelDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.WHITE);
                        }
                    });
                } else {
                    btnCancelBooking.setVisibility(View.GONE);
                }

                llBookingsContainer.addView(cardView);
            }
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            tvNoBookings.setVisibility(View.VISIBLE);
        }
    }
}
