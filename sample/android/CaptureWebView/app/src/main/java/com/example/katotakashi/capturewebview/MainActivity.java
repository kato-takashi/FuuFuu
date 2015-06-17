package com.example.katotakashi.capturewebview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {

    private WebView myWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView = new WebView(this);
        // myWebViewのインスタンスを取得
        myWebView = (WebView)findViewById(R.id.myWebVew);
        myWebView.setWebViewClient(new WebViewClient() {});

        // PictureListener の設定
        myWebView.setPictureListener(new MyPictureListener());
        // htmlファイルを読み込んで表示する
        myWebView.loadUrl("http://www.google.co.jp/");
    }

    // PictureListenerの実装クラス
    private class MyPictureListener implements WebView.PictureListener {

        @Override
        public void onNewPicture(WebView view, Picture picture) {

            // WebViewのキャプチャを取る
            Picture pictureObj = view.capturePicture();

            // 取ったキャプチャの幅と高さを元に
            // 新しいBitmapを生成する。
            Bitmap bitmap = Bitmap.createBitmap(
                    pictureObj.getWidth(),
                    pictureObj.getHeight(),
                    Bitmap.Config.ARGB_8888);

            // canvasを通してBitmapに書き込む
            Canvas canvas = new Canvas(bitmap);
            pictureObj.draw(canvas);

            // Bitmapをファイルシステムに書き出す。
            FileOutputStream fos = null;
            try {
                String path =
                        Environment.getExternalStorageDirectory().toString() +
                                "/testCpa.jpg";
                Toast.makeText(getBaseContext(), path, Toast.LENGTH_LONG).show();

                fos = new FileOutputStream(path);
                if ( fos != null )
                {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                }
            } catch( Exception e ){
                e.printStackTrace();

            } finally {
                try {
                    if(fos != null) fos.close();
                } catch (IOException e) {
                }
            }

        }
    }
}
