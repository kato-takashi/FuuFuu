package com.example.katotakashi.webview_jstest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
<<<<<<< HEAD
import android.widget.Toast;
=======
>>>>>>> ac4e91d0d1a437abdb731dc9f9693c29309bcb3a


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

<<<<<<< HEAD

=======
>>>>>>> ac4e91d0d1a437abdb731dc9f9693c29309bcb3a
        WebView oWebView = new WebView(getApplicationContext());
        oWebView.getSettings().setJavaScriptEnabled(true);
        //milkcocoaがlocalstrageを使用しているため←これがないと動かない
        oWebView.getSettings().setDomStorageEnabled(true);
<<<<<<< HEAD
//        oWebView.getSettings().setSupportMultipleWindows(true);
        /////////
        oWebView.setWebViewClient(new WebViewClient());
//        oWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        oWebView.getSettings().setSupportMultipleWindows(true);
        /////////
//            oWebView.loadUrl("file:///android_asset/test.html");
        oWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return false;
            }

        });
=======


        //新規ウィンドウを開かない
//        oWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return false;
//            }
//        });

        oWebView.loadUrl("http://fuufuu-auth.s3-website-us-east-1.amazonaws.com/auth1/index.html");
>>>>>>> ac4e91d0d1a437abdb731dc9f9693c29309bcb3a
        oWebView.setWebChromeClient(new WebChromeClient() {

            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("oWebView", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }

            /////////////
//            @Override
//            public boolean onCreateWindow(WebView view, boolean dialog,
//                                          boolean userGesture, Message resultMsg) {
//                //この中で画面を作成する。
//
//                //window.openだとdialogがtrueになるかと思ったけど、そうじゃないみたい。
//                StringBuilder sb = new StringBuilder();
//                sb.append("dialog="+dialog);
//                sb.append("tuserGesture="+userGesture);
//                Log.i("window test: ", sb.toString());
//                return false;
//            }
            /////////////
        });
        setContentView(oWebView);
        oWebView.loadUrl("http://fuufuu-auth.s3-website-us-east-1.amazonaws.com/auth1/index.html");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
