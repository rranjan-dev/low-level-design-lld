package com.lld.parkinglot.panels;

import com.lld.parkinglot.models.ParkingLot;
import com.lld.parkinglot.models.ParkingTicket;
import com.lld.parkinglot.models.Vehicle;

public class EntryPanel {
    private final String panelId;

    public EntryPanel(String panelId) {
        this.panelId = panelId;
    }

    public ParkingTicket issueTicket(Vehicle vehicle) {
        ParkingTicket ticket = ParkingLot.getInstance().parkVehicle(vehicle);
        System.out.println("[" + panelId + "] Issued: " + ticket);
        return ticket;
    }

    public String getPanelId() {
        return panelId;
    }
}
