package com.example.livecamcall;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;

public class SmartAssistantActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        
        TextView title = new TextView(this);
        title.setText("🧠 المساعد الذكي");
        title.setTextSize(24);
        layout.addView(title);
        
        // فحص الصلاحيات
        TextView camCheck = new TextView(this);
        boolean hasCam = checkSelfPermission(Manifest.permission.CAMERA) 
                        == PackageManager.PERMISSION_GRANTED;
        camCheck.setText(hasCam ? "✅ الكاميرا: مفعلة" : "❌ الكاميرا: غير مفعلة");
        layout.addView(camCheck);
        
        TextView micCheck = new TextView(this);
        boolean hasMic = checkSelfPermission(Manifest.permission.RECORD_AUDIO) 
                        == PackageManager.PERMISSION_GRANTED;
        micCheck.setText(hasMic ? "✅ الميكروفون: مفعل" : "❌ الميكروفون: غير مفعل");
        layout.addView(micCheck);
        
        // فحص الواي فاي
        SmartWifiHelper wifiHelper = new SmartWifiHelper(this);
        TextView wifiCheck = new TextView(this);
        wifiCheck.setText("📶 " + wifiHelper.getWifiStatus());
        if (wifiHelper.isWifiConnected()) {
            wifiCheck.append("\n📱 IP: " + wifiHelper.getLocalIpAddress());
        }
        layout.addView(wifiCheck);
        
        setContentView(layout);
    }
}