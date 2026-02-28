package com.lld.elevatorsystem.models;

import com.lld.elevatorsystem.enums.Direction;
import com.lld.elevatorsystem.enums.ElevatorState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Elevator {
    private final String elevatorId;
    private final int maxCapacity;
    private int currentFloor;
    private ElevatorState state;
    private Direction direction;
    private int passengerCount;
    private final List<ElevatorRequest> pendingRequests;

    public Elevator(String elevatorId, int maxCapacity) {
        this.elevatorId = elevatorId;
        this.maxCapacity = maxCapacity;
        this.currentFloor = 0;
        this.state = ElevatorState.IDLE;
        this.direction = Direction.IDLE;
        this.passengerCount = 0;
        this.pendingRequests = new ArrayList<>();
    }

    /**
     * Queues a request. Does NOT move the elevator.
     * The panel can immediately display "Go to E1" after this returns.
     */
    public synchronized void addRequest(ElevatorRequest request) {
        pendingRequests.add(request);
    }

    /**
     * Does this elevator already have a pending pickup at this floor?
     * Used by strategy to group passengers at the same floor into one elevator.
     */
    public boolean hasPendingPickupAt(int floor) {
        for (ElevatorRequest req : pendingRequests) {
            if (req.getSourceFloor() == floor) return true;
        }
        return false;
    }

    /**
     * Processes all queued requests in batch:
     *   1. Move to source floor
     *   2. Pick up ALL passengers waiting there
     *   3. Visit each destination in order, dropping off passengers
     */
    public synchronized void processPendingRequests() {
        if (pendingRequests.isEmpty()) return;

        List<ElevatorRequest> toProcess = new ArrayList<>(pendingRequests);
        pendingRequests.clear();

        // Group by source floor
        Map<Integer, List<ElevatorRequest>> bySource = new TreeMap<>();
        for (ElevatorRequest req : toProcess) {
            bySource.computeIfAbsent(req.getSourceFloor(), k -> new ArrayList<>()).add(req);
        }

        for (Map.Entry<Integer, List<ElevatorRequest>> sourceEntry : bySource.entrySet()) {
            List<ElevatorRequest> passengers = sourceEntry.getValue();

            // Move to pickup floor
            moveTo(sourceEntry.getKey());

            // Pick up everyone at this floor
            for (ElevatorRequest req : passengers) {
                passengerCount++;
                System.out.println("[" + elevatorId + "] Picked up " + req.getPerson()
                    + " at Floor " + currentFloor);
            }

            // Group by destination, visit each in sorted order
            TreeMap<Integer, List<ElevatorRequest>> byDest = new TreeMap<>();
            for (ElevatorRequest req : passengers) {
                byDest.computeIfAbsent(req.getDestinationFloor(), k -> new ArrayList<>()).add(req);
            }

            for (Map.Entry<Integer, List<ElevatorRequest>> destEntry : byDest.entrySet()) {
                moveTo(destEntry.getKey());
                for (ElevatorRequest req : destEntry.getValue()) {
                    passengerCount--;
                    System.out.println("[" + elevatorId + "] Dropped off " + req.getPerson()
                        + " at Floor " + currentFloor);
                }
            }
        }

        if (passengerCount == 0) {
            state = ElevatorState.IDLE;
            direction = Direction.IDLE;
        }
    }

    private void moveTo(int targetFloor) {
        if (targetFloor == currentFloor) return;

        direction = targetFloor > currentFloor ? Direction.UP : Direction.DOWN;
        state = ElevatorState.MOVING;

        System.out.println("[" + elevatorId + "] Moving " + direction
            + ": Floor " + currentFloor + " -> Floor " + targetFloor);
        currentFloor = targetFloor;
    }

    public boolean canServe(int floor, Direction requestDirection) {
        if (state == ElevatorState.MAINTENANCE) return false;
        if (!isAvailable()) return false;
        if (state == ElevatorState.IDLE) return true;

        if (direction == requestDirection) {
            if (requestDirection == Direction.UP && currentFloor <= floor) return true;
            if (requestDirection == Direction.DOWN && currentFloor >= floor) return true;
        }
        return false;
    }

    public int getDistanceTo(int floor) {
        return Math.abs(currentFloor - floor);
    }

    public void setMaintenance(boolean maintenance) {
        this.state = maintenance ? ElevatorState.MAINTENANCE : ElevatorState.IDLE;
        if (!maintenance) this.direction = Direction.IDLE;
    }

    public String getElevatorId() { return elevatorId; }
    public int getCurrentFloor() { return currentFloor; }
    public ElevatorState getState() { return state; }
    public Direction getDirection() { return direction; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getPendingCount() { return pendingRequests.size(); }

    public boolean isAvailable() {
        return state != ElevatorState.MAINTENANCE
            && (passengerCount + pendingRequests.size()) < maxCapacity;
    }

    public String getStatusDisplay() {
        String display = String.format("Elevator %s: Floor %d, %s, Passengers: %d/%d",
                elevatorId, currentFloor, state, passengerCount, maxCapacity);
        if (!pendingRequests.isEmpty()) {
            display += ", Pending: " + pendingRequests.size();
        }
        return display;
    }
}
