package com.app.fintoday;

import android.content.Intent;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.model.GradientColor;

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

        Button resumoDespGrafButton = findViewById(R.id.idBtnFazerResumo);
        resumoDespGrafButton.setOnClickListener(v -> {
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
    } // FIM ON CREATE


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
        Toast.makeText(getApplicationContext(),"generateCharts Metodo Chamado", Toast.LENGTH_SHORT).show();

        // Agrupar por tipoDesp e somar os valores
        Map<String, Float> valoresPorTipo = new HashMap<>();
        String[] tipos = {"ALIM", "CRED", "D PUB", "EDUC", "EMPRES", "INVEST", "LAZER", "OUTR", "TRANS", "SAUD"};

        for (String tipo : tipos) {
            valoresPorTipo.put(tipo, 1f);
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
                switch (entry.getKey()) {
                    case "D PUB":
                        labels.add("D PUB");
                        break;
                    case "INVEST":
                        labels.add("INV");
                        break;
                    case "OUTR":
                        labels.add("OUTR");
                        break;
                    default:
                        labels.add(entry.getKey());
                        break;
                }
                //labels.add(entry.getKey().replace(" ", "\n"));
                //labels.add(entry.getKey());
                i++;
            }
        }

        // obter cores do colors.xml
        int blue_500 = ContextCompat.getColor(this, R.color.blue_500);
        int green_200 = ContextCompat.getColor(this, R.color.green_200);
        int laranja = ContextCompat.getColor(this, R.color.laranja);
        int teal_150 = ContextCompat.getColor(this, R.color.teal_150);
        int vermelho = ContextCompat.getColor(this, R.color.red);
        int colorAccent = ContextCompat.getColor(this, R.color.colorAccent);
        int magenta = ContextCompat.getColor(this, R.color.magenta);
        int azulclaro = ContextCompat.getColor(this, R.color.azulclaro);
        int azulescuro = ContextCompat.getColor(this, R.color.azulescuro);
        int amarelo_canario = ContextCompat.getColor(this, R.color.amarelo_canario);


        // Configurar gráfico de Pizza
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        //pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setColors(new int[] {blue_500,vermelho, azulescuro,amarelo_canario, green_200, laranja, teal_150, colorAccent, magenta}); // Usando cores do colors.xml
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueFormatter(new PercentFormatter(pieChart));
        pieDataSet.setValueTextColor(Color.WHITE); // Cor do texto dos valores
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);


        // Configurar gráfico de Barras
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        BarData barData = new BarData(barDataSet);
        List<GradientColor> gradientColors = new ArrayList<>();
        gradientColors.add(new GradientColor(vermelho, Color.WHITE));
        gradientColors.add(new GradientColor(azulescuro, Color.WHITE));
        gradientColors.add(new GradientColor(amarelo_canario, Color.WHITE));
        gradientColors.add(new GradientColor(green_200, Color.WHITE));
        gradientColors.add(new GradientColor(laranja, Color.WHITE));
        gradientColors.add(new GradientColor(teal_150, Color.WHITE));
        gradientColors.add(new GradientColor(colorAccent, Color.WHITE));
        gradientColors.add(new GradientColor(magenta, Color.WHITE));

        barDataSet.setGradientColors(gradientColors);
        barDataSet.setValueTextSize(12f);
        barDataSet.setValueTextColor(Color.WHITE); //OK
        barChart.setData(barData);
     // barChart.setExtraOffsets(20f, 20f, 20f, 20f); // Espaço para eixos

     // Configurar eixo X para barras
        XAxis xAxisBar = barChart.getXAxis();
        xAxisBar.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBar.setGranularity(1f);
        xAxisBar.setDrawGridLines(false);
        xAxisBar.setTextColor(Color.WHITE); // Cor do texto do eixo X
        xAxisBar.setTextSize(11f);
        xAxisBar.setLabelRotationAngle(-30f); // Gira os rótulos para melhor ajuste
        xAxisBar.setLabelCount(labels.size());
        xAxisBar.setAvoidFirstLastClipping(true);

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
        lineDataSet.setColors(new int[] {vermelho, azulescuro, amarelo_canario, green_200, laranja, teal_150, colorAccent, magenta});
        lineDataSet.setValueTextSize(12f);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setFillAlpha(120); // Valor entre 0 (transparente) e 255 (opaco)
        lineDataSet.setLineWidth(3f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Suaviza as curvas
        //lineDataSet.enableDashedLine(10f, 5f, 0f); // (tamanho da linha, tamanho do espaço, fase)
        //lineDataSet.setCubicIntensity(0.2f); // Intensidade da curva

        lineChart.setExtraBottomOffset(30f); //espacamento

// ADICIONE ESTAS LINHAS PARA O PREENCHIMENTO COM GRADIENTE:
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_fill));
        lineDataSet.setFillAlpha(128); // Opacidade (0-255)

