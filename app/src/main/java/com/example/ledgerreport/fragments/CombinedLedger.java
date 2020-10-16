package com.example.ledgerreport.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.example.ledgerreport.Utils.MySharedPreference;
import com.example.ledgerreport.adapters.CombinedLedgerAccountAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

    Table table = new Table(UnitValue.createPercentArray(6), true);


    PdfDocument pdfDoc;
    Document doc;

    int loggedInUser, currentAccountIndex;

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

    Date dateObj;
//    DateFormat format;


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
        ArrayAdapter<String> accountsArrayAdapter = new ArrayAdapter<>
                (Objects.requireNonNull(getContext()),android.R.layout.simple_list_item_1,spinnerAccounts);

        adapter = new CombinedLedgerAccountAdapter(getContext(), accountsArrayAdapter, accounts);
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
                adapter.notifyItemInserted(count);
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

                Log.d(getString(R.string.txtLogTag), "Total " + count + " Items in RecyclerView");
                Log.d(getString(R.string.txtLogTag), "Got " + selectedAccounts.size() + " Selected Accounts back...");
                Log.d(getString(R.string.txtLogTag), "Showing Selected Accounts...");

                if(count == selectedAccounts.size()){
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
                }else{
                    Snackbar.make(view, "Please Select all Accounts. Added: " + count +
                            ". Found: " + selectedAccounts.size(), Snackbar.LENGTH_LONG).show();
                }
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

                                            fileName = getExternalStorageDirectory() +
                                                    "/LedgerReports/LedgerReport:" + ledgerReportsList.get(0).getAC_NAME() + ".pdf";

                                            File file = new File(fileName);
                                            Objects.requireNonNull(file.getParentFile()).mkdirs();

                                            pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(new PdfWriter(fileName));
                                            doc = new Document(pdfDoc);

                                            Log.d(getString(R.string.txtLogTag), "Adding Report Top Items");
                                            addReportTopItems(from, to);

                                            Log.d(getString(R.string.txtLogTag), "Drawing Table......");
                                            drawTable();

                                            currentAccountIndex = 0;
                                            currentAccount = selectedAccountNames.get(currentAccountIndex);
                                            addAccountHeading(currentAccount, "15000");

                                            // attach a file to FileWriter
                                            FileWriter fw=new FileWriter( getExternalStorageDirectory() +
                                                    "/Outputs/output.txt");

                                            Log.d(getString(R.string.txtLogTag), "Starting to add Rows...");
                                            int i = 0;
                                            for (LedgerReportModel item :
                                                    ledgerReportsList) {
                                                i++;
                                                Log.d(getString(R.string.txtLogTag), "Adding Row:" + i + " To Table");
                                                addRow(item);

                                                fw.write("Acc: " + item.getAC_NAME() + item.getV_DATE() + " BAL: " + item.getBALANCE() + " \n");
                                            }

                                            fw.close();

                                            try {
                                                Log.d(getString(R.string.txtLogTag), "Saving File to : " + fileName);

                                                table.complete();
                                                doc.close();

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
//        try {
//            //Add Title
//            titlePaint.setTextAlign(Paint.Align.CENTER);
//            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            titlePaint.setTextSize(70);
//            canvas.drawText("Combined Ledger Report", pageWidth/2, 50, titlePaint);
//
//
//            paint.setColor(Color.BLACK);
//            paint.setTextSize(35f);
//            paint.setTextAlign(Paint.Align.LEFT);
//
//            canvas.drawText(" From:   " + fromDate, 20, 150, paint);
//            canvas.drawText(" To:        " + toDate, 20, 225, paint);
//            canvas.drawText(" As On:  " + new SimpleDateFormat("E, dd-MMM-yyyy").format(dateObj), 730, 150, paint);
//            canvas.drawText("                          " + new SimpleDateFormat("hh:mm a").format(dateObj), 730, 225, paint);
//        }catch (Exception e){
//            Log.d(getString(R.string.txtLogTag), "addReportTopItems Exception: " + e.getMessage());
//        }
        try {
            Text title1 = new Text(new MySharedPreference(getContext()).
                    getCompanyName("companyName")).setFontSize(24).setBold().setUnderline()
                    .setTextAlignment(TextAlignment.CENTER);

            Text title4 = new Text(fromDate).setFontSize(15);
            Text title5 = new Text(toDate).setFontSize(15);

            Paragraph p = new Paragraph().add(title1);
            doc.add(p);

            p = new Paragraph().add("Combined Ledger").setFontSize(20).setTextAlignment(TextAlignment.CENTER);
            doc.add(p);

            p = new Paragraph().add("From ").add(title4).add(" To ").add(title5);
            doc.add(p);

        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "addReportTopItems Exception: " + e.getMessage());
        }
    }

    public void addAccountHeading(String accName, String balance)
    {
//        try {
//            Log.d(getString(R.string.txtLogTag), "Adding new Account Heading: " + accName);
//            //Add Title
//            titlePaint.setTextAlign(Paint.Align.LEFT);
//            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            titlePaint.setTextSize(45);
//            canvas.drawText("Acc: " + accName, 20, yAxis, titlePaint);
//
//
//            paint.setColor(Color.BLACK);
//            paint.setTextSize(35f);
//            paint.setTextAlign(Paint.Align.LEFT);
//
//            canvas.drawText(" Balance: " + balance, 730, yAxis, paint);
//        }catch (Exception e){
//            Log.d(getString(R.string.txtLogTag), "addAccountHeadingError Exception: " + e.getMessage());
//        }
        try {

            Paragraph p = new Paragraph().add("Acc: " + accName).setFontSize(15).setTextAlignment(TextAlignment.LEFT)
                    .add("B/F: " + balance).setFontSize(15).setTextAlignment(TextAlignment.RIGHT);
            doc.add(p);

        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "addAccountHeading Exception: " + e.getMessage());
        }
    }

    //Drawing the table
    public void drawTable()
    {
//        try {
//            paint.setTextAlign(Paint.Align.LEFT);
//            paint.setStyle(Paint.Style.FILL);
//            paint.setTextSize(40f);
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//            canvas.drawText("Date", 40, yAxis, paint);
//            canvas.drawText("V. #", 180, yAxis, paint);
//            canvas.drawText("Description", 280, yAxis, paint);
//            canvas.drawText("Debit", 620, yAxis, paint);
//            canvas.drawText("Credit", 820, yAxis, paint);
//            canvas.drawText("Balance", 1020, yAxis, paint);
//        }catch (Exception e){
//            Log.d(getString(R.string.txtLogTag), "drawTable Exception: " + e.getMessage());
//        }
        try {
            table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Date")));
            table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("V. #")));
            table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Description")));
            table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Debit")));
            table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Credit")));
            table.addHeaderCell(new Cell().setKeepTogether(true).add(new Paragraph("Balance")));

            // For the "large tables" they shall be added to the document before its child elements are populated
            doc.add(table);
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "drawTable Exception: " + e.getMessage());
        }
    }

    //Called each time for adding the row.
    @SuppressLint("SimpleDateFormat")
    public void addRow(LedgerReportModel item)
    {
        if (!item.getAC_NAME().equals(currentAccount)) {
            doc.add(table);
            doc.add(new AreaBreak());
            currentAccountIndex++;
            currentAccount = selectedAccountNames.get(currentAccountIndex);
            addAccountHeading(currentAccount, "15000");
            table = new Table(UnitValue.createPercentArray(6), true);
            drawTable();
        }

        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(new SimpleDateFormat("dd-MM-yy").format(item.getV_DATE()))
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getV_NO())
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(item.getDESCRIPTION())
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(String.valueOf(item.getVDEBIT()))
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(String.valueOf(item.getV_CREDIT()))
                .setMargins(0, 0, 0, 0)));
        table.addCell(new Cell().setKeepTogether(true).add(new Paragraph(String.valueOf(item.getBALANCE()))
                .setMargins(0, 0, 0, 0)));
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