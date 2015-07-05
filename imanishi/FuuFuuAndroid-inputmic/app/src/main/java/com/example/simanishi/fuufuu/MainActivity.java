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
import java.util.Timer;
import java.util.TimerTask;

//milkcocoa sdk
import com.mlkcca.client.DataElement;
import com.mlkcca.client.DataElementValue;
import com.mlkcca.client.DataStore;
import com.mlkcca.client.Streaming;
import com.mlkcca.client.StreamingListener;
import com.mlkcca.client.MilkCocoa;
import com.mlkcca.client.DataStoreEventListener;


public class MainActivity extends AppCompatActivity implements DataStoreEventListener, View.OnClickListener {

    //physicaroid
    private Physicaloid mPhysicaloid;
    private Handler pslHandler = new Handler();

    //milkcocoa
    private MilkCocoa milkCocoa;
    private DataStore messageDataStore;
    private Handler mcHandler = new Handler();

    //camera params
    private Camera mCam = null;
    private CameraPreview mCamPreview = null; //カメラプレビュークラス
    private boolean mIsTake = false; //画面タッチの2度押し禁止用フラグ
    private int frontCameraId = 1; //フロントカメラのID
    String encodedBase64; //base64の格納変数

    //values
    double micReachedVolume = 0.0;
    String breathArrayStr = null;
    String pushArrayStr = null;
    int micValuePushCount = 0;
    String URL = "http://simanishi.angry.jp/chat-master/";
    List<String> breathList;
    MicTimerTask micTimerTask = null;
    Timer micTimer = null;
    Handler micHandler = new Handler();
    Handler micVolumeHanler = new Handler();
    StringBuilder micValueSb = new StringBuilder();
    StringBuilder pushValueSb = new StringBuilder();

    //UIcomponents
    WebView webView;
    Button physicaroidStartBtn;

    //class
    private MicInput micInput;
    private FourierTransform ft;


    /**
     * Life Cycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //physicaroid start
        physicaroidStartBtn = (Button) findViewById(R.id.physicaloidStart);
        mPhysicaloid = new Physicaloid(getApplicationContext());

        //Physicaloid OPEN
        physicaroidStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPhysicaloid.isOpened()) {
                    if (mPhysicaloid.open()) {
                        physicaroidStartBtn.setText("CLOSE");
                    } else {
                        physicaroidStartBtn.setText("CAN NOT OPEN");

                    }
                } else {
                    mPhysicaloid.close();
                    physicaroidStartBtn.setText("OPEN");
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
        //カメラプレビュー
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        mCamPreview = new CameraPreview(this, mCam);
        preview.addView(mCamPreview);
        mCamPreview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mIsTake) {
                        mIsTake = true;
                        mCam.takePicture(null, null, mPicJpgListener);
                    }
                }
                return true;
            }
        });

        //マイク取得
        Button micButton = (Button)findViewById(R.id.micInput);
        micButton.setOnClickListener(this);

        //フーリエ変換のためのクラス
        ft = new FourierTransform();

        // WebView
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
                        + cm.sourceId() );
                return true;
            }
        });

        //milkcococa connect
        mc_connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        micTimer.cancel();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhysicaloid.close();
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button)v;
        switch( btn.getId() ){
            //スタートボタンが押されたとき
            case R.id.micInput:

                //マイク取得
                micInput = new MicInput();
                micInput.setOnVolumeReachedListener(
                        new MicInput.OnReachedVolumeListener() {
                            public void onReachedVolume(final short volume) {
                                micVolumeHanler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        micReachedVolume = volume;
                                    }
                                });
                            }
                        }
                );
                new Thread(micInput).start(); //録音開始

                //タイマー
                //マイクで取得したボリュームを配列に挿入(500ms)
                micTimerTask = new MicTimerTask();
                micTimer = new Timer(true);
                micTimer.scheduleAtFixedRate(micTimerTask, 500, 500);

                break;
            default:
                break;

        }
    }

    /**
     * Camera
     */
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
            float scaleNum = (float)0.1;
            Bitmap rszBitmap = _reSizeBitmap(smallBitmap, scaleNum, scaleNum);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            rszBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] reSizeBytes = baos.toByteArray();

            //Base64に変換
            encodedBase64 = "data:image/jpg;base64," + Base64.encodeToString(reSizeBytes, Base64.NO_WRAP);
//            Log.i("base64", encodedBase64);
            Log.i("base64文字数", String.valueOf(encodedBase64.length()));

            ///////////////////////////////////////
            float scaleNum2 = (float)0.6;
            Bitmap rszBitmap2 = _reSizeBitmap(smallBitmap, scaleNum2, scaleNum2);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            rszBitmap2.compress(Bitmap.CompressFormat.PNG, 100, baos2);
            byte[] reSizeBytes2 = baos2.toByteArray();

            //Base64に変換
            String encodedBase64_2 = "data:image/jpg;base64," + Base64.encodeToString(reSizeBytes2, Base64.NO_WRAP);
            Log.i("base64_2", encodedBase64_2);
            Log.i("base64文字数_2", String.valueOf(encodedBase64_2.length()));

            int spritNum = 3000;
            int count = 0;

            List<String> ss = StringUtils.splitAt(spritNum, encodedBase64_2);
            for (String s : ss){
                Log.i("base64文字数_2", String.valueOf(count)+ "終わり" + s );
                Log.i("base64文字数_2 文字数", String.valueOf(s.length()));
                count ++ ;
            };



            ///////////////////////////////////////
            //ファイル保存
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(imgPath, true);
                fos.write(reSizeBytes);
                fos.close();
                _registAndroidDB(imgPath);
                Toast.makeText(getApplicationContext(), "写真が保存されました", Toast.LENGTH_SHORT).show();
