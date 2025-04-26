package com.gtappdevelopers.findtoday;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewModal viewmodal;
    private static final int ADD_DESP_REQUEST = 1;
    public static final int EDIT_DESP_REQUEST = 2;
    private static final int SEARCH_DESP_REQUEST = 3;
    private DrawerLayout drawerLayout;
    private com.gtappdevelopers.findtoday.DatabaseBackupManager databaseBackupManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Inicialize o databaseBackupManager
        databaseBackupManager = new com.gtappdevelopers.findtoday.DatabaseBackupManager(this);

        // Configuração do ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        // Configurando o listener de clique no item do NavigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_resumo_desp: // ID do item que você adicionou
                        Intent intent = new Intent(MainActivity.this, ResumoDespActivity.class);
                        startActivity(intent);
                        drawerLayout.closeDrawer(GravityCompat.START); // Fecha o drawer após a seleção
                        return true;
                    case R.id.nav_nova_desp:
                        Intent intentNovaDesp = new Intent(MainActivity.this, NewFinActivity.class);
                        startActivity(intentNovaDesp);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.nav_fazer_bkp:
                        databaseBackupManager.performBackup(); // Chama o método de backup
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Configurando o botão de ação flutuante para adicionar
        FloatingActionButton fab = findViewById(R.id.idFABAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewFinActivity.class);
                startActivityForResult(intent, ADD_DESP_REQUEST);
            }
        });

        // Configurando o segundo botão de ação flutuante para buscar
        FloatingActionButton fab2 = findViewById(R.id.idFABbuscar);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BuscarFinActivity.class);
                startActivityForResult(intent, SEARCH_DESP_REQUEST);
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
                        .setTitle("Confirmar Exclusão");
                      //  .setMessage("Você tem certeza que deseja
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirmar Exclusão")
                        .setMessage("Você tem certeza que deseja deletar este registro?")  // String completa e fechada
                        .setPositiveButton("Sim", (dialog, which) -> {
                            viewmodal.delete(itemToDelete);
                            Toast.makeText(MainActivity.this, "Registro Deletado", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Não", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                            Toast.makeText(MainActivity.this, "Exclusão Cancelada", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
        }).attachToRecyclerView(FinRV);
        // FIM CONFIRMA EXCLUSÃO

        // Configurando o listener de clique no item do RecyclerView
        adapter.setOnItemClickListener(new FinRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FinModal model) {
                Intent intent = new Intent(MainActivity.this, NewFinActivity.class);
                intent.putExtra(NewFinActivity.EXTRA_ID, model.getId());
                intent.putExtra(NewFinActivity.EXTRA_VALOR_DESP, model.getValorDesp());
                intent.putExtra(NewFinActivity.EXTRA_TIPO_DESP, model.getTipoDesp());
                intent.putExtra(NewFinActivity.EXTRA_FONT_DESP, model.getFontDesp());
                intent.putExtra(NewFinActivity.EXTRA_DESCR_DESP, model.getDespDescr());
                intent.putExtra(NewFinActivity.EXTRA_DURATION, model.getDataDesp());
                startActivityForResult(intent, EDIT_DESP_REQUEST);
            }
        });
    } // fim onCreate

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
}
