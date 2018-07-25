package com.example.android.umclog;

import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.umclog.data.TermContract;

import java.util.HashMap;
import java.util.Map;


public class MarkFragment extends Fragment {

    Context mContext;

    /* period information */
    Period mPeriod;
    boolean showAttendance;
    int thisPageNumber;

    /* dates and assignments that are important for this page */
    static long[] theseDates;
    static Assignment[] theseAssignments;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();

        View rootView = inflater.inflate(R.layout.mark_fragment, container, false);
        RecyclerView namesView = rootView.findViewById(R.id.marks_names_n_marks_view);
        RecyclerView datesView = rootView.findViewById(R.id.mark_dates_recyclerView);

        /* extract data from Mark activity */
        Bundle args     = getArguments();
        mPeriod         = (Period)args.getSerializable(TermContract.PeriodEntry.COLUMN_PERIOD_NAME);
        thisPageNumber  = args.getInt("thisPage");

        int buttons                      = GeneralUtils.BUTTONS_PER_SCREEN;
        Map<Integer, Long> buttonsToPass = new HashMap<Integer, Long>();

        /* how many buttons will be displayed on the page depending on the number of assignments */
        long numberOfButtons = MarkActivity.pages - thisPageNumber == 1?
                MarkActivity.assignments.length % buttons : buttons;

        /* pack data in a map to be passed down to buttons adapter */
        buttonsToPass.put(Assignment.NUMBER_OF_ASSIGNMENTS_IDX, numberOfButtons);
        buttonsToPass.put(Assignment.WHAT_PAGE_ON_IDX, (long)thisPageNumber);

        /* number of button that have been there before this fragment */
        int thisFragment    = buttons * thisPageNumber;

        /* all types of assignments, strings */
        String[] types      = getResources().getStringArray(R.array.assignment_types_abbr);

        theseDates          = new long[buttons];
        theseAssignments    = new Assignment[buttons];
        String[] labels     = new String[buttons];

        for (int i = 0; i < numberOfButtons; i++) {
            theseDates[i]   = MarkActivity.assignments[thisFragment + i].getDate();
            theseAssignments[i] = MarkActivity.assignments[thisFragment + i];
            labels[i] = GeneralUtils.getShortUtcDate(theseDates[i]) + "\n\t" + types[MarkActivity.assignments[thisFragment + i].getType()];
        }

        AttendanceAdapter namesAdapter = new AttendanceAdapter(
                mContext, MarkActivity.studentNames, 0, mPeriod.getId(), buttonsToPass, showAttendance);
        LinearLayoutManager namesManager = new LinearLayoutManager(
                mContext, LinearLayoutManager.VERTICAL, false);
        namesView.setLayoutManager(namesManager);
        namesView.setAdapter(namesAdapter);

        DividerItemDecoration decoration =
                new DividerItemDecoration(mContext, namesManager.getOrientation());
        namesView.addItemDecoration(decoration);

        LinearLayoutManager datesManager = new LinearLayoutManager(
                mContext, LinearLayoutManager.HORIZONTAL, false);
        datesView.setLayoutManager(datesManager);

        DatesAdapter datesAdapter = new DatesAdapter(mContext, labels, theseAssignments);
        datesView.setAdapter(datesAdapter);

        TextView legendTextView = rootView.findViewById(R.id.legend_textView_2);
        final String legendLabel = getString(R.string.attendance_legend);
        legendTextView.setText(legendLabel);
        legendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
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

        return rootView;
    }

    private CharSequence getLegend() {
        return  getString(R.string.assignment_type_quiz_abbr)               + " - " +
                getString(R.string.assignment_type_quiz)                    + "\n"  +
                getString(R.string.assignment_type_assignment_abbr)         + " - " +
                getString(R.string.assignment_type_assignment)              + "\n"  +
                getString(R.string.assignment_type_final_exam_abbr)         + " - " +
                getString(R.string.assignment_type_final_exam)              + "\n"  +
                getString(R.string.assignment_type_homework_abbr)           + " - " +
                getString(R.string.assignment_type_homework)                + "\n"  +
                getString(R.string.assignment_type_midterm_abbr)            + " - " +
                getString(R.string.assignment_type_midterm)                 + "\n"  +
                getString(R.string.assignment_type_participation_abbr)      + " - " +
                getString(R.string.assignment_type_participation)           + "\n"  +
                getString(R.string.assignment_type_speaking_project_abbr)   + " - " +
                getString(R.string.assignment_type_speaking_project)        + "\n"  +
                getString(R.string.assignment_type_test_abbr)               + " - " +
                getString(R.string.assignment_type_test)                    + "\n"  +
                getString(R.string.assignment_type_total_abbr)              + " - " +
                getString(R.string.assignment_type_total)                   + "\n"  +
                getString(R.string.assignment_type_writing_project_abbr)    + " - " +
                getString(R.string.assignment_type_writing_project)         + "\n";
    }
}
