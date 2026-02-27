# Design Decisions - Parking Lot System

This document explains the key design decisions made during the implementation of the Parking Lot System, including trade-offs, alternatives considered, and rationale.

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

**Decision:** ParkingLot uses Singleton pattern with double-checked locking.

**Rationale:**
- **Single Instance Constraint**: Only one parking lot exists in the system
- **Global Access**: Multiple panels need to access the same lot instance
- **State Consistency**: Ensures all operations work on the same data

**Alternative Considered:**
- Regular class with dependency injection
- **Rejected because:** Adds complexity for a single-instance use case. Could be extended later if multi-lot support is needed.

**Trade-off:**
- ✅ Simpler access pattern (`ParkingLot.getInstance()`)
- ❌ Harder to test (need `resetInstance()` for test isolation)
- ❌ Less flexible if multi-lot support is needed later

**Future Extension:**
```java
// Could evolve to support multiple lots
Map<String, ParkingLot> lots = new HashMap<>();
ParkingLot getLot(String lotId) { ... }
```

---

### 3. Why Strategy Pattern for Pricing?

**Decision:** Pricing logic separated into `PricingStrategy` interface with `HourlyPricingStrategy` implementation.

**Rationale:**
- **Open/Closed Principle**: Can add new pricing models without modifying existing code
- **Business Requirement**: Pricing rules change frequently (hourly, flat-rate, surge pricing, membership discounts)
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
- ✅ Easy to add `FlatRatePricingStrategy`, `DynamicPricingStrategy`, `MembershipPricingStrategy`
- ✅ Pricing logic isolated and testable
- ✅ No changes to ParkingLot when pricing changes

---

## Class Design Decisions

### 4. Why Factory Method for ParkingTicket?

**Decision:** `ParkingTicket` has private constructor with static `issue()` factory method.

**Rationale:**
- **Semantic Clarity**: `issue()` is more meaningful than `new ParkingTicket()`
- **Future Validation**: Single point to add validation (e.g., check spot availability)
- **Flexibility**: Could return subclasses or cached instances in future

**Code:**
```java
// Current
public static ParkingTicket issue(Vehicle vehicle, ParkingSpot spot) {
    return new ParkingTicket(vehicle, spot);
}

// Future: Could add validation
public static ParkingTicket issue(Vehicle vehicle, ParkingSpot spot) {
    if (!spot.isAvailable()) {
        throw new IllegalStateException("Spot not available");
    }
    return new ParkingTicket(vehicle, spot);
}
```

**Alternative:** Public constructor
- **Rejected:** Less control, harder to add validation later

---

### 5. Why EnumMap for Spot Storage?

**Decision:** `ParkingFloor` uses `EnumMap<SpotType, List<ParkingSpot>>` instead of `Map<String, List<ParkingSpot>>`.

**Rationale:**
- **Type Safety**: Compile-time guarantee that keys are valid SpotType values
- **Performance**: EnumMap is optimized for enum keys (faster than HashMap)
- **Memory**: More memory-efficient than HashMap for enum keys

**Code:**
```java
private final Map<SpotType, List<ParkingSpot>> spotsByType;

// Initialization
spotsByType = new EnumMap<>(SpotType.class);
for (SpotType type : SpotType.values()) {
    spotsByType.put(type, new ArrayList<>());
}
```

**Alternative:** HashMap with String keys
- **Rejected:** Less type-safe, potential for typos, slower lookups

---

### 6. Why Size Ranking in SpotType?

**Decision:** SpotType enum has `sizeRank` (1, 2, 3) and `canFit()` method.

**Rationale:**
- **Fallback Logic**: Enables "larger spot can fit smaller vehicle" rule
- **No If-else**: Avoids scattered if-else chains throughout codebase
- **Extensibility**: Easy to add new spot types (e.g., XL with rank 4)

**Code:**
```java
public enum SpotType {
    SMALL(1), MEDIUM(2), LARGE(3);
    
    public boolean canFit(SpotType requiredType) {
        return this.sizeRank >= requiredType.sizeRank;
    }
}
```

**Alternative:** Hardcoded if-else in `findAvailableSpot()`
```java
if (vehicleType == MOTORCYCLE && spotType == SMALL) return true;
if (vehicleType == CAR && (spotType == MEDIUM || spotType == LARGE)) return true;
```
**Rejected:** Scattered logic, hard to maintain, violates DRY principle

---

## Pattern Choices

### 7. Why Not Observer Pattern for Availability Updates?

**Decision:** Status is queried on-demand via `getStatusDisplay()`, not pushed via Observer pattern.

**Rationale:**
- **Simplicity**: Current requirements don't need real-time notifications
- **Pull vs Push**: Pull model (query when needed) is simpler than push (notify on change)
- **Future Extension**: Can add Observer pattern later if needed

**When to Add Observer:**
- If multiple dashboards need real-time updates
- If mobile app needs push notifications
- If admin needs alerts when lot is full

**Future Enhancement:**
```java
interface AvailabilityObserver {
    void onSpotOccupied(ParkingSpot spot);
    void onSpotFreed(ParkingSpot spot);
}

class ParkingLot {
    private List<AvailabilityObserver> observers;
    public void addObserver(AvailabilityObserver observer) { ... }
}
```

---

### 8. Why Not Command Pattern for Operations?

**Decision:** Direct method calls (`parkVehicle()`, `unparkVehicle()`) instead of Command objects.

