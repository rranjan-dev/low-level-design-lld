# Quick Start Guide - BookMyShow

---

## Problem Statement

> Design a Movie Ticket Booking System (like BookMyShow) where users can browse movies in a city, view show timings across theatres, select seats, and book tickets with real-time availability and no double-booking.

### Functional Requirements

1. **City → Movies → Shows → Theatres** browsing flow
2. **Multiple seat types**: REGULAR, PREMIUM, VIP, RECLINER with different pricing
3. **Real-time seat availability** map per show
4. **Seat locking** during payment window (prevent double-booking)
5. **Dynamic pricing** based on show time + day of week
6. **Booking lifecycle**: PENDING → CONFIRMED → CANCELLED
7. **Payment** with success/failure/refund states

### Non-Functional Requirements

1. **No double-booking** — pessimistic locking on seat rows
2. **High read throughput** — "shows for movie in city" at ~50K QPS
3. **Scalable DB** — `show_seat` table handles 14.6B rows/year
4. **Thread-safe** — concurrent booking attempts handled atomically

---

## Quick Compile & Run

```bash
# Compile
javac -d out src/com/lld/bookmyshow/**/*.java src/com/lld/bookmyshow/*.java

# Run
java -cp out com.lld.bookmyshow.BookMyShowDemo
```

---

## 📦 Key Classes at a Glance

| Class | Purpose | Key Method |
|-------|---------|------------|
| `BookMyShowService` | Singleton facade/orchestrator | `bookSeats()`, `getShowsForMovie()` |
| `BookingService` | Seat locking + booking creation | `createBooking()`, `cancelBooking()` |
| `ShowService` | Show scheduling + pricing | `getShowsForMovieInCity()`, `applyPricing()` |
| `Show` | Movie + Screen + TimeSlot | `initializeSeats()`, `getAvailableSeats()` |
| `ShowSeat` | Per-show seat availability | `lockSeat()`, `unlockSeat()` |
| `Booking` | User + Show + seats | `confirm()`, `cancel()`, `expire()` |
| `Payment` | Linked to booking | `markSuccess()`, `markFailed()`, `refund()` |
| `PricingStrategy` | Pricing interface | `calculatePrice(Show, ShowSeat)` |

---

## 🔑 Key Concepts

### Entity Hierarchy
```
City → Theatre → Screen → Seat (physical infrastructure, static)
Movie → Show → ShowSeat (scheduling, dynamic per day)
User → Booking → Payment (transactional)
```

### Seat Types & Base Pricing
| Type | Base Price | Row Position |
|------|-----------|-------------|
| REGULAR | ₹150 | Front rows |
| PREMIUM | ₹250 | Middle rows |
| VIP | ₹400 | Back rows |
| RECLINER | ₹500 | Last row |

### Booking Status Lifecycle
```
PENDING ──→ CONFIRMED (payment success)
   │
   ├──→ EXPIRED (payment timeout → seats released)
   │
   └──→ CANCELLED (user cancel → seats released)
```

---

## 💻 Code Snippets

### Basic Usage

```java
// 1. Initialize system
BookMyShowService service = BookMyShowService.getInstance();

// 2. Add movies, theatres, screens, shows
service.addMovie(movie);
service.addTheatre(theatre);
service.addShow(show);
service.applyPricing(show, new ShowTimePricingStrategy());

// 3. Browse shows for a movie in a city
List<Show> shows = service.getShowsForMovie(movie, City.BANGALORE);

// 4. View available seats
List<ShowSeat> available = service.getAvailableSeats(show);

// 5. Book seats (with atomic locking)
Booking booking = service.bookSeats(user, show, selectedSeats);

// 6. Pay and confirm
Payment payment = new Payment(booking, booking.getTotalAmount(), "UPI");
payment.markSuccess();  // booking → CONFIRMED

// 7. Cancel if needed
service.cancelBooking(booking.getBookingId());  // seats released
```

---

## 🎯 Design Patterns Used

1. **Singleton** → `BookMyShowService` (single system entry point)
2. **Strategy** → `PricingStrategy` (swappable pricing models)
3. **Facade** → `BookMyShowService` (hides service layer complexity)

