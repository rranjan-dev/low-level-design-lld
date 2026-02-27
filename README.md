# Parking Lot System - Low Level Design (LLD)

A comprehensive, production-ready implementation of a Parking Lot Management System designed for interview preparation. This system demonstrates object-oriented design principles, design patterns, and thread-safe concurrent operations.

## 📋 Table of Contents

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
- [Future Enhancements](#future-enhancements)

---

## 🎯 Overview

This Parking Lot System manages vehicle parking across multiple floors with different spot types. It handles entry/exit operations, spot allocation, pricing calculations, and maintains real-time availability status. The design emphasizes:

- **Separation of Concerns**: Clear boundaries between models, panels, and pricing logic
- **Thread Safety**: Synchronized operations for concurrent access
- **Extensibility**: Easy to add new vehicle types, spot types, or pricing strategies
- **SOLID Principles**: Each class has a single responsibility

---

## 📝 Requirements

### Functional Requirements

1. **Multi-floor Parking**: The parking lot has multiple floors, each with multiple parking spots
2. **Spot Types**: Three types of spots - SMALL (motorcycles), MEDIUM (cars), LARGE (trucks/buses)
3. **Flexible Allocation**: A vehicle can park in a spot that matches its size or any larger spot
4. **Ticket System**: Issues a ticket on entry with entry time, spot assignment, and vehicle details
5. **Pricing**: Calculates charges on exit based on duration and vehicle type
6. **Availability Tracking**: Real-time tracking of available spots per floor and per type
7. **Multiple Gates**: Support for multiple entry and exit panels operating concurrently

### Non-Functional Requirements

1. **Thread Safety**: Multiple gates can operate simultaneously without race conditions
2. **Extensibility**: Easy to add new vehicle types, spot types, or pricing models
3. **Maintainability**: Clean code structure following SOLID principles

---

## 🏗️ Architecture

### High-Level Architecture

```
┌─────────────┐
│   Driver    │
└──────┬──────┘
       │
       ├─────────────────┐
       │                 │
       ▼                 ▼
┌─────────────┐    ┌─────────────┐
│ EntryPanel  │    │  ExitPanel  │
└──────┬──────┘    └──────┬───────┘
       │                 │
       └────────┬────────┘
                │
                ▼
         ┌──────────────┐
         │  ParkingLot  │  (Singleton)
         │  (Orchestrator) │
         └──────┬───────┘
                │
        ┌───────┼───────┐
        │       │       │
        ▼       ▼       ▼
   ┌────────┐ ┌────────┐ ┌──────────────┐
   │ Floor 1 │ │ Floor 2 │ │ PricingStrategy│
   └────┬───┘ └────┬───┘ └───────┬────────┘
        │          │             │
        └──────┬───┘             │
               │                 │
               ▼                 ▼
         ┌──────────┐    ┌──────────────────┐
         │  Spots   │    │ HourlyPricing    │
         └──────────┘    └──────────────────┘
```

### Data Flow

**Entry Flow:**
```
Driver → EntryPanel → ParkingLot → ParkingFloor → ParkingSpot
                                    ↓
                              ParkingTicket (created)
```

**Exit Flow:**
```
Driver → ExitPanel → ParkingLot → ParkingSpot (freed)
                              ↓
                        PricingStrategy → Calculate Charges
```

---

## 🎨 Class Design

### Core Classes

#### 1. **Enums**

**`VehicleType`** - Defines vehicle categories
- `MOTORCYCLE` → requires SMALL spot
- `CAR` → requires MEDIUM spot  
- `TRUCK` → requires LARGE spot

**`SpotType`** - Defines parking spot sizes
- `SMALL`, `MEDIUM`, `LARGE` with size ranking (1, 2, 3)
- `canFit()` method enables fallback to larger spots

#### 2. **Models**

**`Vehicle`**
- Immutable class representing a vehicle
- Fields: `licensePlate`, `vehicleType`

**`ParkingSpot`**
- Manages individual parking spot state
- Thread-safe `assignVehicle()` and `removeVehicle()` methods
- Tracks: `spotId`, `spotType`, `available`, `parkedVehicle`

**`ParkingFloor`**
- Manages spots grouped by type using `EnumMap<SpotType, List<ParkingSpot>>`
- `findAvailableSpot()` implements smart search: exact match first, then fallback to larger spots
- Provides availability counts per type

**`ParkingTicket`**
- Factory method pattern (`issue()`)
- Auto-generates UUID ticket ID
- Tracks: `entryTime`, `exitTime`, `charges`, `vehicle`, `spot`

**`ParkingLot`** (Singleton)
- Central orchestrator for all parking operations
- Double-checked locking for thread-safe initialization
- Methods: `parkVehicle()`, `unparkVehicle()`, `getStatusDisplay()`

#### 3. **Panels** (Boundary Layer)

**`EntryPanel`**
- Thin wrapper that delegates to `ParkingLot.parkVehicle()`
- Issues tickets to drivers

**`ExitPanel`**
- Processes exit requests
- Delegates to `ParkingLot.unparkVehicle()`

#### 4. **Pricing**

**`PricingStrategy`** (Interface)
- Strategy pattern interface
- Single method: `calculateCharge(ParkingTicket)`

**`HourlyPricingStrategy`**
- Implements hourly pricing with different rates per vehicle type
- Charges per started hour (ceiling function)
- Rates: Motorcycle $10/hr, Car $20/hr, Truck $30/hr

---

## 🎭 Design Patterns

### 1. **Singleton Pattern**
**Where:** `ParkingLot` class  
**Why:** Only one parking lot instance should exist in the system  
**Implementation:** Double-checked locking for thread-safe lazy initialization

```java
public static ParkingLot getInstance(String name) {
    if (instance == null) {
        synchronized (ParkingLot.class) {
            if (instance == null) {
                instance = new ParkingLot(name);
            }
        }
    }
    return instance;
}
```

### 2. **Strategy Pattern**
**Where:** `PricingStrategy` interface and implementations  
**Why:** Pricing logic varies and should be swappable (hourly, flat-rate, dynamic)  
**Benefit:** Can add new pricing strategies without modifying existing code

```java
// Easy to swap pricing strategies
lot.setPricingStrategy(new HourlyPricingStrategy());
lot.setPricingStrategy(new FlatRatePricingStrategy()); // Future enhancement
```

### 3. **Factory Method Pattern**
**Where:** `ParkingTicket.issue()`  
**Why:** Centralizes ticket creation, provides meaningful method name, enables future validation/logging

```java
public static ParkingTicket issue(Vehicle vehicle, ParkingSpot spot) {
    return new ParkingTicket(vehicle, spot);
}
```

---

## ✨ Key Features

### 1. **Smart Spot Allocation**
- Tries exact match first (motorcycle → SMALL spot)
- Falls back to larger spots if exact match unavailable (motorcycle → MEDIUM spot)
- Searches floor-by-floor until spot is found

### 2. **Thread Safety**
- `synchronized` methods in `ParkingLot` prevent race conditions
- `synchronized` methods in `ParkingSpot` ensure atomic assign/remove operations
- Multiple gates can operate concurrently

### 3. **Real-time Status Tracking**
- Per-floor availability counts
- Per-spot-type availability
- Total available spots across all floors

### 4. **Flexible Pricing**
- Strategy pattern allows easy pricing model changes
- Configurable rates per vehicle type
- Minimum charge (1 hour) even for quick exits

### 5. **Comprehensive Ticket System**
- Unique ticket IDs (UUID-based)
- Tracks entry/exit times
- Records final charges
- Links vehicle to assigned spot

---

## 📁 Project Structure

```
low-level-design/
└── src/
    └── com/
        └── lld/
            └── parkinglot/
                ├── ParkingLotDemo.java          # Main driver class
                ├── enums/
                │   ├── SpotType.java            # SMALL, MEDIUM, LARGE
                │   └── VehicleType.java         # MOTORCYCLE, CAR, TRUCK
                ├── models/
                │   ├── Vehicle.java             # Vehicle entity
                │   ├── ParkingSpot.java         # Individual spot management
                │   ├── ParkingFloor.java       # Floor with spot collection
                │   ├── ParkingTicket.java       # Ticket with entry/exit info
                │   └── ParkingLot.java          # Singleton orchestrator
                ├── panels/
                │   ├── EntryPanel.java          # Entry gate interface
                │   └── ExitPanel.java           # Exit gate interface
                └── pricing/
                    ├── PricingStrategy.java      # Strategy interface
                    └── HourlyPricingStrategy.java # Hourly pricing implementation
```

---

## 🚀 How to Run

### Prerequisites
- Java JDK 8 or higher
- Terminal/Command Prompt

### Compilation

```bash
# Navigate to project root
cd /Users/rranjan/Documents/MY-REPOSITORIES/personal/low-level-design

# Compile all Java files
javac -d out src/com/lld/parkinglot/**/*.java src/com/lld/parkinglot/*.java

# Or compile individually
javac -d out src/com/lld/parkinglot/enums/*.java \
             src/com/lld/parkinglot/models/*.java \
             src/com/lld/parkinglot/panels/*.java \
             src/com/lld/parkinglot/pricing/*.java \
             src/com/lld/parkinglot/ParkingLotDemo.java
```

### Execution

```bash
# Run the demo
java -cp out com.lld.parkinglot.ParkingLotDemo
```

### Expected Output

```
=== City Center Parking Status ===
  Floor 1: SMALL=2/2  MEDIUM=3/3  LARGE=1/1  
  Floor 2: SMALL=1/1  MEDIUM=2/2  LARGE=2/2  
  Total available spots: 11

--- Vehicles Entering ---
[ENTRY-A] Issued: Ticket[E7AAB10C] MOTORCYCLE [KA-01-1234] @ Spot F1-S1 | Entry: 2026-02-27 22:57:43
[ENTRY-A] Issued: Ticket[A62EE46C] CAR [MH-02-5678] @ Spot F1-M1 | Entry: 2026-02-27 22:57:43
...

--- Vehicles Exiting ---
[EXIT-A] Exit processed: Ticket[E7AAB10C] ... | Charges: $10.00
  Motorcycle charge: $10.00
...
```

---

## 🔍 Code Walkthrough

### Example: Parking a Vehicle

```java
// 1. Driver arrives at entry gate
Vehicle car = new Vehicle("MH-02-5678", VehicleType.CAR);
EntryPanel entry1 = new EntryPanel("ENTRY-A");

// 2. Entry panel issues ticket
ParkingTicket ticket = entry1.issueTicket(car);
// Internally calls: ParkingLot.parkVehicle(car)

// 3. ParkingLot searches for spot
//    - Checks Floor 1 for MEDIUM spot (car needs MEDIUM)
//    - Finds F1-M1 available
//    - Assigns vehicle to spot
//    - Creates ticket with entry time

// 4. Ticket returned to driver
//    Ticket[A62EE46C] CAR [MH-02-5678] @ Spot F1-M1
```

### Example: Exiting and Charging

```java
// 1. Driver arrives at exit gate
ExitPanel exit1 = new ExitPanel("EXIT-A");

// 2. Exit panel processes exit
double charge = exit1.processExit(ticket);
// Internally calls: ParkingLot.unparkVehicle(ticket)

// 3. ParkingLot:
//    - Frees the spot (F1-M1 becomes available)
//    - Calculates duration: exitTime - entryTime
//    - Calls PricingStrategy.calculateCharge()
//    - HourlyPricingStrategy: hours = ceil(duration), rate = $20/hr
//    - Returns charge = hours × rate

// 4. Charge returned: $20.00
```

### Example: Fallback to Larger Spot

```java
// Scenario: All SMALL spots are full
Vehicle motorcycle = new Vehicle("KA-07-9999", VehicleType.MOTORCYCLE);

// ParkingFloor.findAvailableSpot() logic:
// 1. Check SMALL spots → all occupied
// 2. Check MEDIUM spots → canFit(SMALL)? YES (rank 2 >= 1)
// 3. Assign motorcycle to MEDIUM spot F1-M1

// Result: Motorcycle parked in MEDIUM spot (fallback)
```

---

## 💼 Interview Talking Points

### SOLID Principles

1. **Single Responsibility**: Each class has one job
   - `ParkingSpot` manages one spot's state
   - `PricingStrategy` only calculates charges
   - `EntryPanel` only handles entry requests

2. **Open/Closed**: Open for extension, closed for modification
   - New pricing strategy? Implement `PricingStrategy` interface
   - No changes needed to `ParkingLot` or `ExitPanel`

3. **Liskov Substitution**: Subtypes are substitutable
   - Any `PricingStrategy` implementation can replace `HourlyPricingStrategy`

4. **Interface Segregation**: Small, focused interfaces
   - `PricingStrategy` has only one method

5. **Dependency Inversion**: Depend on abstractions
   - `ParkingLot` depends on `PricingStrategy` interface, not concrete class

### Thread Safety

- **Coarse-grained**: `synchronized` on `parkVehicle()`/`unparkVehicle()` methods
- **Fine-grained**: `synchronized` on individual `ParkingSpot` operations
- **Double-checked locking**: Thread-safe singleton initialization

### Extensibility Examples

**Adding Electric Vehicle Spots:**
```java
// 1. Add enum value
enum SpotType { SMALL, MEDIUM, LARGE, ELECTRIC }

// 2. Add vehicle type
enum VehicleType { ..., ELECTRIC_CAR(SpotType.ELECTRIC) }

// Done! No other changes needed
```

**Adding Flat-Rate Pricing:**
```java
class FlatRatePricingStrategy implements PricingStrategy {
    public double calculateCharge(ParkingTicket ticket) {
        return 50.0; // Flat $50
    }
}

// Swap it in
lot.setPricingStrategy(new FlatRatePricingStrategy());
```

### Trade-offs Discussed

1. **Singleton vs Multiple Instances**: Chose singleton because only one parking lot exists. Could be extended to support multiple lots.

2. **Synchronization Granularity**: Used method-level synchronization for simplicity. Could use finer-grained locks per floor/spot for better concurrency.

3. **Spot Search Strategy**: Searches floor-by-floor sequentially. Could optimize with priority queues or caching.

---

## 🔮 Future Enhancements

### Potential Additions

1. **Reservation System**
   - Allow users to reserve spots in advance
   - New class: `Reservation` with time slots

2. **Payment Integration**
   - Multiple payment methods (cash, card, digital wallet)
   - Payment strategy pattern

3. **Electric Vehicle Support**
   - Charging stations as special spot type
   - Charging time tracking

4. **Valet Parking**
   - Valet assigns spots on behalf of drivers
   - Additional service charge

5. **Subscription/Membership**
   - Monthly/yearly passes
   - Discounted rates for members

6. **Real-time Dashboard**
   - Web interface showing live availability
   - Historical analytics

7. **Multi-lot Support**
   - Remove singleton constraint
   - Support multiple parking lots in system

8. **Notification System**
   - SMS/Email notifications for entry/exit
   - Reminder for long-parked vehicles

---

## 📚 Learning Outcomes

After studying this implementation, you should understand:

- ✅ How to design a complete LLD system from requirements
- ✅ When and how to apply design patterns (Singleton, Strategy, Factory)
- ✅ Thread-safe programming in Java
- ✅ SOLID principles in practice
- ✅ Class diagram relationships (association, dependency, composition)
- ✅ Separation of concerns (models, panels, pricing)
- ✅ Extensibility through interfaces and polymorphism

---

## 🤝 Contributing

This is a learning project. Feel free to:
- Add new features
- Improve documentation
- Fix bugs
- Suggest design improvements

---

## 📄 License

This project is for educational purposes and interview preparation.

---

## 👤 Author

Created as part of Low-Level Design (LLD) interview preparation.

---

**Happy Learning! 🚀**