//                camera_mc_sendEvent(this);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "保存時にエラーが発生しました", Toast.LENGTH_SHORT).show();
            }

            fos = null;

            mCam.startPreview();
            mIsTake = false;

        }
    };

    /////////////文字列の分割
    private static class StringUtils {
        public static List<String> splitAt(int n, String s) {
            final char[] cs = s.toCharArray();
            List<String> ss = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < cs.length; i++) {
                sb.append(cs[i]);
                if ((i + 1) % n != 0) continue;
                ss.add(sb.toString());
                sb.delete(0, sb.length());
            }
            ss.add(sb.toString());

            return ss;
        }
    }

    /** Androidのデータベースへ画像パスを登録
     *
     * @param path 登録パス
     *
     */
    private void _registAndroidDB(String path) {
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /** Bitmap画像をリサイズ
     *
     * @param bmp Bitmap data
     * @param rszW 拡大比率 w
     * @param rszH 拡大比率 h
     *
     */
    private static Bitmap _reSizeBitmap(Bitmap bmp, double rszW, double rszH){
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        Bitmap bmpRsz;
        // 拡大比率
        float rsz_ratio_w = (float) rszW;
        float rsz_ratio_h = (float) rszH;
        // 比率をMatrixに設定
        matrix.postScale(rsz_ratio_w, rsz_ratio_h);
        // リサイズ画像
        bmpRsz = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),bmp.getHeight(), matrix,true);

        return bmpRsz;
    }

    /**
     * Milkcocoa
     */
    private void mc_connect() {
        milkCocoa = new MilkCocoa("leadib4y5o07.mlkcca.com");
        messageDataStore = milkCocoa.dataStore("fuufuuData");
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

    private void camera_mc_sendEvent(Camera.PictureCallback view) {
        DataElementValue params = new DataElementValue();
        //動画データ
        String dammyUrl = "http://google.com";
        params.put("movie", dammyUrl);
        Date date = new Date();
        params.put("date", date.getTime());
        messageDataStore.push(params);
        Toast.makeText(getApplicationContext(), "milkcocoaへ送信。", Toast.LENGTH_SHORT).show();
    }

    private void breath_mc_sendEvent(String value) {
        //吐息データをmilkcocoaに格納
        DataElementValue params = new DataElementValue();
        params.put("breath", value);
        messageDataStore.push(params);
        Toast.makeText(getApplicationContext(), "milkcocoaへ送信。", Toast.LENGTH_SHORT).show();
    }


    private void push_mc_datastore(String breathVal, String movieVal){

        //吐息データをmilkcocoaに格納
        DataElementValue params = new DataElementValue();
        Date date = new Date();

        params.put("breath", breathVal);
        params.put("movie", movieVal);
        params.put("date", date.getTime());
        Log.i("push_mc_datastore", breathVal);
        Log.i("push_mc_datastore", movieVal);
        messageDataStore.push(params);
    }

    private DataElementValue returnParam(String key, String arrayStr){
        DataElementValue param = new DataElementValue();
        param.put(key, arrayStr);
        return param;
    }

    @Override
    public void onPushed(final DataElement dataElement) {
        final DataElement pushed = dataElement;
        new Thread(new Runnable() {
            public void run() {
                mcHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "送信完了。", Toast.LENGTH_SHORT).show();
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

    private void evaluateJs(WebView webView, String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            webView.evaluateJavascript(script, null);
        else
            webView.loadUrl("javascript:" + script);
    }

    //milkcocoaから値を取得 -> Arduinoへ出力
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

    //2秒おきにマイクの値を取得する
    private class MicTimerTask extends TimerTask {
       final int pushNum = 2;
        @Override
        public void run() {
            micHandler.post(new Runnable() {
                @Override
                public void run() {
                    micValueSb.append(ft.transform(micReachedVolume));
                    Log.i("mic", "test: " + String.valueOf(micReachedVolume));

                    //appendしたタイミングで写真を撮影
                    if (!mIsTake) {
                        mIsTake = true;
                        mCam.takePicture(null, null, mPicJpgListener);
                    }

                    //配列が2個溜まったらpushしてタイマー停止
                    if (micValuePushCount >= pushNum) {
                        breathArrayStr = new String(micValueSb);
                        Log.i("breathArrayStr", breathArrayStr);
                        //送信
                        push_mc_datastore(breathArrayStr, encodedBase64);

                        micValuePushCount = 0;
                        //マイク入力の配列挿入インターバルを停止
                        micTimer.cancel();
                        //マイク入力を停止
                        micInput.stop();
                        micTimer = null;
                    }
                    micValueSb.append(",");
                    micValuePushCount++;
                }
            });
        }
    }
}