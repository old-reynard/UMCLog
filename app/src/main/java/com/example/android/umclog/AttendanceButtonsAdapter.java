package com.example.android.umclog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.example.android.umclog.data.TermContract;
import com.example.android.umclog.data.TermContract.MarkEntry;
import com.example.android.umclog.data.TermContract.AttendanceEntry;
import com.example.android.umclog.data.TermDbHelper;

import java.util.Map;


/**
 * This adapter is a line in a list of students in both mark activity and attendance activity
 * Which activity it is, is governed by boolean showAttendance variable
 * Every line in the adapter is a textView for names and a number of buttons.
 * Buttons behave differently depending on the type of marks
 */
public class AttendanceButtonsAdapter
        extends RecyclerView.Adapter<AttendanceButtonsAdapter.AttendanceButtonsViewHolder> {

    /* activity */
    private Context mContext;

    /* what kind of lesson that is, possibly redundant variable */
    private int mPeriodType;

    /* current student id, needed to pass down to buttons */
    private int thisStudentId;

    /* current period id, needed for SQL query */
    private int thisPeriodId;

    /* dates for the current week, if it's attendance, or number of buttons if it's marks */
    private Map<Integer, Long> thisWeek;

    /* says if it's attendance or marks */
    private boolean showAttendance;

    private TermDbHelper termDbHelper;
    private Resources mResources;

    /* for SQL queries */
    private static final String COMMA = ", ";
    private static final String EQUALS = " = ";
    private static final String AND = " and ";


    AttendanceButtonsAdapter (Context context, int periodType, int studentId, int periodId,
                              Map<Integer, Long> week, boolean showAttendance) {
        mContext = context;
        mPeriodType = periodType;
        thisStudentId = studentId;
        thisPeriodId = periodId;
        thisWeek = week;
        termDbHelper = new TermDbHelper(mContext);
        mResources = mContext.getResources();
        this.showAttendance = showAttendance;
    }

    @NonNull
    @Override
    public AttendanceButtonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.attendance_buttons_item, parent, false);

        return new AttendanceButtonsViewHolder(view);
    }

    /**
     * Queries the database and updates the string values on the buttons. Also hides attendance
     * buttons for those day without attached dates
     * @param holder buttons
     * @param position day of the week
     */
    @Override
    public void onBindViewHolder(@NonNull AttendanceButtonsViewHolder holder, int position) {
        SQLiteDatabase db = termDbHelper.getWritableDatabase();

        long date;

        /* if this happens for attendance */
        if (showAttendance) {
            /* extract current date from the map */
            if (thisWeek != null && thisWeek.containsKey(holder.getAdapterPosition() + 2)) {
                date = thisWeek.get(holder.getAdapterPosition() + 2);
            } else date = 0;
            /* hide the button if this date is outside the term */
            if (date == 0) {
                holder.attendanceButton.setVisibility(View.INVISIBLE);
                return;
            }

            // TODO replace that with getContentResolverCall
            String SQL_ATTENDANCE_REQUEST = "SELECT " +
                    AttendanceEntry.COLUMN_ATTENDANCE_MARK      + " FROM "  +
                    AttendanceEntry.TABLE_NAME                  + " WHERE " +
                    AttendanceEntry.COLUMN_ATTENDANCE_DATE      + EQUALS    + "?"      + AND +
                    AttendanceEntry.COLUMN_PERIOD_ID            + EQUALS    + "?"      + AND +
                    AttendanceEntry.COLUMN_STUDENT_ID           + EQUALS    + "?";
            String[] selectionArgs = {Long.toString(date), Integer.toString(thisPeriodId),
                    Integer.toString(thisStudentId)};
            Cursor cursor = db.rawQuery(SQL_ATTENDANCE_REQUEST, selectionArgs);
            cursor.moveToFirst();

            /* process the returned cursor */
            int initialMark;
            if (cursor.getCount() > 0) {
                /* if the cursor is not empty, extract the mark from the cursor */
                initialMark = cursor.getInt(cursor.getColumnIndex(AttendanceEntry.COLUMN_ATTENDANCE_MARK));
                holder.attendanceButton.setText(getLabelFromMark(initialMark));
            }
            cursor.close();

        /* in the other case, if this happens for marks */
        } else {

            int thisPage = thisWeek.get(Assignment.WHAT_PAGE_ON_IDX).intValue();
            int assignmentId = MarkActivity.assignments[thisPage * GeneralUtils.BUTTONS_PER_SCREEN +
                    holder.getAdapterPosition()].getId();

            String SQL_MARK_REQUEST = "SELECT "         +
                    MarkEntry.COLUMN_MARK_VALUE         + " FROM "  +
                    MarkEntry.TABLE_NAME                + " WHERE " +
                    MarkEntry.COLUMN_STUDENT_ID         + EQUALS    + "?" + AND +
                    MarkEntry.COLUMN_ASSIGNMENT_ID      + EQUALS    + "?";
            String[] where = {Integer.toString(thisStudentId), Integer.toString(assignmentId)};
            Cursor cursor = db.rawQuery(SQL_MARK_REQUEST, where);
            cursor.moveToFirst();

            /* process the returned cursor */
            int assignmentMark;
            if (cursor.getCount() > 0) {
                assignmentMark = cursor.getInt(cursor.getColumnIndex(MarkEntry.COLUMN_MARK_VALUE));
                holder.attendanceButton.setText(Integer.toString(assignmentMark));
            } else {
                holder.attendanceButton.setText(R.string.attendance_empty_label);
            }
            cursor.close();
        }
        db.close();
    }


    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        termDbHelper.close();
    }

    @Override
    public int getItemCount() {
        if (showAttendance) {
            return Period.getNumberOfLessonsPerWeek(mPeriodType);
        } else {
            return thisWeek.get(Assignment.NUMBER_OF_ASSIGNMENTS_IDX).intValue();
        }
    }


    class AttendanceButtonsViewHolder extends RecyclerView.ViewHolder {

        Button attendanceButton;

        /**
         * According to the value on it, the button saves or deletes the attendance mark from db
         * @param itemView attendance button
         */
        AttendanceButtonsViewHolder(View itemView) {

            super(itemView);
            attendanceButton = itemView.findViewById(R.id.attendance_button);
            if (showAttendance) {

                final String[] labels = mResources.getStringArray(R.array.attendance_buttons_labels);
                final Value<Integer> idx = new Value<>(0);

                attendanceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (idx.getVal() == labels.length) {
                            idx.setVal(0);
                        }
                        attendanceButton.setText(labels[idx.getVal()]);
                        idx.setVal(idx.getVal() + 1);
                        long attendanceDate;
                        if (thisWeek.containsKey(getAdapterPosition() + 2)) {
                            attendanceDate = thisWeek.get(getAdapterPosition() + 2);
                        } else attendanceDate = 0;

                        if (attendanceDate != 0) {
                            String markString = attendanceButton.getText().toString();
                            int mark = getMarkFromLabel(markString);

                            String SQL_ATTENDANCE_MARK;
                            if (markString.equals(mResources.getString(R.string.attendance_empty_label))) {
                                SQL_ATTENDANCE_MARK = "DELETE FROM " +
                                        AttendanceEntry.TABLE_NAME                              + " WHERE " +
                                        AttendanceEntry.COLUMN_ATTENDANCE_ID                    + EQUALS +
                                        "(SELECT  " +  AttendanceEntry.COLUMN_ATTENDANCE_ID     + " FROM " +
                                        AttendanceEntry.TABLE_NAME                              + " WHERE " +
                                        AttendanceEntry.COLUMN_ATTENDANCE_DATE  + EQUALS + attendanceDate   + AND +
                                        AttendanceEntry.COLUMN_PERIOD_ID        + EQUALS + thisPeriodId     + AND +
                                        AttendanceEntry.COLUMN_STUDENT_ID       + EQUALS + thisStudentId    + ");";
                            } else {
                                SQL_ATTENDANCE_MARK = "INSERT OR REPLACE INTO " + AttendanceEntry.TABLE_NAME + " (" +
                                        AttendanceEntry.COLUMN_ATTENDANCE_ID                        + COMMA +
                                        AttendanceEntry.COLUMN_ATTENDANCE_DATE                      + COMMA +
                                        AttendanceEntry.COLUMN_ATTENDANCE_MARK                      + COMMA +
                                        AttendanceEntry.COLUMN_PERIOD_ID                            + COMMA +
                                        AttendanceEntry.COLUMN_STUDENT_ID       + ") VALUES (" + "(SELECT " +
                                        AttendanceEntry.COLUMN_ATTENDANCE_ID                     + " FROM " +
                                        AttendanceEntry.TABLE_NAME +                              " WHERE " +
                                        AttendanceEntry.COLUMN_ATTENDANCE_DATE + EQUALS + attendanceDate + AND +
                                        AttendanceEntry.COLUMN_PERIOD_ID + EQUALS + thisPeriodId + AND +
                                        AttendanceEntry.COLUMN_STUDENT_ID + EQUALS + thisStudentId + "), "  +
                                        attendanceDate                                              + COMMA +
                                        mark                                                        + COMMA +
                                        thisPeriodId                                                + COMMA +
                                        thisStudentId + ");";
                            }

                            SQLiteDatabase db = termDbHelper.getWritableDatabase();
                            db.execSQL(SQL_ATTENDANCE_MARK);
                            db.close();
                        }

                    }
                });
            } else {
                attendanceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    Intent newMarkIntent = new Intent(mContext, GiveMarkActivity.class);
                    newMarkIntent.putExtra(TermContract.StudentsEntry.STUDENT_ID, thisStudentId);
                    int page = thisWeek.get(Assignment.WHAT_PAGE_ON_IDX).intValue();
                    newMarkIntent.putExtra("thisPage", page);
                    newMarkIntent.putExtra("position", getAdapterPosition());
                    mContext.startActivity(newMarkIntent);

                    }
                });
            }
        }
    }


    /**
     * After harvesting the string value from the attendance button, converts it to a digital value
     * @param label harvested value from attendance button
     * @return digital representation of attendance type
     */
    private int getMarkFromLabel (String label) {
        AttendanceType mark;

        if (label.equals(mResources.getString(R.string.attendance_present_abbr))) {
            mark = AttendanceType.PRESENT;
        } else if (label.equals(mResources.getString(R.string.attendance_absent_abbr))) {
            mark = AttendanceType.ABSENT;
        } else if (label.equals(mResources.getString(R.string.attendance_absent_we_abbr))) {
            mark = AttendanceType.ABSENT_WITH_EXCUSE;
        } else if (label.equals(mResources.getString(R.string.attendance_late_abbr))) {
            mark = AttendanceType.LATE;
        } else if (label.equals(mResources.getString(R.string.attendance_late_we_abbr))) {
            mark = AttendanceType.LATE_WITH_EXCUSE;
        } else mark = AttendanceType.EMPTY;

        return mark.getType();
    }

    /**
     * Converts attendance type to its string representation
     * @param mark attendance mark value returned from database
     * @return string representation of abbreviated attendance mark
     */
    private String getLabelFromMark(int mark) {
        switch (mark) {
            case 1: return mResources.getString(R.string.attendance_present_abbr);
            case 2: return mResources.getString(R.string.attendance_absent_abbr);
            case 3: return mResources.getString(R.string.attendance_absent_we_abbr);
            case 4: return mResources.getString(R.string.attendance_late_abbr);
            case 5: return mResources.getString(R.string.attendance_late_we_abbr);
            default: return mResources.getString(R.string.attendance_empty_label);
        }
    }
}
