# Design Decisions - BookMyShow

Key design decisions with rationale, alternatives considered, and trade-offs. Focuses on both OOP design and database engineering.

---

## Table of Contents

1. [Architecture Decisions](#architecture-decisions)
2. [Data Model Decisions](#data-model-decisions)
3. [Database Schema Decisions](#database-schema-decisions)
4. [Concurrency & Locking Decisions](#concurrency--locking-decisions)
5. [Pattern Choices](#pattern-choices)
6. [Trade-offs Summary](#trade-offs-summary)

---

## Architecture Decisions

### 1. Why Separate Show from Movie and Screen?

**Decision:** `Show` is a separate entity that links `Movie` to `Screen` at a specific time.

**Rationale:**
- A Movie plays in many Screens across many Theatres
- A Screen hosts many Movies across different time slots
- This is a classic M:N relationship resolved by the `Show` junction entity

**Alternative Considered:**
- Store show timing directly in Movie or Screen
- **Rejected:** Would create 1:1 coupling; a movie can't play on multiple screens simultaneously

**Impact:**
- ✅ Clean M:N resolution between Movie and Screen
- ✅ Show has its own lifecycle (scheduling, cancellation)
- ✅ ShowSeat links naturally to Show, not directly to Movie or Screen

---

### 2. Why ShowSeat is a Separate Entity (Not Just a Flag on Seat)?

**Decision:** `ShowSeat` wraps a physical `Seat` with per-show state (availability, price, lock).

**Rationale:**
- A physical Seat is static — it exists on the Screen regardless of what movie is playing
- Seat availability is per-show — the same seat is available for the 2 PM show but booked for the 5 PM show
- Price can vary per show (morning vs evening, weekday vs weekend)

**Why This is Critical for DB Design:**
```
seat = physical infrastructure (200 rows per screen, static)
show_seat = per-show availability (200 rows × shows_per_day, dynamic)
```

**Alternative Considered:**
- Track booked seats as a JSON array on the Show entity
- **Rejected:** Can't index into JSON for seat-level locking, can't do `SELECT ... FOR UPDATE` on individual seats

**Impact:**
- ✅ Row-level locking on individual seats
- ✅ Price stored per show-seat (dynamic pricing)
- ✅ Clean DB normalization (3NF)
- ❌ Highest volume table (14.6B rows/year) — requires partitioning

---

### 3. Why Facade Pattern for BookMyShowService?

**Decision:** `BookMyShowService` acts as a Facade over MovieService, TheatreService, ShowService, and BookingService.

**Rationale:**
- External callers (demo, API layer) don't need to know about internal service decomposition
- Simplifies the API: one entry point instead of four services
- Encapsulates orchestration logic (e.g., `addShow()` calls `initializeSeats()` internally)

**Alternative Considered:**
- Expose all services directly
- **Rejected:** Increases coupling; callers would need to coordinate between services

**Impact:**
- ✅ Single entry point for all operations
- ✅ Internal refactoring doesn't affect callers
- ✅ Aligns with Singleton pattern (one global service instance)

---

## Data Model Decisions

### 4. Why Enum with Behavior for SeatType?

**Decision:** `SeatType` enum carries `basePrice` as a field with `getBasePrice()` method.

**Rationale:**
- Base price is intrinsic to seat type — REGULAR is always cheaper than VIP
- Avoids a separate lookup table or hardcoded values in pricing logic
- Common Java interview pattern (enum with behavior)

**Code:**
```java
public enum SeatType {
    REGULAR(150.0),
    PREMIUM(250.0),
    VIP(400.0),
    RECLINER(500.0);

    private final double basePrice;
    SeatType(double basePrice) { this.basePrice = basePrice; }
    public double getBasePrice() { return basePrice; }
}
```

**Alternative:** Separate pricing table in DB
- **For production:** Yes, prices should be in DB for dynamic updates
- **For interview:** Enum with behavior is simpler and demonstrates Java skill

---

### 5. Why Sequential Booking IDs (Not UUID)?

**Decision:** Booking IDs use counter (BKG-1, BKG-2, ...) instead of UUID.

**Rationale:**
- Sequential IDs are easier to debug and read in interview demos
- DB: `AUTO_INCREMENT` is faster than UUID for InnoDB (sequential inserts = less page splitting)
- UUID adds 36 chars vs 8 bytes for BIGINT

**Trade-off:**
- ✅ Counter: Simple, DB-friendly, sortable
- ❌ Counter: Predictable (security concern for production)
- ✅ UUID: Globally unique, unpredictable
- ❌ UUID: Larger, random inserts = worse InnoDB performance

**Interview Point:** "In production, I'd use UUID or ULID for external-facing IDs but keep BIGINT AUTO_INCREMENT as the primary key for DB performance."

---

### 6. Why Payment is Separate from Booking?

**Decision:** `Payment` is a separate entity linked to `Booking`, not embedded in it.

**Rationale:**
- **Single Responsibility**: Booking manages seat/show state; Payment manages money flow
- **Multiple attempts**: User might retry payment (1 booking, N payments)
- **Different lifecycle**: Booking can be CANCELLED while payment is REFUNDED
- **Regulatory requirements**: Payment records must be retained longer than booking records

**DB Impact:**
- `payment` table indexed on `(booking_id)` for lookup
- Indexed on `(payment_status, created_at)` for reconciliation batch jobs
- Separate archival policy (3 years for payments vs 2 years for bookings)

---

## Database Schema Decisions

### 7. Why Range Partitioning by Date?

**Decision:** `show`, `show_seat`, and `booking` tables are range-partitioned by date.

**Rationale:**
- Most queries filter by date ("shows today", "bookings this week")
- Partition pruning eliminates irrelevant partitions from query execution
- Archival is instant: `DROP PARTITION` is O(1) vs `DELETE` which is O(n)

**Monthly Partitions for `show`:**
```sql
PARTITION BY RANGE (TO_DAYS(show_date)) (
    PARTITION p_2026_03 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p_2026_04 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p_future  VALUES LESS THAN MAXVALUE
);
```

**Alternative: Hash partitioning by show_id**
- **Rejected:** Doesn't help with date-range queries or date-based archival

**Alternative: No partitioning**
- **Rejected:** `show_seat` grows 14.6B rows/year. Full table scan = disaster

---

### 8. Why Composite Index (show_id, is_booked) on show_seat?

**Decision:** Primary read index on `show_seat` is `(show_id, is_booked)`.

**Rationale:**
- The #1 query on this table is: "give me available seats for show X"
- `WHERE show_id = ? AND is_booked = FALSE`
- Composite index covers both predicates — index-only scan, no table lookup needed

**Why not just index on (show_id)?**
- That would find all seats for a show (200 rows), then filter by `is_booked`
- With composite index, MySQL directly navigates to available seats only
- For a sold-out show: composite index returns 0 rows instantly; single-column scans 200

**Index Size Estimation:**
```
show_seat: 14.6B rows/year
Index entry: ~20 bytes (show_id 8B + is_booked 1B + ptr 6B + overhead)
Index size: ~292 GB/year for this one index
```
This is why we can't add too many indexes — each costs hundreds of GB.

---

### 9. Why Denormalize show_date into show_seat?

**Decision:** Add `show_date` as a denormalized column in `show_seat` for partition key.

**Rationale:**
- MySQL requires the partition key to be part of the table or in a unique index
- `show_seat` naturally references `show` via FK, but show_date lives in `show` table
- Denormalizing show_date into show_seat enables date-based partitioning directly

**Trade-off:**
- ✅ Enables date-based partition pruning on the 14.6B-row table
- ✅ Avoids JOIN to `show` table for partition pruning
- ❌ 4 extra bytes per row (DATE type)
- ❌ Must keep in sync with `show.show_date` (application-level)

**Impact:** 4 bytes × 14.6B rows = ~58 GB extra/year. Worth it for partition pruning benefit.

---

### 10. Why Pessimistic Over Optimistic Locking?

**Decision:** Use `SELECT ... FOR UPDATE` (pessimistic locking) for seat booking.

**Rationale:**
- Booking is a payment-critical flow — user expects that locked seats are guaranteed
- Optimistic locking means user sees "available", fills payment, and might fail → poor UX
- Lock duration is bounded (5-minute payment window with background cleanup)
- Row-level locks (not table-level) — only locks the specific seats being booked

**Alternative: Optimistic locking with version column**
```sql
UPDATE show_seat SET is_booked = TRUE, version = version + 1
WHERE id = ? AND is_booked = FALSE AND version = ?;
-- If affected_rows = 0 → retry
```
- **When to use:** Extremely high contention (10K concurrent bookings for same show)
- **Rejected for default:** Poor UX for payment flows

**Interview Answer:**
> "Pessimistic locking guarantees consistency at the cost of held locks. For a payment flow where double-booking means real money loss, consistency wins. The lock window is short (5 min) and bounded. For flash-sale scenarios, I'd add a virtual queue before the locking step."

---

## Concurrency & Locking Decisions

### 11. Why Synchronized at Service Level, Not Model Level?

**Decision:** `BookingService.createBooking()` is `synchronized`, but individual model classes are also protected.

**Rationale:**
- **Service level**: Ensures the entire booking transaction (lock seats → create booking) is atomic
- **Model level**: `ShowSeat.lockSeat()` is also `synchronized` for defense-in-depth
- Two levels of protection: coarse (service) + fine (model)

**In DB Terms:**
```
Service-level sync → BEGIN TRANSACTION ... COMMIT (transaction boundary)
Model-level sync → SELECT ... FOR UPDATE (row-level lock)
```

**Alternative: Only model-level sync**
- **Rejected:** Multi-seat booking needs all-or-nothing atomicity across multiple ShowSeat objects

---

### 12. Why Atomic Rollback on Partial Failure?

**Decision:** If seat 3 of 5 fails to lock, seats 1 and 2 are rolled back.

**Rationale:**
- Partial booking is worse than no booking (user gets 2 of 5 seats)
- All-or-nothing semantics match DB transaction behavior (ROLLBACK on failure)
- Released seats become available for other users immediately

**Code:**
```java
try {
    for (ShowSeat seat : requestedSeats) {
        if (!seat.lockSeat()) {
            rollbackLockedSeats(lockedSeats);  // Release all previously locked
            throw new SeatNotAvailableException("...");
        }
        lockedSeats.add(seat);
    }
} catch (SeatNotAvailableException e) {
    throw e;  // Already rolled back
}
```

---

## Pattern Choices

### 13. Why Not Observer Pattern for Seat Updates?

**Decision:** Seat availability is queried on-demand, not pushed via Observer.

**Rationale:**
- LLD interview scope: Observer adds complexity without core benefit
- Real-time seat maps use WebSocket in production, not Observer in domain model
- Pull model (query when page loads/refreshes) is simpler

**When to Add:**
- If interviewer asks about real-time seat map updates
- Answer: "I'd use WebSocket + Redis pub/sub for real-time push, not in-domain Observer"

---

### 14. Why Not Factory Pattern for Booking/Payment Creation?

**Decision:** Direct constructors for `Booking` and `Payment`.

**Rationale:**
- Simple constructors are easier to write and explain in interviews
- Current requirements don't need complex creation logic
- Factory adds a layer with no benefit for the current scope

**When to Add:**
- If booking creation needs validation, notification, or complex initialization
- For production: use a Builder or Factory for Payment (different payment methods have different fields)

---

## Trade-offs Summary

| # | Decision | Chosen | Alternative | Rationale |
|---|----------|--------|-------------|-----------|
| 1 | Show as separate entity | Yes | Embed in Movie/Screen | M:N resolution |
| 2 | ShowSeat separate from Seat | Yes | JSON array on Show | Row-level locking, indexing |
| 3 | Facade for BookMyShowService | Yes | Expose all services | Single entry point |
| 4 | Enum with behavior | Yes | Separate pricing table | Interview simplicity |
| 5 | Sequential IDs | Yes (interview) | UUID (production) | DB performance |
| 6 | Separate Payment entity | Yes | Embed in Booking | SRP, retry support |
| 7 | Range partitioning by date | Yes | Hash, no partition | Date queries, archival |
| 8 | Composite index (show_id, is_booked) | Yes | Single column | Covers #1 query |
| 9 | Denormalize show_date | Yes | JOIN for partitioning | Partition pruning benefit |
| 10 | Pessimistic locking | Yes | Optimistic | Payment consistency |
| 11 | Service + Model level sync | Both | Only one level | Defense-in-depth |
| 12 | Atomic rollback | Yes | Partial booking | All-or-nothing UX |
| 13 | No Observer | Skipped | Add Observer | Keep simple for interview |
| 14 | Direct constructors | Yes | Factory/Builder | Simplicity |

---

## Design Principles Applied

1. **SOLID**: Each service has single responsibility; Strategy for pricing; depend on PricingStrategy interface
2. **KISS**: Minimal patterns (Singleton, Strategy, Facade) — only what's needed
3. **YAGNI**: No Observer, no Command, no Abstract Factory until needed
4. **DRY**: Pricing logic in one Strategy; locking logic in BookingService
5. **Separation of Concerns**: Physical infra (Theatre/Screen/Seat) vs Scheduling (Show/ShowSeat) vs Transactional (Booking/Payment)
6. **Database-First Thinking**: Every model class maps to a table; schema drives the design

---

**These decisions prioritize: DB Engineering Depth > Code Elegance > Pattern Showcase**
