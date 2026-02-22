package com.example.livecamcall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class QRScanActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean scanning = false;
    private TextView tvInfo;
    private EditText etManualCode;
    private FrameLayout previewContainer;
    private Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        
        previewContainer = findViewById(R.id.qrContainer);
        tvInfo = findViewById(R.id.tvQRInfo);
        etManualCode = findViewById(R.id.etManualCode);
        Button btnPrimary = findViewById(R.id.btnQRPrimary);
        Button btnSecondary = findViewById(R.id.btnQRSecondary);
        Button btnUseManual = findViewById(R.id.btnUseManual);
        Button btnClose = findViewById(R.id.btnCloseQR);
        TextView title = findViewById(R.id.tvQRTitle);
        
        title.setText("مسح QR");
        tvInfo.setText("ضع رمز QR أمام الكاميرا");
        btnPrimary.setText("بدء المسح");
        btnSecondary.setText("إيقاف");
        
        // إعداد معاينة الكاميرا
        surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        previewContainer.addView(surfaceView, params);
        
        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning();
            }
        });
        
        btnSecondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanning();
            }
        });
        
        btnUseManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = etManualCode.getText().toString();
                if (!code.isEmpty()) {
                    Intent result = new Intent();
                    result.putExtra("qr_code", code);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Toast.makeText(QRScanActivity.this, "أدخل الرمز", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void startScanning() {
        scanning = true;
        tvInfo.setText("جاري المسح...");
    }
    
    private void stopScanning() {
        scanning = false;
        tvInfo.setText("المسح متوقف");
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(parameters);
            
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "لا يمكن الوصول للكاميرا", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!scanning) return;
        
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                data, size.width, size.height, 0, 0, size.width, size.height, false);
            
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            
            if (result != null) {
                scanning = false;
                final String qrText = result.getText(); // ✅ متغير final هنا
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvInfo.setText("تم المسح بنجاح!");
                        Toast.makeText(QRScanActivity.this, "تم المسح", Toast.LENGTH_SHORT).show();
                        
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("qr_code", qrText); // ✅ استخدام المتغير final
                        setResult(RESULT_OK, resultIntent);
                        
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                });
            }
        } catch (Exception e) {
            // لا يوجد رمز في الإطار
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
