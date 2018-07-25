package com.example.android.umclog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.android.umclog.data.TermContract;

public class MarkFragmentAdapter extends FragmentStatePagerAdapter {

    private Period mPeriod;


    MarkFragmentAdapter(FragmentManager fm, Period period) {
        super(fm);
        mPeriod = period;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment markFragment = new MarkFragment();
        Bundle args = new Bundle();
        args.putSerializable(TermContract.PeriodEntry.COLUMN_PERIOD_NAME, mPeriod);
        args.putInt("thisPage", position);
        markFragment.setArguments(args);
        return markFragment;
    }

    @Override
    public int getCount() {
        return MarkActivity.pages;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "page " + (position + 1);
    }
}
