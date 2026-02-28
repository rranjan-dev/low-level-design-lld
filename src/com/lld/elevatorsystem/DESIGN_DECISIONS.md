# Design Decisions - Elevator System

This document explains the key design decisions made during the implementation, focusing on simplicity for interview preparation.

---

## Table of Contents

1. [Architecture Decisions](#architecture-decisions)
2. [Class Design Decisions](#class-design-decisions)
3. [Pattern Choices](#pattern-choices)
4. [Thread Safety Strategy](#thread-safety-strategy)
5. [Trade-offs](#trade-offs)

---

## Architecture Decisions

### 1. Why Singleton for ElevatorSystem?

**Decision:** ElevatorSystem uses the Singleton pattern.

**Rationale:**
- **Single Instance Constraint**: One building has one elevator controller
- **Global Access**: FloorPanels on every floor need access to the same system
- **State Consistency**: Ensures all requests go through one coordinator

**Implementation:**
```java
public static synchronized ElevatorSystem getInstance(String buildingName, int totalFloors) {
    if (instance == null) {
        instance = new ElevatorSystem(buildingName, totalFloors);
    }
    return instance;
}
```

**Trade-off:**
- ✅ Simple synchronized method (easy to remember)
- ✅ Thread-safe
- ❌ Harder to unit test (global state). In production, use dependency injection.

---

### 2. Why Strategy Pattern for Elevator Selection?

**Decision:** Elevator selection logic is behind an `ElevatorSelectionStrategy` interface.

**Rationale:**
- **Open/Closed Principle**: Can add new algorithms without modifying ElevatorSystem
- **Real-world Need**: Different buildings need different strategies (nearest, least loaded, zone-based)
- **Testability**: Easy to mock for unit tests

**Alternatives Considered:**

1. **If-else chain in ElevatorSystem**
   ```java
   if (strategy == "NEAREST") { ... }
   else if (strategy == "LEAST_LOADED") { ... }
   ```
   **Rejected:** Violates Open/Closed Principle

2. **Hardcoded nearest logic**
   **Rejected:** Can't swap algorithms at runtime

**Impact:**
- ✅ Easy to add `LeastLoadedStrategy`, `ZoneBasedStrategy`
- ✅ Selection logic isolated and testable
- ✅ No changes to ElevatorSystem when algorithm changes

---

### 3. Why Synchronous Request Processing?

**Decision:** `requestElevator()` processes the entire ride synchronously — the elevator moves to source, picks up, moves to destination, and drops off in one call.

**Rationale:**
- **Interview Simplicity**: No simulation loops, no Thread.sleep, no complex async state
- **Easy to Demonstrate**: Each call produces clear, readable output
- **Focus on Design**: Shows class relationships and patterns, not async scheduling

**Alternative Considered:**
- Step-by-step simulation with `processMovement()` loop
- **Rejected:** Adds complexity (TreeSet stops, state machines, timers) that obscures the core design

**Trade-off:**
- ✅ Simple, linear flow — easy to walk through on whiteboard
- ✅ Demo output is immediately readable
- ❌ Doesn't model real-time concurrent movement (mention as enhancement)

---

## Class Design Decisions

### 4. Why No Floor Class?

**Decision:** There is no `Floor` class. Floors are just integers (0 to totalFloors).

**Rationale:**
- In an elevator system, floors don't hold state the way parking floors hold spots
- Elevators move between floors — the floor itself is just a number
- Adding a Floor class would add a file with no meaningful logic

**When to Add Floor:**
- If floors have sensors, displays, or door controls
- If floors have access restrictions (e.g., keycard-only floors)

---

### 5. Why Elevator Handles Its Own Movement?

**Decision:** `Elevator.processRequest()` handles the full lifecycle: move to source, pick up, move to destination, drop off.

**Rationale:**
- **Encapsulation**: Movement is an elevator's responsibility, not the system's
- **Simple Interface**: ElevatorSystem just calls `processRequest()`, doesn't micromanage movement
- **Single Method**: Easy to understand — one method, one responsibility

**Code:**
```java
public synchronized void processRequest(ElevatorRequest request) {
    moveTo(request.getSourceFloor());       // Go to pickup
    passengerCount++;                        // Pick up
    moveTo(request.getDestinationFloor());   // Go to destination
    passengerCount--;                        // Drop off
}
```

---

### 6. Why Direction Is Auto-Computed?

**Decision:** `ElevatorRequest` automatically computes direction from source and destination floors.

**Rationale:**
- **No Room for Error**: If source < destination, it's UP. Period.
- **Fewer Parameters**: Caller doesn't need to specify direction
- **Validation Built-in**: Constructor rejects same source and destination

**Code:**
```java
this.direction = sourceFloor < destinationFloor ? Direction.UP : Direction.DOWN;
```

---

### 7. Why Sequential Request IDs?

**Decision:** Request IDs use simple counter (REQ-1, REQ-2, ...) instead of UUID.

**Rationale:**
- **Simplicity**: Easy to understand and remember
- **Interview Clarity**: No UUID complexity to explain
- **Sufficient**: Sequential IDs work fine for single-instance system

**Alternative:** UUID — rejected for interview simplicity.

---

## Pattern Choices

### 8. Why Not Observer Pattern?

**Decision:** No observer/event system for elevator arrivals.

**Rationale:**
- Current requirements don't need real-time notifications
- Synchronous model makes events unnecessary
- Adds complexity without interview value

**When to Add Observer:**
- If floor displays need real-time elevator position updates
- If mobile app needs push notifications ("your elevator is arriving")

---

### 9. Why Not State Pattern for Elevator?

**Decision:** Elevator state is a simple enum field, not a full State pattern.

**Rationale:**
- Only 3 states (IDLE, MOVING, MAINTENANCE), each with simple behavior
- Full State pattern (separate class per state) would be overkill
- Enum + if-checks is clearer for interviews

**When to Add State Pattern:**
- If state transitions become complex (door opening, emergency stop, overweight)
- If each state has significantly different behavior

---

## Thread Safety Strategy

### 10. Why Synchronized at Two Levels?

**Decision:** `synchronized` on both `ElevatorSystem.requestElevator()` and `Elevator.processRequest()`.

**Rationale:**
- **System level**: Prevents two requests from selecting the same elevator simultaneously
- **Elevator level**: Prevents concurrent modifications to elevator state

**Trade-off:**
- ✅ Simple, correct, easy to explain
- ❌ Lower concurrency (one request processed at a time)
- In production: use finer-grained locks or message queues

---

## Trade-offs

### Summary of Key Trade-offs

| Decision | Chosen | Alternative | Trade-off |
|----------|--------|------------|-----------|
| **Singleton** | Simple synchronized | Double-checked locking | Simpler vs Slightly faster |
| **Synchronous processing** | Yes | Async simulation | Simpler vs More realistic |
| **No Floor class** | Yes | Floor with state | Fewer files vs More extensible |
| **Strategy pattern** | Yes | Hardcoded selection | Extensible vs Fewer classes |
| **Simple enum states** | Yes | State pattern | Simpler vs More structured transitions |
| **Counter IDs** | Yes | UUID | Simpler vs Distributed-safe |

---

## Design Principles Applied

1. **KISS**: Synchronous processing, no simulation loops
2. **YAGNI**: No Floor class, no Observer, no State pattern
3. **SRP**: Elevator handles movement, System handles dispatch, Strategy handles selection
4. **OCP**: New selection strategies without modifying existing code
5. **Encapsulation**: `processRequest()` hides internal movement from the system
