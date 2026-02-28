# Interview Tips - 90 Minute LLD Interview

Quick reference guide to avoid pitfalls and deliver working code confidently.

---

## Core Strategy

**Goal**: Deliver working, simple code that you can explain clearly in 90 minutes.

**Principles**:
1. **Keep it simple** - No unnecessary complexity
2. **Explain as you code** - Talk through your thought process
3. **Start with basics** - Get core working first, then add features
4. **Don't over-engineer** - Interviewers prefer simple, working code over complex, buggy code

---

## Time Management (90 minutes)

### Phase 1: Requirements & Design (15-20 min)
- Clarify requirements (floors, elevators, capacity, features)
- Identify core classes (Elevator, Floor, ElevatorSystem, Panels, Display, Dispatcher)
- Draw simple class diagram
- Discuss design patterns (Singleton, Strategy)

### Phase 2: Core Implementation (40-50 min)
- Implement enums (Direction, DoorState)
- Implement models (Elevator, Floor, ElevatorSystem)
- Implement panels (OutsidePanel, InsidePanel)
- Implement displays (InternalDisplay, ExternalDisplay)
- Implement dispatch (DispatchStrategy interface, NearestDispatchStrategy)
- Get basic flow working (call elevator -> board -> select floor -> arrive)

### Phase 3: Testing & Edge Cases (15-20 min)
- Test basic call-and-ride scenario
- Test capacity limits
- Test door safety
- Add status display
- Clean up code

---

## Common Pitfalls to Avoid

### 1. Over-Complicating the Elevator Movement
**Don't do**: Real threading, timers, async movement simulation
**Do this**: Simple `moveOneFloor()` and `processAllStops()` with console output
```java
public void processAllStops() {
    while (!upStops.isEmpty() || !downStops.isEmpty()) {
        moveOneFloor();
    }
    direction = Direction.IDLE;
}
```

### 2. Over-Engineering Dispatch
**Don't do**: Complex scoring with 10+ factors, ML-based optimization
**Do this**: Simple nearest-idle-first with fallback
```java
// Priority: idle nearest > same direction towards > any available
```

### 3. Complex Data Structures for Stops
**Don't do**: Priority queues, custom comparators, linked lists
**Do this**: Two `TreeSet<Integer>` — one for UP stops, one for DOWN
```java
private final TreeSet<Integer> upStops;    // ascending order
private final TreeSet<Integer> downStops;  // descending via iteration
```

### 4. Too Many Abstractions
**Don't do**: Abstract Elevator class, Panel interface hierarchy, Display factory
**Do this**: Concrete classes with simple interfaces only where needed (DispatchStrategy)

### 5. Overcomplicating Door Logic
**Don't do**: State machine with 10 transitions, timer-based auto-close
**Do this**: Simple check — door opens only when no stops remain
```java
public void openDoor() {
    if (hasRemainingStops()) {
        System.out.println("Cannot open door while moving!");
        return;
    }
    doorState = DoorState.OPEN;
}
```

---

## Key Talking Points

### When Explaining Singleton:
> "I'm using Singleton for ElevatorSystem because there's only one controller per building. All panels and displays access the same instance."

### When Explaining Strategy Pattern:
> "I used Strategy pattern for dispatch so we can easily swap the elevator selection algorithm — nearest first, least loaded, etc. — without changing the core system."

### When Explaining Door Safety:
> "The door can only open when the elevator has no pending stops. This prevents unsafe operation. In a real system, this would also check velocity sensors."

### When Explaining TreeSet for Stops:
> "I chose TreeSet for stops because it maintains sorted order — UP stops visited low-to-high, DOWN stops high-to-low — and automatically prevents duplicate floor entries."

### When Explaining Capacity:
> "Each elevator enforces two limits: 8 passengers and 680 kg. Both are checked on `addPassengers()`. This models real-world weight-based restrictions."

---

## Code Structure Reminder

```
1. Enums         (Direction, DoorState)
2. Models        (Elevator, Floor, ElevatorSystem)
3. Panels        (OutsidePanel, InsidePanel)
4. Displays      (InternalDisplay, ExternalDisplay)
5. Dispatcher    (DispatchStrategy interface, NearestDispatchStrategy)
6. Demo          (ElevatorSystemDemo)
```

