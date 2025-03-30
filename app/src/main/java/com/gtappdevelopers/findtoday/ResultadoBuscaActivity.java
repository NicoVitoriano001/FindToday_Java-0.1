package com.gtappdevelopers.findtoday;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;
public class ResultadoBuscaActivity extends AppCompatActivity {
    private RecyclerView idRVRetorno;
    private FinRVAdapter adapter;
    private ViewModal viewmodal;
    private TextView totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_busca_rv);

        // Inicializar o TextView para o total
        totalTextView = findViewById(R.id.idTVTotal);
        // Inicializar RecyclerView
        idRVRetorno = findViewById(R.id.idRVRetorno);
        adapter = new FinRVAdapter();
        idRVRetorno.setLayoutManager(new LinearLayoutManager(this));
        idRVRetorno.setAdapter(adapter);
        viewmodal = new ViewModelProvider(this).get(ViewModal.class); // Inicializar viewmodal

        ArrayList<Parcelable> parcelableList = getIntent().getParcelableArrayListExtra("resultados");
        List<FinModal> resultados = new ArrayList<>();

        if (parcelableList != null) {
            for (Parcelable parcelable : parcelableList) {
                if (parcelable instanceof FinModal) {
                    resultados.add((FinModal) parcelable);
                }
            }

            adapter.submitList(resultados);
            // Calcular a soma dos valores
            double total = 0;
            for (FinModal item : resultados) {
                double valor = Double.parseDouble(item.getValorDesp()); // Supondo que valorDesp é uma String

                // Verifica se tipoDesp é igual a "-"
                if ("-".equals(item.getTipoDesp())) {
                    continue; // Desconsidera este item
                }

                // Verifica se tipoDesp é igual a "CRED"
                if ("CRED".equals(item.getTipoDesp())) {
                    total += valor; // Adiciona se tipoDesp for CRED
                } else {
                    total -= valor; // Subtrai se tipoDesp não for CRED
                }
            }
            DecimalFormat df = new DecimalFormat("#,##0.00"); // Define o formato para valores com duas casas decimais
            String formattedTotal = df.format(total); // Formata o total
            totalTextView.setText("Total: $ " + formattedTotal); // Atualiza o TextView com o total formatado
        }

        adapter.setOnItemClickListener(new FinRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FinModal model) {
                Intent intent = new Intent(ResultadoBuscaActivity.this, NewFinActivity.class);
                intent.putExtra(NewFinActivity.EXTRA_ID, model.getId());
                intent.putExtra(NewFinActivity.EXTRA_VALOR_DESP, model.getValorDesp());
                intent.putExtra(NewFinActivity.EXTRA_TIPO_DESP, model.getTipoDesp());
                intent.putExtra(NewFinActivity.EXTRA_FONT_DESP, model.getFontDesp());
                intent.putExtra(NewFinActivity.EXTRA_DESCR_DESP, model.getDespDescr());
                intent.putExtra(NewFinActivity.EXTRA_DURATION, model.getDataDesp());
                startActivityForResult(intent, MainActivity.EDIT_DESP_REQUEST);
            }
        });

        //botao flutuante retornar
        FloatingActionButton fabReturn = findViewById(R.id.idFABresultadoConsultReturn);
        fabReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Encerra a atividade atual e retorna à atividade anterior
            }
        });

        //botao flutuante retornar para home
        FloatingActionButton fabReturnHome = findViewById(R.id.idFABresultadoConsultReturnHome);
        fabReturnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultadoBuscaActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.EDIT_DESP_REQUEST && resultCode == RESULT_OK && data != null) {
            int id = data.getIntExtra(NewFinActivity.EXTRA_ID, -1);
            if (id != -1) {
                String valorDesp = data.getStringExtra(NewFinActivity.EXTRA_VALOR_DESP);
                String tipoDesp = data.getStringExtra(NewFinActivity.EXTRA_TIPO_DESP);
                String fontDesp = data.getStringExtra(NewFinActivity.EXTRA_FONT_DESP);
                String despDescr = data.getStringExtra(NewFinActivity.EXTRA_DESCR_DESP);
                String dataDesp = data.getStringExtra(NewFinActivity.EXTRA_DURATION);

                FinModal model = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
                model.setId(id);
                viewmodal.update(model);
                Toast.makeText(this, "Registro com busca atualizado.", Toast.LENGTH_SHORT).show();
                adapter.updateItem(id, valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
                //finish();

                // Inicie a MainActivity após a conclusão da edição
                //Intent intent = new Intent(ResultadoBuscaActivity.this, MainActivity.class);
                //startActivity(intent);
            }
        }
    }

}
