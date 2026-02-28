# Quick Start Guide - Elevator System (Simple)

---

## Problem Statement

> Design an Elevator System for a 15-floor building with 3 elevators. The system should handle elevator dispatching, movement, door operations, capacity limits, and provide displays both inside elevators and on each floor.

### Functional Requirements

1. **3 elevators** servicing **15 floors**
2. Elevators move **UP**, **DOWN**, or remain **IDLE**
3. Doors open **only when stopped**, not while moving
4. **Outside panel** on each floor: UP and DOWN buttons to call elevator
5. **Inside panel** per elevator: floor buttons + door open/close
6. **Internal display**: floor, direction, passenger count/capacity
7. **External display**: each elevator's floor and direction per floor
8. **Smart dispatch**: assigns nearest/most suitable elevator
9. **Capacity**: max 8 passengers or 680 kg per elevator

---

## Quick Compile & Run

```bash
# Compile
javac -d out src/com/lld/elevatorsystemsimple/**/*.java src/com/lld/elevatorsystemsimple/*.java

# Run
java -cp out com.lld.elevatorsystemsimple.ElevatorSystemDemo
```

---

## Key Classes at a Glance

| Class | Purpose | Key Method |
|-------|---------|------------|
| `ElevatorSystem` | Singleton controller | `requestElevator()`, `selectFloor()`, `dispatchAll()` |
| `Elevator` | Elevator car (movement, doors, capacity) | `addDestination()`, `processAllStops()`, `openDoor()` |
| `Floor` | Building floor with panel + display | Constructor creates `OutsidePanel` + `ExternalDisplay` |
| `OutsidePanel` | UP/DOWN buttons on each floor | `pressUp()`, `pressDown()` |
| `InsidePanel` | Floor + door buttons inside elevator | `pressFloor()`, `pressOpenDoor()`, `pressCloseDoor()` |
| `InternalDisplay` | Display inside elevator | `show()` |
| `ExternalDisplay` | Display on each floor | `showAll(elevators)` |
| `DispatchStrategy` | Dispatch interface | `selectElevator()` |
| `NearestDispatchStrategy` | Nearest elevator logic | `selectElevator()` |

---

## Key Concepts

### Direction States
- **UP** - Elevator moving upward
- **DOWN** - Elevator moving downward
- **IDLE** - Elevator stationary, no pending stops

### Door States
- **OPEN** - Doors are open (only when stopped)
- **CLOSED** - Doors are closed (required for movement)

### Dispatch Priority
```
1. IDLE elevator nearest to requested floor      (best — lowest distance)
2. Moving elevator heading towards floor, same direction  (distance + 1)
3. Any available elevator                        (fallback)
```

---

## Code Snippets

### Basic Usage

```java
// 1. Initialize system
ElevatorSystem system = ElevatorSystem.getInstance("My Building");
system.setDispatchStrategy(new NearestDispatchStrategy());

// 2. Add elevators (max 3)
system.addElevator(new Elevator("E1"));
system.addElevator(new Elevator("E2"));
system.addElevator(new Elevator("E3"));

// 3. Person at Floor 0 presses UP
OutsidePanel panel = system.getFloors().get(0).getOutsidePanel();
Elevator assigned = panel.pressUp();

// 4. Dispatch elevator to pickup floor
system.dispatchAll();

// 5. Person boards and selects destination
assigned.addPassengers(1, 70.0);
InsidePanel insidePanel = new InsidePanel(assigned);
insidePanel.pressFloor(10);

// 6. Elevator delivers to destination
system.dispatchAll();
assigned.removePassengers(1, 70.0);

// 7. Check status
system.showStatus();
```

---

## Design Patterns Used

1. **Singleton** - `ElevatorSystem` (one controller per building)
2. **Strategy** - `DispatchStrategy` (swappable dispatch algorithm)

---

## Testing Scenarios

### Test Case 1: Basic Elevator Call
```java
OutsidePanel panel = system.getFloors().get(5).getOutsidePanel();
Elevator e = panel.pressUp();
system.dispatchAll();
// Expected: Nearest idle elevator moves to Floor 5
```

### Test Case 2: Smart Dispatch
```java
// E1 at Floor 10, E2 at Floor 2, E3 at Floor 0 — all IDLE
// Person presses UP at Floor 3
Elevator e = system.getFloors().get(3).getOutsidePanel().pressUp();
// Expected: E2 assigned (distance 1, nearest idle)
```

### Test Case 3: Capacity Limit
```java
Elevator e1 = system.getElevators().get(0);
e1.addPassengers(8, 640.0);  // OK — at max
e1.addPassengers(1, 70.0);   // REJECTED — capacity exceeded
```

### Test Case 4: Door Safety
```java
e1.addDestination(10);
e1.moveOneFloor();
e1.openDoor();   // REJECTED — cannot open door while moving
```

---

## Status Display Format

```
=== Skyline Tower — Elevator Status ===
  E1 | Floor: 10 | IDLE | Door: CLOSED | Passengers: 0/8 | Weight: 0/680 kg
  E2 | Floor:  2 | IDLE | Door: CLOSED | Passengers: 0/8 | Weight: 0/680 kg
  E3 | Floor:  0 | IDLE | Door: CLOSED | Passengers: 0/8 | Weight: 0/680 kg
```

---

## Core Flow (Remember This)

```
CALL:     Passenger → OutsidePanel.pressUp/Down()
              → ElevatorSystem.requestElevator(floor, direction)
              → DispatchStrategy.selectElevator() → best Elevator
              → Elevator.addDestination(floor)

BOARD:    system.dispatchAll() → Elevator arrives → Door opens
              → Passenger boards → InsidePanel.pressFloor(destination)
              → Elevator.addDestination(destination)

DELIVER:  system.dispatchAll() → Elevator moves to destination
              → Door opens → Passenger exits
```

---

## Interview-Ready Features

### 1. Smart Dispatch with Strategy Pattern
Pluggable algorithm. Easy to swap:
```java
system.setDispatchStrategy(new NearestDispatchStrategy());
// Later: system.setDispatchStrategy(new LeastLoadedStrategy());
```

### 2. TreeSet for Ordered Stops
UP stops in ascending order, DOWN stops in descending order. No duplicates.
```
upStops: [3, 7, 12]  → visits 3, then 7, then 12
downStops: [10, 5, 1] → visits 10, then 5, then 1
```

### 3. Door Safety Enforcement
Door cannot open while stops remain — prevents unsafe operation.

### 4. Dual Capacity Limits
Both passenger count (8) and weight (680 kg) enforced — shows real-world thinking.

### 5. Boundary Separation
Panels/Displays are thin wrappers — business logic stays in models.

---

## Possible Extensions (Mention If Asked)

- **Priority/VIP elevator** - New dispatch strategy with priority queue
- **Maintenance mode** - Add MAINTENANCE state to Direction/Elevator
- **Emergency stop** - Clear all stops, open door immediately
- **Weight sensor** - Auto-detect passenger weight on entry
- **Multi-building** - Remove Singleton, use building registry
- **Real-time threading** - Actual concurrent elevator movement with Thread/ExecutorService
