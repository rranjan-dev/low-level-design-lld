# Interview Tips - Elevator System LLD

Quick reference guide to deliver working code confidently in a 90-minute LLD interview.

---

## Core Strategy

**Goal**: Deliver working, simple code that you can explain clearly.

**Principles**:
1. **Keep it simple** - Two-phase model, no simulation loops
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
- Mention two-phase model: assign instantly, dispatch to move

### Phase 2: Core Implementation (40-50 min)
- Implement enums (Direction, ElevatorState)
- Implement models (Person, ElevatorRequest, Elevator, ElevatorSystem)
- Implement strategy (ElevatorSelectionStrategy, SmartElevatorStrategy)
- Get basic flow working:
  - Phase 1: request → assign → queue → "Go to E1"
  - Phase 2: dispatch → batch pickup → visit destinations → drop off

### Phase 3: Testing & Polish (15-20 min)
- Write demo showing morning rush (batch pickup)
- Handle edge cases (capacity full, maintenance mode, invalid floors)
- Add status display
- Discuss extensions

---

## Common Pitfalls to Avoid

### 1. Processing Requests One-by-One
**Don't do**: Move elevator to source, pick up one person, deliver them, come back, repeat.
**Do this**: Queue requests (Phase 1), then batch process (Phase 2) — pick up everyone at the floor, visit each destination.
```java
// Phase 1: Instant assignment
system.requestElevator(alice, 0, 10);  // → Go to E1
system.requestElevator(bob, 0, 10);    // → Go to E1 (grouped!)

// Phase 2: Batch processing
system.dispatchElevators();
// E1 picks up Alice AND Bob, drops both at floor 10
```

### 2. Over-Complicating Elevator Movement
**Don't do**: TreeSet-based up/down stop tracking, step-by-step simulation, Thread.sleep loops
**Do this**: `processPendingRequests()` that groups by source, picks up all, visits destinations

### 3. Adding Too Many Patterns
**Don't do**: State pattern, Observer pattern, Command pattern unless specifically asked
**Do this**: Singleton + Strategy is enough

### 4. Building a Simulation Engine
**Don't do**: Real-time simulation with tick loops and async processing
**Do this**: Two-phase model — assign is instant, dispatch processes the batch

### 5. Complex Threading
**Don't do**: ReentrantLock, ReadWriteLock, ConcurrentHashMap
**Do this**: Simple `synchronized` methods

---

## Key Talking Points

### When Explaining Two-Phase Model:
> "Assignment and movement are separate. When you press a floor number at the keypad, the system instantly tells you which elevator to go to — 'Go to E3'. You walk to E3 and wait. Then when the elevator is dispatched, it comes to the floor, picks up everyone assigned to it, and visits each destination in order."

### When Explaining Passenger Grouping:
> "The SmartElevatorStrategy gives cost 0 to elevators that already have pending pickups at the same floor. So if 3 people enter '10' at the ground floor keypad, they all get assigned to the same elevator — no special grouping code needed, the strategy handles it. When that elevator is full, overflow naturally goes to the next best elevator."

### When Explaining Singleton:
> "I'm using Singleton because one building has one elevator controller. All floor keypads share the same system instance."

### When Explaining Strategy Pattern:
> "I used Strategy for elevator selection so we can swap algorithms — nearest elevator, cost-based with grouping, zone-based — without changing the core system."

### When Explaining the SmartElevatorStrategy:
> "It calculates a cost for each elevator: cost 0 if it already has pending at the same floor (grouping), distance-based otherwise, with penalties for elevators going the opposite way or having passed the floor. Picks the lowest cost."

### When Asked About Concurrency:
> "requestElevator() is synchronized so two threads can't assign the same slot simultaneously. processPendingRequests() is synchronized per-elevator to prevent state corruption. In production, I'd use finer-grained locks or a message queue."

---

## Code Structure Reminder

```
1. Enums         (Direction, ElevatorState)
2. Models        (Person, ElevatorRequest, Elevator, ElevatorSystem)
3. Panel         (FloorPanel)
4. Strategy      (ElevatorSelectionStrategy interface, SmartElevatorStrategy)
5. Demo          (ElevatorSystemDemo)
```

---

## Checklist Before Starting

- [ ] Understand requirements (floors, elevators, capacity)
- [ ] Identify core entities (Elevator, Request, Person, System)
- [ ] Choose design patterns (Singleton, Strategy)
- [ ] Plan two-phase model (assign instantly, dispatch to move)
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
> "Elevator represents a single car. It has a pending request queue. In Phase 1, requests are queued via addRequest(). In Phase 2, processPendingRequests() handles the batch — moving to the source floor, picking up ALL passengers, then visiting each destination and dropping off. This models real elevator behavior where one elevator serves multiple passengers in a single trip."

---

## Common Follow-Up Questions

### "How do you handle multiple people at the same floor?"
"The SmartElevatorStrategy gives cost 0 to elevators with pending pickups at the same floor — so everyone at floor 0 gets assigned to the same elevator, up to capacity. When it's full, overflow goes to the next elevator. On dispatch, the elevator picks up everyone at once, then visits each destination."

### "How do you handle concurrent access?"
"requestElevator() is synchronized on ElevatorSystem, so pending counts are accurate. processPendingRequests() is synchronized on each Elevator. This prevents two threads from conflicting."

### "How would you change the selection algorithm?"
"Just implement a new ElevatorSelectionStrategy. For example, a LeastLoadedStrategy that picks the elevator with fewest pending requests. Plug it in with setSelectionStrategy() — no changes to ElevatorSystem."

### "What if an elevator breaks down?"
"setMaintenance(true) sets its state to MAINTENANCE. isAvailable() returns false, so the strategy skips it entirely."

### "How would you handle elevator capacity?"
"isAvailable() checks passengerCount + pendingRequests.size() < maxCapacity. So pending passengers count toward the limit even before boarding."

### "What about priority floors?"
"Add a PriorityStrategy that weighs certain floors higher. Or add an express elevator that only serves specific floor ranges."

---

## Quick Reference

### Core Methods to Remember:
- `ElevatorSystem.requestElevator(Person, int, int)` → assigns elevator, queues request (instant)
- `ElevatorSystem.dispatchElevators()` → each elevator processes its queue
- `Elevator.addRequest(ElevatorRequest)` → queues a request
- `Elevator.processPendingRequests()` → batch pickup + delivery
- `SmartElevatorStrategy.selectElevator(...)` → picks best elevator (cost 0 for grouping)

### Key Patterns:
- **Singleton**: ElevatorSystem
- **Strategy**: ElevatorSelectionStrategy

### Thread Safety:
- `synchronized` on ElevatorSystem.requestElevator()
- `synchronized` on Elevator.processPendingRequests()

---

## Simple Class Diagram (Draw This)

```
ElevatorSystem (Singleton)
  ├── Elevator (1 to many) ← has pending request queue
  └── ElevatorSelectionStrategy (dependency)

FloorPanel → ElevatorSystem (uses)

ElevatorRequest → Person + Elevator (references)
```

**Two-Phase Flow:**
```
Phase 1: FloorPanel → System.requestElevator() → Strategy → Elevator.addRequest() → "Go to E1"
Phase 2: System.dispatchElevators() → Elevator.processPendingRequests() → batch pickup → dropoff
```

---

**Remember**: Simple, working code > Complex, buggy code
