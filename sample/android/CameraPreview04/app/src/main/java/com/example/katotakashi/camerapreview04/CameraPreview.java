package com.example.katotakashi.camerapreview04;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by katotakashi on 15/06/18.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCam;

    /**
     * コンストラクタ
     */
    public CameraPreview(Context context, Camera cam) {
        super(context);

        mCam = cam;

        // サーフェスホルダーの取得とコールバック通知先の設定
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * SurfaceView 生成
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // カメラインスタンスに、画像表示先を設定
            mCam.setPreviewDisplay(holder);
            /////////
            ////////
            // プレビュー開始
            mCam.startPreview();

        } catch (IOException e) {
            //
            e.printStackTrace();
        }
    }

    /**
     * SurfaceView 破棄
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCam.release();
        mCam = null;
    }

    /**
     * SurfaceHolder が変化したときのイベント
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 画面回転に対応する場合は、ここでプレビューを停止し、
        // 回転による処理を実施、再度プレビューを開始する。
        mCam.stopPreview();
        int maxSize = 320;
        setPictureSize(mCam, maxSize);
        mCam.startPreview();
    }
    //撮影サイズの取得 //setPictureSizeにはgetSupportedPictureSizesで得られるセット以外は入れない
    public void setPictureSize(Camera cam, int maxWidth) {
        //端末がサポートするサイズ一覧取得
        Camera.Parameters params = cam.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        if ( sizes != null && sizes.size() > 0) {
            //撮影サイズを設定する
            Camera.Size setSize = sizes.get(0);
            for(Camera.Size size : sizes){
                if(Math.min(size.width, size.height) <= maxWidth) {
                    setSize = size;
                    break;
                }
            }
            params.setPictureSize(setSize.width, setSize.height);
            cam.setParameters(params);
        }
    }

}