package com.app.fintoday;
// Criado em 30.04.2025
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class EditFinActivity extends AppCompatActivity {
    private EditText valorDespEdt, despDescrEdt, dataDespEdt;
    private Spinner tipoDespEdt, fontDespEdt;
    private Button FinBtnSave;
    public static final String EXTRA_ID = "com.app.fintoday.EXTRA_ID";
    public static final String EXTRA_VALOR_DESP = "com.app.fintoday.EXTRA_VALOR_DESP";
    public static final String EXTRA_TIPO_DESP = "com.app.fintoday.EXTRA_TIPO_DESP";
    public static final String EXTRA_FONT_DESP = "com.app.fintoday.EXTRA_FONT_DESP";
    public static final String EXTRA_DESCR_DESP = "com.app.fintoday.EXTRA_DESP_DESCR";
    public static final String EXTRA_DURATION = "com.app.fintoday.EXTRA_DURATION";
    private FirebaseHelper firebaseHelper;
    private NotificationHelper notificationHelper;

    public String getDataHoraAtual() {
        LocalDateTime dataHoraAtual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE yyyy-MM-dd");
        return dataHoraAtual.format(formatter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_fin);

        // Inicializa FirebaseHelper e NotificationHelper
        firebaseHelper = FirebaseHelper.getInstance(this);
        notificationHelper = new NotificationHelper();
        notificationHelper.createNotificationChannel(this);

        valorDespEdt = findViewById(R.id.idEdtValorDesp);
        tipoDespEdt = findViewById(R.id.idEdtTipoDesp);
        fontDespEdt = findViewById(R.id.idEdtFontDesp);
        despDescrEdt = findViewById(R.id.idEdtDespDescr);
        dataDespEdt = findViewById(R.id.idEdtDataDesp);
        FinBtnSave = findViewById(R.id.idBtnSaveDesp);

     // Configurando os Spinners
        setupSpinners();

    // Configura data
        dataDespEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        String dataHoraAtual = getDataHoraAtual();
        dataDespEdt.setText(dataHoraAtual);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            valorDespEdt.setText(intent.getStringExtra(EXTRA_VALOR_DESP));
            tipoDespEdt.setSelection(getIndex(tipoDespEdt, intent.getStringExtra(EXTRA_TIPO_DESP)));
            fontDespEdt.setSelection(getIndex(fontDespEdt, intent.getStringExtra(EXTRA_FONT_DESP)));
            despDescrEdt.setText(intent.getStringExtra(EXTRA_DESCR_DESP));
            dataDespEdt.setText(intent.getStringExtra(EXTRA_DURATION));
        }

        FinBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valorDesp = valorDespEdt.getText().toString();
                String tipoDesp = tipoDespEdt.getSelectedItem().toString();
                String fontDesp = fontDespEdt.getSelectedItem().toString(); // Correct variable name
                String despDescr = despDescrEdt.getText().toString();
                String dataDesp = dataDespEdt.getText().toString();

                if (tipoDesp.isEmpty() || despDescr.isEmpty() || dataDesp.isEmpty()) {
                    Toast.makeText(EditFinActivity.this, "Entre com valores mínimos do registro.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Obter o ID do registro existente
                int id = getIntent().getIntExtra(EXTRA_ID, -1);
                if (id == -1) {
                    Toast.makeText(EditFinActivity.this, "Erro: ID do registro não encontrado.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Criar o objeto FinModal com os dados editados e o ID original
                FinModal finModal = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
                finModal.setId(id); // Manter o ID original
                finModal.setLastUpdated(System.currentTimeMillis());

                // Atualizar no repositório/FinRepository (que cuidará da sincronização com Firebase)
                FinRepository repository = new FinRepository(getApplication());
                repository.update(finModal);

                // Mostrar notificação e finalizar
                NotificationHelper.showSyncNotification(EditFinActivity.this);

                // showSyncNotification();
                saveFin(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
            }
        });
    }

    /**
    private void showSyncNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                    NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_menu) /// modificar
                    .setContentTitle("Sincronização concluída")
                    .setContentText("O registro foi sincronizado com o Firebase")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(1, builder.build());
        }
    }
**/
    private void setupSpinners() {
        String[] tiposDespesa = {"-","ALIM", "CRED", "D PUB","EDUC", "EMPRES", "INVEST","LAZER","OUTR", "TRANS","SAUD"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDespesa);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoDespEdt.setAdapter(tipoAdapter);

        String[] fontesDespesa = {"-","ALELO","BB","BRA","BTG","CASH", "CEF1","CEF2","NU", "MP", "STDER","OUTR"};
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fontesDespesa);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontDespEdt.setAdapter(fontAdapter);
    }
    private int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void showDatePickerDialog() {
        // Obter a data atual
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Criar o DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditFinActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Criar um LocalDateTime com a data selecionada
                        LocalDateTime selectedDateTime = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDay, 0, 0);
                        // Formatar a data selecionada
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE yyyy-MM-dd");
                        String formattedDate = selectedDateTime.format(formatter);
                        dataDespEdt.setText(formattedDate);
                    }
                },
                year, month, day);
        datePickerDialog.show();        // Mostrar o DatePickerDialog
    }

    private void saveFin(String valorDesp, String tipoDesp, String fontDesp, String despDescr, String dataDesp) {
        // PASSA OS DADOS NOVOS/RECUPERADOS PARA SALVAR
        Intent data = new Intent();
        data.putExtra(EditFinActivity.EXTRA_VALOR_DESP, valorDesp);
        data.putExtra(EditFinActivity.EXTRA_TIPO_DESP, tipoDesp);
        data.putExtra(EditFinActivity.EXTRA_FONT_DESP, fontDesp);
        data.putExtra(EditFinActivity.EXTRA_DESCR_DESP, despDescr);
        data.putExtra(EditFinActivity.EXTRA_DURATION, dataDesp);

        int id = getIntent().getIntExtra(EditFinActivity.EXTRA_ID, -1);
        if (id != -1) {
            data.putExtra(EditFinActivity.EXTRA_ID, id);
        }
        setResult(RESULT_OK, data);
        Toast.makeText(this, "Registro foi salvo no Database.", Toast.LENGTH_LONG).show();
        finish();
    }

}
