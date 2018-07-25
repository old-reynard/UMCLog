package com.example.android.umclog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.umclog.data.TermContract;

public class DatesAdapter extends RecyclerView.Adapter<DatesAdapter.DateViewHolder> {
    private Context mContext;
    private String[] mDates;
    private Assignment[] assignments;


    DatesAdapter(Context context, String[] dates, Assignment[] assignments) {
        mContext = context;
        mDates = dates;
        this.assignments = assignments;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.attendance_date_adapter_item, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DateViewHolder holder, int position) {
        final int thisIdx = holder.getAdapterPosition();
        holder.singleDate.setText(mDates[position]);
        if (mContext instanceof MarkActivity) {
            holder.singleDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (assignments[thisIdx] != null) {
                        Intent editAssignmentIntent = new Intent(Intent.ACTION_PICK, null, mContext, NewAssignmentActivity.class);
                        int assignmentId = assignments[thisIdx].getId();
                        editAssignmentIntent.putExtra
                                (TermContract.AssignmentEntry.COLUMN_ASSIGNMENT_ID, assignmentId);

                        Period period = ((MarkActivity) mContext).getmPeriod();
                        editAssignmentIntent.putExtra(TermContract.PeriodEntry.TABLE_NAME, period);
                        mContext.startActivity(editAssignmentIntent);
                    }
                }
            });
            holder.singleDate.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, assignments[thisIdx].getName(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDates.length;
    }

    class DateViewHolder extends RecyclerView.ViewHolder {

        TextView singleDate;

        DateViewHolder(View itemView) {
            super(itemView);
            singleDate = itemView.findViewById(R.id.single_date_text_view);
        }
    }
}
