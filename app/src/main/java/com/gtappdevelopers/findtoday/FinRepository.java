package com.gtappdevelopers.findtoday;

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

    // Método para inserir dados no banco
    public void insert(FinModal model) {
        executorService.execute(() -> dao.insert(model));
    }

    // Método para atualizar dados no banco
    public void update(FinModal model) {
        executorService.execute(() -> dao.update(model));
    }

    // Método para deletar um item específico
    public void delete(FinModal model) {
        executorService.execute(() -> dao.delete(model));
    }

    // Método para deletar todos os itens
    public void deleteallDesp() {
        executorService.execute(dao::deleteallDesp);
    }

    // Método para obter todos os itens como LiveData
    public LiveData<List<FinModal>> getallDesp() {
        return allDesp;
    }

    // Método para buscar despesas por ano e mês
    public LiveData<List<FinModal>> getDespesasPorAnoEMes(String ano, String mes) {
        return dao.buscaPorAnoEMes(ano, mes);
    }
}
