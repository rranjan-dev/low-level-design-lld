# Quick Start Guide - Elevator System

---

## Problem Statement

> Design an Elevator System for a multi-story building with multiple elevators. The system should efficiently assign elevators to passenger requests, manage elevator movement, and handle capacity and maintenance constraints.

### Functional Requirements

1. Building has **multiple floors** (configurable) and **multiple elevators**
2. A passenger can **request an elevator** from any floor to any other floor
3. System **selects the best elevator** based on a pluggable strategy (e.g., nearest, cost-based)
4. **Multiple passengers at the same floor** are grouped into the same elevator (until capacity)
5. Each elevator has a **maximum passenger capacity**
6. Elevators can be put into **maintenance mode** (taken out of service)
7. System provides **real-time status** of all elevators (floor, state, passengers)

### Non-Functional Requirements

1. **Thread-safe** — concurrent requests don't corrupt elevator state
2. **Extensible** — easy to plug in new elevator selection algorithms
3. **Simple** — easy to understand, remember, and code in an interview

---

## Quick Compile & Run

```bash
# Compile
javac -d out src/com/lld/elevatorsystem/**/*.java src/com/lld/elevatorsystem/*.java

# Run
java -cp out com.lld.elevatorsystem.ElevatorSystemDemo
```

---

## Key Classes at a Glance

| Class | Purpose | Key Method |
|-------|---------|------------|
| `ElevatorSystem` | Singleton controller | `requestElevator()`, `dispatchElevators()` |
| `Elevator` | Single elevator car | `addRequest()`, `processPendingRequests()` |
| `ElevatorRequest` | Ride record (source → dest) | Constructor (auto-computes direction) |
| `Person` | Passenger | — |
| `FloorPanel` | Numeric keypad on each floor (destination dispatch) | `requestElevator()` |
| `ElevatorSelectionStrategy` | Selection algorithm interface | `selectElevator()` |
| `NearestElevatorStrategy` | Simple: nearest available | `selectElevator()` |
| `SmartElevatorStrategy` | Cost-based: considers direction + grouping | `selectElevator()` |

---

## Core Flow (Two-Phase Model)

```
PHASE 1 — ASSIGN (instant, no movement):
  Person enters destination on FloorPanel keypad (e.g. presses 1, 5 for floor 15)
      → FloorPanel.requestElevator(person, 15)
      → ElevatorSystem.requestElevator(person, currentFloor, 15)
      → SmartElevatorStrategy.selectElevator() → picks lowest-cost elevator
      → Elevator.addRequest(request)  ← queued, elevator doesn't move yet
      → Panel displays "Go to E3"
      → Person walks to E3 and waits

PHASE 2 — DISPATCH (elevators move):
  ElevatorSystem.dispatchElevators()
      → Each elevator processes its pending queue:
      → Elevator.processPendingRequests()
          → moveTo(sourceFloor)  → pick up ALL passengers at this floor
          → visit each destination in order → drop off passengers
```

**Why two phases?** In real life, the panel tells you "Go to E3" *instantly*. You walk over and wait. The elevator arrives, picks up everyone, and visits each destination. It doesn't drop off one person, come back, drop off the next, come back again — it batches them.

---

## Key Concepts

### Direction
- **UP** → Going to a higher floor
- **DOWN** → Going to a lower floor
- **IDLE** → Elevator is stationary

### Elevator States
- **IDLE** → Waiting for requests
- **MOVING** → Travelling to a floor
- **MAINTENANCE** → Out of service

### How Does the System Decide Which Elevator to Send?

This is the core interview question. Two strategies are provided:

**NearestElevatorStrategy** (simple)
1. Find elevators going in the same direction that haven't passed the floor
2. Pick the nearest one
3. Fallback: any nearest available elevator

**SmartElevatorStrategy** (cost-based — the one interviewers want to hear)

Calculates a cost for every elevator and picks the lowest:

