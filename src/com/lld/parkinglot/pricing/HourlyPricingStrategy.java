package com.lld.parkinglot.pricing;

import com.lld.parkinglot.enums.VehicleType;
import com.lld.parkinglot.models.ParkingTicket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HourlyPricingStrategy implements PricingStrategy {
    private final Map<VehicleType, Double> ratePerHour;

    public HourlyPricingStrategy() {
        ratePerHour = new HashMap<>();
        ratePerHour.put(VehicleType.MOTORCYCLE, 10.0);
        ratePerHour.put(VehicleType.CAR, 20.0);
        ratePerHour.put(VehicleType.TRUCK, 30.0);
    }

    @Override
    public double calculateCharge(ParkingTicket ticket) {
        LocalDateTime entry = ticket.getEntryTime();
        LocalDateTime exit = LocalDateTime.now();
        
        long hours = java.time.temporal.ChronoUnit.HOURS.between(entry, exit);
        if (hours < 1) {
            hours = 1; // minimum 1 hour
        }
        
        VehicleType type = ticket.getVehicle().getVehicleType();
        double rate = ratePerHour.get(type);
        return hours * rate;
    }
}
