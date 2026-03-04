package com.lld.bookmyshow;

import com.lld.bookmyshow.enums.City;
import com.lld.bookmyshow.enums.SeatType;
import com.lld.bookmyshow.exceptions.SeatNotAvailableException;
import com.lld.bookmyshow.models.*;
import com.lld.bookmyshow.pricing.ShowTimePricingStrategy;
import com.lld.bookmyshow.services.BookMyShowService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class BookMyShowDemo {

    public static void main(String[] args) {
        BookMyShowService service = BookMyShowService.getInstance();

        // --- Setup Movies ---
        Movie movie1 = new Movie("MOV-1", "RRR", "Epic action drama",
                Duration.ofMinutes(187), "Telugu", "Action", 8.5);
        Movie movie2 = new Movie("MOV-2", "Jawan", "Action thriller",
                Duration.ofMinutes(169), "Hindi", "Action", 7.8);
        service.addMovie(movie1);
        service.addMovie(movie2);

        // --- Setup Theatre with Screens ---
        Theatre theatre1 = new Theatre("TH-1", "PVR Forum Mall", "Koramangala", City.BANGALORE);
        Screen screen1 = new Screen("SCR-1", "Screen 1 - IMAX");
        Screen screen2 = new Screen("SCR-2", "Screen 2 - Dolby");

        addSeatsToScreen(screen1);
        addSeatsToScreen(screen2);

        theatre1.addScreen(screen1);
        theatre1.addScreen(screen2);
        service.addTheatre(theatre1);

        // --- Setup Shows ---
        LocalDateTime now = LocalDateTime.now();
        Show show1 = new Show("SH-1", movie1, screen1,
                now.plusHours(2), now.plusHours(5));
        Show show2 = new Show("SH-2", movie2, screen2,
                now.plusHours(3), now.plusHours(6));

        service.addShow(show1);
        service.addShow(show2);

        // --- Apply pricing ---
        service.applyPricing(show1, new ShowTimePricingStrategy());
        service.applyPricing(show2, new ShowTimePricingStrategy());

        System.out.println("=== BookMyShow - Movie Ticket Booking System ===\n");

        // --- Browse shows ---
        System.out.println("--- Shows for 'RRR' in Bangalore ---");
        List<Show> shows = service.getShowsForMovie(movie1, City.BANGALORE);
        for (Show show : shows) {
            System.out.println("  " + show);
        }
        System.out.println();

        // --- Check available seats ---
        System.out.println("--- Available Seats for: " + show1 + " ---");
        List<ShowSeat> available = service.getAvailableSeats(show1);
        System.out.println("  Total available: " + available.size());
        for (ShowSeat seat : available) {
            System.out.println("    " + seat);
        }
        System.out.println();

        // --- User books seats ---
        User user1 = new User("USR-1", "Rahul Ranjan", "rahul@example.com", "9876543210");
        User user2 = new User("USR-2", "Priya Sharma", "priya@example.com", "9876543211");

        System.out.println("--- Booking: " + user1.getName() + " books 2 PREMIUM seats ---");
        List<ShowSeat> seatsToBook = Arrays.asList(available.get(2), available.get(3));
        Booking booking1 = service.bookSeats(user1, show1, seatsToBook);
        System.out.println("  " + booking1);

        // --- Confirm with payment ---
        Payment payment1 = new Payment(booking1, booking1.getTotalAmount(), "UPI");
        payment1.markSuccess();
        System.out.println("  " + payment1);
        System.out.println();

        // --- Check availability after booking ---
        System.out.println("--- Available Seats After Booking ---");
        available = service.getAvailableSeats(show1);
        System.out.println("  Total available: " + available.size());
        System.out.println();

        // --- Another user tries same seats (double-booking prevention) ---
        System.out.println("--- " + user2.getName() + " tries to book same seats ---");
        try {
            List<ShowSeat> sameSeats = Arrays.asList(
                    show1.getShowSeats().get(2), show1.getShowSeats().get(3));
            service.bookSeats(user2, show1, sameSeats);
        } catch (SeatNotAvailableException e) {
            System.out.println("  BLOCKED: " + e.getMessage());
        }
        System.out.println();

        // --- User 2 books different seats ---
        System.out.println("--- " + user2.getName() + " books 2 REGULAR seats instead ---");
        List<ShowSeat> otherSeats = Arrays.asList(available.get(0), available.get(1));
        Booking booking2 = service.bookSeats(user2, show1, otherSeats);
        Payment payment2 = new Payment(booking2, booking2.getTotalAmount(), "Credit Card");
        payment2.markSuccess();
        System.out.println("  " + booking2);
        System.out.println("  " + payment2);
        System.out.println();

        // --- Cancel booking ---
        System.out.println("--- " + user1.getName() + " cancels booking ---");
        service.cancelBooking(booking1.getBookingId());
        System.out.println("  Booking status: " + booking1.getStatus());
        System.out.println("  Seats released back to available pool");
        System.out.println();

        // --- Final availability ---
        System.out.println("--- Final Seat Availability ---");
        available = service.getAvailableSeats(show1);
        System.out.println("  Total available: " + available.size());

        // --- User's booking history ---
        System.out.println();
        System.out.println("--- " + user1.getName() + "'s Bookings ---");
        for (Booking b : service.getUserBookings(user1)) {
            System.out.println("  " + b);
        }
        System.out.println();
        System.out.println("--- " + user2.getName() + "'s Bookings ---");
        for (Booking b : service.getUserBookings(user2)) {
            System.out.println("  " + b);
        }
    }

    private static void addSeatsToScreen(Screen screen) {
        int seatNum = 1;
        for (int i = 1; i <= 2; i++) {
            screen.addSeat(new Seat(screen.getScreenId() + "-R" + i + "S" + seatNum,
                    i, seatNum++, SeatType.REGULAR));
        }
        for (int i = 1; i <= 2; i++) {
            screen.addSeat(new Seat(screen.getScreenId() + "-R3S" + seatNum,
                    3, seatNum++, SeatType.PREMIUM));
        }
        for (int i = 1; i <= 2; i++) {
            screen.addSeat(new Seat(screen.getScreenId() + "-R4S" + seatNum,
                    4, seatNum++, SeatType.VIP));
        }
        screen.addSeat(new Seat(screen.getScreenId() + "-R5S" + seatNum,
                5, seatNum, SeatType.RECLINER));
    }
}
