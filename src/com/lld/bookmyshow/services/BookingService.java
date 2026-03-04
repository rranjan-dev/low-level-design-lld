package com.lld.bookmyshow.services;

import com.lld.bookmyshow.enums.BookingStatus;
import com.lld.bookmyshow.exceptions.SeatNotAvailableException;
import com.lld.bookmyshow.models.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles seat locking and booking creation.
 *
 * DB Insight: Booking flow uses pessimistic locking (SELECT ... FOR UPDATE)
 * on show_seat rows to prevent double-booking. The lock is held only during
 * the seat selection → payment window (~5 minutes TTL).
 *
 * Transaction isolation: READ COMMITTED is sufficient here because we
 * lock specific rows, not ranges.
 */
public class BookingService {
    private final Map<String, Booking> bookingsById;

    public BookingService() {
        this.bookingsById = new HashMap<>();
    }

    /**
     * Atomically locks requested seats and creates a PENDING booking.
     * In DB: BEGIN → SELECT ... FOR UPDATE on show_seat rows → INSERT booking → COMMIT.
     * If any seat is already booked, rolls back all locks.
     */
    public synchronized Booking createBooking(User user, Show show, List<ShowSeat> requestedSeats) {
        List<ShowSeat> lockedSeats = new ArrayList<>();

        try {
            for (ShowSeat showSeat : requestedSeats) {
                if (!showSeat.lockSeat()) {
                    rollbackLockedSeats(lockedSeats);
                    throw new SeatNotAvailableException(
                        "Seat " + showSeat.getSeat() + " is no longer available");
                }
                lockedSeats.add(showSeat);
            }

            double totalAmount = 0;
            for (ShowSeat seat : lockedSeats) {
                totalAmount += seat.getPrice();
            }

            Booking booking = new Booking(user, show, lockedSeats, totalAmount);
            bookingsById.put(booking.getBookingId(), booking);
            return booking;

        } catch (SeatNotAvailableException e) {
            throw e;
        }
    }

    public void confirmBooking(String bookingId) {
        Booking booking = bookingsById.get(bookingId);
        if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
            booking.confirm();
        }
    }

    public void cancelBooking(String bookingId) {
        Booking booking = bookingsById.get(bookingId);
        if (booking != null && booking.getStatus() != BookingStatus.CANCELLED) {
            booking.cancel();
        }
    }

    public Booking getBooking(String bookingId) {
        return bookingsById.get(bookingId);
    }

    public List<Booking> getBookingsForUser(User user) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookingsById.values()) {
            if (booking.getUser().getUserId().equals(user.getUserId())) {
                result.add(booking);
            }
        }
        return result;
    }

    private void rollbackLockedSeats(List<ShowSeat> lockedSeats) {
        for (ShowSeat seat : lockedSeats) {
            seat.unlockSeat();
        }
    }
}
