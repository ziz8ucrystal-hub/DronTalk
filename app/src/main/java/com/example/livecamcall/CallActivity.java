package com.example.livecamcall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallActivity extends Activity {
    
    private String mode;
    private String contactName;
    private String contactNumber;
    private String contactIp;
    private boolean isMicMuted = false;
    private boolean isSpeakerOn = false;
    private boolean isCameraOn = true;
    private Handler handler = new Handler();
    private int seconds = 0;
    private Runnable timerRunnable;
    private TextView timerView;
    private LinearLayout localVideoContainer;
    private TextView tvLocalVideo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mode = getIntent().getStringExtra("mode");
        contactName = getIntent().getStringExtra("contact_name");
        contactNumber = getIntent().getStringExtra("contact_number");
        contactIp = getIntent().getStringExtra("contact_ip");
        
        if (contactName == null) contactName = "المستخدم";
        if (contactNumber == null) contactNumber = "غير معروف";
        if (contactIp == null) contactIp = "192.168.1.100";
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#121212"));
        
        // video container
        LinearLayout videoContainer = new LinearLayout(this);
        videoContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 3));
        videoContainer.setBackgroundColor(Color.parseColor("#2C3E50"));
        videoContainer.setGravity(android.view.Gravity.CENTER);
        
        TextView tvRemoteVideo = new TextView(this);
        tvRemoteVideo.setText("📹 " + contactName);
        tvRemoteVideo.setTextColor(Color.WHITE);
        tvRemoteVideo.setTextSize(24);
        videoContainer.addView(tvRemoteVideo);
        mainLayout.addView(videoContainer);
        
        // local video container
        localVideoContainer = new LinearLayout(this);
        localVideoContainer.setLayoutParams(new LinearLayout.LayoutParams(
            200, 300));
        localVideoContainer.setBackgroundColor(Color.parseColor("#1E2A3A"));
        localVideoContainer.setGravity(android.view.Gravity.CENTER);
        
        tvLocalVideo = new TextView(this);
        tvLocalVideo.setText("📹");
        tvLocalVideo.setTextColor(Color.WHITE);
        tvLocalVideo.setTextSize(18);
        localVideoContainer.addView(tvLocalVideo);
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            200, 300);
        params.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        params.topMargin = 20;
        params.rightMargin = 20;
        
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.addView(mainLayout);
        rootLayout.addView(localVideoContainer, params);
        
        // info layout
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setPadding(0, 20, 0, 20);
        infoLayout.setBackgroundColor(Color.parseColor("#1E1E1E"));
        
        TextView tvContactName = new TextView(this);
        tvContactName.setText(contactName);
        tvContactName.setTextColor(Color.WHITE);
        tvContactName.setTextSize(22);
        tvContactName.setGravity(android.view.Gravity.CENTER);
        infoLayout.addView(tvContactName);
        
        TextView tvContactInfo = new TextView(this);
        tvContactInfo.setText("رقم داخلي: " + contactNumber + " | " + contactIp);
        tvContactInfo.setTextColor(Color.parseColor("#B3B3B3"));
        tvContactInfo.setTextSize(14);
        tvContactInfo.setGravity(android.view.Gravity.CENTER);
        infoLayout.addView(tvContactInfo);
        
        timerView = new TextView(this);
        timerView.setText("00:00");
        timerView.setTextColor(Color.parseColor("#BB86FC"));
        timerView.setTextSize(18);
        timerView.setGravity(android.view.Gravity.CENTER);
        timerView.setPadding(0, 10, 0, 20);
        infoLayout.addView(timerView);
        
        mainLayout.addView(infoLayout);
        
        // controls layout
        LinearLayout controlsLayout = new LinearLayout(this);
        controlsLayout.setOrientation(LinearLayout.HORIZONTAL);
        controlsLayout.setPadding(20, 20, 20, 40);
        controlsLayout.setBackgroundColor(Color.parseColor("#121212"));
        
        final Button btnMute = createControlButton("🎤", "#2C3E50");
        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMicMuted = !isMicMuted;
                btnMute.setBackgroundColor(Color.parseColor(isMicMuted ? "#E74C3C" : "#2C3E50"));
                Toast.makeText(CallActivity.this, 
                    isMicMuted ? "الميكروفون مكتوم" : "الميكروفون نشط", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        controlsLayout.addView(btnMute);
        
        Button btnEnd = createControlButton("❌", "#E74C3C");
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
        controlsLayout.addView(btnEnd);
        
        final Button btnSpeaker = createControlButton("🔊", "#2C3E50");
        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSpeakerOn = !isSpeakerOn;
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.setSpeakerphoneOn(isSpeakerOn);
                }
                btnSpeaker.setBackgroundColor(Color.parseColor(isSpeakerOn ? "#BB86FC" : "#2C3E50"));
                Toast.makeText(CallActivity.this, 
                    isSpeakerOn ? "مكبر الصوت نشط" : "مكبر الصوت متوقف", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        controlsLayout.addView(btnSpeaker);
        
        final Button btnCamera = createControlButton("📷", "#2C3E50");
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCameraOn = !isCameraOn;
                btnCamera.setBackgroundColor(Color.parseColor(isCameraOn ? "#2C3E50" : "#E74C3C"));
                localVideoContainer.setBackgroundColor(Color.parseColor(isCameraOn ? "#1E2A3A" : "#000000"));
                tvLocalVideo.setText(isCameraOn ? "📹" : "🚫");
                Toast.makeText(CallActivity.this, 
                    isCameraOn ? "الكاميرا نشطة" : "الكاميرا متوقفة", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        controlsLayout.addView(btnCamera);
        
        mainLayout.addView(controlsLayout);
        
        setContentView(rootLayout);
        
        startTimer();
        
        if ("offer".equals(mode)) {
            Toast.makeText(this, "جاري الاتصال بـ " + contactName + "...", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "تم الرد على المكالمة من " + contactName, Toast.LENGTH_LONG).show();
        }
    }
    
    private Button createControlButton(String text, String colorHex) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0, 80, 1);
        params.setMargins(5, 0, 5, 0);
        button.setLayoutParams(params);
        button.setText(text);
        button.setTextSize(24);
        button.setBackgroundColor(Color.parseColor(colorHex));
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        return button;
    }
    
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                int minutes = seconds / 60;
                int secs = seconds % 60;
                timerView.setText(String.format("%02d:%02d", minutes, secs));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }
    
    private void endCall() {
        Toast.makeText(this, "تم إنهاء المكالمة", Toast.LENGTH_SHORT).show();
        handler.removeCallbacks(timerRunnable);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
    }
}