package com.example.ledgerreport;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.Models.LedgerReportModel;
import com.example.ledgerreport.Utils.CONST;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {
    String fileName = "";
    List<LedgerReportModel> ledgerReportsList;

    EditText etAccCode, etFromDate, etToDate;
    String accCode;
    Date fromDate;
    Date toDate;
    RelativeLayout relativeLayout;

    ApiInterface apiInterface;


    PdfDocument doc = new PdfDocument();
    Paint paint = new Paint(), titlePaint = new Paint();
    Bitmap bmp, scaledBmp;
    int pageWidth = 1200, rowYAxis = 760;
    Date dateObj;
    DateFormat format;

    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
    PdfDocument.Page page1 = doc.startPage(pageInfo);

    Canvas canvas = page1.getCanvas();

//    @SuppressLint("SimpleDateFormat")
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ledgerReportsList = new ArrayList<>();

        etAccCode = findViewById(R.id.etAccCode);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);

        accCode = "";
        fromDate = toDate = new Date();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CONST.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        scaledBmp = Bitmap.createScaledBitmap(bmp, 800, 350, false);
        dateObj = new Date();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


        //View Animations (Splash Screen etc)
        relativeLayout = findViewById(R.id.rellay1);
        handler.postDelayed(runnable, 2000); //2000 is the timeout for the splash
    }

    //Button Click Event for all Functionality
    public void fetchLedgerData(View view) {
        if (etAccCode.getText().toString().equals("")
            || etFromDate.getText().toString().equals("")
            || etToDate.getText().toString().equals("")){
            Toast.makeText(this, "Please Fill all Fields", Toast.LENGTH_SHORT).show();
        }else{
            try {
                accCode = etAccCode.getText().toString();

                String fd = etFromDate.getText().toString(), td = etToDate.getText().toString();

                Log.d(getString(R.string.txtLogTag), "Requesting API Data...");
                getLedgerReportData(accCode, fd, td);

//                while (ledgerReportsList.size() == 0){
//                    Log.d(getString(R.string.txtLogTag), "Last Call Returned Zero Rows, Requesting Again.....");
//                    getLedgerReportData(accCode, fd, td);
//                }

                ledgerReportsList.get(0).setOPENING_BALANCE(1503350);
                int balance = ledgerReportsList.get(0).getOPENING_BALANCE();

    //                    For updating all balances
                    for (LedgerReportModel item :
                            ledgerReportsList) {
                        balance = balance + item.getVDEBIT() - item.getV_CREDIT();
                        item.setBALANCE(balance);
                    }
                Log.d(getString(R.string.txtLogTag), "Adding Report Top Items");
                addReportTopItems(accCode, ledgerReportsList.get(0).getAC_NAME(), fd, td,String.valueOf(ledgerReportsList.get(0).getOPENING_BALANCE()));

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
                    fileName = getExternalStorageDirectory() +
                            "/LedgerReports/LedgerReport:" + ledgerReportsList.get(0).getAC_NAME() + ".pdf";
                    doc.finishPage(page1);
                    File file = new File(fileName);

                    try {
                        Log.d(getString(R.string.txtLogTag), "Saving File to : " + fileName);
                        doc.writeTo(new FileOutputStream(file));
                        Log.d(getString(R.string.txtLogTag), "File Saved to: " + fileName + "Successfully!");
                        Toast.makeText(MainActivity.this,
                                "Saved! at " + fileName, Toast.LENGTH_SHORT).show();

                        Log.d(getString(R.string.txtLogTag), "About to Start Dialog.Show()......");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

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
                                    Intent intent = new Intent(MainActivity.this, PdfViewActivity.class);
                                    intent.putExtra("fileName", fileName);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "Couldn't Open the file Specified!", Toast.LENGTH_LONG).show();
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
                    }catch (Exception e){
                        Log.d(getString(R.string.txtLogTag), "Save File Exc: " + e.getMessage());
                    }
                    doc.close();
                } catch (Exception e) {
//                    Toast.makeText(this, "Error while Fetching Ledger Data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(getString(R.string.txtLogTag), "Error while Fetching Ledger Data: " + e.getMessage());
                }
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
                        Toast.makeText(MainActivity.this, "Retrofit onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
    public void addRow(LedgerReportModel ledgerEntry)
    {
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

            rowYAxis += 80;
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "Exception while adding Row: " + e.getMessage());
        }
    }

    private void printPDF(String fileName) {
        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printDocumentAdapter = new PDFDocumentAdapter(MainActivity.this,
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
