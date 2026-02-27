package com.lld.parkinglot.pricing;

import com.lld.parkinglot.models.ParkingTicket;

public interface PricingStrategy {
    double calculateCharge(ParkingTicket ticket);
}
