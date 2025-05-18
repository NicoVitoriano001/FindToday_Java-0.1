package com.app.fintoday.data;

import com.app.fintoday.data.FinModal;

public interface OnDataUpdateListener {
    void onItemUpdated(FinModal item);
    void onItemRemoved(int itemId);
}