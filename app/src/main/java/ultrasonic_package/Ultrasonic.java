package ultrasonic_package;

public class Ultrasonic {
    static UltrasonicListener UltrasonicListener;

    public void send(int freq,int freq_0,int[] antenna_state,int symbol_rate) {
        Sender.getSender().send(freq,freq_0,antenna_state,symbol_rate);
    }

    public void stopSending() {
        Sender.getSender().stop();
    }

    public interface UltrasonicListener {
        void OnReceiveData(short freq);
    }


}
