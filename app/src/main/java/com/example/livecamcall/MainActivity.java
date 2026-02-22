package com.example.livecamcall;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    
    private SmartWifiHelper wifiHelper;
    private List<Contact> contacts;
    private ContactAdapter adapter;
    private ListView contactsList;
    private String myInternalNumber;
    private String myName;
    private Handler handler = new Handler();
    
    // شاشات
    private LinearLayout usersScreen, qrScreen, settingsScreen;
    private TextView navUsers, navQR, navSettings;
    
    // عناصر QR
    private ImageView myQRImage;
    private TextView myQRInfo;
    
    // عناصر الإعدادات
    private TextView settingsName, settingsNumber, settingsIP;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        wifiHelper = new SmartWifiHelper(this);
        myInternalNumber = "1" + (int)(Math.random() * 100);
        myName = "جهاز " + myInternalNumber;
        
        // طلب الصلاحيات
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            }, 100);
        }
        
        // ربط العناصر
        usersScreen = findViewById(R.id.usersScreen);
        qrScreen = findViewById(R.id.qrScreen);
        settingsScreen = findViewById(R.id.settingsScreen);
        
        navUsers = findViewById(R.id.navUsers);
        navQR = findViewById(R.id.navQR);
        navSettings = findViewById(R.id.navSettings);
        
        contactsList = findViewById(R.id.contactsList);
        
        // عناصر QR
        myQRImage = findViewById(R.id.myQRImage);
        myQRInfo = findViewById(R.id.myQRInfo);
        Button btnShareQR = findViewById(R.id.btnShareQR);
        Button btnCopyQR = findViewById(R.id.btnCopyQR);
        
        // عناصر الإعدادات
        settingsName = findViewById(R.id.settingsName);
        settingsNumber = findViewById(R.id.settingsNumber);
        settingsIP = findViewById(R.id.settingsIP);
        Button btnEditInfo = findViewById(R.id.btnEditInfo);
        
        // تحديث الإعدادات
        updateSettingsInfo();
        
        // قائمة جهات الاتصال
        contacts = new ArrayList<>();
        adapter = new ContactAdapter(this, contacts, new ContactAdapter.OnCallClickListener() {
            @Override
            public void onCallClick(Contact contact) {
                startCall(contact);
            }
        });
        contactsList.setAdapter(adapter);
        
        // إضافة بعض المستخدمين للتجربة
        contacts.add(new Contact("1", "أحمد", "101", "192.168.1.101"));
        contacts.add(new Contact("2", "سارة", "102", "192.168.1.102"));
        contacts.add(new Contact("3", "محمد", "103", "192.168.1.103"));
        adapter.notifyDataSetChanged();
        
        // التنقل بين الشاشات
        navUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScreen(0);
            }
        });
        
        navQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScreen(1);
                generateMyQRCode();
            }
        });
        
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showScreen(2);
                updateSettingsInfo();
            }
        });
        
        // أزرار QR
        btnShareQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareQRCode();
            }
        });
        
        btnCopyQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyQRCode();
            }
        });
        
        // زر تعديل المعلومات
        btnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditInfoDialog();
            }
        });
        
        // إظهار الشاشة الافتراضية
        showScreen(0);
    }
    
    private void showScreen(int screenIndex) {
        // إخفاء الكل
        usersScreen.setVisibility(View.GONE);
        qrScreen.setVisibility(View.GONE);
        settingsScreen.setVisibility(View.GONE);
        
        // تغيير ألوان التنقل
        navUsers.setTextColor(getResources().getColor(R.color.text_secondary));
        navQR.setTextColor(getResources().getColor(R.color.text_secondary));
        navSettings.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // إظهار المطلوب
        switch (screenIndex) {
            case 0:
                usersScreen.setVisibility(View.VISIBLE);
                navUsers.setTextColor(getResources().getColor(R.color.primary));
                break;
            case 1:
                qrScreen.setVisibility(View.VISIBLE);
                navQR.setTextColor(getResources().getColor(R.color.primary));
                break;
            case 2:
                settingsScreen.setVisibility(View.VISIBLE);
                navSettings.setTextColor(getResources().getColor(R.color.primary));
                break;
        }
    }
    
    private void generateMyQRCode() {
        try {
            String ip = wifiHelper.getLocalIpAddress();
            String text = "CONTACT:" + myName + ":" + myInternalNumber + ":" + ip;
            
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 400, 400);
            
            Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);
            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 400; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            myQRImage.setImageBitmap(bitmap);
            myQRInfo.setText(myName + " | " + myInternalNumber + " | " + ip);
            
        } catch (WriterException e) {
            Toast.makeText(this, "خطأ في توليد QR", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareQRCode() {
        String ip = wifiHelper.getLocalIpAddress();
        String text = "CONTACT:" + myName + ":" + myInternalNumber + ":" + ip;
        
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "مشاركة رمز التعريف"));
    }
    
    private void copyQRCode() {
        String ip = wifiHelper.getLocalIpAddress();
        String text = "CONTACT:" + myName + ":" + myInternalNumber + ":" + ip;
        
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = 
            android.content.ClipData.newPlainText("QR Code", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "تم النسخ", Toast.LENGTH_SHORT).show();
    }
    
    private void updateSettingsInfo() {
        settingsName.setText(myName);
        settingsNumber.setText(myInternalNumber);
        settingsIP.setText(wifiHelper.getLocalIpAddress());
    }
    
    private void showEditInfoDialog() {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 30, 50, 30);
        
        final EditText etName = new EditText(this);
        etName.setHint("الاسم");
        etName.setText(myName);
        etName.setTextColor(Color.BLACK);
        etName.setPadding(20, 10, 20, 10);
        dialogLayout.addView(etName);
        
        final EditText etNumber = new EditText(this);
        etNumber.setHint("الرقم الداخلي");
        etNumber.setText(myInternalNumber);
        etNumber.setTextColor(Color.BLACK);
        etNumber.setPadding(20, 10, 20, 10);
        dialogLayout.addView(etNumber);
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("تعديل معلوماتي")
            .setView(dialogLayout)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    private void startCall(Contact contact) {
        Toast.makeText(this, "جاري الاتصال بـ " + contact.getName(), Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(MainActivity.this, IncomingCallActivity.class);
        intent.putExtra("caller_name", contact.getName());
        intent.putExtra("caller_number", contact.getInternalNumber());
        intent.putExtra("caller_ip", contact.getIpAddress());
        startActivity(intent);
    }
}
