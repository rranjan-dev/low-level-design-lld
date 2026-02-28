package com.lld.parkinglot.enums;

public enum VehicleType {
    MOTORCYCLE,
    CAR,
    TRUCK;
    
    public SpotType getRequiredSpotType() {
        if (this == MOTORCYCLE) return SpotType.SMALL;
        if (this == CAR) return SpotType.MEDIUM;
        return SpotType.LARGE; // TRUCK
    }
}
