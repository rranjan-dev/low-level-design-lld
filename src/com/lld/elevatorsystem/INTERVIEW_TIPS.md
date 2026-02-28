# Interview Tips - Elevator System LLD

Quick reference guide to deliver working code confidently in a 90-minute LLD interview.

---

## Core Strategy

**Goal**: Deliver working, simple code that you can explain clearly.

**Principles**:
1. **Keep it simple** - Synchronous processing, no simulation loops
2. **Explain as you code** - Talk through your thought process
3. **Start with basics** - Get core working first, then mention extensions
4. **Don't over-engineer** - Simple working code beats complex buggy code

---

## Time Management (90 minutes)

### Phase 1: Requirements & Design (15-20 min)
- Clarify: How many elevators? How many floors?
- Clarify: Capacity limits? Maintenance mode?
- Identify core classes: ElevatorSystem, Elevator, ElevatorRequest, Person
- Draw simple class diagram
- Mention patterns: Singleton, Strategy

### Phase 2: Core Implementation (40-50 min)
- Implement enums (Direction, ElevatorState)
- Implement models (Person, ElevatorRequest, Elevator, ElevatorSystem)
- Implement strategy (ElevatorSelectionStrategy, NearestElevatorStrategy)
- Get basic flow working (request → assign → move → pickup → move → dropoff)

### Phase 3: Testing & Polish (15-20 min)
- Write demo showing multiple elevators handling requests
- Handle edge cases (capacity full, maintenance mode, invalid floors)
- Add status display
- Discuss extensions

---

## Common Pitfalls to Avoid

### 1. Over-Complicating Elevator Movement
**Don't do**: TreeSet-based up/down stop tracking, step-by-step simulation, Thread.sleep loops
**Do this**: `processRequest()` that moves directly to source then destination
```java
public void processRequest(ElevatorRequest request) {
    moveTo(request.getSourceFloor());       // Go to pickup
    moveTo(request.getDestinationFloor());  // Go to destination
}
```

### 2. Adding Too Many Patterns
**Don't do**: State pattern, Observer pattern, Command pattern unless specifically asked
**Do this**: Singleton + Strategy is enough

### 3. Building a Simulation Engine
**Don't do**: Real-time simulation with tick loops and async processing
**Do this**: Synchronous request handling — each call completes the full ride

### 4. Over-Engineering the Floor
**Don't do**: Full Floor class with request queues, complex button state
**Do this**: Floors are just integers. FloorPanel is a destination dispatch keypad — thin delegation layer.

### 5. Complex Threading
**Don't do**: ReentrantLock, ReadWriteLock, ConcurrentHashMap
**Do this**: Simple `synchronized` methods

---

## Key Talking Points

### When Explaining Singleton:
> "I'm using Singleton because one building has one elevator controller. All floor keypads share the same system instance."

### When Explaining Strategy Pattern:
> "I used Strategy for elevator selection so we can swap algorithms — nearest elevator, least loaded, zone-based — without changing the core system."

### When Explaining the Selection Algorithm:
> "NearestElevatorStrategy first looks for elevators going in the same direction that haven't passed the floor yet. If none found, it falls back to any available elevator, picking the nearest."

### When Explaining Synchronous Processing:
> "Each request is processed synchronously for simplicity. In production, you'd use an event-driven approach with queues, but this keeps the design clear for discussion."

### When Asked About Concurrency:
> "requestElevator and processRequest are synchronized. In production, I'd use finer-grained locks — perhaps per-elevator locks or a message queue for request dispatching."

---

## Code Structure Reminder

```
1. Enums         (Direction, ElevatorState)
2. Models        (Person, ElevatorRequest, Elevator, ElevatorSystem)
3. Panel         (FloorPanel)
4. Strategy      (ElevatorSelectionStrategy interface, NearestElevatorStrategy)
5. Demo          (ElevatorSystemDemo)
```

---

## Checklist Before Starting

- [ ] Understand requirements (floors, elevators, capacity)
- [ ] Identify core entities (Elevator, Request, Person, System)
- [ ] Choose design patterns (Singleton, Strategy)
- [ ] Plan thread safety approach (synchronized methods)
- [ ] Start with simplest working version

---

## Explanation Template

### For Each Class:
1. **What it is**: "This class represents..."
2. **Key responsibility**: "It's responsible for..."
3. **Key methods**: "The main methods are..."
4. **Why this design**: "I chose this because..."

### Example:
> "Elevator represents a single car. Its key method is processRequest() which handles the full ride — moving to the source floor, picking up, moving to destination, and dropping off. I encapsulated movement inside the elevator because it's the elevator's responsibility, not the system's."

---

## Common Follow-Up Questions

### "How do you handle concurrent access?"
"requestElevator() is synchronized on ElevatorSystem, and processRequest() is synchronized on each Elevator. This prevents two threads from selecting the same elevator or modifying its state simultaneously."

### "How would you change the selection algorithm?"
"Just implement a new ElevatorSelectionStrategy. For example, a LeastLoadedStrategy that picks the elevator with fewest current passengers. Plug it in with setSelectionStrategy() — no changes to ElevatorSystem."

### "What if an elevator breaks down?"
"setMaintenance(true) sets its state to MAINTENANCE. canServe() and isAvailable() both return false, so the strategy skips it entirely."

### "How would you handle elevator capacity?"
"Each elevator has maxCapacity. canServe() returns false when full. In the real world, you'd add weight sensors too."

### "What about priority floors?"
"Add a PriorityStrategy that weighs certain floors higher. Or add an express elevator that only serves specific floor ranges — just another Elevator instance with canServe() logic adjusted."

---

## Quick Reference

### Core Methods to Remember:
- `ElevatorSystem.requestElevator(Person, int, int)` → returns ElevatorRequest
- `Elevator.processRequest(ElevatorRequest)` → moves + picks up + drops off
- `Elevator.canServe(int, Direction)` → can handle this request?
- `NearestElevatorStrategy.selectElevator(...)` → picks best elevator

### Key Patterns:
- **Singleton**: ElevatorSystem
- **Strategy**: ElevatorSelectionStrategy

### Thread Safety:
- `synchronized` on ElevatorSystem.requestElevator()
- `synchronized` on Elevator.processRequest()

---

## Simple Class Diagram (Draw This)

```
ElevatorSystem (Singleton)
  ├── Elevator (1 to many)
  └── ElevatorSelectionStrategy (dependency)

FloorPanel → ElevatorSystem (uses)

ElevatorRequest → Person + Elevator (references)
```

---

**Remember**: Simple, working code > Complex, buggy code
