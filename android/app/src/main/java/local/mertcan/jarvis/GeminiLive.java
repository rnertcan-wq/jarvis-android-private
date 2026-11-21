package local.mertcan.jarvis;

import android.R;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
public final class GeminiLive extends Service {
    private static final String CHANNEL = "gemini-live";
    private static final int NOTICE = 84;
    private static final String STOP = "local.mertcan.jarvis.STOP_LIVE";
    static volatile boolean active;
    private Thread input;
    private boolean onDeviceWake;
    private Thread output;
    private volatile boolean ready;
    private volatile boolean running;
    private volatile WebSocket socket;
    private boolean useDefaultRecognizer;
    private int wakeErrors;
    private SpeechRecognizer wakeRecognizer;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ArrayBlockingQueue<byte[]> playback = new ArrayBlockingQueue<>(120);
    private final ExecutorService tools = Executors.newSingleThreadExecutor();
    private final StringBuilder transcript = new StringBuilder();
    private String openingWords = "";
    private final OkHttpClient client = new OkHttpClient.Builder().pingInterval(25, TimeUnit.SECONDS).build();

    /* JADX INFO: renamed from: $r8$lambda$dyZaifW_-oxJzsGCZ0wtztkQvLg, reason: not valid java name */
    public static /* synthetic */ void m27$r8$lambda$dyZaifW_oxJzsGCZ0wtztkQvLg(GeminiLive geminiLive) {
        geminiLive.waitForWake();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    static /* synthetic */ boolean access$100(GeminiLive geminiLive) {
        return geminiLive.running;
    }

    static /* synthetic */ void access$1000(GeminiLive geminiLive, String str) {
        geminiLive.wake(str);
    }

    static /* synthetic */ void access$1100(GeminiLive geminiLive) {
        geminiLive.waitForWake();
    }

    static /* synthetic */ void access$1300(GeminiLive geminiLive, WebSocket webSocket, String str) {
        geminiLive.sessionError(webSocket, str);
    }

    static /* synthetic */ void access$1400(GeminiLive geminiLive, WebSocket webSocket, String str) {
        geminiLive.receive(webSocket, str);
    }

    static /* synthetic */ boolean access$1500(GeminiLive geminiLive) {
        return geminiLive.ready;
    }

    static /* synthetic */ WebSocket access$200(GeminiLive geminiLive) {
        return geminiLive.socket;
    }

    static /* synthetic */ SpeechRecognizer access$300(GeminiLive geminiLive) {
        return geminiLive.wakeRecognizer;
    }

    static /* synthetic */ void access$400(GeminiLive geminiLive, String str) {
        geminiLive.fail(str);
    }

    static /* synthetic */ boolean access$500(GeminiLive geminiLive) {
        return geminiLive.onDeviceWake;
    }

    static /* synthetic */ boolean access$602(GeminiLive geminiLive, boolean z) {
        geminiLive.useDefaultRecognizer = z;
        return z;
    }

    static /* synthetic */ void access$700(GeminiLive geminiLive) {
        geminiLive.closeWakeRecognizer();
    }

    static /* synthetic */ Handler access$800(GeminiLive geminiLive) {
        return geminiLive.main;
    }

    static /* synthetic */ int access$900(GeminiLive geminiLive) {
        return geminiLive.wakeErrors;
    }

    static /* synthetic */ int access$902(GeminiLive geminiLive, int i) {
        geminiLive.wakeErrors = i;
        return i;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent == null || STOP.equals(intent.getAction())) {
            stopSelf();
            return 2;
        }
        if (this.running) {
            return 2;
        }
        try {
            ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel(CHANNEL, "Gemini Live", 2));
            Notification notification = notification("Google’a bağlanıyor…");
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTICE, notification, 128);
            } else {
                startForeground(NOTICE, notification);
            }
            if (Cloud.key != null && !Cloud.key.isEmpty()) {
                this.running = true;
                active = true;
                waitForWake();
                return 2;
            }
            fail("Gemini API anahtarını gir.");
            return 2;
        } catch (LinkageError | RuntimeException e) {
            fail(GeminiAccess.startupFailure(e));
            return 2;
        }
    }

    private void waitForWake() {
        Thread thread;
        if (this.running && this.socket == null) {
            Thread thread2 = this.input;
            if ((thread2 != null && thread2.isAlive()) || ((thread = this.output) != null && thread.isAlive())) {
                this.main.postDelayed(new GeminiLive$$ExternalSyntheticLambda4(this), 120L);
                return;
            }
            try {
                if (this.wakeRecognizer == null) {
                    boolean z = !this.useDefaultRecognizer && Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(this);
                    this.onDeviceWake = z;
                    if (!z && !SpeechRecognizer.isRecognitionAvailable(this)) {
                        fail("Hey Jarvis için telefonda Android ses tanıma hizmeti gerekli.");
                        return;
                    }
                    SpeechRecognizer speechRecognizerCreateOnDeviceSpeechRecognizer = this.onDeviceWake ? SpeechRecognizer.createOnDeviceSpeechRecognizer(this) : SpeechRecognizer.createSpeechRecognizer(this);
                    this.wakeRecognizer = speechRecognizerCreateOnDeviceSpeechRecognizer;
                    speechRecognizerCreateOnDeviceSpeechRecognizer.setRecognitionListener(new WakeListener());
                    state("Beklemede · Hey Jarvis");
                }
                this.wakeRecognizer.startListening(new Intent("android.speech.action.RECOGNIZE_SPEECH").putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form").putExtra("android.speech.extra.LANGUAGE", "tr-TR").putExtra("android.speech.extra.PREFER_OFFLINE", this.onDeviceWake).putExtra("android.speech.extra.MAX_RESULTS", 3));
            } catch (RuntimeException e) {
                fail("Hey Jarvis dinleyicisi açılamadı · " + e.getClass().getSimpleName());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class WakeListener implements RecognitionListener {
        @Override // android.speech.RecognitionListener
        public void onBeginningOfSpeech() {
        }

        @Override // android.speech.RecognitionListener
        public void onBufferReceived(byte[] bArr) {
        }

        @Override // android.speech.RecognitionListener
        public void onEndOfSpeech() {
        }

        @Override // android.speech.RecognitionListener
        public void onEvent(int i, Bundle bundle) {
        }

        @Override // android.speech.RecognitionListener
        public void onPartialResults(Bundle bundle) {
        }

        @Override // android.speech.RecognitionListener
        public void onReadyForSpeech(Bundle bundle) {
        }

        @Override // android.speech.RecognitionListener
        public void onRmsChanged(float f) {
        }

        private WakeListener() {
        }
@Override // android.speech.RecognitionListener
        public void onError(int i) {
            if (GeminiLive.access$100(GeminiLive.this) && GeminiLive.access$200(GeminiLive.this) == null && GeminiLive.access$300(GeminiLive.this) != null) {
                if (i == 9) {
                    GeminiLive.access$400(GeminiLive.this, "Hey Jarvis için mikrofon izni gerekli.");
                    return;
                }
                if (GeminiLive.access$500(GeminiLive.this) && (i == 12 || i == 13)) {
                    if (!SpeechRecognizer.isRecognitionAvailable(GeminiLive.this)) {
                        GeminiLive.access$400(GeminiLive.this, "Cihaz içi Türkçe ses tanıma kullanılamıyor.");
                        return;
                    }
                    GeminiLive.access$602(GeminiLive.this, true);
                    GeminiLive.access$700(GeminiLive.this);
                    GeminiLive.access$800(GeminiLive.this).postDelayed(new GeminiLive$WakeListener$$ExternalSyntheticLambda1(GeminiLive.this), 450L);
                    return;
                }
                GeminiLive geminiLive = GeminiLive.this;
                GeminiLive.access$902(geminiLive, Math.min(8, GeminiLive.access$900(geminiLive) + 1));
                GeminiLive.access$800(GeminiLive.this).postDelayed(new GeminiLive$WakeListener$$ExternalSyntheticLambda2(GeminiLive.this), Math.min(4000L, ((long) GeminiLive.access$900(GeminiLive.this)) * 250));
            }
        }

        static /* synthetic */ void lambda$onError$0(GeminiLive geminiLive) {
            GeminiLive.access$1100(geminiLive);
        }

        static /* synthetic */ void lambda$onError$1(GeminiLive geminiLive) {
            GeminiLive.access$1100(geminiLive);
        }

        @Override // android.speech.RecognitionListener
        public void onResults(Bundle bundle) {
            if (GeminiLive.access$100(GeminiLive.this) && GeminiLive.access$200(GeminiLive.this) == null && GeminiLive.access$300(GeminiLive.this) != null) {
                GeminiLive.access$902(GeminiLive.this, 0);
                ArrayList<String> stringArrayList = bundle.getStringArrayList("results_recognition");
                if (stringArrayList != null) {
                    for (String str : stringArrayList) {
                        if (str != null) {
                            Matcher matcher = Pattern.compile("(?iu)^\\s*(?:hey|ey)\\s*[, ]*\\s*(?:jarvis|carvis|carvıs|carviz|jarvıs)\\b[ ,.!?]*").matcher(str);
                            if (matcher.find()) {
                                GeminiLive.access$1000(GeminiLive.this, str.substring(matcher.end()).trim());
                                return;
                            }
                        }
                    }
                }
                GeminiLive.access$800(GeminiLive.this).postDelayed(new GeminiLive$WakeListener$$ExternalSyntheticLambda0(GeminiLive.this), 250L);
            }
        }

        static /* synthetic */ void lambda$onResults$2(GeminiLive geminiLive) {
            GeminiLive.access$1100(geminiLive);
        }
    }

    private void wake(String str) {
        closeWakeRecognizer();
        try {
            AudioManager audioManager = (AudioManager) getSystemService(AudioManager.class);
            if (audioManager != null && audioManager.isMusicActive()) {
                GeminiAccess.dispatchDeviceTool(this, "stop_music", new JSONObject());
            }
            this.openingWords = str;
            state("Hey Jarvis algılandı · bağlanıyor");
            this.socket = this.client.newWebSocket(new Request.Builder().url(GeminiAccess.liveUrl()).build(), new Listener());
            this.main.postDelayed(new GeminiLive$$ExternalSyntheticLambda0(this, this.socket), 22000L);
        } catch (RuntimeException e) {
            sleep(GeminiAccess.startupFailure(e));
        }
    }

    /* JADX INFO: renamed from: lambda$wake$0$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m35lambda$wake$0$localmertcanjarvisGeminiLive(WebSocket webSocket) {
        if (this.socket != webSocket || this.ready) {
            return;
        }
        sleep("Gemini Live yanıt vermedi.");
    }

    private void closeWakeRecognizer() {
        SpeechRecognizer speechRecognizer = this.wakeRecognizer;
        if (speechRecognizer == null) {
            return;
        }
        this.wakeRecognizer = null;
        speechRecognizer.destroy();
    }

    private void state(String str) {
        ((NotificationManager) getSystemService(NotificationManager.class)).notify(NOTICE, notification(str));
        GeminiAccess.reportStatus(str);
    }

    private void sleep(String str) {
        if (this.running) {
            WebSocket webSocket = this.socket;
            this.socket = null;
            this.ready = false;
            if (webSocket != null) {
                webSocket.cancel();
            }
            Thread thread = this.input;
            if (thread != null) {
                thread.interrupt();
            }
            Thread thread2 = this.output;
            if (thread2 != null) {
                thread2.interrupt();
            }
            this.playback.clear();
            LiveRepair.clearGeminiPlayback();
            state(str + " · Hey Jarvis bekleniyor");
            this.main.postDelayed(new GeminiLive$$ExternalSyntheticLambda4(this), 450L);
        }
    }

    /* JADX INFO: renamed from: lambda$sessionError$1$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m34lambda$sessionError$1$localmertcanjarvisGeminiLive(WebSocket webSocket, String str) {
        if (this.socket == webSocket) {
            sleep(str);
        }
    }

    private void sessionError(WebSocket webSocket, String str) {
        this.main.post(new GeminiLive$$ExternalSyntheticLambda1(this, webSocket, str));
    }

    private final class Listener extends WebSocketListener {
        private Listener() {
        }
@Override // okhttp3.WebSocketListener
        public void onOpen(WebSocket webSocket, Response response) {
            try {
                if (!GeminiLive.access$100(GeminiLive.this) || webSocket.send(GeminiAccess.setup())) {
                    return;
                }
                GeminiLive.access$1300(GeminiLive.this, webSocket, "Gemini Live açılış mesajı gönderilemedi.");
            } catch (Exception | LinkageError e) {
                GeminiLive.access$1300(GeminiLive.this, webSocket, GeminiAccess.startupFailure(e));
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onMessage(WebSocket webSocket, String str) {
            GeminiLive.access$1400(GeminiLive.this, webSocket, str);
        }

        @Override // okhttp3.WebSocketListener
        public void onMessage(WebSocket webSocket, ByteString byteString) {
            GeminiLive.access$1400(GeminiLive.this, webSocket, byteString.utf8());
        }

        @Override // okhttp3.WebSocketListener
        public void onFailure(WebSocket webSocket, Throwable th, Response response) {
            String str;
            if (GeminiLive.access$100(GeminiLive.this)) {
                GeminiLive geminiLive = GeminiLive.this;
                if (response == null) {
                    str = "Gemini Live bağlantısı kesildi · " + th.getClass().getSimpleName();
                } else {
                    str = "Gemini Live bağlantısı reddedildi · HTTP " + response.code();
                }
                GeminiLive.access$1300(geminiLive, webSocket, str);
            }
        }

        @Override // okhttp3.WebSocketListener
        public void onClosing(WebSocket webSocket, int i, String str) {
            if (GeminiLive.access$100(GeminiLive.this)) {
                GeminiLive geminiLive = GeminiLive.this;
                GeminiLive.access$1300(geminiLive, webSocket, GeminiAccess.closeMessage(i, str, GeminiLive.access$1500(geminiLive)));
            }
        }
    }

    private Notification notification(String str) {
        return new Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_btn_speak_now).setContentTitle("JARVIS · Gemini Live").setContentText(str).setOngoing(true).setOnlyAlertOnce(true).addAction(new Notification.Action.Builder((Icon) null, "Durdur", PendingIntent.getService(this, NOTICE, new Intent(this, (Class<?>) GeminiLive.class).setAction(STOP), 67108864)).build()).build();
    }

    private void receive(WebSocket webSocket, String str) {
        JSONArray jSONArrayOptJSONArray;
        if (this.running && this.socket == webSocket) {
            try {
                JSONObject jSONObject = new JSONObject(str);
                if (jSONObject.has("setupComplete") || jSONObject.has("setup_complete")) {
                    this.main.post(new GeminiLive$$ExternalSyntheticLambda7(this, webSocket));
                }
                JSONObject jSONObjectOptJSONObject = jSONObject.optJSONObject("toolCall");
                if (jSONObjectOptJSONObject != null && (jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray("functionCalls")) != null && this.running) {
                    this.tools.execute(new GeminiLive$$ExternalSyntheticLambda8(this, webSocket, jSONArrayOptJSONArray));
                }
                JSONObject jSONObjectOptJSONObject2 = jSONObject.optJSONObject("serverContent");
                if (jSONObjectOptJSONObject2 == null) {
                    return;
                }
                if (jSONObjectOptJSONObject2.optBoolean("interrupted")) {
                    this.playback.clear();
                }
                JSONObject jSONObjectOptJSONObject3 = jSONObjectOptJSONObject2.optJSONObject("inputTranscription");
                if (jSONObjectOptJSONObject3 != null && this.transcript.length() < 2048) {
                    this.transcript.append(jSONObjectOptJSONObject3.optString("text"));
                    if (sleepPhrase(this.transcript.toString())) {
                        sessionError(webSocket, "Sohbet beklemede");
                        return;
                    }
                }
                JSONObject jSONObjectOptJSONObject4 = jSONObjectOptJSONObject2.optJSONObject("modelTurn");
                JSONArray jSONArrayOptJSONArray2 = jSONObjectOptJSONObject4 == null ? null : jSONObjectOptJSONObject4.optJSONArray("parts");
                if (jSONArrayOptJSONArray2 != null) {
                    for (int i = 0; i < jSONArrayOptJSONArray2.length(); i++) {
                        JSONObject jSONObjectOptJSONObject5 = jSONArrayOptJSONArray2.getJSONObject(i).optJSONObject("inlineData");
                        if (jSONObjectOptJSONObject5 != null && jSONObjectOptJSONObject5.optString("mimeType").startsWith("audio/pcm")) {
                            byte[] bArrDecode = Base64.decode(jSONObjectOptJSONObject5.optString("data"), 0);
                            if (bArrDecode.length > 0 && !this.playback.offer(bArrDecode)) {
                                this.playback.clear();
                            }
                        }
                    }
                }
                if (jSONObjectOptJSONObject2.optBoolean("turnComplete")) {
                    LiveRepair.geminiCommand(this, this.transcript.toString());
                    this.transcript.setLength(0);
                }
            } catch (Exception | LinkageError e) {
                if (this.running) {
                    sessionError(webSocket, "Gemini Live yanıtı işlenemedi · " + e.getClass().getSimpleName());
                }
            }
        }
    }

    /* JADX INFO: renamed from: lambda$receive$2$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m31lambda$receive$2$localmertcanjarvisGeminiLive(WebSocket webSocket) {
        connectAudio(webSocket);
    }

    /* JADX INFO: renamed from: lambda$receive$3$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m32lambda$receive$3$localmertcanjarvisGeminiLive(WebSocket webSocket, JSONArray jSONArray) {
        runTools(webSocket, jSONArray);
    }

    private void connectAudio(WebSocket webSocket) {
        if (this.running && !this.ready && this.socket == webSocket) {
            this.ready = true;
            this.input = new Thread(new GeminiLive$$ExternalSyntheticLambda2(this, webSocket), "gemini-capture");
            this.output = new Thread(new GeminiLive$$ExternalSyntheticLambda3(this, webSocket), "gemini-playback");
            this.transcript.setLength(0);
            String str = this.openingWords;
            this.openingWords = "";
            if (!str.isEmpty() && !sleepPhrase(str)) {
                try {
                    webSocket.send(new JSONObject().put("realtimeInput", new JSONObject().put("text", str)).toString());
                } catch (JSONException unused) {
                }
            }
            this.input.start();
            this.output.start();
        }
    }

    /* JADX INFO: renamed from: lambda$connectAudio$4$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m28lambda$connectAudio$4$localmertcanjarvisGeminiLive(WebSocket webSocket) {
        record(webSocket);
    }

    /* JADX INFO: renamed from: lambda$connectAudio$5$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m29lambda$connectAudio$5$localmertcanjarvisGeminiLive(WebSocket webSocket) {
        play(webSocket);
    }

    /* JADX WARN: Code duplicated, block: B:13:0x003d A[PHI: r3
      0x003d: PHI (r3v8 android.media.audiofx.AcousticEchoCanceler) = 
      (r3v11 android.media.audiofx.AcousticEchoCanceler)
      (r3v5 android.media.audiofx.AcousticEchoCanceler)
      (r3v6 android.media.audiofx.AcousticEchoCanceler)
     binds: [B:8:0x0029, B:66:0x003d, B:10:0x0033] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:60:0x0110  */
    /* JADX WARN: Code duplicated, block: B:71:0x0115 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    private void record(WebSocket webSocket) {
        AudioRecord recorder = null;
        AcousticEchoCanceler echoCanceler = null;
        boolean echoEnabled = false;
        try {
            int minBuffer = AudioRecord.getMinBufferSize(16000, 16, 2);
            if (minBuffer <= 0) throw new IllegalStateException("audio format unsupported");
            recorder = new AudioRecord(6, 16000, 16, 2, Math.max(6400, minBuffer * 2));
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("microphone unavailable");
            }
            if (AcousticEchoCanceler.isAvailable()) {
                try {
                    echoCanceler = AcousticEchoCanceler.create(recorder.getAudioSessionId());
                    if (echoCanceler != null) {
                        echoCanceler.setEnabled(true);
                        echoEnabled = echoCanceler.getEnabled();
                    }
                } catch (RuntimeException ignored) {
                    echoEnabled = false;
                }
            }
            recorder.startRecording();
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                throw new IllegalStateException("microphone did not start");
            }
            this.main.post(new GeminiLive$$ExternalSyntheticLambda6(this, webSocket));
            byte[] buffer = new byte[1280];
            while (this.running && this.socket == webSocket) {
                int count = recorder.read(buffer, 0, buffer.length);
                if (count < 0) throw new IllegalStateException("microphone read failed: " + count);
                if (count == 0 || (!echoEnabled && LiveRepair.geminiPlaying())) continue;
                JSONObject audio = new JSONObject()
                    .put("mimeType", "audio/pcm;rate=16000")
                    .put("data", Base64.encodeToString(buffer, 0, count, Base64.NO_WRAP));
                if (webSocket.queueSize() > 128000 || !webSocket.send(new JSONObject().put("realtimeInput", new JSONObject().put("audio", audio)).toString())) {
                    sessionError(webSocket, "Gemini Live ses bağlantısı kesildi.");
                    break;
                }
            }
        } catch (Exception e) {
            if (this.running && this.socket == webSocket) {
                sessionError(webSocket, "Gemini Live mikrofon hatası · " + e.getClass().getSimpleName());
            }
        } finally {
            if (echoCanceler != null) {
                try { echoCanceler.release(); } catch (RuntimeException ignored) { }
            }
            if (recorder != null) {
                try {
                    if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) recorder.stop();
                } catch (RuntimeException ignored) { }
                try { recorder.release(); } catch (RuntimeException ignored) { }
            }
        }
    }

    /* JADX INFO: renamed from: lambda$record$6$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m33lambda$record$6$localmertcanjarvisGeminiLive(WebSocket webSocket) {
        if (this.running && this.socket == webSocket) {
            state("Sohbet açık · “sessiz ol” ile beklemeye al");
        }
    }

    private void play(WebSocket webSocket) {
        AudioTrack audioTrackBuild = null;
        try {
            try {
                try {
                    audioTrackBuild = new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(1).build()).setAudioFormat(new AudioFormat.Builder().setSampleRate(24000).setChannelMask(4).setEncoding(2).build()).setBufferSizeInBytes(Math.max(9600, AudioTrack.getMinBufferSize(24000, 4, 2) * 2)).setTransferMode(1).build();
                    audioTrackBuild.play();
                    while (this.running && this.socket == webSocket) {
                        byte[] bArrPoll = this.playback.poll(500L, TimeUnit.MILLISECONDS);
                        if (bArrPoll != null) {
                            LiveRepair.markGeminiPlayback(bArrPoll.length);
                            audioTrackBuild.write(bArrPoll, 0, bArrPoll.length);
                        }
                    }
                    if (audioTrackBuild == null) {
                        return;
                    }
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                    if (audioTrackBuild == null) {
                        return;
                    }
                }
            } catch (RuntimeException e) {
                if (this.running) {
                    sessionError(webSocket, "Gemini Live ses çıkışı açılamadı · " + e.getClass().getSimpleName());
                }
                if (audioTrackBuild == null) {
                    return;
                }
            }
            audioTrackBuild.release();
        } catch (Throwable th) {
            if (audioTrackBuild != null) {
                audioTrackBuild.release();
            }
            throw th;
        }
    }

    private void runTools(WebSocket webSocket, JSONArray jSONArray) {
        String strDispatchDeviceTool;
        try {
            JSONArray jSONArray2 = new JSONArray();
            for (int i = 0; this.running && this.socket == webSocket && i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                String string = jSONObject.getString("name");
                if ("sleep_live".equals(string)) {
                    sessionError(webSocket, "Sohbet beklemede");
                    return;
                }
                try {
                    strDispatchDeviceTool = GeminiAccess.dispatchDeviceTool(this, string, jSONObject);
                    if (strDispatchDeviceTool == null) {
                        JSONObject jSONObjectOptJSONObject = jSONObject.optJSONObject("args");
                        if ("research".equals(string) && jSONObjectOptJSONObject != null) {
                            strDispatchDeviceTool = Cloud.research(jSONObjectOptJSONObject.optString("query"), Cloud.key).text;
                        } else if ("music".equals(string) && jSONObjectOptJSONObject != null) {
                            strDispatchDeviceTool = Actions.run(this, jSONObjectOptJSONObject.optString("action"), jSONObjectOptJSONObject.optString("query"));
                        } else if ("news".equals(string) || "rates".equals(string)) {
                            strDispatchDeviceTool = Actions.run(this, string);
                        } else {
                            strDispatchDeviceTool = "Komut desteklenmiyor.";
                        }
                    }
                } catch (Exception e) {
                    strDispatchDeviceTool = "Komut tamamlanamadı · " + e.getClass().getSimpleName();
                }
                jSONArray2.put(new JSONObject().put("id", jSONObject.getString("id")).put("name", string).put("response", new JSONObject().put("result", strDispatchDeviceTool)));
            }
            if (this.running && this.socket == webSocket) {
                webSocket.send(new JSONObject().put("toolResponse", new JSONObject().put("functionResponses", jSONArray2)).toString());
            }
        } catch (Exception unused) {
            if (this.running) {
                GeminiAccess.report(this, "Gemini Live komut yanıtı gönderilemedi.");
            }
        }
    }

    static boolean sleepPhrase(String str) {
        String strTrim = Normalizer.normalize(str.toLowerCase(Locale.forLanguageTag("tr-TR")), Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replace((char) 305, 'i').replaceAll("[^a-z ]", " ").replaceAll("\\s+", " ").trim();
        return strTrim.equals("sessiz ol") || strTrim.equals("live modu bitir") || strTrim.equals("live modunu bitir") || strTrim.equals("canli modu bitir") || strTrim.equals("canli yayini bitir") || strTrim.equals("sohbeti bitir");
    }

    private void fail(String str) {
        this.main.post(new GeminiLive$$ExternalSyntheticLambda5(this, str));
    }

    /* JADX INFO: renamed from: lambda$fail$7$local-mertcan-jarvis-GeminiLive, reason: not valid java name */
    /* synthetic */ void m30lambda$fail$7$localmertcanjarvisGeminiLive(String str) {
        GeminiAccess.report(this, str);
        stopSelf();
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.running = false;
        active = false;
        closeWakeRecognizer();
        GeminiAccess.reportStatus("Eller serbest kapalı");
        LiveRepair.clearGeminiPlayback();
        this.main.removeCallbacksAndMessages(null);
        if (this.socket != null) {
            this.socket.cancel();
        }
        Thread thread = this.input;
        if (thread != null) {
            thread.interrupt();
        }
        Thread thread2 = this.output;
        if (thread2 != null) {
            thread2.interrupt();
        }
        this.tools.shutdownNow();
        this.playback.clear();
        stopForeground(1);
        super.onDestroy();
    }
}
