package com.example.katotakashi.capdemo01;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Animation animation;
        animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animation.setDuration(4000);
        File path = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "AppName");
        if (!path.exists()) {
            path.mkdir();
        }
        WebView myWebView1 = (WebView)findViewById(R.id.myWebView);
        myWebView1.setWebViewClient(new WebViewClient());
        myWebView1.loadUrl("https://youtu.be/7V-fIGMDsmE");
        myWebView1.getSettings().setJavaScriptEnabled(true);

        View myView = myWebView1;
        Handler h = new Handler();
        for (int i = 0; i < 5; i++) {
            h.postDelayed(new SavePictureRunner(myView, new File(path, i + ".png")), i * 1000);
        }

        ImageView animate = (ImageView)findViewById(R.id.myIageView);
//        animate.startAnimation(animation);
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
