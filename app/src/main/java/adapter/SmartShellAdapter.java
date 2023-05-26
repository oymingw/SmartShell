package adapter;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import com.example.smartshell_v4.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import ble_tool.BluetoothDeviceExtend;
import connect.BluetoothConnectManager;
import connect.BluetoothSubScribeData;
import connect.ConnectState;
import connect.ConnectStateListener;

public class SmartShellAdapter {
    public static final int  Algorithm_1 = 1;
    public static final int  Algorithm_2 = 2;
    public static final int  Algorithm_3 = 3;
    private static final String TAG = "SmartShellAdapter";

    private String antenna_state="";

    private WifiManager wifiManager = null;
    private BluetoothConnectManager connectManager = null;
    private BluetoothGatt gatt;
    private WifiInfo wifiInfo;


    private BluetoothDeviceExtend mDevice;
    private String uuid;

    private BluetoothGattCharacteristic characteristic;
    private UUID serverUUid = null;

    private boolean start = false;
    private Thread thread;

    private FloatingActionButton start_opt;
    private TextView curRSSI;
    private TextView ssid_name;
    private TextView delta_rssi;


    private Handler handler;
    private int RSSI,RSSI0=0,RSSI1=0;
    private int loop_time=0;

    private ConnectStateListener listener = new ConnectStateListener() {
        @Override
        public void onConnectStateChanged(String address, ConnectState state) {
            switch (state){
                case CONNECTED:

                    break;
                case CONNECTING:
                    break;
                case NORMAL:

                    break;
            }
        }
    };

    public SmartShellAdapter(){
        for(int i=0;i<32;i++) antenna_state+='0';
        handler= new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==1){
                    wifiInfo= wifiManager.getConnectionInfo();
                    ssid_name.setText(wifiInfo.getSSID());
                    RSSI = wifiInfo.getRssi();
                    curRSSI.setText(RSSI+"dBm");
                    //Log.e(TAG, "handleMessage: "+wifiInfo.getRssi()+"dBm" );
                    msg.what=0;
                }
                else if(msg.what==2){
                    String text = "delta RSSI: "+msg.arg1+"dB";
                    delta_rssi.setText(text);
                    msg.what=0;
                }

            }
        };
    }


    public void setWifiManager(WifiManager manager) {
        wifiManager = manager;
    }

    public void setBleManager(BluetoothConnectManager bluetoothConnectManager) {
        connectManager = bluetoothConnectManager;
    }


    public void setDevice(BluetoothDeviceExtend device) {
        mDevice = device;
    }

    public void setUuid(String id) {
        uuid = id;
    }

    public void setButton(FloatingActionButton button) {
        start_opt = button;
    }

    public void setRSSITextView(TextView curRSSI) {
        this.curRSSI = curRSSI;
    }

    public void setSsidTextView(TextView ssid_name) {
        this.ssid_name = ssid_name;
    }

    public void setLoopTime(int loop_time) {
        this.loop_time = loop_time;
    }

    public void setDeltaRssi(TextView delta_rssi){
        this.delta_rssi = delta_rssi;
    }

    public void initBleManager() {
        connectManager.addConnectStateListener(listener);
        gatt = connectManager.getBluetoothGatt(mDevice.getAddress());
        if (gatt != null){
            List<BluetoothGattService> list = gatt.getServices();

            if (list != null){
                for (BluetoothGattService service:list){
                    for (BluetoothGattCharacteristic characteristics : service.getCharacteristics()){
                        if (characteristics.getUuid().toString().equals(uuid)){

                            characteristic = characteristics;
                            serverUUid = service.getUuid();
                            break;
                        }
                    }
                }
            }
        }else {
            Logger.e("gatt is null");
        }
        if (characteristic != null){
            //start subscribe auto
            //1.set service uuid
            connectManager.setServiceUUID(serverUUid.toString());
            //2.clean history descriptor data
            connectManager.cleanSubscribeData();
            //3.add subscribe params
            if (repeatutil.BluetoothUtils.isCharacteristicRead(characteristic.getProperties())){
                connectManager.addBluetoothSubscribeData(
                        new BluetoothSubScribeData.Builder().setCharacteristicRead(characteristic.getUuid()).build());
            }
            if (repeatutil.BluetoothUtils.isCharacteristicNotify(characteristic.getProperties())){
                connectManager.addBluetoothSubscribeData(
                        new BluetoothSubScribeData.Builder().setCharacteristicNotify(characteristic.getUuid()).build()
                );
            }
        }
    }



    public void run(Integer integer) {
        if (!wifiManager.isWifiEnabled()) {
            return;
        }
        if(start){
            stop_opt();
            Log.e(TAG,"SB");
        }else{
            switch (integer){
                case Algorithm_1: deploy_algorithm1();break;
                case Algorithm_2: deploy_algorithm2();break;
                case Algorithm_3: deploy_algorithm3();break;
            }

        }
    }
