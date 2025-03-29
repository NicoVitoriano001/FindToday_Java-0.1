package com.gtappdevelopers.findtoday;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import java.util.List;

public class ResumoDialogFragment extends DialogFragment {
    private List<FinModal> data;

    public static ResumoDialogFragment newInstance(List<FinModal> data) {
        ResumoDialogFragment fragment = new ResumoDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", new ArrayList<>(data));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_result_resumo, container, false);

        if (getArguments() != null) {
            data = (List<FinModal>) getArguments().getSerializable("data");
        }

        TableLayout tableLayout = view.findViewById(R.id.resultadosTable);

        if (data != null && !data.isEmpty()) {
            for (FinModal item : data) {
                TableRow row = new TableRow(getContext());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                addTextViewToRow(row, item.getDataDesp(), 8);
                addTextViewToRow(row, item.getDespDescr(), 8);
                addTextViewToRow(row, item.getValorDesp(), 8);
                addTextViewToRow(row, item.getTipoDesp(), 8);
                addTextViewToRow(row, item.getFontDesp(), 8);

                tableLayout.addView(row);
            }
        }

        Button closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void addTextViewToRow(TableRow row, String text, int padding) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(padding, padding, padding, padding);
        textView.setSingleLine(true);
        row.addView(textView);
    }
}