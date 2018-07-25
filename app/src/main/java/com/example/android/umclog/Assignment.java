package com.example.android.umclog;

public class Assignment {

    public static final int NUMBER_OF_ASSIGNMENTS_IDX = 8;
    public static final int WHAT_PAGE_ON_IDX = 9;

    private int id;
    private int type;
    private long date;
    private int periodId;
    private int maxValue;
    private String name;
    private String description;

    Assignment(String name, long date, int maxValue) {
        this.name = name;
        this.date = date;
        this.maxValue = maxValue;
    }

    public int getPeriodId() {
        return periodId;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public String getDescription() {
        return description;
    }

    public void setPeriodId(int periodId) {
        this.periodId = periodId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
