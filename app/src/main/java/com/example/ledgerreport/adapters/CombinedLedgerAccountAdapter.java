package com.example.ledgerreport.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ledgerreport.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;

public class CombinedLedgerAccountAdapter extends RecyclerView.Adapter<CombinedLedgerAccountAdapter.ViewHolder> {

    private ArrayList<String> accounts;
    private LayoutInflater mInflater;
    private ArrayList<String> selectedAccountsArray;
    private ArrayList<Integer> selectedAccountsIndices;
    ArrayAdapter<String> accountsArrayAdapter;


    public CombinedLedgerAccountAdapter(Context context, ArrayAdapter<String> accountsArrayAdapter,
                                        ArrayList<String> accounts) {
        this.mInflater = LayoutInflater.from(context);
        this.accounts = accounts;
        this.accountsArrayAdapter = accountsArrayAdapter;
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

        holder.spinner.setAdapter(accountsArrayAdapter);

        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = holder.getAdapterPosition();

                if(selectedAccountsArray.size() >= index + 1){
                    selectedAccountsArray.set(index, adapterView.getItemAtPosition(i).toString());
                    selectedAccountsIndices.set(index, index);
                }else{
                    selectedAccountsArray.add(adapterView.getItemAtPosition(i).toString());
                    selectedAccountsIndices.add(index);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SearchableSpinner spinner = itemView.findViewById(R.id.combinedLedgerSpinner);
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
