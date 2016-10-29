package felixgimeno.selftracking;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class dbRow {
    double latitude;
    double longitude;
    double altitude;
    long time;
    String wifiName;
    int cellIdentity;
    float batteryPct;

    private dbRow(Context context) {
        try {
            Location location = getLocation(context);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            time = getTime();
            wifiName = getWifiName(context);
            cellIdentity = getLTECellID(context);
            batteryPct = getBattery(context);
            writeThis(context);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("felix", e.getMessage());
        }
    }

    private static Location getLocation(Context context) throws SecurityException {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private static long getTime() {
        return System.currentTimeMillis();
    }

    private static String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return "";
    }

    private static int getLTECellID(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> lci = tm.getAllCellInfo();
        for (CellInfo ci : lci) {
            if (ci instanceof CellInfoLte) {
                CellInfoLte cit = (CellInfoLte) ci;
                CellIdentityLte cil = cit.getCellIdentity();
                return cil.getCi();
            }
        }
        return 0;
    }

    private static float getBattery(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null) return -1;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level / (float) scale;
    }

    public static void writeData(Context cx) {
        new dbRow(cx);
    }

    private void writeThis(Context context) throws IOException {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "selftracking.txt");
        String dbRowJSON = new Gson().toJson(this);
        final boolean append = true;
        FileOutputStream outputStream = new FileOutputStream(file, append);
        outputStream.write(dbRowJSON.getBytes());
        outputStream.close();
    }
}
