package com.lld.parkinglot.enums;

public enum VehicleType {
    MOTORCYCLE(SpotType.SMALL),
    CAR(SpotType.MEDIUM),
    TRUCK(SpotType.LARGE);

    private final SpotType requiredSpotType;

    VehicleType(SpotType requiredSpotType) {
        this.requiredSpotType = requiredSpotType;
    }

    public SpotType getRequiredSpotType() {
        return requiredSpotType;
    }
}
