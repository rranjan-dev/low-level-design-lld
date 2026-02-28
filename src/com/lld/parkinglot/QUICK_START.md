# Quick Start Guide - Parking Lot System

---

## Problem Statement

> Design a Parking Lot Management System that can manage multiple floors, each with different types of parking spots. The system should handle vehicle entry/exit, automatic spot assignment, and flexible pricing.

### Functional Requirements

1. The parking lot has **multiple floors**, each with **multiple spots**
2. Three spot types: **SMALL** (motorcycle), **MEDIUM** (car), **LARGE** (truck)
3. A vehicle can park in its matching spot size **or any larger spot** (fallback)
4. System issues a **ticket on entry** with entry time, spot, and vehicle details
5. System calculates **charges on exit** based on duration and vehicle type
6. **Multiple entry/exit gates** can operate simultaneously
7. Real-time **availability tracking** per floor and per spot type

### Non-Functional Requirements

1. **Thread-safe** — multiple gates operating concurrently without race conditions
2. **Extensible** — easy to add new vehicle types, spot types, or pricing models
3. **Simple** — easy to understand, remember, and code in an interview

---

## Quick Compile & Run

```bash
# Compile
javac -d out src/com/lld/parkinglot/**/*.java src/com/lld/parkinglot/*.java

# Run
java -cp out com.lld.parkinglot.ParkingLotDemo
```

---

## 📦 Key Classes at a Glance

| Class | Purpose | Key Method |
|-------|---------|------------|
| `ParkingLot` | Singleton orchestrator | `parkVehicle()`, `unparkVehicle()` |
| `ParkingFloor` | Manages spots on a floor | `findAvailableSpot()` |
| `ParkingSpot` | Individual parking space | `assignVehicle()`, `removeVehicle()` |
| `ParkingTicket` | Entry/exit receipt | Constructor (direct) |
| `EntryPanel` | Entry gate | `issueTicket()` |
| `ExitPanel` | Exit gate | `processExit()` |
| `PricingStrategy` | Pricing interface | `calculateCharge()` |

---

## 🔑 Key Concepts

### Spot Types
- **SMALL** → Motorcycles
- **MEDIUM** → Cars
- **LARGE** → Trucks/Buses

### Vehicle Types
- **MOTORCYCLE** → Needs SMALL (or larger)
- **CAR** → Needs MEDIUM (or larger)
- **TRUCK** → Needs LARGE

### Fallback Logic
If exact spot type unavailable, vehicle can use a larger spot:
- Motorcycle can use MEDIUM or LARGE if SMALL is full
- Car can use LARGE if MEDIUM is full

---

## 💻 Code Snippets

### Basic Usage

```java
// 1. Initialize parking lot
ParkingLot lot = ParkingLot.getInstance("My Parking Lot");
lot.setPricingStrategy(new HourlyPricingStrategy());

// 2. Add floors and spots
ParkingFloor floor1 = new ParkingFloor(1);
floor1.addSpot(new ParkingSpot("F1-S1", SpotType.SMALL));
floor1.addSpot(new ParkingSpot("F1-M1", SpotType.MEDIUM));
lot.addFloor(floor1);

// 3. Create entry/exit panels
EntryPanel entry = new EntryPanel("ENTRY-1");
ExitPanel exit = new ExitPanel("EXIT-1");

// 4. Park a vehicle
Vehicle car = new Vehicle("ABC-1234", VehicleType.CAR);
ParkingTicket ticket = entry.issueTicket(car);

// 5. Exit and pay
double charge = exit.processExit(ticket);
System.out.println("Charge: $" + charge);

// 6. Check status
System.out.println(lot.getStatusDisplay());
```

---

## 🎯 Design Patterns Used

1. **Singleton** → `ParkingLot` (one instance)
2. **Strategy** → `PricingStrategy` (swappable pricing)

---

## 🧪 Testing Scenarios

### Test Case 1: Basic Parking
```java
Vehicle car = new Vehicle("CAR-001", VehicleType.CAR);
ParkingTicket ticket = entry.issueTicket(car);
// Expected: Car parked in MEDIUM spot
```

### Test Case 2: Fallback to Larger Spot
```java
// Fill all SMALL spots first
entry.issueTicket(new Vehicle("BIKE-1", VehicleType.MOTORCYCLE));
entry.issueTicket(new Vehicle("BIKE-2", VehicleType.MOTORCYCLE));
entry.issueTicket(new Vehicle("BIKE-3", VehicleType.MOTORCYCLE));
// Expected: Third bike gets MEDIUM spot (fallback)
```

