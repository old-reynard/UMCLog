package com.example.android.umclog.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.umclog.Attendance;
import com.example.android.umclog.data.TermContract.AssignmentEntry;
import com.example.android.umclog.data.TermContract.StudentsEntry;
import com.example.android.umclog.data.TermContract.PeriodEntry;
import com.example.android.umclog.data.TermContract.TermEntry;
import com.example.android.umclog.data.TermContract.StudentToPeriodEntry;
import com.example.android.umclog.data.TermContract.AttendanceEntry;
import com.example.android.umclog.data.TermContract.MarkEntry;

public class TermDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "terms.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    private static final String SQL_INTEGER = " INTEGER";
    private static final String SQL_NOT_NULL = " NOT NULL";
    private static final String SQL_TEXT = " TEXT";
    private static final String COMMA = ", ";
    private static final String REFERENCES = " REFERENCES ";
    private static final String UNIQUE = " UNIQUE ON CONFLICT IGNORE";


    /* SQL statement creates Students_names table */
    private static final String SQL_CREATE_STUDENTS_TABLE =
            SQL_CREATE_TABLE + StudentsEntry.TABLE_NAME + " (" +
            StudentsEntry.STUDENT_ID + SQL_INTEGER      + " PRIMARY KEY AUTOINCREMENT" + COMMA +
            StudentsEntry.COLUMN_STUDENT_NAME           + SQL_TEXT      + SQL_NOT_NULL + COMMA +
            StudentsEntry.COLUMN_LEVEL                  + SQL_INTEGER   + SQL_NOT_NULL + ");";

    /* SQL statement creates Periods table */
    private static final String SQL_CREATE_PERIOD_TABLE =
            SQL_CREATE_TABLE + PeriodEntry.TABLE_NAME + " (" +
            PeriodEntry.COLUMN_PERIOD_ID    + SQL_INTEGER   + " PRIMARY KEY AUTOINCREMENT" + COMMA +
            PeriodEntry.COLUMN_PERIOD_NAME  + SQL_TEXT      + SQL_NOT_NULL                 + COMMA +
            PeriodEntry.COLUMN_LEVEL        + SQL_INTEGER                                  + COMMA +
            PeriodEntry.COLUMN_TERM_ID      + SQL_INTEGER   + REFERENCES +
            TermEntry.TABLE_NAME            + " ("          + TermEntry.TERM_ID + ")" + ");";

    /* SQL statement creates Student_to_Period table */
    private static final String SQL_CREATE_STUDENT_TO_PERIOD_TABLE =
            SQL_CREATE_TABLE + StudentToPeriodEntry.TABLE_NAME  + " ("          +
            StudentToPeriodEntry.COLUMN_STUDENT_ID              + SQL_INTEGER   + REFERENCES +
            StudentsEntry.TABLE_NAME                            + " ("          +
            StudentsEntry.STUDENT_ID                            + ")"           + COMMA +
            StudentToPeriodEntry.COLUMN_PERIOD_ID               + SQL_INTEGER   + REFERENCES +
            PeriodEntry.TABLE_NAME                              + " ("          +
            PeriodEntry.COLUMN_PERIOD_ID                        + ")"           + ");";

    /* SQL statement creates Terms table */
    private static final String SQL_CREATE_TERMS_TABLE =
            SQL_CREATE_TABLE + TermEntry.TABLE_NAME + " (" +
            TermEntry.TERM_ID           + SQL_INTEGER   + " PRIMARY KEY AUTOINCREMENT"  + COMMA +
            TermEntry.COLUMN_START_DATE + SQL_INTEGER   + UNIQUE                        + COMMA +
            TermEntry.COLUMN_END_DATE   + SQL_INTEGER   + UNIQUE                        + ");";

    /* SQL statement creates Attendance table */
    private static final String SQL_CREATE_ATTENDANCE_MARK_TABLE =
            SQL_CREATE_TABLE + AttendanceEntry.TABLE_NAME + " (" +
            AttendanceEntry.COLUMN_ATTENDANCE_ID    + SQL_INTEGER + " PRIMARY KEY AUTOINCREMENT" + COMMA +
            AttendanceEntry.COLUMN_ATTENDANCE_DATE  + SQL_INTEGER + COMMA +
            AttendanceEntry.COLUMN_ATTENDANCE_MARK  + SQL_INTEGER + COMMA +
            AttendanceEntry.COLUMN_PERIOD_ID        + SQL_INTEGER + COMMA +
            AttendanceEntry.COLUMN_STUDENT_ID       + SQL_INTEGER + ");";

    /* SQL statement creates Marks table */
    private static final String SQL_CREATE_MARKS_TABLE =
            SQL_CREATE_TABLE + MarkEntry.TABLE_NAME + " (" +
            MarkEntry.COLUMN_MARK_ID                + SQL_INTEGER + " PRIMARY KEY AUTOINCREMENT UNIQUE" + COMMA +
            MarkEntry.COLUMN_MARK_VALUE             + SQL_INTEGER + COMMA +
            MarkEntry.COLUMN_ASSIGNMENT_ID          + SQL_INTEGER + COMMA +
            MarkEntry.COLUMN_STUDENT_ID             + SQL_INTEGER + ");";

    /* SQL statement creates Assignments table */
    private static final String SQL_CREATE_ASSIGNMENTS_TABLE =
            SQL_CREATE_TABLE + AssignmentEntry.TABLE_NAME   + " (" +
            AssignmentEntry.COLUMN_ASSIGNMENT_ID    + SQL_INTEGER + " PRIMARY KEY AUTOINCREMENT" + COMMA +
            AssignmentEntry.COLUMN_MARK_TYPE        + SQL_INTEGER + COMMA +
            AssignmentEntry.COLUMN_PERIOD_ID        + SQL_INTEGER + COMMA +
            AssignmentEntry.COLUMN_DATE             + SQL_INTEGER + COMMA +
            AssignmentEntry.COLUMN_MAX_VALUE        + SQL_INTEGER + COMMA +
            AssignmentEntry.COLUMN_NAME             + SQL_TEXT    + COMMA +
            AssignmentEntry.COLUMN_DESCRIPTION      + SQL_TEXT    + ");";

    private static final String CREATE_UNIQUE_INDEX_STUDENTS_TO_PERIOD =
            "CREATE UNIQUE INDEX idx_student_to_period ON " +
            StudentToPeriodEntry.TABLE_NAME                 + " (" +
            StudentToPeriodEntry.COLUMN_STUDENT_ID          + COMMA +
            StudentToPeriodEntry.COLUMN_PERIOD_ID           + ");";


    public TermDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is where the creation of
     * tables and the initial population of the tables should happen.
     * @param db database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STUDENTS_TABLE);
        db.execSQL(SQL_CREATE_PERIOD_TABLE);
        db.execSQL(SQL_CREATE_STUDENT_TO_PERIOD_TABLE);
        db.execSQL(SQL_CREATE_TERMS_TABLE);
        db.execSQL(SQL_CREATE_ATTENDANCE_MARK_TABLE);
        db.execSQL(SQL_CREATE_ASSIGNMENTS_TABLE);
        db.execSQL(SQL_CREATE_MARKS_TABLE);
        db.execSQL(CREATE_UNIQUE_INDEX_STUDENTS_TO_PERIOD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
