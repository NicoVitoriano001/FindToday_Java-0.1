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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultBuscaActivity extends AppCompatActivity {
    private FinRVAdapter adapter;
    private ViewModal viewmodal;
    private TextView creditoTextView, despesaTextView, saldoTextView;
    private List<FinModal> resultados;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private static final int ADD_DESP_REQUEST = 1;
    public static final int EDIT_DESP_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_busca_rv);

        // Inicializar os TextViews
        creditoTextView = findViewById(R.id.tvCred_ResultBuscaActivity);
        despesaTextView = findViewById(R.id.tvDesp_ResultBuscaActivity);
        saldoTextView = findViewById(R.id.tvSal_ResultBuscaActivity);

        // Inicializar RecyclerView
        RecyclerView idRVRetorno = findViewById(R.id.idRVRetorno);
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

        // Versão com lambda
        adapter.setOnItemClickListener(model -> {
            Intent intent = new Intent(ResultBuscaActivity.this, EditFinActivity.class);
            intent.putExtra(NewFinActivity.EXTRA_ID, model.getId());
            intent.putExtra(NewFinActivity.EXTRA_VALOR_DESP, model.getValorDesp());
            intent.putExtra(NewFinActivity.EXTRA_TIPO_DESP, model.getTipoDesp());
            intent.putExtra(NewFinActivity.EXTRA_FONT_DESP, model.getFontDesp());
            intent.putExtra(NewFinActivity.EXTRA_DESCR_DESP, model.getDespDescr());
            intent.putExtra(NewFinActivity.EXTRA_DURATION, model.getDataDesp());
            startActivityForResult(intent, MainActivity.EDIT_DESP_REQUEST);
        });

       // lambida do botao fabNewFin
         FloatingActionButton fabNewFin = findViewById(R.id.idFABresultadoConsultNewsFIN);
         fabNewFin.setOnClickListener(v -> {
         Intent intent = new Intent(ResultBuscaActivity.this, NewFinActivity.class);
         startActivityForResult(intent, ADD_DESP_REQUEST);
         });
        /** // tradicional do botao fabNewFin
         FloatingActionButton fabNewFin = findViewById(R.id.idFABresultadoConsultNewsFIN);
         fabNewFin.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        Intent intent = new Intent(ResultBuscaActivity.this, NewFinActivity.class);
        startActivityForResult(intent, ADD_DESP_REQUEST);
        }
        });
         **/

        //botao flutuante retornar para home
        FloatingActionButton fabReturnHome = findViewById(R.id.idFABresultadoConsultReturnHome);
        fabReturnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultBuscaActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Configurar movimento para os FABs
        setupFabMovement(fabNewFin);
        setupFabMovement(fabReturnHome);

        creditoTextView.setOnClickListener(v -> {
            List<FinModal> listaFiltrada = filtrarCreditos();
            if (!listaFiltrada.isEmpty()) {
                try {
                    Intent intent = new Intent(ResultBuscaActivity.this, ResultBuscaCredActivity.class);
                    intent.putParcelableArrayListExtra("resultadosFiltrados", new ArrayList<>(listaFiltrada));

                    // Adicione logs para depuração
                       for (FinModal item : listaFiltrada) {
                    }
                   startActivityForResult(intent, 1001); // ou qualquer código
                   //startActivity(intent);
                } catch (Exception e) {

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
                            recalculateTotals(); // Recalcular os valores de resumo após a atualização 30.04.2025
                        })
                        .setNegativeButton("Não", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // Cancela o swipe
                            Toast.makeText(ResultBuscaActivity.this, "Exclusão Cancelada", Toast.LENGTH_SHORT).show();
                        })
                        .show();

            }
        }).attachToRecyclerView(idRVRetorno); // Vincula ao RecyclerView
   } // Fim ON CREATE



    private void setupFabMovement(FloatingActionButton fab) {
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = (int) fab.getX();
                        initialY = (int) fab.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x = initialX + (int) (event.getRawX() - initialTouchX);
                        int y = initialY + (int) (event.getRawY() - initialTouchY);

                        // Limitar os movimentos para dentro da tela
                        int maxX = ((View) fab.getParent()).getWidth() - fab.getWidth();
                        int maxY = ((View) fab.getParent()).getHeight() - fab.getHeight();

                        x = Math.min(Math.max(x, 0), maxX);
                        y = Math.min(Math.max(y, 0), maxY);

                        fab.setX(x);
                        fab.setY(y);
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Verificar se foi um clique ou arrasto
                        if (Math.abs(event.getRawX() - initialTouchX) < 10 &&
                                Math.abs(event.getRawY() - initialTouchY) < 10) {
                            // This ensures accessibility services can perform the click
                            fab.performClick();
                            return true;
                        }
                        return true;
                }
                return false;
            }
        });
    }

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

    // Metodo para recalcular os totais 30.04.2025
    private void recalculateTotals() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_DESP_REQUEST && resultCode == RESULT_OK) {
            String valorDesp = data.getStringExtra(NewFinActivity.EXTRA_VALOR_DESP);
            String tipoDesp = data.getStringExtra(NewFinActivity.EXTRA_TIPO_DESP);
            String despDescr = data.getStringExtra(NewFinActivity.EXTRA_DESCR_DESP);
            String fontDesp = data.getStringExtra(NewFinActivity.EXTRA_FONT_DESP);
            String dataDesp = data.getStringExtra(NewFinActivity.EXTRA_DURATION);

            FinModal model = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
            viewmodal.insert(model);
            adapter.addItem(model);
            Toast.makeText(this, "Registro salvo.", Toast.LENGTH_LONG).show();
            recalculateTotals();

            //TIRE && data != null
        } else if (requestCode == EDIT_DESP_REQUEST && resultCode == RESULT_OK ) {
            int id = data.getIntExtra(EditFinActivity.EXTRA_ID, -1);
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
                adapter.updateItem(id, valorDesp, tipoDesp, fontDesp, despDescr, dataDesp); //aqui atualiza a tela quando volta
                // Atualiza o total
                recalculateTotals();
            } else {
                Toast.makeText(this, "Operação cancelada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}