//greedy
    private void deploy_algorithm1() {
        if(!start){
            start=true;
            start_opt.setImageResource(R.drawable.ic_baseline_stop_24);
            antenna_state="";
            for(int i=0;i<32;i++) antenna_state+='0';
            StringBuilder stringBuilder = new StringBuilder(antenna_state);
            thread = new Thread() {
                public void run() {
                    int BeforeOpt=0;
                    int AfterOpt =0;
                    int delta =0;
                    while(start){
                        getRSSI();
                        BeforeOpt =RSSI;
                        RSSI0=RSSI;
                        for(int i=0;i<loop_time;i++){
                            stringBuilder.setCharAt(i,'1');
                            antenna_state = stringBuilder.toString();
                            SendState();
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            getRSSI();
                            if(RSSI<RSSI0){
                                stringBuilder.setCharAt(i,'0');
                                antenna_state = stringBuilder.toString();
                            }
                            else { RSSI0=RSSI;}
                        }
                        AfterOpt =RSSI0;
                        delta =AfterOpt-BeforeOpt;
                        sendDelta(delta);
                        Log.e(TAG,"finish!");
                    }
                }
            };
            thread.start();
        }
    }

    private void sendDelta(int delta) {
        Message msg=new Message();
        msg.what=2;
        msg.arg1=delta;
        handler.sendMessage(msg);

    }

    //退火模拟法
    private void deploy_algorithm2() {
        if(!start){
            start=true;
            start_opt.setImageResource(R.drawable.ic_baseline_stop_24);
            antenna_state="";
            for(int i=0;i<32;i++) antenna_state+='0';
            StringBuilder stringBuilder_before = new StringBuilder(antenna_state);
            thread = new Thread() {
                public void run() {
                    int BeforeOpt=0;
                    int AfterOpt =0;
                    int delta =0;
                    while(start){
                        getRSSI();
                        BeforeOpt =RSSI;
                        RSSI0=RSSI;
                        int pos1=getRandom(loop_time);
                        int pos2=getRandom(loop_time);
                        StringBuilder stringBuilder_after = new StringBuilder(stringBuilder_before.toString());
                        stringBuilder_after.setCharAt(pos1, (char) ('0'+(1-(stringBuilder_after.charAt(pos1)-'0'))));
                        stringBuilder_after.setCharAt(pos2, (char) ('0'+(1-(stringBuilder_after.charAt(pos2)-'0'))));
                        antenna_state = stringBuilder_after.toString();
                        SendState();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getRSSI();
                        RSSI1=RSSI;
                        for(int i=0;i<loop_time;i++){
                            if(RSSI1>RSSI0){
                                for(int j=0;j<32;j++){
                                    stringBuilder_before.setCharAt(i,stringBuilder_after.charAt(j));
                                }
                                RSSI0=RSSI1;
                            }
                            else{
                                if(Math.exp( (RSSI1-RSSI0))/(-60*(i+1))<Math.random()){
                                    for(int j=0;j<32;j++){
                                        stringBuilder_before.setCharAt(i,stringBuilder_after.charAt(j));
                                    }
                                    RSSI0=RSSI1;
                                }
                            }
                            for(int j=0;j<32;j++){
                                stringBuilder_after.setCharAt(i,stringBuilder_before.charAt(j));
                            }
                            pos1=getRandom(loop_time);
                            pos2=getRandom(loop_time);
                            stringBuilder_after.setCharAt(pos1, (char) ('0'+(1-(stringBuilder_after.charAt(pos1)-'0'))));
                            stringBuilder_after.setCharAt(pos2, (char) ('0'+(1-(stringBuilder_after.charAt(pos2)-'0'))));
                            antenna_state = stringBuilder_after.toString();
                            SendState();
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            getRSSI();
                            RSSI1=RSSI;
                        }
                        AfterOpt =RSSI0;
                        delta =AfterOpt-BeforeOpt;
                        sendDelta(delta);
                        Log.e(TAG,"finish!");
                    }
                }
            };
            thread.start();
        }

    }
