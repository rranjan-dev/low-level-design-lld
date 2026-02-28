# Elevator System (Simple) - Low Level Design (LLD)

A simplified, interview-ready implementation of an Elevator System. This system demonstrates object-oriented design principles, design patterns, and clean separation of concerns.

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Architecture](#architecture)
- [Class Design](#class-design)
- [Design Patterns](#design-patterns)
- [Key Features](#key-features)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Code Walkthrough](#code-walkthrough)
- [Interview Talking Points](#interview-talking-points)

---

## Overview

This Elevator System manages 3 elevators servicing a 15-floor building. It handles elevator dispatching, movement, door operations, capacity management, and real-time status displays. The design emphasizes:

- **Separation of Concerns**: Clear boundaries between models, panels, displays, and dispatch logic
- **Strategy Pattern**: Pluggable dispatch algorithms for smart elevator assignment
- **Simplicity**: Clean, easy-to-understand code for interview preparation
- **Safety**: Door cannot open while moving, capacity/weight limits enforced

---

## Requirements

### Functional Requirements

1. **Elevators and Floors**: 3 elevator cars servicing a building with up to 15 floors
2. **Elevator Movement**: Elevators move UP, DOWN, or remain IDLE
3. **Door Operation**: Doors open only when the elevator is idle/stopped, not in motion
4. **Floor Access**: Each elevator can stop at every floor in the building
5. **Outside Control Panel**: UP and DOWN buttons on each floor to call an elevator
6. **Inside Control Panel**: Floor buttons + door open/close buttons inside each elevator
7. **Internal Display**: Shows current floor, direction, and passenger count/capacity
8. **External Display**: Shows each elevator's current floor and direction on every floor
9. **Multiple Passengers**: Supports passengers going to different floors and directions simultaneously
10. **Smart Dispatch**: Intelligently assigns the nearest/most suitable elevator
11. **Capacity**: Maximum 8 passengers or 680 kg per elevator
12. **Max 3 Elevators**: Building equipped with up to 3 elevators

### Non-Functional Requirements

1. **Extensibility**: Easy to add new dispatch strategies
2. **Simplicity**: Easy to understand, remember, and code in an interview

---

## Architecture

### High-Level Architecture

```
Passenger → OutsidePanel (UP/DOWN) → ElevatorSystem (Singleton)
                                       ↓
                              DispatchStrategy → selects Elevator
                                       ↓
Passenger → InsidePanel (Floor buttons) → Elevator → processes stops
                                       ↓
                           InternalDisplay / ExternalDisplay → shows status
```

### Data Flow

**Calling an Elevator (Outside):**
```
Passenger → OutsidePanel.pressUp/pressDown()
        → ElevatorSystem.requestElevator(floor, direction)
        → DispatchStrategy.selectElevator() → best Elevator
        → Elevator.addDestination(floor) → elevator queued to arrive
```

**Selecting a Floor (Inside):**
```
Passenger → InsidePanel.pressFloor(destinationFloor)
        → ElevatorSystem.selectFloor(elevator, floor)
        → Elevator.addDestination(floor) → stop added
```

**Elevator Movement:**
```
ElevatorSystem.dispatchAll()
        → Elevator.processAllStops()
        → moveOneFloor() → stop() → openDoor() → InternalDisplay.show() → closeDoor()
```

---

## Class Design

### Core Classes

#### 1. Enums

**`Direction`** - Elevator movement direction
- `UP`, `DOWN`, `IDLE`

**`DoorState`** - Elevator door state
- `OPEN`, `CLOSED`

#### 2. Models

**`Elevator`**
- Core elevator car with movement, door, and capacity management
- Uses two `TreeSet<Integer>` (upStops, downStops) for ordered stop processing
- Methods: `addDestination()`, `moveOneFloor()`, `processAllStops()`, `openDoor()`, `closeDoor()`
- Capacity: MAX_CAPACITY=8, MAX_WEIGHT_KG=680
- Safety: Door cannot open while stops remain, capacity/weight enforced

**`Floor`**
- Represents a building floor
- Owns an `OutsidePanel` and `ExternalDisplay`

**`ElevatorSystem`** (Singleton)
- Central controller managing all elevators and floors
- MAX_ELEVATORS=3, TOTAL_FLOORS=15
- Methods: `requestElevator()`, `selectFloor()`, `dispatchAll()`, `showStatus()`

#### 3. Panels (Boundary Layer)

**`OutsidePanel`**
- UP and DOWN buttons on each floor
- Delegates to `ElevatorSystem.requestElevator()`

**`InsidePanel`**
- Floor buttons + door open/close inside each elevator
- Methods: `pressFloor()`, `pressOpenDoor()`, `pressCloseDoor()`

#### 4. Displays

**`InternalDisplay`**
- Inside each elevator: shows current floor, direction, and passengers/capacity

**`ExternalDisplay`**
- On each floor: shows each elevator's current floor and direction

#### 5. Dispatcher

**`DispatchStrategy`** (Interface)
- Strategy pattern interface for elevator selection
- Single method: `selectElevator(elevators, floor, direction)`

**`NearestDispatchStrategy`**
- Selects: idle nearest > same-direction moving towards > any available fallback

---

## Design Patterns

### 1. Singleton Pattern
**Where:** `ElevatorSystem` class
**Why:** Only one elevator controller should exist per building
```java
public static synchronized ElevatorSystem getInstance(String buildingName) {
    if (instance == null) {
        instance = new ElevatorSystem(buildingName);
    }
    return instance;
}
```

### 2. Strategy Pattern
**Where:** `DispatchStrategy` interface and `NearestDispatchStrategy`
**Why:** Dispatch algorithm should be swappable (nearest, least loaded, etc.)
```java
system.setDispatchStrategy(new NearestDispatchStrategy());
```

---

## Key Features

### 1. Smart Dispatch
- Prefers idle elevators nearest to the requested floor
- Considers elevators already moving towards the floor in the same direction
- Falls back to any available elevator

### 2. Ordered Stop Processing
- `TreeSet<Integer>` for upStops and downStops ensures floors visited in order
- UP stops processed low-to-high, DOWN stops processed high-to-low
- Direction automatically switches when one direction's stops are exhausted

### 3. Door Safety
- Door cannot open while elevator has remaining stops (in motion)
- Door operations logged for audit trail

### 4. Capacity & Weight Limits
- Max 8 passengers per elevator
- Max 680 kg per elevator
- Both limits enforced with clear error messages

### 5. Real-Time Status
- Per-elevator status: floor, direction, door state, passengers, weight
- Internal display shows capacity inside elevator
- External display shows all elevator positions on each floor

---

## Project Structure

```
low-level-design-lld/
└── src/
    └── com/
        └── lld/
            └── elevatorsystemsimple/
                ├── ElevatorSystemDemo.java     # Main driver class
                ├── enums/
                │   ├── Direction.java           # UP, DOWN, IDLE
                │   └── DoorState.java           # OPEN, CLOSED
                ├── models/
                │   ├── Elevator.java            # Core elevator car
                │   ├── Floor.java               # Building floor
                │   └── ElevatorSystem.java      # Singleton controller
                ├── panels/
                │   ├── OutsidePanel.java        # UP/DOWN buttons per floor
                │   └── InsidePanel.java         # Floor + door buttons per elevator
                ├── display/
                │   ├── InternalDisplay.java     # Inside elevator display
                │   └── ExternalDisplay.java     # Per-floor elevator status
                └── dispatcher/
                    ├── DispatchStrategy.java     # Strategy interface
                    └── NearestDispatchStrategy.java  # Nearest elevator logic
```

---

## How to Run

### Prerequisites
- Java JDK 8 or higher

### Compilation

```bash
cd /Users/rranjan/Documents/MY-REPOSITORIES/personal/low-level-design-lld

javac -d out src/com/lld/elevatorsystemsimple/**/*.java src/com/lld/elevatorsystemsimple/*.java
```

### Execution

```bash
java -cp out com.lld.elevatorsystemsimple.ElevatorSystemDemo
```

---

## Code Walkthrough

### Example: Calling an Elevator from a Floor

```java
// 1. Get the floor's outside panel
Floor groundFloor = system.getFloors().get(0);
OutsidePanel panel = groundFloor.getOutsidePanel();

// 2. Press UP button — system assigns nearest elevator
Elevator assigned = panel.pressUp();
// Internally: ElevatorSystem.requestElevator(0, Direction.UP)
//           → DispatchStrategy selects best elevator
//           → Elevator.addDestination(0) queued

// 3. Dispatch — elevator moves to ground floor
system.dispatchAll();
```

### Example: Riding an Elevator

```java
// 1. Passenger boards and adds weight
assigned.addPassengers(1, 70.0);

// 2. Press destination floor on inside panel
InsidePanel insidePanel = new InsidePanel(assigned);
insidePanel.pressFloor(10);

// 3. Elevator moves to destination
system.dispatchAll();

// 4. Passenger exits
assigned.removePassengers(1, 70.0);
```

### Example: Smart Dispatch

```java
// E1 at Floor 10 (IDLE), E2 at Floor 2 (IDLE), E3 at Floor 0 (IDLE)
// Person at Floor 3 presses UP
// → NearestDispatchStrategy selects E2 (distance 1) over E3 (distance 3) or E1 (distance 7)
```

---

## Interview Talking Points

### SOLID Principles

1. **Single Responsibility**: Each class has one job
   - `Elevator` manages movement and stops
   - `DispatchStrategy` only selects elevators
   - `OutsidePanel` only handles floor button presses

2. **Open/Closed**: Open for extension, closed for modification
   - New dispatch algorithm? Implement `DispatchStrategy` interface
   - No changes needed to `ElevatorSystem`

3. **Liskov Substitution**: Any `DispatchStrategy` implementation is substitutable

4. **Interface Segregation**: `DispatchStrategy` has only one method

5. **Dependency Inversion**: `ElevatorSystem` depends on `DispatchStrategy` interface, not concrete class

### Extensibility Examples

**Adding Least-Loaded Dispatch:**
```java
class LeastLoadedStrategy implements DispatchStrategy {
    public Elevator selectElevator(List<Elevator> elevators, int floor, Direction dir) {
        return elevators.stream()
            .filter(Elevator::isAvailable)
            .min(Comparator.comparingInt(Elevator::getPassengerCount))
            .orElse(null);
    }
}
system.setDispatchStrategy(new LeastLoadedStrategy());
```

### Trade-offs

1. **Singleton**: One controller per building. Could extend to multi-building.
2. **TreeSet for stops**: Ordered traversal but no duplicate floors (which is what we want).
3. **Simplicity over realism**: No real threading/timing — stops processed instantly for demo clarity.

---

**Happy Learning!**
