package com.lld.elevatorsystem.strategy;

import com.lld.elevatorsystem.enums.Direction;
import com.lld.elevatorsystem.enums.ElevatorState;
import com.lld.elevatorsystem.models.Elevator;
import java.util.List;

/**
 * Cost-based elevator selection with passenger grouping.
 *
 * Key behavior: if an elevator already has pending pickups at the SAME floor,
 * it gets cost 0 — so multiple people at the same floor are grouped into
 * one elevator (just like real destination dispatch systems).
 *
 * Cost table:
 *   Pending pickup at same floor          → 0   (group them!)
 *   IDLE                                  → distance + 1
 *   Same direction, floor is ahead        → distance + 1
 *   Same direction, floor is behind       → 3 * distance + 1
 *   Opposite direction                    → 2 * distance + 1
 *   MAINTENANCE or full capacity          → skip
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

        // Group with elevator already collecting at same floor — best case
        if (elevator.hasPendingPickupAt(pickupFloor)) {
            return 0;
        }

        int distance = elevator.getDistanceTo(pickupFloor);

        // +1 offset so grouping (cost 0) always wins over distance-based costs
        if (elevator.getState() == ElevatorState.IDLE) {
            return distance + 1;
        }

        Direction elevatorDir = elevator.getDirection();
        int currentFloor = elevator.getCurrentFloor();

        if (elevatorDir == requestDir) {
            boolean floorIsAhead =
                (requestDir == Direction.UP && currentFloor <= pickupFloor) ||
                (requestDir == Direction.DOWN && currentFloor >= pickupFloor);

            if (floorIsAhead) return distance + 1;
            return 3 * distance + 1;
        }

        return 2 * distance + 1;
    }
}
