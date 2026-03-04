# Interview Tips - BookMyShow (90 Minute LLD + DB Design)

Quick reference guide for acing interviews that test both LLD and database engineering depth.

---

## 🎯 Core Strategy

**Goal**: Deliver working code + demonstrate deep understanding of DB schema design, capacity planning, indexing, and query tuning.

**Your Differentiator**: Most candidates stop at class diagrams. You go deep into:
- How many rows each table has at scale
- Which indexes power which queries
- Locking strategy for preventing double-booking
- Partitioning strategy for billion-row tables
- Data archival to keep active dataset manageable

---

## ⏱️ Time Management (90 minutes)

### Phase 1: Requirements & High-Level Design (10-15 min)
- ✅ Clarify: browsing flow, booking, cancellation, pricing
- ✅ Draw entity relationship: City → Theatre → Screen → Seat → Show → ShowSeat → Booking
- ✅ Identify critical path: seat selection → locking → payment → confirmation
- ✅ Mention design patterns: Singleton, Strategy, Facade

### Phase 2: Database Schema Design (20-25 min) ⭐
- ✅ Draw 9 tables with columns, types, constraints
- ✅ Identify high-volume tables (`show_seat`, `show`, `booking`)
- ✅ Capacity analysis: rows/day, rows/year, data size
- ✅ Index strategy for top 3 queries
- ✅ Partitioning strategy for `show_seat` and `show`
- ✅ Locking strategy: `SELECT ... FOR UPDATE` flow

### Phase 3: Code Implementation (30-35 min)
- ✅ Enums (SeatType, BookingStatus, PaymentStatus)
- ✅ Models (Movie, Theatre, Screen, Seat, Show, ShowSeat, Booking, Payment)
- ✅ BookingService with synchronized locking + atomic rollback
- ✅ Get booking flow working

### Phase 4: Testing & Discussion (10-15 min)
- ✅ Demo basic flow: browse → book → pay → cancel
- ✅ Demo double-booking prevention
- ✅ Discuss trade-offs and extensions

---

## 🚨 Common Pitfalls to Avoid

### 1. Skipping Database Design
❌ **Don't do**: Jump straight to Java classes, ignore DB entirely
✅ **Do this**: "Let me start with the schema design since the interviewer mentioned DB focus..."

### 2. Missing `show_seat` as a Separate Entity
❌ **Don't do**: Store seat availability directly on `seat` table
✅ **Do this**: `show_seat` is the per-show availability wrapper — one row per seat per show
```
seat = physical chair (static, 200 per screen)
show_seat = "is this seat available for THIS show?" (dynamic, 200 × shows)
```

### 3. Ignoring Table Capacity
❌ **Don't do**: Design schema without mentioning how big tables get
✅ **Do this**: "The show_seat table is the hottest — 40M rows/day, 14.6B rows/year, ~1.1 TB. It must be partitioned."

### 4. Not Explaining Locking Strategy
❌ **Don't do**: "I'll use synchronized" (only works for in-memory)
✅ **Do this**: "In DB, I'd use `SELECT ... FOR UPDATE` on show_seat rows for pessimistic locking during the 5-minute payment window"

### 5. Over-Engineering the Code
❌ **Don't do**: Observer, Command, Abstract Factory patterns
✅ **Do this**: Singleton + Strategy + Facade — three patterns that are relevant and explainable

### 6. Forgetting Index Impact on Writes
❌ **Don't do**: Add 10 indexes on `show_seat` for every possible query
✅ **Do this**: "I keep indexes minimal on write-heavy tables. `(show_id, is_booked)` is the critical one. Each additional index adds write overhead on a table getting 40M inserts/day."

---

## 💡 Key Talking Points

### When Explaining the Schema:
> "I have 9 tables in 3NF. The physical infrastructure (city, theatre, screen, seat) is static — rarely changes, heavily cached. The scheduling layer (show, show_seat) is high-volume and dynamic. The transactional layer (booking, booking_seat, payment) handles money flow."

### When Explaining show_seat:
> "show_seat is the single most important table. Every physical seat gets a show_seat row for every show. With 200K shows/day and 200 seats each, that's 40 million new rows daily. I partition by date and archive past 90 days."

### When Explaining Locking:
> "For booking, I use pessimistic locking: `SELECT ... FOR UPDATE` on the specific show_seat rows. This prevents two users from booking the same seat. The lock is held for a 5-minute payment window. A background cleanup job releases expired locks every minute."

### When Explaining Indexing:
> "The most critical index is `(show_id, is_booked)` on show_seat — it powers the seat availability map at 100K QPS. Without it, every seat view would scan 40 million rows. I also have `(movie_id, show_date)` on show for the browse query."

### When Explaining Partitioning:
> "I range-partition show and show_seat by date (monthly). This gives two benefits: partition pruning makes queries fast (only scan relevant month), and archival is instant (DROP PARTITION vs DELETE). Dropping a partition is O(1)."

