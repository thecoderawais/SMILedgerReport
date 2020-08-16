package com.example.ledgerreport;

import android.content.Context;
import android.icu.util.Output;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
class PDFDocumentAdapter extends PrintDocumentAdapter {
    Context context;
    String path;

    public PDFDocumentAdapter(Context c, String s) {
        this.context = c;
        this.path = s;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        if (cancellationSignal.isCanceled()){
            callback.onLayoutCancelled();
        }else{
            PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder("file name");
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build();
            callback.onLayoutFinished(builder.build(), !newAttributes.equals(oldAttributes));
        }
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(path);
            in = new FileInputStream(file);
            out = new FileOutputStream(destination.getFileDescriptor());

            byte[] buff = new byte[16384];
            int size;
            while ((size=in.read(buff)) >= 0 && !cancellationSignal.isCanceled()){
                out.write(buff, 0, size);
            }
            if (cancellationSignal.isCanceled()){
                callback.onWriteCancelled();
            }else{
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        }catch(Exception e){
            callback.onWriteFailed(e.getMessage());
            Log.e("PrintExc", "onWrite: " + e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                Log.e("PrintExc", "onClose Files in and out: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}