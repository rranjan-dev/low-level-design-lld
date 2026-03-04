package com.lld.bookmyshow.models;

/**
 * ShowSeat is the per-show availability wrapper around a physical Seat.
 * Each Show generates one ShowSeat per physical seat in the screen.
 *
 * DB Insight: This is THE highest-volume table in the entire system.
 * show_count × avg_seats_per_screen = 200K shows/day × 200 seats = 40M rows/day.
 * Must be partitioned by show_date, indexed on (show_id, is_booked).
 * Locking strategy: SELECT ... FOR UPDATE on specific show_seat rows during booking.
 */
public class ShowSeat {
    private final Seat seat;
    private final Show show;
    private volatile boolean booked;
    private double price;

    public ShowSeat(Seat seat, Show show) {
        this.seat = seat;
        this.show = show;
        this.booked = false;
        this.price = seat.getSeatType().getBasePrice();
    }

    public synchronized boolean lockSeat() {
        if (booked) return false;
        this.booked = true;
        return true;
    }

    public synchronized void unlockSeat() {
        this.booked = false;
    }

    public boolean isAvailable() { return !booked; }
    public Seat getSeat() { return seat; }
    public Show getShow() { return show; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return seat.toString() + (booked ? " [BOOKED]" : " [AVAILABLE]") +
               " ₹" + String.format("%.0f", price);
    }
}
