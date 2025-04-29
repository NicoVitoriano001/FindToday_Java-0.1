package com.app.fintoday;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class ResumoDespActivity extends AppCompatActivity {
    private FinDatabase finDatabase;
    private EditText anoEditText, mesEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumo_desp);

        // Inicializa o banco de dados
        finDatabase = FinDatabase.getInstance(getApplicationContext());

        // Inicializa as views
        anoEditText = findViewById(R.id.idEdtAno);
        mesEditText = findViewById(R.id.idEdtMes);

        Button resumoButton = findViewById(R.id.idBtnFazerResumo);

        resumoButton.setOnClickListener(v -> {
            try {
                String ano = anoEditText.getText().toString().trim();
                String mes = mesEditText.getText().toString().trim();

                // se mês for com 1 dig, preenche zeroa esquerda
                final String mesFinal = mes.length() == 1 ? "0" + mes : mes;

                if (ano.isEmpty() || mesFinal.isEmpty()) {
                    showToast("Preencha ano e mês!");
                    return;
                }

                finDatabase.Dao().buscaPorAnoEMes(ano, mesFinal)
                            .observe(this, finModals -> {
                                if (isFinishing() || isDestroyed()) return;
                                if (finModals != null && !finModals.isEmpty()) {
                                       showResultDialog(finModals);
                                } else {
                                       showToast("Nenhum dado encontrado para " + mesFinal + "/" + ano);
                                }
                            });

            } catch (Exception e) {
                //Log.e("ResumoDesp", "Erro na busca", e);
                showToast("Erro na busca: " + e.getMessage());
            }
        });

    }

    // Adicionando o metodo showToast que estava faltando
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showResultDialog(List<FinModal> dados) {
        try {
            if (dados == null || dados.isEmpty()) {
                showToast("Nenhuma despesa encontrada para o período");
                return;
            }

            ResumoDespDialogFragment dialog = ResumoDespDialogFragment.newInstance(dados);

            // Configuração para ocupar a maior parte da tela
            dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppDialogTheme);

            dialog.show(getSupportFragmentManager(), "ResultadosDespesasDialog");

        } catch (Exception e) {
            //Log.e("ResumoDesp", "Erro ao mostrar diálogo", e);
            showToast("Erro ao exibir resultados: " + e.getMessage());
        }
    }
}