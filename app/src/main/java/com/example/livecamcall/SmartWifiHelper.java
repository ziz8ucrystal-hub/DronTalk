package com.example.livecamcall;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;

public class SmartWifiHelper {
    
    private Context context;
    private WifiManager wifiManager;
    
    public SmartWifiHelper(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
    }
    
    public void openWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    public boolean isWifiConnected() {
        try {
            return wifiManager != null && 
                   wifiManager.isWifiEnabled() && 
                   wifiManager.getConnectionInfo() != null &&
                   wifiManager.getConnectionInfo().getIpAddress() != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getLocalIpAddress() {
        try {
            int ip = wifiManager.getConnectionInfo().getIpAddress();
            return android.text.format.Formatter.formatIpAddress(ip);
        } catch (Exception e) {
            return "غير متاح";
        }
    }
    
    public String getWifiStatus() {
        if (wifiManager == null) return "غير معروف";
        if (!wifiManager.isWifiEnabled()) return "❌ الواي فاي مغلق";
        if (wifiManager.getConnectionInfo().getIpAddress() == 0) return "⚠️ غير متصل";
        return "✅ متصل";
    }
}