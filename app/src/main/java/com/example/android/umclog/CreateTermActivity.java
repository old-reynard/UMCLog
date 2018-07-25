package com.example.android.umclog;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.android.umclog.data.TermContract.PeriodEntry;
import com.example.android.umclog.data.TermContract.TermEntry;
import com.example.android.umclog.data.TermContract.StudentsEntry;
import com.example.android.umclog.data.TermContract.StudentToPeriodEntry;


/**
 * This activity is designed to setup new term every four weeks
 */
public class CreateTermActivity extends AppCompatActivity implements
        DatePickerFragment.DatePickerFragmentListener,
        CreateTermAdapter.OnTermHolderListener {

    /* Adapter for new periods in the term */
    private CreateTermAdapter mCreateTermAdapter;

    /* RecyclerView for new periods */
    RecyclerView mNewPeriodsRecyclerView;

    /* Layout manager */
    LinearLayoutManager mLayoutManager;

    /* Global variables for Add Period button */
    Button mAddPeriodButton;
    private String[] mPeriodsArray;
    private LinkedList<Period> mPeriodsList;

    /* Date picket buttons and variables */
    private Button mStartTermButton;
    private Button mEndTermButton;
    private long termStartTime;
    private long termEndTime;
    private String currentDate;

    /* list of values passed from the adapter */
    private LinkedList<Period> mPeriods;
    /* custom view for confirmation alert dialog window */
    private RecyclerView confirmTermView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_term);

        confirmTermView = (RecyclerView)findViewById(R.id.confirm_term_view);

        /* Setting up the recycler view */
        mNewPeriodsRecyclerView = (RecyclerView)findViewById(R.id.new_term_periods_view);
        mLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mNewPeriodsRecyclerView.setLayoutManager(mLayoutManager);
        mNewPeriodsRecyclerView.setHasFixedSize(false);

        /* Setting up date buttons */
        currentDate = getString(R.string.change_date_button);
        mStartTermButton = findViewById(R.id.set_term_start_button);
        mStartTermButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
        mEndTermButton = findViewById(R.id.set_term_end_button);
        mEndTermButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        /* Setting up the period ArrayList */
        mPeriodsArray = getResources().getStringArray(R.array.all_periods);
        mPeriodsList = new LinkedList<>();
        for (int i = 0; i < 3; i++) mPeriodsList.add(new Period(mPeriodsArray[i]));

        /* initialising the custom term adapter */
        mCreateTermAdapter = new CreateTermAdapter(CreateTermActivity.this, mPeriodsList, this);
        mNewPeriodsRecyclerView.setAdapter(mCreateTermAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                mCreateTermAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mCreateTermAdapter.onItemDismiss(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(mNewPeriodsRecyclerView);

        /* Setting up the 'Add Period' button */
        mAddPeriodButton = findViewById(R.id.add_period_button);
        mAddPeriodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPeriodsList.size() < mPeriodsArray.length) {
                    String thisPeriod = mPeriodsArray[mPeriodsList.size()];
                    mPeriodsList.add(new Period(thisPeriod));
                } else {
                    mPeriodsList.add(new Period(mPeriodsArray[0]));
                }
                mCreateTermAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * used on data changed by DatePickerFragmentListener interface
     * @param date return long value from the interface, selected date
     * @param resId indicates which button has been used
     */
    @Override
    public void getDateOnDateSet(long date, int resId) {
        currentDate = GeneralUtils.getUtcDate(date);
        switch (resId) {
            case R.id.set_term_start_button:
                mStartTermButton.setText(currentDate);
                termStartTime = date;
                break;
            case R.id.set_term_end_button:
                mEndTermButton.setText(currentDate);
                termEndTime = date;
                break;
            default:
                mStartTermButton.setText(R.string.change_date_button);
                mEndTermButton.setText(R.string.change_date_button);
        }
    }

    /**
     * Inflate the menu options from the res/menu/menu_create_term.xml file.
     * @param menu This adds menu items to the app bar.
     * @return true to show menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_term, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:

                /* if either date is not selected, send a toast message and stop */
                if (termStartTime == 0 || termEndTime == 0) {
                    String reminder = getString(R.string.reminder_to_set_dates);
                    Toast.makeText(this, reminder, Toast.LENGTH_SHORT).show();
                    return false;
                    /* if date of the end is not after the date of the start, send a message and stop */
                } else if (termEndTime - termStartTime < 0) {
                    String reminder = getString(R.string.reminder_to_set_end_after_start);
                    Toast.makeText(this, reminder, Toast.LENGTH_SHORT).show();
                    return false;
                }

                /* if term is shorter than 24 days or longer than 27 days, send a toast message and stop */
                long daysStart = TimeUnit.MILLISECONDS.toDays(termStartTime);
                long daysEnd = TimeUnit.MILLISECONDS.toDays(termEndTime);
                if (daysEnd - daysStart > 27 || daysEnd - daysStart < 23) {
                    String reminder = getString(R.string.warning_wrong_term_duration);
                    Toast.makeText(this, reminder, Toast.LENGTH_SHORT).show();
                    return false;
                }

                /* check if any of the student lists are empty, if so, warn and stop */
                for (int i = 0; i < mPeriods.size(); i++) {
                    Period thisPeriod = mPeriods.get(i);
                    if (thisPeriod.getStudents() == null) {
                        String warning = getString(R.string.warning_add_students_number) + " " + (i + 1);
                        Toast.makeText(CreateTermActivity.this, warning, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                /*  */
                LinearLayoutManager manager =
                        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                confirmTermView.setLayoutManager(manager);
                confirmTermView.setHasFixedSize(false);

                ConfirmTermAdapter adapter = new ConfirmTermAdapter(this, mPeriods);

                confirmTermView.setAdapter(adapter);

                /* this fixes the situation when confirmTermView already has a parent */
                if (confirmTermView.getParent() != null) {
                    ((ViewGroup)confirmTermView.getParent()).removeView(confirmTermView);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.confirm_dialog_label))
                        .setView(confirmTermView)
                        .setPositiveButton(getString(R.string.confirm_dialog_positive_button),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean termSaved = saveTerm();
                                if(termSaved) finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.confirm_dialog_negative_button),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                break;
            case android.R.id.home:
                //todo set ontouchlisteners
                NavUtils.navigateUpFromSameTask(CreateTermActivity.this);
        }
        return true;
    }

    /**
     * listens to clicks on Change Date buttons, creates new date pickers and passes the button id
     * to them
     * @param v the clicked button
     */
    public void showDatePickerDialog(View v) {
        DialogFragment dateFragment = DatePickerFragment.newInstance(this, v.getId());
        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public boolean saveTerm() {

        ContentValues termValues = new ContentValues();
        termValues.put(TermEntry.COLUMN_START_DATE, termStartTime);
        termValues.put(TermEntry.COLUMN_END_DATE, termEndTime);

        Uri newTermUri = getContentResolver().insert(TermEntry.CONTENT_URI, termValues);
        String warning;
        if (newTermUri == null) {
            warning = getString(R.string.saving_term_failed_message);
        } else {
            ContentValues periodValues = new ContentValues();
            for (Period p:mPeriods) {
                String thisPeriodValue = p.getName();
                int thisLevelValue = p.getLevel();
                long thisTermValue = ContentUris.parseId(newTermUri);

                periodValues.put(PeriodEntry.COLUMN_PERIOD_NAME, thisPeriodValue);
                periodValues.put(PeriodEntry.COLUMN_LEVEL, thisLevelValue);
                periodValues.put(PeriodEntry.COLUMN_TERM_ID, thisTermValue);

                Uri newPeriodUri = getContentResolver().insert(PeriodEntry.CONTENT_URI, periodValues);
                long newPeriodId = ContentUris.parseId(newPeriodUri);

                ContentValues studentValues = new ContentValues();
                ContentValues studentToPeriodValues = new ContentValues();

                List<Student> theseStudents = p.getStudents();
                for (Student s:theseStudents) {
                    String thisStudentNameValue = s.getName();

                    studentValues.put(StudentsEntry.COLUMN_STUDENT_NAME, thisStudentNameValue);
                    studentValues.put(StudentsEntry.COLUMN_LEVEL, thisLevelValue);

                    Uri newStudentUri = getContentResolver().insert(StudentsEntry.CONTENT_URI, studentValues);
                    long newStudentId = ContentUris.parseId(newStudentUri);

                    studentToPeriodValues.put(StudentToPeriodEntry.COLUMN_STUDENT_ID, newStudentId);
                    studentToPeriodValues.put(StudentToPeriodEntry.COLUMN_PERIOD_ID, newPeriodId);

                    getContentResolver().insert(StudentToPeriodEntry.CONTENT_URI, studentToPeriodValues);
                }
            }
            warning = getString(R.string.saving_term_success_message);
        }
        Toast.makeText(CreateTermActivity.this, warning, Toast.LENGTH_SHORT).show();
        return newTermUri != null;
    }

    /* passes the Periods List from the CreateTermAdapter through the OnTermHolderListener */
    @Override
    public void onClick(LinkedList<Period> periods) {
        mPeriods = periods;
    }
}
