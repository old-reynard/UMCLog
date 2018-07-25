package com.example.android.umclog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.android.umclog.data.TermContract;

import java.util.List;

/**
 * Used to store buttons in TermActivity
 */
public class ChoosePeriodAdapter extends RecyclerView.Adapter<ChoosePeriodAdapter.ChoosePeriodViewHolder> {

    private Context mContext;
    private List<Period> mPeriods;
    private int termId;

    ChoosePeriodAdapter(Context context, List<Period> periods, int termId) {
        mContext = context;
        mPeriods = periods;
        this.termId = termId;
    }


    @NonNull
    @Override
    public ChoosePeriodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.choose_period_item, parent, false);
        return new ChoosePeriodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChoosePeriodViewHolder holder, final int position) {
        final Period thisPeriod = mPeriods.get(position);
        holder.mChooseButton.setText(thisPeriod.getName());
        holder.mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openMarksIntent;
                if (TermActivity.showAttendance) {
                    openMarksIntent = new Intent(mContext, AttendanceActivity.class);
                } else {
                    openMarksIntent = new Intent(mContext, MarkActivity.class);
                }
                openMarksIntent.putExtra(TermContract.PeriodEntry.COLUMN_PERIOD_NAME, thisPeriod);
                openMarksIntent.putExtra("periodNumber", holder.getAdapterPosition() + 1);
                openMarksIntent.putExtra(TermContract.TermEntry.TERM_ID, termId);
                mContext.startActivity(openMarksIntent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mPeriods.size();
    }

    class ChoosePeriodViewHolder extends RecyclerView.ViewHolder {

        Button mChooseButton;

        ChoosePeriodViewHolder(View itemView) {
            super(itemView);

            mChooseButton = itemView.findViewById(R.id.choose_period_button);
        }
    }


}