---

## 🧪 Testing Scenarios

### Test 1: Basic Booking
```java
Booking booking = service.bookSeats(user, show, selectedSeats);
assert booking.getStatus() == BookingStatus.PENDING;
payment.markSuccess();
assert booking.getStatus() == BookingStatus.CONFIRMED;
```

### Test 2: Double-Booking Prevention
```java
// User A books seats 1,2
service.bookSeats(userA, show, Arrays.asList(seat1, seat2));
// User B tries same seats → exception
try {
    service.bookSeats(userB, show, Arrays.asList(seat1, seat2));
    assert false; // should not reach here
} catch (SeatNotAvailableException e) {
    // Expected
}
```

### Test 3: Cancellation Releases Seats
```java
Booking booking = service.bookSeats(user, show, selectedSeats);
int beforeCancel = service.getAvailableSeats(show).size();
service.cancelBooking(booking.getBookingId());
int afterCancel = service.getAvailableSeats(show).size();
assert afterCancel == beforeCancel + selectedSeats.size();
```

### Test 4: Atomic Rollback
```java
// Book seat1 (available), seat2 (already booked) in one request
// Both should remain in original state after failure
try {
    service.bookSeats(user, show, Arrays.asList(availableSeat, bookedSeat));
} catch (SeatNotAvailableException e) {
    assert availableSeat.isAvailable(); // rolled back
}
```

---

## 📊 Core Flows (Remember These)

### Booking Flow
```
User → BookMyShowService.bookSeats()
  → BookingService.createBooking()
    → for each seat: ShowSeat.lockSeat() [synchronized]
    → if any fails: rollbackLockedSeats() + throw SeatNotAvailableException
    → all locked: new Booking(PENDING) created
  → User pays: Payment.markSuccess() → Booking.confirm()
```

### Cancellation Flow
```
User → BookMyShowService.cancelBooking()
  → BookingService.cancelBooking()
    → Booking.cancel()
      → status = CANCELLED
      → for each ShowSeat: unlockSeat() → seat becomes available
```

---

## 🗄️ Database Quick Reference

### Highest Volume Tables
| Table | Rows/Year | Critical Index |
|-------|-----------|---------------|
| `show_seat` | 14.6B | `(show_id, is_booked)` |
| `booking_seat` | 450M | `(booking_id, show_seat_id)` |
| `payment` | 220M | `(booking_id)` |
| `booking` | 180M | `(user_id, booking_status)` |
| `show` | 73M | `(movie_id, show_date)` |

### Critical SQL: Lock Seats for Booking
```sql
BEGIN;
SELECT id FROM show_seat WHERE show_id = ? AND seat_id IN (?, ?, ?) 
  AND is_booked = FALSE FOR UPDATE;
UPDATE show_seat SET is_booked = TRUE WHERE id IN (...);
INSERT INTO booking (...) VALUES (...);
COMMIT;
```

See [DATABASE_DESIGN.md](DATABASE_DESIGN.md) for full schema, capacity analysis, and query tuning.

---

## ⚠️ Important Notes

1. **Thread Safety**: BookingService.createBooking() is synchronized — one booking at a time
2. **Singleton**: Only one BookMyShowService instance
3. **Atomic Rollback**: Failed multi-seat booking releases all previously locked seats
4. **Payment Window**: Seats locked for ~5 min; background job releases expired locks
5. **Pricing Applied Per Show**: Must call `applyPricing()` after `addShow()`

---

## Possible Extensions (Mention If Asked)

- **Coupon/discount system** → new `coupon` table, `discount_amount` on booking
- **Reviews & ratings** → new `review` table, aggregated rating on movie
- **Notifications** → Observer pattern for booking confirmation SMS/email
- **Waitlist** → if show is sold out, users join waitlist; notify on cancellation
- **Multiple languages** → add `language_id` to show, filter in search
- **Analytics dashboard** → materialized views on booking + payment tables
- **Seat map UI** → row/column based grid from `seat` table layout

---

**Tip: Master the DATABASE_DESIGN.md — that's what interviewers care about most.**
