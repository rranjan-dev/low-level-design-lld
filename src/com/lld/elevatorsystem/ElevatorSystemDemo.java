package com.lld.elevatorsystem;

import com.lld.elevatorsystem.models.*;
import com.lld.elevatorsystem.panels.FloorPanel;
import com.lld.elevatorsystem.strategy.SmartElevatorStrategy;

public class ElevatorSystemDemo {

    public static void main(String[] args) {
        // 30-floor building with 5 elevators
        ElevatorSystem system = ElevatorSystem.getInstance("Tech Tower", 30);
        system.setSelectionStrategy(new SmartElevatorStrategy());

        system.addElevator(new Elevator("E1", 10));
        system.addElevator(new Elevator("E2", 10));
        system.addElevator(new Elevator("E3", 10));
        system.addElevator(new Elevator("E4", 10));
        system.addElevator(new Elevator("E5", 10));

        System.out.println(system.getStatusDisplay());

        // Floor panels
        FloorPanel ground = new FloorPanel(0);
        FloorPanel floor15 = new FloorPanel(15);
        FloorPanel floor25 = new FloorPanel(25);

        // ──────────────────────────────────────────────
        // SCENARIO 1: Morning rush — 5 people at ground floor, all going UP
        // Shows how system distributes requests across elevators
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 1: Morning Rush at Ground Floor ===\n");

        ElevatorRequest r1 = ground.requestElevator(new Person("P1", "Alice"), 10);
        System.out.println();
        // E1 picks Alice: 0 → 10. E1 now at floor 10.

        ElevatorRequest r2 = ground.requestElevator(new Person("P2", "Bob"), 20);
        System.out.println();
        // E2 picks Bob: 0 → 20. (E1 at 10, E2/E3/E4/E5 at 0 — E2 nearest)

        ElevatorRequest r3 = ground.requestElevator(new Person("P3", "Charlie"), 5);
        System.out.println();
        // E3 picks Charlie: 0 → 5. (E3/E4/E5 still at 0)

        ElevatorRequest r4 = ground.requestElevator(new Person("P4", "Diana"), 15);
        System.out.println();
        // E4 picks Diana: 0 → 15. (E4/E5 still at 0)

        ElevatorRequest r5 = ground.requestElevator(new Person("P5", "Eve"), 25);
        System.out.println();
        // E5 picks Eve: 0 → 25. (last elevator at 0)

        System.out.println("--- After Morning Rush ---");
        System.out.println(system.getStatusDisplay());

        // ──────────────────────────────────────────────
        // SCENARIO 2: Mid-day — requests from various floors
        // Shows how system picks the nearest elevator
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 2: Mid-day Requests ===\n");
        // Current positions: E1@10, E2@20, E3@5, E4@15, E5@25

        ElevatorRequest r6 = floor15.requestElevator(new Person("P6", "Frank"), 2);
        System.out.println();
        // E4 is right at floor 15! Distance = 0. Best pick.

        ElevatorRequest r7 = floor25.requestElevator(new Person("P7", "Grace"), 10);
        System.out.println();
        // E5 at 25, distance 0 to pickup. Best pick.

        System.out.println("--- After Mid-day ---");
        System.out.println(system.getStatusDisplay());

        // ──────────────────────────────────────────────
        // SCENARIO 3: Same floor, multiple people, different destinations
        // Shows how consecutive requests from same floor distribute
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 3: Multiple People at Floor 10 ===\n");
        // Current: E1@10, E2@20, E3@5, E4@2, E5@10
        FloorPanel floor10 = new FloorPanel(10);

        ElevatorRequest r8 = floor10.requestElevator(new Person("P8", "Hank"), 28);
        System.out.println();
        // E1 at 10 or E5 at 10 — both distance 0, E1 wins (first match)

        ElevatorRequest r9 = floor10.requestElevator(new Person("P9", "Ivy"), 0);
        System.out.println();
        // E5 still at 10 (distance 0), E3 at 5 (distance 5). E5 wins.

        ElevatorRequest r10 = floor10.requestElevator(new Person("P10", "Jake"), 22);
        System.out.println();
        // E2 at 20 (distance 10), E3 at 5 (distance 5), etc. Nearest wins.

        System.out.println("--- Final Positions ---");
        System.out.println(system.getStatusDisplay());

        System.out.println("=== All Completed Requests ===");
        ElevatorRequest[] all = {r1, r2, r3, r4, r5, r6, r7, r8, r9, r10};
        for (ElevatorRequest r : all) {
            System.out.println("  " + r);
        }
    }
}
