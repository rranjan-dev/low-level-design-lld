package com.lld.bookmyshow.models;

import java.util.ArrayList;
import java.util.List;

public class Screen {
    private final String screenId;
    private final String name;
    private final List<Seat> seats;

    public Screen(String screenId, String name) {
        this.screenId = screenId;
        this.name = name;
        this.seats = new ArrayList<>();
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public List<Seat> getSeats() { return seats; }
    public String getScreenId() { return screenId; }
    public String getName() { return name; }
    public int getTotalSeats() { return seats.size(); }

    @Override
    public String toString() {
        return name + " (" + seats.size() + " seats)";
    }
}
