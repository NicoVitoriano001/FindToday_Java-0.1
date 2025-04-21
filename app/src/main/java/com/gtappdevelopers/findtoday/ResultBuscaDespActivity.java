package com.gtappdevelopers.findtoday;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultBuscaDespActivity extends AppCompatActivity {
    private RecyclerView idRVRetorno;
    private FinRVAdapter adapter;
    private TextView totalTextView; // Usa o mesmo idTVTotal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_filtrado); // Mesmo layout

        totalTextView = findViewById(R.id.idTVTotal);
        totalTextView.setText("Total Despesas: $ 0.00"); // Texto diferente
        idRVRetorno = findViewById(R.id.idRVRetorno);

        adapter = new FinRVAdapter();
        idRVRetorno.setLayoutManager(new LinearLayoutManager(this));
        idRVRetorno.setAdapter(adapter);

        ArrayList<FinModal> resultados = getIntent().getParcelableArrayListExtra("resultadosFiltrados");

        if(resultados != null && !resultados.isEmpty()){
            adapter.submitList(resultados);
            double total = calcularTotal(resultados);
            DecimalFormat df = new DecimalFormat("#,##0.00");
            totalTextView.setText("Total Despesas: $ " + df.format(total));
        }
    }

    private double calcularTotal(List<FinModal> lista) {
        double total = 0;
        for (FinModal item : lista) {
            total += Double.parseDouble(item.getValorDesp());
        }
        return total;
    }
}