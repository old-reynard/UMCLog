package com.example.android.umclog;

public class Attendance {

    private int attendanceId;
    private long date;
    private int periodId;
    private int mark;
    private int studentId;

    public int getStudentId() {
        return studentId;
    }

    public int getPeriodId() {
        return periodId;
    }

    public int getMark() {
        return mark;
    }

    public long getDate() {
        return date;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setPeriodId(int periodId) {
        this.periodId = periodId;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getAttendanceId() {
        return attendanceId;
    }
}

