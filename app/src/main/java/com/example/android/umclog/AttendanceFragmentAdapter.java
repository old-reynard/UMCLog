package com.example.android.umclog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import android.widget.Toast;

public class AttendanceFragmentAdapter extends FragmentStatePagerAdapter {

    private int periodId;
    private int termId;
    private int periodLevel;
    private int periodType;
    private boolean showAttendance;

    /**
     * Fragment pager adapter that will populate Attendance Activity with Attendance fragments
     * @param fm fragment manager
     * @param periodId current period id for db queries from fragment
     * @param termId current tm id for db queries from fragment
     * @param periodLevel this period level for Student instances
     * @param periodType indicator of period type so that fragment knows what period it's handling
     */
    AttendanceFragmentAdapter(FragmentManager fm,
                              int periodId, int termId, int periodLevel, int periodType, boolean showAttendance) {
        super(fm);
        this.periodId= periodId;
        this.termId= termId;
        this.periodLevel= periodLevel;
        this.periodType = periodType;
        this.showAttendance = showAttendance;
    }

    @Override
    public Fragment getItem(int position) {
        AttendanceFragment fragment = new AttendanceFragment();
        Bundle args = new Bundle();
        args.putInt("periodId", periodId);
        args.putInt("termId", termId);
        args.putInt("periodLevel", periodLevel);
        args.putInt("periodType", periodType);
        args.putInt("week", position);
        args.putBoolean("showAttendance", showAttendance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return Term.NUMBER_OF_WEEKS;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "Week " + (position + 1);
    }

}
