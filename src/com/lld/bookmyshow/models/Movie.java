package com.lld.bookmyshow.models;

import java.time.Duration;
import java.util.List;

public class Movie {
    private final String movieId;
    private final String title;
    private final String description;
    private final Duration duration;
    private final String language;
    private final String genre;
    private final double rating;

    public Movie(String movieId, String title, String description,
                 Duration duration, String language, String genre, double rating) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.language = language;
        this.genre = genre;
        this.rating = rating;
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Duration getDuration() { return duration; }
    public String getLanguage() { return language; }
    public String getGenre() { return genre; }
    public double getRating() { return rating; }

    @Override
    public String toString() {
        return title + " [" + language + "] (" + duration.toMinutes() + " min) ★" + rating;
    }
}
