package com.lld.parkinglot.panels;

import com.lld.parkinglot.models.ParkingLot;
import com.lld.parkinglot.models.ParkingTicket;

public class ExitPanel {
    private final String panelId;

    public ExitPanel(String panelId) {
        this.panelId = panelId;
    }

    public double processExit(ParkingTicket ticket) {
        double charges = ParkingLot.getInstance().unparkVehicle(ticket);
        System.out.println("[" + panelId + "] Exit processed: " + ticket);
        return charges;
    }

    public String getPanelId() {
        return panelId;
    }
}
