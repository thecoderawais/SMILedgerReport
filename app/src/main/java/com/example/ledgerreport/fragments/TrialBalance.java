package com.example.ledgerreport.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ledgerreport.R;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TrialBalance extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trial_balance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialSpinner spinner = (MaterialSpinner) Objects.requireNonNull(getActivity())
                .findViewById(R.id.trialBalanceSpinner);
        spinner.setItems(1,2,3,4,5);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Integer>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Integer item) {
                Snackbar.make(view, "Clicked " + item.toString(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}