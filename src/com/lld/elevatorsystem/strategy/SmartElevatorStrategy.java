package com.lld.elevatorsystem.strategy;

import com.lld.elevatorsystem.enums.Direction;
import com.lld.elevatorsystem.enums.ElevatorState;
import com.lld.elevatorsystem.models.Elevator;
import java.util.List;

/**
 * Cost-based elevator selection strategy.
 *
 * For each elevator, calculates a "cost" to serve the request.
 * Picks the elevator with the lowest cost.
 *
 * Cost cases:
 *   IDLE                                  → distance to pickup floor
 *   Same direction, floor is ahead        → distance (best: picks up on the way)
 *   Same direction, floor is behind       → high penalty (must finish run, reverse, come back)
 *   Opposite direction                    → medium penalty (must finish current direction first)
 *   MAINTENANCE or full capacity          → skip entirely
 */
public class SmartElevatorStrategy implements ElevatorSelectionStrategy {

    @Override
    public Elevator selectElevator(List<Elevator> elevators, int pickupFloor, Direction direction) {
        Elevator best = null;
        int bestCost = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int cost = calculateCost(elevator, pickupFloor, direction);
            if (cost < bestCost) {
                bestCost = cost;
                best = elevator;
            }
        }

        return best;
    }

    private int calculateCost(Elevator elevator, int pickupFloor, Direction requestDir) {
        if (elevator.getState() == ElevatorState.MAINTENANCE) return Integer.MAX_VALUE;
        if (!elevator.isAvailable()) return Integer.MAX_VALUE;

        int distance = elevator.getDistanceTo(pickupFloor);

        // IDLE — just the distance, no penalty
        if (elevator.getState() == ElevatorState.IDLE) {
            return distance;
        }

        Direction elevatorDir = elevator.getDirection();
        int currentFloor = elevator.getCurrentFloor();

        if (elevatorDir == requestDir) {
            // Same direction — check if floor is ahead or behind
            boolean floorIsAhead =
                (requestDir == Direction.UP && currentFloor <= pickupFloor) ||
                (requestDir == Direction.DOWN && currentFloor >= pickupFloor);

            if (floorIsAhead) {
                // Best case: elevator will pass this floor on the way
                return distance;
            }
            // Floor is behind: elevator must finish its run, reverse, then reach pickup
            return 3 * distance;
        }

        // Opposite direction: elevator must finish current run, then come to pickup
        return 2 * distance;
    }
}
