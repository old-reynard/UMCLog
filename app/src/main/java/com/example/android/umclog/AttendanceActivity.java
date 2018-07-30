package com.example.android.umclog;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.UserManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.umclog.data.TermContract;
import com.example.android.umclog.data.TermContract.TermEntry;
import com.example.android.umclog.data.TermContract.PeriodEntry;
import com.example.android.umclog.data.TermDbHelper;

import java.util.concurrent.TimeUnit;

public class AttendanceActivity extends AppCompatActivity {

    /* period this attendance is for, passed from TermActivity via intent */
    Period mPeriod;
    int thisPeriodNumber;
    int thisTermId;
    long start;
    ViewPager viewPager;

    boolean cameFromTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Intent incomingIntent = getIntent();
        mPeriod         = (Period)incomingIntent.getSerializableExtra(PeriodEntry.COLUMN_PERIOD_NAME);
//        cameFromTerm    = incomingIntent.getBooleanExtra("showAttendance", true);
        thisPeriodNumber = incomingIntent.getIntExtra("periodNumber", 1);

//        TODO set custom actionbar
        thisTermId  = incomingIntent.getIntExtra(TermEntry.TERM_ID, 1);

        TermDbHelper dbHelper = new TermDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String SQL_QUERY_TERM = "SELECT " +
                TermContract.TermEntry.COLUMN_START_DATE    + " FROM "  +
                TermContract.TermEntry.TABLE_NAME           + " WHERE " +
                TermContract.TermEntry.TERM_ID              + "="       + thisTermId;
        Cursor cursor = db.rawQuery(SQL_QUERY_TERM, null);
        cursor.moveToFirst();
        start = cursor.getLong(cursor.getColumnIndex(TermContract.TermEntry.COLUMN_START_DATE));
        cursor.close();

        String[] allPeriods = getResources().getStringArray(R.array.all_periods);
        int periodType = GeneralUtils.find(allPeriods, mPeriod.getName()) + 1;

        viewPager = findViewById(R.id.viewpager);

        AttendanceFragmentAdapter adapter =
                new AttendanceFragmentAdapter(getSupportFragmentManager(), mPeriod.getId(),
                        thisTermId, mPeriod.getLevel(), periodType, cameFromTerm);

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        long today = System.currentTimeMillis();
        int thisWeek = (int)TimeUnit.MILLISECONDS.toDays(today - start) / 7;
        if (thisWeek  < Term.NUMBER_OF_WEEKS) {
            viewPager.setCurrentItem(thisWeek);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_to_marks:
                Intent switchToMarksIntent = new Intent(AttendanceActivity.this, MarkActivity.class);
                switchToMarksIntent.putExtra(PeriodEntry.COLUMN_PERIOD_NAME, mPeriod);
                switchToMarksIntent.putExtra(TermEntry.TERM_ID, thisTermId);
                switchToMarksIntent.putExtra("periodNumber", thisPeriodNumber);
                startActivity(switchToMarksIntent);
                break;
            case R.id.add_student_attendance:
                if (mPeriod != null) {
                    GeneralUtils.addOrEditStudent(this, null, mPeriod, false);
                }
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(AttendanceActivity.this);
                break;
        }
        return true;
    }
}