package com.lld.parkinglot;

import com.lld.parkinglot.enums.SpotType;
import com.lld.parkinglot.enums.VehicleType;
import com.lld.parkinglot.models.*;
import com.lld.parkinglot.panels.EntryPanel;
import com.lld.parkinglot.panels.ExitPanel;
import com.lld.parkinglot.pricing.HourlyPricingStrategy;

public class ParkingLotDemo {

    public static void main(String[] args) {
        // Initialize parking lot
        ParkingLot lot = ParkingLot.getInstance("City Center Parking");
        lot.setPricingStrategy(new HourlyPricingStrategy());

        // Setup floors with spots
        ParkingFloor floor1 = new ParkingFloor(1);
        floor1.addSpot(new ParkingSpot("F1-S1", SpotType.SMALL));
        floor1.addSpot(new ParkingSpot("F1-S2", SpotType.SMALL));
        floor1.addSpot(new ParkingSpot("F1-M1", SpotType.MEDIUM));
        floor1.addSpot(new ParkingSpot("F1-M2", SpotType.MEDIUM));
        floor1.addSpot(new ParkingSpot("F1-M3", SpotType.MEDIUM));
        floor1.addSpot(new ParkingSpot("F1-L1", SpotType.LARGE));

        ParkingFloor floor2 = new ParkingFloor(2);
        floor2.addSpot(new ParkingSpot("F2-S1", SpotType.SMALL));
        floor2.addSpot(new ParkingSpot("F2-M1", SpotType.MEDIUM));
        floor2.addSpot(new ParkingSpot("F2-M2", SpotType.MEDIUM));
        floor2.addSpot(new ParkingSpot("F2-L1", SpotType.LARGE));
        floor2.addSpot(new ParkingSpot("F2-L2", SpotType.LARGE));

        lot.addFloor(floor1);
        lot.addFloor(floor2);

        // Setup entry and exit panels
        EntryPanel entry1 = new EntryPanel("ENTRY-A");
        EntryPanel entry2 = new EntryPanel("ENTRY-B");
        ExitPanel exit1 = new ExitPanel("EXIT-A");

        // Show initial status
        System.out.println(lot.getStatusDisplay());
        System.out.println();

        // Vehicles arrive and park
        System.out.println("--- Vehicles Entering ---");
        Vehicle bike1 = new Vehicle("KA-01-1234", VehicleType.MOTORCYCLE);
        Vehicle car1 = new Vehicle("MH-02-5678", VehicleType.CAR);
        Vehicle car2 = new Vehicle("DL-03-9012", VehicleType.CAR);
        Vehicle truck1 = new Vehicle("TN-04-3456", VehicleType.TRUCK);

        ParkingTicket ticketBike1 = entry1.issueTicket(bike1);
        ParkingTicket ticketCar1 = entry1.issueTicket(car1);
        ParkingTicket ticketCar2 = entry2.issueTicket(car2);
        ParkingTicket ticketTruck1 = entry2.issueTicket(truck1);
        System.out.println();

        // Show status after parking
        System.out.println(lot.getStatusDisplay());
        System.out.println();

        // Vehicles exit
        System.out.println("--- Vehicles Exiting ---");
        double charge1 = exit1.processExit(ticketBike1);
        System.out.printf("  Motorcycle charge: $%.2f%n", charge1);

        double charge2 = exit1.processExit(ticketCar1);
        System.out.printf("  Car charge: $%.2f%n", charge2);
        System.out.println();

        // Show final status
        System.out.println(lot.getStatusDisplay());
    }
}
