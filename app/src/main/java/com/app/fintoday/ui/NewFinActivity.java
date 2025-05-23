package com.app.fintoday.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

import com.app.fintoday.R;
import com.app.fintoday.data.FinModal;
import com.app.fintoday.data.FinRepository;
import com.app.fintoday.data.FirebaseHelper;
import com.app.fintoday.utils.NotificationHelper;

public class NewFinActivity extends AppCompatActivity {
    private EditText valorDespEdt, despDescrEdt, dataDespEdt;
    private Spinner tipoDespEdt, fontDespEdt;// Declarado como Spinner
    private Button FinBtnSave;
    public static final String EXTRA_ID = "com.app.fintoday.EXTRA_ID";
    //public static final String EXTRA_ID = "com.gtappdevelopers.gfgroomdatabase.EXTRA_ID";
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
        setContentView(R.layout.activity_new_fin);

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
        // FinBtnConsult = findViewById(R.id.idBtnConsultarResumo);

        setupSpinners();

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
                String fontDesp = fontDespEdt.getSelectedItem().toString();
                String despDescr = despDescrEdt.getText().toString();
                String dataDesp = dataDespEdt.getText().toString();

                if (tipoDesp.isEmpty() || despDescr.isEmpty() || dataDesp.isEmpty()) {
                    Toast.makeText(NewFinActivity.this, "Entre com os valores mínimos do registro.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Cria o objeto FinModal
                FinModal finModal = new FinModal(valorDesp, tipoDesp, fontDesp, despDescr, dataDesp);
                finModal.setLastUpdated(System.currentTimeMillis());

                // Salva via Repository (único ponto de salvamento)
                FinRepository repository = new FinRepository(getApplication());
                repository.insert(finModal);

                // Mostra notificação reutilizavel
                NotificationHelper.showSyncNotification(NewFinActivity.this);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_VALOR_DESP, valorDesp);
                resultIntent.putExtra(EXTRA_TIPO_DESP, tipoDesp);
                resultIntent.putExtra(EXTRA_FONT_DESP, fontDesp);
                resultIntent.putExtra(EXTRA_DESCR_DESP, despDescr);
                resultIntent.putExtra(EXTRA_DURATION, dataDesp);

                // Apenas indica sucesso e fecha a atividade
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(NewFinActivity.this, "Registro salvo com sucesso.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupSpinners() {
        String[] tiposDespesa = {"-", "ALIM", "CRED", "D PUB", "EDUC", "EMPRES", "INVEST", "LAZER", "OUTR", "TRANS", "SAUD"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDespesa);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoDespEdt.setAdapter(tipoAdapter);

        String[] fontesDespesa = {"-", "ALELO", "BB", "BRA", "BTG", "CASH", "CEF1", "CEF2", "NU", "MP", "STDER", "OUTR"};
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
                NewFinActivity.this,
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
        datePickerDialog.show();// Mostrar o DatePickerDialog
    }

}
