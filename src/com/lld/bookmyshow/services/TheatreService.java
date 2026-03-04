package com.lld.bookmyshow.services;

import com.lld.bookmyshow.enums.City;
import com.lld.bookmyshow.models.Theatre;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages theatres. In production, backed by `theatre` table.
 * Query pattern: filter by city, paginated listing.
 * Index on (city) for city-based theatre lookup — most common query.
 */
public class TheatreService {
    private final Map<String, Theatre> theatresById;

    public TheatreService() {
        this.theatresById = new HashMap<>();
    }

    public void addTheatre(Theatre theatre) {
        theatresById.put(theatre.getTheatreId(), theatre);
    }

    public Theatre getTheatre(String theatreId) {
        return theatresById.get(theatreId);
    }

    public List<Theatre> getTheatresByCity(City city) {
        List<Theatre> result = new ArrayList<>();
        for (Theatre theatre : theatresById.values()) {
            if (theatre.getCity() == city) {
                result.add(theatre);
            }
        }
        return result;
    }
}
