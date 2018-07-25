package com.example.android.umclog;

public class Mark {

    private int id;
    private int value;
    private int max;
    private int type;
    private int periodId;
    private int studentId;

    public int getId() {
        return id;
    }

    public int getMax() {
        return max;
    }

    public int getPeriodId() {
        return periodId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setPeriodId(int periodId) {
        this.periodId = periodId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
