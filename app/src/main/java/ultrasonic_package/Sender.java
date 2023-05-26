package ultrasonic_package;

import android.media.AudioTrack;
import android.util.Log;

import util.AudioUtils;

public class Sender {
    private static Sender sSender;

    private AudioTrack mAudioTrack;



    static Sender getSender() {
        if (sSender == null) {
            sSender = new Sender();
        }
        return sSender;
    }

    void send(int freq,int freq_0,int[] antenna_state,int symbol_rate) {
        float duration = 1F;
        float[] freqArray = AudioUtils.generateFreqArray(freq,freq_0, duration,antenna_state,symbol_rate);

        if (mAudioTrack == null) {
            mAudioTrack = AudioUtils.generateAudioTrack();
            mAudioTrack.write(freqArray, 0, freqArray.length, AudioTrack.WRITE_BLOCKING);
            play();
        }
    }

    private synchronized void play() {
        if (mAudioTrack == null) return;
        mAudioTrack.play();

        mAudioTrack.stop();
        mAudioTrack.release();
        mAudioTrack = null;
                }


    synchronized void stop() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }

    }

}
