package com.example.android.umclog;

public enum AttendanceType {
    PRESENT             (1),
    ABSENT              (2),
    ABSENT_WITH_EXCUSE  (3),
    LATE                (4),
    LATE_WITH_EXCUSE    (5),
    EMPTY               (0);

    private int type;

    AttendanceType(int type) {
        this.type = type;
    }

    int getType() {
        return this.type;
    }

}
