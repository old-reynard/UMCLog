package com.example.android.umclog;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.umclog.data.TermContract.PeriodEntry;
import com.example.android.umclog.data.TermContract.MarkEntry;
import com.example.android.umclog.data.TermContract.AssignmentEntry;
import com.example.android.umclog.data.TermDbHelper;

public class NewAssignmentActivity extends AppCompatActivity
        implements DatePickerFragment.DatePickerFragmentListener {

    Period mPeriod;

    EditText titleView;
    EditText descriptionView;
    EditText maximumView;
    Spinner typeSpinner;
    Button dateButton;

    long chosenDate;

    static int assignmentId;

    boolean assignmentExists;

    DatePickerFragment.DatePickerFragmentListener listener = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_assigment);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        mPeriod = (Period)getIntent().getSerializableExtra(PeriodEntry.TABLE_NAME);

        titleView       = findViewById(R.id.title_editText);
        typeSpinner     = findViewById(R.id.assignment_type_spinner);
        dateButton      = findViewById(R.id.new_assignment_choose_date_button);
        maximumView     = findViewById(R.id.maximum_mark_editView);
        descriptionView = findViewById(R.id.description_editText);

        assignmentId    = getIntent().getIntExtra(AssignmentEntry.COLUMN_ASSIGNMENT_ID, 0);
        if (assignmentId != 0) {
            setTitle(R.string.edit_assignment_label);

        }

        TermDbHelper dbHelper = new TermDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

//        String assignmentExistsQuery = "SELECT * FROM" + AssignmentEntry.TABLE_NAME;
        String[] columns = {
                AssignmentEntry.COLUMN_MARK_TYPE,
                AssignmentEntry.COLUMN_DATE,
                AssignmentEntry.COLUMN_MAX_VALUE,
                AssignmentEntry.COLUMN_NAME,
                AssignmentEntry.COLUMN_DESCRIPTION};
        String where = AssignmentEntry.COLUMN_ASSIGNMENT_ID + " = ?";
        String[] args = {Integer.toString(assignmentId)};

        Cursor cursor = db.query(AssignmentEntry.TABLE_NAME, columns, where, args,
                null, null, null);

        int markType;
        int max;
        String name;
        String description;

        /* if an existing assignment is being edited */
        if (cursor.getCount() > 0) {
            assignmentExists = true;
            invalidateOptionsMenu();
            cursor.moveToFirst();
            markType    = cursor.getInt(cursor.getColumnIndex(AssignmentEntry.COLUMN_MARK_TYPE));
            max         = cursor.getInt(cursor.getColumnIndex(AssignmentEntry.COLUMN_MAX_VALUE));
            chosenDate  = cursor.getLong(cursor.getColumnIndex(AssignmentEntry.COLUMN_DATE));
            name        = cursor.getString(cursor.getColumnIndex(AssignmentEntry.COLUMN_NAME));
            description = cursor.getString(cursor.getColumnIndex(AssignmentEntry.COLUMN_DESCRIPTION));

            titleView.setText(name);
//            Toast.makeText(NewAssignmentActivity.this,
//                    "" + markType, Toast.LENGTH_LONG).show();
//            if (!description.isEmpty()) {
                descriptionView.setText(description);
//            }
            typeSpinner.setSelection(markType);
            maximumView.setText(Integer.toString(max));
            dateButton.setText(GeneralUtils.getUtcDate(chosenDate));
        }
        cursor.close();

        ArrayAdapter typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.assignment_types, R.layout.assignment_type_spinner_item);
        typeSpinner.setAdapter(typeAdapter);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dateFragment = DatePickerFragment.newInstance(listener, v.getId());
                dateFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_assignment, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (!assignmentExists) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_assignment);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_assignment:
                saveAssignment();
                break;
            case R.id.action_delete_assignment:
                if (assignmentId != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.assignment_delete_dialog_title)
                            .setNegativeButton(R.string.confirm_dialog_negative_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteAssignment();
                                }
                            });
                    builder.create().show();
                }
        }
        return true;
    }


    private void saveAssignment() {

        /* verification: for the database, date and max value are mandatory */

        if (chosenDate == 0) {
            Toast.makeText(NewAssignmentActivity.this,
                    getString(R.string.empty_date_warning), Toast.LENGTH_LONG).show();
            return;
        }

        if (maximumView.getText().toString().isEmpty()) {
            Toast.makeText(NewAssignmentActivity.this,
                    getString(R.string.empty_maximum_warning), Toast.LENGTH_LONG).show();
            return;
        }

        String titleValue       = GeneralUtils.capitalize(titleView.getText().toString());
        int typeValue           = typeSpinner.getSelectedItemPosition();
        int maxValue            = Integer.parseInt(maximumView.getText().toString());
        String descriptionValue = descriptionView.getText().toString();

        ContentValues assignmentValues = new ContentValues();
        assignmentValues.put(AssignmentEntry.COLUMN_MARK_TYPE, typeValue);
        assignmentValues.put(AssignmentEntry.COLUMN_PERIOD_ID, mPeriod.getId());
        assignmentValues.put(AssignmentEntry.COLUMN_DATE, chosenDate);
        assignmentValues.put(AssignmentEntry.COLUMN_MAX_VALUE, maxValue);
        assignmentValues.put(AssignmentEntry.COLUMN_NAME, titleValue);
        assignmentValues.put(AssignmentEntry.COLUMN_DESCRIPTION, descriptionValue);

        Uri newAssignmentUri = null;
        if (assignmentId == 0) {
            newAssignmentUri = getContentResolver().insert(AssignmentEntry.CONTENT_URI, assignmentValues);
//        long newAssignmentId = ContentUris.parseId(newAssignmentUri);
        } else {
            getContentResolver().update(AssignmentEntry.CONTENT_URI,
                    assignmentValues,
                    AssignmentEntry.COLUMN_ASSIGNMENT_ID + "=?",
                    new String[] {Integer.toString(assignmentId)});
        }

        if (newAssignmentUri != null) {
            String warning = getString(R.string.assignment_saved_warning);
            Toast.makeText(NewAssignmentActivity.this, warning, Toast.LENGTH_LONG).show();
        }

        setResult(RESULT_OK, null);
        finish();
    }

    private void deleteAssignment() {
        if (assignmentId != 0) {

            String marksSelection       = MarkEntry.COLUMN_ASSIGNMENT_ID + "=?";
            String[] marksArgs          = {Integer.toString(assignmentId)};

            String assignmentSelection  = AssignmentEntry.COLUMN_ASSIGNMENT_ID + "=?";

            getContentResolver().delete(MarkEntry.CONTENT_URI, marksSelection, marksArgs);
            int deletedAssignmentRows = getContentResolver()
                    .delete(AssignmentEntry.CONTENT_URI, assignmentSelection, marksArgs);
            if (deletedAssignmentRows != 0) {
                Toast.makeText(NewAssignmentActivity.this,
                        R.string.assignment_deleted_warning, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NewAssignmentActivity.this,
                        R.string.assignment_not_deleted_warning, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public void getDateOnDateSet(long date, int resId) {
        chosenDate = date;
        dateButton.setText(GeneralUtils.getUtcDate(chosenDate));
    }


}
