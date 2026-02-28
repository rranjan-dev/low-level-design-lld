package com.lld.elevatorsystem;

import com.lld.elevatorsystem.models.*;
import com.lld.elevatorsystem.panels.FloorPanel;
import com.lld.elevatorsystem.strategy.SmartElevatorStrategy;

public class ElevatorSystemDemo {

    public static void main(String[] args) {
        // 30-floor building, 4 elevators with capacity 5 each
        ElevatorSystem system = ElevatorSystem.getInstance("Tech Tower", 30);
        system.setSelectionStrategy(new SmartElevatorStrategy());

        system.addElevator(new Elevator("E1", 5));
        system.addElevator(new Elevator("E2", 5));
        system.addElevator(new Elevator("E3", 5));
        system.addElevator(new Elevator("E4", 5));

        System.out.println(system.getStatusDisplay());

        FloorPanel ground = new FloorPanel(0);

        // ──────────────────────────────────────────────
        // SCENARIO 1: Morning rush — 8 people at ground floor
        //
        //   Phase 1 (instant): each person enters a floor on the keypad.
        //           System assigns elevator immediately → "Go to E1".
        //           Strategy groups same-floor people into one elevator until full.
        //
        //   Phase 2 (dispatch): elevators actually move.
        //           E1 picks up its batch at floor 0, visits destinations.
        //           E2 picks up its batch at floor 0, visits destinations.
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 1: Morning Rush at Ground Floor ===");
        System.out.println("--- Phase 1: Passengers enter destinations (instant) ---\n");

        // E1 fills up (capacity 5)
        ground.requestElevator(new Person("P1", "Alice"), 10);
        ground.requestElevator(new Person("P2", "Bob"), 10);
        ground.requestElevator(new Person("P3", "Charlie"), 10);
        ground.requestElevator(new Person("P4", "Diana"), 20);
        ground.requestElevator(new Person("P5", "Eve"), 20);

        // E1 now full → overflow to E2
        ground.requestElevator(new Person("P6", "Frank"), 5);
        ground.requestElevator(new Person("P7", "Grace"), 25);
        ground.requestElevator(new Person("P8", "Hank"), 15);

        System.out.println("\n--- Phase 2: Elevators dispatched ---\n");
        system.dispatchElevators();

        System.out.println("\n--- Status After Morning Rush ---");
        System.out.println(system.getStatusDisplay());

        // ──────────────────────────────────────────────
        // SCENARIO 2: Mid-day — scattered requests from upper floors
        //   E1 ended at floor 20, E2 ended at floor 25 → both away from ground
        //   E3/E4 still at floor 0
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 2: Mid-day Requests from Different Floors ===");
        System.out.println("--- Phase 1: Assignments ---\n");

        FloorPanel floor18 = new FloorPanel(18);
        FloorPanel floor3 = new FloorPanel(3);

        floor18.requestElevator(new Person("P9", "Ivy"), 2);
        floor3.requestElevator(new Person("P10", "Jake"), 22);

        System.out.println("\n--- Phase 2: Dispatch ---\n");
        system.dispatchElevators();

        System.out.println("\n--- Status After Mid-day ---");
        System.out.println(system.getStatusDisplay());

        // ──────────────────────────────────────────────
        // SCENARIO 3: Evening rush — 5 people at floor 20
        //   2 going to floor 0 (going home), 2 going to floor 0 again,
        //   1 going to floor 28 (late meeting)
        //   System groups the 4 going down together, separate from the 1 going up
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 3: Evening Rush at Floor 20 ===");
        System.out.println("--- Phase 1: Assignments ---\n");

        FloorPanel floor20 = new FloorPanel(20);

        floor20.requestElevator(new Person("P11", "Kate"), 0);
        floor20.requestElevator(new Person("P12", "Leo"), 0);
        floor20.requestElevator(new Person("P13", "Mia"), 0);
        floor20.requestElevator(new Person("P14", "Nick"), 0);
        floor20.requestElevator(new Person("P15", "Olive"), 28);

        System.out.println("\n--- Phase 2: Dispatch ---\n");
        system.dispatchElevators();

        System.out.println("\n--- Final Elevator Positions ---");
        System.out.println(system.getStatusDisplay());
    }
}
