package com.lld.elevatorsystemsimple.dispatcher;

import com.lld.elevatorsystemsimple.enums.Direction;
import com.lld.elevatorsystemsimple.models.Elevator;

import java.util.List;

/**
 * Assigns the most suitable elevator based on:
 *   1. Idle elevators nearest to the requested floor (best)
 *   2. Elevators already moving towards the requested floor in the same direction
 *   3. Any available elevator as fallback
 */
public class NearestDispatchStrategy implements DispatchStrategy {

    @Override
    public Elevator selectElevator(List<Elevator> elevators, int requestedFloor, Direction direction) {
        Elevator best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (!elevator.isAvailable()) continue;

            int distance = elevator.getDistanceTo(requestedFloor);

            if (elevator.isIdle()) {
                // Idle elevator — score is just distance (lowest priority number)
                if (distance < bestScore) {
                    bestScore = distance;
                    best = elevator;
                }
            } else if (elevator.isMovingTowards(requestedFloor)
                    && elevator.getDirection() == direction) {
                // Moving in same direction towards requested floor — slight penalty
                int score = distance + 1;
                if (score < bestScore) {
                    bestScore = score;
                    best = elevator;
                }
            }
        }

        // Fallback: any available elevator regardless of direction
        if (best == null) {
            for (Elevator elevator : elevators) {
                if (!elevator.isAvailable()) continue;
                int distance = elevator.getDistanceTo(requestedFloor);
                if (distance < bestScore) {
                    bestScore = distance;
                    best = elevator;
                }
            }
        }

        return best;
    }
}
