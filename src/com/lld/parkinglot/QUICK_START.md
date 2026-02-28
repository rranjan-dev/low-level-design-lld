# Quick Start Guide - Parking Lot System

A quick reference guide for understanding and using the Parking Lot System.

---

## 🚀 Quick Compile & Run

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

## 🐛 Troubleshooting

### "No available spot"
- Check if spots exist for that vehicle type
- Verify spots are not all occupied
- Check if larger spots are available (fallback)

### "Pricing strategy not configured"
- Call `lot.setPricingStrategy(new HourlyPricingStrategy())` before parking

### "Spot already occupied"
- Race condition: two threads tried to assign same spot
- Should not happen with synchronized methods

---

## 📚 Next Steps

1. Read [README.md](README.md) for full documentation
2. Check [CLASS_DIAGRAM.md](CLASS_DIAGRAM.md) for class relationships
3. Explore the code in `src/com/lld/parkinglot/`

---

**Happy Coding! 🚀**
