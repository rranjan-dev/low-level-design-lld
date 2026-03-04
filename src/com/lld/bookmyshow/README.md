# BookMyShow - Low Level Design (LLD)

A comprehensive, interview-ready implementation of a Movie Ticket Booking System (BookMyShow). This design demonstrates object-oriented principles, design patterns, thread-safe booking, and **production-grade database design with schema, indexing, partitioning, and query tuning** — tailored for system design interviews that test DB engineering depth.

## 📋 Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Architecture](#architecture)
- [Class Design](#class-design)
- [Design Patterns](#design-patterns)
- [Key Features](#key-features)
- [Database Design (Interview Focus)](#database-design-interview-focus)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Code Walkthrough](#code-walkthrough)
- [Interview Talking Points](#interview-talking-points)

---

## 🎯 Overview

BookMyShow lets users browse movies, view show timings across theatres in a city, select seats, and book tickets. The design emphasizes:

- **Database-First Thinking**: Schema design for 100M+ row tables, indexing strategies, query tuning
- **Concurrency Control**: Pessimistic locking to prevent double-booking of seats
- **Separation of Concerns**: Models, services, pricing, and exceptions in separate layers
- **Extensibility**: Strategy pattern for pricing, easy addition of new seat/show types
- **Interview Readiness**: Clean, explainable code with DB depth

---

## 📝 Requirements

### Functional Requirements

1. **City-based Browsing**: Users select a city and see movies playing there
2. **Theatre & Screen Management**: Theatres have multiple screens, each with fixed seat layout
3. **Show Scheduling**: A Show = Movie + Screen + Time slot. Multiple shows per screen per day
4. **Seat Selection**: Users see real-time seat availability map and select seats
5. **Booking with Locking**: Selected seats are temporarily locked during payment window
6. **Payment & Confirmation**: Booking confirmed after successful payment
7. **Cancellation**: Users can cancel bookings; seats released back to available pool
8. **Dynamic Pricing**: Prices vary by seat type, show time, and day of week

### Non-Functional Requirements

1. **Consistency over Availability**: No double-booking — pessimistic locks on seat rows
2. **High Read Throughput**: "Shows for movie in city" query must be fast (most frequent)
3. **Scalable Schema**: Tables designed for 100M+ rows with partitioning
4. **Thread Safety**: Concurrent booking attempts handled atomically

---

## 🏗️ Architecture

### High-Level Flow

```
User → BookMyShowService (Facade/Singleton)
        ├── MovieService       → Movie catalog
        ├── TheatreService     → Theatre + Screen + Seat catalog
        ├── ShowService        → Show scheduling + pricing
        └── BookingService     → Seat locking + Booking + Payment
```

### Booking Flow (Critical Path)

```
1. User selects City → sees Movies (READ: movie + show + theatre JOIN)
2. User selects Movie → sees Shows grouped by Theatre (READ: show + screen + theatre)
3. User selects Show → sees Seat Map with availability (READ: show_seat WHERE show_id = ?)
4. User selects Seats → seats LOCKED (WRITE: UPDATE show_seat SET is_booked = true ... FOR UPDATE)
5. User makes Payment → booking CONFIRMED (WRITE: INSERT booking, INSERT payment)
6. On timeout/failure → seats UNLOCKED (WRITE: UPDATE show_seat SET is_booked = false)
```

### Data Flow Diagram

```
BROWSE:    City → TheatreService → Theatre[] → ShowService → Show[]
SELECT:    Show → ShowSeat[] (available seats)
BOOK:      User + ShowSeat[] → BookingService.createBooking() → Booking (PENDING)
PAY:       Booking → Payment.markSuccess() → Booking (CONFIRMED)
CANCEL:    Booking → BookingService.cancelBooking() → seats released
```

---

## 🎨 Class Design

### 1. Enums

| Enum | Values | Behavior |
|------|--------|----------|
| `City` | BANGALORE, MUMBAI, DELHI, HYDERABAD, CHENNAI, KOLKATA, PUNE | Simple enum |
| `SeatType` | REGULAR (₹150), PREMIUM (₹250), VIP (₹400), RECLINER (₹500) | `getBasePrice()` method |
| `BookingStatus` | PENDING, CONFIRMED, CANCELLED, EXPIRED | Booking lifecycle |
| `PaymentStatus` | PENDING, SUCCESS, FAILED, REFUNDED | Payment lifecycle |

### 2. Models

| Class | Responsibility | DB Table Equivalent |
|-------|---------------|-------------------|
| `Movie` | Immutable movie info (title, duration, language, genre, rating) | `movie` table |
| `Theatre` | Theatre with screens in a city | `theatre` table |
| `Screen` | Screen within a theatre, holds list of seats | `screen` table |
| `Seat` | Physical seat (row, number, type) — fixed per screen | `seat` table |
| `Show` | A screening: Movie + Screen + TimeSlot. Initializes ShowSeats | `show` table |
| `ShowSeat` | Per-show seat availability. **Highest volume entity** | `show_seat` table |
| `Booking` | User + Show + selected ShowSeats. Lifecycle: PENDING → CONFIRMED/CANCELLED | `booking` table |
| `Payment` | Linked to booking. Separate lifecycle (retries, refunds) | `payment` table |
| `User` | User profile (name, email, phone) | `user` table |

### 3. Services

| Service | Role | Key Methods |
|---------|------|-------------|
| `BookMyShowService` | **Singleton Facade** — orchestrates all services | `bookSeats()`, `getShowsForMovie()`, `cancelBooking()` |
| `MovieService` | Movie catalogue CRUD | `addMovie()`, `searchByTitle()` |
| `TheatreService` | Theatre + Screen management | `addTheatre()`, `getTheatresByCity()` |
| `ShowService` | Show scheduling + pricing application | `addShow()`, `getShowsForMovieInCity()`, `applyPricing()` |
| `BookingService` | **Seat locking + booking creation** (critical section) | `createBooking()`, `confirmBooking()`, `cancelBooking()` |

### 4. Pricing

| Class | Role |
|-------|------|
| `PricingStrategy` | Interface: `calculatePrice(Show, ShowSeat)` |
| `ShowTimePricingStrategy` | Morning discount (0.8x), evening premium (1.3x), weekend surcharge (1.2x) |

---

## 🎭 Design Patterns

### 1. Singleton Pattern
**Where:** `BookMyShowService`  
**Why:** Single entry point to the system, shared state across all operations

```java
public static synchronized BookMyShowService getInstance() {
    if (instance == null) {
        instance = new BookMyShowService();
    }
    return instance;
}
```

### 2. Strategy Pattern
**Where:** `PricingStrategy` → `ShowTimePricingStrategy`  
**Why:** Pricing varies by time, day, demand. Easy to swap without touching booking logic

```java
service.applyPricing(show, new ShowTimePricingStrategy());
// Later: service.applyPricing(show, new DemandBasedPricingStrategy());
```

### 3. Facade Pattern
**Where:** `BookMyShowService`  
**Why:** Hides complexity of MovieService, TheatreService, ShowService, BookingService behind a single API

---

## ✨ Key Features

### 1. Pessimistic Seat Locking (Double-Booking Prevention)
```java
public synchronized Booking createBooking(User user, Show show, List<ShowSeat> requestedSeats) {
    // Lock each seat atomically — if any fails, rollback all
    for (ShowSeat seat : requestedSeats) {
        if (!seat.lockSeat()) {
            rollbackLockedSeats(lockedSeats);
            throw new SeatNotAvailableException("Seat already taken");
        }
    }
    // All locked → create booking
    return new Booking(user, show, lockedSeats, totalAmount);
}
```

### 2. Dynamic Pricing
- **Morning shows**: 20% discount (multiplier 0.8)
- **Afternoon shows**: Base price (multiplier 1.0)
- **Evening/Night shows**: 30% premium (multiplier 1.3)
- **Weekends**: Additional 20% surcharge

### 3. Booking Lifecycle
```
PENDING → CONFIRMED (payment success)
PENDING → EXPIRED (payment timeout/failure → seats released)
CONFIRMED → CANCELLED (user cancels → seats released)
```

### 4. Atomic Rollback on Failure
If any seat in a multi-seat booking fails to lock, all previously locked seats are rolled back.

---

## 🗄️ Database Design (Interview Focus)

**See [DATABASE_DESIGN.md](DATABASE_DESIGN.md) for the complete deep-dive including:**

- Full normalized schema (9 tables)
- Table capacity analysis (rows at scale for 100K+ theatres)
- Index strategy for every table
- Partitioning strategy for high-volume tables
- Query tuning for the 5 most critical queries
- Locking strategy for concurrent bookings
- ER diagram

This is the **most interview-relevant** document for system design rounds focused on DB engineering.

---

## 📁 Project Structure

```
low-level-design-lld/
└── src/
    └── com/
        └── lld/
            └── bookmyshow/
                ├── BookMyShowDemo.java          # Main driver class
                ├── README.md                    # This file
                ├── QUICK_START.md               # Quick reference
                ├── INTERVIEW_TIPS.md            # 90-min interview guide
                ├── DESIGN_DECISIONS.md          # Architecture rationale
                ├── CLASS_DIAGRAM.md             # UML + relationships
                ├── DATABASE_DESIGN.md           # Schema + query tuning (★)
                ├── enums/
                │   ├── City.java
                │   ├── SeatType.java
                │   ├── BookingStatus.java
                │   └── PaymentStatus.java
                ├── models/
                │   ├── Movie.java
                │   ├── Theatre.java
                │   ├── Screen.java
                │   ├── Seat.java
                │   ├── Show.java
                │   ├── ShowSeat.java
                │   ├── Booking.java
                │   ├── Payment.java
                │   └── User.java
                ├── services/
                │   ├── BookMyShowService.java   # Singleton facade
                │   ├── MovieService.java
                │   ├── TheatreService.java
                │   ├── ShowService.java
                │   └── BookingService.java
                ├── pricing/
                │   ├── PricingStrategy.java     # Strategy interface
                │   └── ShowTimePricingStrategy.java
                └── exceptions/
                    ├── SeatNotAvailableException.java
                    └── BookingNotFoundException.java
```

---

## 🚀 How to Run

### Prerequisites
- Java JDK 8 or higher

### Compilation

```bash
cd /Users/rranjan/Documents/MY-REPOSITORIES/personal/low-level-design-lld

javac -d out src/com/lld/bookmyshow/**/*.java src/com/lld/bookmyshow/*.java
```

### Execution

```bash
java -cp out com.lld.bookmyshow.BookMyShowDemo
```

### Expected Output

```
=== BookMyShow - Movie Ticket Booking System ===

--- Shows for 'RRR' in Bangalore ---
  RRR @ Screen 1 - IMAX | 04-Mar 18:30

--- Available Seats for: RRR @ Screen 1 - IMAX | 04-Mar 18:30 ---
  Total available: 7
    REGULAR-R1S1 [AVAILABLE] ₹195
    REGULAR-R2S2 [AVAILABLE] ₹195
    PREMIUM-R3S3 [AVAILABLE] ₹325
    ...

--- Booking: Rahul Ranjan books 2 PREMIUM seats ---
  Booking[BKG-1] RRR | 2 seats | ₹650 | PENDING | 04-Mar-2026 16:30:00
  Payment[PAY-1] ₹650 via UPI | SUCCESS

--- Priya Sharma tries to book same seats ---
  BLOCKED: Seat PREMIUM-R3S3 is no longer available

--- Rahul Ranjan cancels booking ---
  Booking status: CANCELLED
  Seats released back to available pool
```

---

## 🔍 Code Walkthrough

### Example: Booking Seats

```java
// 1. User browses shows for a movie in their city
List<Show> shows = service.getShowsForMovie(movie, City.BANGALORE);

// 2. User selects a show and views available seats
List<ShowSeat> available = service.getAvailableSeats(show);

// 3. User selects specific seats to book
List<ShowSeat> selected = Arrays.asList(available.get(0), available.get(1));
Booking booking = service.bookSeats(user, show, selected);
// Internally: locks each seat atomically, creates PENDING booking

// 4. User pays — booking confirmed
Payment payment = new Payment(booking, booking.getTotalAmount(), "UPI");
payment.markSuccess();  // Sets booking to CONFIRMED
```

### Example: Double-Booking Prevention

```java
// User A and User B try to book the same seat simultaneously
// BookingService.createBooking() is synchronized
// User A locks seat first → succeeds
// User B tries same seat → lockSeat() returns false → SeatNotAvailableException
// User B's previously locked seats (if any) are rolled back
```

### Example: Cancellation and Seat Release

```java
service.cancelBooking(booking.getBookingId());
// Internally:
// 1. booking.cancel() → sets status to CANCELLED
// 2. For each ShowSeat in booking → unlockSeat() → seat becomes available again
// 3. Other users can now book those seats
```

---

## 💼 Interview Talking Points

### SOLID Principles

1. **Single Responsibility**: `BookingService` only handles booking logic, `ShowService` only handles show scheduling, `PricingStrategy` only calculates prices
2. **Open/Closed**: New pricing strategy? Implement `PricingStrategy`. No changes to services
3. **Liskov Substitution**: Any `PricingStrategy` impl can replace `ShowTimePricingStrategy`
4. **Interface Segregation**: `PricingStrategy` has a single focused method
5. **Dependency Inversion**: Services depend on `PricingStrategy` interface, not concrete class

### Concurrency & Locking

- **Pessimistic locking**: `synchronized` on `BookingService.createBooking()` + `ShowSeat.lockSeat()`
- **Atomic rollback**: If any seat in a multi-seat booking fails, all are released
- **In DB**: `SELECT ... FOR UPDATE` on `show_seat` rows during booking window

### Database Engineering (Key Differentiator)

- **Schema normalization**: 3NF with 9 tables, proper foreign keys
- **High-volume tables**: `show_seat` (40M rows/day), `show` (200K rows/day), `booking` (500K rows/day)
- **Partitioning**: Range partition on `show_date` for `show` and `show_seat` tables
- **Indexing**: Composite indexes tuned for top 5 query patterns
- **Query tuning**: Explain plans, covering indexes, avoiding full table scans
- **See [DATABASE_DESIGN.md](DATABASE_DESIGN.md) for full details**

### Trade-offs

| Decision | Chosen | Why |
|----------|--------|-----|
| Pessimistic vs Optimistic locking | Pessimistic | Prevents overselling; consistency > throughput for payments |
| Singleton vs DI | Singleton | Simpler for interviews; mention DI as production alternative |
| In-memory vs DB | In-memory | LLD focus; DB design documented separately for SD rounds |
| Synchronized vs ReentrantLock | Synchronized | Simpler; mention finer-grained locking as extension |

---

## 📚 Learning Outcomes

After studying this implementation, you should understand:

- ✅ How to design a booking system with concurrency control
- ✅ Database schema design for high-volume transactional systems
- ✅ Pessimistic locking to prevent double-booking
- ✅ Strategy pattern for dynamic pricing
- ✅ Facade pattern to simplify complex subsystems
- ✅ Table partitioning and indexing strategies
- ✅ Query tuning for the most critical read/write paths
- ✅ Capacity estimation for tables at scale

---

**Happy Learning! 🚀**
