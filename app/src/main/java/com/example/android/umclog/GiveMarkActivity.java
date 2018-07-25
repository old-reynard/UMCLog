package com.example.android.umclog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.umclog.data.TermContract;
import com.example.android.umclog.data.TermContract.MarkEntry;
import com.example.android.umclog.data.TermDbHelper;

public class GiveMarkActivity extends AppCompatActivity {

    EditText markEditText;

    private static final String COMMA = ", ";
    private static final String EQUALS = " = ";
    private static final String AND = " AND ";

    private boolean markExists = false;

    TermDbHelper helper;
    SQLiteDatabase db;

    private int markId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_mark);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);      // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }

        helper = new TermDbHelper(GiveMarkActivity.this);
        db = helper.getWritableDatabase();

        Intent markIntent = getIntent();
        final int studentId = markIntent.getIntExtra(TermContract.StudentsEntry.STUDENT_ID, 0);
        int page = markIntent.getIntExtra("thisPage", 0);
        int position = markIntent.getIntExtra("position", 0);

        /* getting student name */
        final Value<String> name = new Value<>();
        for (int i = 0; i < MarkActivity.studentNames.size(); i++) {
            if (studentId == MarkActivity.studentNames.get(i).getStudentId()) {
                name.setVal(MarkActivity.studentNames.get(i).getName());
            }
        }

        /* getting assignment details */
        int idx = GeneralUtils.BUTTONS_PER_SCREEN * page + position;
        String type = GeneralUtils.getAssignmentType(getResources(), MarkActivity.assignments[idx].getType());
        final String assignment = MarkActivity.assignments[idx].getName().isEmpty() ?
                type: MarkActivity.assignments[idx].getName() + " (" + type + ")";
        final int max = MarkActivity.assignments[idx].getMaxValue();
        final int assignmentId = MarkActivity.assignments[idx].getId();
        CharSequence hint = getString(R.string.out_of_hint) + " " + max;

        String markExistsQuery = "Select "          +
                MarkEntry.COLUMN_MARK_ID            + COMMA     +
                MarkEntry.COLUMN_MARK_VALUE         + " FROM "  +
                MarkEntry.TABLE_NAME                + " WHERE " +
                MarkEntry.COLUMN_ASSIGNMENT_ID      + EQUALS    + "?" + AND +
                MarkEntry.COLUMN_STUDENT_ID         + EQUALS    + "?";
        String[] where = {Integer.toString(assignmentId), Integer.toString(studentId)};

        String returnedValue = null;
        Cursor cursor = db.rawQuery(markExistsQuery, where);
        if (cursor.getCount() != 0) {
            markExists = true;
            cursor.moveToFirst();
            markId = cursor.getInt(cursor.getColumnIndex(MarkEntry.COLUMN_MARK_ID));
            returnedValue = cursor.getString(cursor.getColumnIndex(MarkEntry.COLUMN_MARK_VALUE));
        }
        cursor.close();


        TextView nameTextView = findViewById(R.id.mark_name_text_view);
        markEditText = findViewById(R.id.new_mark_editText);
        if (markExists) {
            markEditText.setText(returnedValue);
            setTitle(R.string.set_title_if_mark_exists);
        }
        TextView assignmentTextView = findViewById(R.id.chosen_assignment_textView);
        FloatingActionButton fab = findViewById(R.id.save_mark_button);
        FloatingActionButton deleteFab = findViewById(R.id.delete_mark_button);
        deleteFab.setBackgroundTintList(ColorStateList.valueOf
                (getResources().getColor(R.color.lightPrimaryColor, null)));

        nameTextView.setText(name.getVal());
        assignmentTextView.setText(assignment);
        markEditText.setHint(hint);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mark;
                if (markEditText.getText().toString().isEmpty()) {
                    Toast.makeText(GiveMarkActivity.this,
                            getString(R.string.empty_value_warning), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mark = Integer.parseInt(markEditText.getText().toString());
                    if (mark > max) {
                        Toast.makeText(GiveMarkActivity.this, getString(R.string.above_max_warning)
                                + " " + max, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                String SQL_MARK = "INSERT OR REPLACE INTO " + MarkEntry.TABLE_NAME + " (" +
                        MarkEntry.COLUMN_MARK_ID        + COMMA +
                        MarkEntry.COLUMN_MARK_VALUE     + COMMA +
                        MarkEntry.COLUMN_ASSIGNMENT_ID  + COMMA +
                        MarkEntry.COLUMN_STUDENT_ID     + ") VALUES (" + "(SELECT " +
                        MarkEntry.COLUMN_MARK_ID        + " FROM " +
                        MarkEntry.TABLE_NAME            + " WHERE " +
                        MarkEntry.COLUMN_STUDENT_ID     + EQUALS + studentId + AND +
                        MarkEntry.COLUMN_ASSIGNMENT_ID  + EQUALS + assignmentId + ") " + COMMA +
                        mark + COMMA + assignmentId + COMMA + studentId + ");";

                db.execSQL(SQL_MARK);

                finish();
            }
        });

        if (markId == 0) {
            deleteFab.setVisibility(View.GONE);
        }

        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(GiveMarkActivity.this);
                builder.setTitle(R.string.delete_this_mark_dialog_title)
                        .setNegativeButton(R.string.confirm_dialog_negative_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String whereClause = MarkEntry.COLUMN_MARK_ID + EQUALS + "?";
                                String[] args = {Integer.toString(markId)};
                                db.delete(MarkEntry.TABLE_NAME, whereClause, args);
                                finish();
                            }
                        });
                builder.create().show();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
            }
        });
    }

}
