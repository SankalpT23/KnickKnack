package com.sankalp.KnickKnack.model.enums;

public enum ReservationStatus {
    PENDING,
    CHECKED_OUT,
    RETURNED,
    RETURNED_LATE,
    RETURNED_DAMAGED, // NEW
    CANCELLED,
    EXPIRED,
    OWNER_CANCELLED, // NEW
    DISPUTED
}
