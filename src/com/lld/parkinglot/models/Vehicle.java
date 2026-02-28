package com.lld.parkinglot.models;

import com.lld.parkinglot.enums.VehicleType;

public class Vehicle {
    private final String licensePlate;
    private final VehicleType vehicleType;

    public Vehicle(String licensePlate, VehicleType vehicleType) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    /**
     * Returns vehicle details as string.
     * Example output: "CAR [MH-02-5678]" or "MOTORCYCLE [KA-01-1234]"
     */
    @Override
    public String toString() {
        return vehicleType + " [" + licensePlate + "]";
    }
}
