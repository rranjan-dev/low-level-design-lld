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

    public String getStatusDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Floor ").append(floorNumber).append(": ");
        sb.append("SMALL=").append(getAvailableCount(SpotType.SMALL))
          .append("/").append(spotsByType.get(SpotType.SMALL).size()).append("  ");
        sb.append("MEDIUM=").append(getAvailableCount(SpotType.MEDIUM))
          .append("/").append(spotsByType.get(SpotType.MEDIUM).size()).append("  ");
        sb.append("LARGE=").append(getAvailableCount(SpotType.LARGE))
          .append("/").append(spotsByType.get(SpotType.LARGE).size());
        return sb.toString();
    }

    public int getFloorNumber() {
        return floorNumber;
    }
}
