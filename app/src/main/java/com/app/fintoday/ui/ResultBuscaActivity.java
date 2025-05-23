package com.app.fintoday.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import com.app.fintoday.R;
import com.app.fintoday.data.DatabaseBackupManager;
import com.app.fintoday.data.FinModal;
import com.app.fintoday.data.FinRVAdapter;
import com.app.fintoday.data.ViewModal;
import com.app.fintoday.utils.AppInfoDialogHelper;
import com.app.fintoday.utils.DrawerUtil;
import com.app.fintoday.utils.FabMovementUtil;
import com.app.fintoday.utils.SyncUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultBuscaActivity extends AppCompatActivity implements DrawerUtil.DrawerActions {
    private FinRVAdapter adapter;
    private ViewModal viewmodal;
    private TextView creditoTextView, despesaTextView, saldoTextView;
    private List<FinModal> resultados;
    private static final int ADD_DESP_REQUEST = 1;
    public static final int EDIT_DESP_REQUEST = 2;
    private DatabaseBackupManager databaseBackupManager;
    private AppInfoDialogHelper appInfoDialogHelper;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_busca_rv);

        //floatinmovimento fab 1/3
        FloatingActionButton fab = findViewById(R.id.idFABresultadoConsultReturnHome);
        FabMovementUtil.setupFabMovement(fab);

        // Usar drawerer nas outras UI 2/3 - os layouts têm que ter os mesmo drawer_layout e nav_view
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        databaseBackupManager = new DatabaseBackupManager(this);
        appInfoDialogHelper = new AppInfoDialogHelper(this); // // INICIALIZE PRIMEIRO  MOVER PARA ANTES DO setupDrawer
        DrawerUtil.setupDrawer(this, drawerLayout, navigationView,  // DEPOIS CONFIGURE O DRAWER
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close,
                this, // DrawerActions
                appInfoDialogHelper); // ← AGORA NÃO SERÁ MAIS NULL

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
            overridePendingTransition(0, 0); // Desativa animação
        });


       // lambida do botao fabNewFin
        FloatingActionButton fabNewFin = findViewById(R.id.idFABresultadoConsultNewsFIN);
        fabNewFin.setOnClickListener(v -> {
            Intent intent = new Intent(ResultBuscaActivity.this, NewFinActivity.class);
            startActivityForResult(intent, ADD_DESP_REQUEST);
            overridePendingTransition(0, 0); // Desativa animação
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
                overridePendingTransition(0, 0); // Desativa animação
            }
        });

        // //floatinmovimento fab 2/3
        FabMovementUtil.setupFabMovement(fabNewFin);
        FabMovementUtil.setupFabMovement(fabReturnHome);

        creditoTextView.setOnClickListener(v -> {
            List<FinModal> listaFiltrada = filtrarCreditos();
            if (!listaFiltrada.isEmpty()) {
                    Intent intent = new Intent(ResultBuscaActivity.this, ResultBuscaCredActivity.class);
                    intent.putParcelableArrayListExtra("resultadosFiltrados", new ArrayList<>(listaFiltrada));
                startActivity(intent);
                overridePendingTransition(0, 0); // Desativa animação
            } else {
                Toast.makeText(this, "Nenhuma despesa encontrada", Toast.LENGTH_SHORT).show();
            }
        });

        despesaTextView.setOnClickListener(v -> {
            List<FinModal> listaFiltrada = filtrarDespesas();
            if (!listaFiltrada.isEmpty()) {
                Intent intent = new Intent(ResultBuscaActivity.this, ResultBuscaDespActivity.class);
                intent.putParcelableArrayListExtra("resultadosFiltrados", new ArrayList<>(listaFiltrada));
                startActivity(intent);
                overridePendingTransition(0, 0); // Desativa animação
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

    // Implementação dos métodos da interface DrawerActions   // Usar drawerer nas outras UI 3/3
    @Override public void onBackupRequested() { databaseBackupManager.performBackup();}
    @Override public void onRestoreRequested() {databaseBackupManager.performRestore();}
    @Override  public void onSyncRequested() { SyncUtils.MainSyncWithFirebaseUtils(this); }

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
        if (data == null) return;

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