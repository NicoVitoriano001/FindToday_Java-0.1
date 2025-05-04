package com.app.fintoday;

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
}
