package com.lld.elevatorsystem;

import com.lld.elevatorsystem.models.*;
import com.lld.elevatorsystem.panels.FloorPanel;
import com.lld.elevatorsystem.strategy.NearestElevatorStrategy;

public class ElevatorSystemDemo {

    public static void main(String[] args) {
        // Initialize elevator system
        ElevatorSystem system = ElevatorSystem.getInstance("Tech Tower", 10);
        system.setSelectionStrategy(new NearestElevatorStrategy());

        // Add 3 elevators (all start at ground floor)
        system.addElevator(new Elevator("E1", 8));
        system.addElevator(new Elevator("E2", 8));
        system.addElevator(new Elevator("E3", 8));

        // Show initial status
        System.out.println(system.getStatusDisplay());

        // Setup floor panels (like entry panels in parking lot)
        FloorPanel ground = new FloorPanel(0);
        FloorPanel floor5 = new FloorPanel(5);
        FloorPanel floor8 = new FloorPanel(8);

        // Passengers request elevators
        System.out.println("--- Passengers Requesting Elevators ---\n");

        Person alice = new Person("P1", "Alice");
        Person bob = new Person("P2", "Bob");
        Person charlie = new Person("P3", "Charlie");
        Person diana = new Person("P4", "Diana");

        ElevatorRequest r1 = ground.requestElevator(alice, 5);   // E1: ground → 5
        System.out.println();

        ElevatorRequest r2 = ground.requestElevator(bob, 7);     // E2: ground → 7 (E1 moved away)
        System.out.println();

        ElevatorRequest r3 = floor5.requestElevator(charlie, 0); // E1 at 5, picks Charlie → 0
        System.out.println();

        ElevatorRequest r4 = floor8.requestElevator(diana, 2);   // E2 at 7, nearest to 8 → picks Diana
        System.out.println();

        // Show elevator positions after all rides
        System.out.println("--- Final Elevator Positions ---");
        System.out.println(system.getStatusDisplay());

        // Show all completed requests
        System.out.println("--- Completed Requests ---");
        System.out.println("  " + r1);
        System.out.println("  " + r2);
        System.out.println("  " + r3);
        System.out.println("  " + r4);
    }
}
