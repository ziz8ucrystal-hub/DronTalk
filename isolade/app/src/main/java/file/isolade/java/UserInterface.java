package file.isolade.java;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserInterface extends Activity {
    
    private QRCode qrCode;
    private WebRTC webRTC;
    private Handler handler = new Handler();
    private int callSeconds = 0;
    private TextView timerView;
    private LinearLayout mainContainer;
    private boolean isInCall = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Full Screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        requestPermissions();
        
        // Initialisation des classes
        qrCode = new QRCode(this);
        webRTC = new WebRTC(this);
        
        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setBackgroundColor(Color.WHITE);
        mainContainer.setPadding(40, 60, 40, 40);
        
        setContentView(mainContainer);
        
        showMainUI();
    }
    
    private GradientDrawable createButtonBackground(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(25);
        return drawable;
    }
    
    private void showMainUI() {
        mainContainer.removeAllViews();
        mainContainer.setBackgroundColor(Color.WHITE);
        
        // Titre
        TextView title = new TextView(this);
        title.setText("DronTalk");
        title.setTextSize(36);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        mainContainer.addView(title);
        
        // Statut
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setPadding(30, 20, 30, 20);
        statusCard.setBackground(createButtonBackground("#F5F5F5"));
        
        TextView statusText = new TextView(this);
        statusText.setText("● Connecté");
        statusText.setTextColor(Color.parseColor("#4CAF50"));
        statusText.setTextSize(14);
        statusCard.addView(statusText);
        mainContainer.addView(statusCard);
        
        // Bouton Scanner QR
        Button btnScan = new Button(this);
        btnScan.setText("📷 Scanner QR Code");
        btnScan.setTextColor(Color.WHITE);
        btnScan.setTextSize(16);
        btnScan.setTypeface(null, Typeface.BOLD);
        btnScan.setPadding(30, 20, 30, 20);
        btnScan.setBackground(createButtonBackground("#6200EE"));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);
        btnScan.setLayoutParams(params);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserInterface.this, "📷 Ouverture de la caméra", Toast.LENGTH_SHORT).show();
                qrCode.startScan();
            }
        });
        mainContainer.addView(btnScan);
        
        // Bouton Mon QR Code
        Button btnMyQR = new Button(this);
        btnMyQR.setText("🔳 Mon QR Code");
        btnMyQR.setTextColor(Color.WHITE);
        btnMyQR.setTextSize(16);
        btnMyQR.setTypeface(null, Typeface.BOLD);
        btnMyQR.setPadding(30, 20, 30, 20);
        btnMyQR.setBackground(createButtonBackground("#000000"));
        btnMyQR.setLayoutParams(params);
        btnMyQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrCode.showMyQR();
            }
        });
        mainContainer.addView(btnMyQR);
        
        // Bouton Nouvel Appel
        Button btnCall = new Button(this);
        btnCall.setText("📞 Nouvel Appel");
        btnCall.setTextColor(Color.WHITE);
        btnCall.setTextSize(16);
        btnCall.setTypeface(null, Typeface.BOLD);
        btnCall.setPadding(30, 20, 30, 20);
        btnCall.setBackground(createButtonBackground("#6200EE"));
        btnCall.setLayoutParams(params);
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCallUI();
                webRTC.demarrerAppel("192.168.1.100");
            }
        });
        mainContainer.addView(btnCall);
    }
    
    private void showCallUI() {
        isInCall = true;
        mainContainer.removeAllViews();
        mainContainer.setBackgroundColor(Color.BLACK);
        
        // Vidéo distante (placeholder)
        FrameLayout remoteVideo = new FrameLayout(this);
        remoteVideo.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 600));
        remoteVideo.setBackgroundColor(Color.parseColor("#1A1A1A"));
        
        TextView remoteText = new TextView(this);
        remoteText.setText("📹 Flux vidéo distant");
        remoteText.setTextColor(Color.WHITE);
        remoteText.setTextSize(18);
        remoteText.setGravity(Gravity.CENTER);
        remoteVideo.addView(remoteText);
        mainContainer.addView(remoteVideo);
        
        // Informations d'appel
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setPadding(40, 30, 40, 30);
        
        TextView contactName = new TextView(this);
        contactName.setText("📱 Appel en cours");
        contactName.setTextColor(Color.WHITE);
        contactName.setTextSize(24);
        contactName.setTypeface(null, Typeface.BOLD);
        contactName.setGravity(Gravity.CENTER);
        infoLayout.addView(contactName);
        
        timerView = new TextView(this);
        timerView.setText("00:00");
        timerView.setTextColor(Color.WHITE);
        timerView.setTextSize(20);
        timerView.setGravity(Gravity.CENTER);
        infoLayout.addView(timerView);
        
        // Contrôles d'appel
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setPadding(20, 20, 20, 40);
        
        Button btnMute = new Button(this);
        btnMute.setText("🔇");
        btnMute.setBackground(createButtonBackground("#333333"));
        btnMute.setTextColor(Color.WHITE);
        btnMute.setLayoutParams(new LinearLayout.LayoutParams(0, 80, 1));
        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webRTC.couperMicro(true);
            }
        });
        controls.addView(btnMute);
        
        Button btnEnd = new Button(this);
        btnEnd.setText("🔴 Fin");
        btnEnd.setBackground(createButtonBackground("#E74C3C"));
        btnEnd.setTextColor(Color.WHITE);
        btnEnd.setLayoutParams(new LinearLayout.LayoutParams(0, 80, 1));
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webRTC.terminerAppel();
                isInCall = false;
                showMainUI();
            }
        });
        controls.addView(btnEnd);
        
        Button btnSpeaker = new Button(this);
        btnSpeaker.setText("🔊");
        btnSpeaker.setBackground(createButtonBackground("#333333"));
        btnSpeaker.setTextColor(Color.WHITE);
        btnSpeaker.setLayoutParams(new LinearLayout.LayoutParams(0, 80, 1));
        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webRTC.activerHautParleur(true);
            }
        });
        controls.addView(btnSpeaker);
        
        infoLayout.addView(controls);
        mainContainer.addView(infoLayout);
        
        // Vidéo locale (miniature)
        FrameLayout localVideo = new FrameLayout(this);
        FrameLayout.LayoutParams localParams = new FrameLayout.LayoutParams(150, 200);
        localParams.gravity = Gravity.TOP | Gravity.END;
        localParams.topMargin = 20;
        localParams.rightMargin = 20;
        localVideo.setLayoutParams(localParams);
        localVideo.setBackgroundColor(Color.parseColor("#333333"));
        
        TextView localText = new TextView(this);
        localText.setText("Vous");
        localText.setTextColor(Color.WHITE);
        localText.setTextSize(12);
        localText.setGravity(Gravity.CENTER);
        localVideo.addView(localText);
        
        FrameLayout root = new FrameLayout(this);
        root.addView(mainContainer);
        root.addView(localVideo);
        setContentView(root);
        
        startCallTimer();
    }
    
    private void startCallTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInCall) {
                    callSeconds++;
                    int minutes = callSeconds / 60;
                    int secs = callSeconds % 60;
                    timerView.setText(String.format("%02d:%02d", minutes, secs));
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }
    
    private void requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET
            }, 100);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (isInCall) {
            webRTC.terminerAppel();
        }
    }
}
