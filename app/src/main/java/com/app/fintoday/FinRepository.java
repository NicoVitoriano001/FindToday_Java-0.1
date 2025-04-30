package com.app.fintoday;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinRepository {
    private final Dao dao;
    private final LiveData<List<FinModal>> allDesp;
    private final ExecutorService executorService;

    // Construtor para inicializar variáveis
    public FinRepository(Application application) {
        FinDatabase database = FinDatabase.getInstance(application);
        dao = database.Dao();
        allDesp = dao.getallDesp();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(FinModal model) {
        executorService.execute(() -> dao.insert(model));
    }

    public void update(FinModal model) {
        executorService.execute(() -> dao.update(model));
    }

    public void delete(FinModal model) {
        executorService.execute(() -> dao.delete(model));
    }

    public void deleteallDesp() {
        executorService.execute(dao::deleteallDesp);
    }

    public LiveData<List<FinModal>> getallDesp() {
        return allDesp;
    }

    public LiveData<List<FinModal>> getDespesasPorAnoEMes(
            String ano,
            String mes) {
        return dao.buscaPorAnoEMes(ano, mes);
    }

    //ver Dao.java, relacao direta
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
}


