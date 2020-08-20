package com.example.ledgerreport.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.Models.LedgerReportModel;
import com.example.ledgerreport.Models.TbAccountsModel;
import com.example.ledgerreport.PDFDocumentAdapter;
import com.example.ledgerreport.PdfViewActivity;
import com.example.ledgerreport.R;
import com.example.ledgerreport.Utils.CONST;
import com.example.ledgerreport.adapters.CombinedLedgerAccountAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

public class CombinedLedger extends Fragment {

    int loggedInUser, currentAccountIndex, pages= 1;

    SharedPreferences sharedPreference;

    int dayFrom, monthFrom, yearFrom, dayTo, monthTo, yearTo;
    String from = "", to = "";
    DatePicker fromDate, toDate;

    private String fileName = "", whereClause, currentAccount;
    private List<LedgerReportModel> ledgerReportsList;
    private CombinedLedgerAccountAdapter adapter;
    private int count = 1;
    private ArrayList<String> accounts;
    private ArrayList<String> selectedAccounts;
    private ArrayList<String> selectedAccountNames = new ArrayList<>();
    private ArrayList<Integer> selectedAccountsIndices;
    private ArrayList<String> spinnerAccounts;
    private ApiInterface apiInterface;

    Paint paint = new Paint(), titlePaint = new Paint();
    int pageWidth = 1200, rowYAxis = 420;
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

        Log.d(getString(R.string.txtLogTag), "Started Combined Ledger Activity!");

        Button btnSubmit = view.findViewById(R.id.btnSubmitCombinedLedger);

        sharedPreference = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        loggedInUser = sharedPreference.getInt(getString(R.string.prefKey), 0);

        spinnerAccounts = new ArrayList<>();
        accounts = new ArrayList<>();
        accounts.add(String.valueOf(count));

        ledgerReportsList = new ArrayList<>();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CONST.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dateObj = new Date();


        apiInterface = retrofit.create(ApiInterface.class);
        getAllAccounts();
        FloatingActionButton fab = Objects.requireNonNull(getActivity()).findViewById(R.id.floatingActionButton);
        adapter = new CombinedLedgerAccountAdapter(getContext(), accounts, spinnerAccounts);
        final RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.combinedLedgerRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        fromDate = view.findViewById(R.id.combinedLedgerFromDate);
        toDate = view.findViewById(R.id.combinedLedgerToDate);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                accounts.add(String.valueOf(count));
                adapter.notifyDataSetChanged();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dayFrom = fromDate.getDayOfMonth();
                monthFrom = fromDate.getMonth();
                yearFrom = fromDate.getYear();

                dayTo = toDate.getDayOfMonth();
                monthTo = toDate.getMonth();
                yearTo = toDate.getYear();

                from = yearFrom + "-" + monthFrom + "-" + dayFrom;
                to = yearTo + "-" + monthTo + "-" + dayTo;

                selectedAccounts = adapter.getSelectedAccounts();
                selectedAccountsIndices = adapter.getSelectedAccountsIndies();

                Log.d(getString(R.string.txtLogTag), "Got " + selectedAccounts.size() + " Selected Accounts back...");
                Log.d(getString(R.string.txtLogTag), "Showing Selected Accounts...");

                whereClause = " WHERE AC_NO IN ( ";
                int count = 1;

                for (String item :
                        selectedAccounts) {
                    Log.d(getString(R.string.txtLogTag), "Selected Accounts:  " + item);
                    whereClause = whereClause.concat(item.split("--")[0]);
                    selectedAccountNames.add(item.split("--")[1]);
                    if (count <  selectedAccounts.size()){
                        whereClause = whereClause.concat(", ");
                    }
                    count++;
                }
                whereClause = whereClause.concat(") AND V_DATE BETWEEN '" + from + "' AND '" + to + "' ORDER BY AC_NO");

