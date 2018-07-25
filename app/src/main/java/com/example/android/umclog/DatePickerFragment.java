package com.example.android.umclog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 *  Used to set up starting and ending dates of the term
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    /* Used to pass the chosen date to the activity */
    DatePickerFragmentListener datePickerFragmentListener;
    /* Used to pass the button id through this class */
    int resId;
    /* interface for the listener that passes the picked date to the activity */
    public interface DatePickerFragmentListener {
        void getDateOnDateSet(long date, int resId);
    }
    /* getter for the listener */
    public DatePickerFragmentListener getDatePickerFragmentListener() {
        return datePickerFragmentListener;
    }
    /* setter for the listener */
    public void setDatePickerFragmentListener(DatePickerFragmentListener listener, int resId) {
        this.datePickerFragmentListener = listener;
        this.resId = resId;
    }
    /* Used in OnDateSet to pass the date in millis */
    protected void notifyDatePicketListener (long date, int resId) {
        if (this.datePickerFragmentListener != null) {
            this.datePickerFragmentListener.getDateOnDateSet(date, resId);
        }
    }

    /**
     * Fragment constructors must not take any arguments, so this fragment has an alternative one
     * @param listener to initialize the OnDateSetListener
     * @return DatePickerFragment
     */
    public static DatePickerFragment newInstance(DatePickerFragmentListener listener, int resId) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setDatePickerFragmentListener(listener, resId);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    /**
     * Notifies the listener in the activity that the date has been set
     * @param view picker
     * @param year selected year
     * @param month selected month
     * @param dayOfMonth selected day
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        long timeInMillis = calendar.getTimeInMillis();

        notifyDatePicketListener(timeInMillis, resId);
    }
}