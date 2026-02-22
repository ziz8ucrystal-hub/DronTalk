package file.isolade.java;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTC {
    
    private Activity activity;
    private PeerConnectionFactory factory;
    private VideoCapturer videoCapturer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private AudioSource audioSource;
    private AudioTrack localAudioTrack;
    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    
    private static final String TAG = "WebRTC";
    
    public WebRTC(Activity activity) {
        this.activity = activity;
        initialize();
    }
    
    private void initialize() {
        try {
            rootEglBase = EglBase.create();
            
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(activity)
                    .setEnableInternalTracer(true)
                    .createInitializationOptions()
            );
            
            VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true, true);
            VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(
                rootEglBase.getEglBaseContext());
            
            factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
            
            Log.d(TAG, "WebRTC initialized");
            
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
    
    public void startCall(String targetIp) {
        Toast.makeText(activity, "الاتصال بـ " + targetIp, Toast.LENGTH_SHORT).show();
        createVideoCapturer();
        createAudioTrack();
        createPeerConnection();
    }
    
    private void createVideoCapturer() {
        try {
            CameraEnumerator enumerator = new Camera1Enumerator(true);
            String[] devices = enumerator.getDeviceNames();
            
            for (String name : devices) {
                if (enumerator.isFrontFacing(name)) {
                    videoCapturer = enumerator.createCapturer(name, null);
                    break;
                }
            }
            
            if (videoCapturer == null && devices.length > 0) {
                videoCapturer = enumerator.createCapturer(devices[0], null);
            }
            
            if (videoCapturer != null) {
                videoSource = factory.createVideoSource(videoCapturer.isScreencast());
                videoCapturer.initialize(null, null, videoSource.getCapturerObserver());
                videoCapturer.startCapture(1280, 720, 30);
                
                localVideoTrack = factory.createVideoTrack("video", videoSource);
                localVideoTrack.setEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
    
    private void createAudioTrack() {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("audio", audioSource);
        localAudioTrack.setEnabled(true);
    }
    
    private void createPeerConnection() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        
        peerConnection = factory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState state) {}
            
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
                if (state == PeerConnection.IceConnectionState.CONNECTED) {
                    activity.runOnUiThread(() -> 
                        Toast.makeText(activity, "تم الاتصال", Toast.LENGTH_SHORT).show());
                }
            }
            
            @Override
            public void onIceConnectionReceivingChange(boolean b) {}
            
            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState state) {}
            
            @Override
            public void onIceCandidate(IceCandidate candidate) {}
            
            @Override
            public void onIceCandidatesRemoved(IceCandidate[] candidates) {}
            
            @Override
            public void onAddStream(MediaStream stream) {}
            
            @Override
            public void onRemoveStream(MediaStream stream) {}
            
            @Override
            public void onDataChannel(org.webrtc.DataChannel channel) {}
            
            @Override
            public void onRenegotiationNeeded() {}
            
            @Override
            public void onAddTrack(org.webrtc.RtpReceiver receiver, org.webrtc.MediaStream[] streams) {}
        });
        
        if (peerConnection != null) {
            MediaStream stream = factory.createLocalMediaStream("stream");
            if (localVideoTrack != null) stream.addTrack(localVideoTrack);
            if (localAudioTrack != null) stream.addTrack(localAudioTrack);
            peerConnection.addStream(stream);
        }
    }
    
    public void endCall() {
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}