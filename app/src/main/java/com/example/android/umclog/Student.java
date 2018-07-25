package com.example.android.umclog;

import java.io.Serializable;

public class Student implements Serializable {

    private int studentId;
    private String name;
    private int level;
    private String period;

    public Student(String name) {
        this.name = name;
//        this.level = 7;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
}
