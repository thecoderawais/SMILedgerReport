package com.example.ledgerreport.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.Models.LedgerReportModel;
import com.example.ledgerreport.Models.TbAccountsModel;
import com.example.ledgerreport.PDFDocumentAdapter;
import com.example.ledgerreport.PdfViewActivity;
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
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

public class SingleLedger extends Fragment {

    int selectedIndex, loggedInUser;

    SearchableSpinner spinner;

    String fileName = "";
    List<LedgerReportModel> ledgerReportsList;

    private ArrayList<String> accounts;
    ApiInterface apiInterface;

//    Paint paint = new Paint(), titlePaint = new Paint();
//    int pageWidth = 1200, rowYAxis = 760;
    Date dateObj;

    PdfDocument pdfDoc;
    Document doc;

    // The second argument determines 'large table' functionality is used
    // It defines whether parts of the table will be written before all data is added.
    Table table = new Table(UnitValue.createPercentArray(6), true);

    DatePicker fromDate, toDate;
    Button btnSubmit;
    int dayFrom, monthFrom, yearFrom, dayTo, monthTo, yearTo;
    String selectedAccount = "", from = "", to = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_ledger, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loggedInUser = new MySharedPreference(getContext()).getCompanyCode("companyCode");

        ledgerReportsList = new ArrayList<>();
        accounts = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CONST.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(ApiInterface.class);
        getAllAccounts();

