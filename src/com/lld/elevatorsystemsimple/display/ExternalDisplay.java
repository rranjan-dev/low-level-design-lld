package com.lld.elevatorsystemsimple.display;

import com.lld.elevatorsystemsimple.models.Elevator;

import java.util.List;

/**
 * Display outside on each floor showing each elevator's current floor and direction.
 */
public class ExternalDisplay {
    private final int floorNumber;

    public ExternalDisplay(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public void showAll(List<Elevator> elevators) {
        System.out.println("Floor " + floorNumber + " Display:");
        for (Elevator e : elevators) {
            System.out.println("    " + e.getElevatorId()
                    + " → Floor " + e.getCurrentFloor()
                    + " [" + e.getDirection() + "]");
        }
    }

    public int getFloorNumber() { return floorNumber; }
}
