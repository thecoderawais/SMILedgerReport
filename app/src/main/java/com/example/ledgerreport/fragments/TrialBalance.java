package com.example.ledgerreport.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.Models.TrialBalanceModel;
import com.example.ledgerreport.R;
import com.example.ledgerreport.Utils.CONST;
import com.example.ledgerreport.Utils.MySharedPreference;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

public class TrialBalance extends Fragment {

    DatePicker toDate;
    Button btnSubmitTrialBalance;

    ApiInterface apiInterface;

    int dayTo, monthTo, yearTo;
    String to = "", fileName;
    PdfDocument pdfDoc;
    Document doc;

    // The second argument determines 'large table' functionality is used
    // It defines whether parts of the table will be written before all data is added.
    Table table = new Table(UnitValue.createPercentArray(4), true);

    ArrayList<TrialBalanceModel> trialBalances = new ArrayList<>();

    public TrialBalance() {
    }

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

        apiInterface = retrofit.create(ApiInterface.class);

        toDate = view.findViewById(R.id.trialBalanceToDate);
        btnSubmitTrialBalance = view.findViewById(R.id.btnSubmitTrialBalance);


        btnSubmitTrialBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            dayTo = toDate.getDayOfMonth();
            monthTo = toDate.getMonth();
            yearTo = toDate.getYear();

            to = yearTo + "-" + monthTo + "-" + dayTo;

            getTrialBalanceData(to, view);
            }
        });
    }

    //Retrofit API Data filling
    public void getTrialBalanceData(final String toDate, final View view){
        Call<List<TrialBalanceModel>> call = apiInterface.getTrialBalanceData(toDate);
        Snackbar.make(view, "Processing your request...", Snackbar.LENGTH_LONG);

        if (call != null){
            try {
                call.enqueue(new Callback<List<TrialBalanceModel>>() {
                    @Override
                    public void onResponse(Call<List<TrialBalanceModel>> call, Response<List<TrialBalanceModel>> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null){
                                trialBalances.clear();
                                for (TrialBalanceModel item :
                                        response.body()) {
                                    if (Integer.parseInt(item.getBAL()) != 0){
                                        trialBalances.add(item);
                                    }
                                }
//                                trialBalances.addAll(response.body());
                                Snackbar.make(view, "Found " + trialBalances.size() + " Entries.", Snackbar.LENGTH_LONG);

                                Log.d(getString(R.string.txtLogTag), "Found " + trialBalances.size() + " Entries.");

                                fileName = getExternalStorageDirectory() +
                                        "/TrialBalances/TrialBalance Till " + to + ".pdf";

                                File file = new File(fileName);
                                Objects.requireNonNull(file.getParentFile()).mkdirs();

                                pdfDoc = new PdfDocument(new PdfWriter(fileName));
                                doc = new Document(pdfDoc);

                                addTopItems(to);

                                drawTable();

                                for (int i = 0 ; i < trialBalances.size() ; i++){
                                    if (i % 4 == 0){
                                        table.flush();
                                    }

                                    if(Integer.parseInt(trialBalances.get(i).getBAL()) > 0){
                                        addDebitRow(trialBalances.get(i));
                                    }else{
                                        trialBalances.get(i).setBAL(String.valueOf
                                                (Integer.parseInt(trialBalances.get(i).getBAL()) * (-1)));
                                        addCreditRow(trialBalances.get(i));
                                    }
                                }

                                table.complete();
                                doc.close();
                            }else{

                                Log.d(getString(R.string.txtLogTag), "Response was not successful!");
                            }
                        }catch (Exception e){
                            Log.d(getString(R.string.txtLogTag), "Exception While Populating List: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TrialBalanceModel>> call, Throwable t) {
                        Log.d(getString(R.string.txtLogTag), "onFailure: " + t.getMessage());
                        Snackbar.make(view, "No Data Found for the Entered Parameters!", Snackbar.LENGTH_LONG);
                        Toast.makeText(getContext(), "No Data Found for the Entered Parameters!", Toast.LENGTH_LONG).show();
                    }
                });
            }catch (Exception e){
                Log.d(getString(R.string.txtLogTag), "getLedgerReportData Exception: " + e.getMessage());
            }
        }else{
            Log.d(getString(R.string.txtLogTag), "Call was null");
        }
    }

    public void addTopItems(String to){
        Text title1 = new Text(new MySharedPreference(getContext()).getCompanyName("companyName")).setFontSize(22);
        Text title2 = new Text("Trial Balance ").setFontSize(16);
        Text author = new Text(to);
        Paragraph p = new Paragraph().setFontSize(16)
                .add(title1).add(title2).add(" Till ").add(author);
        doc.add(p);
    }

    public void drawTable(){

        table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Ac# ")));
        table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Ac Title ")));
        table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Debit ")));
        table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Credit ")));

        // For the "large tables" they shall be added to the document before its child elements are populated
        doc.add(table);
    }

    public void addDebitRow(TrialBalanceModel item){
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getAC_CODE())
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getAC_NAME())
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getBAL())
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph("0")
                .setMargins(0, 0, 0, 0)));
    }

    public void addCreditRow(TrialBalanceModel item){
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getAC_CODE())
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getAC_NAME())
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph("0")
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getBAL())
                .setMargins(0, 0, 0, 0)));
    }

}