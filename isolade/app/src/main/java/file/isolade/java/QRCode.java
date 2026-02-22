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

/**
 * Gère les appels WebRTC pour l'application DronTalk.
 * Cette classe initialise WebRTC, gère la caméra, le microphone et l'établissement des connexions.
 */
public class WebRTC {

    private Activity activite;
    private PeerConnectionFactory usineDeConnexion;
    private VideoCapturer captureVideo;
    private VideoSource sourceVideo;
    private VideoTrack pisteVideoLocale;
    private AudioSource sourceAudio;
    private AudioTrack pisteAudioLocale;
    private PeerConnection connexionPair;
    private EglBase baseEgl;
    private boolean estEnAppel = false;

    private static final String TAG = "WebRTC";

    /**
     * Constructeur de la classe WebRTC.
     * @param activite L'activité Android qui utilise ce gestionnaire.
     */
    public WebRTC(Activity activite) {
        this.activite = activite;
        initialiser();
    }

    /**
     * Initialise le moteur WebRTC.
     */
    private void initialiser() {
        try {
            // 1. Création du contexte EGL pour le rendu vidéo
            baseEgl = EglBase.create();

            // 2. Initialisation globale de WebRTC
            PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(activite)
                            .setEnableInternalTracer(true)
                            .createInitializationOptions()
            );

            // 3. Configuration des encodeurs/décodeurs vidéo
            VideoEncoderFactory usineEncodeur = new DefaultVideoEncoderFactory(
                    baseEgl.getEglBaseContext(), true, true);
            VideoDecoderFactory usineDecodeur = new DefaultVideoDecoderFactory(
                    baseEgl.getEglBaseContext());

            // 4. Création de la fabrique de connexions
            usineDeConnexion = PeerConnectionFactory.builder()
                    .setVideoEncoderFactory(usineEncodeur)
                    .setVideoDecoderFactory(usineDecodeur)
                    .createPeerConnectionFactory();

            Log.d(TAG, "✅ Moteur WebRTC initialisé avec succès");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors de l'initialisation: " + e.getMessage());
            Toast.makeText(activite, "Erreur WebRTC: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Démarre un appel vers une adresse IP cible.
     * @param ipCible L'adresse IP du destinataire.
     */
    public void demarrerAppel(String ipCible) {
        estEnAppel = true;
        Log.d(TAG, "📞 Démarrage de l'appel vers " + ipCible);
        Toast.makeText(activite, "📞 Appel vers " + ipCible, Toast.LENGTH_SHORT).show();

        // Création des flux audio/vidéo
        creerCaptureVideo();
        creerPisteAudio();
        creerConnexionPair();
    }

    /**
     * Termine l'appel en cours.
     */
    public void terminerAppel() {
        estEnAppel = false;
        Log.d(TAG, "🔴 Appel terminé");
        Toast.makeText(activite, "🔴 Appel terminé", Toast.LENGTH_SHORT).show();

        if (connexionPair != null) {
            connexionPair.close();
            connexionPair = null;
        }

        if (captureVideo != null) {
            try {
                captureVideo.stopCapture();
                captureVideo.dispose();
            } catch (InterruptedException e) {
                Log.e(TAG, "Erreur arrêt capture: " + e.getMessage());
            }
        }
    }

    /**
     * Crée le capteur vidéo (caméra avant par défaut).
     */
    private void creerCaptureVideo() {
        try {
            CameraEnumerator enumerateur = new Camera1Enumerator(true);
            String[] peripheriques = enumerateur.getDeviceNames();

            // Recherche de la caméra avant
            for (String nom : peripheriques) {
                if (enumerateur.isFrontFacing(nom)) {
                    captureVideo = enumerateur.createCapturer(nom, null);
                    Log.d(TAG, "📷 Caméra avant trouvée: " + nom);
                    break;
                }
            }

            // Si aucune caméra avant, prend la première disponible
            if (captureVideo == null && peripheriques.length > 0) {
                captureVideo = enumerateur.createCapturer(peripheriques[0], null);
                Log.d(TAG, "📷 Caméra arrière utilisée: " + peripheriques[0]);
            }

            if (captureVideo != null) {
                sourceVideo = usineDeConnexion.createVideoSource(captureVideo.isScreencast());
                captureVideo.initialize(null, null, sourceVideo.getCapturerObserver());
                captureVideo.startCapture(1280, 720, 30);

                pisteVideoLocale = usineDeConnexion.createVideoTrack("video", sourceVideo);
                pisteVideoLocale.setEnabled(true);
                Log.d(TAG, "✅ Flux vidéo créé");
            } else {
                Log.e(TAG, "❌ Aucune caméra trouvée");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur création vidéo: " + e.getMessage());
        }
    }

    /**
     * Crée la piste audio.
     */
    private void creerPisteAudio() {
        try {
            MediaConstraints contraintesAudio = new MediaConstraints();
            sourceAudio = usineDeConnexion.createAudioSource(contraintesAudio);
            pisteAudioLocale = usineDeConnexion.createAudioTrack("audio", sourceAudio);
            pisteAudioLocale.setEnabled(true);
            Log.d(TAG, "✅ Flux audio créé");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur création audio: " + e.getMessage());
        }
    }

    /**
     * Crée et configure la connexion pair-à-pair.
     */
    private void creerConnexionPair() {
        try {
            // Configuration des serveurs STUN (pour le NAT traversal)
            List<PeerConnection.IceServer> serveursIce = new ArrayList<>();
            serveursIce.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
            serveursIce.add(PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer());

            PeerConnection.RTCConfiguration configRtc = new PeerConnection.RTCConfiguration(serveursIce);

            // Création de la connexion avec un observateur
            connexionPair = usineDeConnexion.createPeerConnection(configRtc, new PeerConnection.Observer() {
                @Override
                public void onSignalingChange(PeerConnection.SignalingState etat) {
                    Log.d(TAG, "État signalisation: " + etat);
                }

                @Override
                public void onIceConnectionChange(PeerConnection.IceConnectionState etat) {
                    Log.d(TAG, "État ICE: " + etat);
                    if (etat == PeerConnection.IceConnectionState.CONNECTED) {
                        activite.runOnUiThread(() ->
                                Toast.makeText(activite, "✅ Connecté", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onIceConnectionReceivingChange(boolean recu) {
                    Log.d(TAG, "Réception ICE: " + recu);
                }

                @Override
                public void onIceGatheringChange(PeerConnection.IceGatheringState etat) {
                    Log.d(TAG, "Collecte ICE: " + etat);
                }

                @Override
                public void onIceCandidate(IceCandidate candidat) {
                    Log.d(TAG, "Candidat ICE trouvé: " + candidat.sdp);
                    // Ici, vous enverriez le candidat à l'autre pair via un serveur de signalisation
                }

                @Override
                public void onIceCandidatesRemoved(IceCandidate[] candidats) {
                    Log.d(TAG, "Candidats ICE supprimés");
                }

                @Override
                public void onAddStream(MediaStream flux) {
                    Log.d(TAG, "Flux distant ajouté");
                    // Le flux distant (vidéo de l'autre personne) est disponible
                }

                @Override
                public void onRemoveStream(MediaStream flux) {
                    Log.d(TAG, "Flux distant supprimé");
                }

                @Override
                public void onDataChannel(org.webrtc.DataChannel canal) {
                    Log.d(TAG, "Canal de données créé");
                }

                @Override
                public void onRenegotiationNeeded() {
                    Log.d(TAG, "Renégociation nécessaire");
                }

                @Override
                public void onAddTrack(org.webrtc.RtpReceiver recepteur, org.webrtc.MediaStream[] flux) {
                    Log.d(TAG, "Piste ajoutée");
                }
            });

            if (connexionPair != null) {
                // Ajout des flux locaux à la connexion
                MediaStream fluxLocal = usineDeConnexion.createLocalMediaStream("flux_local");
                if (pisteVideoLocale != null) fluxLocal.addTrack(pisteVideoLocale);
                if (pisteAudioLocale != null) fluxLocal.addTrack(pisteAudioLocale);
                connexionPair.addStream(fluxLocal);

                Log.d(TAG, "✅ Connexion pair créée");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur création connexion: " + e.getMessage());
        }
    }

    /**
     * Vérifie si un appel est en cours.
     * @return true si un appel est actif, false sinon.
     */
    public boolean estEnAppel() {
        return estEnAppel;
    }

    /**
     * Active/désactive le microphone.
     * @param coupe true pour couper le micro, false pour le réactiver.
     */
    public void couperMicro(boolean coupe) {
        if (pisteAudioLocale != null) {
            pisteAudioLocale.setEnabled(!coupe);
            Toast.makeText(activite, coupe ? "🔇 Micro coupé" : "🎤 Micro activé", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Active/désactive le haut-parleur.
     * @param actif true pour activer le haut-parleur, false pour l'écouteur.
     */
    public void activerHautParleur(boolean actif) {
        android.media.AudioManager gestionnaireAudio =
                (android.media.AudioManager) activite.getSystemService(activite.AUDIO_SERVICE);
        if (gestionnaireAudio != null) {
            gestionnaireAudio.setSpeakerphoneOn(actif);
            Toast.makeText(activite, actif ? "🔊 Haut-parleur activé" : "📞 Écouteur activé", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Bascule entre la caméra avant et arrière.
     */
    public void basculerCamera() {
        if (captureVideo instanceof CameraVideoCapturer) {
            ((CameraVideoCapturer) captureVideo).switchCamera(null);
            Toast.makeText(activite, "🔄 Caméra basculée", Toast.LENGTH_SHORT).show();
        }
    }
}