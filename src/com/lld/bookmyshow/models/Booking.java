package com.lld.bookmyshow.models;

import com.lld.bookmyshow.enums.BookingStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Booking ties a User to a set of ShowSeats for a specific Show.
 *
 * DB Insight: High-volume transactional table.
 * ~500K bookings/day across all shows. Partition by booking_date.
 * Index on (user_id, booking_status) for "my bookings" queries.
 * Index on (show_id, booking_status) for "show occupancy" queries.
 */
public class Booking {
    private static int bookingCounter = 1;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private final String bookingId;
    private final User user;
    private final Show show;
    private final List<ShowSeat> bookedSeats;
    private final LocalDateTime bookingTime;
    private BookingStatus status;
    private double totalAmount;

    public Booking(User user, Show show, List<ShowSeat> bookedSeats, double totalAmount) {
        this.bookingId = "BKG-" + bookingCounter++;
        this.user = user;
        this.show = show;
        this.bookedSeats = bookedSeats;
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.PENDING;
        this.totalAmount = totalAmount;
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        for (ShowSeat showSeat : bookedSeats) {
            showSeat.unlockSeat();
        }
    }

    public void expire() {
        this.status = BookingStatus.EXPIRED;
        for (ShowSeat showSeat : bookedSeats) {
            showSeat.unlockSeat();
        }
    }

    public String getBookingId() { return bookingId; }
    public User getUser() { return user; }
    public Show getShow() { return show; }
    public List<ShowSeat> getBookedSeats() { return bookedSeats; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public BookingStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }

    @Override
    public String toString() {
        return "Booking[" + bookingId + "] " + show.getMovie().getTitle() +
               " | " + bookedSeats.size() + " seats | ₹" + String.format("%.0f", totalAmount) +
               " | " + status + " | " + bookingTime.format(FMT);
    }
}
