package com.lld.elevatorsystem.models;

import com.lld.elevatorsystem.strategy.ElevatorSelectionStrategy;
import java.util.ArrayList;
import java.util.List;

public class ElevatorSystem {
    private static ElevatorSystem instance;

    private final String buildingName;
    private final int totalFloors;
    private final List<Elevator> elevators;
    private ElevatorSelectionStrategy selectionStrategy;

    private ElevatorSystem(String buildingName, int totalFloors) {
        this.buildingName = buildingName;
        this.totalFloors = totalFloors;
        this.elevators = new ArrayList<>();
    }

    public static synchronized ElevatorSystem getInstance(String buildingName, int totalFloors) {
        if (instance == null) {
            instance = new ElevatorSystem(buildingName, totalFloors);
        }
        return instance;
    }

    public static ElevatorSystem getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ElevatorSystem not initialized");
        }
        return instance;
    }

    public void addElevator(Elevator elevator) {
        elevators.add(elevator);
    }

    public void setSelectionStrategy(ElevatorSelectionStrategy strategy) {
        this.selectionStrategy = strategy;
    }

    /**
     * Assigns the best elevator and queues the request.
     * Does NOT move the elevator — returns instantly so the panel can display
     * "Go to E1" without waiting.
     *
     * Call dispatchElevators() to actually move the elevators.
     */
    public synchronized ElevatorRequest requestElevator(Person person, int sourceFloor, int destinationFloor) {
        if (sourceFloor < 0 || sourceFloor > totalFloors ||
            destinationFloor < 0 || destinationFloor > totalFloors) {
            throw new IllegalArgumentException("Invalid floor. Building has floors 0 to " + totalFloors);
        }
        if (selectionStrategy == null) {
            throw new IllegalStateException("Elevator selection strategy not set");
        }

        ElevatorRequest request = new ElevatorRequest(person, sourceFloor, destinationFloor);

        Elevator selected = selectionStrategy.selectElevator(
            elevators, sourceFloor, request.getDirection());

        if (selected == null) {
            throw new RuntimeException("No available elevator for " + request);
        }

        request.assignElevator(selected);
        selected.addRequest(request);
        return request;
    }

    /**
     * Dispatches all elevators to process their queued requests.
     * Each elevator picks up all assigned passengers and delivers them.
     */
    public synchronized void dispatchElevators() {
        for (Elevator elevator : elevators) {
            elevator.processPendingRequests();
        }
    }

    public String getStatusDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(buildingName).append(" Elevator System ===\n");
        for (Elevator e : elevators) {
            sb.append("  ").append(e.getStatusDisplay()).append("\n");
        }
        return sb.toString();
    }

    public String getBuildingName() { return buildingName; }
    public int getTotalFloors() { return totalFloors; }
    public List<Elevator> getElevators() { return new ArrayList<>(elevators); }
}
