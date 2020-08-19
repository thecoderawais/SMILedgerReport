package com.example.ledgerreport.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.Models.LedgerReportModel;
import com.example.ledgerreport.Models.TbAccountsModel;
import com.example.ledgerreport.R;
import com.example.ledgerreport.Utils.CONST;
import com.example.ledgerreport.adapters.CombinedLedgerAccountAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CombinedLedger extends Fragment {

    private String fileName = "";
    private List<LedgerReportModel> ledgerReportsList;
    private CombinedLedgerAccountAdapter adapter;
    private int count = 1;
    private ArrayList<String> accounts;
    private ArrayList<String> spinnerAccounts;
    private ApiInterface apiInterface;
    private FloatingActionButton fab;

    Paint paint = new Paint(), titlePaint = new Paint();
    Bitmap bmp, scaledBmp;
    int pageWidth = 1200, rowYAxis = 760;
    Date dateObj;
//    DateFormat format;

    PdfDocument doc = new PdfDocument();
    PdfDocument.PageInfo pageInfo;
    PdfDocument.Page page1;

    Canvas canvas;

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

        spinnerAccounts = new ArrayList<>();
        accounts = new ArrayList<>();
        accounts.add(String.valueOf(count));
        pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
        page1 = doc.startPage(pageInfo);
        canvas = page1.getCanvas();

        ledgerReportsList = new ArrayList<>();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CONST.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        scaledBmp = Bitmap.createScaledBitmap(bmp, 800, 350, false);
        dateObj = new Date();


        apiInterface = retrofit.create(ApiInterface.class);
        getAllAccounts();
        fab = Objects.requireNonNull(getActivity()).findViewById(R.id.floatingActionButton);
        adapter = new CombinedLedgerAccountAdapter(getContext(), accounts, spinnerAccounts);
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
                                        spinnerAccounts.add(item.getAC_CODE() + " -- " + item.getAC_NAME());
                                    }
                                    adapter.notifyDataSetChanged();
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

    //Retrofit API Data filling
    public void getLedgerReportData(final String accCode, final String fromDate,  final String toDate){
        Call<List<LedgerReportModel>> call = apiInterface.getLedgerReportData(accCode,  fromDate, toDate);

        if (call != null){
            try {
                call.enqueue(new Callback<List<LedgerReportModel>>() {
                    @Override
                    public void onResponse(Call<List<LedgerReportModel>> call, Response<List<LedgerReportModel>> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null){
                                ledgerReportsList.clear();
                                ledgerReportsList.addAll(response.body());
                                Log.d(getString(R.string.txtLogTag), "No of Entries Found: " + ledgerReportsList.size());
                            }else{
                                Log.d(getString(R.string.txtLogTag), "Response was not successful!");
                            }
                        }catch (Exception e){
                            Log.d(getString(R.string.txtLogTag), "Exception While Populating List: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LedgerReportModel>> call, Throwable t) {
                        Log.d(getString(R.string.txtLogTag), "onFailure: " + t.getMessage());
                        Toast.makeText(getContext(), "Retrofit onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }catch (Exception e){
                Log.d(getString(R.string.txtLogTag), "getLedgerReportData Exception: " + e.getMessage());
            }
        }else{
            Log.d(getString(R.string.txtLogTag), "Call was null");
        }
    }


}