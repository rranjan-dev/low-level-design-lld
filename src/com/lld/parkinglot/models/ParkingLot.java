package com.lld.parkinglot.models;

import com.lld.parkinglot.pricing.PricingStrategy;
import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private static ParkingLot instance;
    
    private final String name;
    private final List<ParkingFloor> floors;
    private PricingStrategy pricingStrategy;

    private ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
    }

    public static synchronized ParkingLot getInstance(String name) {
        if (instance == null) {
            instance = new ParkingLot(name);
        }
        return instance;
    }

    public static ParkingLot getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ParkingLot not initialized");
        }
        return instance;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public synchronized ParkingTicket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle.getVehicleType());
            if (spot != null && spot.isAvailable()) {
                spot.assignVehicle(vehicle);
                return new ParkingTicket(vehicle, spot);
            }
        }
        throw new RuntimeException("No available spot for " + vehicle);
    }

    public synchronized double unparkVehicle(ParkingTicket ticket) {
        if (pricingStrategy == null) {
            throw new IllegalStateException("Pricing strategy not set");
        }
        ticket.markExitTime();  // Mark exit time first
        ticket.getSpot().removeVehicle();  // Then free the spot
        double charges = pricingStrategy.calculateCharge(ticket);  // Calculate charges using recorded exit time
        ticket.setCharges(charges);  // Store charges
        return charges;
    }

    /**
     * Returns status display showing availability on each floor.
     * Example output:
     * === City Center Parking Status ===
     *   Floor 1: SMALL=2/2  MEDIUM=3/3  LARGE=1/1
     *   Floor 2: SMALL=1/1  MEDIUM=2/2  LARGE=2/2
     *   Total available: 11
     */
    public String getStatusDisplay() {
        String result = "=== " + name + " Status ===\n";
        int total = 0;
        for (ParkingFloor floor : floors) {
            result += floor.getStatusDisplay() + "\n";
            total += floor.getTotalAvailableCount();
        }
        result += "  Total available: " + total;
        return result;
    }

    public String getName() {
        return name;
    }
}
