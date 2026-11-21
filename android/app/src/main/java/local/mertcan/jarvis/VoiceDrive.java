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
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public final class VoiceDrive extends Service implements RecognitionListener {
    static final String STOP = "local.mertcan.jarvis.VOICE_STOP";
    static volatile boolean active;
    private float ambientRms;
    private AudioManager audio;
    private boolean awake;
    private int errors;
    private AudioFocusRequest focus;
    private SpeechRecognizer recognizer;
    private int rmsSamples;
    private boolean speaking;
    private boolean speechStarted;
    private boolean stopped;
    private TextToSpeech tts;
    private int turn;
    private boolean voiceReady;
    private boolean useDefaultRecognizer;
    private static final Pattern WAKE = Pattern.compile("(?iu)^(?:hey[\\s,]+)?(?:jarvis|carvis)\\b[\\s,.:!?-]*");
    private final Handler timer = new Handler(Looper.getMainLooper());
    private String utterance = "";
    private final Runnable startNext = new VoiceDrive$$ExternalSyntheticLambda7(this);
    private final Runnable speakTimeout = new VoiceDrive$$ExternalSyntheticLambda8(this);

    void liveReply(String str) {
        say(str, true, false);
    }

    void liveResume() {
        this.awake = true;
        queue(640L);
    }

    /* JADX INFO: renamed from: $r8$lambda$CRYL4_fh_N7OhA7vvPfb_4d-es4, reason: not valid java name */
    public static /* synthetic */ void m98$r8$lambda$CRYL4_fh_N7OhA7vvPfb_4des4(VoiceDrive voiceDrive) {
        voiceDrive.listen();
    }

    static /* synthetic */ Handler access$000(VoiceDrive voiceDrive) {
        return voiceDrive.timer;
    }

    static /* synthetic */ void access$100(VoiceDrive voiceDrive, String str) {
        voiceDrive.finishVoice(str);
    }

    /* JADX INFO: renamed from: lambda$new$0$local-mertcan-jarvis-VoiceDrive, reason: not valid java name */
    /* synthetic */ void m104lambda$new$0$localmertcanjarvisVoiceDrive() {
        finishVoice(this.utterance);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent != null && STOP.equals(intent.getAction())) {
            stopSelf();
            return 2;
        }
        if (active) {
            return 2;
        }
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                startForeground(81, notification("Yerel dinleyici hazırlanıyor…"), 128);
            } else {
                startForeground(81, notification("Yerel dinleyici hazırlanıyor…"));
            }
            LiveRepair.attach(this);
            if (!LiveRepair.available(this)) {
                failure("Telefonda kullanılabilir Türkçe ses tanıma hizmeti bulunamadı.");
                return 2;
            }
            this.useDefaultRecognizer = LiveRepair.requested()
                    || Build.VERSION.SDK_INT < 31
                    || !SpeechRecognizer.isOnDeviceRecognitionAvailable(this);
            active = true;
            this.audio = (AudioManager) getSystemService(AudioManager.class);
            AudioAttributes audioAttributesBuild = new AudioAttributes.Builder().setUsage(1).setContentType(1).build();
            this.focus = new AudioFocusRequest.Builder(3).setAudioAttributes(audioAttributesBuild).build();
            this.tts = new TextToSpeech(this, new VoiceDrive$$ExternalSyntheticLambda4(this, audioAttributesBuild));
            try {
                this.recognizer = LiveRepair.recognizer(this);
                this.recognizer.setRecognitionListener(this);
                notice("Müzik çalabilir • “Hey Jarvis” bekleniyor");
                queue(450L);
            } catch (RuntimeException e) {
                failure("Yerel ses tanıma açılamadı.");
            }
            return 2;
        } catch (RuntimeException e2) {
            stopSelf();
            return 2;
        }
    }

    /* JADX INFO: renamed from: lambda$onStartCommand$1$local-mertcan-jarvis-VoiceDrive, reason: not valid java name */
    /* synthetic */ void m105lambda$onStartCommand$1$localmertcanjarvisVoiceDrive(AudioAttributes audioAttributes, int i) {
        if (this.stopped || i != 0 || this.tts == null) {
            return;
        }
        this.voiceReady = this.tts.setLanguage(Main.TR) >= 0;
        this.tts.setAudioAttributes(audioAttributes);
        this.tts.setSpeechRate(0.96f);
        this.tts.setOnUtteranceProgressListener(new AnonymousClass1());
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.VoiceDrive$1, reason: invalid class name */
    class AnonymousClass1 extends UtteranceProgressListener {
        AnonymousClass1() {
        }

        @Override // android.speech.tts.UtteranceProgressListener
        public void onStart(String str) {
        }

        /* JADX INFO: renamed from: lambda$onDone$0$local-mertcan-jarvis-VoiceDrive$1, reason: not valid java name */
        /* synthetic */ void m106lambda$onDone$0$localmertcanjarvisVoiceDrive$1(String str) {
            VoiceDrive.access$100(VoiceDrive.this, str);
        }

        @Override // android.speech.tts.UtteranceProgressListener
        public void onDone(String str) {
            VoiceDrive.access$000(VoiceDrive.this).post(new VoiceDrive$1$$ExternalSyntheticLambda1(this, str));
        }

        /* JADX INFO: renamed from: lambda$onError$1$local-mertcan-jarvis-VoiceDrive$1, reason: not valid java name */
        /* synthetic */ void m107lambda$onError$1$localmertcanjarvisVoiceDrive$1(String str) {
            VoiceDrive.access$100(VoiceDrive.this, str);
        }

        @Override // android.speech.tts.UtteranceProgressListener
        public void onError(String str) {
            VoiceDrive.access$000(VoiceDrive.this).post(new VoiceDrive$1$$ExternalSyntheticLambda0(this, str));
        }
    }

    private PendingIntent openApp() {
        return PendingIntent.getActivity(this, 81, new Intent(this, (Class<?>) Main.class), 67108864);
    }

    private Notification notification(String str) {
        ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel("voice-drive", "Jarvis sesli komut", 2));
        return new Notification.Builder(this, "voice-drive").setSmallIcon(R.drawable.ic_btn_speak_now).setContentTitle("Jarvis dinleyici").setContentText(str).setContentIntent(openApp()).setOngoing(true).setOnlyAlertOnce(true).addAction(new Notification.Action.Builder((Icon) null, "Durdur", PendingIntent.getService(this, 82, new Intent(this, (Class<?>) VoiceDrive.class).setAction(STOP), 67108864)).build()).build();
    }

    private void notice(String str) {
        ((NotificationManager) getSystemService(NotificationManager.class)).notify(81, notification(str));
    }

    private void failure(String str) {
        ((NotificationManager) getSystemService(NotificationManager.class)).notify(83, new Notification.Builder(this, "voice-drive").setSmallIcon(R.drawable.ic_dialog_info).setContentTitle("Jarvis ses ayarı").setContentText(str).setStyle(new Notification.BigTextStyle().bigText(str)).setContentIntent(openApp()).setAutoCancel(true).build());
        stopSelf();
    }

    private void queue(long j) {
        this.timer.removeCallbacks(this.startNext);
        if (this.stopped) {
            return;
        }
        this.timer.postDelayed(this.startNext, j);
    }

    private void listen() {
        if (LiveRepair.busy(this) || this.stopped || this.recognizer == null || this.speaking) {
            return;
        }
        try {
            this.recognizer.startListening(new Intent("android.speech.action.RECOGNIZE_SPEECH").putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form").putExtra("android.speech.extra.LANGUAGE", "tr-TR").putExtra("android.speech.extra.PREFER_OFFLINE", !LiveRepair.requested() && !this.useDefaultRecognizer).putExtra("android.speech.extra.MAX_RESULTS", 3));
        } catch (RuntimeException e) {
            failure("Mikrofon açılamadı. Kaydı kapatıp yeniden dene.");
        }
    }

    @Override // android.speech.RecognitionListener
    public void onResults(Bundle bundle) {
        if (LiveRepair.busy(this) || this.stopped || this.speaking) {
            return;
        }
        this.errors = 0;
        ArrayList<String> stringArrayList = bundle.getStringArrayList("results_recognition");
        if (stringArrayList == null || stringArrayList.isEmpty()) {
            reset();
            return;
        }
        String strTrim = stringArrayList.get(0).trim();
        if (this.awake) {
            command(strTrim);
            return;
        }
        for (String str : stringArrayList) {
            Matcher matcher = WAKE.matcher(str.trim());
            if (matcher.find()) {
                this.awake = true;
                this.turn++;
                this.audio.requestAudioFocus(this.focus);
                if (this.audio.isMusicActive()) {
                    media(127);
                }
                notice("Seni dinliyorum");
                String strTrim2 = str.trim().substring(matcher.end()).trim();
                if (strTrim2.isEmpty()) {
                    say("Dinliyorum.", true, false);
                    return;
                } else {
                    command(strTrim2);
                    return;
                }
            }
        }
        queue(450L);
    }

    private void command(String str) {
        String str2;
        if (LiveRepair.wikiCommand(this, str)) {
            return;
        }
        String strLocal = LiveRepair.local(this, str);
        if (strLocal != null) {
            say(strLocal, LiveRepair.requested(), true);
            return;
        }
        String strReplaceAll = str.trim().toLowerCase(Main.TR).replaceAll("[.!?]+$", "");
        if (strReplaceAll.isEmpty()) {
            reset();
            return;
        }
        if (Cloud.isSearch(str)) {
            String strSearchQuery = Cloud.searchQuery(str);
            String str3 = Cloud.key;
            if (strSearchQuery.isEmpty()) {
                say("Araştırma konusunu da söyle.", true, false);
                return;
            }
            if (str3.isEmpty()) {
                say("Google araştırması için önce Gemini anahtarı gir.", false, false);
                return;
            }
            this.audio.abandonAudioFocusRequest(this.focus);
            int i = this.turn + 1;
            this.turn = i;
            new Thread(new VoiceDrive$$ExternalSyntheticLambda0(this, strSearchQuery, str3, i), "voice-google").start();
            return;
        }
        if (Main.awaitingTelegram() && Main.dispatchTelegram(str)) {
            reset();
            return;
        }
        if (strReplaceAll.startsWith("telegram")) {
            if (Main.prefs(this).getBoolean("driving", false)) {
                say("Sürüşte yeni taslak açılmaz; gelen mesaja yanıt verebilirsin.", false, false);
                return;
            } else if (Main.dispatchTelegram(str)) {
                reset();
                return;
            } else {
                say("Yeni Telegram taslağı için Jarvis ekranı açık olmalı.", false, false);
                return;
            }
        }
        if (strReplaceAll.equals("gönder")) {
            say(Messages.confirm(this), false, false);
            return;
        }
        if (strReplaceAll.equals("iptal")) {
            say(Messages.cancel(), false, false);
            return;
        }
        if (strReplaceAll.startsWith("yanıtla ") || strReplaceAll.startsWith("cevap ver ")) {
            String strDraft = Messages.draft(this, str.substring(strReplaceAll.startsWith("yanıtla ") ? 8 : 10).trim());
            say(strDraft, strDraft.contains("Göndermek için Gönder"), false);
            return;
        }
        History.add(this, "Sen", str);
        if (strReplaceAll.equals("müziğe devam et") || strReplaceAll.equals("müziği devam ettir") || strReplaceAll.equals("müziği aç")) {
            reset();
            media(126);
            History.add(this, "Jarvis", "Müzik devam ediyor.");
            return;
        }
        if (strReplaceAll.equals("müziği durdur")) {
            media(127);
            say("Müzik durdu.", false, true);
            return;
        }
        if (strReplaceAll.equals("sonraki şarkı")) {
            media(87);
            say("Sonraki şarkı.", false, true);
            return;
        }
        if (strReplaceAll.equals("sürüşü bitir")) {
            Main.prefs(this).edit().putBoolean("driving", false).apply();
            Messages.clear();
            stopService(new Intent(this, (Class<?>) Dashcam.class));
            say("Sürüş modu kapalı.", false, true);
            this.timer.postDelayed(new VoiceDrive$$ExternalSyntheticLambda1(this), 2600L);
            return;
        }
        if (strReplaceAll.equals("son mesajı oku") || strReplaceAll.equals("mesajları oku")) {
            String strText = Main.prefs(this).getBoolean("driving", false) ? Messages.text() : null;
            say(strText == null ? "Yeni mesaj yok." : strText.substring(0, Math.min(220, strText.length())), false, false);
            return;
        }
        if (strReplaceAll.equals("kurları söyle") || strReplaceAll.equals("kurlar") || strReplaceAll.equals("haberleri oku") || strReplaceAll.equals("haberler")) {
            new Thread(new VoiceDrive$$ExternalSyntheticLambda2(this, strReplaceAll.startsWith("kur"), this.turn), "voice-brief").start();
            return;
        }
        if (strReplaceAll.equals("selam") || strReplaceAll.equals("merhaba")) {
            say("Merhaba, buradayım.", false, true);
            return;
        }
        if (strReplaceAll.equals("ortam sesi nasıl") || strReplaceAll.equals("yol sesi nasıl")) {
            if (this.rmsSamples < 8) {
                str2 = "Ortam sesini değerlendirecek kadar örnek yok.";
            } else {
                str2 = this.ambientRms > 8.0f ? "Arka plan sesi belirgin. Bu, desibel ölçümü değil." : "Arka plan sesi düşük görünüyor. Bu, desibel ölçümü değil.";
            }
            say(str2, false, true);
            return;
        }
        if (strReplaceAll.startsWith("hatırla ") && str.length() > 8) {
            String strTrim = str.substring(8).trim();
            if (strTrim.length() > 500) {
                strTrim = strTrim.substring(0, 500);
            }
            Main.prefs(this).edit().putString("voiceNote", strTrim).apply();
            say("Notunu telefona kaydettim.", false, true);
            return;
        }
        if (strReplaceAll.equals("notumu oku")) {
            String string = Main.prefs(this).getString("voiceNote", "");
            if (string.isEmpty()) {
                string = "Kayıtlı sesli not yok.";
            }
            say(string, false, false);
            return;
        }
        String strIntent = Actions.intent(str);
        if (!strIntent.isEmpty()) {
            say(Actions.run(this, strIntent, strIntent.equals("youtube_music") ? Actions.songQuery(str) : ""), false, true);
        } else {
            if (LiveRepair.answer(this, str)) {
                return;
            }
            say("Bu komut eller serbest dinleyicide henüz desteklenmiyor.", false, true);
        }
    }

    /* JADX INFO: renamed from: lambda$command$4$local-mertcan-jarvis-VoiceDrive, reason: not valid java name */
    /* synthetic */ void m101lambda$command$4$localmertcanjarvisVoiceDrive(String str, String str2, int i) {
        try {
            this.timer.post(new VoiceDrive$$ExternalSyntheticLambda5(this, i, Cloud.research(str, str2)));
        } catch (Exception e) {
            this.timer.post(new VoiceDrive$$ExternalSyntheticLambda6(this, i));
        }
    }

    /* JADX INFO: renamed from: lambda$command$2$local-mertcan-jarvis-VoiceDrive, reason: not valid java name */
    /* synthetic */ void m99lambda$command$2$localmertcanjarvisVoiceDrive(int i, Cloud.Result result) {
        if (this.stopped || this.turn != i) {
            return;
        }
        this.audio.requestAudioFocus(this.focus);
        say(result.text, false, true);
    }

    /* JADX INFO: renamed from: lambda$command$3$local-mertcan-jarvis-VoiceDrive, reason: not valid java name */
    /* synthetic */ void m100lambda$command$3$localmertcanjarvisVoiceDrive(int i) {
        if (this.stopped || this.turn != i) {
            return;
        }
        this.audio.requestAudioFocus(this.focus);
        say("Google araştırması tamamlanamadı.", false, false);
    }

    /* JADX INFO: renamed from: lambda$command$6$local-mertcan-jarvis-VoiceDrive, reason: not valid java name */
    /* synthetic */ void m103lambda$command$6$localmertcanjarvisVoiceDrive(boolean z, int i) {
        String strRates;
        try {
            strRates = z ? Brief.rates() : Brief.news();
        } catch (Exception e) {
            strRates = "Güncel veriye erişemedim.";
        }
        this.timer.post(new VoiceDrive$$ExternalSyntheticLambda3(this, i, strRates));
    }

    /* JADX INFO: renamed from: lambda$command$5$local-mertcan-jarvis-VoiceDrive, reason: not valid java name */
    /* synthetic */ void m102lambda$command$5$localmertcanjarvisVoiceDrive(int i, String str) {
        if (this.stopped || this.turn != i) {
            return;
        }
        say(str, false, true);
    }

    private void media(int i) {
        this.audio.dispatchMediaKeyEvent(new KeyEvent(0, i));
        this.audio.dispatchMediaKeyEvent(new KeyEvent(1, i));
    }

    private void say(String str, boolean z, boolean z2) {
        if (z2) {
            History.add(this, "Jarvis", str);
        }
        this.awake = z;
        if (!this.voiceReady || this.tts == null) {
            if (this.awake) {
                queue(350L);
                return;
            } else {
                reset();
                return;
            }
        }
        this.speaking = true;
        this.utterance = UUID.randomUUID().toString();
        this.timer.removeCallbacks(this.speakTimeout);
        this.timer.postDelayed(this.speakTimeout, 20000L);
        if (this.tts.speak(str, 0, null, this.utterance) == -1) {
            finishVoice(this.utterance);
        }
    }

    private void finishVoice(String str) {
        if (this.stopped || !this.speaking || !this.utterance.equals(str)) {
            return;
        }
        this.timer.removeCallbacks(this.speakTimeout);
        this.speaking = false;
        if (this.awake) {
            queue(400L);
        } else {
            reset();
        }
    }

    private void reset() {
        this.awake = false;
        this.turn++;
        if (this.audio != null && this.focus != null) {
            this.audio.abandonAudioFocusRequest(this.focus);
        }
        notice("“Hey Jarvis” bekleniyor");
        queue(650L);
    }

    @Override // android.speech.RecognitionListener
    public void onError(int i) {
        if (LiveRepair.busy(this) || this.stopped || this.speaking) {
            return;
        }
        if (i == 9) {
            failure("Mikrofon izni gerekli. Android uygulama izinlerinden mikrofon erişimini aç.");
            return;
        }
        if (i == 12 || i == 13) {
            if (!this.useDefaultRecognizer && SpeechRecognizer.isRecognitionAvailable(this)) {
                try {
                    this.useDefaultRecognizer = true;
                    if (this.recognizer != null) {
                        try { this.recognizer.cancel(); } catch (RuntimeException ignored) { }
                        this.recognizer.destroy();
                    }
                    this.recognizer = SpeechRecognizer.createSpeechRecognizer(this);
                    this.recognizer.setRecognitionListener(this);
                    this.errors = 0;
                    notice("Çevrimdışı Türkçe model yok · çevrimiçi ses tanıma kullanılıyor");
                    queue(700L);
                    return;
                } catch (RuntimeException ignored) {
                    // Fall through to a clear service error below.
                }
            }
            failure("Türkçe ses tanıma hizmeti kullanılamıyor. Google konuşma hizmetini ve mikrofon iznini kontrol et.");
            return;
        }
        if (this.awake) {
            reset();
            return;
        }
        if (i == 7 || i == 6) {
            this.errors = 0;
            queue(700L);
            return;
        }
        int i2 = this.errors + 1;
        this.errors = i2;
        if (i2 >= 12) {
            failure("Ses tanıma tekrar tekrar kesildi. Mikrofonu kullanan uygulamaları ve bağlantıyı kontrol et.");
        } else {
            queue(((long) this.errors) * 650);
        }
    }

    @Override // android.speech.RecognitionListener
    public void onReadyForSpeech(Bundle bundle) {
        this.speechStarted = false;
    }

    @Override // android.speech.RecognitionListener
    public void onBeginningOfSpeech() {
        this.speechStarted = true;
    }

    @Override // android.speech.RecognitionListener
    public void onRmsChanged(float f) {
        if (!this.awake && !this.speaking && !this.speechStarted && Float.isFinite(f)) {
            if (this.rmsSamples != 0) {
                f = (f * 0.08f) + (this.ambientRms * 0.92f);
            }
            this.ambientRms = f;
            if (this.rmsSamples < 1000) {
                this.rmsSamples++;
            }
        }
    }

    @Override // android.speech.RecognitionListener
    public void onBufferReceived(byte[] bArr) {
    }

    @Override // android.speech.RecognitionListener
    public void onEndOfSpeech() {
    }

    @Override // android.speech.RecognitionListener
    public void onPartialResults(Bundle bundle) {
    }

    @Override // android.speech.RecognitionListener
    public void onEvent(int i, Bundle bundle) {
    }

    @Override // android.app.Service
    public void onDestroy() {
        LiveRepair.detach(this);
        this.stopped = true;
        active = false;
        this.timer.removeCallbacksAndMessages(null);
        if (this.recognizer != null) {
            this.recognizer.destroy();
        }
        if (this.tts != null) {
            this.tts.stop();
            this.tts.shutdown();
        }
        if (this.audio != null && this.focus != null) {
            this.audio.abandonAudioFocusRequest(this.focus);
        }
        stopForeground(1);
        super.onDestroy();
    }
}