        spinner = Objects.requireNonNull(getActivity())
                .findViewById(R.id.singleLedgerSpinner);
        fromDate = Objects.requireNonNull(getActivity()).findViewById(R.id.singleLedgerFromDate);
        toDate = Objects.requireNonNull(getActivity()).findViewById(R.id.singleLedgerToDate);
        btnSubmit = Objects.requireNonNull(getActivity()).findViewById(R.id.btnSubmit);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedAccount = adapterView.getItemAtPosition(i).toString();
                selectedIndex = i - 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!selectedAccount.equals(getString(R.string.select_any_item))){
                    Snackbar.make(view, "Processing Your Request, Please Wait........" + loggedInUser, Snackbar.LENGTH_LONG).show();
                    Log.d(getString(R.string.txtLogTag), "Starting the process...");
                    try {

                        dayFrom = fromDate.getDayOfMonth();
                        monthFrom = fromDate.getMonth();
                        yearFrom = fromDate.getYear();

                        dayTo = toDate.getDayOfMonth();
                        monthTo = toDate.getMonth();
                        yearTo = toDate.getYear();

                        String[] account = selectedAccount.split("--");

                        from = yearFrom + "-" + monthFrom + "-" + dayFrom;
                        to = yearTo + "-" + monthTo + "-" + dayTo;

                        getLedgerReportData(loggedInUser, account[1], account[0], from, to, view);
                    }catch (Exception e){
                        Log.d(getString(R.string.txtLogTag), "Button Click Exception: " + e.getMessage());
                        Snackbar.make(view, "Nothing Found!", Snackbar.LENGTH_LONG).show();
                    }
                }else{
                    Snackbar.make(view, "Please select the account.", Snackbar.LENGTH_LONG).show();
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
                                Log.d(getString(R.string.txtLogTag), "Response Successful! Entries: " + response.body().size());
                                if (response.body().size() > 0) {
//                                    Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                    for(TbAccountsModel item : response.body()){
                                        accounts.add(item.getAC_CODE() + " -- " + item.getAC_NAME());
                                    }
                                    ArrayAdapter<String> accountsArrayAdapter = new ArrayAdapter<>
                                            (Objects.requireNonNull(getContext()),android.R.layout.simple_list_item_1,accounts);
                                    spinner.setAdapter(accountsArrayAdapter);
                                    Log.d(getString(R.string.txtLogTag), "Set " + response.body().size() + " entries in Spinner");
                                } else {
                                    Toast.makeText(getContext(), "No Accounts Found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Response was not successful!", Toast.LENGTH_SHORT).show();
                                Log.d(getString(R.string.txtLogTag), "Response was not successful!" + response.message() );
                            }
                        } catch (Exception e) {
                            Log.d(getString(R.string.txtLogTag), "Error while getting User's info: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TbAccountsModel>> call, Throwable t) {
                        Log.d(getString(R.string.txtLogTag), "onFailure: " + t.getMessage());

                    }
                });
            } catch (Exception e) {
                Log.d(getString(R.string.txtLogTag), "LedgerReportData Getting Exception: " + e.getMessage());
            }
        } else {
            Log.d(getString(R.string.txtLogTag), "Call was null");
        }
    }

    //Retrofit API Data filling
    public void getLedgerReportData(final int compCode, final String accName, final String accCode,
                                    final String fromDate,  final String toDate, final View view){
        Call<List<LedgerReportModel>> call = apiInterface.getLedgerReportData(compCode, accCode,  fromDate, toDate);

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
                                Snackbar.make(view, "Size " + ledgerReportsList.size(), Snackbar.LENGTH_LONG);
                                Log.d(getString(R.string.txtLogTag), "Size after Snackbar: " + ledgerReportsList.size());

                                Snackbar.make(view, "Size " + ledgerReportsList.size(), Snackbar.LENGTH_LONG);


                                if (ledgerReportsList.size() > 0) {
                                    try {

                                        int balance = ledgerReportsList.get(0).getOPENING_BALANCE();

                                        //                    For updating all balances
                                        for (LedgerReportModel item :
                                                ledgerReportsList) {
                                            balance = balance + item.getVDEBIT() - item.getV_CREDIT();
                                            item.setBALANCE(balance);
                                        }

                                        dateObj = new Date();
                                        fileName = getExternalStorageDirectory() +
                                                "/LedgerReports/LedgerReport:" + ledgerReportsList.get(0).getAC_NAME() + ".pdf";

                                        File file = new File(fileName);
                                        Objects.requireNonNull(file.getParentFile()).mkdirs();

                                        pdfDoc = new PdfDocument(new PdfWriter(fileName));
                                        doc = new Document(pdfDoc);

                                        Log.d(getString(R.string.txtLogTag), "Adding Report Top Items");
                                        addReportTopItems(accCode, accName, from, to, String.valueOf(ledgerReportsList.get(0).getOPENING_BALANCE()));

                                        Log.d(getString(R.string.txtLogTag), "Drawing Table......");
                                        drawTable();

                                        Log.d(getString(R.string.txtLogTag), "Starting to Add Rows");
                                        int i = 0;
                                        for (LedgerReportModel item :
                                                ledgerReportsList) {
                                            i++;
                                            Log.d(getString(R.string.txtLogTag), "Adding Row:" + i + " To Table");
                                            if (i % 6 == 0){
                                                Log.d(getString(R.string.txtLogTag), "Flushing Table");
                                                table.flush();
                                            }
                                            addRow(item);
                                        }

                                        Log.d(getString(R.string.txtLogTag), "Completing table and closing Doc");

                                        table.complete();
                                        doc.close();

                                        try {

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
                                }else {
                                    Log.d(getString(R.string.txtLogTag), "ledgerReportList is zero.");
                                    Toast.makeText(getContext(), "No Data Found for the Entered Parameters!", Toast.LENGTH_LONG).show();
                                }
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

    //Report top Items
    @SuppressLint("SimpleDateFormat")
    public void addReportTopItems(String acCode, String acName, String fromDate, String toDate, String balance)
    {
        try {
            Text title1 = new Text(new MySharedPreference(getContext()).getCompanyName("companyName")).setFontSize(22);

            Text title2 = new Text(acCode).setFontSize(18);
            Text title3 = new Text(acName).setFontSize(18);
            Text title4 = new Text(fromDate).setFontSize(15);
            Text title5 = new Text(toDate).setFontSize(15);
            Text title6 = new Text(balance).setFontSize(16);

            Paragraph p = new Paragraph().add(title1);
            doc.add(p);

            p = new Paragraph().add(title2).add(":").add(title3);
            doc.add(p);

            p = new Paragraph().add("From ").add(title4).add(" To ").add(title5);
            doc.add(p);

            p = new Paragraph().add("B/F: ").add(title6);
            doc.add(p);

        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "addReportTopItems Exception: " + e.getMessage());
        }

    }

    //Drawing the table
    public void drawTable()
    {
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