package com.lld.elevatorsystemsimple.models;

import com.lld.elevatorsystemsimple.display.ExternalDisplay;
import com.lld.elevatorsystemsimple.panels.OutsidePanel;

public class Floor {
    private final int floorNumber;
    private final OutsidePanel outsidePanel;
    private final ExternalDisplay externalDisplay;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.outsidePanel = new OutsidePanel(floorNumber);
        this.externalDisplay = new ExternalDisplay(floorNumber);
    }

    public int getFloorNumber() { return floorNumber; }
    public OutsidePanel getOutsidePanel() { return outsidePanel; }
    public ExternalDisplay getExternalDisplay() { return externalDisplay; }
}