| Elevator State | Condition | Cost | Why |
|---|---|---|---|
| Has pending pickup at same floor | — | `0` | Group passengers together! |
| IDLE | — | `distance + 1` | Just come to me |
| MOVING same dir | Floor is ahead | `distance + 1` | Best: picks up on the way |
| MOVING same dir | Floor is behind | `3 * distance + 1` | Must finish run, reverse, come back |
| MOVING opposite dir | — | `2 * distance + 1` | Must finish current direction first |
| MAINTENANCE / full | — | skip | Unavailable |

**Key insight:** The `+1` offset ensures that **grouping** (cost 0) always wins over distance-based costs. If E1 already has pending pickups at your floor, you're grouped with them — one elevator, one trip.

### What About Multiple People on the Same Floor?

This is the whole point of the two-phase model:

1. **Phase 1 (instant):** Each person enters their destination → system assigns the best elevator → "Go to E1"
2. Strategy gives **cost 0** to elevators with pending pickups at the same floor → everyone gets grouped into the same elevator (until capacity)
3. When elevator is full, overflow goes to the next best elevator
4. **Phase 2:** Elevator arrives, picks up **everyone at once**, visits each destination in order

**Example — Morning Rush at Ground Floor:**
```
Alice enters "10" → Go to E1     (E1 has pending at floor 0 → cost 0 for all subsequent)
Bob enters "10"   → Go to E1     (grouped!)
Charlie enters "10" → Go to E1   (grouped!)
Diana enters "20" → Go to E1     (grouped — same source floor, capacity allows)
Eve enters "20"   → Go to E1     (E1 now full, capacity 5)
Frank enters "5"  → Go to E2     (overflow — E1 full)

Dispatch:
  E1: picks up 5 at floor 0 → drops 3 at floor 10 → drops 2 at floor 20
  E2: picks up 1 at floor 0 → drops 1 at floor 5
```

---

## Code Snippets

### Basic Usage (Two-Phase)

```java
// 1. Initialize system
ElevatorSystem system = ElevatorSystem.getInstance("Tech Tower", 10);
system.setSelectionStrategy(new SmartElevatorStrategy());

// 2. Add elevators
system.addElevator(new Elevator("E1", 5));
system.addElevator(new Elevator("E2", 5));

// 3. Create floor keypad
FloorPanel ground = new FloorPanel(0);

// 4. Phase 1: Passengers enter destinations (instant — no movement)
ground.requestElevator(new Person("P1", "Alice"), 10);  // Alice → Go to E1
ground.requestElevator(new Person("P2", "Bob"), 10);    // Bob → Go to E1 (grouped!)
ground.requestElevator(new Person("P3", "Charlie"), 5); // Charlie → Go to E1

// 5. Phase 2: Dispatch — elevators actually move
system.dispatchElevators();
// E1: picks up Alice, Bob, Charlie → drops Charlie at 5, drops Alice+Bob at 10
```

### Direct Usage (Without FloorPanel)

```java
system.requestElevator(new Person("P1", "Alice"), 0, 5);
system.requestElevator(new Person("P2", "Bob"), 0, 5);
system.dispatchElevators();
```

---

## Design Patterns Used

1. **Singleton** → `ElevatorSystem` (one controller per building)
2. **Strategy** → `ElevatorSelectionStrategy` (swappable selection algorithm)

---

## Sample Output

```
=== SCENARIO 1: Morning Rush at Ground Floor ===
--- Phase 1: Passengers enter destinations (instant) ---

  [Floor 0 Panel] Alice → Go to E1
  [Floor 0 Panel] Bob → Go to E1
  [Floor 0 Panel] Charlie → Go to E1
  [Floor 0 Panel] Diana → Go to E1
  [Floor 0 Panel] Eve → Go to E1
  [Floor 0 Panel] Frank → Go to E2
  [Floor 0 Panel] Grace → Go to E2
  [Floor 0 Panel] Hank → Go to E2

--- Phase 2: Elevators dispatched ---

[E1] Picked up Alice [P1] at Floor 0
[E1] Picked up Bob [P2] at Floor 0
[E1] Picked up Charlie [P3] at Floor 0
[E1] Picked up Diana [P4] at Floor 0
[E1] Picked up Eve [P5] at Floor 0
[E1] Moving UP: Floor 0 -> Floor 10
[E1] Dropped off Alice [P1] at Floor 10
[E1] Dropped off Bob [P2] at Floor 10
[E1] Dropped off Charlie [P3] at Floor 10
[E1] Moving UP: Floor 10 -> Floor 20
[E1] Dropped off Diana [P4] at Floor 20
[E1] Dropped off Eve [P5] at Floor 20
[E2] Picked up Frank [P6] at Floor 0
[E2] Picked up Grace [P7] at Floor 0
[E2] Picked up Hank [P8] at Floor 0
[E2] Moving UP: Floor 0 -> Floor 5
[E2] Dropped off Frank [P6] at Floor 5
[E2] Moving UP: Floor 5 -> Floor 15
[E2] Dropped off Hank [P8] at Floor 15
[E2] Moving UP: Floor 15 -> Floor 25
[E2] Dropped off Grace [P7] at Floor 25
```

