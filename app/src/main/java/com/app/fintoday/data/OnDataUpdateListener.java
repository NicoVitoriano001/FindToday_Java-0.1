package com.app.fintoday.data;

public interface OnDataUpdateListener {
    void onItemUpdated(FinModal item);
    void onItemRemoved(int itemId);
}