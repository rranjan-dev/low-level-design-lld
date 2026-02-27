package com.lld.parkinglot.models;

import com.lld.parkinglot.panels.EntryPanel;
import com.lld.parkinglot.panels.ExitPanel;
import com.lld.parkinglot.pricing.PricingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Parking Lot that orchestrates vehicle parking and unparking.
 * Uses double-checked locking for thread-safe lazy initialization.
 */
public class ParkingLot {
    private static volatile ParkingLot instance;

    private final String name;
    private final List<ParkingFloor> floors;
    private final List<EntryPanel> entryPanels;
    private final List<ExitPanel> exitPanels;
    private PricingStrategy pricingStrategy;

    private ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
        this.entryPanels = new ArrayList<>();
        this.exitPanels = new ArrayList<>();
    }

    public static ParkingLot getInstance(String name) {
        if (instance == null) {
            synchronized (ParkingLot.class) {
                if (instance == null) {
                    instance = new ParkingLot(name);
                }
            }
        }
        return instance;
    }

    public static ParkingLot getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ParkingLot has not been initialized. Call getInstance(name) first.");
        }
        return instance;
    }

    /** Reset singleton -- useful for testing */
    public static void resetInstance() {
        instance = null;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    public void addEntryPanel(EntryPanel panel) {
        entryPanels.add(panel);
    }

    public void addExitPanel(ExitPanel panel) {
        exitPanels.add(panel);
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    /**
     * Finds an available spot across all floors and parks the vehicle.
     * Synchronized to prevent two threads from grabbing the same spot.
     */
    public synchronized ParkingTicket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle.getVehicleType());
            if (spot != null) {
                spot.assignVehicle(vehicle);
                return ParkingTicket.issue(vehicle, spot);
            }
        }
        throw new RuntimeException("No available spot for " + vehicle);
    }

    /**
     * Frees the spot and calculates the charge using the configured pricing strategy.
     */
    public synchronized double unparkVehicle(ParkingTicket ticket) {
        if (pricingStrategy == null) {
            throw new IllegalStateException("Pricing strategy not configured");
        }
        ticket.getSpot().removeVehicle();
        double charges = pricingStrategy.calculateCharge(ticket);
        ticket.markExit(charges);
        return charges;
    }

    public String getStatusDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(name).append(" Status ===\n");
        for (ParkingFloor floor : floors) {
            sb.append(floor.getStatusDisplay()).append("\n");
        }
        int totalAvailable = floors.stream().mapToInt(ParkingFloor::getTotalAvailableCount).sum();
        sb.append("  Total available spots: ").append(totalAvailable);
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public List<ParkingFloor> getFloors() {
        return floors;
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }
}
