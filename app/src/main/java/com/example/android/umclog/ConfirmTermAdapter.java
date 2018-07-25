package com.example.android.umclog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

public class ConfirmTermAdapter extends RecyclerView.Adapter<ConfirmTermAdapter.ConfirmTermAdapterViewHolder> {
    private LinkedList<Period> mPeriods;
    private Context mContext;

    ConfirmTermAdapter(Context context, LinkedList<Period> periods) {
        mContext = context;
        mPeriods = periods;
    }

    @NonNull
    @Override
    public ConfirmTermAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.confirm_term_dialog_item, parent, false);
        return new ConfirmTermAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmTermAdapterViewHolder holder, int position) {
        if (mPeriods != null) {
            Period thisPeriod = mPeriods.get(position);
            String periodName = thisPeriod.getName();
            holder.period.setText(periodName);

            String level = Integer.toString(thisPeriod.getLevel());
            holder.level.setText(level);

            ArrayList<Student> students = thisPeriod.getStudents();
            StringBuilder names = new StringBuilder();
            if (students != null) {
                for (Student s: students) {
                    if (s != null) {
                        names.append(s.getName());
                        names.append("\n");
                    }
                }
            }
            if (!names.toString().equals("")) {
                holder.students.setText(names.toString());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPeriods.size();
    }

    class ConfirmTermAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView period;
        TextView level;
        TextView students;

        ConfirmTermAdapterViewHolder(View itemView) {
            super(itemView);

            period = itemView.findViewById(R.id.confirm_term_period_view);
            level = itemView.findViewById(R.id.confirm_term_level_view);
            students = itemView.findViewById(R.id.confirm_term_students_view);
        }
    }
}
