package com.lld.elevatorsystemsimple.display;

import com.lld.elevatorsystemsimple.models.Elevator;

/**
 * Display inside each elevator showing current floor, direction, and capacity.
 */
public class InternalDisplay {
    private final Elevator elevator;

    public InternalDisplay(Elevator elevator) {
        this.elevator = elevator;
    }

    public void show() {
        System.out.println("  [" + elevator.getElevatorId() + " Display] Floor: "
                + elevator.getCurrentFloor()
                + " | Direction: " + elevator.getDirection()
                + " | Passengers: " + elevator.getPassengerCount()
                + "/" + elevator.getMaxCapacity());
    }
}
