package com.example.android.umclog;

import java.io.Serializable;
import java.util.ArrayList;

public class Period implements Serializable {

    private int id;
    private String name;
    private int level;
    private ArrayList<Student> students;

    public static final int GRAMMAR_LISTENING = 1;
    public static final int READING_WRITING = 2;
    public static final int SPEAKING_SKILLS = 3;
    public static final int WRITING_CLUB = 4;
    public static final int SPEAKING_CLUB = 5;

    public static int getNumberOfLessonsPerWeek(int periodType) {
        switch (periodType) {
            case SPEAKING_SKILLS:
                return 4;
            case WRITING_CLUB:
            case SPEAKING_CLUB:
                return 1;
            default:
                return 5;
        }
    }

    public Period(String name) {
        this.name = name;
        this.level = 7;
        students = null;
    }

    public String getName() {
        return name;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }

    public ArrayList<Student> getStudents() {
        return students;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
