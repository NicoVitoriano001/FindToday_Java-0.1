package com.app.fintoday.data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.app.fintoday.data.OnDataUpdateListener;

public class FinRepository {
    private final FirebaseHelper firebaseHelper;
    private final Dao dao;
    private final LiveData<List<FinModal>> allDesp;
    private final ExecutorService executorService;
    private OnDataUpdateListener firebaseListener;

    public FinRepository(Application application) {
        FinDatabase database = FinDatabase.getInstance(application);
        dao = database.Dao();
        allDesp = dao.getallDesp();
        executorService = Executors.newSingleThreadExecutor();
        firebaseHelper = FirebaseHelper.getInstance(application);

        setupFirebaseListener();
    }

    private void setupFirebaseListener() {
        firebaseListener = new OnDataUpdateListener() {
            @Override
            public void onItemUpdated(FinModal item) {
                syncFromFirebase(item);
            }

            @Override
            public void onItemRemoved(int itemId) {
                executorService.execute(() -> {
                    FinModal item = dao.getDespById(itemId);
                    if (item != null) {
                        dao.delete(item);
                    }
                });
            }
        };

        firebaseHelper.startRealtimeListener(firebaseListener);
    }

    public void cleanup() {
        firebaseHelper.stopRealtimeListener();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public void insert(FinModal model) {
        executorService.execute(() -> {
            model.setLastUpdated(System.currentTimeMillis());
            long insertedId = dao.insert(model);
            model.setId((int) insertedId);
            firebaseHelper.syncItemToFirebase(model);
        });
    }

    public void update(FinModal model) {
        executorService.execute(() -> {
            model.setLastUpdated(System.currentTimeMillis());
            dao.update(model);
            firebaseHelper.syncItemToFirebase(model);
        });
    }

    public void delete(FinModal model) {
        executorService.execute(() -> {
            dao.delete(model);
            try {
                String userId = firebaseHelper.getCurrentUserId();
                if (userId != null) {
                    firebaseHelper.getUserFinancesReference(userId)
                            .child(String.valueOf(model.getId()))
                            .removeValue()
                            .addOnSuccessListener(aVoid ->
                                    Log.d("SYNC_DEBUG", "Item removido do Firebase: " + model.getId()))
                            .addOnFailureListener(e ->
                                    Log.e("SYNC_DEBUG", "Erro ao remover item do Firebase", e));
                }
            } catch (Exception e) {
                Log.e("SYNC_DEBUG", "Erro geral ao remover item do Firebase", e);
            }
        });
    }

    public void deleteAllDesp() {
        executorService.execute(dao::deleteallDesp);
    }

    public LiveData<List<FinModal>> getAllDesp() {
        return allDesp;
    }

    public LiveData<List<FinModal>> buscarPorTipoAnoMes(String tipo, String ano, String mes) {
        return dao.buscarPorTipoAnoMes(tipo, ano, mes);
    }

    public LiveData<List<FinModal>> buscaDesp(String valorDesp, String tipoDesp,
                                              String fontDesp, String despDescr,
                                              String dataDesp) {
        return dao.buscaDesp(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
    }

    public void bidirectionalSyncWithFirebase() {
        executorService.execute(() -> {
            List<FinModal> localItems = dao.getAllItemsSync();
            if (firebaseHelper != null) {
                firebaseHelper.syncAllItemsToFirebase(localItems);
            }

            String userId = firebaseHelper.getCurrentUserId();
            if (userId != null) {
                firebaseHelper.getUserFinancesReference(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    FinModal remoteItem = snapshot.getValue(FinModal.class);
                                    if (remoteItem != null) {
                                        syncFromFirebase(remoteItem);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("SYNC_DEBUG", "Erro ao carregar dados do Firebase",
                                        databaseError.toException());
                            }
                        });
            }
        });
    }

    public void forceSyncWithFirebase() {
        bidirectionalSyncWithFirebase();
    }

    private void syncFromFirebase(FinModal remoteItem) {
        executorService.execute(() -> {
            FinModal localItem = dao.getDespById(remoteItem.getId());

            if (localItem == null || remoteItem.getLastUpdated() > localItem.getLastUpdated()) {
                if (localItem == null && remoteItem.getDataDesp() == null) {
                    remoteItem.setDataDesp(formatLocalDate(new Date(remoteItem.getLastUpdated())));
                }
                dao.update(remoteItem);
            }
        });
    }

    private String formatLocalDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
}