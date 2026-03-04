package com.lld.bookmyshow.enums;

public enum SeatType {
    REGULAR(150.0),
    PREMIUM(250.0),
    VIP(400.0),
    RECLINER(500.0);

    private final double basePrice;

    SeatType(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}
