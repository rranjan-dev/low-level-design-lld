package com.lld.elevatorsystem.strategy;

import com.lld.elevatorsystem.enums.Direction;
import com.lld.elevatorsystem.models.Elevator;
import java.util.List;

/**
 * Selects the nearest available elevator that can serve the request direction.
 * Falls back to any available elevator if no perfect match exists.
 */
public class NearestElevatorStrategy implements ElevatorSelectionStrategy {

    @Override
    public Elevator selectElevator(List<Elevator> elevators, int pickupFloor, Direction direction) {
        Elevator nearest = null;
        int minDistance = Integer.MAX_VALUE;

        // First pass: nearest elevator already going in the same direction
        for (Elevator elevator : elevators) {
            if (elevator.canServe(pickupFloor, direction)) {
                int distance = elevator.getDistanceTo(pickupFloor);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = elevator;
                }
            }
        }

        // Fallback: any available elevator
        if (nearest == null) {
            for (Elevator elevator : elevators) {
                if (elevator.isAvailable()) {
                    int distance = elevator.getDistanceTo(pickupFloor);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = elevator;
                    }
                }
            }
        }

        return nearest;
    }
}
