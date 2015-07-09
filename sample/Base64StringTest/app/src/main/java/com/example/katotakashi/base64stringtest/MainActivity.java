package com.example.katotakashi.base64stringtest;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mlkcca.client.DataElement;
import com.mlkcca.client.DataElementValue;
import com.mlkcca.client.DataStore;
import com.mlkcca.client.DataStoreEventListener;
import com.mlkcca.client.MilkCocoa;
import com.mlkcca.client.Streaming;
import com.mlkcca.client.StreamingListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataStoreEventListener {

//    private int splitNum = 3000;
    private int splitNum = 3;
    HashMap<String, String> map = new HashMap<String, String>();
    ArrayList<String> keyArray = new ArrayList<String>();
    //milkcocoaから取得
    ArrayList<String> getBase64 = new ArrayList<String>();
    //配列の連結
    StringBuilder chainBase64 = new StringBuilder();

    //分割数
    int sendNum = 0;

    //milkcocoa
    private MilkCocoa milkCocoa;
    private DataStore messageDataStore;
    private Handler mcHandler = new Handler();
    Streaming stream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //milkcococa connect
        milkCocoa = new MilkCocoa("leadib4y5o07.mlkcca.com");
        messageDataStore = milkCocoa.dataStore("base64Test");
        stream = messageDataStore.streaming();

        mc_connect();

        Button button = (Button) findViewById(R.id.btn1);


        Resources r = getResources();
        //写真の変換
        Bitmap beforeBmp = BitmapFactory.decodeResource(r, R.drawable.sky01);

        //バイト配列にしたらどうか調査
        ////////////////
//        byte[] testByte = getBitmapAsByteArray(beforeBmp);
//        Log.i("byteTest　配列の数", String.valueOf(testByte.length));
//        StringBuilder sb = new StringBuilder();
//        for(int i=0; i<testByte.length; i++){
////            Log.i("byteTest", "バイトの中身"+String.valueOf(testByte[i]));
//            sb.append(String.valueOf(testByte[i] + ","));
//        }
//        Log.i("byteTest", "文字数" + String.valueOf(sb.toString().length()) + "バイトの中身" + sb.toString());

        ////////////////

        //モノクロ
        //monocrome(beforeBmp);

        //リサイズ
        float scaleNum2 = (float) 0.35;
        Bitmap rszBitmap2 = _reSizeBitmap(monocrome(beforeBmp), scaleNum2, scaleNum2);

//        String base64Str = encodeTobase64(rszBitmap2);
        String base64Str = "かとうたかし加藤貴司カトウタカシ";
        Log.i("spilitBase64 length", String.valueOf(base64Str.length()));

        //配列の分割
        final List<String> spilitBase64 = returnSpilit(splitNum, base64Str);
        sendNum = spilitBase64.size();
        Log.i("spilitBase64 分割数", String.valueOf(sendNum));


//        for(int i = 0; i < sendNum; i++){
//            getBase64.add(spilitBase64.get(i));
//        }
        // ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンがクリックされた時に呼び出されます
                Button button = (Button) v;
                Toast.makeText(MainActivity.this, "onClick()",
                        Toast.LENGTH_SHORT).show();
                //milkcocoaへ送信
                for (int i = 0; i < sendNum; i++) {
//            Log.i("spilitBase64", spilitBase64.get(i));
                    push_mc_datastore(spilitBase64.get(i));
                }
            }
        });
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

    //モノクロ処理
    public static Bitmap monocrome(Bitmap bmp) {
        // モノクロにする処理
        Bitmap outBitMap = bmp.copy(Bitmap.Config.ARGB_8888, true);

        int width = outBitMap.getWidth();
        int height = outBitMap.getHeight();
        int totalPixcel = width * height;

        int i, j;
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                int pixelColor = outBitMap.getPixel(i, j);
                int y = (int) (0.299 * Color.red(pixelColor) + 0.587 * Color.green(pixelColor) + 0.114 * Color
                        .blue(pixelColor));
                outBitMap.setPixel(i, j, Color.rgb(y, y, y));
            }
        }

        return outBitMap;
    }

    //byte配列に格納
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //PNG, クオリティー100としてbyte配列にデータを格納
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
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

    public List<String> returnSpilit(int n, String s) {
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

    //Base64変換
    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    /**
     * Milkcocoa
     */
    private void mc_connect() {
        stream.size(25);
        stream.sort("asc");
        stream.addStreamingListener(new StreamingListener() {
            @Override
            public void onData(ArrayList<DataElement> arg0) {
                final ArrayList<DataElement> messages = arg0;
                new Thread(new Runnable() {
                    public void run() {
                        mcHandler.post(new Runnable() {
                            public void run() {
//                                if(sendNum >0){
//                                    for (int i = 0; i < sendNum; i++) {
//                                        Log.i("milkcocoa OK", messages.get(i).getValue("pct"));
//                                        getBase64.add(messages.get(i).getValue("pct"));
//                                    }
//                                    //配列の連結
//                                    for (String str : getBase64) {
//                                        chainBase64.append(str);
//                                    }
//                                    Log.i("合成　Base64", chainBase64.toString());
//                                    //デコード
//                                    Bitmap bmp = decodeBase64(chainBase64.toString());
//                                    ImageView image1 = (ImageView) findViewById(R.id.imageView1);
//                                    image1.setImageBitmap(bmp);
//
//                                }

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

    private void push_mc_datastore(String base64Val) {
        //吐息データをmilkcocoaに格納
        DataElementValue params = new DataElementValue();
        Date date = new Date();

        params.put("pct", base64Val);
        params.put("date", date.getTime());
        Log.i("push_mc_datastore", base64Val);
        messageDataStore.push(params);
    }

    @Override
    public void onPushed(DataElement dataElement) {
        final DataElement pushed = dataElement;
        new Thread(new Runnable() {
            public void run() {
                mcHandler.post(new Runnable() {
                    public void run() {

                        Log.i("get mc", String.valueOf(pushed.getValue()));

                        for (int i = 0; i < 4; i++) {
                            Log.i("get mc for", pushed.getValue("pct"));
                        }
                        //配列の連結
//                        for (String str : getBase64) {
//                            chainBase64.append(str);
//                        }
//                        Log.i("pushされたよ", "get get get");
//                        Log.i("合成　Base64", chainBase64.toString());
//                        //デコード
//                        Bitmap bmp = decodeBase64(chainBase64.toString());
//                        ImageView image1 = (ImageView) findViewById(R.id.imageView1);
//                        image1.setImageBitmap(bmp);

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

