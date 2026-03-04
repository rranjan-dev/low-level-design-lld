package com.lld.bookmyshow.models;

import com.lld.bookmyshow.enums.PaymentStatus;
import java.time.LocalDateTime;

/**
 * Payment linked to a Booking. Separate from Booking for SRP and
 * because payments have their own lifecycle (retries, refunds).
 *
 * DB Insight: 1:1 with Booking initially, but supports multiple attempts (1:N).
 * Index on (booking_id) for payment lookup.
 * Index on (payment_status, created_at) for reconciliation batch jobs.
 */
public class Payment {
    private static int paymentCounter = 1;

    private final String paymentId;
    private final Booking booking;
    private final double amount;
    private final String paymentMethod;
    private final LocalDateTime paymentTime;
    private PaymentStatus status;

    public Payment(Booking booking, double amount, String paymentMethod) {
        this.paymentId = "PAY-" + paymentCounter++;
        this.booking = booking;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
        booking.confirm();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
        booking.expire();
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    public String getPaymentId() { return paymentId; }
    public Booking getBooking() { return booking; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public PaymentStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "Payment[" + paymentId + "] ₹" + String.format("%.0f", amount) +
               " via " + paymentMethod + " | " + status;
    }
}
