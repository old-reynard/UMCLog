package com.example.android.umclog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.umclog.data.TermContract.TermEntry;
import com.example.android.umclog.data.TermContract.PeriodEntry;

import com.example.android.umclog.data.TermContract.AssignmentEntry;
import com.example.android.umclog.data.TermDbHelper;

import java.util.List;

public class MarkActivity extends AppCompatActivity {

    /* period data passed through intent */
    Period mPeriod;
    int periodNumber;
    int termId;

    /* how many pages will be returned by pageAdapter, received from database */
    static int pages;

    TermDbHelper helper;
    SQLiteDatabase db;

    static Assignment[] assignments;
    static List<Student> studentNames;


    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);

        helper = new TermDbHelper(this);
        db = helper.getReadableDatabase();


        Intent currentIntent = getIntent();
        mPeriod         = (Period)currentIntent.getSerializableExtra(PeriodEntry.COLUMN_PERIOD_NAME);
        periodNumber    = currentIntent.getIntExtra("periodNumber", 0);
        termId          = currentIntent.getIntExtra(TermEntry.TERM_ID, 0);

        studentNames    = GeneralUtils.getStudentNames(db, mPeriod.getId(), mPeriod.getLevel());
        findAssignments();
        pages           = findPages();

        ViewPager pager = findViewById(R.id.viewpager_mark);
        MarkFragmentAdapter adapter = new MarkFragmentAdapter(getSupportFragmentManager(), mPeriod);
        pager.setAdapter(adapter);

        TabLayout tab   = findViewById(R.id.sliding_tabs_mark);
        tab.setupWithViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_marks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_to_attendance:
                Intent switchToAttendanceIntent = new Intent(MarkActivity.this, AttendanceActivity.class);
                switchToAttendanceIntent.putExtra(PeriodEntry.COLUMN_PERIOD_NAME, mPeriod);
                switchToAttendanceIntent.putExtra(TermEntry.TERM_ID, termId);
                switchToAttendanceIntent.putExtra("periodNumber", periodNumber);
                startActivity(switchToAttendanceIntent);
                break;
            case R.id.add_mark_button:
                Intent newAssignmentIntent = new Intent(MarkActivity.this, NewAssignmentActivity.class);
                newAssignmentIntent.putExtra(PeriodEntry.TABLE_NAME, mPeriod);
                startActivity(newAssignmentIntent);
                break;
            case R.id.add_student_marks:
                if (mPeriod != null) {
                    GeneralUtils.addOrEditStudent(this, null, mPeriod, false);
                }
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(MarkActivity.this);
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        db.close();
        helper.close();
        super.onDestroy();
    }

    private int findPages() {
            String SQL_FIND_PAGES =              "SELECT COUNT(" +
            AssignmentEntry._ID                + ") FROM " +
            AssignmentEntry.TABLE_NAME         + " WHERE " +
            AssignmentEntry.COLUMN_PERIOD_ID   +   "="     + mPeriod.getId();
            Cursor cursor = db.rawQuery(SQL_FIND_PAGES, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
        return count / GeneralUtils.BUTTONS_PER_SCREEN + 1;

    }

    /**
     * queries the list of assignments for Marks activity
     */
    private void findAssignments() {
        Cursor datesCursor = db.query(  AssignmentEntry.TABLE_NAME,
                new String[]{           AssignmentEntry.COLUMN_DATE,
                                        AssignmentEntry.COLUMN_MARK_TYPE,
                                        AssignmentEntry.COLUMN_NAME,
                                        AssignmentEntry.COLUMN_MAX_VALUE,
                                        AssignmentEntry.COLUMN_ASSIGNMENT_ID},
                                        AssignmentEntry.COLUMN_PERIOD_ID+"=?",
                                        new String[] {Integer.toString(mPeriod.getId())},
                                        null,
                                        null,
                                        null);
        int assignmentsThisTerm = datesCursor.getCount();
        assignments = new Assignment[assignmentsThisTerm];

        int idx = 0;
        for (datesCursor.moveToFirst(); !datesCursor.isAfterLast(); datesCursor.moveToNext()) {
            String title    = datesCursor.getString(datesCursor.getColumnIndex(AssignmentEntry.COLUMN_NAME));
            long date       = datesCursor.getLong(datesCursor.getColumnIndex(AssignmentEntry.COLUMN_DATE));
            int type        = datesCursor.getInt(datesCursor.getColumnIndex(AssignmentEntry.COLUMN_MARK_TYPE));
            int max         = datesCursor.getInt(datesCursor.getColumnIndex(AssignmentEntry.COLUMN_MAX_VALUE));
            int id          = datesCursor.getInt(datesCursor.getColumnIndex(AssignmentEntry.COLUMN_ASSIGNMENT_ID));
            Assignment assignment = new Assignment(title, date, max);
            assignment.setType(type);
            assignment.setId(id);
            assignments[idx++] = assignment;
        }
        datesCursor.close();
    }

    public Period getmPeriod() {
        return mPeriod;
    }
}
