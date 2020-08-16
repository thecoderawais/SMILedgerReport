package com.example.ledgerreport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;

public class PdfViewActivity extends AppCompatActivity {

    PDFView pdfView;
    String fileName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        pdfView = findViewById(R.id.pdfView);
        fileName = getIntent().getStringExtra("fileName");

        try {
            Log.d(getString(R.string.txtLogTag), "Opening file in PDF Viewer.");
            pdfView.fromFile(new File(fileName != null ? fileName : ""))
                    .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    // spacing between pages in dp. To define spacing color, set view background
                    .spacing(0)
                    .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
                    .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
                    .fitEachPage(false) // fit each page to the view, else smaller pages are scaled relative to largest page.
                    .pageSnap(false) // snap pages to screen boundaries
                    .pageFling(false) // make a fling change only a single page like ViewPager
                    .nightMode(false) // toggle night mode
                    .load();
        }catch (Exception e){
            Log.d(getString(R.string.txtLogTag), "Exception in PDFView Activity: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.share:
            Toast.makeText(this, "User Selected Share!", Toast.LENGTH_SHORT).show();
            return(true);
        case R.id.print:
            printPDF(fileName);
            return(true);

    }
        return(super.onOptionsItemSelected(item));
    }


    private void printPDF(String fileName) {
        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printDocumentAdapter = new PDFDocumentAdapter(PdfViewActivity.this,
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

