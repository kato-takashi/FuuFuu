package com.example.katotakashi.webview_jstest;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView oWebView = new WebView(getApplicationContext());
        oWebView.getSettings().setJavaScriptEnabled(true);
        //milkcocoaがlocalstrageを使用しているため←これがないと動かない
        oWebView.getSettings().setDomStorageEnabled(true);


        //新規ウィンドウを開かない
//        oWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return false;
//            }
//        });

        oWebView.loadUrl("http://fuufuu-auth.s3-website-us-east-1.amazonaws.com/auth1/index.html");
        oWebView.setWebChromeClient(new WebChromeClient() {

            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("oWebView", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
        oWebView.setWebChromeClient(new WebChromeClient());
        setContentView(oWebView);

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