                getLedgerReportData(whereClause, view);
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
                                        spinnerAccounts.add(item.getAC_CODE() + "--" + item.getAC_NAME());
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
    public void getLedgerReportData(final String whereClause, final View v){
        Call<List<LedgerReportModel>> call = apiInterface.getCombinedLedgerReportData(whereClause);

        if (call != null){
            try {
                call.enqueue(new Callback<List<LedgerReportModel>>() {
                    @Override
                    public void onResponse(Call<List<LedgerReportModel>> call, Response<List<LedgerReportModel>> response) {
                        try {
                            if (response.isSuccessful() ){
                                if (response.body() != null)
                                {
                                    ledgerReportsList.clear();
                                    ledgerReportsList.addAll(response.body());
                                    Log.d(getString(R.string.txtLogTag), "Combined Ledger total Entries: " + ledgerReportsList.size());
                                    if (ledgerReportsList.size() > 0){
                                        try{

                                            Log.d(getString(R.string.txtLogTag), "Updating all Balances...");

                                            ledgerReportsList.get(0).setOPENING_BALANCE(1503350);
                                            int balance = ledgerReportsList.get(0).getOPENING_BALANCE();

                                            //                    For updating all balances
                                            for (LedgerReportModel item :
                                                    ledgerReportsList) {
                                                balance = balance + item.getVDEBIT() - item.getV_CREDIT();
                                                item.setBALANCE(balance);
                                            }

                                            //
                                            dateObj = new Date();

                                            Log.d(getString(R.string.txtLogTag), "Setting Page");

                                            //Setting Pages
                                            pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
                                            page1 = doc.startPage(pageInfo);
                                            canvas = page1.getCanvas();

                                            Log.d(getString(R.string.txtLogTag), "Adding Report Top Items");
                                            addReportTopItems(from, to);

                                            Log.d(getString(R.string.txtLogTag), "Drawing Table......");
                                            drawTable(350);

                                            currentAccountIndex = 0;
                                            currentAccount = selectedAccountNames.get(currentAccountIndex);
                                            addAccountHeading(currentAccount, "15000", rowYAxis);
                                            rowYAxis += 120;
                                            Log.d(getString(R.string.txtLogTag), "Starting to add Rows...");
                                            int i = 0;
                                            for (LedgerReportModel item :
                                                    ledgerReportsList) {
                                                i++;
                                                Log.d(getString(R.string.txtLogTag), "Adding Row:" + i + " To Table");
                                                addRow(item);
                                            }
                                            doc.finishPage(page1);

                                            fileName = getExternalStorageDirectory() +
                                                    "/LedgerReports/LedgerReport:" + ledgerReportsList.get(0).getAC_NAME() + ".pdf";
                                            File file = new File(fileName);

                                            try {
                                                Log.d(getString(R.string.txtLogTag), "Saving File to : " + fileName);
                                                doc.writeTo(new FileOutputStream(file));
                                                Log.d(getString(R.string.txtLogTag), "File Saved to: " + fileName + "Successfully!");
                                                Toast.makeText(getContext(),
                                                        "Saved! at " + fileName, Toast.LENGTH_SHORT).show();

                                                Log.d(getString(R.string.txtLogTag), "About to Start Dialog.Show()......");
                                                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));

                                                Log.d(getString(R.string.txtLogTag), "Setting Dialog Message & Title");
                                                builder.setMessage(R.string.dialog_message)
                                                        .setTitle(R.string.dialog_title);

                                                Log.d(getString(R.string.txtLogTag), "Creating Dialog......");

                                                // Button to View PDF in Other Activity....
                                                builder.setPositiveButton("View PDF", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        try {
                                                            Log.d(getString(R.string.txtLogTag), "User Selected to Open PDF");
                                                            Log.d(getString(R.string.txtLogTag), "Opening file in PDF Viewer Activity");
                                                            Intent intent = new Intent(getContext(), PdfViewActivity.class);
                                                            intent.putExtra("fileName", fileName);
                                                            startActivity(intent);
                                                        } catch (Exception e) {
                                                            Toast.makeText(getContext(), "Couldn't Open the file Specified!", Toast.LENGTH_LONG).show();
                                                            Log.d(getString(R.string.txtLogTag), "Open File Exception: " + e.getMessage());
                                                        }
                                                    }
                                                });

                                                //Button to Print PDF....
                                                builder.setNegativeButton("Print PDF", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        Log.d(getString(R.string.txtLogTag), "User Selected to Print the PDF");
                                                        printPDF(fileName);
                                                    }
                                                });

                                                //Button to dismiss the Dialog........
                                                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Log.d(getString(R.string.txtLogTag), "Dismissed the Dialog Box");
                                                        dialog.cancel();
                                                    }
                                                });
                                                builder.create().show();
                                            } catch (Exception e) {
                                                Log.d(getString(R.string.txtLogTag), "Save File Exc: " + e.getMessage());

                                            }
                                        }catch (Exception e){
                                            Log.d(getString(R.string.txtLogTag), "Exc after checking for Server Received Data: " + e.getMessage());
                                        }
                                    }
                                }
                                else{
                                    Snackbar.make(v, "Nothing Found!", Snackbar.LENGTH_LONG).show();
                                }
                            }else{
                                Log.d(getString(R.string.txtLogTag), "Response was not successful!");
                                Snackbar.make(v, "No Response from the Server!", Snackbar.LENGTH_LONG).show();
                            }
                        }catch (Exception e){
                            Log.d(getString(R.string.txtLogTag), "Exception While Populating List: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LedgerReportModel>> call, Throwable t) {
                        Log.d(getString(R.string.txtLogTag), "onFailure: " + t.getMessage());
                        Snackbar.make(v, "Couldn't connect to the Server or some other problem.", Snackbar.LENGTH_LONG).show();
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


    //Report top Items
    @SuppressLint("SimpleDateFormat")
    public void addReportTopItems(String fromDate, String toDate)
    {
        try {
            //Add Title
            titlePaint.setTextAlign(Paint.Align.CENTER);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            titlePaint.setTextSize(70);
            canvas.drawText("Combined Ledger Report", pageWidth/2, 50, titlePaint);


            paint.setColor(Color.BLACK);
            paint.setTextSize(35f);
            paint.setTextAlign(Paint.Align.LEFT);

            canvas.drawText(" From:   " + fromDate, 20, 150, paint);
            canvas.drawText(" To:        " + toDate, 20, 225, paint);
            canvas.drawText(" As On:  " + new SimpleDateFormat("E, dd-MMM-yyyy").format(dateObj), 730, 150, paint);
            canvas.drawText("                          " + new SimpleDateFormat("hh:mm a").format(dateObj), 730, 225, paint);
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "addReportTopItems Exception: " + e.getMessage());
        }

    }

    public void addAccountHeading(String accName, String balance, int yAxis)
    {
        try {
            Log.d(getString(R.string.txtLogTag), "Adding new Account Heading: " + accName);
            //Add Title
            titlePaint.setTextAlign(Paint.Align.LEFT);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            titlePaint.setTextSize(45);
            canvas.drawText("Acc: " + accName, 20, yAxis, titlePaint);


            paint.setColor(Color.BLACK);
            paint.setTextSize(35f);
            paint.setTextAlign(Paint.Align.LEFT);

            canvas.drawText(" Balance: " + balance, 730, yAxis, paint);
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "addAccountHeadingError Exception: " + e.getMessage());
        }

    }

    //Drawing the table
    public void drawTable(int yAxis)
    {
        try {
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(40f);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Date", 40, yAxis, paint);
            canvas.drawText("V. #", 180, yAxis, paint);
            canvas.drawText("Description", 280, yAxis, paint);
            canvas.drawText("Debit", 620, yAxis, paint);
            canvas.drawText("Credit", 820, yAxis, paint);
            canvas.drawText("Balance", 1020, yAxis, paint);
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "drawTable Exception: " + e.getMessage());
        }
    }

    //Called each time for adding the row.
    @SuppressLint("SimpleDateFormat")
    public void addRow(LedgerReportModel ledgerEntry)
    {
        if(rowYAxis <= 2000){ //If there is space left on the current page.
            try {
//            canvas.drawText(String.valueOf(ledgerEntry.getV_DATE()), 40, rowYAxis, paint); //Date

                if (currentAccount.equals(ledgerEntry.getAC_NAME())){
                    paint.setTextSize(30f);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    try {
                        canvas.drawText(new SimpleDateFormat("dd-MM-yy").format(ledgerEntry.getV_DATE()), 40, rowYAxis, paint); //Date
                    }catch (Exception e){
                        canvas.drawText("Date Error", 40, rowYAxis, paint); //Date
                    }
                    canvas.drawText(ledgerEntry.getV_NO(), 180, rowYAxis, paint); //V No
                    canvas.drawText(ledgerEntry.getDESCRIPTION(), 280, rowYAxis, paint); //Description
                    canvas.drawText(String.valueOf(ledgerEntry.getVDEBIT()), 620, rowYAxis, paint); //Debit
                    canvas.drawText(String.valueOf(ledgerEntry.getV_CREDIT()), 820, rowYAxis, paint); //Credit
                    canvas.drawText(String.valueOf(ledgerEntry.getBALANCE()), 1020, rowYAxis, paint); //Balance
                }else{
                    currentAccountIndex++;
                    currentAccount = selectedAccountNames.get(currentAccountIndex);
                    addAccountHeading(currentAccount, "15000", rowYAxis);
                    rowYAxis += 120;

                    drawTable(rowYAxis);
                    rowYAxis += 80;

                    paint.setTextSize(30f);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    try {
                        canvas.drawText(new SimpleDateFormat("dd-MM-yy").format(ledgerEntry.getV_DATE()), 40, rowYAxis, paint); //Date
                    }catch (Exception e){
                        canvas.drawText("Date Error", 40, rowYAxis, paint); //Date
                    }
                    canvas.drawText(ledgerEntry.getV_NO(), 180, rowYAxis, paint); //V No
                    canvas.drawText(ledgerEntry.getDESCRIPTION(), 280, rowYAxis, paint); //Description
                    canvas.drawText(String.valueOf(ledgerEntry.getVDEBIT()), 620, rowYAxis, paint); //Debit
                    canvas.drawText(String.valueOf(ledgerEntry.getV_CREDIT()), 820, rowYAxis, paint); //Credit
                    canvas.drawText(String.valueOf(ledgerEntry.getBALANCE()), 1020, rowYAxis, paint); //Balance
                }

            }catch (Exception e){
                Log.d(getString(R.string.txtLogTag), "Exception while adding Row: " + e.getMessage());
            }
        }else{ //If the page's height has been filled!
            Log.d(getString(R.string.txtLogTag), "Finishing Prev. Page ");

            paint.setTextSize(30f);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            canvas.drawText("Page: " + pages, 180, rowYAxis, paint); //V No
            pages++;
            doc.finishPage(page1);
            Log.d(getString(R.string.txtLogTag), "Starting Next Page");
            pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
            page1 = doc.startPage(pageInfo);
            canvas = page1.getCanvas();

            rowYAxis = 150;

            drawTable(rowYAxis);
            rowYAxis += 80;

            try {
                canvas.drawText(new SimpleDateFormat("dd-MM-yy").format(ledgerEntry.getV_DATE()), 40, rowYAxis, paint); //Date
            }catch (Exception e){
                canvas.drawText("Date Error", 40, rowYAxis, paint); //Date
            }
            canvas.drawText(ledgerEntry.getV_NO(), 180, rowYAxis, paint); //V No
            canvas.drawText(ledgerEntry.getDESCRIPTION(), 280, rowYAxis, paint); //Description
            canvas.drawText(String.valueOf(ledgerEntry.getVDEBIT()), 620, rowYAxis, paint); //Debit
            canvas.drawText(String.valueOf(ledgerEntry.getV_CREDIT()), 820, rowYAxis, paint); //Credit
            canvas.drawText(String.valueOf(ledgerEntry.getBALANCE()), 1020, rowYAxis, paint); //Balance
        }
        rowYAxis += 80;
    }

    private void printPDF(String fileName) {
        PrintManager printManager = (PrintManager) Objects.requireNonNull(getContext()).getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printDocumentAdapter = new PDFDocumentAdapter(getContext(),
                fileName);
        try {
            if (printManager != null) {
                Log.d(getString(R.string.txtLogTag), "Starting the Printing Process......");
                printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());
            }
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "Error while printing: " + e.getMessage());
        }
    }


}