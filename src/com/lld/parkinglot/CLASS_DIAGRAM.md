# Class Diagram - Parking Lot System

Simplified class diagram showing core structure and relationships.

---

## UML Class Diagram

```mermaid
classDiagram
    class ParkingLot {
        -String name
        -List~ParkingFloor~ floors
        -PricingStrategy pricingStrategy
        +getInstance(String) ParkingLot$
        +parkVehicle(Vehicle) ParkingTicket
        +unparkVehicle(ParkingTicket) double
        +addFloor(ParkingFloor) void
        +setPricingStrategy(PricingStrategy) void
    }

    class ParkingFloor {
        -int floorNumber
        -Map~SpotType_List~ParkingSpot~~ spotsByType
        +addSpot(ParkingSpot) void
        +findAvailableSpot(VehicleType) ParkingSpot
        +getAvailableCount(SpotType) int
    }

    class ParkingSpot {
        -String spotId
        -SpotType spotType
        -boolean available
        -Vehicle parkedVehicle
        +assignVehicle(Vehicle) void
        +removeVehicle() Vehicle
        +isAvailable() boolean
    }

    class Vehicle {
        -String licensePlate
        -VehicleType vehicleType
    }

    class ParkingTicket {
        -String ticketId
        -Vehicle vehicle
        -ParkingSpot spot
        -LocalDateTime entryTime
        -LocalDateTime exitTime
        -double charges
        +markExitTime() void
        +setCharges(double) void
    }

    class EntryPanel {
        -String panelId
        +issueTicket(Vehicle) ParkingTicket
    }

    class ExitPanel {
        -String panelId
        +processExit(ParkingTicket) double
    }

    class PricingStrategy {
        <<interface>>
        +calculateCharge(ParkingTicket) double
    }

    class HourlyPricingStrategy {
        -Map~VehicleType_Double~ ratePerHour
        +calculateCharge(ParkingTicket) double
    }

    class VehicleType {
        <<enumeration>>
        MOTORCYCLE
        CAR
        TRUCK
        +getRequiredSpotType() SpotType
    }

    class SpotType {
        <<enumeration>>
        SMALL
        MEDIUM
        LARGE
    }

    %% Core Relationships
    ParkingLot "1" *-- "*" ParkingFloor
    ParkingLot --> PricingStrategy
    ParkingLot ..> ParkingTicket : creates
    
    ParkingFloor "1" *-- "*" ParkingSpot
    
    ParkingSpot --> Vehicle : holds
    ParkingTicket --> Vehicle
    ParkingTicket --> ParkingSpot
    
    EntryPanel ..> ParkingLot : uses
    ExitPanel ..> ParkingLot : uses
    
    HourlyPricingStrategy ..|> PricingStrategy
    
    Vehicle --> VehicleType
    ParkingSpot --> SpotType
    VehicleType --> SpotType
```

---

## Key Relationships

| Relationship | Type | Description |
|--------------|------|-------------|
| `ParkingLot → ParkingFloor` | Composition (1 to Many) | ParkingLot owns floors |
| `ParkingFloor → ParkingSpot` | Composition (1 to Many) | Floor owns spots |
| `ParkingSpot → Vehicle` | Association (1 to 0..1) | Spot can hold one vehicle |
| `ParkingTicket → Vehicle + Spot` | Association | Ticket links vehicle to spot |
| `EntryPanel → ParkingLot` | Dependency | Panel uses ParkingLot |
| `ExitPanel → ParkingLot` | Dependency | Panel uses ParkingLot |
| `ParkingLot → PricingStrategy` | Dependency | Uses pricing interface |
| `HourlyPricingStrategy → PricingStrategy` | Implementation | Implements interface |

---

## Design Patterns

1. **Singleton** - `ParkingLot` (one instance)
2. **Strategy** - `PricingStrategy` (swappable pricing)

---

## Package Structure

```
com.lld.parkinglot
├── enums/        (SpotType, VehicleType)
├── models/       (Vehicle, ParkingSpot, ParkingFloor, ParkingTicket, ParkingLot)
├── panels/       (EntryPanel, ExitPanel)
└── pricing/      (PricingStrategy, HourlyPricingStrategy)
```

---

## Flow Summary

**Entry:** `EntryPanel` → `ParkingLot` → `ParkingFloor` → `ParkingSpot` → `ParkingTicket`

**Exit:** `ExitPanel` → `ParkingLot` → `ParkingSpot` (free) → `PricingStrategy` (calculate) → return charges
