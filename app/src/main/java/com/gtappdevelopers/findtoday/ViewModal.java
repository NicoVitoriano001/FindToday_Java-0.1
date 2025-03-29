package com.gtappdevelopers.findtoday;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
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

    // Método para inserir uma nova despesa
    public void insert(FinModal model) {
        repository.insert(model);
    }

    // Método para atualizar uma despesa existente
    public void update(FinModal model) {
        repository.update(model);
    }

    // Método para deletar uma despesa específica
    public void delete(FinModal model) {
        repository.delete(model);
    }

    // Método para deletar todas as despesas
    public void deleteallDesp() {
        repository.deleteallDesp();
    }

    // Método para obter todas as despesas
    public LiveData<List<FinModal>> getallDesp() {
        return allDesp;
    }

    // Método adicional para buscar despesas por ano e mês
    public LiveData<List<FinModal>> getDespesasPorAnoEMes(String ano, String mes) {
        return repository.getDespesasPorAnoEMes(ano, mes);
    }
}
