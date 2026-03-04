package com.lld.bookmyshow.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A Show represents a specific screening of a Movie on a Screen at a particular time.
 * This is the central entity — it links Movie, Screen, and ShowSeats together.
 *
 * DB Insight: The `show` table is one of the highest-volume tables.
 * With ~10K theatres × 5 screens × 4 shows/day = 200K rows/day, ~73M rows/year.
 * Partition by show_date for efficient range queries.
 */
public class Show {
    private final String showId;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<ShowSeat> showSeats;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM HH:mm");

    public Show(String showId, Movie movie, Screen screen,
                LocalDateTime startTime, LocalDateTime endTime) {
        this.showId = showId;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.endTime = endTime;
        this.showSeats = new ArrayList<>();
    }

    public void initializeSeats() {
        for (Seat seat : screen.getSeats()) {
            showSeats.add(new ShowSeat(seat, this));
        }
    }

    public List<ShowSeat> getAvailableSeats() {
        List<ShowSeat> available = new ArrayList<>();
        for (ShowSeat showSeat : showSeats) {
            if (showSeat.isAvailable()) {
                available.add(showSeat);
            }
        }
        return available;
    }

    public String getShowId() { return showId; }
    public Movie getMovie() { return movie; }
    public Screen getScreen() { return screen; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<ShowSeat> getShowSeats() { return showSeats; }

    @Override
    public String toString() {
        return movie.getTitle() + " @ " + screen.getName() + " | " + startTime.format(FMT);
    }
}
