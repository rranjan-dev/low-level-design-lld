package com.lld.bookmyshow.models;

import com.lld.bookmyshow.enums.SeatType;

public class Seat {
    private final String seatId;
    private final int rowNumber;
    private final int seatNumber;
    private final SeatType seatType;

    public Seat(String seatId, int rowNumber, int seatNumber, SeatType seatType) {
        this.seatId = seatId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public String getSeatId() { return seatId; }
    public int getRowNumber() { return rowNumber; }
    public int getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }

    @Override
    public String toString() {
        return seatType + "-R" + rowNumber + "S" + seatNumber;
    }
}
