package com.example.livecamcall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IncomingCallActivity extends Activity {
    
    private Ringtone ringtone;
    private Handler handler = new Handler();
    private int seconds = 0;
    private Runnable timerRunnable;
    private TextView timerView;
    private String callerName, callerNumber, callerIp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Full Screen Dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        setContentView(R.layout.activity_incoming);
        
        callerName = getIntent().getStringExtra("caller_name");
        callerNumber = getIntent().getStringExtra("caller_number");
        callerIp = getIntent().getStringExtra("caller_ip");
        
        if (callerName == null) callerName = "شخص ما";
        if (callerNumber == null) callerNumber = "غير معروف";
        if (callerIp == null) callerIp = "192.168.1.100";
        
        TextView tvName = findViewById(R.id.tvCallerName);
        TextView tvNumber = findViewById(R.id.tvCallerNumber);
        timerView = findViewById(R.id.tvTimer);
        Button btnAccept = findViewById(R.id.btnAccept);
        Button btnReject = findViewById(R.id.btnReject);
        
        tvName.setText(callerName);
        tvNumber.setText("رقم داخلي: " + callerNumber);
        
        playRingtone();
        startTimer();
        
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRingtone();
                Toast.makeText(IncomingCallActivity.this, "جاري الرد...", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(IncomingCallActivity.this, CallActivity.class);
                intent.putExtra("mode", "answer");
                intent.putExtra("contact_name", callerName);
                intent.putExtra("contact_number", callerNumber);
                intent.putExtra("contact_ip", callerIp);
                startActivity(intent);
                finish();
            }
        });
        
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRingtone();
                Toast.makeText(IncomingCallActivity.this, "تم رفض المكالمة", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void playRingtone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
    
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                int minutes = seconds / 60;
                int secs = seconds % 60;
                timerView.setText(String.format("⏱️ %02d:%02d", minutes, secs));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
        handler.removeCallbacks(timerRunnable);
    }
}