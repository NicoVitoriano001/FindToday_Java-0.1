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

public class FinRepository {
    private final FirebaseHelper firebaseHelper;
    private final Dao dao;
    private final LiveData<List<FinModal>> allDesp;
    private final ExecutorService executorService;

    // Construtor para inicializar variáveis
    public FinRepository(Application application) {
        FinDatabase database = FinDatabase.getInstance(application);
        dao = database.Dao();
        allDesp = dao.getallDesp();
        executorService = Executors.newSingleThreadExecutor();
        firebaseHelper = FirebaseHelper.getInstance(application); //16.05.25
    }

    public void insert(FinModal model) {
        executorService.execute(() -> {
            // 1. Define timestamp
            model.setLastUpdated(System.currentTimeMillis());

            // 2. Insere no SQLite (Room gera o ID)
            long insertedId = dao.insert(model);
            model.setId((int) insertedId); // Atualiza o modelo com o ID real

            // 3. Sincroniza com Firebase APÓS ter o ID definitivo
            firebaseHelper.syncItemToFirebase(model);
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
                // 2. Remove do Firebase
                String userId = firebaseHelper.getCurrentUserId();
                if (userId == null) {
                   // Log.e("SYNC_DEBUG", "Usuário não autenticado, não é possível excluir no Firebase");
                    return;
                }

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
    public void bidirectionalSyncWithFirebase() {
        executorService.execute(() -> {
            // 1. Sincronizar dados locais para o Firebase
            List<FinModal> localItems = dao.getAllItemsSync();
            if (firebaseHelper != null) {
                firebaseHelper.syncAllItemsToFirebase(localItems);
            }

            // 2. Sincronizar dados do Firebase para o local
            String userId = firebaseHelper.getCurrentUserId();
            if (userId != null) {
                firebaseHelper.getUserFinancesReference(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    FinModal remoteItem = snapshot.getValue(FinModal.class);
                                    if (remoteItem != null) {
                                        syncFromFirebase(remoteItem); // Usa a resolução de conflito existente
                                    }
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

    // Atualizar o método forceSyncWithFirebase para usar o novo método
    public void forceSyncWithFirebase() {
        bidirectionalSyncWithFirebase();
    }




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