# Database Design - BookMyShow

**This is the most interview-relevant document.** Covers schema design, table capacity at scale, indexing strategies, partitioning, query tuning, and locking — the exact topics tested in system design rounds focused on DB engineering.

---

## 📋 Table of Contents

- [ER Diagram](#er-diagram)
- [Schema Design (9 Tables)](#schema-design-9-tables)
- [Table Capacity Analysis](#table-capacity-analysis)
- [Indexing Strategy](#indexing-strategy)
- [Partitioning Strategy](#partitioning-strategy)
- [Top 5 Critical Queries (Tuned)](#top-5-critical-queries-tuned)
- [Locking Strategy for Bookings](#locking-strategy-for-bookings)
- [Read vs Write Path Separation](#read-vs-write-path-separation)
- [Data Archival Strategy](#data-archival-strategy)
- [Interview Cheat Sheet](#interview-cheat-sheet)

---

## 🗂️ ER Diagram

```
┌──────────┐       ┌──────────────┐       ┌───────────┐
│   city   │──1:N──│   theatre    │──1:N──│  screen   │──1:N──┌──────────┐
└──────────┘       └──────────────┘       └───────────┘       │   seat   │
                                                │              └──────────┘
                                               1:N                  │
                   ┌──────────┐            ┌──────────┐            1:N (per show)
                   │  movie   │──1:N──────│   show   │──1:N──┌───────────┐
                   └──────────┘            └──────────┘       │ show_seat │
                                                │              └───────────┘
                                               1:N                  │
                   ┌──────────┐            ┌──────────┐            N:1
                   │   user   │──1:N──────│ booking  │─────────────┘
                   └──────────┘            └──────────┘
                                                │
                                               1:N
                                           ┌──────────┐
                                           │ payment  │
                                           └──────────┘
```

**Relationships:**
- City 1:N Theatre 1:N Screen 1:N Seat (physical infrastructure)
- Movie 1:N Show N:1 Screen (scheduling)
- Show 1:N ShowSeat N:1 Seat (per-show seat availability)
- User 1:N Booking N:N ShowSeat (through booking_seat junction)
- Booking 1:N Payment (supports retries)

---

## 🗄️ Schema Design (9 Tables)

### Table 1: `city`
```sql
CREATE TABLE city (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    state       VARCHAR(100),
    country     VARCHAR(50) DEFAULT 'India',
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE INDEX idx_city_name (name)
);
```
**Volume:** ~50 rows. Tiny, cacheable. Rarely changes.

---

### Table 2: `movie`
```sql
CREATE TABLE movie (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    duration_mins   INT NOT NULL,
    language        VARCHAR(50) NOT NULL,
    genre           VARCHAR(100),
    rating          DECIMAL(3,1),
    release_date    DATE,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_movie_language (language),
    INDEX idx_movie_active_release (is_active, release_date),
    FULLTEXT INDEX idx_movie_title_search (title)
);
```
**Volume:** ~10K rows (all movies ever). Small table, heavily cached.

---

### Table 3: `theatre`
```sql
CREATE TABLE theatre (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(500),
    city_id     BIGINT NOT NULL,
    latitude    DECIMAL(10, 8),
    longitude   DECIMAL(11, 8),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (city_id) REFERENCES city(id),
    INDEX idx_theatre_city (city_id),
    INDEX idx_theatre_city_active (city_id, is_active)
);
```
**Volume:** ~100K rows (all theatres across India). Small-medium, cacheable.

---

### Table 4: `screen`
```sql
CREATE TABLE screen (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    theatre_id  BIGINT NOT NULL,
    total_seats INT NOT NULL,
    screen_type VARCHAR(50),   -- IMAX, Dolby, Regular
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (theatre_id) REFERENCES theatre(id),
    INDEX idx_screen_theatre (theatre_id)
);
```
**Volume:** ~500K rows (100K theatres × 5 screens avg). Small-medium.

---

### Table 5: `seat` (Physical Seats — Fixed per Screen)
```sql
CREATE TABLE seat (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    screen_id   BIGINT NOT NULL,
    seat_type   ENUM('REGULAR', 'PREMIUM', 'VIP', 'RECLINER') NOT NULL,
    row_number  INT NOT NULL,
    seat_number INT NOT NULL,
    
    FOREIGN KEY (screen_id) REFERENCES screen(id),
    UNIQUE INDEX idx_seat_screen_row_num (screen_id, row_number, seat_number),
    INDEX idx_seat_screen (screen_id)
);
```
**Volume:** ~100M rows (500K screens × 200 seats avg). **Medium-large table.**
This is a static/master table — rows are inserted once when a screen is set up. No updates.

---

### Table 6: `show` ⚡ HIGH VOLUME
```sql
CREATE TABLE show (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    movie_id    BIGINT NOT NULL,
    screen_id   BIGINT NOT NULL,
    show_date   DATE NOT NULL,
    start_time  DATETIME NOT NULL,
    end_time    DATETIME NOT NULL,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (movie_id) REFERENCES movie(id),
    FOREIGN KEY (screen_id) REFERENCES screen(id),
    
    -- THE critical index for "shows for movie X in city Y on date Z"
    INDEX idx_show_movie_date (movie_id, show_date),
    INDEX idx_show_screen_date (screen_id, show_date),
    INDEX idx_show_date (show_date)
) PARTITION BY RANGE (TO_DAYS(show_date)) (
    PARTITION p_2026_01 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p_2026_02 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p_2026_03 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p_2026_04 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p_future  VALUES LESS THAN MAXVALUE
);
```
**Volume:** ~73M rows/year (500K screens × 4 shows/day × 365 days). **HIGH volume.**

---

### Table 7: `show_seat` ⚡⚡ HIGHEST VOLUME TABLE
```sql
CREATE TABLE show_seat (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    show_id     BIGINT NOT NULL,
    seat_id     BIGINT NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    is_booked   BOOLEAN DEFAULT FALSE,
    locked_at   TIMESTAMP NULL,        -- When seat was locked for payment
    locked_by   BIGINT NULL,           -- user_id who locked it
    version     INT DEFAULT 0,         -- For optimistic locking (alternative)
    
    FOREIGN KEY (show_id) REFERENCES show(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    
    -- THE critical index for "available seats for show X"
    INDEX idx_show_seat_show_booked (show_id, is_booked),
    -- For lock expiry cleanup batch job
    INDEX idx_show_seat_locked (locked_at),
    UNIQUE INDEX idx_show_seat_unique (show_id, seat_id)
) PARTITION BY RANGE (show_id) (
    -- Partitioned by show_id ranges (aligned with show table partitions)
    -- In practice, partition by the show's date using a composite approach
    PARTITION p_range_1 VALUES LESS THAN (10000000),
    PARTITION p_range_2 VALUES LESS THAN (20000000),
    PARTITION p_range_3 VALUES LESS THAN (30000000),
    PARTITION p_future  VALUES LESS THAN MAXVALUE
);
```
**Volume:** ~14.6 BILLION rows/year (73M shows × 200 seats avg). **THE largest table.**

### Why `show_seat` is the Table Interviewers Care About Most

| Metric | Value |
|--------|-------|
| **Rows per day** | ~40M (200K shows × 200 seats) |
| **Rows per month** | ~1.2B |
| **Rows per year** | ~14.6B |
| **Row size** | ~80 bytes (IDs + price + flags + timestamps) |
| **Data per year** | ~1.1 TB (raw, before indexes) |
| **Read QPS** | ~100K (users viewing seat maps) |
| **Write QPS** | ~10K (bookings, lock/unlock) |

**This is where you demonstrate DB engineering depth:**
- Partitioning is essential — cannot scan 14B rows
- Index on `(show_id, is_booked)` is the most critical index in the system
- Locking strategy (pessimistic vs optimistic) directly affects this table
- Old data must be archived/purged — you can't keep 14B rows forever

---

### Table 8: `booking`
```sql
CREATE TABLE booking (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    show_id         BIGINT NOT NULL,
    booking_status  ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    total_amount    DECIMAL(10, 2) NOT NULL,
    seat_count      INT NOT NULL,
    booking_date    DATE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (show_id) REFERENCES show(id),
    
    INDEX idx_booking_user_status (user_id, booking_status),
    INDEX idx_booking_show_status (show_id, booking_status),
    INDEX idx_booking_date_status (booking_date, booking_status),
    INDEX idx_booking_created (created_at)
) PARTITION BY RANGE (TO_DAYS(booking_date)) (
    PARTITION p_2026_01 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p_2026_02 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p_2026_03 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p_2026_04 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p_future  VALUES LESS THAN MAXVALUE
);
```
**Volume:** ~180M rows/year (~500K bookings/day). High volume, partitioned by date.

---

### Table 9: `booking_seat` (Junction Table)
```sql
CREATE TABLE booking_seat (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id      BIGINT NOT NULL,
    show_seat_id    BIGINT NOT NULL,
    
    FOREIGN KEY (booking_id) REFERENCES booking(id),
    FOREIGN KEY (show_seat_id) REFERENCES show_seat(id),
    
    UNIQUE INDEX idx_booking_seat_unique (booking_id, show_seat_id),
    INDEX idx_booking_seat_show_seat (show_seat_id)
);
```
**Volume:** ~450M rows/year (180M bookings × 2.5 seats avg). Junction table.

---

### Table 10: `payment`
```sql
CREATE TABLE payment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id      BIGINT NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL,
    payment_method  VARCHAR(50) NOT NULL,  -- UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING
    payment_status  ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_id  VARCHAR(255),           -- External payment gateway reference
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (booking_id) REFERENCES booking(id),
    
    INDEX idx_payment_booking (booking_id),
    INDEX idx_payment_status_date (payment_status, created_at),
    INDEX idx_payment_txn_id (transaction_id)
);
```
**Volume:** ~220M rows/year (includes retries — ~1.2 attempts/booking avg).

---

### Table 11: `user`
```sql
CREATE TABLE user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE INDEX idx_user_email (email),
    UNIQUE INDEX idx_user_phone (phone)
);
```
**Volume:** ~100M rows (registered users). Medium table, heavily cached in auth layer.

---

## 📊 Table Capacity Analysis

### Summary at BookMyShow Scale

| Table | Rows/Day | Rows/Year | Row Size | Data/Year | Growth Rate |
|-------|----------|-----------|----------|-----------|-------------|
| `city` | ~0 | ~50 | ~200B | negligible | Static |
| `movie` | ~5 | ~2K | ~500B | ~1 MB | Low |
| `theatre` | ~10 | ~4K | ~300B | ~1.2 MB | Low |
| `screen` | ~50 | ~18K | ~150B | ~2.7 MB | Low |
| `seat` | ~10K | ~3.6M | ~50B | ~180 MB | Low (bulk insert on screen setup) |
| **`show`** | **200K** | **73M** | **~100B** | **~7.3 GB** | **High** |
| **`show_seat`** ⭐ | **40M** | **14.6B** | **~80B** | **~1.1 TB** | **Very High** |
| `booking` | 500K | 180M | ~150B | ~27 GB | High |
| `booking_seat` | 1.25M | 450M | ~30B | ~13.5 GB | High |
| `payment` | 600K | 220M | ~200B | ~44 GB | High |
| `user` | 50K | 18M | ~300B | ~5.4 GB | Medium |

### Total Active Data (1 Year)
- **Without archival:** ~1.2 TB (dominated by `show_seat`)
- **With 90-day rolling window:** ~300 GB
- **With 30-day rolling window:** ~110 GB

### Interview Talking Points on Capacity
> "The `show_seat` table is the elephant in the room — 14.6 billion rows per year, ~1.1 TB of data. This table MUST be partitioned by date and we need a 90-day archival policy to keep the active dataset manageable. The critical index is `(show_id, is_booked)` which powers the seat availability map — the most frequently executed query."

---

## 🔍 Indexing Strategy

### Index Design Principles
1. **Cover the WHERE clause** — indexes match query predicates
2. **Leading column selectivity** — most selective column first in composite index
3. **Avoid redundant indexes** — a composite index `(A, B)` already covers queries on `A`
4. **Monitor index size** — especially on `show_seat` where each index costs ~100+ GB/year

### Critical Indexes by Table

#### `show` Table
| Index | Columns | Purpose | Query |
|-------|---------|---------|-------|
| `idx_show_movie_date` | `(movie_id, show_date)` | Find shows for a movie on a date | Q1 |
| `idx_show_screen_date` | `(screen_id, show_date)` | Find shows on a screen for scheduling | Admin |
| `idx_show_date` | `(show_date)` | Partition pruning + date range queries | Q1, Archival |

#### `show_seat` Table ⭐
| Index | Columns | Purpose | Query |
|-------|---------|---------|-------|
| `idx_show_seat_show_booked` | `(show_id, is_booked)` | **THE most critical index** — seat availability | Q2 |
| `idx_show_seat_locked` | `(locked_at)` | Lock expiry cleanup batch job | Background |
| `idx_show_seat_unique` | `(show_id, seat_id)` | Uniqueness + direct seat lookup | Q3 (locking) |

#### `booking` Table
| Index | Columns | Purpose | Query |
|-------|---------|---------|-------|
| `idx_booking_user_status` | `(user_id, booking_status)` | "My bookings" page | Q4 |
| `idx_booking_show_status` | `(show_id, booking_status)` | Show occupancy dashboard | Admin |
| `idx_booking_date_status` | `(booking_date, booking_status)` | Daily reports, archival | Reports |

---

## 📦 Partitioning Strategy

### Why Partition?
- `show_seat` grows at 40M rows/day. Without partitioning, queries scan billions of rows
- `show` grows at 200K rows/day. Partition pruning eliminates old partitions from queries
- Old data must be archivable without locking active partitions

### Partition Scheme

#### `show` — Range Partition by `show_date` (Monthly)
```sql
PARTITION BY RANGE (TO_DAYS(show_date)) (
    PARTITION p_2026_01 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p_2026_02 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    ...
    PARTITION p_future  VALUES LESS THAN MAXVALUE
);
```
**Benefit:** Query for "shows on 2026-03-04" only scans `p_2026_03` partition.

#### `show_seat` — Range Partition by `show_id` Range
Since `show_seat` has FK to `show`, and `show` is partitioned by date, we align `show_seat` partition boundaries with `show.id` ranges.

**Alternative (better):** If using MySQL 8.0+, use **composite partition** on `(show_date)` by duplicating `show_date` into `show_seat` as a denormalized column:
```sql
ALTER TABLE show_seat ADD COLUMN show_date DATE NOT NULL;

PARTITION BY RANGE (TO_DAYS(show_date)) (
    PARTITION p_2026_03 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    ...
);
```
**Trade-off:** Denormalization adds 4 bytes/row but enables date-based partition pruning on the largest table.

#### `booking` — Range Partition by `booking_date` (Monthly)
Same approach as `show`. Enables efficient archival and date-range reports.

### Partition Pruning in Action
```sql
-- Without partitioning: scans 14.6B rows
-- With partitioning: scans only ~1.2B rows (1 month partition)
SELECT * FROM show_seat 
WHERE show_id IN (SELECT id FROM show WHERE show_date = '2026-03-04')
  AND is_booked = FALSE;
```

---

## 🔥 Top 5 Critical Queries (Tuned)

### Q1: "Find all shows for Movie X in City Y on Date Z"
**Frequency:** ~50K QPS (most common read query)

```sql
-- Unoptimized (3-table JOIN, potential full scan)
SELECT s.*, t.name as theatre_name, sc.name as screen_name
FROM show s
JOIN screen sc ON s.screen_id = sc.id
JOIN theatre t ON sc.theatre_id = t.id
WHERE s.movie_id = 101
  AND t.city_id = 5
  AND s.show_date = '2026-03-04'
ORDER BY s.start_time;
```

**Optimization:**
1. **Partition pruning** on `show.show_date` — only scans March 2026 partition
2. **Index** `idx_show_movie_date (movie_id, show_date)` — index lookup, not table scan
3. **Covering index** — add `screen_id` to avoid table lookup:
   ```sql
   CREATE INDEX idx_show_movie_date_screen 
   ON show (movie_id, show_date, screen_id, start_time);
   ```
4. **Cache** theatre + screen data (rarely changes) — reduces JOIN cost

**EXPLAIN Plan (optimized):**
```
+----+-------+-------+-------------------------------+---------+------+
| id | table | type  | key                           | rows    | Extra|
+----+-------+-------+-------------------------------+---------+------+
|  1 | s     | range | idx_show_movie_date_screen    | 15      | ...  |
|  1 | sc    | eq_ref| PRIMARY                       | 1       | ...  |
|  1 | t     | eq_ref| PRIMARY                       | 1       | ...  |
+----+-------+-------+-------------------------------+---------+------+
```
**Result:** From scanning millions of rows → scanning ~15 rows.

---

### Q2: "Get available seats for Show X" (Seat Map)
**Frequency:** ~100K QPS (every user viewing seat availability)

```sql
SELECT ss.id, ss.price, ss.is_booked,
       s.row_number, s.seat_number, s.seat_type
FROM show_seat ss
JOIN seat s ON ss.seat_id = s.id
WHERE ss.show_id = 50001
  AND ss.is_booked = FALSE
ORDER BY s.row_number, s.seat_number;
```

**Optimization:**
1. **Index** `idx_show_seat_show_booked (show_id, is_booked)` — exact match on both columns
2. **Partition pruning** if `show_seat` is partitioned by date
3. Each show has ~200 seats, so even without optimization, it's bounded. But the index avoids scanning 40M rows/day

**EXPLAIN Plan:**
```
+----+-------+------+-------------------------------+---------+
| id | table | type | key                           | rows    |
+----+-------+------+-------------------------------+---------+
|  1 | ss    | ref  | idx_show_seat_show_booked     | 150     |
|  1 | s     | eq_ref| PRIMARY                      | 1       |
+----+-------+------+-------------------------------+---------+
```
**Result:** ~150 rows scanned (only available seats for that show). Lightning fast.

---

### Q3: "Lock seats for booking" (Critical Write Path)
**Frequency:** ~10K QPS (booking attempts)

```sql
BEGIN;

-- Step 1: Lock specific rows (pessimistic locking)
SELECT id, is_booked FROM show_seat 
WHERE show_id = 50001 
  AND seat_id IN (1001, 1002, 1003)
  AND is_booked = FALSE
FOR UPDATE;

-- Step 2: Verify all requested seats are available (row count check)
-- If count != requested count → ROLLBACK

-- Step 3: Mark seats as booked
UPDATE show_seat 
SET is_booked = TRUE, locked_at = NOW(), locked_by = 42
WHERE show_id = 50001 
  AND seat_id IN (1001, 1002, 1003);

-- Step 4: Insert booking
INSERT INTO booking (user_id, show_id, booking_status, total_amount, seat_count, booking_date)
VALUES (42, 50001, 'PENDING', 975.00, 3, CURDATE());

-- Step 5: Insert booking_seat junction rows
INSERT INTO booking_seat (booking_id, show_seat_id) VALUES
  (LAST_INSERT_ID(), 60001),
  (LAST_INSERT_ID(), 60002),
  (LAST_INSERT_ID(), 60003);

COMMIT;
```

**Key Design Decisions:**
- `SELECT ... FOR UPDATE` locks only the specific rows, not the whole table
- `UNIQUE INDEX idx_show_seat_unique (show_id, seat_id)` ensures point lookups
- Transaction isolation: `READ COMMITTED` is sufficient (we lock specific rows)
- Lock held for ~5 minutes max (payment window TTL)

---

### Q4: "My Bookings" (User's booking history)
**Frequency:** ~20K QPS

```sql
SELECT b.id, b.booking_status, b.total_amount, b.created_at,
       m.title, s.start_time, t.name as theatre_name
FROM booking b
JOIN show s ON b.show_id = s.id
JOIN movie m ON s.movie_id = m.id
JOIN screen sc ON s.screen_id = sc.id
JOIN theatre t ON sc.theatre_id = t.id
WHERE b.user_id = 42
  AND b.booking_status IN ('CONFIRMED', 'CANCELLED')
ORDER BY b.created_at DESC
LIMIT 20;
```

**Optimization:**
1. **Index** `idx_booking_user_status (user_id, booking_status)` — covers WHERE clause
2. `LIMIT 20` — pagination prevents full scan
3. Movie/Theatre data is cacheable (rarely changes)

---

### Q5: "Lock expiry cleanup" (Background Job)
**Frequency:** Runs every minute (batch job, not user-facing)

```sql
-- Release seats that were locked > 5 minutes ago but never paid
UPDATE show_seat 
SET is_booked = FALSE, locked_at = NULL, locked_by = NULL
WHERE locked_at IS NOT NULL 
  AND locked_at < DATE_SUB(NOW(), INTERVAL 5 MINUTE)
  AND is_booked = TRUE;

-- Also expire the corresponding bookings
UPDATE booking 
SET booking_status = 'EXPIRED', updated_at = NOW()
WHERE booking_status = 'PENDING'
  AND created_at < DATE_SUB(NOW(), INTERVAL 5 MINUTE);
```

**Optimization:**
- **Index** `idx_show_seat_locked (locked_at)` — finds expired locks without full scan
- **Index** `idx_booking_date_status (booking_date, booking_status)` — finds stale pending bookings
- Runs in batches of 1000 to avoid long-running transactions

---

## 🔒 Locking Strategy for Bookings

### Pessimistic Locking (Chosen)

```
User selects seats → SELECT ... FOR UPDATE → row-level lock acquired
    → Payment window (5 min TTL)
    → Payment success → UPDATE is_booked = TRUE, INSERT booking → COMMIT
    → Payment failure/timeout → ROLLBACK (or background cleanup)
```

**Pros:**
- Guarantees no double-booking (strongest consistency)
- Simple to reason about
- Well-supported by MySQL InnoDB

**Cons:**
- Locks held during payment (~5 min) — reduces concurrency
- Lock contention on popular shows (same rows)

### Optimistic Locking (Alternative — Mention in Interview)

```sql
-- Use version column
UPDATE show_seat 
SET is_booked = TRUE, version = version + 1
WHERE show_id = 50001 AND seat_id = 1001 
  AND is_booked = FALSE AND version = 3;

-- If affected_rows = 0 → someone else booked it → retry or fail
```

**Pros:**
- No held locks — higher throughput
- Better for high-contention scenarios

**Cons:**
- Retry logic needed at application layer
- Can lead to user frustration (seat "available" then fails at booking)

### Interview Answer Template
> "I'd use pessimistic locking with `SELECT ... FOR UPDATE` because booking consistency is critical — we cannot oversell seats. The lock is held for a short window (~5 min payment TTL) and we have a background cleanup job that releases expired locks. For extremely popular shows with high contention, I'd consider optimistic locking with version columns as a tradeoff for higher throughput."

---

## 📡 Read vs Write Path Separation

### Read Path (Browseable Data)
- **Pattern:** Read-heavy, cacheable, eventual consistency OK
- **Tables:** movie, theatre, screen, seat, city
- **Optimization:**
  - MySQL read replicas for read queries
  - Redis/Memcached for movie, theatre, screen data (TTL: 5 min)
  - CDN for static movie info (posters, descriptions)

### Write Path (Transactional Data)
- **Pattern:** Write-heavy, strong consistency required
- **Tables:** show_seat, booking, booking_seat, payment
- **Optimization:**
  - Write to primary MySQL only
  - Minimal indexes on write-heavy tables (each index slows writes)
  - Batch inserts for show_seat creation (200 rows per show)

### Read-Write Ratio by Table

| Table | Read % | Write % | Caching |
|-------|--------|---------|---------|
| `city` | 99.9% | 0.1% | Cache forever |
| `movie` | 99% | 1% | Cache 5 min |
| `theatre` | 99% | 1% | Cache 5 min |
| `screen` | 99% | 1% | Cache 5 min |
| `seat` | 99% | 1% | Cache 5 min |
| `show` | 95% | 5% | Cache 1 min (shows added daily) |
| `show_seat` | 70% | 30% | **No cache** (real-time availability) |
| `booking` | 60% | 40% | No cache (transactional) |
| `payment` | 40% | 60% | No cache (transactional) |

---

## 🗑️ Data Archival Strategy

### Why Archival Matters
- `show_seat` grows 1.1 TB/year. After 3 years = 3.3 TB of data, 44B rows
- Query performance degrades as partitions grow
- Old show data (past screenings) is rarely accessed

### Archival Policy

| Table | Active Window | Archive After | Archive To |
|-------|--------------|---------------|-----------|
| `show_seat` | 30 days | 90 days | Cold storage (S3 + Athena) |
| `show` | 90 days | 180 days | Archive DB |
| `booking` | 1 year | 2 years | Archive DB |
| `payment` | 1 year | 3 years (regulatory) | Archive DB |

### Archival Process
```sql
-- 1. Move old show_seat rows to archive table
INSERT INTO show_seat_archive 
SELECT * FROM show_seat 
WHERE show_id IN (SELECT id FROM show WHERE show_date < DATE_SUB(CURDATE(), INTERVAL 90 DAY));

-- 2. Delete from main table
DELETE FROM show_seat 
WHERE show_id IN (SELECT id FROM show WHERE show_date < DATE_SUB(CURDATE(), INTERVAL 90 DAY));

-- 3. With partitioning, this is even faster:
ALTER TABLE show_seat DROP PARTITION p_2025_12;  -- Drop entire month partition
```

**Interview Point:** Dropping a partition is O(1) — instant. Deleting rows is O(n) — slow and generates tons of redo log. Always prefer partition drops for archival.

---

## 📝 Interview Cheat Sheet

### Quick Reference: What to Say About Each Table

| Table | Volume | Key Index | Key Decision |
|-------|--------|-----------|-------------|
| `show_seat` ⭐ | 14.6B/yr | `(show_id, is_booked)` | Partition by date, pessimistic lock |
| `show` | 73M/yr | `(movie_id, show_date)` | Partition by show_date monthly |
| `booking` | 180M/yr | `(user_id, booking_status)` | Partition by booking_date |
| `payment` | 220M/yr | `(booking_id)` | Separate from booking for SRP |
| `seat` | 100M | `(screen_id, row, num)` | Static master data, cached |
| `user` | 100M | `(email)` UNIQUE | Heavily cached in auth layer |

### Key Numbers to Remember

| Metric | Value |
|--------|-------|
| Shows per day (India) | ~200K |
| Show seats per day | ~40M |
| Bookings per day | ~500K |
| Seat availability QPS | ~100K |
| Booking write QPS | ~10K |
| show_seat data per year | ~1.1 TB |
| Payment window TTL | 5 minutes |
| Archive window | 90 days (show_seat), 2 years (booking) |

### The 3 Sentences That Impress Interviewers

1. > "The `show_seat` table is the hottest table — 14 billion rows/year, 1.1 TB of data. It MUST be partitioned by date and archived every 90 days."

2. > "For seat booking, I use pessimistic locking with `SELECT ... FOR UPDATE` on specific `show_seat` rows. The lock is held for a 5-minute payment window with a background cleanup job for expired locks."

3. > "The most critical index is `(show_id, is_booked)` on `show_seat` — it powers the seat availability map which runs at 100K QPS. Without it, every seat map view would scan 40 million rows."

---

## 🔧 Schema Evolution Tips

### Adding a New Feature: "Offers/Coupons"
```sql
CREATE TABLE coupon (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(50) UNIQUE NOT NULL,
    discount_pct DECIMAL(5,2),
    max_discount DECIMAL(10,2),
    valid_from  DATETIME,
    valid_until DATETIME,
    max_usage   INT,
    current_usage INT DEFAULT 0,
    INDEX idx_coupon_code_validity (code, valid_from, valid_until)
);

ALTER TABLE booking ADD COLUMN coupon_id BIGINT NULL REFERENCES coupon(id);
ALTER TABLE booking ADD COLUMN discount_amount DECIMAL(10,2) DEFAULT 0;
```
**Impact:** One new table + two nullable columns on booking. No schema migration on high-volume tables.

### Adding "Reviews/Ratings"
```sql
CREATE TABLE review (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    movie_id    BIGINT NOT NULL,
    rating      TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (movie_id) REFERENCES movie(id),
    UNIQUE INDEX idx_review_user_movie (user_id, movie_id),
    INDEX idx_review_movie_rating (movie_id, rating)
);
```
**Impact:** Independent table. No changes to existing schema.

---

**This document is your secret weapon for system design interviews. Master the capacity numbers, indexing strategy, and locking flow — they demonstrate real DB engineering experience.**