// Configuração específica para o gráfico de linhas
        Legend lineLegend = lineChart.getLegend();
        lineLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        lineLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        lineLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        lineLegend.setDrawInside(false);
        lineLegend.setTextColor(Color.WHITE);
        lineLegend.setForm(Legend.LegendForm.CIRCLE);
        lineLegend.setFormSize(10f);
        lineLegend.setTextSize(12f);
        lineLegend.setXEntrySpace(15f);  // Espaço horizontal entre itens
        lineLegend.setYEntrySpace(5f);   // Espaço vertical entre linhas
        lineLegend.setYOffset(25f);      // Distância do fundo do gráfico
        lineLegend.setWordWrapEnabled(true); // Permite quebrar em múltiplas linhas
        lineLegend.setMaxSizePercent(0.95f); // Usa 95% da largura disponível

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

// Configurações do eixo X
        XAxis xAxisLine = lineChart.getXAxis();
        xAxisLine.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxisLine.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisLine.setGranularity(1f);
        xAxisLine.setDrawGridLines(false);
        xAxisLine.setTextColor(Color.WHITE);
        xAxisLine.setTextSize(11f);
        xAxisLine.setYOffset(15f); // Aumenta o espaço entre os rótulos e a legenda

// Configurações do eixo Y
        YAxis yAxisLeftLine = lineChart.getAxisLeft();
        yAxisLeftLine.setGranularity(1f);
        yAxisLeftLine.setTextColor(Color.WHITE);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragDecelerationFrictionCoef(0.95f);
        lineChart.setScaleEnabled(true);
        lineChart.animateY(1000);
        lineChart.setExtraBottomOffset(25f); // Aumenta espaço inferior
        lineChart.invalidate();

// Configurações comuns para todos os gráficos
        for (Chart chart : new Chart[]{pieChart, barChart}) {
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
         // legend.setTextSize(12f);
            legend.setForm(Legend.LegendForm.CIRCLE); // Mais visível no tema escuro
           // legend.setFormSize(10f);
        }
        updateChartVisibility();


        // Adicionar listener de clique para os gráficos
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                abrirTelaFiltrada(pe.getLabel());
            }

            @Override
            public void onNothingSelected() {}
        });

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String tipo = labels.get((int) e.getX());
                abrirTelaFiltrada(tipo);
            }

            @Override
            public void onNothingSelected() {}
        });

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String tipo = labels.get((int) e.getX());
                abrirTelaFiltrada(tipo);
            }

            @Override
            public void onNothingSelected() {}
        });

    } //fim generateCharts

    private void abrirTelaFiltrada(String tipo) {
        String ano = anoEditText.getText().toString();
        String mes = mesEditText.getText().toString();

        Intent intent = new Intent(this, ResultBuscaGrafActivity.class);
        intent.putExtra("TIPO_SELECIONADO", tipo);  // Usar o parâmetro 'tipo' ao invés da variável não declarada
        intent.putExtra("ANO", ano);
        intent.putExtra("MES", mes);
        startActivity(intent);
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