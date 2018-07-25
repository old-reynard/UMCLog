package com.example.android.umclog;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.umclog.data.TermContract.PeriodEntry;
import com.example.android.umclog.data.TermContract.TermEntry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TermActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /* Views */
    private Spinner mTermSpinner;
    private TextView termLabelTextView;
    RecyclerView choosePeriodView;
    Button whatToShowButton;

    /* Used to set labels and buttons for the current term */
    String[] termLabels;
    String currentTermLabel;
    private int[] termIds;
    private int currentTerm;

    boolean noTerms = true;

    /* for the recycler view */
    ChoosePeriodAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    /* for the ChoosePeriodAdapter */
    private List<Period> mPeriods;

    /* Loader IDs */
    private static final int TERM_LOADER = 99;
    private static final int PERIOD_LOADER = 98;

    static boolean showAttendance = true;

    /* used to restart loaders after spinner selection */
    LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = this;

    @Override
    protected void onStart() {
        showAttendance = whatToShowButton.getText().equals(getString(R.string.attendance_activity_label));
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);

        /* Setup up the intent in the "+1" action button to redirect to CreateNewTermActivity */
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.add_new_term_actionbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createTermIntent = new Intent(TermActivity.this, CreateTermActivity.class);
                startActivity(createTermIntent);
            }
        });

        mTermSpinner = (Spinner)findViewById(R.id.choose_term_spinner);
        termLabelTextView = (TextView)findViewById(R.id.current_term_label_text_view);
        whatToShowButton = findViewById(R.id.what_to_show_button);
        whatToShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showAttendance) {
                    whatToShowButton.setText(R.string.mark_activity_label);
                } else {
                    whatToShowButton.setText(R.string.attendance_activity_label);
                }
                showAttendance = !showAttendance;
            }
        });

        setTermSpinnerVisibility();

        getLoaderManager().initLoader(TERM_LOADER, null, this);
        getLoaderManager().initLoader(PERIOD_LOADER, null, this);
    }


    public void setupSpinner(List<String> termsList) {
        setTermSpinnerVisibility();
        ArrayAdapter<String> termSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, termsList);
        termSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mTermSpinner.setAdapter(termSpinnerAdapter);
        mTermSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTerm = termIds[position];
                currentTermLabel = termLabels[position];
                termLabelTextView.setText(currentTermLabel);

                getLoaderManager().restartLoader(PERIOD_LOADER, null, loaderCallbacks);
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mTermSpinner.setSelection(termsList.size() - 1);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case TERM_LOADER:
                String[] termProjection = {
                    TermEntry.TERM_ID, TermEntry.COLUMN_START_DATE, TermEntry.COLUMN_END_DATE
                };
                return new CursorLoader(this, TermEntry.CONTENT_URI, termProjection,
                null, null, null);

            case PERIOD_LOADER:
                String[] periodProjection = {
                    PeriodEntry.COLUMN_PERIOD_ID, PeriodEntry.COLUMN_PERIOD_NAME, PeriodEntry.COLUMN_LEVEL
                };
                String selection = PeriodEntry.COLUMN_TERM_ID + "=?";
                String arg = Integer.toString(currentTerm);
                String[] selectionArgs = {arg};
                return new CursorLoader(this, PeriodEntry.CONTENT_URI, periodProjection,
                        selection, selectionArgs, null);
            default:
                throw new IllegalArgumentException("There is no loader with id " + id);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        noTerms = false;
        switch (loader.getId()) {
            case TERM_LOADER:

                termIds = new int[cursor.getCount()];
                termLabels = new String[cursor.getCount()];

                int idx = 0;
                List<String> terms = new ArrayList<>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(TermEntry.TERM_ID));
                    termIds[idx] = id;
                    String start = GeneralUtils.getUtcDate
                            (cursor.getLong(cursor.getColumnIndex(TermEntry.COLUMN_START_DATE)));
                    String startCut = start.split(",")[0];
                    String end = GeneralUtils.getUtcDate
                            (cursor.getLong(cursor.getColumnIndex(TermEntry.COLUMN_END_DATE)));
                    String endCut = end.split(",")[0];
                    String date = start + " - " + end;
                    String dateCut = startCut + " - " + endCut;
                    termLabels[idx] = date;
                    terms.add(dateCut);
                    idx++;
            }
                setupSpinner(terms);
                break;
            case PERIOD_LOADER:
                mPeriods = new LinkedList<>();
                if (cursor.getCount() < 1) {
                    return;
                }
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String periodName = cursor.getString(cursor.getColumnIndex(PeriodEntry.COLUMN_PERIOD_NAME));
                    Period thisPeriod = new Period(periodName);
                    int id = cursor.getInt(cursor.getColumnIndex(PeriodEntry.COLUMN_PERIOD_ID));
                    thisPeriod.setId(id);
                    int level = cursor.getInt(cursor.getColumnIndex(PeriodEntry.COLUMN_LEVEL));
                    thisPeriod.setLevel(level);
                    mPeriods.add(thisPeriod);
                }
                choosePeriodView = findViewById(R.id.choose_period_recyclerview);
                mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                choosePeriodView.setLayoutManager(mLayoutManager);
                choosePeriodView.setHasFixedSize(false);
                mAdapter = new ChoosePeriodAdapter(this, mPeriods, currentTerm);
                choosePeriodView.setAdapter(mAdapter);
                break;
            default:
                throw new IllegalArgumentException("There is no loader with id " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setTermSpinnerVisibility() {
        if (noTerms) {
            mTermSpinner.setVisibility(View.GONE);
        } else {
            mTermSpinner.setVisibility(View.VISIBLE);
        }
    }
}
