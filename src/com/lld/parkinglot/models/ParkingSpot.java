package com.lld.parkinglot.models;

import com.lld.parkinglot.enums.SpotType;

public class ParkingSpot {
    private final String spotId;
    private final SpotType spotType;
    private boolean available;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.available = true;
    }

    public synchronized void assignVehicle(Vehicle vehicle) {
        if (!available) {
            throw new IllegalStateException("Spot " + spotId + " is already occupied");
        }
        this.parkedVehicle = vehicle;
        this.available = false;
    }

    public synchronized Vehicle removeVehicle() {
        if (available) {
            throw new IllegalStateException("Spot " + spotId + " is already empty");
        }
        Vehicle vehicle = this.parkedVehicle;
        this.parkedVehicle = null;
        this.available = true;
        return vehicle;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    @Override
    public String toString() {
        return spotId + " (" + spotType + ")" + (available ? " [FREE]" : " [OCCUPIED by " + parkedVehicle + "]");
    }
}
