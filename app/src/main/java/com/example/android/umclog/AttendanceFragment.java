package com.example.android.umclog;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.android.umclog.data.TermContract.TermEntry;
import com.example.android.umclog.data.TermContract.StudentsEntry;
import com.example.android.umclog.data.TermContract.StudentToPeriodEntry;
import com.example.android.umclog.data.TermDbHelper;

public class AttendanceFragment extends Fragment {

    int thisPeriodId;
    int thisTermId;
    int thisPeriodLevel;
    int thisPeriodType;
    int thisWeek;
    boolean showAttendance;

    TermDbHelper termDbHelper;
    long start;
    long end;


    List<Student> mStudents;
    List<Long> mDates;

    SQLiteDatabase db;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final Activity activity = getActivity();
        termDbHelper    = new TermDbHelper(activity);

        /* extract parameters passed down from activity */
        Bundle args = getArguments();
        thisPeriodId    = args.getInt("periodId");
        thisTermId      = args.getInt("termId");
        thisPeriodLevel = args.getInt("periodLevel");
        thisPeriodType  = args.getInt("periodType");
        thisWeek        = args.getInt("week");
        showAttendance  = true;

        db              = termDbHelper.getReadableDatabase();

        mStudents       = GeneralUtils.getStudentNames(db, thisPeriodId, thisPeriodLevel);
        getDates();
        ArrayList<Map<Integer, Long>> dateLabels = populateWeekLabels();

        Map<Integer, Long> week = dateLabels.get(thisWeek);

        String[] datesToHang = new String[GeneralUtils.BUTTONS_PER_SCREEN];
        for (Map.Entry<Integer, Long> day: week.entrySet()) {
            datesToHang[day.getKey()-2] = GeneralUtils.getShortUtcDate(day.getValue());
        }

        for (String a:datesToHang) if (a == null) a = " ";

        View rootView = inflater.inflate(R.layout.attendance_week_fragment, container, false);
        RecyclerView mRecyclerView = rootView.findViewById(R.id.attendance_names_n_marks_view);
        RecyclerView datesRecyclerView = rootView.findViewById(R.id.dates_recyclerView);


        TextView legendTextView = rootView.findViewById(R.id.legend_textView);
        final String legendLabel = getString(R.string.attendance_legend);
        legendTextView.setText(legendLabel);
        legendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                        .setTitle(legendLabel)
                        .setMessage(getLegend())
                        .setPositiveButton(getString(R.string.ok_label), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });

        /* populate the dates view */
        LinearLayoutManager datesManager =
                new LinearLayoutManager(activity,
                        LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(datesManager);
        DatesAdapter datesAdapter = new DatesAdapter(activity, datesToHang, null);
        datesRecyclerView.setAdapter(datesAdapter);

        /* populate the names view */
        LinearLayoutManager manager = new LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration decoration =
                new DividerItemDecoration(activity, manager.getOrientation());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(decoration);

        AttendanceAdapter adapter =
                new AttendanceAdapter(activity, mStudents, thisPeriodType, thisPeriodId, week, showAttendance);
        mRecyclerView.setAdapter(adapter);

        db.close();
        termDbHelper.close();

        return rootView;
    }


    /**
     * queries the dates from the database
     */
    public void getDates() {
        //select start_date, end_date from terms where terms._id=thisTermId

        String[] projection = {TermEntry.COLUMN_START_DATE, TermEntry.COLUMN_END_DATE};
        String selection = TermEntry.TERM_ID + "=?";
        String arg = Integer.toString(thisTermId);
        String[] selectionArgs = {arg};

        Cursor termCursor = db.query(TermEntry.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);

        termCursor.moveToFirst();

        /* extract start and end dates from database */
        start = termCursor.getLong(termCursor.getColumnIndex(TermEntry.COLUMN_START_DATE));
        end = termCursor.getLong(termCursor.getColumnIndex(TermEntry.COLUMN_END_DATE));

        //      String days = new SimpleDateFormat("EE").format(start);

        /* store all the dates in the term in a list */
        mDates      = new ArrayList<>();
        /* for the for loop that will select all the learning dates in the term */
        Date startTermDate = new Date(start);
        Date endTermDate = new Date(end);

        int oneDay = 24 * 60 * 60 * 1000;
        long startCounter = start;
        Calendar c = Calendar.getInstance();
        for (c.setTime(startTermDate); c.getTime().before(endTermDate); c.add(Calendar.DATE, 1)) {

            int today = c.get(Calendar.DAY_OF_WEEK);
            /* if this day is not Sunday or Saturday, add it to the list */
            if (today != Calendar.SATURDAY && today != Calendar.SUNDAY) {
                mDates.add(startCounter);
            }
            startCounter += oneDay;
        }
        termCursor.close();
    }

    public ArrayList<Map<Integer, Long>> populateWeekLabels() {
        ArrayList<Map<Integer, Long>> termMap = new ArrayList<>();
        if (mDates != null) {
            Calendar c = Calendar.getInstance();
            termMap.add(new HashMap());
            int idx = 0;
            for (int i = 0; i < mDates.size(); i++) {
                long day = mDates.get(i);

                c.setTime(new Date(day));
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

                if (termMap.size() <= idx) {
                    termMap.add(new HashMap());
                }
                termMap.get(idx).put(dayOfWeek, day);

                if (dayOfWeek == Calendar.FRIDAY) {
                    idx++;
                }
            }
        }
        return termMap;
    }

    private CharSequence getLegend() {
        return  getString(R.string.attendance_present_abbr)         + " - " +
                getString(R.string.attendance_present)      + "\n"  +
                getString(R.string.attendance_absent_abbr)          + " - " +
                getString(R.string.attendance_absent)       + "\n"  +
                getString(R.string.attendance_absent_we_abbr)       + " - " +
                getString(R.string.attendance_absent_we)    + "\n"  +
                getString(R.string.attendance_late_abbr)            + " - " +
                getString(R.string.attendance_late)         + "\n"  +
                getString(R.string.attendance_late_we_abbr)         + " - " +
                getString(R.string.attendance_late_we);
    }
}
