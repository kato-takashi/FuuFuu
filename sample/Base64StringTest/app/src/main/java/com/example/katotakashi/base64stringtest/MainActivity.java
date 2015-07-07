package com.example.katotakashi.base64stringtest;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int count1 = 0;
    private int count2 = 0;
    private int splitNum = 3000;
    HashMap<String,String> map = new HashMap<String,String>();
    ArrayList<String> keyArray = new ArrayList<String>();

    private String colorBase64 = "";
    private String monochroBase64 = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ImageView image1 = (ImageView) findViewById(R.id.imageView);
//        image1.setImageResource(R.drawable.sky01);
//        Bitmap bmp = ((BitmapDrawable)image1.getDrawable()).getBitmap();

//        colorBase64 = returnBase64(R.drawable.mori02, R.id.imageView1);
//        monochroBase64 = returnBase64(R.drawable.sky02, R.id.imageView2);
        Resources r = getResources();

        //写真の変換
        Bitmap beforeBmp = BitmapFactory.decodeResource(r, R.drawable.sky03);

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

        //リサイズ
        float scaleNum2 = (float)0.6;
        Bitmap rszBitmap2 = _reSizeBitmap(beforeBmp, scaleNum2, scaleNum2);

        String base64Str = encodeTobase64(rszBitmap2);
        Log.i("spilitBase64 length", String.valueOf(base64Str.length()));

        //配列の分割
        List<String> spilitBase64 = returnSpilit(splitNum, base64Str);
        int sendNum = spilitBase64.size();
        Log.i("spilitBase64 分割数", String.valueOf(sendNum));

        //milkcocoaへ送信
        for(int i = 0; i < sendNum; i++){
//            Log.i("spilitBase64", spilitBase64.get(i));
        }

        //milkcocoaから取得
        ArrayList<String> getBase64 = new ArrayList<String>();

        for(int i = 0; i < sendNum; i++){
            getBase64.add(spilitBase64.get(i));
        }

        //配列の連結
        StringBuilder chainBase64 = new StringBuilder();
        for (String str : getBase64) {
            chainBase64.append(str);
        }

        //デコード
        Bitmap bmp = decodeBase64(chainBase64.toString());
        ImageView image1 = (ImageView) findViewById(R.id.imageView1);
        image1.setImageBitmap(bmp);

        List<String> ss1 = returnSpilit(splitNum, colorBase64);

        for(int i = 0; i < ss1.size(); i++  ){
//            map.put(keyArray.get(0), ss1.get(i));
//            Log.i("map", String.valueOf(map));
            Log.i("mori", ss1.get(i)+"終わり");
        }

        for (String key : map.keySet()) {
//           Log.i("すべて : ", map.get(key));
        }
        for (String s : ss1){
//            Log.i("配列1", String.valueOf(count1) + s );


        }
        count1 ++;

    }

//    public String returnBase64(int id, int resId){
//        Resources r = getResources();
//        Bitmap bmp = BitmapFactory.decodeResource(r, id);
//
//        ImageView image1 = (ImageView) findViewById(resId);
//        image1.setImageBitmap(bmp);
//
//
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//        byte[] reSizeBytes = bos.toByteArray();
//
//        //Base64に変換
//        String encodedBase64Color = "data:image/jpg;base64," + Base64.encodeToString(reSizeBytes, Base64.NO_WRAP);
//        Log.i("base64 mori", encodedBase64Color);
//        Log.i("base64 color", String.valueOf(encodedBase64Color.length()) + "文字");
//        int num = encodedBase64Color.length()/splitNum;
//        Log.i("base64 分割数", String.valueOf(num) + "分割");
//        return encodedBase64Color;
//    }

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

    public List<String> returnSpilit(int n, String s){
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

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}

