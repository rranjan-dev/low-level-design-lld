package com.lld.parkinglot.enums;

public enum SpotType {
    SMALL(1),
    MEDIUM(2),
    LARGE(3);

    private final int sizeRank;

    SpotType(int sizeRank) {
        this.sizeRank = sizeRank;
    }

    public int getSizeRank() {
        return sizeRank;
    }

    public boolean canFit(SpotType requiredType) {
        return this.sizeRank >= requiredType.sizeRank;
    }
}
