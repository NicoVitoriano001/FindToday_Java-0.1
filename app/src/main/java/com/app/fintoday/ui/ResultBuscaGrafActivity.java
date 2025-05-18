package com.app.fintoday.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.fintoday.R;
import com.app.fintoday.data.FinModal;
import com.app.fintoday.data.FinRVAdapter;
import com.app.fintoday.data.ViewModal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;

public class ResultBuscaGrafActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FinRVAdapter adapter;
    private TextView tituloTextView, totalTextView;
    private ViewModal viewModel;
    private String tipoSelecionado, ano, mes;
    private static final int ADD_DESP_REQUEST = 1;
    public static final int EDIT_DESP_REQUEST = 2;


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

        tituloTextView.setText(String.format("Filtro: %s - %s/%s", tipoSelecionado, mes, ano));

        viewModel = new ViewModelProvider(this).get(ViewModal.class);

        /**
         No ResultBuscaGrafActivity, após inserir ou atualizar um item, ele chama buscarDadosFiltrados() que recarrega toda a lista do banco de dados
         **/
        buscarDadosFiltrados();

        // Configurar clique nos itens
        adapter.setOnItemClickListener(new FinRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FinModal model) {
                Intent intent = new Intent(ResultBuscaGrafActivity.this, EditFinActivity.class);
                intent.putExtra(NewFinActivity.EXTRA_ID, model.getId());
                intent.putExtra(NewFinActivity.EXTRA_VALOR_DESP, model.getValorDesp());
                intent.putExtra(NewFinActivity.EXTRA_TIPO_DESP, model.getTipoDesp());
                intent.putExtra(NewFinActivity.EXTRA_FONT_DESP, model.getFontDesp());
                intent.putExtra(NewFinActivity.EXTRA_DESCR_DESP, model.getDespDescr());
                intent.putExtra(NewFinActivity.EXTRA_DURATION, model.getDataDesp());
                startActivityForResult(intent, MainActivity.EDIT_DESP_REQUEST);
            }
        });

        //  botão flutuante
        FloatingActionButton fabNewFin  = findViewById(R.id.FABresultBuscaGrafAdd);
        fabNewFin.setOnClickListener(v -> {
            Intent newFinIntent = new Intent(ResultBuscaGrafActivity.this, NewFinActivity.class);
            startActivityForResult(newFinIntent, ADD_DESP_REQUEST);
        });

        FloatingActionButton fabReturnHome  = findViewById(R.id.FABresultBuscaGrafHome);
        fabReturnHome .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultBuscaGrafActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        // Adicionar ItemTouchHelper para swipe
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                FinModal itemToDelete = adapter.getDespAt(position);

                // Criar uma cópia da lista atual para manipulação segura
                List<FinModal> currentList = new ArrayList<>(adapter.getCurrentList());

                new AlertDialog.Builder(ResultBuscaGrafActivity.this)
                        .setTitle("Confirmar Exclusão")
                        .setMessage("Você tem certeza que deseja deletar este registro?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            // 1. Remove da lista local
                            currentList.remove(position);

                            // 2. Atualiza o adapter
                            adapter.submitList(currentList);

                            // 3. Atualiza o total
                            calcularTotal(currentList);

                            // 4. Remove do banco de dados
                            viewModel.delete(itemToDelete);

                            Toast.makeText(ResultBuscaGrafActivity.this, "Registro Deletado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Não", (dialog, which) -> {
                            // Cancela a exclusão
                            adapter.notifyItemChanged(position);
                            Toast.makeText(ResultBuscaGrafActivity.this, "Exclusão Cancelada", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);

    } //FIM ON CREATE

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_DESP_REQUEST && resultCode == RESULT_OK && data != null) {
            String valorDesp = data.getStringExtra(NewFinActivity.EXTRA_VALOR_DESP);
            String tipoDesp = data.getStringExtra(NewFinActivity.EXTRA_TIPO_DESP);
            String despDescr = data.getStringExtra(NewFinActivity.EXTRA_DESCR_DESP);
            String fontDesp = data.getStringExtra(NewFinActivity.EXTRA_FONT_DESP);
            String dataDesp = data.getStringExtra(NewFinActivity.EXTRA_DURATION);

            FinModal model = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
            viewModel.insert(model);
            Toast.makeText(this, "Registro salvo.", Toast.LENGTH_LONG).show();


        } else if (requestCode == EDIT_DESP_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(NewFinActivity.EXTRA_ID, -1);
            if (id == -1) {
                Toast.makeText(this, "Registro não pode ser atualizado.", Toast.LENGTH_LONG).show();
                return;
            }
            String valorDesp = data.getStringExtra(NewFinActivity.EXTRA_VALOR_DESP);
            String tipoDesp = data.getStringExtra(NewFinActivity.EXTRA_TIPO_DESP);
            String fontDesp = data.getStringExtra(NewFinActivity.EXTRA_FONT_DESP);
            String despDescr = data.getStringExtra(NewFinActivity.EXTRA_DESCR_DESP);
            String dataDesp = data.getStringExtra(NewFinActivity.EXTRA_DURATION);

            FinModal model = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
            model.setId(id);
            viewModel.update(model);
            Toast.makeText(this, "Registro atualizado.", Toast.LENGTH_SHORT).show();

        }
    }


}