**Rationale:**
- **Simplicity**: No need for undo/redo, queuing, or logging yet
- **Current Requirements**: Operations are synchronous and immediate
- **Future Extension**: Can add Command pattern if needed for audit logs or undo

**When to Add Command Pattern:**
- If operations need to be logged/audited
- If undo functionality is required
- If operations need to be queued/batched

---

## Thread Safety Strategy

### 9. Why Method-Level Synchronization?

**Decision:** `synchronized` on `parkVehicle()` and `unparkVehicle()` methods in ParkingLot.

**Rationale:**
- **Simplicity**: Easy to understand and maintain
- **Correctness**: Prevents race conditions when multiple threads park simultaneously
- **Performance**: Acceptable for most use cases (parking operations are not high-frequency)

**Code:**
```java
public synchronized ParkingTicket parkVehicle(Vehicle vehicle) {
    for (ParkingFloor floor : floors) {
        ParkingSpot spot = floor.findAvailableSpot(vehicle.getVehicleType());
        if (spot != null) {
            spot.assignVehicle(vehicle);
            return ParkingTicket.issue(vehicle, spot);
        }
    }
    throw new RuntimeException("No available spot");
}
```

**Alternative: Fine-Grained Locking**
```java
// Lock per floor or per spot
private final Map<Integer, ReentrantLock> floorLocks;

public ParkingTicket parkVehicle(Vehicle vehicle) {
    for (ParkingFloor floor : floors) {
        floorLocks.get(floor.getFloorNumber()).lock();
        try {
            // search spots
        } finally {
            floorLocks.get(floor.getFloorNumber()).unlock();
        }
    }
}
```

**Trade-off:**
- ✅ Current: Simpler, easier to reason about
- ❌ Current: Lower concurrency (one thread parks at a time)
- ✅ Fine-grained: Higher concurrency (multiple threads can park on different floors)
- ❌ Fine-grained: More complex, potential for deadlocks

**When to Use Fine-Grained:**
- High-frequency operations (hundreds of vehicles per minute)
- Large parking lots (100+ floors)
- Performance-critical scenarios

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

**Why Not Just ParkingLot Lock?**
- Spot-level locks allow concurrent operations on different spots
- Even with ParkingLot lock, spot-level lock prevents bugs if lock is removed accidentally

---

## Data Structure Choices

### 11. Why List Instead of Queue for Spots?

**Decision:** Spots stored in `List<ParkingSpot>` instead of `Queue<ParkingSpot>`.

**Rationale:**
- **Random Access**: Need to iterate and check availability, not FIFO
- **No Ordering Requirement**: Spots don't need to be assigned in order
- **Flexibility**: Can implement different allocation strategies (nearest to entrance, etc.)

**Alternative: PriorityQueue**
- Could use if we want "nearest spot first" allocation
- **Not chosen:** Current requirements don't specify spot selection strategy

**Future Enhancement:**
```java
// Could add spot selection strategy
interface SpotSelectionStrategy {
    ParkingSpot selectSpot(List<ParkingSpot> availableSpots);
}

class NearestEntranceStrategy implements SpotSelectionStrategy {
    // Select spot closest to entrance
}
```

---

### 12. Why UUID for Ticket IDs?

**Decision:** Ticket IDs generated from UUID (first 8 characters).

**Rationale:**
- **Uniqueness**: Guaranteed unique across time and space
- **No Collisions**: No need to check if ID already exists
- **Security**: Harder to guess than sequential IDs

**Alternative: Sequential IDs**
```java
private static int nextTicketId = 1;
String ticketId = "TKT-" + nextTicketId++;
```
**Rejected:** Requires synchronization, potential for collisions in distributed systems

**Trade-off:**
- ✅ UUID: Truly unique, no synchronization needed
- ❌ UUID: Longer, less human-readable
- ✅ Sequential: Shorter, easier to remember
- ❌ Sequential: Requires locking, not suitable for distributed systems

---

## Trade-offs

### Summary of Key Trade-offs

| Decision | Chosen | Alternative | Trade-off |
|----------|--------|------------|-----------|
| **Singleton** | Yes | Dependency Injection | Simpler vs More flexible |
| **Method-level sync** | Yes | Fine-grained locks | Simpler vs Higher concurrency |
| **Strategy pattern** | Yes | If-else chain | Extensible vs More classes |
| **EnumMap** | Yes | HashMap | Type-safe vs More generic |
| **Factory method** | Yes | Public constructor | Controlled vs Direct access |
| **UUID tickets** | Yes | Sequential IDs | Unique vs Readable |

---

## Design Principles Applied

1. **YAGNI (You Aren't Gonna Need It)**: Didn't add Observer, Command patterns until needed
2. **KISS (Keep It Simple, Stupid)**: Chose simpler synchronization over complex fine-grained locks
3. **DRY (Don't Repeat Yourself)**: Size ranking in enum avoids scattered if-else
4. **SOLID**: Each principle applied where appropriate
5. **Separation of Concerns**: Clear boundaries between models, panels, pricing

---

## Conclusion

These design decisions prioritize:
- **Correctness**: Thread-safe, no race conditions
- **Maintainability**: Clear structure, easy to understand
- **Extensibility**: Can add features without major refactoring
- **Simplicity**: Not over-engineered for current requirements

The design balances these concerns while remaining interview-ready and production-quality.
