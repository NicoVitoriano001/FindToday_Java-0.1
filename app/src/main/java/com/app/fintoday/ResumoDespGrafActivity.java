package com.app.fintoday;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumoDespGrafActivity extends AppCompatActivity {
    private FinDatabase finDatabase;
    private EditText anoEditText, mesEditText;
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private Spinner spinnerChartType;
    private String selectedChartType = "Pizza";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumo_desp_graf);

        finDatabase = FinDatabase.getInstance(getApplicationContext());
        anoEditText = findViewById(R.id.idEdtAno);
        mesEditText = findViewById(R.id.idEdtMes);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        spinnerChartType = findViewById(R.id.spinnerChartType);

        setCurrentDate();

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.chart_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChartType.setAdapter(adapter);

        spinnerChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChartType = parent.getItemAtPosition(position).toString();
                updateChartVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button resumoButton = findViewById(R.id.idBtnFazerResumo);
        resumoButton.setOnClickListener(v -> {
            try {
                String ano = anoEditText.getText().toString().trim();
                String mes = mesEditText.getText().toString().trim();
                final String mesFinal = mes.length() == 1 ? "0" + mes : mes;

                if (ano.isEmpty() || mesFinal.isEmpty()) {
                    showToast("Preencha ano e mês!");
                    return;
                }

                finDatabase.Dao().buscaPorAnoEMes(ano, mesFinal)
                        .observe(this, finModals -> {
                            if (isFinishing() || isDestroyed()) return;
                            if (finModals != null && !finModals.isEmpty()) {
                                generateCharts(finModals);
                            } else {
                                showToast("Nenhum dado encontrado para " + mesFinal + "/" + ano);
                                clearCharts();
                            }
                        });

            } catch (Exception e) {
                showToast("Erro na busca: " + e.getMessage());
                clearCharts();
            }
        });
    } // fim on create

    private void updateChartVisibility() {
        pieChart.setVisibility(selectedChartType.equals("Pizza") ? View.VISIBLE : View.GONE);
        barChart.setVisibility(selectedChartType.equals("Barras") ? View.VISIBLE : View.GONE);
        lineChart.setVisibility(selectedChartType.equals("Linhas") ? View.VISIBLE : View.GONE);
    }

    private void clearCharts() {
        pieChart.clear();
        barChart.clear();
        lineChart.clear();
    }

    private void generateCharts(List<FinModal> dados) {
        // Agrupar por tipoDesp e somar os valores
        Map<String, Float> valoresPorTipo = new HashMap<>();
        String[] tipos = {"ALIM", "CRED", "D PUB", "EDUC", "EMPRES", "INVEST", "LAZER", "OUTR", "TRANS", "SAUD"};

        for (String tipo : tipos) {
            valoresPorTipo.put(tipo, 0f);
        }

        for (FinModal item : dados) {
            String tipo = item.getTipoDesp();
            float valor = Float.parseFloat(item.getValorDesp());

            if (valoresPorTipo.containsKey(tipo)) {
                valoresPorTipo.put(tipo, valoresPorTipo.get(tipo) + valor);
            } else {
                valoresPorTipo.put(tipo, valor);
            }
        }

        // Preparar dados para os gráficos
        List<PieEntry> pieEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Float> entry : valoresPorTipo.entrySet()) {
            if (entry.getValue() > 0) {
                pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
                barEntries.add(new BarEntry(i, entry.getValue()));
                lineEntries.add(new Entry(i, entry.getValue()));
                labels.add(entry.getKey());
                i++;
            }
        }


        // obter cores do colors.xml
        int blue_500 = ContextCompat.getColor(this, R.color.blue_500);
        int green_200 = ContextCompat.getColor(this, R.color.green_200);
        int laranja = ContextCompat.getColor(this, R.color.laranja);
        int teal_150 = ContextCompat.getColor(this, R.color.teal_150);
        int vermelho = ContextCompat.getColor(this, R.color.vermelho);
        int colorAccent = ContextCompat.getColor(this, R.color.colorAccent);
        int magenta = ContextCompat.getColor(this, R.color.magenta);
        int azulclaro = ContextCompat.getColor(this, R.color.azulclaro);
        int azulescuro = ContextCompat.getColor(this, R.color.azulescuro);
        int amarelo_canario = ContextCompat.getColor(this, R.color.amarelo_canario);



        // Configurar gráfico de Pizza
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        //pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setColors(new int[] {vermelho, azulescuro,amarelo_canario, green_200, laranja, teal_150, colorAccent, magenta}); // Usando cores do colors.xml
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueFormatter(new PercentFormatter(pieChart));
        pieDataSet.setValueTextColor(Color.WHITE); // Cor do texto dos valores
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);


        // Configurar gráfico de Barras
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        BarData barData = new BarData(barDataSet);
        barDataSet.setColors(new int[] {vermelho, azulescuro,amarelo_canario, green_200, laranja, teal_150, colorAccent, magenta}); // Usando cores do colors.xml
        //barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextSize(12f);
        barDataSet.setValueTextColor(Color.WHITE); //OK
        barChart.setData(barData);
        // barChart.setExtraOffsets(20f, 20f, 20f, 20f); // Espaço para eixos

        // Configurar eixo X para barras
        XAxis xAxisBar = barChart.getXAxis();
        xAxisBar.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBar.setGranularity(1f);
        xAxisBar.setDrawGridLines(true);
        xAxisBar.setTextColor(Color.WHITE); // Cor do texto do eixo X
        xAxisBar.setTextSize(10f);

        // Configurar eixo Y para barras
        YAxis yAxisLeftBar = barChart.getAxisLeft();
        yAxisLeftBar.setGranularity(1f);
        yAxisLeftBar.setTextColor(Color.WHITE); // Cor do texto do eixo X

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(true);
        barChart.setTouchEnabled(true);
        barChart.setDragDecelerationFrictionCoef(0.95f);
        barChart.setScaleEnabled(true);
        barChart.animateY(1000);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh

        // Configurar gráfico de Linhas
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "");
        lineDataSet.setColors(new int[] {vermelho, azulescuro,amarelo_canario, green_200, laranja, teal_150, colorAccent, magenta});
        // Usando cores do colors.xml
        // lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        lineDataSet.setValueTextSize(12f);
        lineDataSet.setValueTextColor(Color.WHITE); // Alterado para branco para melhor visibilidade

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

