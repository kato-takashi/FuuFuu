package com.example.katotakashi.camerapreview04;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.mlkcca.client.DataElement;
import com.mlkcca.client.DataElementValue;
import com.mlkcca.client.DataStore;
import com.mlkcca.client.Streaming;
import com.mlkcca.client.StreamingListener;
import com.mlkcca.client.MilkCocoa;
import com.mlkcca.client.DataStoreEventListener;

public class MainActivity extends Activity implements DataStoreEventListener{
    // カメラインスタンス
    private Camera mCam = null;

    // カメラプレビュークラス
    private CameraPreview mCamPreview = null;

    // 画面タッチの2度押し禁止用フラグ
    private boolean mIsTake = false;
    //フロントカメラのID
    private int frontCameraId = 1;
    //base64の格納変数
    String encodedBase64;

    //////milkcocoa
    private MilkCocoa milkcocoa;
    private Handler handler = new Handler();
    private DataStore messagesDataStore;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // カメラインスタンスの取得
        try {
            mCam = Camera.open(frontCameraId);
        } catch (Exception e) {
            // エラー
            this.finish();
        }

        // FrameLayout に CameraView クラスを設定
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        mCamPreview = new CameraPreview(this, mCam);
        preview.addView(mCamPreview);

        // mCamPreview に タッチイベントを設定
        mCamPreview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mIsTake) {
                        // 撮影中の2度押し禁止用フラグ
                        mIsTake = true;
                        // 画像取得
                        mCam.takePicture(null, null, mPicJpgListener);
                    }
                }
                return true;
            }
        });

        //milkcocoa connect
        connect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // カメラ破棄インスタンスを解放
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }
    }


    /**
     * JPEG データ生成完了時のコールバック
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/test";

            // SD カードフォルダを取得
            File file = new File(saveDir);
            Log.e("saveDir", saveDir);
            // フォルダ作成
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.i("Debug", "Make Dir Error");
                }
            }

            // 画像保存パス
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";
            //base64ファイル
//            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".txt";
//            Log.i("imgPath", imgPath);
            //bitmapに変換後→またバイトに戻す
            Bitmap smallBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            float sacaleNum = (float)0.10;
            Bitmap rszBitmap = reSizeBitmap(smallBitmap, sacaleNum, sacaleNum);
///////////////////////
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            rszBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] reSizeBytes = baos.toByteArray();

            //Base64に変換
            encodedBase64 = "data:image/jpg;base64," + Base64.encodeToString(reSizeBytes, Base64.NO_WRAP);
//            Log.i("encodedBase64", encodedBase64);
            // ファイル保存
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(imgPath, true);
                //jpeg　保存
//                fos.write(data);
                fos.write(reSizeBytes);
                //base64テキストファイル
//                fos.write(encodedBase64.getBytes());
                fos.close();
                Log.i("imgPath", imgPath);
                // アンドロイドのデータベースへ登録
                // (登録しないとギャラリーなどにすぐに反映されないため)
                registAndroidDB(imgPath);
                Toast.makeText(getApplicationContext(), "写真が保存されました。", Toast.LENGTH_SHORT).show();
                //milkcocoaへ送信
                sendEvent(this);

            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
                Toast.makeText(getApplicationContext(), "保存時にエラーが発生しました。", Toast.LENGTH_SHORT).show();
            }

            fos = null;

            // takePicture するとプレビューが停止するので、再度プレビュースタート
            mCam.startPreview();

            mIsTake = false;
        }
    };

    /**
     * アンドロイドのデータベースへ画像のパスを登録
     * @param path 登録するパス
     */
    private void registAndroidDB(String path) {
        // アンドロイドのデータベースへ登録
        // (登録しないとギャラリーなどにすぐに反映されないため)
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    ///////////
    private static Bitmap reSizeBitmap(Bitmap bmp, double rszW, double rszY){
        Matrix matrix = new Matrix();
        Bitmap bmpRsz;
        // 拡大比率
        float rsz_ratio_w = (float) rszW;
        float rsz_ratio_h = (float) rszY;
        Log.i("Bitmap rsz", String.valueOf(rsz_ratio_w));
        // 比率をMatrixに設定
        matrix.postScale(rsz_ratio_w, rsz_ratio_h);
        Log.i("Bitmap matrix", String.valueOf(matrix));
        // リサイズ画像
        bmpRsz = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),bmp.getHeight(), matrix,true);
        return bmpRsz;
    }
    ///////////

    /*
    * milkcocoa sdk
    * */
    private void connect() {
        this.milkcocoa = new MilkCocoa("readiaxuoy2r.mlkcca.com");
        this.messagesDataStore = this.milkcocoa.dataStore("androidCap");
        Streaming stream = this.messagesDataStore.streaming();
        stream.size(25);
        stream.sort("desc");
        stream.addStreamingListener(new StreamingListener() {

            @Override
            public void onData(ArrayList<DataElement> arg0) {
                final ArrayList<DataElement> messages = arg0;

                new Thread(new Runnable() {
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                for (int i = 0; i < messages.size(); i++) {
//                                    adapter.insert(messages.get(i).getValue("content"), i);
                                    Log.i("milkcocoa OK", messages.get(i).getValue("content"));
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

        this.messagesDataStore.addDataStoreEventListener((DataStoreEventListener) this);
        this.messagesDataStore.on("push");
    }

    public void sendEvent(Camera.PictureCallback view){
//        if (editText.getText().toString().length() == 0) {
//            return;
//        }
//
//        DataElementValue params = new DataElementValue();
//        params.put("content", editText.getText().toString());
//        Date date = new Date();
//        params.put("date", date.getTime());
//        this.messagesDataStore.push(params);
//        editText.setText("");
        DataElementValue params = new DataElementValue();
        String pushStr = encodedBase64;
        params.put("content", pushStr);
        Date date = new Date();
        params.put("date", date.getTime());
        this.messagesDataStore.push(params);
        Toast.makeText(getApplicationContext(), "milkcocoaへ送信。", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPushed(DataElement dataElement) {
        final DataElement pushed = dataElement;
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        String content = pushed.getValue("content");
                        Toast.makeText(getApplicationContext(), "送信完了。", Toast.LENGTH_SHORT).show();

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
}
