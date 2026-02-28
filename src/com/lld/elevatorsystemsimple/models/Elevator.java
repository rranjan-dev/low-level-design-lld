package com.lld.elevatorsystemsimple.models;

import com.lld.elevatorsystemsimple.enums.Direction;
import com.lld.elevatorsystemsimple.enums.DoorState;
import com.lld.elevatorsystemsimple.display.InternalDisplay;

import java.util.TreeSet;

public class Elevator {
    private static final int MAX_CAPACITY = 8;
    private static final double MAX_WEIGHT_KG = 680.0;

    private final String elevatorId;
    private final InternalDisplay internalDisplay;
    private int currentFloor;
    private Direction direction;
    private DoorState doorState;
    private int passengerCount;
    private double currentWeightKg;
    private final TreeSet<Integer> upStops;
    private final TreeSet<Integer> downStops;

    public Elevator(String elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0;
        this.direction = Direction.IDLE;
        this.doorState = DoorState.CLOSED;
        this.passengerCount = 0;
        this.currentWeightKg = 0.0;
        this.upStops = new TreeSet<>();
        this.downStops = new TreeSet<>();
        this.internalDisplay = new InternalDisplay(this);
    }

    public void addDestination(int floor) {
        if (floor > currentFloor) {
            upStops.add(floor);
        } else if (floor < currentFloor) {
            downStops.add(floor);
        }
        if (direction == Direction.IDLE) {
            direction = floor > currentFloor ? Direction.UP : Direction.DOWN;
        }
    }

    public void moveOneFloor() {
        if (doorState == DoorState.OPEN) {
            System.out.println("  [" + elevatorId + "] Cannot move — door is open!");
            return;
        }
        if (direction == Direction.UP && !upStops.isEmpty()) {
            currentFloor++;
            System.out.println("  [" + elevatorId + "] Moving UP to Floor " + currentFloor);
            if (upStops.contains(currentFloor)) {
                upStops.remove(currentFloor);
                stop();
            }
        } else if (direction == Direction.DOWN && !downStops.isEmpty()) {
            currentFloor--;
            System.out.println("  [" + elevatorId + "] Moving DOWN to Floor " + currentFloor);
            if (downStops.contains(currentFloor)) {
                downStops.remove(currentFloor);
                stop();
            }
        }
        updateDirection();
    }

    /**
     * Processes all stops in current and reverse direction until no stops remain.
     */
    public void processAllStops() {
        while (!upStops.isEmpty() || !downStops.isEmpty()) {
            moveOneFloor();
        }
        direction = Direction.IDLE;
    }

    private void stop() {
        System.out.println("  [" + elevatorId + "] Stopped at Floor " + currentFloor);
        openDoor();
        internalDisplay.show();
        closeDoor();
    }

    private void updateDirection() {
        if (direction == Direction.UP && upStops.isEmpty()) {
            direction = downStops.isEmpty() ? Direction.IDLE : Direction.DOWN;
        } else if (direction == Direction.DOWN && downStops.isEmpty()) {
            direction = upStops.isEmpty() ? Direction.IDLE : Direction.UP;
        }
    }

    public void openDoor() {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            if (!upStops.isEmpty() || !downStops.isEmpty()) {
                System.out.println("  [" + elevatorId + "] Cannot open door while moving!");
                return;
            }
        }
        doorState = DoorState.OPEN;
        System.out.println("  [" + elevatorId + "] Door OPENED at Floor " + currentFloor);
    }

    public void closeDoor() {
        doorState = DoorState.CLOSED;
        System.out.println("  [" + elevatorId + "] Door CLOSED");
    }

    public void addPassengers(int count, double weightKg) {
        if (passengerCount + count > MAX_CAPACITY) {
            System.out.println("  [" + elevatorId + "] Capacity exceeded! Max " + MAX_CAPACITY + " passengers.");
            return;
        }
        if (currentWeightKg + weightKg > MAX_WEIGHT_KG) {
            System.out.println("  [" + elevatorId + "] Weight limit exceeded! Max " + MAX_WEIGHT_KG + " kg.");
            return;
        }
        passengerCount += count;
        currentWeightKg += weightKg;
    }

    public void removePassengers(int count, double weightKg) {
        passengerCount = Math.max(0, passengerCount - count);
        currentWeightKg = Math.max(0.0, currentWeightKg - weightKg);
    }

    public boolean isAvailable() {
        return passengerCount < MAX_CAPACITY && currentWeightKg < MAX_WEIGHT_KG;
    }

    public boolean isIdle() {
        return direction == Direction.IDLE && upStops.isEmpty() && downStops.isEmpty();
    }

    public int getDistanceTo(int floor) {
        return Math.abs(currentFloor - floor);
    }

    public boolean isMovingTowards(int floor) {
        if (direction == Direction.UP && floor >= currentFloor) return true;
        if (direction == Direction.DOWN && floor <= currentFloor) return true;
        return false;
    }

    public String getElevatorId() { return elevatorId; }
    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
    public DoorState getDoorState() { return doorState; }
    public int getPassengerCount() { return passengerCount; }
    public double getCurrentWeightKg() { return currentWeightKg; }
    public int getMaxCapacity() { return MAX_CAPACITY; }
    public InternalDisplay getInternalDisplay() { return internalDisplay; }

    public String getStatus() {
        return String.format("  %s | Floor: %2d | %-4s | Door: %-6s | Passengers: %d/%d | Weight: %.0f/%.0f kg",
                elevatorId, currentFloor, direction, doorState,
                passengerCount, MAX_CAPACITY, currentWeightKg, MAX_WEIGHT_KG);
    }
}
