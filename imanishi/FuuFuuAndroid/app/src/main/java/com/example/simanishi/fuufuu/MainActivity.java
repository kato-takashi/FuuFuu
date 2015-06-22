package com.example.simanishi.fuufuu;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//milkcocoa sdk
import com.mlkcca.client.DataElement;
import com.mlkcca.client.DataElementValue;
import com.mlkcca.client.DataStore;
import com.mlkcca.client.Streaming;
import com.mlkcca.client.StreamingListener;
import com.mlkcca.client.MilkCocoa;
import com.mlkcca.client.DataStoreEventListener;

//wind sensor
import com.weatherflow.windmeter.sensor_sdk.entities.HeadsetState;
import com.weatherflow.windmeter.sensor_sdk.sdk.AnemometerObservation;
import com.weatherflow.windmeter.sensor_sdk.sdk.HeadphonesReceiver;
import com.weatherflow.windmeter.sensor_sdk.sdk.IHeadphonesStateListener;
import com.weatherflow.windmeter.sensor_sdk.sdk.WFConfig;
import com.weatherflow.windmeter.sensor_sdk.sdk.WFSensor;

public class MainActivity extends AppCompatActivity implements IHeadphonesStateListener, WFSensor.OnValueChangedListener, DataStoreEventListener {

    //physicaroid
    private Physicaloid mPhysicaloid;
    private Handler pslHandler = new Handler();

    //milkcocoa
    private MilkCocoa milkCocoa;
    private DataStore messageDataStore;
    private Handler mcHandler = new Handler();

    //wind sensor
    private final static String HEADSET_ACTION = "android.intent.action.HEADSET_PLUG";
    private BroadcastReceiver mHeadphonesReceiver;
    private TextView mSpeed;
    private Button mStart, mStop;

    //camera
    private Camera mCam = null;
    private CameraPreview mCamPreview = null; //カメラプレビュークラス
    private boolean mIsTake = false; //画面タッチの2度押し禁止用フラグ
    private int frontCameraId = 1; //フロントカメラのID
    String encodedBase64; //base64の格納変数

    //components
    WebView webView;
    String mCensorVal;
    final String URL = "http://simanishi.angry.jp/chat-master/";
    ArrayList windArray = new ArrayList();
    int count = 0;
    private List<String> breathList;
    List<Byte> breathByteList;

    //timer
    private Handler timerHandler = new Handler();
    private int timerCounter = 0; //timerの回数カウント
    private int takePctNum = 7; // 撮影枚数
    private int takepctSec = 500; //撮影秒数


    /**
     * Life Cycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //wind sensor
        mSpeed = (TextView) findViewById(R.id.speed);
        mHeadphonesReceiver = new HeadphonesReceiver(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //wind sensor start
        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WFSensor.getInstance(MainActivity.this).setOnValueChangedListener(MainActivity.this);
            }
        });

        //wind sensor stop
        mStop = (Button) findViewById(R.id.stop);
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windToArrayStop();
                windSensorStop();
            }
        });

        //physicaroid start
        final Button pStartBtn = (Button) findViewById(R.id.physicaloidStart);
        mPhysicaloid = new Physicaloid(getApplicationContext());

        //Physicaloid OPEN
        pStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPhysicaloid.isOpened()) {
                    if (mPhysicaloid.open()) {
                        pStartBtn.setText("CLOSE");
                        // センサーのリスナー登録
                        mPhysicaloid.addReadListener(new ReadLisener() {
                            @Override
                            // Androidでシリアル文字を受信したらコールバックが発生
                            public void onRead(int size) {
                                readSensorToNative(size);
                            }
                        });
                    } else {
                        pStartBtn.setText("CAN NOT OPEN");

                    }
                } else {
                    mPhysicaloid.close();
                    pStartBtn.setText("OPEN");
                    evaluateJs(webView, "addTextNode('CLOSE')");
                }
            }
        });

        //カメラインスタンスの取得
        try {
            mCam = Camera.open(frontCameraId);
        } catch (Exception e) {
            this.finish();
        }

        //FrameLayout
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        mCamPreview = new CameraPreview(this, mCam);
        preview.addView(mCamPreview);

        //mCamPreviewにタッチイベントを設定
        mCamPreview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mIsTake) {
                        mIsTake = true;
//                        撮影メソッド
//                        mCam.takePicture(null, null, mPicJpgListener);
                        //撮影の繰り返し
                        timerHandler.removeCallbacks(CallbackTimer);
                        timerHandler.postDelayed(CallbackTimer, takepctSec);
                    }
                }
                return true;
            }
        });

        // WebView
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webView.addJavascriptInterface(new MyJavaScriptInterface(this), "Native");
        webView.loadUrl(URL);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!Uri.parse(url).getScheme().equals("native")) {
                    return false;
                }
                Toast.makeText(MainActivity.this, "Url requested: " + url, Toast.LENGTH_SHORT).show();
                return true;
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        //milkcococa connect
        _connect();

    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mHeadphonesReceiver, new IntentFilter(HEADSET_ACTION));
        WFConfig.getAnoConfig(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WFSensor.getInstance(this).onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        WFSensor.getInstance(this).onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        HeadsetState state = new HeadsetState();
        state.setPluggedIn(false);
        onHeadphonesStateChanged(state);
        unregisterReceiver(mHeadphonesReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhysicaloid.close();
    }

    /**
     * Camera
     */

