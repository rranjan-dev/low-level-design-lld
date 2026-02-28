package com.lld.elevatorsystem.models;

import com.lld.elevatorsystem.enums.Direction;
import com.lld.elevatorsystem.enums.ElevatorState;

public class Elevator {
    private final String elevatorId;
    private final int maxCapacity;
    private int currentFloor;
    private ElevatorState state;
    private Direction direction;
    private int passengerCount;

    public Elevator(String elevatorId, int maxCapacity) {
        this.elevatorId = elevatorId;
        this.maxCapacity = maxCapacity;
        this.currentFloor = 0;
        this.state = ElevatorState.IDLE;
        this.direction = Direction.IDLE;
        this.passengerCount = 0;
    }

    /**
     * Processes a request end-to-end: moves to source floor, picks up passenger,
     * moves to destination floor, drops off passenger.
     */
    public synchronized void processRequest(ElevatorRequest request) {
        if (state == ElevatorState.MAINTENANCE) {
            throw new IllegalStateException("Elevator " + elevatorId + " is under maintenance");
        }
        if (passengerCount >= maxCapacity) {
            throw new IllegalStateException("Elevator " + elevatorId + " is at full capacity");
        }

        moveTo(request.getSourceFloor());

        passengerCount++;
        System.out.println("[" + elevatorId + "] Picked up " + request.getPerson() +
                          " at Floor " + currentFloor);

        moveTo(request.getDestinationFloor());

        passengerCount--;
        System.out.println("[" + elevatorId + "] Dropped off " + request.getPerson() +
                          " at Floor " + currentFloor);

        if (passengerCount == 0) {
            state = ElevatorState.IDLE;
            direction = Direction.IDLE;
        }
    }

    private void moveTo(int targetFloor) {
        if (targetFloor == currentFloor) return;

        direction = targetFloor > currentFloor ? Direction.UP : Direction.DOWN;
        state = ElevatorState.MOVING;

        System.out.println("[" + elevatorId + "] Moving " + direction +
                          ": Floor " + currentFloor + " -> Floor " + targetFloor);
        currentFloor = targetFloor;
    }

    /**
     * Can this elevator serve a request at the given floor and direction?
     * Used by selection strategy.
     */
    public boolean canServe(int floor, Direction requestDirection) {
        if (state == ElevatorState.MAINTENANCE) return false;
        if (passengerCount >= maxCapacity) return false;
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

    public String getElevatorId() {
        return elevatorId;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public ElevatorState getState() {
        return state;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean isAvailable() {
        return state != ElevatorState.MAINTENANCE && passengerCount < maxCapacity;
    }

    /**
     * Example output: "Elevator E1: Floor 5, IDLE, Passengers: 0/8"
     */
    public String getStatusDisplay() {
        return String.format("Elevator %s: Floor %d, %s, Passengers: %d/%d",
                elevatorId, currentFloor, state, passengerCount, maxCapacity);
    }
}
