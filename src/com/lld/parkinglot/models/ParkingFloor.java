package com.lld.parkinglot.models;

import com.lld.parkinglot.enums.SpotType;
import com.lld.parkinglot.enums.VehicleType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ParkingFloor {
    private final int floorNumber;
    private final Map<SpotType, List<ParkingSpot>> spotsByType;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spotsByType = new EnumMap<>(SpotType.class);
        for (SpotType type : SpotType.values()) {
            spotsByType.put(type, new ArrayList<>());
        }
    }

    public void addSpot(ParkingSpot spot) {
        spotsByType.get(spot.getSpotType()).add(spot);
    }

    /**
     * Finds an available spot that can fit the given vehicle type.
     * Tries the exact match first, then falls back to larger spots.
     */
    public ParkingSpot findAvailableSpot(VehicleType vehicleType) {
        SpotType required = vehicleType.getRequiredSpotType();

        for (SpotType spotType : SpotType.values()) {
            if (!spotType.canFit(required)) {
                continue;
            }
            for (ParkingSpot spot : spotsByType.get(spotType)) {
                if (spot.isAvailable()) {
                    return spot;
                }
            }
        }
        return null;
    }

    public int getAvailableCount(SpotType spotType) {
        return (int) spotsByType.get(spotType).stream()
                .filter(ParkingSpot::isAvailable)
                .count();
    }

    public int getTotalAvailableCount() {
        int count = 0;
        for (SpotType type : SpotType.values()) {
            count += getAvailableCount(type);
        }
        return count;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public String getStatusDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Floor ").append(floorNumber).append(": ");
        for (SpotType type : SpotType.values()) {
            List<ParkingSpot> spots = spotsByType.get(type);
            long available = spots.stream().filter(ParkingSpot::isAvailable).count();
            sb.append(type).append("=").append(available).append("/").append(spots.size()).append("  ");
        }
        return sb.toString();
    }
}
