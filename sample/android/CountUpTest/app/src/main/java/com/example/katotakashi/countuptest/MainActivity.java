package com.example.katotakashi.countuptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import android.os.Handler;

public class MainActivity extends AppCompatActivity {

//    /**
//     * スレッドUI操作用ハンドラ
//     */
//    private Handler timerHandler = new Handler();
//    //    停止用
//    private Handler deleteHandler = new Handler();
//    /**
//     * テキストオブジェクト
//     */
//    private Runnable updateText;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        updateText = new Runnable() {
//            public void run() {
//                TextView text = (TextView) findViewById(R.id.count);
//                Integer count = Integer.valueOf(text.getText().toString());
//                count += 1;
//                text.setText(count.toString());
//                timerHandler.removeCallbacks(updateText);
//                timerHandler.postDelayed(updateText, 1000);
//            }
//        };
//        timerHandler.postDelayed(updateText, 1000);
//    }

    private Handler timerHandler = new Handler();
    private Handler deleteHandler = new Handler();
    private int counter = 0;
    private int stopNum = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        timerHandler.removeCallbacks(CallbackTimer);
        timerHandler.postDelayed(CallbackTimer, 1000);
//        deleteHandler.postDelayed(CallbackDelete, 6000);
    }

    private Runnable CallbackTimer = new Runnable() {
        public void run() {
            TextView text = (TextView) findViewById(R.id.count);
            /* カウンタ値を更新 */
            counter += 1;
            text.setText(String.valueOf(counter));

            Log.d("->", String.valueOf(counter));

            /* 次の通知を設定 */
            timerHandler.postDelayed(this, 1000);


            if(counter == stopNum){
                /* コールバックを削除して周期処理を停止  stopNumの回数を繰り返したら*/
                timerHandler.removeCallbacks(CallbackTimer);
                text.setText("stop");
            }
        }
    };

    private Runnable CallbackDelete = new Runnable() {
        public void run() {
            /* コールバックを削除して周期処理を停止 */
            timerHandler.removeCallbacks(CallbackTimer);
        }
    };

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
