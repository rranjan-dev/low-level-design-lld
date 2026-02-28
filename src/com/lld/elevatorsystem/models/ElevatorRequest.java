package com.lld.elevatorsystem.models;

import com.lld.elevatorsystem.enums.Direction;

public class ElevatorRequest {
    private static int counter = 1;

    private final String requestId;
    private final Person person;
    private final int sourceFloor;
    private final int destinationFloor;
    private final Direction direction;
    private Elevator assignedElevator;

    public ElevatorRequest(Person person, int sourceFloor, int destinationFloor) {
        if (sourceFloor == destinationFloor) {
            throw new IllegalArgumentException("Source and destination floors cannot be the same");
        }
        this.requestId = "REQ-" + counter++;
        this.person = person;
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.direction = sourceFloor < destinationFloor ? Direction.UP : Direction.DOWN;
    }

    public void assignElevator(Elevator elevator) {
        this.assignedElevator = elevator;
    }

    public String getRequestId() {
        return requestId;
    }

    public Person getPerson() {
        return person;
    }

    public int getSourceFloor() {
        return sourceFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public Elevator getAssignedElevator() {
        return assignedElevator;
    }

    /**
     * Example output: "Request[REQ-1] Alice [P1]: Floor 0 -> Floor 5 (UP) via E1"
     */
    @Override
    public String toString() {
        String result = "Request[" + requestId + "] " + person +
                       ": Floor " + sourceFloor + " -> Floor " + destinationFloor + " (" + direction + ")";
        if (assignedElevator != null) {
            result += " via " + assignedElevator.getElevatorId();
        }
        return result;
    }
}
