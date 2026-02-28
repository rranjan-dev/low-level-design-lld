package com.lld.elevatorsystemsimple.panels;

import com.lld.elevatorsystemsimple.enums.Direction;
import com.lld.elevatorsystemsimple.models.Elevator;
import com.lld.elevatorsystemsimple.models.ElevatorSystem;

/**
 * External panel on each floor with UP and DOWN buttons to call an elevator.
 */
public class OutsidePanel {
    private final int floorNumber;

    public OutsidePanel(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Elevator pressUp() {
        System.out.println("[Floor " + floorNumber + " Panel] UP button pressed");
        return ElevatorSystem.getInstance().requestElevator(floorNumber, Direction.UP);
    }

    public Elevator pressDown() {
        System.out.println("[Floor " + floorNumber + " Panel] DOWN button pressed");
        return ElevatorSystem.getInstance().requestElevator(floorNumber, Direction.DOWN);
    }

    public int getFloorNumber() { return floorNumber; }
}
