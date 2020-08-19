package com.example.ledgerreport.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.Models.TbAccountsModel;
import com.example.ledgerreport.R;
import com.example.ledgerreport.Utils.CONST;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrialBalance extends Fragment {

    ApiInterface apiInterface;
    ArrayList<String> accounts;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CONST.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        accounts = new ArrayList<>();

        apiInterface = retrofit.create(ApiInterface.class);
        getAllAccounts();

        MaterialSpinner spinner = (MaterialSpinner) Objects.requireNonNull(getActivity())
                .findViewById(R.id.trialBalanceSpinner);
        spinner.setItems(accounts);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
            }
        });
    }
    public void getAllAccounts(){
        Call<List<TbAccountsModel>> call = apiInterface.getAllTbAccounts();
        if (call != null) {
            try {
                call.enqueue(new Callback<List<TbAccountsModel>>() {
                    @Override
                    public void onResponse(Call<List<TbAccountsModel>> call, Response<List<TbAccountsModel>> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().size() > 0) {
//                                    Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                    for(TbAccountsModel item : response.body()){
                                        accounts.add(item.getAC_CODE() + " -- " + item.getAC_NAME());
                                    }
                                    Log.d(getString(R.string.txtLogTag), "User Logged in!");
                                } else {
                                    Toast.makeText(getContext(), "Sorry, but your account is not activated!", Toast.LENGTH_SHORT).show();
                                    Log.d(getString(R.string.txtLogTag), "Account not activated!");
                                }
                            } else {
                                Toast.makeText(getContext(), "Wrong Credentials!", Toast.LENGTH_SHORT).show();
                                Log.d(getString(R.string.txtLogTag), "Response was not successful!");
                            }
                        } catch (Exception e) {
                            Log.d(getString(R.string.txtLogTag), "Error while getting User's info: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TbAccountsModel>> call, Throwable t) {
                        Log.d(getString(R.string.txtLogTag), "onFailure: " + t.getMessage());
                        Toast.makeText(getContext(), "Retrofit onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.d(getString(R.string.txtLogTag), "loginUser Exception: " + e.getMessage());
            }
        } else {
            Log.d(getString(R.string.txtLogTag), "Call was null");
        }
    }
}