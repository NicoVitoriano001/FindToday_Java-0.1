package com.app.fintoday;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.app.fintoday.R;

import java.util.ArrayList;
import java.util.List;

public class BuscarFinActivity extends AppCompatActivity {
    private EditText valorDespEdtBusca, despDescrEdtBusca, dataDespEdtBusca;
    private Spinner tipoDespEdtBusca, fontDespEdtBusca; // Declarado como Spinner
    private Button FinBtnBusca;
    public static final String EXTRA_ID_BUSCA = "EXTRA_ID_BUSCA";
    //public static final String EXTRA_ID_BUSCA = "com.gtappdevelopers.gfgroomdatabase.EXTRA_ID_BUSCA";
    public static final String EXTRA_VALOR_DESP_BUSCA = "EXTRA_VALOR_DESP_BUSCA";
    public static final String EXTRA_TIPO_DESP_BUSCA = "EXTRA_TIPO_DESP_BUSCA";
    public static final String EXTRA_FONT_DESP_BUSCA = "EXTRA_FONT_DESP_BUSCA";
    public static final String EXTRA_DESCR_DESP_BUSCA = "EXTRA_DESP_DESCR";
    public static final String EXTRA_DURATION_BUSCA = "EXTRA_DURATION_BUSCA";
    private Dao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_busca_fin);

        dao = FinDatabase.getInstance(this).Dao();

        valorDespEdtBusca = findViewById(R.id.idEdtValorDespBuscar);
        tipoDespEdtBusca = findViewById(R.id.idEdtTipoDespBuscar);
        fontDespEdtBusca = findViewById(R.id.idEdtFontDespBuscar);
        despDescrEdtBusca = findViewById(R.id.idEdtDespDescrBuscar);
        dataDespEdtBusca = findViewById(R.id.idEdtDataDespBuscar);
        FinBtnBusca = findViewById(R.id.idBtnBuscarDesp);

        setupSpinners();

        FinBtnBusca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valorDesp = valorDespEdtBusca.getText().toString();
                String tipoDesp = tipoDespEdtBusca.getSelectedItem().toString();
                String fontDesp = fontDespEdtBusca.getSelectedItem().toString();
                String despDescr = despDescrEdtBusca.getText().toString();
                String dataDesp = dataDespEdtBusca.getText().toString();

                dao.buscaDesp(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp)
                        .observe(BuscarFinActivity.this, new Observer<List<FinModal>>() {
                            @Override
                            public void onChanged(List<FinModal> finModals) {
                                if (finModals == null || finModals.isEmpty()) {
                                    // Se não houver resultados, exibir uma mensagem de alerta
                                    new AlertDialog.Builder(BuscarFinActivity.this)
                                            .setTitle("Nenhum Resultado")
                                            .setMessage("Nenhum resultado encontrado para a busca realizada.")
                                            .setPositiveButton("OK", null)
                                            .show();
                                } else {
                                    // Se houver resultados, abrir a próxima atividade
                                    Intent intent = new Intent(BuscarFinActivity.this, ResultBuscaActivity.class);
                                    intent.putExtra("resultados", new ArrayList<>(finModals));
                                    startActivity(intent);
                                }
                                // Remover o observador após a primeira chamada
                                dao.buscaDesp(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp)
                                        .removeObserver(this);
                            }
                        });
            }
        });

        FloatingActionButton fabvoltardaBusca = findViewById(R.id.idFABvoltardaBusca);
        fabvoltardaBusca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Encerra a atividade atual e retorna à atividade anterior
                }
          });
    }
    // spinners
    private void setupSpinners() {
        // Configurando o Spinner para Tipo de Despesa
        String[] tiposDespesa = {"","-","ALIM", "CRED", "D PUB","EDUC", "EMPRES", "INVEST","LAZER","OUTR", "TRANS","SAUD"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDespesa);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoDespEdtBusca.setAdapter(tipoAdapter);

        // Configurando o Spinner para Fonte de Despesa
        String[] fontesDespesa = {"","-","ALELO","BB","BRA","BTG","CASH", "CEF1","CEF2","NU", "MP", "STDER","OUTR"};
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fontesDespesa);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontDespEdtBusca.setAdapter(fontAdapter);
    }
}
