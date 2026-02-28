package com.lld.elevatorsystem.panels;

import com.lld.elevatorsystem.models.ElevatorRequest;
import com.lld.elevatorsystem.models.ElevatorSystem;
import com.lld.elevatorsystem.models.Person;

/**
 * Destination dispatch panel on each floor.
 * Has a numeric keypad (0-9) where the passenger enters their destination floor.
 * System then assigns the best elevator and displays it.
 *
 * Flow: passenger enters destination → system assigns elevator → panel displays "Go to E3"
 */
public class FloorPanel {
    private final int floorNumber;

    public FloorPanel(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    /**
     * Passenger enters destination floor on the keypad.
     * System assigns the best elevator and processes the ride.
     */
    public ElevatorRequest requestElevator(Person person, int destinationFloor) {
        ElevatorRequest request = ElevatorSystem.getInstance()
            .requestElevator(person, floorNumber, destinationFloor);
        System.out.println("  [Floor " + floorNumber + " Panel] " + person.getName()
            + " → Go to " + request.getAssignedElevator().getElevatorId());
        return request;
    }

    public int getFloorNumber() {
        return floorNumber;
    }
}
