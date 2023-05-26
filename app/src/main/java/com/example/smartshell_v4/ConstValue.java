package com.example.smartshell_v4;

import android.content.Context;
import util.Constants;
import util.PreferencesUtils;
import ble_tool.BleParamsOptions;
import connect.ConnectConfig;
import adrecord.AdRecord;
import java.util.HashMap;
import java.util.Map;

public final class ConstValue   {

    private static BleParamsOptions.Builder options = new BleParamsOptions.Builder()
            .setBackgroundBetweenScanPeriod(5 * 60 * 1000)
            .setBackgroundScanPeriod(10000)
            .setForegroundBetweenScanPeriod(5000)
            .setForegroundScanPeriod(15000)
            .setDebugMode(BuildConfig.DEBUG)
            .setMaxConnectDeviceNum(5)
            .setReconnectBaseSpaceTime(8000)
            .setReconnectMaxTimes(4)
            .setReconnectStrategy(ConnectConfig.RECONNECT_FIXED_TIME)
            .setReconnectedLineToExponentTimes(5);

    public static BleParamsOptions getBleOptions(Context context){
        int scanPeriod = PreferencesUtils.getInt(context, Constants.SCAN_PERIOD, 10*1000);
        int pausePeriod = PreferencesUtils.getInt(context, Constants.PAUSE_PERIOD, 5*1000);
        return options.setForegroundScanPeriod(scanPeriod)
                .setForegroundBetweenScanPeriod(pausePeriod)
                .build();
    }

    public static Map<Integer, String> RECORD_MAP = new HashMap<>();
    static {
        RECORD_MAP.put(AdRecord.TYPE_CONNECTION_INTERVAL_RANGE, "Slave Connection Interval Range");
        RECORD_MAP.put(AdRecord.TYPE_DEVICE_CLASS,"Class of device");
        RECORD_MAP.put(AdRecord.TYPE_FLAGS,"Flags");
        RECORD_MAP.put(AdRecord.TYPE_MANUFACTURER_SPECIFIC_DATA,"Manufacturer Specific Data");
        RECORD_MAP.put(AdRecord.TYPE_LOCAL_NAME_COMPLETE,"Name (Complete)");
        RECORD_MAP.put(AdRecord.TYPE_LOCAL_NAME_SHORT,"Name (Short)");
        RECORD_MAP.put(AdRecord.TYPE_SECURITY_MANAGER_OOB_FLAGS,"Security Manager OOB Flags");
        RECORD_MAP.put(AdRecord.TYPE_SERVICE_UUIDS_LIST_128BIT,"Service UUIDs (128bit)");
        RECORD_MAP.put(AdRecord.TYPE_SERVICE_UUIDS_LIST_16BIT,"Service UUIDs (16bit)");
        RECORD_MAP.put(AdRecord.TYPE_SERVICE_DATA,"Service Data");
        RECORD_MAP.put(AdRecord.TYPE_SIMPLE_PAIRING_HASH_C,"Simple Pairing Hash C");
        RECORD_MAP.put(AdRecord.TYPE_SIMPLE_PAIRING_RANDOMIZER_R,"Simple Pairing Randomizer R");
        RECORD_MAP.put(AdRecord.TYPE_TK_VALUE,"TK Value");
        RECORD_MAP.put(AdRecord.TYPE_TX_POWER_LEVEL,"Transmission Power Level");
        RECORD_MAP.put(AdRecord.TYPE_UUID128,"Complete list of 128-bit UUIDs available");
        RECORD_MAP.put(AdRecord.TYPE_UUID128_INC,"More 128-bit UUIDs available");
        RECORD_MAP.put(AdRecord.TYPE_UUID16,"Complete list of 16-bit UUIDs available");
        RECORD_MAP.put(AdRecord.TYPE_UUID16_INC,"More 16-bit UUIDs available");
        RECORD_MAP.put(AdRecord.TYPE_UUID32,"Complete list of 32-bit UUIDs available");
        RECORD_MAP.put(AdRecord.TYPE_UUID32_INC,"More 32-bit UUIDs available");
    }
}
