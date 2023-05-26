package com.example.smartshell_v4;

import static adapter.SmartShellAdapter.Algorithm_1;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import adapter.SmartShellAdapter;
import ble_tool.BleManager;
import ble_tool.BluetoothDeviceExtend;
import connect.BluetoothConnectManager;
import connect.BluetoothSubScribeData;
import connect.ConnectState;
import connect.ConnectStateListener;


public class Activity2 extends AppCompatActivity {
    private static final String TAG = "Activity2";
    private FloatingActionButton start_opt;
    private int loop_time;

    private WifiManager wifiManager;
    private TextView curRSSI;
    private Thread thread;
    private TextView ssid_name;
    private TextView delta_rssi;
    private WifiInfo wifiInfo;
    private BluetoothDeviceExtend  mDevice;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private BluetoothConnectManager connectManager;
    private BluetoothGatt gatt;

    private SmartShellAdapter smartShellAdapter;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);
        initView();

        //receive num from mainActivity
        Bundle bundle =this.getIntent().getExtras();
        loop_time=Integer.parseInt(bundle.getString("param"));
        Intent intent = getIntent();
        mDevice = (BluetoothDeviceExtend) intent.getParcelableExtra("ExtraDevice");
        String uuid = intent.getStringExtra("ExtraUuid");

        //receive data
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        BleManager.setBleParamsOptions(ConstValue.getBleOptions(this));

        //set smartShellAdapter
        smartShellAdapter = new SmartShellAdapter();
        smartShellAdapter.setDevice(mDevice);
        smartShellAdapter.setUuid(uuid);
        smartShellAdapter.setLoopTime(loop_time);
        initButton();
        smartShellAdapter.setButton(start_opt);
        smartShellAdapter.setRSSITextView(curRSSI);
        smartShellAdapter.setSsidTextView(ssid_name);
        smartShellAdapter.setDeltaRssi(delta_rssi);
        smartShellAdapter.setWifiManager(wifiManager);
        connectManager = BluetoothConnectManager.getInstance(this);
        smartShellAdapter.setBleManager(connectManager);
        smartShellAdapter.initBleManager();


    }

    private void initView() {
        curRSSI=findViewById(R.id.curRSSI);
        ssid_name=findViewById(R.id.ssid_name);
        start_opt=findViewById(R.id.start_opt);
        delta_rssi=findViewById(R.id.delta_rssi);
    }


    private void initButton() {
        start_opt.setOnClickListener(v -> {
            smartShellAdapter.run(Algorithm_1);
        });
    }

}
























