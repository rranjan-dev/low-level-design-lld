package com.lld.parkinglot.models;

import com.lld.parkinglot.enums.SpotType;
import com.lld.parkinglot.enums.VehicleType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingFloor {
    private final int floorNumber;
    private final Map<SpotType, List<ParkingSpot>> spotsByType;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spotsByType = new HashMap<>();
        spotsByType.put(SpotType.SMALL, new ArrayList<>());
        spotsByType.put(SpotType.MEDIUM, new ArrayList<>());
        spotsByType.put(SpotType.LARGE, new ArrayList<>());
    }

    public void addSpot(ParkingSpot spot) {
        spotsByType.get(spot.getSpotType()).add(spot);
    }

    public ParkingSpot findAvailableSpot(VehicleType vehicleType) {
        SpotType required = vehicleType.getRequiredSpotType();
        
        // Try exact match first
        ParkingSpot spot = findFirstAvailable(required);
        if (spot != null) {
            return spot;
        }
        
        // Try larger spots if needed
        if (required == SpotType.SMALL) {
            spot = findFirstAvailable(SpotType.MEDIUM);
            if (spot != null) return spot;
            return findFirstAvailable(SpotType.LARGE);
        } else if (required == SpotType.MEDIUM) {
            return findFirstAvailable(SpotType.LARGE);
        }
        
        return null;
    }

    private ParkingSpot findFirstAvailable(SpotType spotType) {
        for (ParkingSpot spot : spotsByType.get(spotType)) {
            if (spot.isAvailable()) {
                return spot;
            }
        }
        return null;
    }

    public int getAvailableCount(SpotType spotType) {
        int count = 0;
        for (ParkingSpot spot : spotsByType.get(spotType)) {
            if (spot.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalAvailableCount() {
        return getAvailableCount(SpotType.SMALL) + 
               getAvailableCount(SpotType.MEDIUM) + 
               getAvailableCount(SpotType.LARGE);
    }

    /**
     * Returns floor status display.
     * Example output: "  Floor 1: SMALL=2/2  MEDIUM=3/3  LARGE=1/1"
     * Format: Floor {number}: SMALL={available}/{total}  MEDIUM={available}/{total}  LARGE={available}/{total}
     */
    public String getStatusDisplay() {
        int smallAvailable = getAvailableCount(SpotType.SMALL);
        int smallTotal = spotsByType.get(SpotType.SMALL).size();
        int mediumAvailable = getAvailableCount(SpotType.MEDIUM);
        int mediumTotal = spotsByType.get(SpotType.MEDIUM).size();
        int largeAvailable = getAvailableCount(SpotType.LARGE);
        int largeTotal = spotsByType.get(SpotType.LARGE).size();
        
        return "  Floor " + floorNumber + ": " +
               "SMALL=" + smallAvailable + "/" + smallTotal + "  " +
               "MEDIUM=" + mediumAvailable + "/" + mediumTotal + "  " +
               "LARGE=" + largeAvailable + "/" + largeTotal;
    }

    public int getFloorNumber() {
        return floorNumber;
    }
}
