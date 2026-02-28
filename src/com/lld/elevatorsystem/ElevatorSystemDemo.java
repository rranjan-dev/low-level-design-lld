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

        // Floor keypads (destination dispatch — passengers enter floor number)
        FloorPanel ground = new FloorPanel(0);

        // ──────────────────────────────────────────────
        // SCENARIO 1: Morning rush — 8 people at ground floor
        //   - Multiple people entering the SAME destination (floor 10)
        //   - Multiple people entering DIFFERENT destinations
        //   - Shows how system distributes across elevators
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 1: Morning Rush at Ground Floor ===\n");

        // 3 people all enter "10" on the keypad (same destination)
        ElevatorRequest r1 = ground.requestElevator(new Person("P1", "Alice"), 10);
        System.out.println();
        // All elevators at 0, E1 picked (first match). E1 → floor 10.

        ElevatorRequest r2 = ground.requestElevator(new Person("P2", "Bob"), 10);
        System.out.println();
        // E1 at 10 (dist 10), E2-E5 at 0 (dist 0). E2 picked. E2 → floor 10.

        ElevatorRequest r3 = ground.requestElevator(new Person("P3", "Charlie"), 10);
        System.out.println();
        // E1,E2 at 10. E3-E5 at 0 (dist 0). E3 picked. E3 → floor 10.
        // 3 elevators now at floor 10, 2 still at ground.

        // 2 people enter "20" (same destination, different from above)
        ElevatorRequest r4 = ground.requestElevator(new Person("P4", "Diana"), 20);
        System.out.println();
        // E4,E5 at 0 (dist 0). E4 picked. E4 → floor 20.

        ElevatorRequest r5 = ground.requestElevator(new Person("P5", "Eve"), 20);
        System.out.println();
        // E5 at 0 (dist 0), others far. E5 picked. E5 → floor 20.

        // Mix: different destinations from here
        ElevatorRequest r6 = ground.requestElevator(new Person("P6", "Frank"), 5);
        System.out.println();
        // All elevators away. Nearest to ground wins.

        ElevatorRequest r7 = ground.requestElevator(new Person("P7", "Grace"), 25);
        System.out.println();

        ElevatorRequest r8 = ground.requestElevator(new Person("P8", "Hank"), 10);
        System.out.println();
        // Another person to floor 10 — whichever elevator is nearest to ground now.

        System.out.println("--- After Morning Rush ---");
        System.out.println(system.getStatusDisplay());

        // ──────────────────────────────────────────────
        // SCENARIO 2: Mid-day — requests from upper floors
        // Shows how system picks the nearest elevator from scattered positions
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 2: Mid-day Requests from Upper Floors ===\n");

        FloorPanel floor15 = new FloorPanel(15);
        FloorPanel floor25 = new FloorPanel(25);

        ElevatorRequest r9 = floor15.requestElevator(new Person("P9", "Ivy"), 2);
        System.out.println();

        ElevatorRequest r10 = floor25.requestElevator(new Person("P10", "Jake"), 8);
        System.out.println();

        System.out.println("--- After Mid-day ---");
        System.out.println(system.getStatusDisplay());

        // ──────────────────────────────────────────────
        // SCENARIO 3: Evening rush — multiple people at floor 20, mixed destinations
        //   - Some going down, some going further up
        //   - Shows dispatching from a non-ground floor
        // ──────────────────────────────────────────────
        System.out.println("=== SCENARIO 3: Evening Rush at Floor 20 ===\n");

        FloorPanel floor20 = new FloorPanel(20);

        ElevatorRequest r11 = floor20.requestElevator(new Person("P11", "Kate"), 0);
        System.out.println();

        ElevatorRequest r12 = floor20.requestElevator(new Person("P12", "Leo"), 0);
        System.out.println();
        // Same destination (ground floor) — gets a different elevator

        ElevatorRequest r13 = floor20.requestElevator(new Person("P13", "Mia"), 28);
        System.out.println();
        // Going further up from floor 20

        System.out.println("--- Final Positions ---");
        System.out.println(system.getStatusDisplay());

        System.out.println("=== All Completed Requests ===");
        ElevatorRequest[] all = {r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13};
        for (ElevatorRequest r : all) {
            System.out.println("  " + r);
        }
    }
}
