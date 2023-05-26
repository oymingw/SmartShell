package util;

import android.media.AudioFormat;

public class ConfigUtils {
    public static final short FREQ_RANGE_START = 3000;
    public static final short FREQ_RANGE_END = 20000;
    public static final short FREQ_STEP = 10;
    public static final int SAMPLE_RATE = 40000;
    public static final int TIME_BAND = 8192;
    public static final int SENDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    public static final int RECEIVER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final int MAX_SIGNAL_STRENGTH = 65535;

    public static final short NONSENSE_DATA = 256;

    public static final short MIN_FREQ_THRESHOLD = 19000;
    public static final short ALARM_FREQ =  5000;
    public static final short BREAK_TIME =  5000;

    public static final boolean DEBUG_ON = true;
}
