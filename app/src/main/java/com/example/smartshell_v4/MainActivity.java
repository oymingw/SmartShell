package com.example.smartshell_v4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ble_tool.BluetoothDeviceExtend;
import connect.BluetoothConnectManager;
import connect.ConnectState;
import connect.ConnectStateListener;
import connect.GattError;
import connect.multiple.MultiConnectManager;
import containers.BluetoothDeviceStore;
import event.UpdateEvent;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;
import scan.BluetoothScanManager;
import scan.ScanOverListener;
import scan.bluetoothcompat.ScanCallbackCompat;
import scan.bluetoothcompat.ScanResultCompat;
import util.BluetoothUtils;
import util.IntentUtils;
import util.LocationUtils;
import widget.MyAlertDialog;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText num;
    private ProgressBar pb;
    private BluetoothUtils mBluetoothUtils;
    private BluetoothDeviceStore mDeviceStore;
    private BluetoothScanManager scanManager;
    private BluetoothConnectManager connectManager;
    private boolean filterSwitch=true;
    private String filterName="Simple Peripheral";
    private int filterRssi=-100;
    private String[] permissionList = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    };
    private BluetoothDeviceExtend device;
    private int connectState = 0;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private boolean mConnected = false;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initScan();
        scan();
        work();
    }

    private void initScan() {
        mBluetoothUtils = new BluetoothUtils(this);
        mDeviceStore = new BluetoothDeviceStore();
        scanManager = BluetoothScanManager.getInstance(this);
        scanManager.setScanOverListener(new ScanOverListener() {
            @Override
            public void onScanOver() {
                if (scanManager.isPauseScanning()){
                    invalidateOptionsMenu();
                }
            }
        });
        scanManager.setScanCallbackCompat(new ScanCallbackCompat() {
            @Override
            public void onBatchScanResults(List<ScanResultCompat> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(final int errorCode) {
                super.onScanFailed(errorCode);
                if (errorCode == SCAN_FAILED_LOCATION_CLOSE){
                    Toast.makeText(MainActivity.this, "Location is closed, you should open first", Toast.LENGTH_LONG).show();
                }else if(errorCode == SCAN_FAILED_LOCATION_PERMISSION_FORBID){
                    Toast.makeText(MainActivity.this, "You have not permission of location", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this, "Other exception", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onScanResult(int callbackType, ScanResultCompat result) {
                super.onScanResult(callbackType, result);
                String deviceName = result.getScanRecord().getDeviceName();

                if (deviceName != null) deviceName = deviceName.toLowerCase();
                if (filterSwitch) {
                    if (filterRssi <= result.getRssi()) {
                        if (filterName == null || filterName.equals("")) {
                            mDeviceStore.addDevice(result.getLeDevice());
                        } else if (filterName.toLowerCase().equals(deviceName)) {
                            mDeviceStore.addDevice(result.getLeDevice());
                            Log.e(TAG,"scan device "+result.getLeDevice().getAddress()+" "+deviceName);

                            scanManager.stopCycleScan();
                            connect(result.getLeDevice().getAddress());
                            //saveFileLog(result.getDevice().getAddress(), result.getScanRecord().getBytes(), result.getLeDevice().getTimestamp());
                        }
                    }
                } else {
                    mDeviceStore.addDevice(result.getLeDevice());
                }
                EventBus.getDefault().post(new UpdateEvent(UpdateEvent.Type.SCAN_UPDATE));
            }
        });
    }

    private void connect(String address) {
        MultiConnectManager.getInstance(this).addDeviceToQueue(address);
        MultiConnectManager.getInstance(this).startConnect();
        Toast.makeText(MainActivity.this, "Connect Successfully!", Toast.LENGTH_LONG).show();
        device=(BluetoothDeviceExtend) mDeviceStore.GetDevice(address);
        connectManager = BluetoothConnectManager.getInstance(this);
        connectManager.addConnectStateListener(stateListener);
        connectManager.setBluetoothGattCallback(new BluetoothGattCallback() {

            @Override
            public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    EventBus.getDefault().post(new UpdateEvent(UpdateEvent.Type.BLE_DATA, characteristic, "read"));
                }else{
                    EventBus.getDefault().post(new UpdateEvent(UpdateEvent.Type.BLE_DATA, characteristic, "fail"));
                    Log.e(TAG, "fail to read characteristic");
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    EventBus.getDefault().post(new UpdateEvent(UpdateEvent.Type.BLE_DATA, characteristic, "write"));
                }else{
                    EventBus.getDefault().post(new UpdateEvent(UpdateEvent.Type.BLE_DATA, characteristic, "fail"));
                    Log.e(TAG, "fail to write characteristic");
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                EventBus.getDefault().post(new UpdateEvent(UpdateEvent.Type.BLE_DATA, characteristic, "notify"));
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, final int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Disconnect! error："+ GattError.parseConnectionError(status), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayGattServices(gatt.getServices());
                        }
                    });
                }
            }
        });
        connectManager.connect(device.getAddress());
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(final List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        final List<Map<String, String>> gattServiceData = new ArrayList<>();
        final List<List<Map<String, String>>> gattCharacteristicData = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (final BluetoothGattService gattService : gattServices) {
            final Map<String, String> currentServiceData = new HashMap<>();
            gattServiceData.add(currentServiceData);
            final List<Map<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            final List<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                final Map<String, String> currentCharaData = new HashMap<>();

                gattCharacteristicGroupData.add(currentCharaData);

            }

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

    }



    private ConnectStateListener stateListener = new ConnectStateListener() {
        @Override
        public void onConnectStateChanged(String address, ConnectState state) {
            switch (state) {
                case CONNECTED:
                    connectState = 1;
                    mConnected = true;

                    break;
                case CONNECTING:
                    mConnected = false;
                    break;
                case NORMAL:
                    connectState = 2;
                    mConnected = false;

                    break;
            }
            invalidateOptionsMenu();
        }
    };

    private void scan() {

        if (checkPermission()){
            if (checkIsBleState()){
                mDeviceStore.clear();
                EventBus.getDefault().post(new UpdateEvent(UpdateEvent.Type.SCAN_UPDATE));
//                scanManager.startCycleScan();
                scanManager.startScanNow();
                invalidateOptionsMenu();
            }
        }

        //MultiConnectManager.getInstance(this).startConnect();
    }
    private boolean checkIsBleState(){
        if (!mBluetoothUtils.isBluetoothLeSupported()){
            showNotSupportDialog();
        }else if(!mBluetoothUtils.isBluetoothOn()){
            showOpenBleDialog();
        }else{
            return true;
        }
        return false;
    }

    private void showNotSupportDialog(){
        MyAlertDialog.getDialog(this, R.string.ble_not_support, R.string.ble_exit_app,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }


    private void work() {
        pb=findViewById(R.id.pb);
        num=findViewById(R.id.number);
    }


    public void start_opt(View view) {

        pb.setVisibility(View.VISIBLE);
        //send data

        String num_text = num.getText().toString();

        if(num_text.length()==0)num_text = "30";
        BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(3).get(2);

        Intent intent=new Intent(this,Activity2.class);

        Bundle bundle=new Bundle();
        bundle.putString("param", num_text);
        intent.putExtras(bundle);
        intent.putExtra("ExtraDevice", device);
        intent.putExtra("ExtraUuid", characteristic.getUuid().toString());
        //intent.putExtra("ExtraUuid", "");

        //open activity
        startActivity(intent);

        pb.setVisibility(View.INVISIBLE);
    }


    /**
     * 是否打开ble
     */
    private void showOpenBleDialog() {
        MyAlertDialog.getDialog(this, R.string.ble_not_open, R.string.ble_open, R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
                        dialog.dismiss();
                    }
                },
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 重新检查位置信息是否开启
     */
    private void showReOpenLocationDialog() {
        MyAlertDialog.getDialog(this, R.string.ble_location_not_open, R.string.ble_location_open, R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IntentUtils.startLocationSettings(MainActivity.this, 12);
                        dialog.dismiss();
                    }
                },
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 打开位置信息
     */
    private void showOpenLocationSettingDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.include_location_dialog, null);
        MyAlertDialog.getViewDialog(this, view, R.string.ble_location_open, R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IntentUtils.startLocationSettings(MainActivity.this, 11);
                        dialog.dismiss();
                    }
                },
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showReOpenLocationDialog();
                        dialog.dismiss();
                    }
                }, false).show();
    }


    /**
     * 检查权限
     * @return
     */
    public boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= 23){
            boolean hasPermission = PermissionUtils.hasSelfPermissions(this, permissionList);
            MainActivityPermissionsDispatcher.showCheckPermissionStateWithCheck(this);
            if (!LocationUtils.isGpsProviderEnabled(this)){
                return false;
            }
            return hasPermission;
        }
        return true;
    }


    //请求权限
    /**
     * 这个方法中写正常的逻辑（假设有该权限应该做的事）
     */
    @NeedsPermission({Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    })
    void showCheckPermissionState(){
        //检查是否开启位置信息（如果没有开启，则无法扫描到任何蓝牙设备在6.0）
        if (!LocationUtils.isGpsProviderEnabled(this)){
            showOpenLocationSettingDialog();
        }
    }

    /**
     * 弹出权限同意窗口之前调用的提示窗口
     * @param request
     */
    @OnShowRationale({Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    })
    void showRationaleForPermissionState(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        MyAlertDialog.showRationaleDialog(this, R.string.permission_rationale, request);
    }

    /**
     * 提示窗口和权限同意窗口--被拒绝时调用
     */
    @OnPermissionDenied({Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    })
    void onPermissionStateDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
    }

    /**
     * 当完全拒绝了权限打开之后调用
     */
    @OnNeverAskAgain({Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    })
    void onPermissionNeverAskAgain() {
        MyAlertDialog.showOpenSettingDialog(this, R.string.open_setting_permission);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}