---

## 📝 Schema Quick Reference (Draw This on Whiteboard)

```
city (id, name)
  └── theatre (id, name, city_id, address)
        └── screen (id, name, theatre_id, total_seats)
              └── seat (id, screen_id, seat_type, row_number, seat_number)

movie (id, title, duration, language, genre, rating)
  └── show (id, movie_id, screen_id, show_date, start_time, end_time)
        └── show_seat (id, show_id, seat_id, price, is_booked, locked_at)  ⭐ HOTTEST TABLE

user (id, name, email, phone)
  └── booking (id, user_id, show_id, status, total_amount, booking_date)
        ├── booking_seat (booking_id, show_seat_id)  -- junction
        └── payment (id, booking_id, amount, method, status)
```

---

## ✅ Checklist Before Starting

- [ ] Clarify: browsing flow, seat types, pricing, cancellation
- [ ] Identify the 9 entities and their relationships
- [ ] Draw schema with columns, types, and indexes
- [ ] Calculate capacity for show_seat (THE table they'll ask about)
- [ ] Explain locking strategy (pessimistic vs optimistic)
- [ ] Explain partitioning and archival
- [ ] Implement booking flow with atomic rollback
- [ ] Demo double-booking prevention

---

## 🎤 Explanation Templates

### For Each Table:
1. **What it stores**: "This table holds..."
2. **Volume**: "At scale, it has X rows/day..."
3. **Key index**: "The critical index is... because..."
4. **Why this design**: "I separated this because..."

### Example — show_seat:
> "show_seat stores the availability of each seat for each show. At BookMyShow scale, it grows 40M rows/day — that's 14.6B rows/year and 1.1 TB of data. The critical index is (show_id, is_booked) which powers the seat map view at 100K QPS. I partition this table by date and archive past 90 days. For booking, I use SELECT ... FOR UPDATE on specific rows for pessimistic locking."

---

## 🔧 Quick Answers for Common Questions

### "How do you prevent double-booking?"
> "Pessimistic locking: `SELECT ... FOR UPDATE` on show_seat rows during booking. The lock is held for a 5-minute payment window. A background job releases expired locks. In-memory, I use `synchronized` on BookingService.createBooking()."

### "What happens if payment fails?"
> "Booking stays PENDING with locked seats. After 5 minutes, a cleanup job marks it EXPIRED and unlocks the seats. The user can retry with a new booking."

### "How do you handle peak load (e.g., Avengers release)?"
> "The read path (show browsing) is separated from the write path (booking). Read replicas + caching handle the 50K QPS browse queries. The write path (10K QPS booking) uses row-level locking, not table-level. For extreme cases, add a virtual queue before the booking step."

### "Why not use optimistic locking?"
> "Optimistic locking means the user sees seats as available, goes through the booking flow, and might fail at the end. That's a poor UX for a payment-critical flow. Pessimistic locking guarantees that once seats appear locked, they're yours. I'd consider optimistic locking only for extremely high-contention scenarios where throughput matters more than UX."

### "What about the show_seat table size?"
> "14.6B rows/year, ~1.1 TB. I handle this with: (1) Range partitioning by date — queries only scan relevant partition, (2) Archival — drop partitions older than 90 days (O(1) operation), (3) Minimal indexes — only (show_id, is_booked) to keep write performance high."

### "How would you scale the database?"
> "Vertical scaling first (MySQL handles 100M+ rows well per partition). Then: read replicas for browse queries, write to primary only, partition high-volume tables, archive aggressively. For show_seat, consider sharding by city_id if single-instance hits limits."

---

## 🎯 Success Criteria

You've succeeded if:
- ✅ Schema drawn with all 9 tables and relationships
- ✅ Capacity numbers mentioned for high-volume tables
- ✅ Indexing strategy explained for top queries
- ✅ Locking strategy (pessimistic) clearly explained with SQL
- ✅ Partitioning and archival strategy covered
- ✅ Code compiles and booking flow works
- ✅ Double-booking prevention demonstrated
- ✅ Can answer follow-ups about scaling and trade-offs

---

## 💬 Sample Interview Flow

**Interviewer**: "Design BookMyShow."

**You**:
1. "Let me clarify requirements — are we focusing on the booking flow and database design?" (buy time, show structure)
2. "Here are the 9 tables I'd design..." (draw schema on whiteboard)
3. "The show_seat table is the critical one — let me explain the capacity..." (14.6B rows/yr)
4. "For preventing double-booking, here's my locking strategy..." (SELECT ... FOR UPDATE)
5. "Let me implement the core booking logic..." (code BookingService)
6. "Let me demo the flow..." (run BookMyShowDemo, show double-booking prevention)
7. "For production, here's how I'd handle scale..." (partitioning, archival, caching)

---

**Remember: Schema + Capacity + Indexes + Locking > Perfect Code**

**Good luck! 🍀**
