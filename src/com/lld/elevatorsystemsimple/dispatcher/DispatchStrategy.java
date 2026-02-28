package com.lld.elevatorsystemsimple.dispatcher;

import com.lld.elevatorsystemsimple.enums.Direction;
import com.lld.elevatorsystemsimple.models.Elevator;

import java.util.List;

/**
 * Strategy interface for smart elevator dispatch.
 * Allows plugging in different algorithms (nearest, least loaded, etc.).
 */
public interface DispatchStrategy {
    Elevator selectElevator(List<Elevator> elevators, int requestedFloor, Direction direction);
}
