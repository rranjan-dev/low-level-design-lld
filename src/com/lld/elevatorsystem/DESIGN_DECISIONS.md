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

### 3. Why Two-Phase Model (Assign → Dispatch)?

**Decision:** `requestElevator()` only assigns an elevator and queues the request (instant). A separate `dispatchElevators()` call makes elevators actually move.

**Rationale:**
- **Real-world behavior**: In real life, the panel tells you "Go to E3" instantly. You walk to E3 and wait. E3 then arrives, picks up everyone, and visits each destination. It doesn't escort one person end-to-end before looking at the next person.
- **Batch pickups**: Multiple people at the same floor are grouped into one elevator naturally — the strategy sees pending requests and groups them.
- **Interview clarity**: Two distinct phases make the flow easy to explain on a whiteboard.

**How it works:**
```
Phase 1 — requestElevator():
  → Strategy picks best elevator (gives cost 0 if it already has pending at same floor)
  → Request queued via elevator.addRequest()
  → Returns instantly → panel displays "Go to E1"

Phase 2 — dispatchElevators():
  → Each elevator processes its pending queue
  → Moves to source floor, picks up ALL passengers, visits each destination
```

**Alternative Considered:**
- Synchronous single-request model: `requestElevator()` moves the elevator immediately for each person.
- **Rejected:** This means person 1 rides the elevator alone, then person 2, then person 3 — not how real elevators work.

**Trade-off:**
- ✅ Realistic batch behavior (pick up 5 people at once, drop at different floors)
- ✅ Efficient — one trip serves multiple passengers
- ✅ Demonstrates strategy pattern well (grouping logic is in the strategy)
- ❌ Slightly more complex than single-request synchronous model

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

### 5. Why Elevator Manages Its Own Request Queue?

**Decision:** `Elevator` has a `pendingRequests` list and `processPendingRequests()` handles the full batch lifecycle: move to source, pick up all, visit each destination, drop off.

**Rationale:**
- **Encapsulation**: Movement and passenger management are an elevator's responsibility
- **Batch processing**: Naturally handles multiple passengers going to different floors in one trip
- **Simple Interface**: ElevatorSystem just calls `dispatchElevators()`, doesn't micromanage

**Code:**
```java
public synchronized void processPendingRequests() {
    // Group by source floor
    // For each source: move there, pick up everyone
    // Visit each destination in order, drop off passengers
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

### 7. Why Passenger Grouping Is in the Strategy, Not the System?

**Decision:** `SmartElevatorStrategy` checks `elevator.hasPendingPickupAt(floor)` and gives cost 0 for grouping. The system doesn't have explicit grouping logic.

**Rationale:**
- **Strategy's job**: Deciding which elevator is "best" IS the strategy's responsibility
- **No special code**: Grouping emerges naturally from the cost function — no `groupByFloor()` method needed
- **Flexibility**: A different strategy could choose NOT to group (e.g., spread load evenly)

---

### 8. Why Capacity Considers Pending Requests?

**Decision:** `isAvailable()` checks `passengerCount + pendingRequests.size() < maxCapacity`.

**Rationale:**
- Pending passengers haven't boarded yet, but they WILL — the elevator is already assigned to them
- Without this, an elevator with capacity 5 could get 10 pending requests and be overbooked
- When pending count reaches capacity, `isAvailable()` returns false and strategy overflows to next elevator

---

## Pattern Choices

### 9. Why Not Observer Pattern?

**Decision:** No observer/event system for elevator arrivals.

**Rationale:**
- Current requirements don't need real-time notifications
- Two-phase model makes events unnecessary
- Adds complexity without interview value

**When to Add Observer:**
- If floor displays need real-time elevator position updates
- If mobile app needs push notifications ("your elevator is arriving")

---

### 10. Why Not State Pattern for Elevator?

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

### 11. Why Synchronized at Two Levels?

**Decision:** `synchronized` on both `ElevatorSystem.requestElevator()` and `Elevator.processPendingRequests()`.

**Rationale:**
- **System level**: Prevents two requests from selecting the same elevator simultaneously. Ensures pending counts are accurate for capacity checks.
- **Elevator level**: Prevents concurrent modifications to elevator state during batch processing.

**Trade-off:**
- ✅ Simple, correct, easy to explain
- ❌ Lower concurrency (assignments are serialized)
- In production: use finer-grained locks or message queues

---

## Trade-offs

### Summary of Key Trade-offs

| Decision | Chosen | Alternative | Trade-off |
|----------|--------|------------|-----------|
| **Singleton** | Simple synchronized | Double-checked locking | Simpler vs Slightly faster |
| **Two-phase model** | Assign + Dispatch | Single synchronous call | Realistic vs Simpler |
| **Grouping in strategy** | Cost-based | Explicit group-by-floor | Elegant vs Obvious |
| **No Floor class** | Yes | Floor with state | Fewer files vs More extensible |
| **Strategy pattern** | Yes | Hardcoded selection | Extensible vs Fewer classes |
| **Simple enum states** | Yes | State pattern | Simpler vs More structured |

---

## Design Principles Applied

1. **KISS**: Two-phase model is simple to explain; no simulation loops
2. **YAGNI**: No Floor class, no Observer, no State pattern
3. **SRP**: Elevator handles movement, System handles dispatch, Strategy handles selection
4. **OCP**: New selection strategies without modifying existing code
5. **Encapsulation**: `processPendingRequests()` hides internal batch logic from the system
