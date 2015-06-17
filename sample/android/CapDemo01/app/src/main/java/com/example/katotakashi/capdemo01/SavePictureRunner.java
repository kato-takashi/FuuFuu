package com.example.katotakashi.capdemo01;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SavePictureRunner implements Runnable {
    private File mFileName;
    private View mView;

    public SavePictureRunner(View view, File filename) {
        this.mView = view;
        this.mFileName = filename;
    }

    @Override
    public void run() {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mFileName);

            Bitmap saveBitmap = null;
            mView.setDrawingCacheEnabled(true);
            mView.setDrawingCacheBackgroundColor(Color.WHITE);
            saveBitmap = Bitmap.createBitmap(mView.getDrawingCache());
            saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
        } catch (IOException e) {
            Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            mView.setDrawingCacheEnabled(false);
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
