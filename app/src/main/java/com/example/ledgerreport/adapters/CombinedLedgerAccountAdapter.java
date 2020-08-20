package com.example.ledgerreport.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ledgerreport.R;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class CombinedLedgerAccountAdapter extends RecyclerView.Adapter<CombinedLedgerAccountAdapter.ViewHolder> {

    private ArrayList<String> accounts;
    private LayoutInflater mInflater;
    private ArrayList<String> spinnerAccounts;
    private ArrayList<String> selectedAccountsArray;
    private ArrayList<Integer> selectedAccountsIndices;

    public CombinedLedgerAccountAdapter(Context context, ArrayList<String> accounts, ArrayList<String> spinnerAccounts) {
        this.mInflater = LayoutInflater.from(context);
        this.accounts = accounts;
        this.spinnerAccounts = spinnerAccounts;
        selectedAccountsArray = new ArrayList<>();
        selectedAccountsIndices = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.account_spinner_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.spinner.setItems(spinnerAccounts);

        holder.spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                int index = holder.getLayoutPosition();

                if(selectedAccountsArray.size() >= index + 1){
                    selectedAccountsArray.set(index, item);
                    selectedAccountsIndices.set(index, index);
                }else{
                    selectedAccountsArray.add(item);
                    selectedAccountsIndices.add(index);
                }
                accounts.remove(item);
            }
        });


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

    public ArrayList<String> getSelectedAccounts(){
        return selectedAccountsArray;
    }

    public ArrayList<Integer> getSelectedAccountsIndies(){
        return selectedAccountsIndices;
    }
}
