# Quick Start Guide - Elevator System

---

## Problem Statement

> Design an Elevator System for a multi-story building with multiple elevators. The system should efficiently assign elevators to passenger requests, manage elevator movement, and handle capacity and maintenance constraints.

### Functional Requirements

1. Building has **multiple floors** (configurable) and **multiple elevators**
2. A passenger can **request an elevator** from any floor to any other floor
3. System **selects the best elevator** based on a pluggable strategy (e.g., nearest)
4. Each elevator has a **maximum passenger capacity**
5. Elevators can be put into **maintenance mode** (taken out of service)
6. System provides **real-time status** of all elevators (floor, state, passengers)

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
| `ElevatorSystem` | Singleton controller | `requestElevator()` |
| `Elevator` | Single elevator car | `processRequest()`, `canServe()` |
| `ElevatorRequest` | Ride record (source → dest) | Constructor (auto-computes direction) |
| `Person` | Passenger | — |
| `FloorPanel` | UP/DOWN buttons on a floor | `requestElevator()` |
| `ElevatorSelectionStrategy` | Selection algorithm interface | `selectElevator()` |
| `NearestElevatorStrategy` | Picks nearest available elevator | `selectElevator()` |

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

### Selection Logic (NearestElevatorStrategy)
1. Find elevators going in the same direction that haven't passed the pickup floor
2. Among those, pick the nearest one
3. If none found, pick any nearest available elevator

---

## Code Snippets

### Basic Usage

```java
// 1. Initialize system
ElevatorSystem system = ElevatorSystem.getInstance("Tech Tower", 10);
system.setSelectionStrategy(new NearestElevatorStrategy());

// 2. Add elevators
system.addElevator(new Elevator("E1", 8));
system.addElevator(new Elevator("E2", 8));

// 3. Create floor panel
FloorPanel ground = new FloorPanel(0);

// 4. Request elevator
Person alice = new Person("P1", "Alice");
ElevatorRequest request = ground.requestElevator(alice, 5);

// 5. Check status
System.out.println(system.getStatusDisplay());
```

### Direct Usage (Without FloorPanel)

```java
ElevatorRequest request = system.requestElevator(
    new Person("P1", "Alice"), 0, 5);
```

---

## Design Patterns Used

1. **Singleton** → `ElevatorSystem` (one controller per building)
2. **Strategy** → `ElevatorSelectionStrategy` (swappable selection algorithm)

---

## Sample Output

```
=== Tech Tower Elevator System ===
  Elevator E1: Floor 0, IDLE, Passengers: 0/8
  Elevator E2: Floor 0, IDLE, Passengers: 0/8
  Elevator E3: Floor 0, IDLE, Passengers: 0/8

--- Passengers Requesting Elevators ---

[E1] Picked up Alice [P1] at Floor 0
[E1] Moving UP: Floor 0 -> Floor 5
[E1] Dropped off Alice [P1] at Floor 5
  [Floor 0 Panel] Completed: Request[REQ-1] Alice [P1]: Floor 0 -> Floor 5 (UP) via E1

[E2] Picked up Bob [P2] at Floor 0
[E2] Moving UP: Floor 0 -> Floor 7
[E2] Dropped off Bob [P2] at Floor 7
  [Floor 0 Panel] Completed: Request[REQ-2] Bob [P2]: Floor 0 -> Floor 7 (UP) via E2

--- Final Elevator Positions ---
=== Tech Tower Elevator System ===
  Elevator E1: Floor 5, IDLE, Passengers: 0/8
  Elevator E2: Floor 7, IDLE, Passengers: 0/8
  Elevator E3: Floor 0, IDLE, Passengers: 0/8

--- Completed Requests ---
  Request[REQ-1] Alice [P1]: Floor 0 -> Floor 5 (UP) via E1
  Request[REQ-2] Bob [P2]: Floor 0 -> Floor 7 (UP) via E2
```

---

## Common Operations

### Request an Elevator
```java
ElevatorRequest req = system.requestElevator(person, sourceFloor, destFloor);
```

### Put Elevator in Maintenance
```java
elevator.setMaintenance(true);   // Out of service
elevator.setMaintenance(false);  // Back in service
```

### Check Elevator Status
```java
System.out.println(elevator.getStatusDisplay());
// Output: "Elevator E1: Floor 5, IDLE, Passengers: 0/8"
```

### Check System Status
```java
System.out.println(system.getStatusDisplay());
// Shows all elevator positions and states
```

---

## Core Flow (Remember This)

```
REQUEST:  Person → FloorPanel.requestElevator(person, destFloor)
              → ElevatorSystem.requestElevator(person, source, dest)
              → ElevatorSelectionStrategy.selectElevator()
              → Elevator.processRequest()
                  → moveTo(sourceFloor)  → pick up
                  → moveTo(destFloor)    → drop off
              → ElevatorRequest returned
```

---

## Interview-Ready Features

### 1. Strategy Pattern for Elevator Selection
The core interview differentiator. Selection algorithm is pluggable:
```java
system.setSelectionStrategy(new NearestElevatorStrategy());
// Later: system.setSelectionStrategy(new LeastLoadedStrategy());
```

### 2. NearestElevatorStrategy Logic
Two-pass selection — shows you think about optimization:
1. **First pass**: elevators going in the same direction that haven't passed the floor
2. **Fallback**: any nearest available elevator

### 3. Elevator.canServe() — Smart Availability Check
Considers state, capacity, direction, and position:
```java
public boolean canServe(int floor, Direction dir) {
    if (state == MAINTENANCE) return false;     // Out of service
    if (passengerCount >= maxCapacity) return false; // Full
    if (state == IDLE) return true;              // Free to serve
    // Moving same direction and floor is ahead
    if (direction == dir) {
        if (dir == UP && currentFloor <= floor) return true;
        if (dir == DOWN && currentFloor >= floor) return true;
    }
    return false;
}
```

### 4. Auto-Computed Direction
No room for caller error — direction is derived from floors:
```java
this.direction = sourceFloor < destinationFloor ? Direction.UP : Direction.DOWN;
```

### 5. Maintenance Mode
One-liner to take an elevator out of service. Strategy automatically skips it:
```java
elevator.setMaintenance(true);  // canServe() now returns false
```

### 6. Thread Safety at Two Levels
- **System level**: `synchronized` on `requestElevator()` — prevents conflicting assignments
- **Elevator level**: `synchronized` on `processRequest()` — prevents state corruption

### 7. Encapsulated Movement
`Elevator.processRequest()` handles the full lifecycle internally — the system just says "serve this request" and doesn't micromanage floor-by-floor movement.

---

## Possible Extensions (Mention If Asked)

- **LeastLoadedStrategy** → pick elevator with fewest current passengers
- **ZoneBasedStrategy** → assign elevator ranges to floor zones (1-5, 6-10)
- **Priority floors** → ground floor gets higher weight in selection
- **Express elevators** → skip intermediate floors, serve only certain ranges
- **Door management** → OPENING, OPEN, CLOSING states for the elevator
- **Weight-based capacity** → replace passenger count with weight sensor
- **Emergency stop** → immediate halt, override all requests
- **Request queuing** → async model with event-driven dispatch
