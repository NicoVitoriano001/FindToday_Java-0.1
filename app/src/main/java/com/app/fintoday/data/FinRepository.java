package com.app.fintoday.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
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

public class FinRepository {
    private final FirebaseHelper firebaseHelper;
    private final Dao dao;
    private final LiveData<List<FinModal>> allDesp;
    private final ExecutorService executorService;

    private static final String PREF_LAST_SYNC_TIME = "last_sync_time";
    private SharedPreferences sharedPreferences;

    // Construtor para inicializar variáveis
    public FinRepository(Application application) {
        FinDatabase database = FinDatabase.getInstance(application);
        dao = database.Dao();
        allDesp = dao.getallDesp();
        executorService = Executors.newSingleThreadExecutor();
        firebaseHelper = FirebaseHelper.getInstance(application);
        sharedPreferences = application.getSharedPreferences("FinRepositoryPrefs", Context.MODE_PRIVATE);
    }

    public void insert(FinModal model) {
        executorService.execute(() -> {
            model.setLastUpdated(System.currentTimeMillis());  // 1. Define timestamp

            long insertedId = dao.insert(model);  // 2. Insere no SQLite (Room gera o ID)
            model.setId((int) insertedId); // Atualiza o modelo com o ID real

            firebaseHelper.syncItemToFirebase(model); // 3. Sincroniza com Firebase APÓS ter o ID definitivo
           // Log.d("SYNC_DEBUG", "Novo item inserido. ID: " + insertedId);
        });
    }

    /**
     * ORIGINAL //16.05.25
     * public void insert(FinModal model) {
     * executorService.execute(() -> dao.insert(model));
     * }
     **/

    public void update(FinModal model) {
        executorService.execute(() -> {
            model.setLastUpdated(System.currentTimeMillis());// 1. Atualiza timestamp ANTES de enviar
            dao.update(model);  // 2. Persiste localmente
            firebaseHelper.syncItemToFirebase(model); // 3. Envia para Firebase (apenas campos relevantes)
        });
    }

    /**
     * ORIGINAL
     * //    public void update(FinModal model) {
     * //        executorService.execute(() -> dao.update(model));
     * }
     **/
    public void delete(FinModal model) {
        executorService.execute(() -> {
            dao.delete(model);// 1. Remove do banco local

            try {
                String userId = firebaseHelper.getCurrentUserId();
                if (userId == null) {
                   // Log.e("SYNC_DEBUG", "Usuário não autenticado, não é possível excluir no Firebase");
                    return;
                }
                // 2. Remove do Firebase
                firebaseHelper.getUserFinancesReference(userId)
                        .child(String.valueOf(model.getId()))
                        .removeValue()
                        .addOnSuccessListener(aVoid ->
                                Log.d("SYNC_DEBUG", "Item removido do Firebase: " + model.getId()))
                        .addOnFailureListener(e ->
                                Log.e("SYNC_DEBUG", "Erro ao remover item do Firebase", e));
            } catch (Exception e) {
                Log.e("SYNC_DEBUG", "Erro geral ao remover item do Firebase", e);
            }
        });
    }
    /**
     * ORIGINAL
     * public void delete(FinModal model) {
     * executorService.execute(() -> dao.delete(model));
     * }
     **/
    public void deleteallDesp() {
        executorService.execute(dao::deleteallDesp);
    }

    public LiveData<List<FinModal>> getallDesp() {
        return allDesp;
    }

    // NOVO MeTODO ADICIONADO //Listener no grafico
    public LiveData<List<FinModal>> buscarPorTipoAnoMes(String tipo, String ano, String mes) {
        return dao.buscarPorTipoAnoMes(tipo, ano, mes);
    }

    //ver Dao.java é responsável pelo acesso a dados. repositório lida com a lógica de negócios e a agregação de dados
    public LiveData<List<FinModal>> buscaDesp(
            String valorDesp,
            String tipoDesp,
            String fontDesp,
            String despDescr,
            String dataDesp
    ) {
        return dao.buscaDesp(
                valorDesp,
                tipoDesp,
                fontDesp,
                despDescr,
                dataDesp
        );
    }

    // Adicionar este método para sincronização bidirecional
    public void bidirectionalMainSyncWithFirebase() {
        executorService.execute(() -> {
            // 1. Obter o timestamp da última sincronização. é por sincronização e não por item
            long lastSyncTime = sharedPreferences.getLong(PREF_LAST_SYNC_TIME, 0);

            // 2. Sincronizar dados locais modificados para o Firebase
            List<FinModal> modifiedLocalItems = dao.getModifiedItems(lastSyncTime);
            if (firebaseHelper != null && modifiedLocalItems != null && !modifiedLocalItems.isEmpty()) {
                firebaseHelper.syncAllItemsToFirebase(modifiedLocalItems);
            }

            // 3. Sincronizar dados do Firebase para o local
            String userId = firebaseHelper.getCurrentUserId();
            if (userId != null) {
                firebaseHelper.getUserFinancesReference(userId)
                        .orderByChild("lastUpdated")
                        .startAt(lastSyncTime + 1) // Busca apenas itens mais recentes que a última sincronização
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long newSyncTime = System.currentTimeMillis();
                                boolean hasUpdates = false;

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    FinModal remoteItem = snapshot.getValue(FinModal.class);
                                    if (remoteItem != null) {
                                        syncFromFirebase(remoteItem);
                                        hasUpdates = true;
                                    }
                                }

                                // Atualiza o timestamp apenas se houver atualizações
                                if (hasUpdates) {
                                    sharedPreferences.edit()
                                            .putLong(PREF_LAST_SYNC_TIME, newSyncTime)
                                            .apply();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("SYNC_DEBUG", "Erro ao carregar dados do Firebase", databaseError.toException());
                            }
                        });
            }
        });
    }

    //

    public void syncFromFirebase(FinModal remoteItem) {
        executorService.execute(() -> {
            FinModal localItem = dao.getDespById(remoteItem.getId());

            // Resolução de conflito baseada em lastUpdated
            if (localItem == null || remoteItem.getLastUpdated() > localItem.getLastUpdated()) {

                // Se for um novo item (localItem == null), formata dataDesp se necessário
                if (localItem == null && remoteItem.getDataDesp() == null) {
                    remoteItem.setDataDesp(formatLocalDate(new Date(remoteItem.getLastUpdated())));
                }

                // Atualiza o banco local
                dao.update(remoteItem);
            }
        });
    }

    // "%a. %Y-%m-%d" = "EEE yyyy-MM-dd"
    private String formatLocalDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
}