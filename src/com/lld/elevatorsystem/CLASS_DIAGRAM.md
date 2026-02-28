# Class Diagram - Elevator System

Simplified class diagram showing core structure and relationships.

---

## UML Class Diagram

```mermaid
classDiagram
    class ElevatorSystem {
        -String buildingName
        -int totalFloors
        -List~Elevator~ elevators
        -ElevatorSelectionStrategy selectionStrategy
        +getInstance(String, int) ElevatorSystem$
        +requestElevator(Person, int, int) ElevatorRequest
        +dispatchElevators() void
        +addElevator(Elevator) void
        +setSelectionStrategy(ElevatorSelectionStrategy) void
    }

    class Elevator {
        -String elevatorId
        -int maxCapacity
        -int currentFloor
        -ElevatorState state
        -Direction direction
        -int passengerCount
        -List~ElevatorRequest~ pendingRequests
        +addRequest(ElevatorRequest) void
        +processPendingRequests() void
        +hasPendingPickupAt(int) boolean
        +canServe(int, Direction) boolean
        +isAvailable() boolean
        +getDistanceTo(int) int
        +setMaintenance(boolean) void
    }

    class ElevatorRequest {
        -String requestId
        -Person person
        -int sourceFloor
        -int destinationFloor
        -Direction direction
        -Elevator assignedElevator
        +assignElevator(Elevator) void
    }

    class Person {
        -String personId
        -String name
    }

    class FloorPanel {
        <<destination dispatch keypad>>
        -int floorNumber
        +requestElevator(Person, int) ElevatorRequest
    }

    class ElevatorSelectionStrategy {
        <<interface>>
        +selectElevator(List~Elevator~, int, Direction) Elevator
    }

    class NearestElevatorStrategy {
        +selectElevator(List~Elevator~, int, Direction) Elevator
    }

    class SmartElevatorStrategy {
        +selectElevator(List~Elevator~, int, Direction) Elevator
        -calculateCost(Elevator, int, Direction) int
    }

    class Direction {
        <<enumeration>>
        UP
        DOWN
        IDLE
    }

    class ElevatorState {
        <<enumeration>>
        IDLE
        MOVING
        MAINTENANCE
    }

    %% Core Relationships
    ElevatorSystem "1" *-- "*" Elevator
    ElevatorSystem --> ElevatorSelectionStrategy
    ElevatorSystem ..> ElevatorRequest : creates

    Elevator "1" o-- "*" ElevatorRequest : pending queue

    ElevatorRequest --> Person
    ElevatorRequest --> Elevator : assigned to

    FloorPanel ..> ElevatorSystem : uses

    NearestElevatorStrategy ..|> ElevatorSelectionStrategy
    SmartElevatorStrategy ..|> ElevatorSelectionStrategy

    Elevator --> ElevatorState
    Elevator --> Direction
    ElevatorRequest --> Direction
```

---

## Key Relationships

| Relationship | Type | Description |
|--------------|------|-------------|
| `ElevatorSystem → Elevator` | Composition (1 to Many) | System owns elevators |
| `ElevatorSystem → ElevatorSelectionStrategy` | Dependency | Uses selection algorithm |
| `Elevator → ElevatorRequest` | Aggregation (pending queue) | Elevator holds queued requests |
| `ElevatorRequest → Person` | Association | Request belongs to a person |
| `ElevatorRequest → Elevator` | Association | Request assigned to an elevator |
| `FloorPanel → ElevatorSystem` | Dependency | Keypad delegates to system |
| `SmartElevatorStrategy → ElevatorSelectionStrategy` | Implementation | Implements interface |

---

## Design Patterns

1. **Singleton** - `ElevatorSystem` (one instance per building)
2. **Strategy** - `ElevatorSelectionStrategy` (swappable elevator selection algorithm)

---

## Package Structure

```
com.lld.elevatorsystem
├── enums/        (Direction, ElevatorState)
├── models/       (Person, Elevator, ElevatorRequest, ElevatorSystem)
├── panels/       (FloorPanel — destination dispatch keypad)
└── strategy/     (ElevatorSelectionStrategy, NearestElevatorStrategy, SmartElevatorStrategy)
```

---

## Flow Summary (Two-Phase)

**Phase 1 — Assign:** `FloorPanel` → `ElevatorSystem.requestElevator()` → `SmartElevatorStrategy` → `Elevator.addRequest()` → panel shows "Go to E1"

**Phase 2 — Dispatch:** `ElevatorSystem.dispatchElevators()` → `Elevator.processPendingRequests()` → batch pickup → visit destinations → drop off
