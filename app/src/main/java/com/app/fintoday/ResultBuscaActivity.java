package com.app.fintoday;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultBuscaActivity extends AppCompatActivity {
    private RecyclerView idRVRetorno;
    private FinRVAdapter adapter;
    private ViewModal viewmodal;
    private TextView creditoTextView, despesaTextView, saldoTextView;
    private List<FinModal> resultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_busca_rv);

        // Inicializar os TextViews
        creditoTextView = findViewById(R.id.idTVCredito);
        despesaTextView = findViewById(R.id.idTVDespesa);
        saldoTextView = findViewById(R.id.idTVSaldo);

        // Inicializar RecyclerView
        idRVRetorno = findViewById(R.id.idRVRetorno);
        adapter = new FinRVAdapter();
        idRVRetorno.setLayoutManager(new LinearLayoutManager(this));
        idRVRetorno.setAdapter(adapter);
        viewmodal = new ViewModelProvider(this).get(ViewModal.class); // Inicializar viewmodal

        ArrayList<Parcelable> parcelableList = getIntent().getParcelableArrayListExtra("resultados");
        resultados = new ArrayList<>();

        if (parcelableList != null) {
            for (Parcelable parcelable : parcelableList) {
                if (parcelable instanceof FinModal) {
                    resultados.add((FinModal) parcelable);
                }
            }

            adapter.submitList(resultados);

            // Calcular a soma dos valores
            double totalCredito = 0;
            double totalDespesa = 0;

            for (FinModal item : resultados) {
                double valor = Double.parseDouble(item.getValorDesp());

                if ("-".equals(item.getTipoDesp())) continue;

                if ("CRED".equals(item.getTipoDesp())) {
                    totalCredito += valor;
                } else {
                    totalDespesa += valor;
                }
            }

            double saldo = totalCredito - totalDespesa;

            DecimalFormat df = new DecimalFormat("#,##0.00");

            creditoTextView.setText("Créditos: $ " + df.format(totalCredito));
            despesaTextView.setText("Despesas: $ " + df.format(totalDespesa));
            saldoTextView.setText("Saldo: $ " + df.format(saldo));
        }

        adapter.setOnItemClickListener(new FinRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FinModal model) {
                Intent intent = new Intent(ResultBuscaActivity.this, EditFinActivity.class);
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
              //  Intent intent = new Intent(ResultBuscaActivity.this, BuscarFinActivity.class);
              //  startActivity(intent);

            }
        });

        //botao flutuante retornar para home
        FloatingActionButton fabReturnHome = findViewById(R.id.idFABresultadoConsultReturnHome);
        fabReturnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultBuscaActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        creditoTextView.setOnClickListener(v -> {
            List<FinModal> listaFiltrada = filtrarCreditos();
            if (!listaFiltrada.isEmpty()) {
                try {
                    Intent intent = new Intent(ResultBuscaActivity.this, ResultBuscaCredActivity.class);
                    intent.putParcelableArrayListExtra("resultadosFiltrados", new ArrayList<>(listaFiltrada));

                    // Adicione logs para depuração
                    Log.d("DEBUG", "Iniciando ResultBuscaCredActivity com " + listaFiltrada.size() + " itens");
                    for (FinModal item : listaFiltrada) {
                        Log.d("DEBUG", "Item: " + item.getTipoDesp() + " - " + item.getValorDesp());
                    }

                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("ERROR", "Falha ao iniciar ResultBuscaCredActivity", e);
                    Toast.makeText(this, "Erro ao exibir créditos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Nenhum crédito encontrado", Toast.LENGTH_SHORT).show();
            }
        });

        despesaTextView.setOnClickListener(v -> {
            List<FinModal> listaFiltrada = filtrarDespesas();
            if (!listaFiltrada.isEmpty()) {
                Intent intent = new Intent(ResultBuscaActivity.this, ResultBuscaDespActivity.class);
                intent.putParcelableArrayListExtra("resultadosFiltrados", new ArrayList<>(listaFiltrada));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Nenhuma despesa encontrada", Toast.LENGTH_SHORT).show();
            }
        });


       // Configurar o ItemTouchHelper para swipe (exclusão)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, // Não suporta drag-and-drop
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT // Suporta swipe para ambos os lados
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Não permite reordenar itens
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                FinModal itemToDelete = adapter.getDespAt(position);

                // Diálogo de confirmação
                new AlertDialog.Builder(ResultBuscaActivity.this)
                        .setTitle("Confirmar Exclusão")
                        .setMessage("Você tem certeza que deseja deletar este registro?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            viewmodal.delete(itemToDelete);
                            Toast.makeText(ResultBuscaActivity.this, "Registro Deletado", Toast.LENGTH_SHORT).show();
                            // Atualiza a lista após exclusão (opcional)
                            resultados.remove(position);
                            adapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("Não", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // Cancela o swipe
                            Toast.makeText(ResultBuscaActivity.this, "Exclusão Cancelada", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        }).attachToRecyclerView(idRVRetorno); // Vincula ao RecyclerView
    } // Fim ON CREATE

    // Métodos auxiliares para filtrar, métodos de filtro (fora do onCreate):
    private List<FinModal> filtrarCreditos() {
        List<FinModal> filtrada = new ArrayList<>();
        for (FinModal item : resultados) {
            if ("CRED".equals(item.getTipoDesp())) {
                filtrada.add(item);
            }
        }
        return filtrada;
    }
    private List<FinModal> filtrarDespesas() {
        List<FinModal> filtrada = new ArrayList<>();
        for (FinModal item : resultados) {
            String tipo = item.getTipoDesp();
            if (tipo != null && !tipo.equals("CRED") && !tipo.equals("-")) {
                filtrada.add(item);
            }
        }
        return filtrada;
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
                //Intent intent = new Intent(ResultBuscaActivity.this, MainActivity.class);
                //startActivity(intent);
            }
        }
    }
}