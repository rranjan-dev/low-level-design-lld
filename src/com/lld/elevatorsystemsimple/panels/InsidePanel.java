package com.lld.elevatorsystemsimple.panels;

import com.lld.elevatorsystemsimple.models.Elevator;
import com.lld.elevatorsystemsimple.models.ElevatorSystem;

/**
 * Control panel inside each elevator with buttons for all floors
 * and door open/close operations.
 */
public class InsidePanel {
    private final Elevator elevator;

    public InsidePanel(Elevator elevator) {
        this.elevator = elevator;
    }

    public void pressFloor(int floor) {
        System.out.println("[" + elevator.getElevatorId() + " Panel] Floor " + floor + " pressed");
        ElevatorSystem.getInstance().selectFloor(elevator, floor);
    }

    public void pressOpenDoor() {
        System.out.println("[" + elevator.getElevatorId() + " Panel] Open Door pressed");
        elevator.openDoor();
    }

    public void pressCloseDoor() {
        System.out.println("[" + elevator.getElevatorId() + " Panel] Close Door pressed");
        elevator.closeDoor();
    }

    public Elevator getElevator() { return elevator; }
}
