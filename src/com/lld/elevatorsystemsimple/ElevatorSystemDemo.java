package com.lld.elevatorsystemsimple;

import com.lld.elevatorsystemsimple.dispatcher.NearestDispatchStrategy;
import com.lld.elevatorsystemsimple.models.Elevator;
import com.lld.elevatorsystemsimple.models.ElevatorSystem;
import com.lld.elevatorsystemsimple.models.Floor;
import com.lld.elevatorsystemsimple.panels.InsidePanel;
import com.lld.elevatorsystemsimple.panels.OutsidePanel;

public class ElevatorSystemDemo {

    public static void main(String[] args) {
        // Setup: 15-floor building, 3 elevators, capacity 8 each
        ElevatorSystem system = ElevatorSystem.getInstance("Skyline Tower");
        system.setDispatchStrategy(new NearestDispatchStrategy());

        Elevator e1 = new Elevator("E1");
        Elevator e2 = new Elevator("E2");
        Elevator e3 = new Elevator("E3");
        system.addElevator(e1);
        system.addElevator(e2);
        system.addElevator(e3);

        InsidePanel panelE1 = new InsidePanel(e1);
        InsidePanel panelE2 = new InsidePanel(e2);
        InsidePanel panelE3 = new InsidePanel(e3);

        system.showStatus();

        // ──────────────────────────────────────────────────
        // SCENARIO 1: Person at Floor 0 wants to go to Floor 10
        // ──────────────────────────────────────────────────
        System.out.println("=== SCENARIO 1: Person at Ground Floor going to Floor 10 ===\n");

        Floor groundFloor = system.getFloors().get(0);
        OutsidePanel groundPanel = groundFloor.getOutsidePanel();

        // Step 1: Press UP on ground floor panel
        Elevator assigned1 = groundPanel.pressUp();

        // Step 2: Elevator arrives at ground floor, passenger boards
        system.dispatchAll();
        assigned1.addPassengers(1, 70.0);

        // Step 3: Passenger presses Floor 10 inside elevator
        panelE1.pressFloor(10);

        // Step 4: Elevator moves to destination
        system.dispatchAll();
        assigned1.removePassengers(1, 70.0);

        system.showStatus();

        // ──────────────────────────────────────────────────
        // SCENARIO 2: Two people on different floors, same direction
        //   Person A at Floor 3 going UP to Floor 12
        //   Person B at Floor 5 going UP to Floor 14
        // ──────────────────────────────────────────────────
        System.out.println("=== SCENARIO 2: Two people going UP from different floors ===\n");

        OutsidePanel floor3Panel = system.getFloors().get(3).getOutsidePanel();
        OutsidePanel floor5Panel = system.getFloors().get(5).getOutsidePanel();

        Elevator assignedA = floor3Panel.pressUp();
        Elevator assignedB = floor5Panel.pressUp();

        // Dispatch elevators to pickup floors
        system.dispatchAll();

        // Passengers board and select floors
        assignedA.addPassengers(1, 65.0);
        new InsidePanel(assignedA).pressFloor(12);

        assignedB.addPassengers(1, 80.0);
        new InsidePanel(assignedB).pressFloor(14);

        // Elevators deliver passengers
        system.dispatchAll();
        assignedA.removePassengers(1, 65.0);
        assignedB.removePassengers(1, 80.0);

        system.showStatus();

        // ──────────────────────────────────────────────────
        // SCENARIO 3: Person going DOWN from Floor 12 to Floor 2
        // ──────────────────────────────────────────────────
        System.out.println("=== SCENARIO 3: Person going DOWN from Floor 12 to Floor 2 ===\n");

        OutsidePanel floor12Panel = system.getFloors().get(12).getOutsidePanel();
        Elevator assignedC = floor12Panel.pressDown();

        system.dispatchAll();
        assignedC.addPassengers(1, 75.0);
        new InsidePanel(assignedC).pressFloor(2);

        system.dispatchAll();
        assignedC.removePassengers(1, 75.0);

        system.showStatus();

        // ──────────────────────────────────────────────────
        // SCENARIO 4: Capacity check — try to exceed 8 passengers
        // ──────────────────────────────────────────────────
        System.out.println("=== SCENARIO 4: Capacity Limit Check ===\n");

        e3.addPassengers(7, 560.0);
        System.out.println("  Added 7 passengers to E3 (560 kg)");
        e3.addPassengers(1, 80.0);
        System.out.println("  Added 1 passenger to E3 (80 kg)");
        e3.addPassengers(1, 70.0); // should fail — capacity exceeded
        e3.removePassengers(8, 640.0);

        system.showStatus();

        // ──────────────────────────────────────────────────
        // SCENARIO 5: Door operation — door cannot open while moving
        // ──────────────────────────────────────────────────
        System.out.println("=== SCENARIO 5: Door Operation Safety ===\n");

        OutsidePanel floor0Panel = system.getFloors().get(0).getOutsidePanel();
        Elevator assignedD = floor0Panel.pressUp();

        if (assignedD != null) {
            new InsidePanel(assignedD).pressFloor(5);
            assignedD.closeDoor();
            // Elevator now has destination — simulate mid-move door open attempt
            assignedD.addDestination(5);
            assignedD.moveOneFloor();
            System.out.println("  Attempting to open door while moving...");
            assignedD.openDoor(); // should fail
            // Let elevator complete its journey
            assignedD.processAllStops();
        }

        System.out.println("\n=== Final Status ===");
        system.showStatus();
    }
}
