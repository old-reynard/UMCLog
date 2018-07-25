package com.example.android.umclog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class CreateTermAdapter extends RecyclerView.Adapter<CreateTermAdapter.CreateTermAdapterViewHolder>
        implements ItemTouchAdapter {

    private Context mContext;
    private String[] allPeriods;
    private LinkedList<Period> mPeriods;
    private OnTermHolderListener onTermHolderListener;

    /**
     * Adapter is used in CreateTermActivity and populated with separate periods data
     * @param context needed to communicate with the activity
     * @param listener OnTermHolderListener used to pass data from views to activity
     */
    public CreateTermAdapter(Context context, LinkedList<Period> periods, OnTermHolderListener listener) {
        mContext = context;
        mPeriods = periods;
        onTermHolderListener = listener;
        allPeriods = mContext.getResources().getStringArray(R.array.all_periods);
    }

    @NonNull
    @Override
    public CreateTermAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.create_term_period_item, parent, false);
        return new CreateTermAdapterViewHolder(view);
    }

    @Override
    public void onItemDismiss(int position) {
        mPeriods.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(mPeriods, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(mPeriods, i, i - 1);
            }
        }
        notifyItemMoved(from, to);
        return true;
    }

    /**
     * Used to pass data from the adapter to CreateTermActivity
     */
    public interface OnTermHolderListener {
        /**
         * called from CreateTermActivity
         * @param periods passed to CreateTermActivity with all contents of the adapter
         */
        void onClick(LinkedList<Period> periods);
    }

    private void notifyTermAdapterChanges() {
        if (onTermHolderListener != null) {
            onTermHolderListener.onClick(mPeriods);
        }
    }

    /**
     * Used to populate the CreateTermAdapterViewHolder with views. Updates text in those views
     * @param holder current CreateTermAdapterViewHolder
     * @param position position in this holder. Used to setup the spinner from the ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull final CreateTermAdapterViewHolder holder, final int position) {

//        String a = "";
//        for (Period p: mPeriods) a += p.getName() + " " + p.getLevel() +  "\n";
//        Toast.makeText(mContext, a, Toast.LENGTH_SHORT).show();

        holder.mToLevelTextView.setText(R.string.to_level);
        holder.mAddStudentsButton.setText(R.string.add_students_button);

        String thisPeriod   = mPeriods.get(holder.getAdapterPosition()).getName();
        holder.mChoosePeriodSpinner.setSelection(GeneralUtils.find(allPeriods, thisPeriod));
        int thisLevel       = mPeriods.get(holder.getAdapterPosition()).getLevel();
        holder.mLevelSpinner.setSelection(thisLevel - 1);


        /* setup the period spinner to extract the current period */
        holder.mChoosePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = (String)parent.getItemAtPosition(position);
                mPeriods.get(holder.getAdapterPosition()).setName(selectedPeriod);
                notifyTermAdapterChanges();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                String selectedPeriod = parent.getSelectedItem().toString();
                mPeriods.get(holder.getAdapterPosition()).setName(selectedPeriod);
                notifyTermAdapterChanges();
            }
        });

        /* setup the level spinner to extract the current level */
        holder.mLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int currentLevel = Integer.parseInt(((String)parent.getItemAtPosition(position)));
                Period currentPeriod = mPeriods.get(holder.getAdapterPosition());
                currentPeriod.setLevel(currentLevel);
                setLevelsToStudents(currentPeriod, currentLevel);
                notifyTermAdapterChanges();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                int currentLevel = (Integer.parseInt((String)parent.getSelectedItem()));
                mPeriods.get(holder.getAdapterPosition()).setLevel(currentLevel);
                notifyTermAdapterChanges();
            }
        });

        /* setup the Add students button to extract student names from the dialog window */
        holder.mAddStudentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputField = new EditText(mContext);
                inputField.setSingleLine(false);
                inputField.setLines(9);
                inputField.setGravity(Gravity.START | Gravity.TOP);

                ArrayList<Student> students = mPeriods.get(holder.getAdapterPosition()).getStudents();
                if (students != null) {
                    StringBuilder displayableNames = new StringBuilder();
                    for (Student s: students) {
                        displayableNames.append(s.getName());
                        displayableNames.append("\n");
                    }
                    inputField.setText(displayableNames);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setTitle(R.string.add_students_button)
                    .setMessage(R.string.add_students_dialog_message)
                    .setPositiveButton(R.string.confirm_students,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = inputField.getText().toString();
                                    if (!input.isEmpty()) {
                                        ArrayList<String> names = GeneralUtils.getNamesFromOutside(input);
                                        ArrayList<Student> studentsToAdd = new ArrayList<>();
                                        for (String name: names) studentsToAdd.add(new Student(name));
                                        mPeriods.get(holder.getAdapterPosition())
                                                .setStudents(studentsToAdd);
                                        String s = "";
                                        ArrayList<Student> ss = mPeriods.get(holder.getAdapterPosition()).getStudents();
                                        StringBuilder studentNamesToast = new StringBuilder();
                                        for (Student st: ss) {
                                            studentNamesToast.append(st.getName() + " " + "\n");
                                        }
                                        Toast.makeText(mContext, studentNamesToast, Toast.LENGTH_SHORT).show();
                                    } else {
                                        String warning = mContext.getResources().
                                                getString(R.string.warning_add_students);
                                        Toast.makeText(mContext, warning, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    .setNegativeButton(R.string.discard_students,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                            })
                    .setView(inputField);
                builder.create().show();
            }
        });
        notifyTermAdapterChanges();
    }

    /**
     * @return the default number of periods per day
     */
    @Override
    public int getItemCount() {
        return mPeriods.size();
    }


    /**
     * Custom ViewHolder meant to accommodate a spinner, a small TextView and two buttons - one to
     * choose the class level and another - to add students to the database
     */
    class CreateTermAdapterViewHolder extends RecyclerView.ViewHolder {
        Spinner mChoosePeriodSpinner;
        TextView mToLevelTextView;
        Button mAddStudentsButton;
        Spinner mLevelSpinner;

        private CreateTermAdapterViewHolder(View view) {
            super(view);

            /* initialize the views inside the ViewHolder*/
            mChoosePeriodSpinner =(Spinner)view.findViewById(R.id.period_spinner);
            mToLevelTextView = (TextView)view.findViewById(R.id.to_level_textview);
            mAddStudentsButton = (Button)view.findViewById(R.id.add_students_button);
            mLevelSpinner = (Spinner)view.findViewById(R.id.choose_level_spinner);
            setupSpinner();
            setupLevelSpinner();

        }

        private void setupSpinner() {
            ArrayAdapter periodAdapter = ArrayAdapter.createFromResource(mContext,
                    R.array.all_periods, R.layout.period_adapter_dropdown_item);
            periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mChoosePeriodSpinner.setAdapter(periodAdapter);
        }

        private void setupLevelSpinner() {
            ArrayAdapter levelAdapter = ArrayAdapter.createFromResource(mContext,
                    R.array.choose_level, R.layout.level_adapter_dropdown_item);
            levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mLevelSpinner.setAdapter(levelAdapter);
            mLevelSpinner.setSelection(6);
        }
    }

    private void setLevelsToStudents(Period period, int level) {
        ArrayList<Student> students = period.getStudents();
        if (students != null) {
            for (Student student:students) {
                student.setLevel(level);
            }
        }
        period.setStudents(students);
    }
}
