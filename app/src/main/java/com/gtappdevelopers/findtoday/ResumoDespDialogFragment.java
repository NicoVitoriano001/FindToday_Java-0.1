package com.gtappdevelopers.findtoday;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ResumoDespDialogFragment extends DialogFragment {
    private List<FinModal> data;

    public static ResumoDespDialogFragment newInstance(List<FinModal> data) {
        ResumoDespDialogFragment fragment = new ResumoDespDialogFragment();
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

        // Aqui está a utilização do listView
        ListView listView = view.findViewById(R.id.listView);

        if (data != null && !data.isEmpty()) {
            List<String> displayData = new ArrayList<>();
            for (FinModal item : data) {
                displayData.add(item.getDataDesp() + " - " + item.getDespDescr() + " - " + item.getValorDesp() + " - " + item.getTipoDesp() + " - " + item.getFontDesp());
            }

            // Configurando o adaptador para o ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, displayData);
            listView.setAdapter(adapter); // Aqui é onde listView é utilizado
        }

        Button closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }
}
