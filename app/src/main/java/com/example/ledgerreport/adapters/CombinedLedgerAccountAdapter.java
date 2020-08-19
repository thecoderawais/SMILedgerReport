package com.example.ledgerreport.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.R;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class CombinedLedgerAccountAdapter extends RecyclerView.Adapter<CombinedLedgerAccountAdapter.ViewHolder> {

    private ArrayList<String> accounts;
    private LayoutInflater mInflater;

    public CombinedLedgerAccountAdapter(Context context, ArrayList<String> accounts) {
        this.mInflater = LayoutInflater.from(context);
        this.accounts = accounts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.account_spinner_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.spinner.setItems(accounts);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialSpinner spinner = itemView.findViewById(R.id.combinedLedgerSpinner);
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
