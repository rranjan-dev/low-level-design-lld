package com.lld.elevatorsystemsimple.models;

import com.lld.elevatorsystemsimple.dispatcher.DispatchStrategy;
import com.lld.elevatorsystemsimple.enums.Direction;

import java.util.ArrayList;
import java.util.List;

public class ElevatorSystem {
    private static final int MAX_ELEVATORS = 3;
    private static final int TOTAL_FLOORS = 15;

    private static ElevatorSystem instance;

    private final String buildingName;
    private final List<Elevator> elevators;
    private final List<Floor> floors;
    private DispatchStrategy dispatchStrategy;

    private ElevatorSystem(String buildingName) {
        this.buildingName = buildingName;
        this.elevators = new ArrayList<>();
        this.floors = new ArrayList<>();
        for (int i = 0; i <= TOTAL_FLOORS; i++) {
            floors.add(new Floor(i));
        }
    }

    public static synchronized ElevatorSystem getInstance(String buildingName) {
        if (instance == null) {
            instance = new ElevatorSystem(buildingName);
        }
        return instance;
    }

    public static ElevatorSystem getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ElevatorSystem not initialized. Call getInstance(buildingName) first.");
        }
        return instance;
    }

    public void addElevator(Elevator elevator) {
        if (elevators.size() >= MAX_ELEVATORS) {
            System.out.println("Cannot add more elevators. Maximum " + MAX_ELEVATORS + " allowed.");
            return;
        }
        elevators.add(elevator);
    }

    public void setDispatchStrategy(DispatchStrategy strategy) {
        this.dispatchStrategy = strategy;
    }

    /**
     * Called when a passenger presses UP/DOWN on a floor's outside panel.
     * Returns the assigned elevator so the floor display can show it.
     */
    public Elevator requestElevator(int fromFloor, Direction direction) {
        if (dispatchStrategy == null) {
            throw new IllegalStateException("Dispatch strategy not set");
        }
        if (fromFloor < 0 || fromFloor > TOTAL_FLOORS) {
            throw new IllegalArgumentException("Invalid floor: " + fromFloor);
        }

        Elevator assigned = dispatchStrategy.selectElevator(elevators, fromFloor, direction);
        if (assigned == null) {
            System.out.println("No elevator available. Please wait.");
            return null;
        }

        assigned.addDestination(fromFloor);
        System.out.println("  -> Elevator " + assigned.getElevatorId()
                + " assigned to Floor " + fromFloor + " (" + direction + ")");
        return assigned;
    }

    /**
     * Called when a passenger presses a floor button inside the elevator.
     */
    public void selectFloor(Elevator elevator, int destinationFloor) {
        if (destinationFloor < 0 || destinationFloor > TOTAL_FLOORS) {
            throw new IllegalArgumentException("Invalid floor: " + destinationFloor);
        }
        elevator.addDestination(destinationFloor);
        System.out.println("  -> " + elevator.getElevatorId()
                + " destination set to Floor " + destinationFloor);
    }

    public void dispatchAll() {
        for (Elevator elevator : elevators) {
            if (!elevator.isIdle()) {
                elevator.processAllStops();
            }
        }
    }

    public void showStatus() {
        System.out.println("\n=== " + buildingName + " — Elevator Status ===");
        for (Elevator e : elevators) {
            System.out.println(e.getStatus());
        }
        System.out.println();
    }

    public void showFloorDisplays() {
        for (Floor floor : floors) {
            floor.getExternalDisplay().showAll(elevators);
        }
    }

    public List<Elevator> getElevators() { return new ArrayList<>(elevators); }
    public List<Floor> getFloors() { return new ArrayList<>(floors); }
    public int getTotalFloors() { return TOTAL_FLOORS; }
    public String getBuildingName() { return buildingName; }
}
