package com.example.android.umclog;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class AttendanceAdapter extends Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private Context mContext;
    private List<Student> mStudents;
    private int mPeriodType;
    private int mPeriodId;
    private Map<Integer, Long> mWeek;
    private boolean showAttendance;

    AttendanceAdapter(Context context, List<Student> students, int periodType,
                      int periodId, Map<Integer, Long> week, boolean showAttendance) {
        mContext = context;
        mStudents = students;
        mPeriodType = periodType;
        mPeriodId = periodId;
        mWeek = week;
        this.showAttendance = showAttendance;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.attendance_student_item, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {

        /* find this student id to pass it to buttons for sql query */
        int mStudentId = mStudents.get(holder.getAdapterPosition()).getStudentId();

        /* set the name of the student in the list */
        holder.nameView.setText(mStudents.get(position).getName());


        LinearLayoutManager manager = new LinearLayoutManager(
                    mContext, LinearLayoutManager.HORIZONTAL, false);

        holder.buttonsView.setLayoutManager(manager);

        AttendanceButtonsAdapter adapter = new AttendanceButtonsAdapter(
            mContext, mPeriodType, mStudentId, mPeriodId, mWeek, showAttendance);

        holder.buttonsView.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return mStudents.size();
    }

    class AttendanceViewHolder extends RecyclerView.ViewHolder {

        private TextView nameView;
        private RecyclerView buttonsView;

        AttendanceViewHolder(View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.name_text_view);
            nameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Student s = mStudents.get(getAdapterPosition());
                    GeneralUtils.addOrEditStudent(mContext, s, null, true);
                }
            });


            buttonsView = itemView.findViewById(R.id.attendance_buttons_recycler_view);
        }
    }
}
