# Quick Start Guide - Elevator System

A quick reference guide for understanding and using the Elevator System.

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

## Important Notes

1. **Synchronous Processing**: Each request completes the full ride immediately
2. **Thread Safety**: requestElevator() and processRequest() are synchronized
3. **Capacity Check**: Elevator rejects requests when at maxCapacity
4. **Maintenance**: Maintenance elevators are skipped by the selection strategy
5. **Direction Auto-Computed**: sourceFloor < destinationFloor → UP, otherwise → DOWN
6. **Request Flow**: select elevator → move to source → pick up → move to destination → drop off
