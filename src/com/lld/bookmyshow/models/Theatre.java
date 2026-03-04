package com.lld.bookmyshow.models;

import com.lld.bookmyshow.enums.City;
import java.util.ArrayList;
import java.util.List;

public class Theatre {
    private final String theatreId;
    private final String name;
    private final String address;
    private final City city;
    private final List<Screen> screens;

    public Theatre(String theatreId, String name, String address, City city) {
        this.theatreId = theatreId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.screens = new ArrayList<>();
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
    }

    public String getTheatreId() { return theatreId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public City getCity() { return city; }
    public List<Screen> getScreens() { return screens; }

    @Override
    public String toString() {
        return name + " [" + city + "] (" + screens.size() + " screens)";
    }
}