// Configurar eixo X para linhas
        XAxis xAxisLine = lineChart.getXAxis();
        xAxisLine.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxisLine.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisLine.setGranularity(1f);
        xAxisLine.setDrawGridLines(false);
        xAxisLine.setTextColor(Color.WHITE); // Cor do texto do eixo X
        xAxisLine.setTextSize(10f); // Tamanho do texto do eixo X

// Configurar eixo Y para linhas
        YAxis yAxisLeftLine = lineChart.getAxisLeft();
        yAxisLeftLine.setGranularity(1f);
        yAxisLeftLine.setTextColor(Color.WHITE); // Cor do texto do eixo Y

        lineChart.getAxisRight().setEnabled(false); // Desabilitar o eixo Y direito
        lineChart.getDescription().setEnabled(false); // Desabilitar descrição
        lineChart.setTouchEnabled(true);
        lineChart.setDragDecelerationFrictionCoef(0.95f);
        lineChart.setScaleEnabled(true);
        lineChart.animateY(1000);
        lineChart.invalidate();



// Configurações comuns para todos os gráficos
        for (Chart chart : new Chart[]{pieChart, barChart, lineChart}) {
            chart.getDescription().setEnabled(false);
            chart.setTouchEnabled(true);
            chart.setDragDecelerationFrictionCoef(0.95f);
            chart.animateY(1000);
            Legend legend = chart.getLegend();
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);
            legend.setTextColor(Color.WHITE); // Cor da legenda
            legend.setTextSize(12f);
            legend.setFormSize(10f);

        }
        updateChartVisibility();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setCurrentDate() {
        try {
            LocalDate currentDate = LocalDate.now();

            // Preencher ano com o ano atual (yyyy)
            String currentYear = String.valueOf(currentDate.getYear());
            anoEditText.setText(currentYear);

            // Preencher mês com o mês atual (MM) - adiciona zero à esquerda se necessário
            String currentMonth = String.format("%02d", currentDate.getMonthValue());
            mesEditText.setText(currentMonth);

        } catch (Exception e) {
            showToast("Erro ao definir data atual: " + e.getMessage());
        }
    }
}