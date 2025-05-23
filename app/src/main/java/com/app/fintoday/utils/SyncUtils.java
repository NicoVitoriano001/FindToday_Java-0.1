package com.app.fintoday.utils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.app.fintoday.data.FinRepository;

public class SyncUtils {
    public static void syncBidirecionalWithRefresh(Context context, SwipeRefreshLayout swipeRefreshLayout) {
        FinRepository repository = new FinRepository((Application) context.getApplicationContext());
        repository.bidirectionalMainSyncWithFirebase();
        NotificationHelper.showSyncNotification(context);
        Toast.makeText(context, "Sincronização Bidirecional Firebase", Toast.LENGTH_SHORT).show();

        // Desativa o refresh após a sincronização (simulação)
        new Handler().postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    public static void MainSyncWithFirebaseUtils(Context context) {
        FinRepository repository = new FinRepository((Application) context.getApplicationContext());
        repository.bidirectionalMainSyncWithFirebase();
        NotificationHelper.showSyncNotification(context);
        Toast.makeText(context, "Sincronização Bidirecional Firebase", Toast.LENGTH_SHORT).show();
    }
}