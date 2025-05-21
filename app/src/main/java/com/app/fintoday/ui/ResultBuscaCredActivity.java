package com.app.fintoday.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.fintoday.R;
import com.app.fintoday.data.DatabaseBackupManager;
import com.app.fintoday.data.FinModal;
import com.app.fintoday.data.FinRVAdapter;
import com.app.fintoday.data.ViewModal;
import com.app.fintoday.utils.DrawerUtil;
import com.app.fintoday.utils.AppInfoDialogHelper;
import com.app.fintoday.utils.NotificationHelper;
import com.app.fintoday.utils.SwipeToDeleteUtil;
import com.app.fintoday.utils.SyncUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.android.material.navigation.NavigationView;

public class ResultBuscaCredActivity extends AppCompatActivity implements DrawerUtil.DrawerItemSelectedListener
{
    private RecyclerView idRVRetorno;
    private FinRVAdapter adapter;
    private TextView totalTextView;
    private ViewModal viewModal;
    private static final int ADD_DESP_REQUEST = 1; //acrescentei já tinha metodo onActivityResult e com botao de add despesas
    public static final int EDIT_DESP_REQUEST = 2;
    private DrawerLayout drawerLayout;
    private DatabaseBackupManager databaseBackupManager;
    private AppInfoDialogHelper appInfoDialogHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_filtrado);

        // Usar drawerer nas outras UI 2/3 - os layouts têm que ter os mesmo drawer_layout e nav_view
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerUtil.setupDrawer(this, drawerLayout, navigationView,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        databaseBackupManager = new DatabaseBackupManager(this);
        appInfoDialogHelper = new AppInfoDialogHelper(this);

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

//botao flutuante newsfin com expressao lambda
        FloatingActionButton fabNewFin = findViewById(R.id.idFABresultadoConsultNewsFIN);
        fabNewFin.setOnClickListener(v -> {
            Intent intent = new Intent(ResultBuscaCredActivity.this, NewFinActivity.class);
            startActivityForResult(intent, ADD_DESP_REQUEST);
        });

        //botao flutuante retornar para home
        FloatingActionButton fabReturnHome = findViewById(R.id.idFABresultadoConsultReturnHome);
        fabReturnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultBuscaCredActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        // Incio Classe utilitária ItemTouchHelper e Swipe
        SwipeToDeleteUtil.setupSwipeToDelete(idRVRetorno, this, new SwipeToDeleteUtil.OnItemDeletedListener() {
            @Override
            public void onItemDeleted(int position) {
                FinModal itemToDelete = adapter.getDespAt(position);
                viewModal.delete(itemToDelete);
                NotificationHelper.showSyncNotification(ResultBuscaCredActivity.this);
            }

            @Override
            public void onDeleteCancelled(int position) {
                adapter.notifyItemChanged(position);
            }
        });
       // Fim Classe utilitária ItemTouchHelper e Swipe

    } //FIM ON CREATE

    // Metodo para filtrar créditos
    private List<FinModal> filtrarCreditos(List<FinModal> listaOriginal) {
        List<FinModal> filtrada = new ArrayList<>();
        if (listaOriginal != null) {
            for (FinModal item : listaOriginal) {
                if (item.getTipoDesp() != null &&
                    item.getTipoDesp().equals("CRED") &&
                   !item.getTipoDesp().equals("-")) {
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


    // implementação do DrawerItemSelectedListener
    @Override
    public boolean onDrawerItemSelected(android.view.MenuItem item) {
        // Implementação igual à do ResultBuscaActivity
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_resumo_desp_graf:
                startActivity(new Intent(this, ResumoDespGrafActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_nova_desp:
                startActivity(new Intent(this, NewFinActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_fazer_bkp:
                databaseBackupManager.performBackup();
                break;
            case R.id.nav_restoreDB:
                databaseBackupManager.performRestore();
                break;
            case R.id.nav_sync_firebase:
                SyncUtils.MainSyncWithFirebaseUtils(this);
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_about:
                appInfoDialogHelper.showAboutDialog();
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_help:
                appInfoDialogHelper.openHelpScreen();
                overridePendingTransition(0, 0);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // onBackPressed para fechar o drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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