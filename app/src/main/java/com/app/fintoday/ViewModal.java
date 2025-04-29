package com.app.fintoday;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;

public class ViewModal extends AndroidViewModel {

    // Variável para o repositório
    private final FinRepository repository;

    // Variável para armazenar todas as despesas
    private final LiveData<List<FinModal>> allDesp;

    // Construtor do ViewModel
    public ViewModal(@NonNull Application application) {
        super(application);
        repository = new FinRepository(application);
        allDesp = repository.getallDesp();
    }

    public void insert(FinModal model) {
        repository.insert(model);
    }
    public void update(FinModal model) {
        repository.update(model);
    }
    public void delete(FinModal model) {
        repository.delete(model);
    }
    public void deleteallDesp() {
        repository.deleteallDesp();
    }
    public LiveData<List<FinModal>> getallDesp() {
        return allDesp;
    }
    public LiveData<List<FinModal>> getDespesasPorAnoEMes(
            String ano,
            String mes)
    {
        return repository.getDespesasPorAnoEMes(ano, mes);
    }



    public static class QueryBuilder {

        public static SimpleSQLiteQuery buildSearchQuery(String valorDesp,
                                                         String tipoDesp,
                                                         String fontDesp,
                                                         String despDescr,
                                                         String dataDesp)
        {
            StringBuilder query = new StringBuilder("SELECT * FROM fin_table WHERE 1=1 ");

            if (!valorDesp.isEmpty()) {
                query.append("AND valorDesp LIKE '%").append(valorDesp).append("%' ");
            }
            if (!tipoDesp.isEmpty()) {
                query.append("AND tipoDesp LIKE '%").append(tipoDesp).append("%' ");
            }
            if (!fontDesp.isEmpty()) {
                query.append("AND fontDesp LIKE '%").append(fontDesp).append("%' ");
            }
            if (!dataDesp.isEmpty()) {
                query.append("AND dataDesp LIKE '%").append(dataDesp).append("%' ");
            }

            // Adiciona um LIKE para cada palavra separada por espaço em despDescr
            if (!despDescr.isEmpty()) {
                String[] palavras = despDescr.split("\\s+");
                for (String palavra : palavras) {
                    query.append("AND despDescr LIKE '%").append(palavra).append("%' ");
                }
            }

            query.append("ORDER BY SUBSTR(dataDesp, INSTR(dataDesp, ' ') + 1) DESC");

            return new SimpleSQLiteQuery(query.toString());
        }
    }



}
