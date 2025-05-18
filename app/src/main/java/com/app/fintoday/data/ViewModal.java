package com.app.fintoday.data;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ViewModal extends AndroidViewModel {

    // Variável para o repositório
    private final FinRepository repository;
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
    public void update(FinModal model) { repository.update(model); }
    public void delete(FinModal model) {
        repository.delete(model);
    }
    public void deleteallDesp() {
        repository.deleteallDesp();
    }
    public LiveData<List<FinModal>> getallDesp() {
        return allDesp;
    }

    //NOVO MeTODO ADICIONADO //Listener no grafico
    public LiveData<List<FinModal>> buscarPorTipoAnoMes(String tipo, String ano, String mes) {
        return repository.buscarPorTipoAnoMes(tipo, ano, mes);
    }


  }

