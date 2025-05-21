package com.app.fintoday.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.fintoday.utils.AppInfoDialogHelper;
import com.app.fintoday.utils.NotificationHelper;
import com.app.fintoday.utils.SwipeToDeleteUtil;
import com.app.fintoday.utils.SyncUtils;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import com.app.fintoday.R;
import com.app.fintoday.data.DatabaseBackupManager;
import com.app.fintoday.data.FinModal;
import com.app.fintoday.data.FinRVAdapter;
import com.app.fintoday.data.ViewModal;
import com.app.fintoday.utils.DrawerUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultBuscaActivity extends AppCompatActivity implements DrawerUtil.DrawerItemSelectedListener
{
    private FinRVAdapter adapter;
    private ViewModal viewmodal;
    private TextView creditoTextView, despesaTextView, saldoTextView;
    private List<FinModal> resultados;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private static final int ADD_DESP_REQUEST = 1;
    public static final int EDIT_DESP_REQUEST = 2;
    private DrawerLayout drawerLayout;
    private DatabaseBackupManager databaseBackupManager;
    private AppInfoDialogHelper appInfoDialogHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_busca_rv);

        // Usar drawerer nas outras UI 2/3 - os layouts têm que ter os mesmo drawer_layout e nav_view
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerUtil.setupDrawer(this, drawerLayout, navigationView,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        databaseBackupManager = new DatabaseBackupManager(this);
        appInfoDialogHelper = new AppInfoDialogHelper(this);

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

        // Observando os dados do ViewModel
        viewmodal.getallDesp().observe(this, models -> adapter.submitList(models));

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


        // Incio Classe utilitária ItemTouchHelper e Swipe
        SwipeToDeleteUtil.setupSwipeToDelete(idRVRetorno, this, new SwipeToDeleteUtil.OnItemDeletedListener() {
            @Override
            public void onItemDeleted(int position) {
                FinModal itemToDelete = adapter.getDespAt(position);
                viewmodal.delete(itemToDelete);
                NotificationHelper.showSyncNotification(ResultBuscaActivity.this);
            }

            @Override
            public void onDeleteCancelled(int position) {
                adapter.notifyItemChanged(position);
            }
        });
        // Fim Classe utilitária ItemTouchHelper e Swipe

   } // Fim ON CREATE

    // Usar drawerer nas outras UI 3/3
    @Override
    public boolean onDrawerItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(ResultBuscaActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.nav_resumo_desp_graf:
                startActivity(new Intent(ResultBuscaActivity.this, ResumoDespGrafActivity.class));
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