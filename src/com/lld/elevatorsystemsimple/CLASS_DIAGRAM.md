# Class Diagram - Elevator System (Simple)

Simplified class diagram showing core structure and relationships.

---

## UML Class Diagram

```mermaid
classDiagram
    class ElevatorSystem {
        -String buildingName
        -List~Elevator~ elevators
        -List~Floor~ floors
        -DispatchStrategy dispatchStrategy
        +getInstance(String) ElevatorSystem$
        +addElevator(Elevator) void
        +requestElevator(int, Direction) Elevator
        +selectFloor(Elevator, int) void
        +dispatchAll() void
        +showStatus() void
    }

    class Elevator {
        -String elevatorId
        -int currentFloor
        -Direction direction
        -DoorState doorState
        -int passengerCount
        -double currentWeightKg
        -TreeSet~Integer~ upStops
        -TreeSet~Integer~ downStops
        -InternalDisplay internalDisplay
        +addDestination(int) void
        +moveOneFloor() void
        +processAllStops() void
        +openDoor() void
        +closeDoor() void
        +addPassengers(int, double) void
        +removePassengers(int, double) void
        +isAvailable() boolean
        +isIdle() boolean
    }

    class Floor {
        -int floorNumber
        -OutsidePanel outsidePanel
        -ExternalDisplay externalDisplay
    }

    class OutsidePanel {
        -int floorNumber
        +pressUp() Elevator
        +pressDown() Elevator
    }

    class InsidePanel {
        -Elevator elevator
        +pressFloor(int) void
        +pressOpenDoor() void
        +pressCloseDoor() void
    }

    class InternalDisplay {
        -Elevator elevator
        +show() void
    }

    class ExternalDisplay {
        -int floorNumber
        +showAll(List~Elevator~) void
    }

    class DispatchStrategy {
        <<interface>>
        +selectElevator(List~Elevator~, int, Direction) Elevator
    }

    class NearestDispatchStrategy {
        +selectElevator(List~Elevator~, int, Direction) Elevator
    }

    class Direction {
        <<enumeration>>
        UP
        DOWN
        IDLE
    }

    class DoorState {
        <<enumeration>>
        OPEN
        CLOSED
    }

    %% Core Relationships
    ElevatorSystem "1" *-- "1..3" Elevator : manages
    ElevatorSystem "1" *-- "0..15" Floor : has
    ElevatorSystem --> DispatchStrategy : uses

    Floor "1" *-- "1" OutsidePanel : has
    Floor "1" *-- "1" ExternalDisplay : has

    Elevator "1" *-- "1" InternalDisplay : has
    InsidePanel --> Elevator : wraps

    OutsidePanel ..> ElevatorSystem : calls
    InsidePanel ..> ElevatorSystem : calls

    NearestDispatchStrategy ..|> DispatchStrategy

    Elevator --> Direction
    Elevator --> DoorState
```

---

## Key Relationships

| Relationship | Type | Description |
|--------------|------|-------------|
| `ElevatorSystem → Elevator` | Composition (1 to 3) | System manages up to 3 elevators |
| `ElevatorSystem → Floor` | Composition (1 to 16) | System owns floors 0-15 |
| `ElevatorSystem → DispatchStrategy` | Dependency | Uses strategy for dispatch |
| `Floor → OutsidePanel` | Composition (1 to 1) | Each floor has one outside panel |
| `Floor → ExternalDisplay` | Composition (1 to 1) | Each floor has one external display |
| `Elevator → InternalDisplay` | Composition (1 to 1) | Each elevator has one internal display |
| `InsidePanel → Elevator` | Association | Panel wraps an elevator for control |
| `OutsidePanel → ElevatorSystem` | Dependency | Panel calls system to request elevator |
| `NearestDispatchStrategy → DispatchStrategy` | Implementation | Implements strategy interface |

---

## Design Patterns

1. **Singleton** - `ElevatorSystem` (one controller per building)
2. **Strategy** - `DispatchStrategy` (swappable dispatch algorithm)

---

## Package Structure

```
com.lld.elevatorsystemsimple
├── enums/        (Direction, DoorState)
├── models/       (Elevator, Floor, ElevatorSystem)
├── panels/       (OutsidePanel, InsidePanel)
├── display/      (InternalDisplay, ExternalDisplay)
└── dispatcher/   (DispatchStrategy, NearestDispatchStrategy)
```

---

## Flow Summary

**Call Elevator:**
`OutsidePanel` → `ElevatorSystem` → `DispatchStrategy` → best `Elevator` → `addDestination(floor)`

**Select Floor:**
`InsidePanel` → `ElevatorSystem` → `Elevator.addDestination(floor)`

**Dispatch:**
`ElevatorSystem.dispatchAll()` → each `Elevator.processAllStops()` → `moveOneFloor()` → `stop()` → `InternalDisplay.show()`

---

## Quick-Draw Version (For Whiteboard)

```
ElevatorSystem (Singleton)
  ├── Elevator [1..3]
  │     ├── InternalDisplay
  │     ├── upStops (TreeSet)
  │     └── downStops (TreeSet)
  ├── Floor [0..15]
  │     ├── OutsidePanel → pressUp/Down → ElevatorSystem
  │     └── ExternalDisplay
  └── DispatchStrategy (interface)
        └── NearestDispatchStrategy

InsidePanel → wraps Elevator → pressFloor/OpenDoor/CloseDoor
```
