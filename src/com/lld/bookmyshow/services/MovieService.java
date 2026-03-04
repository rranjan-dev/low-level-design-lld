package com.lld.bookmyshow.services;

import com.lld.bookmyshow.models.Movie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages movie catalogue. In production, backed by `movie` table.
 * Query pattern: search by title, filter by language/genre, sort by rating.
 */
public class MovieService {
    private final Map<String, Movie> moviesById;

    public MovieService() {
        this.moviesById = new HashMap<>();
    }

    public void addMovie(Movie movie) {
        moviesById.put(movie.getMovieId(), movie);
    }

    public Movie getMovie(String movieId) {
        return moviesById.get(movieId);
    }

    public List<Movie> searchByTitle(String keyword) {
        List<Movie> results = new ArrayList<>();
        for (Movie movie : moviesById.values()) {
            if (movie.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(movie);
            }
        }
        return results;
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(moviesById.values());
    }
}
