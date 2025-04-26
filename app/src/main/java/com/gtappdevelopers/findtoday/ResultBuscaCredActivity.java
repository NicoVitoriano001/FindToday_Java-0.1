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

public class ResultBuscaCredActivity extends AppCompatActivity {
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

        // Usar o metodo filtrarCreditos desta classe
        List<FinModal> listaFiltrada = filtrarCreditos(resultados);

        if (listaFiltrada != null && !listaFiltrada.isEmpty()) {
            adapter.submitList(listaFiltrada);
            // Usar o metodo calcularTotal desta classe
            double total = calcularTotal(listaFiltrada);
            DecimalFormat df = new DecimalFormat("#,##0.00");
            totalTextView.setText("Total Créditos: $ " + df.format(total));
        } else {
            totalTextView.setText("Nenhum crédito encontrado");
            Toast.makeText(this, "Lista de créditos vazia", Toast.LENGTH_SHORT).show();
        }
    }

    // Metodo para filtrar créditos (versão para esta atividade)
    private List<FinModal> filtrarCreditos(List<FinModal> listaOriginal) {
        List<FinModal> filtrada = new ArrayList<>();
        if (listaOriginal != null) {
            for (FinModal item : listaOriginal) {
                if (item.getTipoDesp() != null && item.getTipoDesp().equals("CRED")) {
                    filtrada.add(item);
                }
            }
        }
        return filtrada;
    }

    // Metodo para calcular o total
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