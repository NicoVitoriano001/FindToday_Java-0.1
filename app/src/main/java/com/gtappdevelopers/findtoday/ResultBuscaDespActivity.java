package com.gtappdevelopers.findtoday;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultBuscaDespActivity extends AppCompatActivity {
    private RecyclerView idRVRetorno;
    private FinRVAdapter adapter;
    private TextView totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_filtrado);

        totalTextView = findViewById(R.id.idTVTotal);
        idRVRetorno = findViewById(R.id.idRVRetorno);

        // Configurar RecyclerView
        adapter = new FinRVAdapter();
        idRVRetorno.setLayoutManager(new LinearLayoutManager(this));
        idRVRetorno.setAdapter(adapter);

        // Obter dados da Intent
        ArrayList<FinModal> resultados = getIntent().getParcelableArrayListExtra("resultadosFiltrados");

        // Usar o método filtrarDespesas desta classe
        List<FinModal> listaFiltrada = filtrarDespesas(resultados);

        if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
            adapter.submitList(listaFiltrada);
            // Usar o método calcularTotal desta classe
            double total = calcularTotal(listaFiltrada);
            DecimalFormat df = new DecimalFormat("#,##0.00");
            totalTextView.setText("Total Despesas: $ " + df.format(total));
        } else {
            totalTextView.setText("Nenhum despesa encontrado");
            Toast.makeText(this, "Lista de despesa vazia", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para filtrar créditos (versão para esta atividade)
    private List<FinModal> filtrarDespesas(List<FinModal> listaOriginal) {
        List<FinModal> filtrada = new ArrayList<>();
        if (listaOriginal != null) {
            for (FinModal item : listaOriginal) {
                if (item.getTipoDesp() != null && !item.getTipoDesp().equals("CRED")) {
                    filtrada.add(item);
                }
            }
        }
        return filtrada;
    }

    // Método para calcular o total
    private double calcularTotal(List<FinModal> lista) {
        double total = 0;
        for (FinModal item : lista) {
            try {
                total += Double.parseDouble(item.getValorDesp());
            } catch (NumberFormatException e) {
                Log.e("CALCULO", "Valor inválido: " + item.getValorDesp(), e);
            }
        }
        return total;
    }


}