### Test Case 3: Pricing Calculation
```java
ParkingTicket ticket = entry.issueTicket(car);
Thread.sleep(2000); // Wait 2 seconds
double charge = exit.processExit(ticket);
// Expected: $20.00 (1 hour minimum charge for car)
```

---

## 📊 Status Display Format

```
=== City Center Parking Status ===
  Floor 1: SMALL=2/2  MEDIUM=1/3  LARGE=0/1
  Floor 2: SMALL=1/1  MEDIUM=2/2  LARGE=2/2
  Total available: 7
```

Format: `TYPE=available/total`

---

## 🔍 Common Operations

### Find Available Spot
```java
ParkingSpot spot = floor.findAvailableSpot(VehicleType.CAR);
// Returns first available MEDIUM or LARGE spot
```

### Check Availability
```java
int availableSmall = floor.getAvailableCount(SpotType.SMALL);
int totalAvailable = floor.getTotalAvailableCount();
```

### Get Ticket Info
```java
String ticketId = ticket.getTicketId();  // e.g., "TKT-1"
Vehicle vehicle = ticket.getVehicle();
ParkingSpot spot = ticket.getSpot();
LocalDateTime entryTime = ticket.getEntryTime();
double charges = ticket.getCharges(); // After exit
```

---

## ⚠️ Important Notes

1. **Thread Safety**: All parking operations are synchronized
2. **Singleton**: Only one ParkingLot instance exists
3. **Minimum Charge**: Even quick exits charge for 1 hour minimum
4. **Spot Assignment**: Searches floor-by-floor, exact match first
5. **Ticket Required**: Must have ticket to exit
6. **Exit Flow**: markExitTime() → removeVehicle() → calculateCharge() → setCharges()

---

## Core Flow (Remember This)

```
ENTRY:  Vehicle → EntryPanel.issueTicket() → ParkingLot.parkVehicle()
            → ParkingFloor.findAvailableSpot() → ParkingSpot.assignVehicle()
            → ParkingTicket created and returned

EXIT:   Ticket → ExitPanel.processExit() → ParkingLot.unparkVehicle()
            → ticket.markExitTime() → spot.removeVehicle()
            → PricingStrategy.calculateCharge() → charge returned
```

---

## Interview-Ready Features

### 1. Smart Spot Allocation with Fallback
Tries exact match first, then falls back to larger spots. Shows you think about real-world constraints.
```
MOTORCYCLE → try SMALL → try MEDIUM → try LARGE
CAR        → try MEDIUM → try LARGE
TRUCK      → try LARGE only
```

### 2. Enum with Behavior
`VehicleType` has a `getRequiredSpotType()` method — a common interview pattern.
```java
public SpotType getRequiredSpotType() {
    if (this == MOTORCYCLE) return SpotType.SMALL;
    if (this == CAR) return SpotType.MEDIUM;
    return SpotType.LARGE;
}
```

### 3. Thread Safety at Two Levels
- **Coarse-grained**: `synchronized` on `ParkingLot.parkVehicle()` / `unparkVehicle()`
- **Fine-grained**: `synchronized` on `ParkingSpot.assignVehicle()` / `removeVehicle()`

### 4. Strategy Pattern for Pricing
Swap pricing without touching core logic. Easy to extend:
```java
lot.setPricingStrategy(new HourlyPricingStrategy());
// Later: lot.setPricingStrategy(new FlatRatePricingStrategy());
```

### 5. Boundary Separation (Panels)
EntryPanel/ExitPanel are thin wrappers — keep I/O separate from business logic. Shows clean architecture thinking.

### 6. Status Display
Real-time availability per floor per type. Shows you think about observability.
```
Floor 1: SMALL=2/2  MEDIUM=1/3  LARGE=0/1
```

---

## Possible Extensions (Mention If Asked)

- **Electric vehicle spots** → new SpotType + VehicleType
- **Handicap/reserved spots** → priority flag on ParkingSpot
- **Surge/weekend pricing** → new PricingStrategy implementation
- **Payment system** → separate Payment class with multiple methods
- **Sensor integration** → ParkingSpot gets `updateStatus()` from hardware
- **Multiple parking lots** → remove Singleton, use registry pattern