---

## Common Operations

### Request an Elevator (Phase 1)
```java
ElevatorRequest req = system.requestElevator(person, sourceFloor, destFloor);
// Returns instantly — elevator doesn't move yet
```

### Dispatch All Elevators (Phase 2)
```java
system.dispatchElevators();
// Each elevator processes its queued requests in batch
```

### Put Elevator in Maintenance
```java
elevator.setMaintenance(true);   // Out of service
elevator.setMaintenance(false);  // Back in service
```

### Check Elevator Status
```java
System.out.println(elevator.getStatusDisplay());
// Output: "Elevator E1: Floor 5, IDLE, Passengers: 0/5, Pending: 3"
```

---

## Interview-Ready Features

### 1. Two-Phase Model (Assign → Dispatch)
The core design insight. Assignment is instant (panel shows "Go to E1" immediately), movement happens when dispatched. This naturally enables batch pickups.

### 2. Passenger Grouping via Strategy
`SmartElevatorStrategy` gives cost 0 to elevators with pending pickups at the same floor — so 3 people entering "10" at the ground floor all get assigned to E1 without any special grouping code. The strategy handles it naturally.

### 3. Strategy Pattern for Elevator Selection
Selection algorithm is pluggable:
```java
system.setSelectionStrategy(new SmartElevatorStrategy());
// Later: system.setSelectionStrategy(new LeastLoadedStrategy());
```

### 4. SmartElevatorStrategy — Cost-Based Dispatching with Grouping
```java
Has pending pickup at same floor  → cost = 0               (group them!)
IDLE                              → cost = distance + 1
Same direction, floor ahead       → cost = distance + 1     (on the way!)
Same direction, floor behind      → cost = 3 * distance + 1 (finish + reverse)
Opposite direction                → cost = 2 * distance + 1 (finish current first)
```

### 5. Capacity-Based Overflow
When an elevator is full (passengers + pending >= maxCapacity), `isAvailable()` returns false and the strategy skips it. Overflow naturally goes to the next best elevator.

### 6. Auto-Computed Direction
No room for caller error — direction is derived from floors:
```java
this.direction = sourceFloor < destinationFloor ? Direction.UP : Direction.DOWN;
```

### 7. Maintenance Mode
One-liner to take an elevator out of service. Strategy automatically skips it.

### 8. Thread Safety at Two Levels
- **System level**: `synchronized` on `requestElevator()` — prevents conflicting assignments
- **Elevator level**: `synchronized` on `processPendingRequests()` — prevents state corruption

---

## Possible Extensions (Mention If Asked)

- **LeastLoadedStrategy** → pick elevator with fewest current passengers
- **ZoneBasedStrategy** → assign elevator ranges to floor zones (1-5, 6-10)
- **Priority floors** → ground floor gets higher weight in selection
- **Express elevators** → skip intermediate floors, serve only certain ranges
- **LOOK/SCAN algorithm** → optimize stop ordering for mixed up/down destinations
- **Door management** → OPENING, OPEN, CLOSING states for the elevator
- **Weight-based capacity** → replace passenger count with weight sensor
- **Emergency stop** → immediate halt, override all requests
- **Real-time async dispatch** → event-driven model with continuous elevator movement
