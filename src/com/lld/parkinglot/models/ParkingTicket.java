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

    public void markExit(double charges) {
        this.exitTime = LocalDateTime.now();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticket[").append(ticketId).append("] ");
        sb.append(vehicle).append(" @ Spot ").append(spot.getSpotId());
        sb.append(" | Entry: ").append(entryTime.format(FORMATTER));
        if (exitTime != null) {
            sb.append(" | Exit: ").append(exitTime.format(FORMATTER));
            sb.append(" | Charges: $").append(String.format("%.2f", charges));
        }
        return sb.toString();
    }
}
