package com.example.livecamcall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRGenerateActivity extends Activity {
    
    private ImageView qrImage;
    private String myName, myNumber, myIP;
    private SmartWifiHelper wifiHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        
        wifiHelper = new SmartWifiHelper(this);
        
        myName = getIntent().getStringExtra("my_name");
        myNumber = getIntent().getStringExtra("my_number");
        myIP = wifiHelper.getLocalIpAddress();
        
        FrameLayout container = findViewById(R.id.qrContainer);
        TextView tvInfo = findViewById(R.id.tvQRInfo);
        EditText etManual = findViewById(R.id.etManualCode);
        Button btnPrimary = findViewById(R.id.btnQRPrimary);
        Button btnSecondary = findViewById(R.id.btnQRSecondary);
        Button btnUseManual = findViewById(R.id.btnUseManual);
        Button btnClose = findViewById(R.id.btnCloseQR);
        TextView title = findViewById(R.id.tvQRTitle);
        
        title.setText("رمز التعريف الخاص بي");
        tvInfo.setText(myName + " | " + myNumber);
        btnPrimary.setText("مشاركة الرمز");
        btnSecondary.setText("نسخ الرمز");
        btnUseManual.setVisibility(View.GONE);
        etManual.setVisibility(View.GONE);
        
        // إنشاء صورة QR
        qrImage = new ImageView(this);
        qrImage.setPadding(20, 20, 20, 20);
        qrImage.setBackgroundColor(Color.WHITE);
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        container.addView(qrImage, params);
        
        generateQRCode();
        
        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrText = "CONTACT:" + myName + ":" + myNumber + ":" + myIP;
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, qrText);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "مشاركة رمز التعريف"));
            }
        });
        
        btnSecondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrText = "CONTACT:" + myName + ":" + myNumber + ":" + myIP;
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = 
                    android.content.ClipData.newPlainText("QR Code", qrText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(QRGenerateActivity.this, "تم النسخ", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void generateQRCode() {
        try {
            String text = "CONTACT:" + myName + ":" + myNumber + ":" + myIP;
            
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 400, 400);
            
            Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);
            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 400; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            qrImage.setImageBitmap(bitmap);
            
        } catch (WriterException e) {
            Toast.makeText(this, "خطأ في التوليد", Toast.LENGTH_SHORT).show();
        }
    }
}