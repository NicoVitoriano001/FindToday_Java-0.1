package com.app.fintoday;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.util.List;

public class ResultBuscaGrafActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FinRVAdapter adapter;
    private TextView tituloTextView;
    private TextView totalTextView;
    private ViewModal viewModel;
    private String tipoSelecionado;
    private String ano;
    private String mes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_busca_graf);

        // Inicializar componentes
        tituloTextView = findViewById(R.id.tvTitulo);
        totalTextView = findViewById(R.id.tvTotal);
        recyclerView = findViewById(R.id.rvResultados);

        // Configurar RecyclerView
        adapter = new FinRVAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Obter dados da Intent
        Intent intent = getIntent();
        tipoSelecionado = intent.getStringExtra("TIPO_SELECIONADO");
        ano = intent.getStringExtra("ANO");
        mes = intent.getStringExtra("MES");

        // Configurar título
        tituloTextView.setText(String.format("Filtro: %s - %s/%s", tipoSelecionado, mes, ano));

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(ViewModal.class);

        // Buscar dados filtrados
        buscarDadosFiltrados();

        // Configurar botão flutuante para voltar
        FloatingActionButton fabVoltar = findViewById(R.id.fabVoltar);
        fabVoltar.setOnClickListener(v -> finish());
    }

    // MÉTODO DE BUSCA - DEVE FICAR NA ACTIVITY
    private void buscarDadosFiltrados() {
        viewModel.buscarPorTipoAnoMes(tipoSelecionado, ano, mes).observe(this, lista -> {
            if (lista != null && !lista.isEmpty()) {
                adapter.submitList(lista);
                calcularTotal(lista);
            } else {
                Toast.makeText(this, "Nenhum registro encontrado", Toast.LENGTH_SHORT).show();
                totalTextView.setText("Total: R$ 0,00");
            }
        });
    }

    private void calcularTotal(List<FinModal> lista) {
        double total = 0;
        for (FinModal item : lista) {
            try {
                total += Double.parseDouble(item.getValorDesp());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        DecimalFormat df = new DecimalFormat("#,##0.00");
        totalTextView.setText("Total: R$ " + df.format(total));
    }
}