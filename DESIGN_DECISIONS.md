# Design Decisions - Parking Lot System

This document explains the key design decisions made during the implementation, focusing on simplicity for interview preparation.

---

## Table of Contents

1. [Architecture Decisions](#architecture-decisions)
2. [Class Design Decisions](#class-design-decisions)
3. [Pattern Choices](#pattern-choices)
4. [Thread Safety Strategy](#thread-safety-strategy)
5. [Data Structure Choices](#data-structure-choices)
6. [Trade-offs](#trade-offs)

---

## Architecture Decisions

### 1. Why Separate Panels from Core Logic?

**Decision:** EntryPanel and ExitPanel are separate classes in a `panels` package, not part of `models`.

**Rationale:**
- **Boundary Objects**: Panels represent the interface between the system and external actors (drivers)
- **Separation of Concerns**: Core business logic (parking/unparking) lives in `ParkingLot`, while panels are thin wrappers
- **Single Responsibility**: Panels only handle I/O and delegation, not business rules

**Alternative Considered:**
- Merge panels into ParkingLot as methods
- **Rejected because:** Would mix boundary concerns with core logic, violating Single Responsibility Principle

**Impact:**
- ✅ Easier to test core logic independently
- ✅ Can swap panel implementations (e.g., web UI, mobile app, physical kiosks)
- ✅ Clear separation makes code more maintainable

---

### 2. Why Singleton for ParkingLot?

**Decision:** ParkingLot uses Singleton pattern with simple synchronized method.

**Rationale:**
- **Single Instance Constraint**: Only one parking lot exists in the system
- **Global Access**: Multiple panels need to access the same lot instance
- **State Consistency**: Ensures all operations work on the same data

**Implementation:**
```java
public static synchronized ParkingLot getInstance(String name) {
    if (instance == null) {
        instance = new ParkingLot(name);
    }
    return instance;
}
```

**Alternative Considered:**
- Double-checked locking
- **Rejected because:** More complex, harder to remember for interviews

**Trade-off:**
- ✅ Simple synchronized method (easy to remember)
- ✅ Thread-safe (synchronized ensures only one thread creates instance)
- ❌ Slightly less performant than double-checked locking (acceptable for interviews)

---

### 3. Why Strategy Pattern for Pricing?

**Decision:** Pricing logic separated into `PricingStrategy` interface with `HourlyPricingStrategy` implementation.

**Rationale:**
- **Open/Closed Principle**: Can add new pricing models without modifying existing code
- **Business Requirement**: Pricing rules change frequently (hourly, flat-rate, surge pricing)
- **Testability**: Easy to mock pricing for unit tests

**Alternatives Considered:**

1. **If-else chain in ParkingLot**
   ```java
   if (pricingType == "HOURLY") { ... }
   else if (pricingType == "FLAT") { ... }
   ```
   **Rejected:** Violates Open/Closed Principle, hard to extend

2. **Enum-based pricing**
   ```java
   enum PricingType { HOURLY, FLAT, DYNAMIC }
   ```
   **Rejected:** Still requires modification to add new types

**Impact:**
- ✅ Easy to add `FlatRatePricingStrategy`, `DynamicPricingStrategy`
- ✅ Pricing logic isolated and testable
- ✅ No changes to ParkingLot when pricing changes

---

## Class Design Decisions

### 4. Why Direct Constructor for ParkingTicket?

**Decision:** `ParkingTicket` uses public constructor instead of factory method.

**Rationale:**
- **Simplicity**: Direct constructor is simpler and easier to remember
- **Interview Clarity**: Less complexity to explain
- **Sufficient**: Current requirements don't need factory method complexity

**Code:**
```java
// Simple direct constructor
public ParkingTicket(Vehicle vehicle, ParkingSpot spot) {
    this.ticketId = "TKT-" + ticketCounter++;
    this.vehicle = vehicle;
    this.spot = spot;
    this.entryTime = LocalDateTime.now();
}
```

**Alternative:** Factory method `issue()`
- **Rejected:** Adds unnecessary complexity for interview preparation

---

### 5. Why HashMap for Spot Storage?

**Decision:** `ParkingFloor` uses `HashMap<SpotType, List<ParkingSpot>>` instead of `EnumMap`.

**Rationale:**
- **Simplicity**: HashMap is more familiar and easier to explain
- **Interview Clarity**: Less to remember (no EnumMap specifics)
- **Sufficient**: Performance difference is negligible for interview scenarios

**Code:**
```java
this.spotsByType = new HashMap<>();
spotsByType.put(SpotType.SMALL, new ArrayList<>());
spotsByType.put(SpotType.MEDIUM, new ArrayList<>());
spotsByType.put(SpotType.LARGE, new ArrayList<>());
```

**Alternative:** EnumMap
- **Rejected:** More complex, harder to remember for interviews

---

### 6. Why Simple Enum for SpotType?

**Decision:** SpotType enum has no methods (no `canFit()` or `getSizeRank()`).

**Rationale:**
- **Simplicity**: Easier to understand and remember
- **Logic Location**: Fallback logic moved to `ParkingFloor.findAvailableSpot()` where it's clearer
- **Interview Clarity**: Less enum complexity to explain

**Code:**
```java
public enum SpotType {
    SMALL,
    MEDIUM,
    LARGE
}
```

**Alternative:** Enum with `canFit()` method
- **Rejected:** Adds complexity, logic is clearer in ParkingFloor

---

### 7. Why Sequential Ticket IDs?

**Decision:** Ticket IDs use simple counter (TKT-1, TKT-2, ...) instead of UUID.

**Rationale:**
- **Simplicity**: Easy to understand and remember
- **Interview Clarity**: No UUID complexity to explain
- **Sufficient**: Sequential IDs work fine for single-instance system

**Code:**
```java
private static int ticketCounter = 1;
this.ticketId = "TKT-" + ticketCounter++;
```

**Alternative:** UUID
- **Rejected:** More complex, harder to remember for interviews

---

## Pattern Choices

### 8. Why Not Observer Pattern for Availability Updates?

**Decision:** Status is queried on-demand via `getStatusDisplay()`, not pushed via Observer pattern.

**Rationale:**
- **Simplicity**: Current requirements don't need real-time notifications
- **Pull vs Push**: Pull model (query when needed) is simpler than push (notify on change)
- **Interview Clarity**: Less patterns to explain

**When to Add Observer:**
- If multiple dashboards need real-time updates
- If mobile app needs push notifications
- If admin needs alerts when lot is full

---

## Thread Safety Strategy

### 9. Why Method-Level Synchronization?

**Decision:** `synchronized` on `parkVehicle()` and `unparkVehicle()` methods in ParkingLot.

**Rationale:**
- **Simplicity**: Easy to understand and explain
- **Correctness**: Prevents race conditions when multiple threads park simultaneously
- **Interview Clarity**: Simple synchronization is easier to discuss

**Code:**
```java
public synchronized ParkingTicket parkVehicle(Vehicle vehicle) {
    for (ParkingFloor floor : floors) {
        ParkingSpot spot = floor.findAvailableSpot(vehicle.getVehicleType());
        if (spot != null && spot.isAvailable()) {
            spot.assignVehicle(vehicle);
            return new ParkingTicket(vehicle, spot);
        }
    }
    throw new RuntimeException("No available spot for " + vehicle);
}
```

**Alternative: Fine-Grained Locking**
- **Rejected:** More complex, harder to explain in interviews

**Trade-off:**
- ✅ Current: Simpler, easier to reason about
- ❌ Current: Lower concurrency (one thread parks at a time)
- ✅ Fine-grained: Higher concurrency
- ❌ Fine-grained: More complex, potential for deadlocks

---

### 10. Why Synchronized on ParkingSpot Methods?

**Decision:** `assignVehicle()` and `removeVehicle()` are `synchronized` at spot level.

**Rationale:**
- **Atomic Operations**: Assign/remove must be atomic (check available, set vehicle, mark unavailable)
- **Double-Booking Prevention**: Prevents two threads from assigning the same spot
- **Defense in Depth**: Even if ParkingLot locking is removed, spot-level safety remains

**Code:**
```java
public synchronized void assignVehicle(Vehicle vehicle) {
    if (!available) {
        throw new IllegalStateException("Spot already occupied");
    }
    this.parkedVehicle = vehicle;
    this.available = false;
}
```

---

## Data Structure Choices

### 11. Why List Instead of Queue for Spots?

**Decision:** Spots stored in `List<ParkingSpot>` instead of `Queue<ParkingSpot>`.

**Rationale:**
- **Random Access**: Need to iterate and check availability, not FIFO
- **No Ordering Requirement**: Spots don't need to be assigned in order
- **Simplicity**: List is simpler and more familiar

**Alternative: PriorityQueue**
- **Not chosen:** Current requirements don't specify spot selection strategy

---

### 12. Why Counter for Ticket IDs?

**Decision:** Ticket IDs generated from simple counter (TKT-1, TKT-2, ...).

**Rationale:**
- **Simplicity**: Easy to understand and remember
- **No Collisions**: Counter ensures uniqueness in single-instance system
- **Interview Clarity**: No UUID complexity

**Alternative: Sequential IDs with synchronization**
- **Not needed:** Counter increment is simple enough

**Trade-off:**
- ✅ Counter: Simple, easy to remember
- ❌ Counter: Not suitable for distributed systems (but fine for single instance)
- ✅ UUID: Truly unique, distributed-safe
- ❌ UUID: More complex, harder to remember

---

## Trade-offs

### Summary of Key Trade-offs

| Decision | Chosen | Alternative | Trade-off |
|----------|--------|------------|-----------|
| **Singleton** | Simple synchronized | Double-checked locking | Simpler vs Slightly less performant |
| **Method-level sync** | Yes | Fine-grained locks | Simpler vs Higher concurrency |
| **Strategy pattern** | Yes | If-else chain | Extensible vs More classes |
| **HashMap** | Yes | EnumMap | Simpler vs More type-safe |
| **Direct constructor** | Yes | Factory method | Simpler vs More controlled |
| **Counter tickets** | Yes | UUID | Simpler vs Distributed-safe |

---

## Design Principles Applied

1. **KISS (Keep It Simple, Stupid)**: Chose simpler implementations over complex ones
2. **YAGNI (You Aren't Gonna Need It)**: Didn't add Observer, Command patterns until needed
3. **DRY (Don't Repeat Yourself)**: Fallback logic in one place (ParkingFloor)
4. **SOLID**: Each principle applied where appropriate
5. **Separation of Concerns**: Clear boundaries between models, panels, pricing

---

## Conclusion

These design decisions prioritize:
- **Correctness**: Thread-safe, no race conditions
- **Simplicity**: Easy to understand and remember for interviews
- **Maintainability**: Clear structure, easy to extend
- **Interview Readiness**: Code that's easy to explain and justify

The design balances these concerns while remaining interview-friendly and production-quality.
