package com.app.fintoday;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

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
    public LiveData<List<FinModal>> getDespesasPorAnoEMes(
            String ano,
            String mes) {
        return dao.buscaPorAnoEMes(ano, mes);
    }


    public LiveData<List<FinModal>> buscarComFormatacao(
            String valorDesp,
            String tipoDesp,
            String fontDesp,
            String despDescr,
            String dataDesp
    ) {
        // Função local para formatar os termos
        Function<String, String> formatar = termo -> {
            if (termo == null || termo.trim().isEmpty()) {
                return "%%";
            }
            // Limita a 3 substituições de espaços por %
            String[] partes = termo.split(" ", 4);
            return "%" + String.join("%", partes) + "%";
        };

        return dao.buscaDesp(
                formatar.apply(valorDesp),
                formatar.apply(tipoDesp),
                formatar.apply(fontDesp),
                formatar.apply(despDescr),
                formatar.apply(dataDesp)
        );
    }



/** ORIGINAL
    public LiveData<List<FinModal>> buscaDesp(
            String valorDesp,
            String tipoDesp,
            String fontDesp,
            String despDescr,
            String dataDesp )
    {
        SimpleSQLiteQuery query = ViewModal.QueryBuilder.buildSearchQuery(
                valorDesp,
                tipoDesp,
                fontDesp,
                despDescr,
                dataDesp);
        return dao.buscaDesp(query);
    }

 **/

}
