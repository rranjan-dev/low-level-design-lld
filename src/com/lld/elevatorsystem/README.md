# Elevator System - Low Level Design

Simple, interview-ready implementation of a multi-elevator system using OOP and design patterns.

## How It Works

```
Person presses FloorPanel → ElevatorSystem selects best Elevator → Elevator processes the ride
```

## File Structure

```
elevatorsystem/
├── ElevatorSystemDemo.java              # Demo class (run this)
├── enums/
│   ├── Direction.java                   # UP, DOWN, IDLE
│   └── ElevatorState.java              # IDLE, MOVING, MAINTENANCE
├── models/
│   ├── ElevatorSystem.java             # Singleton — manages all elevators
│   ├── Elevator.java                   # Single elevator car
│   ├── ElevatorRequest.java            # A ride request (source → destination)
│   └── Person.java                     # Passenger
├── panels/
│   └── FloorPanel.java                 # UP/DOWN button panel on each floor
└── strategy/
    ├── ElevatorSelectionStrategy.java   # Strategy interface
    └── NearestElevatorStrategy.java     # Picks nearest available elevator
```

## Design Patterns

1. **Singleton** — `ElevatorSystem` ensures one controller per building
2. **Strategy** — `ElevatorSelectionStrategy` makes elevator selection algorithm pluggable
3. **Encapsulation** — `Elevator.processRequest()` hides internal movement details

## Key Classes

### ElevatorSystem (Singleton)
- `requestElevator(person, sourceFloor, destFloor)` — selects best elevator, processes ride
- `getStatusDisplay()` — shows all elevator positions
- `addElevator()` / `setSelectionStrategy()` — configuration

### Elevator
- `processRequest(request)` — moves to source, picks up, moves to destination, drops off
- `canServe(floor, direction)` — can this elevator handle a request? (used by strategy)
- `getDistanceTo(floor)` — distance from current floor (used by strategy)
- `setMaintenance(boolean)` — take elevator out of service

### ElevatorRequest
- Auto-incrementing ID, person, source/destination floors
- Direction auto-computed from floor numbers
- Tracks which elevator was assigned

### FloorPanel
- Represents the UP/DOWN buttons on a floor
- `requestElevator(person, destinationFloor)` — delegates to ElevatorSystem

### NearestElevatorStrategy
1. Find nearest elevator already going in the same direction
2. Fallback: any nearest available elevator

## Usage

```java
ElevatorSystem system = ElevatorSystem.getInstance("Tech Tower", 10);
system.setSelectionStrategy(new NearestElevatorStrategy());
system.addElevator(new Elevator("E1", 8));
system.addElevator(new Elevator("E2", 8));

FloorPanel ground = new FloorPanel(0);
ElevatorRequest r = ground.requestElevator(new Person("P1", "Alice"), 5);
// Output:
//   [E1] Picked up Alice [P1] at Floor 0
//   [E1] Moving UP: Floor 0 -> Floor 5
//   [E1] Dropped off Alice [P1] at Floor 5
```

## Interview Talking Points

- **Why Singleton?** — One controller per building, avoids conflicting elevator assignments
- **Why Strategy?** — Different buildings need different algorithms (nearest, least loaded, zone-based)
- **How to handle capacity?** — `maxCapacity` check before pickup, `canServe()` returns false when full
- **How to handle maintenance?** — `MAINTENANCE` state excludes elevator from selection
- **Thread safety?** — `synchronized` on `processRequest()` and `requestElevator()`
- **How to extend?** — New strategies (LeastLoadedStrategy, ZoneBasedStrategy), weight-based capacity, priority floors, door open/close states
