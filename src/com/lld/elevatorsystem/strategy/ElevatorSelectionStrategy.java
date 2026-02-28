package com.lld.elevatorsystem.strategy;

import com.lld.elevatorsystem.enums.Direction;
import com.lld.elevatorsystem.models.Elevator;
import java.util.List;

/**
 * Strategy interface for selecting which elevator should handle a request.
 * Implement this to plug in different algorithms (nearest, least loaded, etc.)
 */
public interface ElevatorSelectionStrategy {
    Elevator selectElevator(List<Elevator> elevators, int pickupFloor, Direction direction);
}