---

## Checklist Before Starting

- [ ] Understand requirements (floors, elevators, capacity, features)
- [ ] Identify core entities (Elevator, Floor, System, Panel, Display)
- [ ] Identify relationships (System has Elevators, Floor has Panel + Display)
- [ ] Choose design patterns (Singleton, Strategy)
- [ ] Plan dispatch logic (nearest idle first)
- [ ] Start with simplest working version

---

## Explanation Template

### For Each Class:
1. **What it is**: "This class represents..."
2. **Key responsibility**: "It's responsible for..."
3. **Key methods**: "The main methods are..."
4. **Why this design**: "I chose this because..."

### Example:
> "Elevator is the core class representing an elevator car. It manages movement via two TreeSets of stops (up and down), door operations with safety checks, and passenger capacity. I used TreeSet so stops are processed in order — ascending for UP, descending for DOWN."

---

## Quick Fixes for Common Interview Questions

### Q: "How do you handle concurrent elevator calls?"
**A**: "ElevatorSystem.requestElevator() is the central point. In a production system, I'd make it synchronized. The dispatch strategy evaluates all elevators and picks the best one atomically."

### Q: "What if the dispatch algorithm changes?"
**A**: "I used the Strategy pattern. Just implement DispatchStrategy and call setDispatchStrategy(). No changes to ElevatorSystem or Elevator."

### Q: "How do you handle multiple stops?"
**A**: "Two TreeSets — upStops and downStops. The elevator processes all stops in the current direction, then switches. This is similar to the SCAN (elevator) disk scheduling algorithm."

### Q: "What about capacity overflow?"
**A**: "addPassengers() checks both passenger count (max 8) and weight (max 680 kg). If either limit is exceeded, the request is rejected with a clear message."

### Q: "How would you make this multi-threaded?"
**A**: "Each Elevator could run on its own Thread. Use synchronized on shared state and a BlockingQueue for requests. The dispatch strategy would need to be thread-safe."

---

## Simple Class Diagram (Draw This)

```
ElevatorSystem (Singleton)
  ├── Elevator (1 to 3)
  │     ├── InternalDisplay
  │     ├── TreeSet<upStops>
  │     └── TreeSet<downStops>
  ├── Floor (0 to 15)
  │     ├── OutsidePanel
  │     └── ExternalDisplay
  └── DispatchStrategy (dependency)
        └── NearestDispatchStrategy

InsidePanel → Elevator (wraps)
OutsidePanel → ElevatorSystem (uses)
```

---

## Success Criteria

You've succeeded if:
- Code compiles and runs
- Basic call → board → ride → arrive flow works
- Can explain each class clearly
- Can explain design patterns used
- Can handle follow-up questions (threading, scaling, new strategies)
- Code is clean and readable

---

## Sample Interview Flow

**Interviewer**: "Design an elevator system."

**You**:
1. "Let me clarify requirements..." (ask about floors, elevators, capacity)
2. "I'll start with the core entities..." (draw simple diagram)
3. "Let me implement the enums and models..." (Direction, DoorState, Elevator, Floor)
4. "Now the controller and dispatch..." (ElevatorSystem, DispatchStrategy)
5. "Panels and displays for user interaction..." (OutsidePanel, InsidePanel, displays)
6. "Let me test it..." (run demo, show output)

---

## Quick Reference

### Core Methods to Remember:
- `OutsidePanel.pressUp/Down()` - calls `ElevatorSystem.requestElevator()`
- `InsidePanel.pressFloor(floor)` - calls `ElevatorSystem.selectFloor()`
- `ElevatorSystem.dispatchAll()` - processes all pending stops
- `Elevator.addDestination(floor)` - queues a stop
- `Elevator.processAllStops()` - moves through all queued stops

### Key Patterns:
- **Singleton**: ElevatorSystem
- **Strategy**: DispatchStrategy

### Key Data Structures:
- **TreeSet**: Ordered, no-duplicate stop management
- **List**: Elevator and Floor collections

---

**Remember**: Simple, working code > Complex, buggy code

**Good luck!**
