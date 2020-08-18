package com.example.ledgerreport.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ledgerreport.R;
import com.example.ledgerreport.adapters.CombinedLedgerAccountAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CombinedLedger extends Fragment {

    private int count = 1;
    private ArrayList<String> accounts;
    private FloatingActionButton fab;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_combined_ledger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accounts = new ArrayList<>();
        accounts.add(String.valueOf(count));
        fab = Objects.requireNonNull(getActivity()).findViewById(R.id.floatingActionButton);
        final CombinedLedgerAccountAdapter adapter = new CombinedLedgerAccountAdapter(getContext(), accounts);
        final RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.combinedLedgerRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                accounts.add(String.valueOf(count));
                adapter.notifyDataSetChanged();
            }
        });


    }
}