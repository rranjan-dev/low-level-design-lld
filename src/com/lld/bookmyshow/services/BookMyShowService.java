package com.lld.bookmyshow.services;

import com.lld.bookmyshow.enums.City;
import com.lld.bookmyshow.models.*;
import com.lld.bookmyshow.pricing.PricingStrategy;
import java.util.List;

/**
 * Facade / Singleton orchestrator that ties all services together.
 * Entry point for the entire system — similar to ParkingLot in parking lot LLD.
 */
public class BookMyShowService {
    private static BookMyShowService instance;

    private final MovieService movieService;
    private final TheatreService theatreService;
    private final ShowService showService;
    private final BookingService bookingService;

    private BookMyShowService() {
        this.movieService = new MovieService();
        this.theatreService = new TheatreService();
        this.showService = new ShowService(theatreService);
        this.bookingService = new BookingService();
    }

    public static synchronized BookMyShowService getInstance() {
        if (instance == null) {
            instance = new BookMyShowService();
        }
        return instance;
    }

    // --- Movie operations ---
    public void addMovie(Movie movie) {
        movieService.addMovie(movie);
    }

    public List<Movie> searchMovies(String keyword) {
        return movieService.searchByTitle(keyword);
    }

    // --- Theatre operations ---
    public void addTheatre(Theatre theatre) {
        theatreService.addTheatre(theatre);
    }

    public List<Theatre> getTheatresInCity(City city) {
        return theatreService.getTheatresByCity(city);
    }

    // --- Show operations ---
    public void addShow(Show show) {
        show.initializeSeats();
        showService.addShow(show);
    }

    public void applyPricing(Show show, PricingStrategy strategy) {
        showService.applyPricing(show, strategy);
    }

    public List<Show> getShowsForMovie(Movie movie, City city) {
        return showService.getShowsForMovieInCity(movie, city);
    }

    public List<ShowSeat> getAvailableSeats(Show show) {
        return show.getAvailableSeats();
    }

    // --- Booking operations ---
    public Booking bookSeats(User user, Show show, List<ShowSeat> seats) {
        return bookingService.createBooking(user, show, seats);
    }

    public void confirmBooking(String bookingId) {
        bookingService.confirmBooking(bookingId);
    }

    public void cancelBooking(String bookingId) {
        bookingService.cancelBooking(bookingId);
    }

    public List<Booking> getUserBookings(User user) {
        return bookingService.getBookingsForUser(user);
    }

    // --- Reset for testing ---
    public static void resetInstance() {
        instance = null;
    }
}
