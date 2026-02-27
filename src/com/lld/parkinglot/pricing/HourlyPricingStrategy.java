package com.lld.parkinglot.pricing;

import com.lld.parkinglot.enums.VehicleType;
import com.lld.parkinglot.models.ParkingTicket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

public class HourlyPricingStrategy implements PricingStrategy {
    private final Map<VehicleType, Double> ratePerHour;

    public HourlyPricingStrategy() {
        ratePerHour = new EnumMap<>(VehicleType.class);
        ratePerHour.put(VehicleType.MOTORCYCLE, 10.0);
        ratePerHour.put(VehicleType.CAR, 20.0);
        ratePerHour.put(VehicleType.TRUCK, 30.0);
    }

    public HourlyPricingStrategy(Map<VehicleType, Double> customRates) {
        ratePerHour = new EnumMap<>(customRates);
    }

    @Override
    public double calculateCharge(ParkingTicket ticket) {
        LocalDateTime entry = ticket.getEntryTime();
        LocalDateTime exit = LocalDateTime.now();

        long totalSeconds = Duration.between(entry, exit).getSeconds();
        long hours = (long) Math.ceil(totalSeconds / 3600.0);
        if (hours < 1) {
            hours = 1; // minimum 1 hour charge
        }

        VehicleType type = ticket.getVehicle().getVehicleType();
        double rate = ratePerHour.getOrDefault(type, 20.0);
        return hours * rate;
    }
}
