# Design Decisions - Elevator System (Simple)

This document explains the key design decisions made during the implementation, focusing on simplicity for interview preparation.

---

## Table of Contents

1. [Architecture Decisions](#architecture-decisions)
2. [Class Design Decisions](#class-design-decisions)
3. [Pattern Choices](#pattern-choices)
4. [Data Structure Choices](#data-structure-choices)
5. [Trade-offs](#trade-offs)

---

## Architecture Decisions

### 1. Why Separate Panels and Displays from Core Models?

**Decision:** Panels (OutsidePanel, InsidePanel) and Displays (InternalDisplay, ExternalDisplay) are in separate packages, not inside models.

**Rationale:**
- **Boundary Objects**: Panels represent the user interface — buttons passengers interact with
- **Separation of Concerns**: Movement/capacity logic lives in `Elevator`, panel logic is thin delegation
- **Single Responsibility**: Panels handle user input, displays handle output, models handle business logic

**Alternative Considered:**
- Put panel methods directly on Elevator or Floor
- **Rejected because:** Mixes boundary concerns with core logic

**Impact:**
- Easy to swap UI implementations (physical buttons, touchscreen, mobile app)
- Core logic testable without panels

---

### 2. Why Singleton for ElevatorSystem?

**Decision:** ElevatorSystem uses Singleton pattern with simple synchronized method.

**Rationale:**
- **Single Instance**: Only one elevator controller exists per building
- **Global Access**: All panels on all floors need to reach the same controller
- **State Consistency**: All dispatch decisions operate on the same elevator list

**Implementation:**
```java
public static synchronized ElevatorSystem getInstance(String buildingName) {
    if (instance == null) {
        instance = new ElevatorSystem(buildingName);
    }
    return instance;
}
```

**Alternative Considered:**
- Pass ElevatorSystem reference to each panel via constructor
- **Rejected:** More wiring code, harder to demonstrate in interviews

**Trade-off:**
- Simple synchronized (easy to remember) vs slightly less performant than double-checked locking

---

### 3. Why ElevatorSystem Creates Floors Automatically?

**Decision:** `ElevatorSystem` constructor creates all 16 floors (0-15) automatically.

**Rationale:**
- **Simplicity**: Floors are a fixed part of the building — no need to add them manually
- **Fewer Setup Steps**: Demo code is shorter and clearer
- **Real-World Modeling**: A building doesn't add/remove floors dynamically

**Alternative Considered:**
- Manual `addFloor()` like parking lot's `addFloor()`
- **Rejected:** Floors don't vary at runtime; keeps setup simple

---

### 4. Why Two Separate Panel Classes?

**Decision:** `OutsidePanel` (UP/DOWN buttons) and `InsidePanel` (floor buttons + door controls) are separate classes.

**Rationale:**
- **Different Responsibilities**: Outside panel only calls elevators; inside panel selects floors and controls doors
- **Different Locations**: Outside is on the floor wall; inside is in the elevator car
- **Different Lifecycles**: One OutsidePanel per floor; one InsidePanel per elevator

**Alternative Considered:**
- Single `Panel` class with mode flag
- **Rejected:** Two distinct behaviors with different fields — separate classes are cleaner

---

## Class Design Decisions

### 5. Why Two TreeSets for Stops Instead of One List?

**Decision:** Elevator uses `TreeSet<Integer> upStops` and `TreeSet<Integer> downStops` instead of a single list.

**Rationale:**
- **Ordered Processing**: TreeSet maintains natural ordering — UP stops visited low-to-high, DOWN stops visited by iterating the set
- **No Duplicates**: If multiple passengers press the same floor, it's stored once
- **Direction Separation**: Elevator processes all UP stops, then switches to DOWN (SCAN algorithm)
- **O(log n) Operations**: add/remove/contains are all O(log n)

**Alternative Considered:**
- Single `PriorityQueue<Integer>` with direction-based comparator
- **Rejected:** Need to handle direction change; two sets make the logic clearer

**Code:**
```java
public void addDestination(int floor) {
    if (floor > currentFloor) {
        upStops.add(floor);
    } else if (floor < currentFloor) {
        downStops.add(floor);
    }
}
```

---

### 6. Why Elevator Owns InternalDisplay?

**Decision:** `Elevator` creates and owns its `InternalDisplay` in the constructor.

**Rationale:**
- **1:1 Relationship**: Every elevator always has exactly one internal display
- **Lifecycle Coupling**: Display lives and dies with the elevator
- **Auto-Show on Stop**: When elevator stops at a floor, it automatically shows the display

**Alternative Considered:**
- Display created separately and passed in
- **Rejected:** Unnecessary wiring for a fixed 1:1 relationship

---

### 7. Why Floor Owns OutsidePanel and ExternalDisplay?

**Decision:** `Floor` creates `OutsidePanel` and `ExternalDisplay` in its constructor.

**Rationale:**
- **Composition**: Every floor always has these components — they're part of the floor
- **Encapsulation**: Access via `floor.getOutsidePanel()` — clean API
- **Real-World Modeling**: Panels and displays are physically mounted on the floor wall

---

### 8. Why addPassengers/removePassengers Instead of Tracking Individual People?

**Decision:** Elevator tracks `passengerCount` and `currentWeightKg` as aggregates, not individual Person objects.

**Rationale:**
- **Simplicity**: For an elevator system, who is inside matters less than how many and how heavy
- **Fewer Classes**: No need for a Person/Passenger model class
- **Interview Clarity**: Easier to demonstrate capacity logic with simple counts

**Alternative Considered:**
- `List<Person>` inside Elevator with individual weights
- **Rejected:** Over-complicates the demo without adding interview value

---

## Pattern Choices

### 9. Why Strategy Pattern for Dispatch?

**Decision:** Dispatch logic separated into `DispatchStrategy` interface with `NearestDispatchStrategy`.

**Rationale:**
- **Open/Closed Principle**: Can add LeastLoaded, RoundRobin, SmartDispatch without modifying ElevatorSystem
- **Business Requirement**: Dispatch algorithm is the key differentiator in elevator systems
- **Testability**: Easy to mock dispatch for testing

**Alternatives Considered:**

1. **If-else chain in ElevatorSystem**
   ```java
   if (dispatchType == "NEAREST") { ... }
   else if (dispatchType == "LEAST_LOADED") { ... }
   ```
   **Rejected:** Violates Open/Closed Principle

2. **Dispatch logic embedded in Elevator**
   **Rejected:** Elevator shouldn't know about other elevators

**Impact:**
- Easy to add new strategies without touching existing code
- ElevatorSystem delegates selection cleanly

---

### 10. Why Not Observer Pattern for Displays?

**Decision:** Displays query elevator state on-demand, not via Observer push notifications.

**Rationale:**
- **Simplicity**: Current requirements don't need real-time push updates
- **Pull Model**: Display calls `show()` when needed — simpler than event system
- **Interview Clarity**: Less patterns to explain

**When to Add Observer:**
- If displays need to update in real-time as elevator moves
- If a monitoring dashboard needs live feed
- If mobile app needs push notifications

---

## Data Structure Choices

### 11. Why TreeSet Over PriorityQueue?

**Decision:** `TreeSet<Integer>` for stops instead of `PriorityQueue`.

**Rationale:**
- **contains() in O(log n)**: Need to check if current floor is a stop — PriorityQueue has O(n) contains
- **No Duplicates**: TreeSet handles this automatically
- **remove() in O(log n)**: Can remove specific floor when reached
- **Iteration Order**: Natural ascending order for UP processing

**PriorityQueue Drawbacks:**
- O(n) contains and remove-by-value
- Allows duplicates (need manual dedup)
- Only guarantees head is min — not full ordering for iteration

---

### 12. Why ArrayList for Elevators in ElevatorSystem?

**Decision:** `List<Elevator>` backed by `ArrayList`.

**Rationale:**
- **Small Fixed Size**: Max 3 elevators — any collection works
- **Index Access**: Can reference elevator by index if needed
- **Simplicity**: Most familiar collection in Java

---

## Trade-offs

### Summary of Key Trade-offs

| Decision | Chosen | Alternative | Trade-off |
|----------|--------|------------|-----------|
| **Singleton** | Simple synchronized | Double-checked locking | Simpler vs slightly faster |
| **Two TreeSets** | Yes | Single list/queue | Cleaner direction handling vs more fields |
| **Strategy pattern** | Yes | If-else chain | Extensible vs fewer classes |
| **Aggregate passengers** | Count + weight | Individual Person list | Simpler vs more detailed tracking |
| **Auto-create floors** | Yes | Manual addFloor() | Less setup vs more flexible |
| **Pull-based displays** | Yes | Observer pattern | Simpler vs real-time updates |
| **Separate panels** | Two classes | Single class with mode | Cleaner SRP vs fewer files |

---

## Design Principles Applied

1. **KISS**: Chose simpler implementations over complex ones
2. **YAGNI**: No Observer, Command, or Factory patterns until needed
3. **DRY**: Dispatch logic in one place (DispatchStrategy), stop processing in one method
4. **SOLID**: Each principle applied where appropriate
5. **Separation of Concerns**: Clear boundaries between models, panels, displays, dispatch

---

## Conclusion

These design decisions prioritize:
- **Correctness**: Door safety, capacity enforcement, ordered stop processing
- **Simplicity**: Easy to understand and remember for interviews
- **Maintainability**: Clear structure, pluggable dispatch strategy
- **Interview Readiness**: Code that's easy to explain and justify

The design balances these concerns while remaining interview-friendly and demonstrating solid OOP principles.
