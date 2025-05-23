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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.fintoday.R;
import com.app.fintoday.data.FinModal;
import com.app.fintoday.data.FinRVAdapter;
import com.app.fintoday.data.ViewModal;
import com.app.fintoday.utils.FabMovementUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultBuscaCredActivity extends AppCompatActivity {
    private RecyclerView idRVRetorno;
    private FinRVAdapter adapter;
    private TextView totalTextView;
    private ViewModal viewModal;
    private static final int ADD_DESP_REQUEST = 1; //acrescentei já tinha metodo onActivityResult e com botao de add despesas
    public static final int EDIT_DESP_REQUEST = 2;


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

        // Inicialize o ViewModel para quando chanvar o NewFinActivity.class e atualizar
        viewModal = new ViewModelProvider(this).get(ViewModal.class);

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

        adapter.setOnItemClickListener(new FinRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FinModal model) {
                Intent intent = new Intent(ResultBuscaCredActivity.this, EditFinActivity.class);
                intent.putExtra(NewFinActivity.EXTRA_ID, model.getId());
                intent.putExtra(NewFinActivity.EXTRA_VALOR_DESP, model.getValorDesp());
                intent.putExtra(NewFinActivity.EXTRA_TIPO_DESP, model.getTipoDesp());
                intent.putExtra(NewFinActivity.EXTRA_FONT_DESP, model.getFontDesp());
                intent.putExtra(NewFinActivity.EXTRA_DESCR_DESP, model.getDespDescr());
                intent.putExtra(NewFinActivity.EXTRA_DURATION, model.getDataDesp());
                startActivityForResult(intent, MainActivity.EDIT_DESP_REQUEST);
            }
        });


        // botão flutuantes adiconar
        FloatingActionButton fab = findViewById(R.id.idFABresultadoConsultNewsFIN);
        FabMovementUtil.setupFabMovement(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultBuscaCredActivity.this, NewFinActivity.class);
                startActivityForResult(intent, ADD_DESP_REQUEST);
                overridePendingTransition(0, 0);
            }
        });

        // botao flutuante buscar
        FloatingActionButton fab2 = findViewById(R.id.idFABresultadoConsultReturnHome);
        FabMovementUtil.setupFabMovement(fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultBuscaCredActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
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

                new AlertDialog.Builder(ResultBuscaCredActivity.this)
                        .setTitle("Confirmar Exclusão")
                        .setMessage("Você tem certeza que deseja deletar este registro?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            // 1. Remove da lista local
                            currentList.remove(position);

                            // 2. Atualiza o adapter
                            adapter.submitList(currentList);

                            // 3. Atualiza o total
                            double total = calcularTotal(currentList);
                            DecimalFormat df = new DecimalFormat("#,##0.00");
                            totalTextView.setText("Total Créditos: $ " + df.format(total));

                            // 4. Remove do banco de dados
                            viewModal.delete(itemToDelete);

                            Toast.makeText(ResultBuscaCredActivity.this, "Registro Deletado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Não", (dialog, which) -> {
                            // Cancela a exclusão
                            adapter.notifyItemChanged(position);
                            Toast.makeText(ResultBuscaCredActivity.this, "Exclusão Cancelada", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        }).attachToRecyclerView(idRVRetorno);

    } //FIM ON CREATE

    // Metodo para filtrar créditos
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_DESP_REQUEST && resultCode == RESULT_OK) {
            String valorDesp = data.getStringExtra(NewFinActivity.EXTRA_VALOR_DESP);
            String tipoDesp = data.getStringExtra(NewFinActivity.EXTRA_TIPO_DESP);
            String despDescr = data.getStringExtra(NewFinActivity.EXTRA_DESCR_DESP);
            String fontDesp = data.getStringExtra(NewFinActivity.EXTRA_FONT_DESP);
            String dataDesp = data.getStringExtra(NewFinActivity.EXTRA_DURATION);

            //FinModal model = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
            //viewmodal.insert(model);  // salvando novamente
            Toast.makeText(this, "Registro salvo.", Toast.LENGTH_LONG).show();

            // Atualiza o total
            double total = calcularTotal(adapter.getCurrentList());
            DecimalFormat df = new DecimalFormat("#,##0.00");
            totalTextView.setText("Total Créditos: $ " + df.format(total));
        }
        // TIREI  && data != null
        else   if (requestCode == EDIT_DESP_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(NewFinActivity.EXTRA_ID, -1);
            if (id != -1) {
                String valorDesp = data.getStringExtra(NewFinActivity.EXTRA_VALOR_DESP);
                String tipoDesp = data.getStringExtra(NewFinActivity.EXTRA_TIPO_DESP);
                String fontDesp = data.getStringExtra(NewFinActivity.EXTRA_FONT_DESP);
                String despDescr = data.getStringExtra(NewFinActivity.EXTRA_DESCR_DESP);
                String dataDesp = data.getStringExtra(NewFinActivity.EXTRA_DURATION);

                FinModal model = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
                model.setId(id);
                viewModal.update(model);
                Toast.makeText(this, "Registro com busca atualizado.", Toast.LENGTH_SHORT).show();
                adapter.updateItem(id, valorDesp, tipoDesp, fontDesp, despDescr, dataDesp); //aqui atualiza a tela quando volta

                // Atualiza o total
                double total = calcularTotal(adapter.getCurrentList());
                DecimalFormat df = new DecimalFormat("#,##0.00");
                totalTextView.setText("Total Créditos: $ " + df.format(total));

                Toast.makeText(this, "Despesa atualizada.", Toast.LENGTH_SHORT).show();
                //finish();
                // Inicie a MainActivity após a conclusão da edição
                //Intent intent = new Intent(ResultBuscaActivity.this, MainActivity.class);
                //startActivity(intent);
            }
        }
     }


}