//QS
    private void deploy_algorithm3() {
        if(!start){
            start=true;
            start_opt.setImageResource(R.drawable.ic_baseline_stop_24);
            antenna_state="";
            for(int i=0;i<32;i++) antenna_state+='0';
            StringBuilder stringBuilder_before = new StringBuilder(antenna_state);
            thread = new Thread() {
                public void run() {
                    int BeforeOpt=0;
                    int AfterOpt =0;
                    int delta =0;
                    while(start){
                        getRSSI();
                        BeforeOpt =RSSI;
                        RSSI0=RSSI;
                        int pos1=getRandom(loop_time);
                        int pos2=getRandom(loop_time);
                        StringBuilder stringBuilder_after = new StringBuilder(stringBuilder_before.toString());
                        stringBuilder_after.setCharAt(pos1, (char) ('0'+(1-(stringBuilder_after.charAt(pos1)-'0'))));
                        stringBuilder_after.setCharAt(pos2, (char) ('0'+(1-(stringBuilder_after.charAt(pos2)-'0'))));
                        antenna_state = stringBuilder_after.toString();
                        SendState();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getRSSI();
                        RSSI1=RSSI;
                        int flag = 0;
                        int Delta = RSSI1-RSSI0;
                        for(int i=0;i<loop_time;i++){
                            if(Delta>0){
                                for(int j=0;j<32;j++){
                                    stringBuilder_before.setCharAt(i,stringBuilder_after.charAt(j));
                                }
                                RSSI0=RSSI1;
                                flag++;
                            }
                            else{
                                if(Math.exp( (RSSI1-RSSI0))/(-60*(i+1))<Math.random()){
                                    for(int j=0;j<32;j++){
                                        stringBuilder_before.setCharAt(i,stringBuilder_after.charAt(j));
                                    }

                                }
                            }
                            for(int j=0;j<32;j++){
                                stringBuilder_after.setCharAt(i,stringBuilder_before.charAt(j));
                            }
                            if(flag >= 1 && Delta >0){
                                pos1=getRandom(loop_time);
                                stringBuilder_after.setCharAt(pos1, (char) ('0'+(1-(stringBuilder_after.charAt(pos1)-'0'))));
                            }
                            if(flag<2){
                            pos1=getRandom(loop_time);
                            pos2=getRandom(loop_time);
                            stringBuilder_after.setCharAt(pos1, (char) ('0'+(1-(stringBuilder_after.charAt(pos1)-'0'))));
                            stringBuilder_after.setCharAt(pos2, (char) ('0'+(1-(stringBuilder_after.charAt(pos2)-'0'))));}
                            antenna_state = stringBuilder_after.toString();
                            SendState();
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            getRSSI();
                            RSSI1=RSSI;
                            Delta = RSSI1-RSSI0;
                            flag=0;
                        }
                        AfterOpt =RSSI0;
                        delta =AfterOpt-BeforeOpt;
                        sendDelta(delta);
                        Log.e(TAG,"finish!");
                    }
                }
            };
            thread.start();
        }
    }

    private int getRandom(int loop_time){
        Random random = new Random();
        int result = random.nextInt(loop_time);
        return result;
    }

    private void stop_opt() {

        if (start) {
            if (thread != null) {
                try {
                    thread.interrupt();
                    thread = null;
                } catch (Exception e) {
                    Log.e("Err", e.toString());
                }
            }

            start = false;
            start_opt.setImageResource(R.drawable.ic_baseline_play_arrow_white_24dp);

        }}

    private void ChangeState() {
        if(RSSI0==0&&RSSI1==0){

        }
        else{}
    }

    private void SendState() {
        if (characteristic != null) {
            int len = antenna_state.length();
            if (len > 0 && len % 2 == 0) {
                len = antenna_state.length() / 8;
                byte[] bytes = new byte[len];
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        Integer val = Integer.valueOf(antenna_state.substring(i * 8, i * 8 + 8), 2);
                        bytes[i] = val.byteValue();
                    }}

                if (bytes != null) {
                    characteristic.setValue(bytes);
                    gatt.writeCharacteristic(characteristic);

                } else {
                    Logger.e("write value fail");
                }
            } else {
                //Toast.makeText(Activity2.this, "Input value is invalid, you should input like(hex value): 01, 1101, 0A11", Toast.LENGTH_LONG).show();
            }
        } else return; //Toast.makeText(Activity2.this, "wow", Toast.LENGTH_LONG).show();

    }



    private void getRSSI() {
        // scanning
        Message msg=new Message();
        msg.what=1;
        handler.sendMessage(msg);
    }



}
