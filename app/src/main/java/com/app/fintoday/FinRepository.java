package com.app.fintoday;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;
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
            dao.insert(model);
            firebaseHelper.syncLocalDataWithFirebase(Collections.singletonList(model)); //16.05.25  Sincroniza com Firebase após inserção local
        });

    }

    /** ORIGINAL //16.05.25
    public void insert(FinModal model) {
        executorService.execute(() -> dao.insert(model));
    }
    **/

    public void update(FinModal model) {
        executorService.execute(() -> {
            dao.update(model);
            firebaseHelper.syncLocalDataWithFirebase(Collections.singletonList(model)); // Sincroniza com Firebase após edicao local
        });
    }
    /** ORIGINAL
    //    public void update(FinModal model) {
    //        executorService.execute(() -> dao.update(model));
       }
     **/
    public void delete(FinModal model) {
        executorService.execute(() -> {
            dao.delete(model);
            if (firebaseHelper != null) {
                String userId = firebaseHelper.getCurrentUserId();
                if (userId != null) {
                    firebaseHelper.getUserFinancesReference()  // Já aponta para users/{userId}/finances
                            .child(String.valueOf(model.getId()))
                            .removeValue();
                }
            }
        });
    }
    /** ORIGINAL
        public void delete(FinModal model) {
          executorService.execute(() -> dao.delete(model));
       }
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

    // Metodo para sincronização manual, verificar
    public void forceSyncWithFirebase() {
        executorService.execute(() -> {
            List<FinModal> allItems = dao.getAllItemsSync();
            if (firebaseHelper != null) {
                firebaseHelper.syncAllItemsToFirebase(allItems);
            } else {
                Log.e("FinRepository", "FirebaseHelper não inicializado");
            }
        });
    }


    public void syncFromFirebase(FinModal modal) {
        executorService.execute(() -> {
            FinModal existingItem = dao.getDespById(modal.getId());// Verifica se o item já existe localmente

            if (existingItem == null) {// Item não existe - insere novo
                dao.insert(modal);
                Log.d("FirebaseSync", "Novo item inserido do Firebase: " + modal.getId());
            } else if (modal.getLastUpdated() > existingItem.getLastUpdated()) {
                // Item existe e está mais atualizado no Firebase - atualiza
                dao.update(modal);
                Log.d("FirebaseSync", "Item atualizado do Firebase: " + modal.getId());
            }
            // Se o item local estiver mais recente, não faz nada
        });
    }

}


