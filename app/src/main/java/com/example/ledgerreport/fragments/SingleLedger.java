package com.example.ledgerreport.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

public class SingleLedger extends Fragment {

    int selectedIndex, loggedInUser;

    SharedPreferences sharedPreference;

    MaterialSpinner spinner;

    String fileName = "";
    List<LedgerReportModel> ledgerReportsList;

    private ArrayList<String> accounts;
    ApiInterface apiInterface;

    Paint paint = new Paint(), titlePaint = new Paint();
    Bitmap bmp, scaledBmp;
    int pageWidth = 1200, rowYAxis = 760;
    Date dateObj;

    PdfDocument doc = new PdfDocument();
    PdfDocument.PageInfo pageInfo;
    PdfDocument.Page page1;

    Canvas canvas;
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

        sharedPreference = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        loggedInUser = sharedPreference.getInt(getString(R.string.prefKey), 0);

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
        if (accounts.size() == 0)
            spinner.setItems("Loading the Data...");

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                selectedAccount = item;
                selectedIndex = position - 1;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                    spinner.setItems(accounts);
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

                                        ledgerReportsList.get(0).setOPENING_BALANCE(1503350);
                                        int balance = ledgerReportsList.get(0).getOPENING_BALANCE();

                                        //                    For updating all balances
                                        for (LedgerReportModel item :
                                                ledgerReportsList) {
                                            balance = balance + item.getVDEBIT() - item.getV_CREDIT();
                                            item.setBALANCE(balance);
                                        }

                                        //
                                        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                                        scaledBmp = Bitmap.createScaledBitmap(bmp, 800, 350, false);
                                        dateObj = new Date();

                                        //Setting Pages
                                        pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
                                        page1 = doc.startPage(pageInfo);
                                        canvas = page1.getCanvas();

                                        Log.d(getString(R.string.txtLogTag), "Adding Report Top Items");
                                        addReportTopItems(accCode, accName, from, to, String.valueOf(ledgerReportsList.get(0).getOPENING_BALANCE()));

                                        Log.d(getString(R.string.txtLogTag), "Drawing Table......");
                                        drawTable();

                                        Log.d(getString(R.string.txtLogTag), "Adding Report Top Items");
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
            canvas.drawBitmap(scaledBmp, 0,0,paint);
            //Add Title
            titlePaint.setTextAlign(Paint.Align.CENTER);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            titlePaint.setTextSize(70);
            canvas.drawText("Ledger Report", pageWidth/2, 350, titlePaint);


            //Left Aligned Items
            paint.setColor(Color.BLACK);
            paint.setTextSize(35f);
            paint.setTextAlign(Paint.Align.LEFT);

            canvas.drawText(" Account Code: " + acCode, 20, 450, paint);
            canvas.drawText(" Account Title: " + acName, 20, 525, paint);
            canvas.drawText(" B/F: " + balance, 20, 590, paint);
            canvas.drawText("From:   " + fromDate, 730, 450, paint);
            canvas.drawText("To:        " + toDate, 730, 490, paint);
            canvas.drawText("As On:  " + new SimpleDateFormat("E, dd-MMM-yyyy").format(dateObj), 730, 540, paint);
            canvas.drawText("                         " + new SimpleDateFormat("hh:mm a").format(dateObj), 730, 590, paint);
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "addReportTopItems Exception: " + e.getMessage());
        }

    }

    //Drawing the table
    public void drawTable()
    {
        try {
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(40f);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Date", 40, 680, paint);
            canvas.drawText("V. #", 180, 680, paint);
            canvas.drawText("Description", 280, 680, paint);
            canvas.drawText("Debit", 620, 680, paint);
            canvas.drawText("Credit", 820, 680, paint);
            canvas.drawText("Balance", 1020, 680, paint);
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

            }catch (Exception e){
                Log.d(getString(R.string.txtLogTag), "Exception while adding Row: " + e.getMessage());
            }
        }else{ //If the page's height has been filled!
            Log.d(getString(R.string.txtLogTag), "Finishing Prev. Page ");
            doc.finishPage(page1);
            Log.d(getString(R.string.txtLogTag), "Starting Next Page");
            pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
            page1 = doc.startPage(pageInfo);
            canvas = page1.getCanvas();

            rowYAxis = 150;

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