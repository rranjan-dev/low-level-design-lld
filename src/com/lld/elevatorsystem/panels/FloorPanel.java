package com.lld.elevatorsystem.panels;

import com.lld.elevatorsystem.models.ElevatorRequest;
import com.lld.elevatorsystem.models.ElevatorSystem;
import com.lld.elevatorsystem.models.Person;

/**
 * Represents the panel on each floor where passengers press UP/DOWN buttons.
 * Analogous to EntryPanel in the Parking Lot design.
 */
public class FloorPanel {
    private final int floorNumber;

    public FloorPanel(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public ElevatorRequest requestElevator(Person person, int destinationFloor) {
        ElevatorRequest request = ElevatorSystem.getInstance()
            .requestElevator(person, floorNumber, destinationFloor);
        System.out.println("  [Floor " + floorNumber + " Panel] Completed: " + request);
        return request;
    }

    public int getFloorNumber() {
        return floorNumber;
    }
}
