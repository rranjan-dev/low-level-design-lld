package com.lld.parkinglot.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkingTicket {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static int ticketCounter = 1;

    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double charges;

    public ParkingTicket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = "TKT-" + ticketCounter++;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
    }

    public void markExitTime() {
        this.exitTime = LocalDateTime.now();
    }

    public void setCharges(double charges) {
        this.charges = charges;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public double getCharges() {
        return charges;
    }

    /**
     * Returns ticket details as string.
     * Example output (before exit):
     *   "Ticket[TKT-1] CAR [MH-02-5678] @ Spot F1-M1 | Entry: 2026-02-27 10:30:45"
     * 
     * Example output (after exit):
     *   "Ticket[TKT-1] CAR [MH-02-5678] @ Spot F1-M1 | Entry: 2026-02-27 10:30:45 | Exit: 2026-02-27 12:30:45 | Charges: $40.00"
     */
    @Override
    public String toString() {
        String result = "Ticket[" + ticketId + "] " + vehicle + " @ Spot " + spot.getSpotId();
        result += " | Entry: " + entryTime.format(FORMATTER);
        if (exitTime != null) {
            result += " | Exit: " + exitTime.format(FORMATTER);
            result += " | Charges: $" + String.format("%.2f", charges);
        }
        return result;
    }
}
