package com.lld.bookmyshow.services;

import com.lld.bookmyshow.enums.City;
import com.lld.bookmyshow.models.Movie;
import com.lld.bookmyshow.models.Show;
import com.lld.bookmyshow.models.Theatre;
import com.lld.bookmyshow.pricing.PricingStrategy;
import com.lld.bookmyshow.models.ShowSeat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages shows (screenings). In production, backed by `show` table.
 *
 * Critical query: "Find all shows for Movie X in City Y on Date Z"
 * This is the MOST FREQUENT read query and must be optimized with:
 * - Composite index on (movie_id, city_id, show_date)
 * - Partition by show_date for efficient range scans
 */
public class ShowService {
    private final Map<String, Show> showsById;
    private final TheatreService theatreService;

    public ShowService(TheatreService theatreService) {
        this.showsById = new HashMap<>();
        this.theatreService = theatreService;
    }

    public void addShow(Show show) {
        showsById.put(show.getShowId(), show);
    }

    public Show getShow(String showId) {
        return showsById.get(showId);
    }

    /**
     * Core query: find shows for a movie in a given city.
     * In DB: SELECT s.* FROM show s
     *        JOIN screen sc ON s.screen_id = sc.id
     *        JOIN theatre t ON sc.theatre_id = t.id
     *        WHERE s.movie_id = ? AND t.city = ? AND s.show_date = ?
     *        ORDER BY s.start_time;
     */
    public List<Show> getShowsForMovieInCity(Movie movie, City city) {
        List<Show> result = new ArrayList<>();
        List<Theatre> theatres = theatreService.getTheatresByCity(city);
        for (Show show : showsById.values()) {
            if (show.getMovie().getMovieId().equals(movie.getMovieId())) {
                for (Theatre theatre : theatres) {
                    if (theatre.getScreens().contains(show.getScreen())) {
                        result.add(show);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void applyPricing(Show show, PricingStrategy strategy) {
        for (ShowSeat showSeat : show.getShowSeats()) {
            double price = strategy.calculatePrice(show, showSeat);
            showSeat.setPrice(price);
        }
    }
}
