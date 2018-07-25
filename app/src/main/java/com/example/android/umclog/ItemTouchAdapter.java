package com.example.android.umclog;

public interface ItemTouchAdapter {
    void onItemDismiss(int position);
    boolean onItemMove(int from, int to);
}