    //撮影の繰り返し
    private Runnable CallbackTimer = new Runnable() {
        public void run() {
            //撮影メソッド
            mCam.takePicture(null, null, mPicJpgListener);
            /* カウンタ値を更新 */
            timerCounter += 1;
            Log.d("->", String.valueOf(timerCounter));

            /* 次の通知を設定 */
            timerHandler.postDelayed(this, 1000);
            String showStr = "撮影回数" + String.valueOf(timerCounter);
            Toast.makeText(getApplicationContext(), showStr, Toast.LENGTH_SHORT).show();

            if (timerCounter == takePctNum) {
                /* コールバックを削除して周期処理を停止  stopNumの回数を繰り返したら*/
                timerHandler.removeCallbacks(CallbackTimer);
                Toast.makeText(getApplicationContext(), "撮影のストップ", Toast.LENGTH_SHORT).show();
                timerCounter = 0;
            }
        }
    };

    //JPEGデータ生成完了時のコールバック
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/test";

            //SDカードフオルダを取得
            File file = new File(saveDir);
            //フォルダ作成
            if (!file.exists() && !file.mkdir()) {
                Log.v("Debug", "Make Dir Error");
            }

            //画像保存パス
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd HHmmss");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

            //Bitmap生成
            Bitmap smallBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            float scaleNum = (float) 0.1;
            Bitmap rszBitmap = _reSizeBitmap(smallBitmap, scaleNum, scaleNum);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            rszBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] reSizeBytes = baos.toByteArray();

            //Base64に変換
            encodedBase64 = "data:image/jpg;base64," + Base64.encodeToString(reSizeBytes, Base64.NO_WRAP);

            //ファイル保存
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(imgPath, true);
                fos.write(reSizeBytes);
                fos.close();
                _registAndroidDB(imgPath);
                Toast.makeText(getApplicationContext(), "写真が保存されました", Toast.LENGTH_SHORT).show();
                //milkcocoaへ送信
                _sendEvent(this);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "保存時にエラーが発生しました", Toast.LENGTH_SHORT).show();
            }

            fos = null;

            mCam.startPreview();
            mIsTake = false;

        }
    };

    /**
     * Androidのデータベースへ画像パスを登録
     *
     * @param path 登録パス
     */
    private void _registAndroidDB(String path) {
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * Bitmap画像をリサイズ
     *
     * @param bmp  Bitmap data
     * @param rszW 拡大比率 w
     * @param rszH 拡大比率 h
     */
    private static Bitmap _reSizeBitmap(Bitmap bmp, double rszW, double rszH) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        Bitmap bmpRsz;
        // 拡大比率
        float rsz_ratio_w = (float) rszW;
        float rsz_ratio_h = (float) rszH;
        // 比率をMatrixに設定
        matrix.postScale(rsz_ratio_w, rsz_ratio_h);
        // リサイズ画像
        bmpRsz = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        return bmpRsz;
    }

    /**
     * Milkcocoa
     * <p/>
     * send Data -------->
     */
    private void _connect() {
        milkCocoa = new MilkCocoa("leadib4y5o07.mlkcca.com");
        messageDataStore = milkCocoa.dataStore("androidCap");
        Streaming stream = messageDataStore.streaming();
        stream.size(25);
        stream.sort("desc");
        stream.addStreamingListener(new StreamingListener() {
            @Override
            public void onData(ArrayList<DataElement> arg0) {
                final ArrayList<DataElement> messages = arg0;
                new Thread(new Runnable() {
                    public void run() {
                        mcHandler.post(new Runnable() {
                            public void run() {
                                for (int i = 0; i < messages.size(); i++) {
                                    //Log.i("milkcocoa OK", messages.get(i).getValue("content"));
                                }
                            }
                        });
                    }
                }).start();

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        stream.next();

        messageDataStore.addDataStoreEventListener((DataStoreEventListener) this);
        messageDataStore.on("push");
    }

    public void _sendEvent(Camera.PictureCallback view) {
        DataElementValue params = new DataElementValue();
        //吐息データ
        String breathArray = "99,99,99,0,0,0,0,0,0,10";
        params.put("breath", breathArray);
        //動画データ
        String pushStr = encodedBase64;
        params.put("movie", pushStr);
        Date date = new Date();
        params.put("date", date.getTime());
        messageDataStore.push(params);
        Toast.makeText(getApplicationContext(), "milkcocoaへ送信。", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPushed(final DataElement dataElement) {
        final DataElement pushed = dataElement;
        new Thread(new Runnable() {
            public void run() {
                mcHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "送信完了。", Toast.LENGTH_SHORT).show();
                        Log.v("test", String.valueOf(pushed.getValue("breath")));
                        breathList = new LinkedList(Arrays.asList(pushed.getValue("breath").split(",", 0)));

                        byte[] breathByteArray = new byte[breathList.size()];
                        for (int i = 0; i < breathByteArray.length; i++) {
                            breathByteArray[i] = Byte.valueOf(breathList.get(i));
                        }

                        mPhysicaloid.write(breathByteArray, breathByteArray.length);
                        breathList.clear();

                        Log.v("BREATH_ARRAY", String.valueOf(breathByteArray.length));
                        Log.v("BREATH_LIST", String.valueOf(breathList.size()));
                    }
                });
            }
        }).start();
    }


    @Override
    public void onSetted(DataElement dataElement) {

    }

    @Override
    public void onSended(DataElement dataElement) {

    }

    @Override
    public void onRemoved(DataElement dataElement) {

    }


    /**
     * Pysicaroid read sensor
     */
    private void readSensorToNative(int size) {
        byte[] buf = new byte[size];
        mPhysicaloid.read(buf, size);
        try {
            mCensorVal = new String(buf, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }
        Integer num = decodePacket(buf);
        mCensorVal = String.valueOf(num);
        //WebViewに値を渡す
        pslHandler.post(new Runnable() {
            @Override
            public void run() {
                evaluateJs(webView, "addTextNode('" + mCensorVal.toString() + "')");
            }
        });
    }

    // s と \r の間の数値を抜き出す
    private int decodePacket(byte[] buf) {
        boolean existStx = false;
        int result = 0;

        for (int i = 0; i < buf.length; i++) {
            if (!existStx) {
                if (buf[i] == 's') { // 最初のsを検索
                    existStx = true;
                }
            } else {
                if (buf[i] == '\r') { // 最後の ¥r までresultに取り込む
                    return result;
                } else {
                    if ('0' <= buf[i] && buf[i] <= '9') { // 数値情報をシフトさせながらresultに保存する
                        result = result * 10 + (buf[i] - '0'); // 文字 '0' 分を引くことでASCIIコードから数値に変換
                    } else {
                        return -1;
                    }
                }
            }
        }

        return -1;
    }

    private void evaluateJs(WebView webView, String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            webView.evaluateJavascript(script, null);
        else
            webView.loadUrl("javascript:" + script);
    }

    private class MyJavaScriptInterface {
        private Context context;

        public MyJavaScriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void sendToNative(String value) {

            byte[] bb = value.getBytes();
            mPhysicaloid.write(bb, bb.length);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onHeadphonesStateChanged(HeadsetState headsetState) {
        WFSensor.getInstance(this).onHeadphonesStateChanged(headsetState);
    }

    private void windToArrayStop() {
        if (windArray.size() > 0) {
            windArray.clear();
            Log.v("ARRAY_CHECK", String.valueOf(windArray.size()));
            windSensorStop();
        }
        Log.v("ARRAY_CHECK", "止めて配列初期化したよ");
    }

    private void windSensorStop() {
        WFSensor.getInstance(MainActivity.this).setOnValueChangedListener(null);
        mSpeed.setText("Stop now");
    }

    @Override
    public void onValueChanged(final AnemometerObservation anemometerObservation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int wind = 0;

                final int MAX_WINDARRAY = 10;

                if (anemometerObservation.getWindSpeed() > 0.0) {
                    if (anemometerObservation.getWindSpeed() > 4.0) {
                        wind = 254;
                    } else {
                        wind = (int) (anemometerObservation.getWindSpeed() * 254) / 4;
                    }
                    windArray.add(wind);

                    if (windArray.size() == MAX_WINDARRAY) {
                        windToArrayStop();
                        Log.v("ARRAY_CHECK", "10個になったよ");
                        count = 0;
                        return;
                    }
                    //windArray.add(wind);
                    evaluateJs(webView, "addTextNode('" + wind + "')");
                    count++;
                }

                mSpeed.setText("" + wind);
            }
        });
    }

    @Override
    public void onError(String s) {
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }
}