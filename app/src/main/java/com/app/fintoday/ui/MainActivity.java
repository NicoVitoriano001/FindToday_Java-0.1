package com.app.fintoday.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.app.fintoday.R;
import com.app.fintoday.data.DatabaseBackupManager;
import com.app.fintoday.data.FinModal;
import com.app.fintoday.data.FinRVAdapter;
import com.app.fintoday.data.ViewModal;
import com.app.fintoday.utils.AppInfoDialogHelper;
import com.app.fintoday.utils.NotificationHelper;
import com.app.fintoday.utils.SyncUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import com.app.fintoday.utils.DrawerUtil;

public class MainActivity extends AppCompatActivity implements DrawerUtil.DrawerActions {
    // Usar drawerer nas outras UI 1/3 - adicione implements DrawerUtil.DrawerItemSelectedListener
    private ViewModal viewmodal;
    private static final int ADD_DESP_REQUEST = 1;
    public static final int EDIT_DESP_REQUEST = 2;
    private static final int SEARCH_DESP_REQUEST = 3;
    private DatabaseBackupManager databaseBackupManager;
    private AppInfoDialogHelper appInfoDialogHelper;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        NotificationHelper.createNotificationChannel(this); // Criar canal de notificação

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { MainSyncWithFirebase(); }
        });

        // Configurar o SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> { MainSyncWithFirebase();
        });

        // Configurando o botão de ação flutuante para adicionar
        FloatingActionButton fab = findViewById(R.id.idFABAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewFinActivity.class);
                startActivityForResult(intent, ADD_DESP_REQUEST);
                overridePendingTransition(0, 0);
            }
        });

        // Configurando o segundo botão de ação flutuante para buscar
        FloatingActionButton fab2 = findViewById(R.id.idFABbuscar);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BuscarFinActivity.class);
                startActivityForResult(intent, SEARCH_DESP_REQUEST);
                overridePendingTransition(0, 0);
            }
        });

       // Configurando o RecyclerView
        RecyclerView FinRV = findViewById(R.id.idRVFin);
        FinRV.setLayoutManager(new LinearLayoutManager(this));
        FinRV.setHasFixedSize(true);
        final FinRVAdapter adapter = new FinRVAdapter();
        FinRV.setAdapter(adapter);
        viewmodal = new ViewModelProvider(this).get(ViewModal.class);

        // Observando os dados do ViewModel
        viewmodal.getallDesp().observe(this, new Observer<List<FinModal>>() {
            @Override
            public void onChanged(List<FinModal> models) {
                adapter.submitList(models);
            }
        });

        // CONFIRMA EXCLUSÃO
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                FinModal itemToDelete = adapter.getDespAt(position);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirmar Exclusão")
                        .setMessage("Você tem certeza que deseja deletar este registro?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            viewmodal.delete(itemToDelete);
                            Toast.makeText(MainActivity.this, "Registro Deletado", Toast.LENGTH_SHORT).show();
                            // Mostrar notificação reutilizavel
                            NotificationHelper.showSyncNotification(MainActivity.this);
                        })
                        .setNegativeButton("Não", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                            Toast.makeText(MainActivity.this, "Exclusão Cancelada", Toast.LENGTH_SHORT).show();
                        })
                        .show();

            }
        }).attachToRecyclerView(FinRV);
        // FIM ItemTouchHelper CONFIRMA EXCLUSÃO

        // Configurando o listener de clique no item do RecyclerView
        adapter.setOnItemClickListener(new FinRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FinModal model) {
                Intent intent = new Intent(MainActivity.this, EditFinActivity.class);
                intent.putExtra(NewFinActivity.EXTRA_ID, model.getId());
                intent.putExtra(NewFinActivity.EXTRA_VALOR_DESP, model.getValorDesp());
                intent.putExtra(NewFinActivity.EXTRA_TIPO_DESP, model.getTipoDesp());
                intent.putExtra(NewFinActivity.EXTRA_FONT_DESP, model.getFontDesp());
                intent.putExtra(NewFinActivity.EXTRA_DESCR_DESP, model.getDespDescr());
                intent.putExtra(NewFinActivity.EXTRA_DURATION, model.getDataDesp());
                startActivityForResult(intent, EDIT_DESP_REQUEST);
                overridePendingTransition(0, 0); // Desativa animação
            }
        });

    } // FIM ON CREATE


    // Implementação dos métodos da interface DrawerActions   // Usar drawerer nas outras UI 3/3
    @Override public void onBackupRequested() { databaseBackupManager.performBackup();}
    @Override public void onRestoreRequested() {databaseBackupManager.performRestore();}
    @Override  public void onSyncRequested() { SyncUtils.MainSyncWithFirebaseUtils(this); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(getApplicationContext(),"onActivityResult Metodo Chamado", Toast.LENGTH_SHORT).show();

        if (data == null) {
            Toast.makeText(this, "Sem dados retornados.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == ADD_DESP_REQUEST && resultCode == RESULT_OK) {
            String valorDesp = data.getStringExtra(NewFinActivity.EXTRA_VALOR_DESP);
            String tipoDesp = data.getStringExtra(NewFinActivity.EXTRA_TIPO_DESP);
            String despDescr = data.getStringExtra(NewFinActivity.EXTRA_DESCR_DESP);
            String fontDesp = data.getStringExtra(NewFinActivity.EXTRA_FONT_DESP);
            String dataDesp = data.getStringExtra(NewFinActivity.EXTRA_DURATION);

            FinModal model = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
           // viewmodal.insert(model);  // salvando novamente
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
            viewmodal.update(model);
            Toast.makeText(this, "Registro atualizado.", Toast.LENGTH_SHORT).show();

        } else if (requestCode == SEARCH_DESP_REQUEST && resultCode == RESULT_OK) {
            // Código para lidar com a resposta da BuscarFinActivity
        } else {
            Toast.makeText(this, "Operação cancelada.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void MainSyncWithFirebase() {
        SyncUtils.syncBidirecionalWithRefresh(this, swipeRefreshLayout);
    }
    /**
    private void MainSyncWithFirebase() {
        FinRepository repository = new FinRepository(getApplication());
        repository.bidirectionalMainSyncWithFirebase();
        NotificationHelper.showSyncNotification(this);
        Toast.makeText(this, "Sincronização Bidirecional Iniciada", Toast.LENGTH_SHORT).show();

        // Desativa o refresh após a sincronização (simulação)
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
               // Toast.makeText(MainActivity.this, "Sincronização concluída", Toast.LENGTH_SHORT).show();
            }
        }, 2000); // Ajuste este tempo conforme necessário
    }
**/

}
