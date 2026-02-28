# Interview Tips - 90 Minute LLD Interview

Quick reference guide to avoid pitfalls and deliver working code confidently.

---

## 🎯 Core Strategy

**Goal**: Deliver working, simple code that you can explain clearly in 90 minutes.

**Principles**:
1. **Keep it simple** - No unnecessary complexity
2. **Explain as you code** - Talk through your thought process
3. **Start with basics** - Get core working first, then add features
4. **Don't over-engineer** - Interviewers prefer simple, working code over complex, buggy code

---

## ⏱️ Time Management (90 minutes)

### Phase 1: Requirements & Design (15-20 min)
- ✅ Clarify requirements
- ✅ Identify core classes
- ✅ Draw simple class diagram
- ✅ Discuss design patterns

### Phase 2: Core Implementation (40-50 min)
- ✅ Implement enums (VehicleType, SpotType)
- ✅ Implement models (Vehicle, ParkingSpot, ParkingFloor, ParkingTicket, ParkingLot)
- ✅ Implement panels (EntryPanel, ExitPanel)
- ✅ Implement pricing (PricingStrategy interface, HourlyPricingStrategy)
- ✅ Get basic flow working (park → unpark)

### Phase 3: Testing & Edge Cases (15-20 min)
- ✅ Test basic scenarios
- ✅ Handle edge cases (no spots available, null checks)
- ✅ Add status display
- ✅ Clean up code

---

## 🚨 Common Pitfalls to Avoid

### 1. **Over-Complicating Singleton**
❌ **Don't do**: Double-checked locking, volatile, complex initialization
✅ **Do this**: Simple synchronized method
```java
public static synchronized ParkingLot getInstance(String name) {
    if (instance == null) {
        instance = new ParkingLot(name);
    }
    return instance;
}
```

### 2. **Over-Engineering Patterns**
❌ **Don't do**: Factory Method, Builder, Observer, Command patterns unless asked
✅ **Do this**: Use only Singleton and Strategy (if pricing varies)

### 3. **Complex Data Structures**
❌ **Don't do**: EnumMap, PriorityQueue, Custom Collections
✅ **Do this**: HashMap, ArrayList, simple Lists

### 4. **Too Many Abstractions**
❌ **Don't do**: Abstract classes, multiple interfaces, complex inheritance
✅ **Do this**: Simple interfaces only where needed (PricingStrategy)

### 5. **Complex Threading**
❌ **Don't do**: ReentrantLock, ReadWriteLock, ConcurrentHashMap
✅ **Do this**: Simple `synchronized` methods

---

## 💡 Key Talking Points

### When Explaining Singleton:
> "I'm using Singleton pattern because there's only one parking lot instance. I kept it simple with synchronized method for thread safety."

### When Explaining Strategy Pattern:
> "I used Strategy pattern for pricing so we can easily swap pricing models without changing the core parking logic."

### When Explaining Thread Safety:
> "I synchronized the parkVehicle and unparkVehicle methods to prevent race conditions when multiple gates operate simultaneously. I also synchronized assignVehicle and removeVehicle at the spot level for additional safety."

### When Explaining Spot Allocation:
> "The system tries exact match first (motorcycle → SMALL spot). If not available, it falls back to larger spots (motorcycle → MEDIUM or LARGE)."

---

## 📝 Code Structure Reminder

```
1. Enums (VehicleType, SpotType)
2. Models (Vehicle, ParkingSpot, ParkingFloor, ParkingTicket, ParkingLot)
3. Panels (EntryPanel, ExitPanel)
4. Pricing (PricingStrategy interface, HourlyPricingStrategy)
5. Demo (ParkingLotDemo)
```

---

## ✅ Checklist Before Starting

- [ ] Understand requirements clearly
- [ ] Identify core entities (Vehicle, Spot, Floor, Lot, Ticket)
- [ ] Identify relationships (Lot has Floors, Floor has Spots, etc.)
- [ ] Choose design patterns (Singleton, Strategy)
- [ ] Plan thread safety approach (synchronized methods)
- [ ] Start with simplest working version

---

## 🎤 Explanation Template

### For Each Class:
1. **What it is**: "This class represents..."
2. **Key responsibility**: "It's responsible for..."
3. **Key methods**: "The main methods are..."
4. **Why this design**: "I chose this because..."

### Example:
> "ParkingLot is a Singleton that orchestrates all parking operations. It has methods like parkVehicle() and unparkVehicle(). I made it Singleton because there's only one parking lot instance. I synchronized these methods for thread safety."

---

## 🔧 Quick Fixes for Common Issues

### Issue: "How do you handle concurrent access?"
**Answer**: "I use synchronized methods on ParkingLot for coarse-grained locking, and synchronized methods on ParkingSpot for fine-grained safety."

### Issue: "What if pricing changes?"
**Answer**: "I used Strategy pattern, so we can swap pricing strategies without changing ParkingLot code."

### Issue: "How do you find available spots?"
**Answer**: "I search floor by floor, trying exact match first, then falling back to larger spots if needed."

### Issue: "What about edge cases?"
**Answer**: "I check for null spots, validate spot availability in assignVehicle(), and throw exceptions for invalid states."

---

## 📊 Simple Class Diagram (Draw This)

```
ParkingLot (Singleton)
  ├── ParkingFloor (1 to many)
  │     └── ParkingSpot (1 to many)
  ├── PricingStrategy (dependency)
  └── ParkingTicket (creates)

EntryPanel → ParkingLot (uses)
ExitPanel → ParkingLot (uses)
```

---

## 🎯 Success Criteria

You've succeeded if:
- ✅ Code compiles and runs
- ✅ Basic park/unpark works
- ✅ Can explain each class clearly
- ✅ Can explain design patterns used
- ✅ Can handle follow-up questions
- ✅ Code is clean and readable

---

## 💬 Sample Interview Flow

**Interviewer**: "Design a parking lot system."

**You**: 
1. "Let me clarify requirements..." (ask about floors, spot types, pricing)
2. "I'll start with the core entities..." (draw simple diagram)
3. "Let me implement the basic structure..." (code enums, models)
4. "Now I'll add the parking logic..." (implement park/unpark)
5. "Let me test it..." (run demo, show output)

---

## 🚀 Quick Reference

### Core Methods to Remember:
- `ParkingLot.parkVehicle(Vehicle)` → returns ParkingTicket
- `ParkingLot.unparkVehicle(ParkingTicket)` → returns double (charges)
- `ParkingFloor.findAvailableSpot(VehicleType)` → returns ParkingSpot
- `ParkingSpot.assignVehicle(Vehicle)` → void
- `ParkingSpot.removeVehicle()` → returns Vehicle

### Key Patterns:
- **Singleton**: ParkingLot
- **Strategy**: PricingStrategy

### Thread Safety:
- `synchronized` on ParkingLot methods
- `synchronized` on ParkingSpot methods

---

**Remember**: Simple, working code > Complex, buggy code

**Good luck! 🍀**
