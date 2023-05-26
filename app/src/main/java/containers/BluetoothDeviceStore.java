package containers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.smartshell_v4.R;
import util.CsvWriterHelper;
import util.TimeFormatter;
import util.ByteUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ble_tool.BluetoothDeviceExtend;
import EasyCursor.EasyObjectCursor;

public class BluetoothDeviceStore {
    private final Map<String, BluetoothDeviceExtend> mDeviceMap;

    public BluetoothDeviceStore() {
        mDeviceMap = new HashMap<>();
    }

    public BluetoothDeviceExtend GetDevice(String address){
        return mDeviceMap.get(address);
    }

    public void addDevice(final BluetoothDeviceExtend device) {
        //if (mDeviceMap.containsKey(device.getAddress())) {
        //    mDeviceMap.get(device.getAddress()).updateRssiReading(device.getTimestamp(), device.getRssi());
        //} else {
        //    mDeviceMap.put(device.getAddress(), device);
        //}
        mDeviceMap.put(device.getAddress(), device);
    }

    public void clear() {
        mDeviceMap.clear();
    }

    public EasyObjectCursor<BluetoothDeviceExtend> getDeviceCursor() {
        return new EasyObjectCursor<>(
                BluetoothDeviceExtend.class,
                getDeviceList(),
                "address");
    }

    public List<BluetoothDeviceExtend> getDeviceList() {
        final List<BluetoothDeviceExtend> methodResult = new ArrayList<>(mDeviceMap.values());

        Collections.sort(methodResult, new Comparator<BluetoothDeviceExtend>() {

            @Override
            public int compare(final BluetoothDeviceExtend arg0, final BluetoothDeviceExtend arg1) {
                return arg0.getAddress().compareToIgnoreCase(arg1.getAddress());
            }
        });

        return methodResult;
    }

    public int size(){
        return mDeviceMap.size();
    }

    private String getListAsCsv() {
        final List<BluetoothDeviceExtend> list = getDeviceList();
        final StringBuilder sb = new StringBuilder();
        sb.append(CsvWriterHelper.addStuff("mac"));
        sb.append(CsvWriterHelper.addStuff("name"));
        sb.append(CsvWriterHelper.addStuff("firstTimestamp"));
        sb.append(CsvWriterHelper.addStuff("firstRssi"));
        sb.append(CsvWriterHelper.addStuff("currentTimestamp"));
        sb.append(CsvWriterHelper.addStuff("currentRssi"));
        sb.append(CsvWriterHelper.addStuff("adRecord"));
        sb.append(CsvWriterHelper.addStuff("iBeacon"));
        sb.append(CsvWriterHelper.addStuff("uuid"));
        sb.append(CsvWriterHelper.addStuff("major"));
        sb.append(CsvWriterHelper.addStuff("minor"));
        sb.append(CsvWriterHelper.addStuff("txPower"));
        sb.append(CsvWriterHelper.addStuff("distance"));
        sb.append(CsvWriterHelper.addStuff("accuracy"));
        sb.append('\n');

        for (final BluetoothDeviceExtend device : list) {
            sb.append(CsvWriterHelper.addStuff(device.getAddress()));
            sb.append(CsvWriterHelper.addStuff(device.getName()));
            sb.append(CsvWriterHelper.addStuff(TimeFormatter.getIsoDateTime(device.getFirstTimestamp())));
            sb.append(CsvWriterHelper.addStuff(device.getFirstRssi()));
            sb.append(CsvWriterHelper.addStuff(TimeFormatter.getIsoDateTime(device.getTimestamp())));
            sb.append(CsvWriterHelper.addStuff(device.getRssi()));
            sb.append(CsvWriterHelper.addStuff(ByteUtils.byteArrayToHexString(device.getScanRecord())));
            final boolean isIBeacon = BeaconUtils.getBeaconType(device) == BeaconType.IBEACON;
            final String uuid;
            final String minor;
            final String major;
            final String txPower;
            final String distance;
            final String accuracy;

            if (isIBeacon) {
                final IBeaconDevice beacon = new IBeaconDevice(device);
                uuid = String.valueOf(beacon.getUUID());
                minor = String.valueOf(beacon.getMinor());
                major = String.valueOf(beacon.getMajor());
                txPower = String.valueOf(beacon.getCalibratedTxPower());
                distance = beacon.getDistanceDescriptor().toString().toLowerCase(Locale.US);
                accuracy = String.valueOf(beacon.getAccuracy());
            } else {
                uuid = "";
                minor = "";
                major = "";
                txPower = "";
                distance = "";
                accuracy = "";
            }

            sb.append(CsvWriterHelper.addStuff(isIBeacon));
            sb.append(CsvWriterHelper.addStuff(uuid));
            sb.append(CsvWriterHelper.addStuff(minor));
            sb.append(CsvWriterHelper.addStuff(major));
            sb.append(CsvWriterHelper.addStuff(txPower));
            sb.append(CsvWriterHelper.addStuff(distance));
            sb.append(CsvWriterHelper.addStuff(accuracy));

            sb.append('\n');
        }

        return sb.toString();
    }

    public void shareDataAsEmail(final Context context) {
        final long timeInMillis = System.currentTimeMillis();

        final String to = null;
        final String subject = context.getString(
                R.string.exporter_email_device_list_subject,
                TimeFormatter.getIsoDateTime(timeInMillis));

        final String message = context.getString(R.string.exporter_email_device_list_body);

        final Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("plain/text");
        try {
            final File outputDir = context.getCacheDir();
            final File outputFile = File.createTempFile("bluetooth_le_" + timeInMillis, ".csv", outputDir);
            outputFile.setReadable(true, false);
            generateFile(outputFile, getListAsCsv());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outputFile));
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, message);
            context.startActivity(Intent.createChooser(i, context.getString(R.string.exporter_email_device_list_picker_text)));

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static FileWriter generateFile(final File file, final String contents) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.append(contents);
            writer.flush();

        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return writer;
    }
}
