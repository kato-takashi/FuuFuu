package com.example.simanishi.fuufuu;

/**
 * Created by simanishi on 2015/06/28.
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class MicInput implements Runnable {

    //ボリューム感知リスナー
    private OnReachedVolumeListener mListener;
    //録音中フラグ
    private boolean isRecoding = true;
    //サンプリングレート
    private static final int SAMPLE_RATE = 8000; //80.0KHz
    //ボーダー音量
    private short mBorderVolume = 1;
    //ボーダー音量をセット
    public void setmBorderVolume(short volume) {
        mBorderVolume = volume;
    }
    //ボーダー音量を取得
    public short getmBorderVolume() {
        return mBorderVolume;
    }
    //録音を停止
    public void stop() {
        isRecoding = false;
    }

    public void setOnVolumeReachedListener(OnReachedVolumeListener listener) {
        mListener = listener;
    }

    //ボーダー音量を検知した時のためのリスナー
    public interface OnReachedVolumeListener {
        void onReachedVolume(short volume);
    }

    //スレッド開始（録音を開始）
    public void run() {
        android.os.Process.setThreadPriority(
                android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);
        short[] buffer = new short[bufferSize];
        audioRecord.startRecording();
        while(isRecoding) {
            audioRecord.read(buffer, 0, bufferSize);
            short max = 0;
            for (int i=0; i<bufferSize; i++) {
                //最大音量を計算
                max = (short)Math.max(max, buffer[i]);
                //最大音量がボーダーを超えていたら
                if (max > mBorderVolume) {
                    if (mListener != null) {
                        //リスナーを実行
                        mListener.onReachedVolume(max);
                        break;
                    }
                }
            }
        }
        audioRecord.stop();
        audioRecord.release();
    }

}