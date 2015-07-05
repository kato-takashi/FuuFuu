package com.example.simanishi.fuufuu;

//フーリエ変換のクラスです

public class FourierTransform {

    public FourierTransform() {
        //コンストラクタです
        //ここに初期化したい内容を記述できます
    }

    public byte transform (double volume) {
        //double型(マイクのボリューム)を引数にもち、byte型を返すメソッドです
        //ここでフーリエ変換の処理を記述し、byte型で変換後の値を返してください

        byte transformedVolume = 0;

        //ここではテストのため、適当に入ってきた値を100で割っています
        if (volume > 0) {
            transformedVolume = (byte) (volume / 100);
        }

        //変換後の値を返す
        return transformedVolume;
    }
}
