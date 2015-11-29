package fr.an.screencast.compressor.imgstream.codecs.humbleio;

import io.humble.video.Rational;

public class HumbleIODoNothing {


    public static void main(String[] args) {
        Rational ratio = Rational.make(1, 2);
        System.out.println("ok, checked using jni, io.humble.video.Rational " + ratio.getNumerator() + "/" + ratio.getDenominator());
        System.out.println("Finished");
    }
}
