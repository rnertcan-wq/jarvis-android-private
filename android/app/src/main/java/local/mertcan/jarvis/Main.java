package local.mertcan.jarvis;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public final class Main extends Activity implements RecognitionListener {
    private AudioManager audio;
    private EditText command;
    private int evrenGeneration;
    private MediaPlayer evrenPlayer;
    private AudioFocusRequest focus;
    private boolean pendingHome;
    private boolean pendingTelegram;
    private boolean ready;
    private boolean recordDual;
    private boolean recordFront;
    private OrientationEventListener recordingOrientation;
    private LinearLayout row;
    private SpeechRecognizer speech;
    private TextView status;
    private LinearLayout ui;
    private static Main visible;
    private TextToSpeech voice;
    private boolean wide;
    static final Locale TR = new Locale("tr", "TR");
    private String telegramRecipient = "";
    private int physicalDegrees = -1;
    private final BroadcastReceiver recordStatus = new AnonymousClass1();

    public static /* synthetic */ void $r8$lambda$30V5lWmJ6d1fiZbDuiLMrHJkz7I(Main main) {
        main.confirmLive();
    }

    public static /* synthetic */ void $r8$lambda$5Iw86coIwrdS1cm3fpP4BpTmsFU(Main main) {
        main.drive();
    }

    public static /* synthetic */ void $r8$lambda$7VOapvd8JO98gjrGGgS3i8bkuLw(Main main) {
        main.provider();
    }

    public static /* synthetic */ void $r8$lambda$Cifv0eGci4jOUVhsZcwIQ32g7Ds(Main main) {
        main.history();
    }

    public static /* synthetic */ void $r8$lambda$GtkaGGU1RNaqKihFEUlzJYW_o6s(Main main) {
        main.cloudKey();
    }

    public static /* synthetic */ void $r8$lambda$K22e7qgVbK4ixsaZdiRQDMq2uD4(Main main) {
        main.searchDialog();
    }

    public static /* synthetic */ void $r8$lambda$QKchNTXXs4CFTa82hfETZ8beySs(Main main) {
        main.listen();
    }

    public static /* synthetic */ void $r8$lambda$T3iWxaTNMDVpCx5dOOeBvkTVJm0(Main main) {
        main.endDrive();
    }

    public static /* synthetic */ void $r8$lambda$TVZ34JZ2dPx10qm8ToPmwDzBtHM(Main main) {
        main.car();
    }

    public static /* synthetic */ void $r8$lambda$WCHO4A8TTLwoV2Jc78NepR2Zm3k(Main main) {
        main.evrenKey();
    }

    public static /* synthetic */ void $r8$lambda$gWh5EnQUmhIbcTH6REJGNVT8hl8(Main main) {
        main.exportRecording();
    }

    private void confirmLive() {
        LiveRepair.select(this);
    }

    /* JADX INFO: renamed from: $r8$lambda$9oAQSMUwdsXwOe7o80run-WWQW8, reason: not valid java name */
    public static /* synthetic */ void m36$r8$lambda$9oAQSMUwdsXwOe7o80runWWQW8(Main main) {
        main.termux();
    }

    /* JADX INFO: renamed from: $r8$lambda$KbWs02R5FFAopQIRM0QySYC-GoY, reason: not valid java name */
    public static /* synthetic */ void m37$r8$lambda$KbWs02R5FFAopQIRM0QySYCGoY(Main main) {
        main.cameraCheck();
    }

    /* JADX INFO: renamed from: $r8$lambda$N2UatQ6Wyum6V8-xeB8KqP3slw8, reason: not valid java name */
    public static /* synthetic */ void m38$r8$lambda$N2UatQ6Wyum6V8xeB8KqP3slw8(Main main) {
        main.enableHandsFree();
    }

    /* JADX INFO: renamed from: $r8$lambda$RADq1mGgwGfhi3-aCP31wjvh_C4, reason: not valid java name */
    public static /* synthetic */ void m39$r8$lambda$RADq1mGgwGfhi3aCP31wjvh_C4(Main main) {
        main.openTelegram();
    }

    /* JADX INFO: renamed from: $r8$lambda$dl5Vr-j-YsgSe38xTE24IQz2CPo, reason: not valid java name */
    public static /* synthetic */ void m40$r8$lambda$dl5VrjYsgSe38xTE24IQz2CPo(Main main) {
        main.home();
    }

    /* JADX INFO: renamed from: $r8$lambda$nu9vfFaR_cabscrdkuIKY-iu7qk, reason: not valid java name */
    public static /* synthetic */ void m41$r8$lambda$nu9vfFaR_cabscrdkuIKYiu7qk(Main main) {
        main.stopRecord();
    }

    void releaseMicForEvrenTalk() {
        stopEvren();
        SpeechRecognizer speechRecognizer = this.speech;
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
        }
        TextToSpeech textToSpeech = this.voice;
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    void showLiveStatus(String str) {
        this.status.setText(str);
    }

    void startEvrenLive() {
        if (GeminiLive.active || VoiceDrive.active) {
            this.status.setText("Önce açık olan ses oturumunu durdur.");
        } else if (LiveRepair.request(this)) {
            DriveCamera.yieldMicrophone(this);
            stopEvren();
            enableHandsFree();
        }
    }

    static boolean isVisible(Main main) {
        return main != null && visible == main && !main.isFinishing() && !main.isDestroyed();
    }

    static boolean openEvrenLiveFromVoice() {
        Main main = visible;
        if (!isVisible(main) || Evren.key.isEmpty()) return false;
        main.runOnUiThread(() -> {
            if (isVisible(main) && !Evren.key.isEmpty()) EvrenTalk.open(main);
        });
        return true;
    }

    static boolean startGeminiLiveFromVoice() {
        Main main = visible;
        if (!isVisible(main)) return false;
        main.runOnUiThread(() -> {
            if (isVisible(main)) main.startLive();
        });
        return true;
    }

    static /* synthetic */ TextView access$000(Main main) {
        return main.status;
    }

    static /* synthetic */ int access$102(Main main, int i) {
        main.physicalDegrees = i;
        return i;
    }

    static /* synthetic */ AudioFocusRequest access$200(Main main) {
        return main.focus;
    }

    static /* synthetic */ AudioManager access$300(Main main) {
        return main.audio;
    }

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("jarvis", 0);
    }

    static String launchYouTubeMusic(String str) {
        Main main = visible;
        if (main == null) {
            return "YouTube Music oturumu açık değil. Park hâlindeyken uygulamayı bir kez aç.";
        }
        if (main.driving()) {
            return "Sürüşte yeni ekran açılmadı. Park hâlindeyken YouTube Music'i aç.";
        }
        if (Actions.youtubeLaunchIntent(main) == null) {
            return "YouTube Music kurulu değil.";
        }
        main.runOnUiThread(new Main$$ExternalSyntheticLambda3(main, str));
        return "YouTube Music'e oynatma isteği gönderildi.";
    }

    static /* synthetic */ void lambda$launchYouTubeMusic$0(Main main, String str) {
        if (visible != main || main.driving()) {
            return;
        }
        try {
            main.startActivity(Actions.youtubePlayIntent(str));
        } catch (ActivityNotFoundException e) {
            if (!str.isEmpty()) {
                main.open(new Intent("android.intent.action.VIEW", Uri.parse("https://music.youtube.com/search?q=" + Uri.encode(str))));
            } else {
                main.open(Actions.youtubeLaunchIntent(main));
            }
        }
    }

    static boolean dispatchTelegram(String str) {
        return visible != null && visible.telegram(str);
    }

    static boolean awaitingTelegram() {
        return visible != null && visible.pendingTelegram;
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.Main$1, reason: invalid class name */
    class AnonymousClass1 extends BroadcastReceiver {
        AnonymousClass1() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String stringExtra = intent.getStringExtra("text");
            if (stringExtra != null) {
                Main.access$000(Main.this).setText(stringExtra);
            }
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Cloud.key = Secrets.loadPersonal(this, "gemini");
        Evren.key = Secrets.loadPersonal(this, "evren");
        this.recordingOrientation = new AnonymousClass2(this);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener(new Main$$ExternalSyntheticLambda25());
        }
        this.audio = (AudioManager) getSystemService(AudioManager.class);
        AudioAttributes audioAttributesBuild = new AudioAttributes.Builder().setUsage(16).setContentType(1).build();
        this.focus = new AudioFocusRequest.Builder(3).setAudioAttributes(audioAttributesBuild).build();
        this.voice = new TextToSpeech(this, new Main$$ExternalSyntheticLambda36(this, audioAttributesBuild));
        this.wide = getResources().getConfiguration().orientation == 2;
        if (bundle != null) {
            this.pendingHome = bundle.getBoolean("homePending");
            this.pendingTelegram = bundle.getBoolean("telegramPending");
            this.telegramRecipient = bundle.getString("telegramRecipient", "");
        }
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        this.ui = new LinearLayout(this);
        this.ui.setOrientation(1);
        this.ui.setBackgroundColor(0xFF081522);
        int iDp = dp(this.wide ? 12 : 16);
        this.ui.setPadding(iDp, iDp, iDp, iDp);
        scrollView.addView(this.ui);
        setContentView(scrollView);
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(1);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable heroBackground = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF17344D, 0xFF0D1E30});
        heroBackground.setCornerRadius(dp(26));
        heroBackground.setStroke(dp(1), 0xFF294A65);
        hero.setBackground(heroBackground);
        hero.setElevation(dp(4));

        JarvisOrbView orb = new JarvisOrbView(this);
        LinearLayout.LayoutParams orbParams = new LinearLayout.LayoutParams(dp(116), dp(116));
        orbParams.gravity = Gravity.CENTER;
        hero.addView(orb, orbParams);

        TextView brand = new TextView(this);
        brand.setText("JARVIS AI 2.0");
        brand.setTextColor(0xFFF0F8FF);
        brand.setTextSize(21.0f);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setGravity(Gravity.CENTER);
        brand.setPadding(0, dp(4), 0, 0);
        hero.addView(brand);

        TextView tagline = new TextView(this);
        tagline.setText("PERSONAL AI  •  HANDS-FREE");
        tagline.setTextColor(0xFF8EB7D4);
        tagline.setTextSize(10.0f);
        tagline.setLetterSpacing(0.14f);
        tagline.setGravity(Gravity.CENTER);
        tagline.setPadding(0, dp(4), 0, dp(10));
        hero.addView(tagline);

        this.status = new TextView(this);
        this.status.setText("Hazır · “Hey Jarvis” bekleniyor");
        this.status.setTextColor(0xFFB9D8EC);
        this.status.setTextSize(12.0f);
        this.status.setGravity(Gravity.CENTER);
        this.status.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable statusBackground = new GradientDrawable();
        statusBackground.setColor(0xFF132B40);
        statusBackground.setCornerRadius(dp(18));
        statusBackground.setStroke(dp(1), 0xFF2A506D);
        this.status.setBackground(statusBackground);
        hero.addView(this.status, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(-1, -2);
        heroParams.bottomMargin = dp(12);
        this.ui.addView(hero, heroParams);

        this.command = new EditText(this);
        this.command.setId(13260033);
        this.command.setHint(this.pendingTelegram ? "Mesajını yaz veya söyle…" : "Komut yaz…");
        this.command.setHintTextColor(0xFF7894AC);
        this.command.setTextColor(0xFFEAF5FF);
        this.command.setSingleLine(true);
        this.command.setTextSize(14.0f);
        this.command.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable commandBackground = new GradientDrawable();
        commandBackground.setColor(0xFF102235);
        commandBackground.setCornerRadius(dp(16));
        commandBackground.setStroke(dp(1), 0xFF29465F);
        this.command.setBackground(commandBackground);
        LinearLayout.LayoutParams commandParams = new LinearLayout.LayoutParams(-1, dp(54));
        commandParams.bottomMargin = dp(8);
        this.ui.addView(this.command, commandParams);
        section("Hızlı işlemler");
        button("Komutu çalıştır", new Main$$ExternalSyntheticLambda52(this));
        button("Hey Jarvis dinleyicisini aç", new Main$$ExternalSyntheticLambda53(this));
        button("Hey Jarvis dinleyicisini kapat", new Main$$ExternalSyntheticLambda54(this));
        button("Sohbet ve komut geçmişi", new Main$$ExternalSyntheticLambda55(this));
        section("Ses ve ajanlar");
        button("Sağlayıcı: " + (usingGemini() ? "Gemini" : "EVREN"), new Main$$ExternalSyntheticLambda56(this));
        button("Gemini anahtarı", new Main$$ExternalSyntheticLambda57(this));
        button("EVREN anahtarı", new Main$$ExternalSyntheticLambda58(this));
        button("EVREN ile konuş", new Main$$ExternalSyntheticLambda26(this));
        button("Hey Jarvis · Dinlemeyi başlat", new Main$$ExternalSyntheticLambda27(this));
        button("Eller serbest dinlemeyi kapat", new Main$$ExternalSyntheticLambda28(this));
        button("Google'da araştır", new Main$$ExternalSyntheticLambda29(this));
        button("Telegram", new Main$$ExternalSyntheticLambda30(this));
        button("Telegram kişi takma adı", new Main$$ExternalSyntheticLambda31(this));
        section("Sürüş ve navigasyon");
        button("Sürüşü başlat", new Main$$ExternalSyntheticLambda32(this));
        button("Sürüşü bitir", new Main$$ExternalSyntheticLambda33(this));
        button("Aracın Bluetooth'unu seç", new Main$$ExternalSyntheticLambda34(this));
        button("Ev adresini ayarla", new Main$$ExternalSyntheticLambda35(this));
        button("Eve git", new Main$$ExternalSyntheticLambda37(this));
        button("Bildirim erişimi ayarları", new Main$$ExternalSyntheticLambda38(this));
        button("Arama izinleri", new Main$$ExternalSyntheticLambda39(this));
        section("Bilgi ve kayıt");
        button("Haber başlıklarını oku", new Main$$ExternalSyntheticLambda40(this));
        button("TCMB döviz kurlarını oku", new Main$$ExternalSyntheticLambda41(this));
        button("Sürüş kamerası", DriveCamera.panelAction(this));
        button("Kamera uyumluluğunu kontrol et", new Main$$ExternalSyntheticLambda42(this));
        button("Arka kamera + ses kaydını başlat", new Main$$ExternalSyntheticLambda43(this));
        button("Ön kamera + ses kaydını başlat", new Main$$ExternalSyntheticLambda44(this));
        button("Ön ve arka kamera + ses kaydını başlat", new Main$$ExternalSyntheticLambda45(this));
        button("Kayıt durdur", new Main$$ExternalSyntheticLambda46(this));
        button("Kayıtları galeriye aktar", new Main$$ExternalSyntheticLambda48(this));
        section("Araçlar");
        button("Termux'u aç", new Main$$ExternalSyntheticLambda49(this));
        button("GitHub'ı aç", new Main$$ExternalSyntheticLambda50(this));
        IntentFilter intentFilter = new IntentFilter("local.mertcan.jarvis.RECORDING_STATUS");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(this.recordStatus, intentFilter, 4);
        } else {
            registerReceiver(this.recordStatus, intentFilter);
        }
        Retention.schedule(this);
        new Thread(new Main$$ExternalSyntheticLambda51(this), "retention").start();
        fromIntent(getIntent());
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.Main$2, reason: invalid class name */
    class AnonymousClass2 extends OrientationEventListener {
        AnonymousClass2(Context context) {
            super(context);
        }

        @Override // android.view.OrientationEventListener
        public void onOrientationChanged(int i) {
            if (i != -1) {
                Main.access$102(Main.this, (((i + 45) / 90) * 90) % 360);
            }
        }
    }

    static /* synthetic */ WindowInsets lambda$onCreate$1(View view, WindowInsets windowInsets) {
        Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars());
        view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        return windowInsets;
    }

    /* JADX INFO: renamed from: lambda$onCreate$2$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m77lambda$onCreate$2$localmertcanjarvisMain(AudioAttributes audioAttributes, int i) {
        if (i != 0 || this.voice == null) {
            return;
        }
        this.ready = this.voice.setLanguage(TR) >= 0;
        this.voice.setAudioAttributes(audioAttributes);
        this.voice.setOnUtteranceProgressListener(new AnonymousClass3());
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.Main$3, reason: invalid class name */
    class AnonymousClass3 extends UtteranceProgressListener {
        AnonymousClass3() {
        }

        @Override // android.speech.tts.UtteranceProgressListener
        public void onStart(String str) {
        }

        @Override // android.speech.tts.UtteranceProgressListener
        public void onDone(String str) {
            Main.access$300(Main.this).abandonAudioFocusRequest(Main.access$200(Main.this));
        }

        @Override // android.speech.tts.UtteranceProgressListener
        public void onError(String str) {
            Main.access$300(Main.this).abandonAudioFocusRequest(Main.access$200(Main.this));
        }
    }

    /* JADX INFO: renamed from: lambda$onCreate$3$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m78lambda$onCreate$3$localmertcanjarvisMain() {
        act(this.command.getText().toString());
    }

    /* JADX INFO: renamed from: lambda$onCreate$4$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m79lambda$onCreate$4$localmertcanjarvisMain() {
        LiveRepair.stop(this);
        stopService(new Intent(this, (Class<?>) VoiceDrive.class));
    }

    /* JADX INFO: renamed from: lambda$onCreate$5$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m80lambda$onCreate$5$localmertcanjarvisMain() {
        this.command.setText("EVREN ");
        this.command.requestFocus();
        this.command.setSelection(this.command.length());
        this.status.setText("Sorunu EVREN sözcüğünden sonra yaz veya söyle.");
    }

    /* JADX INFO: renamed from: lambda$onCreate$6$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m81lambda$onCreate$6$localmertcanjarvisMain() {
        LiveRepair.stop(this);
        stopService(new Intent(this, (Class<?>) GeminiLive.class));
    }

    /* JADX INFO: renamed from: lambda$onCreate$7$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m82lambda$onCreate$7$localmertcanjarvisMain() {
        editTelegramAlias("", "");
    }

    /* JADX INFO: renamed from: lambda$onCreate$8$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m83lambda$onCreate$8$localmertcanjarvisMain() {
        settingHome();
    }

    /* JADX INFO: renamed from: lambda$onCreate$9$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m84lambda$onCreate$9$localmertcanjarvisMain() {
        open(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    /* JADX INFO: renamed from: lambda$onCreate$10$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m69lambda$onCreate$10$localmertcanjarvisMain() {
        requestPermissions(new String[]{"android.permission.READ_CONTACTS", "android.permission.CALL_PHONE"}, 4);
    }

    /* JADX INFO: renamed from: lambda$onCreate$11$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m70lambda$onCreate$11$localmertcanjarvisMain() {
        brief(false);
    }

    /* JADX INFO: renamed from: lambda$onCreate$12$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m71lambda$onCreate$12$localmertcanjarvisMain() {
        brief(true);
    }

    /* JADX INFO: renamed from: lambda$onCreate$13$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m72lambda$onCreate$13$localmertcanjarvisMain() {
        record(false, false);
    }

    /* JADX INFO: renamed from: lambda$onCreate$14$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m73lambda$onCreate$14$localmertcanjarvisMain() {
        record(false, true);
    }

    /* JADX INFO: renamed from: lambda$onCreate$15$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m74lambda$onCreate$15$localmertcanjarvisMain() {
        record(true, false);
    }

    /* JADX INFO: renamed from: lambda$onCreate$16$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m75lambda$onCreate$16$localmertcanjarvisMain() {
        if (driving()) {
            return;
        }
        view("https://github.com/");
    }

    /* JADX INFO: renamed from: lambda$onCreate$17$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m76lambda$onCreate$17$localmertcanjarvisMain() {
        Retention.purge(this);
    }

    @Override // android.app.Activity
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fromIntent(intent);
    }

    private void fromIntent(Intent intent) {
        if ("local.mertcan.jarvis.DRIVE".equals(intent.getAction())) {
            intent.setAction(null);
            drive();
        }
    }

    private int dp(int i) {
        return (int) ((i * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void section(String str) {
        this.row = null;
        TextView textView = new TextView(this);
        textView.setText(str.toUpperCase(TR));
        textView.setTextColor(0xFF82B8D8);
        textView.setTextSize(11.0f);
        textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textView.setLetterSpacing(0.09f);
        textView.setPadding(dp(4), dp(18), 0, dp(7));
        this.ui.addView(textView);
    }

    private void button(String str, Runnable runnable) {
        Button button = new Button(this);
        button.setText(str);
        button.setAllCaps(false);
        button.setOnClickListener(new Main$ExternalSyntheticLambda59(runnable));
        button.setTextSize(13.0f);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(0xFFE6F2FC);
        boolean accent = str.toLowerCase(TR).contains("başlat") || str.toLowerCase(TR).contains("aç");
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                accent ? new int[]{0xFF1B4867, 0xFF12334D} : new int[]{0xFF1B3045, 0xFF122338});
        gradientDrawable.setCornerRadius(dp(16));
        gradientDrawable.setStroke(dp(1), accent ? 0xFF34759A : 0xFF2A455D);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x3347CFFF), gradientDrawable, null));
        button.setPadding(dp(10), dp(6), dp(10), dp(6));
        button.setElevation(dp(2));
        int i = this.wide ? 3 : 2;
        if (this.row == null || this.row.getChildCount() >= i) {
            this.row = new LinearLayout(this);
            this.ui.addView(this.row);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(60), 1.0f);
        layoutParams.setMargins(dp(4), dp(4), dp(4), dp(4));
        this.row.setWeightSum(i);
        this.row.addView(button, layoutParams);
    }

    static /* synthetic */ void lambda$button$18(Runnable runnable, View view) {
        runnable.run();
    }

    private boolean has(String str) {
        return checkSelfPermission(str) == 0;
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            say("Uygun uygulama bulunamadı.");
        }
    }

    private void view(String str) {
        open(new Intent("android.intent.action.VIEW", Uri.parse(str)));
    }

    private void openTelegram() {
        if (driving()) {
            say("Telegram ekranı sürüşte kapalı.");
            return;
        }
        Intent intentApp = TelegramCommand.app(this);
        if (intentApp == null) {
            say("Telegram kurulu değil.");
        } else {
            open(intentApp);
        }
    }

    private boolean telegram(String str) {
        if (this.pendingTelegram) {
            this.pendingTelegram = false;
            if (str.equalsIgnoreCase("iptal") || str.isEmpty()) {
                say("Taslak iptal edildi.");
                return true;
            }
            deliverTelegram(this.telegramRecipient, str);
            return true;
        }
        if (TelegramCommand.isOpen(str)) {
            openTelegram();
            return true;
        }
        TelegramCommand telegramCommand = TelegramCommand.parse(str, false);
        if (telegramCommand == null) {
            return false;
        }
        if (driving()) {
            say("Sürüşte yeni Telegram taslağı açılmaz; gelen mesaja yanıt verebilirsin.");
            return true;
        }
        String string = prefs(this).getString(TelegramCommand.key(telegramCommand.recipient), "");
        if (string.isEmpty() && ((telegramCommand.recipient.startsWith("@") || telegramCommand.recipient.startsWith("+")) && TelegramCommand.valid(telegramCommand.recipient))) {
            string = telegramCommand.recipient;
        }
        if (string.isEmpty()) {
            say("Önce Telegram takma adını eşleştir.");
            editTelegramAlias(telegramCommand.recipient, telegramCommand.body);
        } else {
            deliverTelegram(string, telegramCommand.body);
        }
        return true;
    }

    private void deliverTelegram(String str, String str2) {
        if (driving()) {
            say("Sürüşte Telegram taslağı açılmaz.");
            return;
        }
        Intent intentApp = TelegramCommand.app(this);
        if (intentApp == null) {
            say("Telegram kurulu değil.");
            return;
        }
        if (str2.isEmpty()) {
            this.telegramRecipient = str;
            this.pendingTelegram = true;
            this.command.setHint("Mesajını yaz veya söyle…");
            say("Ne yazayım? Mesajı söyle veya yaz.");
            return;
        }
        this.command.setHint("Komut yaz…");
        say("Telegram taslağı: " + str2, false);
        new AlertDialog.Builder(this).setTitle("Telegram taslağı").setMessage(str2).setPositiveButton("Telegram'da aç", new Main$$ExternalSyntheticLambda1(this, str, str2, intentApp)).setNegativeButton("İptal", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$deliverTelegram$19$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m50lambda$deliverTelegram$19$localmertcanjarvisMain(String str, String str2, Intent intent, DialogInterface dialogInterface, int i) {
        Intent intentDraft = TelegramCommand.draft(str, str2);
        if (intent.getComponent() != null) {
            intentDraft.setPackage(intent.getComponent().getPackageName());
        }
        open(intentDraft);
    }

    private void editTelegramAlias(String str, String str2) {
        if (driving()) {
            say("Takma ad ayarı sürüşte kapalı.");
            return;
        }
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(16), 0, dp(16), 0);
        EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setHint("Takma ad: aşkım");
        editText.setText(str);
        linearLayout.addView(editText);
        EditText editText2 = new EditText(this);
        editText2.setSingleLine(true);
        editText2.setHint("@kullanıcıadı veya +90…");
        linearLayout.addView(editText2);
        AlertDialog alertDialogCreate = new AlertDialog.Builder(this).setTitle("Telegram kişisini eşleştir").setView(linearLayout).setPositiveButton("Kaydet", (DialogInterface.OnClickListener) null).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.show();
        alertDialogCreate.getButton(-1).setOnClickListener(new Main$$ExternalSyntheticLambda23(this, editText, editText2, alertDialogCreate, str, str2));
    }

    /* JADX INFO: renamed from: lambda$editTelegramAlias$20$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m52lambda$editTelegramAlias$20$localmertcanjarvisMain(EditText editText, EditText editText2, AlertDialog alertDialog, String str, String str2, View view) {
        String strTrim = editText.getText().toString().trim();
        String strTrim2 = editText2.getText().toString().trim();
        if (strTrim.isEmpty()) {
            editText.setError("Takma adı gir.");
            return;
        }
        if (!TelegramCommand.valid(strTrim2)) {
            editText2.setError("Telegram kullanıcı adı veya uluslararası numara gir.");
            return;
        }
        prefs(this).edit().putString(TelegramCommand.key(strTrim), strTrim2).apply();
        alertDialog.dismiss();
        if (!str.isEmpty()) {
            deliverTelegram(strTrim2, str2);
        } else {
            say("Telegram kişisi kaydedildi.");
        }
    }

    private void say(String str) {
        say(str, true);
    }

    private void say(String str, boolean z) {
        this.status.setText(str);
        if (z) {
            History.add(this, "Jarvis", str);
        }
        if (!VoiceDrive.active && !GeminiLive.active && this.ready) {
            this.audio.requestAudioFocus(this.focus);
            if (this.voice.speak(str, 0, null, UUID.randomUUID().toString()) == -1) {
                this.audio.abandonAudioFocusRequest(this.focus);
            }
        }
    }

    private void listen() {
        if (GeminiLive.active) {
            this.status.setText("Gemini Live mikrofonu kullanıyor.");
            return;
        }
        if (VoiceDrive.active) {
            this.status.setText("Eller serbest dinleyici açık. “Hey Jarvis” de.");
            return;
        }
        if (!has("android.permission.RECORD_AUDIO")) {
            requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 1);
            return;
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            say("Bu telefonda konuşma tanıma hizmeti bulunamadı.");
            return;
        }
        if (this.voice != null) {
            this.voice.stop();
        }
        if (this.speech == null) {
            this.speech = SpeechRecognizer.createSpeechRecognizer(this);
            this.speech.setRecognitionListener(this);
        }
        this.speech.cancel();
        this.speech.startListening(new Intent("android.speech.action.RECOGNIZE_SPEECH").putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form").putExtra("android.speech.extra.LANGUAGE", "tr-TR").putExtra("android.speech.extra.PREFER_OFFLINE", true));
        this.status.setText("Dinliyorum…");
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        DriveDashboard.onPermission(this, i);
        EvrenTalk.permission(this, i);
        if (i == 1 && has("android.permission.RECORD_AUDIO")) {
            listen();
        }
        if (i == 2 && (Build.VERSION.SDK_INT < 31 || has("android.permission.BLUETOOTH_CONNECT"))) {
            car();
        }
        if (i == 5 && has("android.permission.CAMERA") && has("android.permission.RECORD_AUDIO")) {
            confirmRecord(this.recordDual, this.recordFront);
        }
        if (i == 6 && has("android.permission.RECORD_AUDIO")) {
            enableHandsFree();
        }
        if (i == 7 && has("android.permission.POST_NOTIFICATIONS")) {
            enableHandsFree();
        }
        if (i == 8 && has("android.permission.RECORD_AUDIO")) {
            startLive();
        }
        if (i != 9 || !has("android.permission.POST_NOTIFICATIONS")) {
            return;
        }
        startLive();
    }

    private void act(String str) {
        String strReplaceFirst = str.trim().replaceFirst("(?iu)^(?:hey\\s+)?jarvis[, ]+", "");
        String strLocal = LiveRepair.local(this, strReplaceFirst);
        if (strLocal != null) {
            say(strLocal);
            return;
        }
        String strNormalize = Actions.normalize(strReplaceFirst);
        if (strNormalize.equals("gönder")) {
            say(Messages.confirm(this), false);
            return;
        }
        if (strNormalize.equals("iptal") && !this.pendingTelegram) {
            say(Messages.cancel(), false);
            return;
        }
        if (telegram(strReplaceFirst)) {
            return;
        }
        if (strReplaceFirst.matches("(?isu)^evren(?:[\\s,:]+.*)?$")) {
            evrenChat(strReplaceFirst.replaceFirst("(?iu)^evren[\\s,:]*", "").trim());
            return;
        }
        if (Actions.intent(strReplaceFirst).equals("youtube_music")) {
            say(Actions.run(this, "youtube_music", Actions.songQuery(strReplaceFirst)));
            return;
        }
        if (Cloud.isSearch(strReplaceFirst)) {
            research(Cloud.searchQuery(strReplaceFirst));
            return;
        }
        if (!strNormalize.startsWith("yanıtla ") && !strNormalize.startsWith("cevap ver ")) {
            History.add(this, "Sen", strReplaceFirst);
        }
        String lowerCase = strReplaceFirst.toLowerCase(TR);
        if (lowerCase.equals("eve git") || lowerCase.equals("eve rota") || lowerCase.equals("eve dön")) {
            home();
            return;
        }
        if (lowerCase.equals("live modu aç") || lowerCase.equals("canlı sohbet aç")) {
            confirmLive();
            return;
        }
        if (lowerCase.equals("live modu kapat") || lowerCase.equals("canlı sohbeti kapat")) {
            LiveRepair.stop(this);
            stopService(new Intent(this, (Class<?>) GeminiLive.class));
            return;
        }
        if (!lowerCase.equals("sürüş modu")) {
            if (!lowerCase.equals("sürüşü bitir")) {
                if (!lowerCase.equals("kaydı durdur")) {
                    if (lowerCase.equals("son mesajı oku") || lowerCase.equals("mesajları oku")) {
                        String strText = prefs(this).getBoolean("driving", false) ? Messages.text() : null;
                        say(strText == null ? "Sürüşte alınan destekli bir mesaj yok." : strText.substring(0, Math.min(220, strText.length())), false);
                        return;
                    }
                    if (lowerCase.startsWith("yanıtla ") || lowerCase.startsWith("cevap ver ")) {
                        if (prefs(this).getBoolean("driving", false)) {
                            say(Messages.draft(this, strReplaceFirst.substring(lowerCase.startsWith("yanıtla ") ? 8 : 10).trim()), false);
                            return;
                        } else {
                            say("Sürüş modu kapalı.");
                            return;
                        }
                    }
                    if (lowerCase.startsWith("internette ara ")) {
                        if (!prefs(this).getBoolean("driving", false)) {
                            view("https://www.google.com/search?q=" + Uri.encode(strReplaceFirst.substring(15)));
                            return;
                        } else {
                            say("Ekranlı arama sürüş modunda kapalı.");
                            return;
                        }
                    }
                    if (!lowerCase.startsWith("ara ")) {
                        if (!lowerCase.endsWith(" ara")) {
                            if (lowerCase.equals("haberler") || lowerCase.equals("haberleri oku") || lowerCase.equals("haberleri aç")) {
                                brief(false);
                                return;
                            }
                            if (lowerCase.equals("kurlar") || lowerCase.equals("kurları söyle") || lowerCase.equals("kurları aç")) {
                                brief(true);
                                return;
                            }
                            if (!lowerCase.equals("sonraki şarkı")) {
                                if (!lowerCase.equals("müziği durdur")) {
                                    if (!lowerCase.equals("müziğe devam et")) {
                                        if (!strReplaceFirst.isEmpty()) {
                                            if (usingGemini()) {
                                                geminiChat(strReplaceFirst);
                                                return;
                                            } else {
                                                evrenChat(strReplaceFirst);
                                                return;
                                            }
                                        }
                                        return;
                                    }
                                    media(126);
                                    return;
                                }
                                media(127);
                                return;
                            }
                            media(87);
                            return;
                        }
                        call(strReplaceFirst.substring(0, strReplaceFirst.length() - 4));
                        return;
                    }
                    call(strReplaceFirst.substring(4));
                    return;
                }
                stopRecord();
                return;
            }
            endDrive();
            return;
        }
        drive();
    }

    private boolean usingGemini() {
        return prefs(this).getString("provider", "evren").equals("gemini");
    }

    private void provider() {
        if (driving()) {
            say("Sağlayıcı ayarı sürüşte kapalı.");
            return;
        }
        boolean zUsingGemini = usingGemini();
        new AlertDialog.Builder(this).setTitle("Varsayılan sohbet sağlayıcısı").setSingleChoiceItems(new String[]{"EVREN", "Gemini"}, zUsingGemini ? 1 : 0, new Main$$ExternalSyntheticLambda16(this)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$provider$21$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m85lambda$provider$21$localmertcanjarvisMain(DialogInterface dialogInterface, int i) {
        prefs(this).edit().putString("provider", i == 1 ? "gemini" : "evren").apply();
        dialogInterface.dismiss();
        recreate();
    }

    private void geminiChat(String str) {
        String str2 = Cloud.key;
        if (str2.isEmpty()) {
            say("Önce Gemini anahtarını gir.");
        } else {
            this.status.setText("Gemini yanıt hazırlıyor…");
            new Thread(new Main$$ExternalSyntheticLambda0(this, str, str2), "gemini-chat").start();
        }
    }

    /* JADX INFO: renamed from: lambda$geminiChat$23$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m65lambda$geminiChat$23$localmertcanjarvisMain(String str, String str2) {
        String strChat;
        try {
            strChat = Cloud.chat(str, str2);
        } catch (Exception e) {
            strChat = "Gemini yanıt vermedi; anahtarı ve bağlantıyı kontrol et.";
        }
        runOnUiThread(new Main$$ExternalSyntheticLambda24(this, strChat));
    }

    /* JADX INFO: renamed from: lambda$geminiChat$22$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m64lambda$geminiChat$22$localmertcanjarvisMain(String str) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        say(str);
    }

    private void media(int i) {
        this.audio.dispatchMediaKeyEvent(new KeyEvent(0, i));
        this.audio.dispatchMediaKeyEvent(new KeyEvent(1, i));
    }

    private void cloudKey() {
        if (driving()) {
            say("Anahtar ayarı sürüşte kapalı.");
            return;
        }
        EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setInputType(129);
        new AlertDialog.Builder(this).setTitle("Gemini API anahtarı").setMessage("Anahtar telefonda şifreli saklanır. Google özellikleri proje kotasına veya ücrete tabi olabilir.").setView(editText).setPositiveButton("Kaydet", new Main$$ExternalSyntheticLambda5(this, editText)).setNeutralButton("Unut", new Main$$ExternalSyntheticLambda6(this)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$cloudKey$24$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m45lambda$cloudKey$24$localmertcanjarvisMain(EditText editText, DialogInterface dialogInterface, int i) {
        saveSecret("gemini", editText.getText().toString().trim());
    }

    /* JADX INFO: renamed from: lambda$cloudKey$25$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m46lambda$cloudKey$25$localmertcanjarvisMain(DialogInterface dialogInterface, int i) {
        saveSecret("gemini", "");
    }

    private void evrenKey() {
        if (driving()) {
            say("Anahtar ayarı sürüşte kapalı.");
            return;
        }
        EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setInputType(129);
        new AlertDialog.Builder(this).setTitle("EVREN API anahtarı").setMessage("EVREN komutunda soru ve seslendirilecek yanıt EVREN'e gönderilir. Anahtar telefonda şifreli saklanır.").setView(editText).setPositiveButton("Kaydet", new Main$$ExternalSyntheticLambda14(this, editText)).setNeutralButton("Unut", new Main$$ExternalSyntheticLambda15(this)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$evrenKey$26$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m60lambda$evrenKey$26$localmertcanjarvisMain(EditText editText, DialogInterface dialogInterface, int i) {
        saveSecret("evren", editText.getText().toString().trim());
    }

    /* JADX INFO: renamed from: lambda$evrenKey$27$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m61lambda$evrenKey$27$localmertcanjarvisMain(DialogInterface dialogInterface, int i) {
        saveSecret("evren", "");
    }

    private void saveSecret(String str, String str2) {
        try {
            Secrets.save(this, str, str2);
            if (str.equals("gemini")) {
                Cloud.key = str2;
                if (str2.isEmpty()) {
                    stopService(new Intent(this, (Class<?>) GeminiLive.class));
                }
            } else {
                Evren.key = str2;
                if (str2.isEmpty()) {
                    stopEvren();
                }
            }
            this.status.setText(str2.isEmpty() ? "Anahtar unutuldu." : "Anahtar telefona şifreli kaydedildi.");
        } catch (Exception e) {
            this.status.setText("Anahtar şifreli saklanamadı; ayar değiştirilmedi.");
        }
    }

    private void evrenChat(String str) {
        if (str.isEmpty()) {
            say("EVREN'e sorunu da söyle.");
            return;
        }
        String str2 = Evren.key;
        if (str2.isEmpty()) {
            say("Önce EVREN API anahtarını gir.");
            return;
        }
        if (GeminiLive.active || VoiceDrive.active) {
            say("Önce etkin ses oturumunu kapat.");
            return;
        }
        stopEvren();
        int i = this.evrenGeneration;
        History.add(this, "Sen · EVREN", str);
        this.status.setText("EVREN yanıtını hazırlıyor…");
        new Thread(new Main$$ExternalSyntheticLambda60(this, str, str2, i), "evren-chat").start();
    }

    /* JADX INFO: renamed from: lambda$evrenChat$34$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m59lambda$evrenChat$34$localmertcanjarvisMain(String str, String str2, int i) {
        String strRun;
        File fileSpeech;
        try {
            String strIntent = Actions.intent(str);
            if (strIntent.isEmpty()) {
                strRun = Evren.answer(str.substring(0, Math.min(1500, str.length())), str2);
            } else {
                strRun = Actions.run(this, strIntent, strIntent.equals("youtube_music") ? Actions.songQuery(str) : "");
            }
            runOnUiThread(new Main$$ExternalSyntheticLambda70(this, i, strRun));
            try {
                fileSpeech = Evren.speech(this, strRun, str2);
            } catch (Exception e) {
                fileSpeech = null;
            }
            runOnUiThread(new Main$$ExternalSyntheticLambda71(this, i, fileSpeech, strRun));
        } catch (Exception e2) {
            runOnUiThread(new Main$$ExternalSyntheticLambda69(this, i, e2));
        }
    }

    /* JADX INFO: renamed from: lambda$evrenChat$28$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m53lambda$evrenChat$28$localmertcanjarvisMain(int i, Exception exc) {
        if (isDestroyed() || i != this.evrenGeneration) {
            return;
        }
        say(Evren.error(exc));
    }

    /* JADX INFO: renamed from: lambda$evrenChat$29$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m54lambda$evrenChat$29$localmertcanjarvisMain(int i, String str) {
        if (isDestroyed() || i != this.evrenGeneration) {
            return;
        }
        this.status.setText(str);
    }

    /* JADX INFO: renamed from: lambda$evrenChat$33$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m58lambda$evrenChat$33$localmertcanjarvisMain(int i, File file, String str) {
        if (isDestroyed() || i != this.evrenGeneration) {
            if (file != null) {
                file.delete();
                return;
            }
            return;
        }
        if (file == null) {
            say(str);
            return;
        }
        this.status.setText(str);
        History.add(this, "Jarvis · EVREN", str);
        try {
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                this.evrenPlayer = mediaPlayer;
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(1).build());
                FileInputStream fileInputStream = new FileInputStream(file);
                try {
                    mediaPlayer.setDataSource(fileInputStream.getFD());
                    fileInputStream.close();
                    mediaPlayer.setOnPreparedListener(new Main$$ExternalSyntheticLambda61(this));
                    mediaPlayer.setOnCompletionListener(new Main$$ExternalSyntheticLambda62(this));
                    mediaPlayer.setOnErrorListener(new Main$$ExternalSyntheticLambda63(this, str));
                    mediaPlayer.prepareAsync();
                } catch (Throwable th) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } catch (Exception e) {
                stopEvren();
                say(str, false);
            }
            file.delete();
        } catch (Throwable th3) {
            file.delete();
            throw th3;
        }
    }

    /* JADX INFO: renamed from: lambda$evrenChat$30$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m55lambda$evrenChat$30$localmertcanjarvisMain(MediaPlayer mediaPlayer) {
        if (this.evrenPlayer == mediaPlayer) {
            this.audio.requestAudioFocus(this.focus);
            mediaPlayer.start();
        }
    }

    /* JADX INFO: renamed from: lambda$evrenChat$31$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m56lambda$evrenChat$31$localmertcanjarvisMain(MediaPlayer mediaPlayer) {
        if (this.evrenPlayer == mediaPlayer) {
            stopEvren();
        }
    }

    /* JADX INFO: renamed from: lambda$evrenChat$32$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ boolean m57lambda$evrenChat$32$localmertcanjarvisMain(String str, MediaPlayer mediaPlayer, int i, int i2) {
        if (this.evrenPlayer != mediaPlayer) {
            return true;
        }
        stopEvren();
        say(str, false);
        return true;
    }

    private void stopEvren() {
        this.evrenGeneration++;
        if (this.evrenPlayer != null) {
            this.evrenPlayer.release();
            this.evrenPlayer = null;
            this.audio.abandonAudioFocusRequest(this.focus);
        }
    }

    void confirmGemini() {
        if (Cloud.key.isEmpty()) {
            say("Önce Gemini API anahtarını gir.");
        } else {
            new AlertDialog.Builder(this).setTitle("Gemini Live").setMessage("Beklerken Hey Jarvis ifadesini Android ses tanıma dinler. Cihaz içi tanıma varsa önceliklidir. Konuşma açılınca ses Google’a gönderilir. Sessiz ol diyerek beklemeye dönebilirsin.").setPositiveButton("Live başlat", new Main$$ExternalSyntheticLambda4(this)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
        }
    }

    /* JADX INFO: renamed from: lambda$confirmLive$35$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m47lambda$confirmLive$35$localmertcanjarvisMain(DialogInterface dialogInterface, int i) {
        startLive();
    }

    private void startLive() {
        LiveRepair.stop(this);
        stopEvren();
        if (Cloud.key.isEmpty()) {
            return;
        }
        if (!has("android.permission.RECORD_AUDIO")) {
            requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 8);
            return;
        }
        stopService(new Intent(this, (Class<?>) VoiceDrive.class));
        DriveCamera.yieldMicrophone(this);
        if (this.speech != null) {
            this.speech.cancel();
        }
        if (this.voice != null) {
            this.voice.stop();
        }
        this.audio.abandonAudioFocusRequest(this.focus);
        try {
            startForegroundService(new Intent(this, (Class<?>) GeminiLive.class));
            this.status.setText("Gemini Live durumu bildirimde.");
        } catch (RuntimeException e) {
            this.status.setText("Live açılamadı. Jarvis ekrandayken tekrar dene.");
        }
    }

    private void searchDialog() {
        if (driving()) {
            say("Sesle 'Google'da araştır' ve konuyu söyle.");
            return;
        }
        EditText editText = new EditText(this);
        editText.setHint("Neyi araştırayım?");
        new AlertDialog.Builder(this).setTitle("Google araştırması ve sesli özet").setView(editText).setPositiveButton("Araştır", new Main$$ExternalSyntheticLambda66(this, editText)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$searchDialog$36$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m90lambda$searchDialog$36$localmertcanjarvisMain(EditText editText, DialogInterface dialogInterface, int i) {
        research(editText.getText().toString().trim());
    }

    private void research(String str) {
        if (str.isEmpty()) {
            say("Araştırma konusunu da söyle.");
            return;
        }
        String str2 = Cloud.key;
        if (str2.isEmpty()) {
            say("Google araştırması için Gemini anahtarı gerekli.");
        } else {
            this.status.setText("Google'da araştırıyorum…");
            new Thread(new Main$$ExternalSyntheticLambda67(this, str, str2), "google-search").start();
        }
    }

    /* JADX INFO: renamed from: lambda$research$40$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m89lambda$research$40$localmertcanjarvisMain(String str, String str2) {
        try {
            runOnUiThread(new Main$$ExternalSyntheticLambda8(this, Cloud.research(str, str2)));
        } catch (Exception e) {
            runOnUiThread(new Main$$ExternalSyntheticLambda9(this, e));
        }
    }

    /* JADX INFO: renamed from: lambda$research$38$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m87lambda$research$38$localmertcanjarvisMain(Cloud.Result result) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        say(result.text);
        if (!driving()) {
            String[] strArr = (String[]) result.sources.keySet().toArray(new String[0]);
            new AlertDialog.Builder(this).setTitle("Araştırma kaynakları").setItems((String[]) result.sources.values().toArray(new String[0]), new Main$$ExternalSyntheticLambda22(this, strArr)).setNegativeButton("Kapat", (DialogInterface.OnClickListener) null).show();
        }
    }

    /* JADX INFO: renamed from: lambda$research$37$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m86lambda$research$37$localmertcanjarvisMain(String[] strArr, DialogInterface dialogInterface, int i) {
        view(strArr[i]);
    }

    /* JADX INFO: renamed from: lambda$research$39$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m88lambda$research$39$localmertcanjarvisMain(Exception exc) {
        if (isDestroyed()) {
            return;
        }
        say(((exc instanceof IOException) && exc.getMessage() != null && exc.getMessage().startsWith("Google")) ? exc.getMessage() : "Araştırma tamamlanamadı; ağ ve proje erişimini kontrol et.");
    }

    private void brief(boolean z) {
        this.status.setText(z ? "TCMB’ye bakıyorum…" : "Haber başlıklarına bakıyorum…");
        new Thread(new Main$$ExternalSyntheticLambda17(this, z), "brief").start();
    }

    /* synthetic */ void m43lambda$brief$42$localmertcanjarvisMain(boolean rates) {
        String result;
        try {
            result = rates ? Brief.rates() : Brief.news();
        } catch (Exception e) {
            result = rates ? "TCMB verisi şu anda alınamadı." : "Haber başlıkları şu anda alınamadı.";
        }
        runOnUiThread(new Main$$ExternalSyntheticLambda20(this, result));
    }

    /* JADX INFO: renamed from: lambda$brief$41$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m42lambda$brief$41$localmertcanjarvisMain(String str) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        say(str);
    }

    private void call(String str) {
        String strTrim = str.trim();
        if (strTrim.isEmpty()) {
            say("Kimin aranacağını söyle.");
            return;
        }
        String strReplaceAll = strTrim.replaceAll("[ ()-]", "");
        boolean z = false;
        if (!strReplaceAll.matches("\\+?[0-9]{3,15}")) {
            if (!has("android.permission.READ_CONTACTS")) {
                say("Kişiler izni gerekli.");
                return;
            }
            HashSet hashSet = new HashSet();
            Cursor cursorQuery = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{"display_name", "data1"}, null, null, null);
            if (cursorQuery != null) {
                while (cursorQuery.moveToNext()) {
                    try {
                        if (strTrim.equalsIgnoreCase(cursorQuery.getString(0)) && cursorQuery.getString(1) != null) {
                            hashSet.add(cursorQuery.getString(1).replaceAll("[ ()-]", ""));
                        }
                    } catch (Throwable th) {
                        if (cursorQuery != null) {
                            try {
                                cursorQuery.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            if (hashSet.size() != 1) {
                say("Tek bir numara bulamadım. Rehberdeki tam adı söyle.");
                return;
            }
            strReplaceAll = (String) hashSet.iterator().next();
        }
        if (!strReplaceAll.matches("\\+?[0-9]{3,15}")) {
            say("Numara geçersiz.");
            return;
        }
        if (has("android.permission.CALL_PHONE") && prefs(this).getBoolean("driving", false)) {
            z = true;
        }
        open(new Intent(z ? "android.intent.action.CALL" : "android.intent.action.DIAL", Uri.fromParts("tel", strReplaceAll, null)));
    }

    private void drive() {
        Messages.clear();
        prefs(this).edit().putBoolean("driving", true).apply();
        this.status.setText("Sürüş modu açık.");
        startHandsFreeIfReady();
        new Handler(Looper.getMainLooper()).postDelayed(new Main$$ExternalSyntheticLambda21(this), 900L);
    }

    /* JADX INFO: renamed from: lambda$drive$43$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m51lambda$drive$43$localmertcanjarvisMain() {
        if (isFinishing() || isDestroyed() || !driving()) {
            return;
        }
        home();
    }

    private boolean driving() {
        return prefs(this).getBoolean("driving", false);
    }

    private void endDrive() {
        prefs(this).edit().putBoolean("driving", false).apply();
        Messages.clear();
        stopRecord();
        stopService(new Intent(this, (Class<?>) VoiceDrive.class));
        stopService(new Intent(this, (Class<?>) GeminiLive.class));
        say("Sürüş modu kapalı.");
    }

    private void enableHandsFree() {
        if (!has("android.permission.RECORD_AUDIO")) {
            requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 6);
        } else if (Build.VERSION.SDK_INT < 33 || has("android.permission.POST_NOTIFICATIONS")) {
            startHandsFreeIfReady();
        } else {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 7);
        }
    }

    private void startHandsFreeIfReady() {
        if (GeminiLive.active) {
            this.status.setText("Gemini Live açık; yerel dinleyici başlatılmadı.");
            return;
        }
        if (!has("android.permission.RECORD_AUDIO") || (Build.VERSION.SDK_INT >= 33 && !has("android.permission.POST_NOTIFICATIONS"))) {
            this.status.setText("Eller serbest mod için mikrofon ve bildirim izinlerini ver.");
            return;
        }
        if (!LiveRepair.available(this)) {
            this.status.setText("Bu telefonda yerel ses tanıma bulunamadı. Bas-konuş kullanılabilir.");
            return;
        }
        stopEvren();
        DriveCamera.yieldMicrophone(this);
        if (this.speech != null) {
            this.speech.cancel();
        }
        if (this.voice != null) {
            this.voice.stop();
        }
        this.audio.abandonAudioFocusRequest(this.focus);
        try {
            startForegroundService(new Intent(this, (Class<?>) VoiceDrive.class));
            this.status.setText("Eller serbest dinleyici hazırlanıyor.");
        } catch (RuntimeException e) {
            this.status.setText("Dinleyici açılamadı. Uygulama ekrandayken yeniden dene.");
        }
    }

    private void history() {
        if (driving()) {
            this.status.setText("Geçmiş ekranı sürüşte kapalı.");
            return;
        }
        TextView textView = new TextView(this);
        textView.setPadding(24, 16, 24, 16);
        textView.setText(History.display(this));
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(textView);
        boolean z = prefs(this).getBoolean("saveHistory", true);
        new AlertDialog.Builder(this).setTitle("Telefondaki geçmiş").setView(scrollView).setPositiveButton("Kapat", (DialogInterface.OnClickListener) null).setNeutralButton(z ? "Kaydı kapat" : "Kaydı aç", new Main$$ExternalSyntheticLambda64(this, z)).setNegativeButton("Temizle", new Main$$ExternalSyntheticLambda65(this)).show();
    }

    /* JADX INFO: renamed from: lambda$history$44$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m66lambda$history$44$localmertcanjarvisMain(boolean z, DialogInterface dialogInterface, int i) {
        prefs(this).edit().putBoolean("saveHistory", !z).apply();
    }

    /* JADX INFO: renamed from: lambda$history$46$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m68lambda$history$46$localmertcanjarvisMain(DialogInterface dialogInterface, int i) {
        new AlertDialog.Builder(this).setTitle("Geçmiş silinsin mi?").setPositiveButton("Sil", new Main$$ExternalSyntheticLambda13(this)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$history$45$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m67lambda$history$45$localmertcanjarvisMain(DialogInterface dialogInterface, int i) {
        History.clear(this);
    }

    private void stopRecord() {
        stopService(new Intent(this, (Class<?>) Dashcam.class));
    }

    private void cameraCheck() {
        try {
            boolean z = true;
            if (Dashcam.cameras((CameraManager) getSystemService(CameraManager.class), true).isEmpty()) {
                z = false;
            }
            say(Build.MANUFACTURER + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE + (z ? " · Sistem eşzamanlı ön ve arka kamerayı bildiriyor." : " · Sistem eşzamanlı ön ve arka kamera desteği bildirmiyor."));
        } catch (Exception e) {
            say("Kamera bilgisi okunamadı.");
        }
    }

    private void record(boolean z, boolean z2) {
        if (DriveCamera.requestGpsBeforeRecording(this)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && !has("android.permission.POST_NOTIFICATIONS")) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 3);
            say("Kayıt bildiriminin görünmesi için izin ver, sonra tekrar dokun.");
            return;
        }
        this.recordDual = z;
        this.recordFront = z2;
        if (!has("android.permission.CAMERA") || !has("android.permission.RECORD_AUDIO")) {
            requestPermissions(new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"}, 5);
        } else {
            confirmRecord(z, z2);
        }
    }

    private void confirmRecord(boolean z, boolean z2) {
        String str;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (z) {
            str = "Çift kamera kaydı";
        } else {
            str = z2 ? "Ön kamera kaydı" : "Arka kamera kaydı";
        }
        builder.setTitle(str).setMessage("Sesli kayıt, eller serbest dinlemeyi ve Gemini Live'ı durdurur. Dosyalar cihazda kalır ve yedi gün sonra temizlenir. Yolcuları bilgilendir; ayarları park hâlindeyken yap.").setPositiveButton("Kaydı başlat", new Main$$ExternalSyntheticLambda18(this, z, z2)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$confirmRecord$47$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m48lambda$confirmRecord$47$localmertcanjarvisMain(boolean z, boolean z2, DialogInterface dialogInterface, int i) {
        int rotation = this.physicalDegrees == -1 ? getWindowManager().getDefaultDisplay().getRotation() * 90 : (360 - this.physicalDegrees) % 360;
        stopService(new Intent(this, (Class<?>) VoiceDrive.class));
        stopService(new Intent(this, (Class<?>) GeminiLive.class));
        try {
            startForegroundService(new Intent(this, (Class<?>) Dashcam.class).putExtra("sound", true).putExtra("dual", z).putExtra("front", z2).putExtra("displayDegrees", rotation));
        } catch (RuntimeException e) {
            say("Kayıt başlatılamadı.");
        }
    }

    private void exportRecording() {
        if (driving()) {
            say("Galeriye aktarma sürüş modunda kapalı.");
            return;
        }
        if (Build.VERSION.SDK_INT < 29) {
            say("Galeriye aktarma Android 10 gerektirir.");
            return;
        }
        File[] fileArrListFiles = Retention.directory(this).listFiles(new Main$$ExternalSyntheticLambda10());
        if (fileArrListFiles == null || fileArrListFiles.length == 0) {
            say("Tamamlanmış kayıt yok.");
            return;
        }
        Arrays.sort(fileArrListFiles, new Main$$ExternalSyntheticLambda11());
        String[] strArr = new String[fileArrListFiles.length];
        for (int i = 0; i < fileArrListFiles.length; i++) {
            strArr[i] = new SimpleDateFormat("dd.MM HH:mm", TR).format(new Date(fileArrListFiles[i].lastModified())) + (namesForFront(fileArrListFiles[i]) ? " • Ön" : " • Arka");
        }
        new AlertDialog.Builder(this).setTitle("Kalıcı galeri kopyasını seç").setItems(strArr, new Main$$ExternalSyntheticLambda12(this, fileArrListFiles)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ boolean lambda$exportRecording$48(File file, String str) {
        return str.endsWith(".mp4");
    }

    static /* synthetic */ int lambda$exportRecording$49(File file, File file2) {
        return file2.getName().compareTo(file.getName());
    }

    /* JADX INFO: renamed from: lambda$exportRecording$50$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m62lambda$exportRecording$50$localmertcanjarvisMain(File[] fileArr, int i) {
        copyRecording(fileArr[i]);
    }

    /* JADX INFO: renamed from: lambda$exportRecording$51$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m63lambda$exportRecording$51$localmertcanjarvisMain(File[] fileArr, DialogInterface dialogInterface, int i) {
        new Thread(new Main$$ExternalSyntheticLambda68(this, fileArr, i), "export").start();
    }

    private boolean namesForFront(File file) {
        return file.getName().endsWith("-on.mp4");
    }

    private void copyRecording(File file) {
        Uri uriInsert;
        boolean z = false;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", "Jarvis-" + file.getName());
            contentValues.put("mime_type", "video/mp4");
            contentValues.put("relative_path", "Movies/Jarvis");
            contentValues.put("is_pending", (Integer) 1);
            uriInsert = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                if (uriInsert == null) {
                    throw new IOException();
                }
                FileInputStream fileInputStream = new FileInputStream(file);
                try {
                    OutputStream outputStreamOpenOutputStream = getContentResolver().openOutputStream(uriInsert);
                    try {
                        if (outputStreamOpenOutputStream == null) {
                            throw new IOException();
                        }
                        byte[] bArr = new byte[65536];
                        while (true) {
                            int i = fileInputStream.read(bArr);
                            if (i == -1) {
                                break;
                            } else {
                                outputStreamOpenOutputStream.write(bArr, 0, i);
                            }
                        }
                        if (outputStreamOpenOutputStream != null) {
                            outputStreamOpenOutputStream.close();
                        }
                        fileInputStream.close();
                        contentValues.clear();
                        contentValues.put("is_pending", (Integer) 0);
                        if (getContentResolver().update(uriInsert, contentValues, null, null) != 1) {
                            throw new IOException();
                        }
                        z = true;
                        runOnUiThread(new Main$$ExternalSyntheticLambda7(this, z));
                    } catch (Throwable th) {
                        if (outputStreamOpenOutputStream != null) {
                            try {
                                outputStreamOpenOutputStream.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                    throw th3;
                }
            } catch (Exception e) {
                if (uriInsert != null) {
                    try {
                        getContentResolver().delete(uriInsert, null, null);
                    } catch (Exception e2) {
                    }
                }
                runOnUiThread(new Main$$ExternalSyntheticLambda7(this, z));
            }
        } catch (Exception e3) {
            uriInsert = null;
        }
    }

    /* JADX INFO: renamed from: lambda$copyRecording$52$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m49lambda$copyRecording$52$localmertcanjarvisMain(boolean z) {
        if (isDestroyed()) {
            return;
        }
        say(z ? "Galeriye kalıcı kopya aktarıldı." : "Kayıt aktarılamadı.");
    }

    private void termux() {
        if (driving()) {
            say("Termux sürüş modunda kapalı.");
            return;
        }
        Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage("com.termux");
        if (launchIntentForPackage == null) {
            say("Termux kurulu değil.");
        } else {
            open(launchIntentForPackage);
        }
    }

    private void home() {
        if (Build.VERSION.SDK_INT >= 28 && !((LocationManager) getSystemService(LocationManager.class)).isLocationEnabled()) {
            this.pendingHome = true;
            say("Konumu açınca rotayı başlatacağım.");
            startActivityForResult(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"), 12);
            return;
        }
        launchHome();
    }

    private void launchHome() {
        String strTrim = prefs(this).getString("home", "").trim();
        if (strTrim.isEmpty()) {
            Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
            if (launchIntentForPackage == null) {
                say("Google Maps kurulu değil. Jarvis'e ev adresini ekleyebilirsin.");
                return;
            } else {
                say("Google Maps açılıyor. Kayıtlı Ev kısayolunu seç.");
                open(launchIntentForPackage);
                return;
            }
        }
        view("https://www.google.com/maps/dir/?api=1&travelmode=driving&dir_action=navigate&destination=" + Uri.encode(strTrim));
    }

    private void settingHome() {
        EditText editText = new EditText(this);
        editText.setText(prefs(this).getString("home", ""));
        editText.setHint("Adres veya koordinatlar");
        new AlertDialog.Builder(this).setTitle("Ev adresi").setMessage("Google Maps'teki özel Ev adresini Jarvis okuyamaz. Otomatik rota için aynı adresi bir kez buraya kaydet.").setView(editText).setPositiveButton("Kaydet", new Main$$ExternalSyntheticLambda2(this, editText)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$settingHome$53$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m91lambda$settingHome$53$localmertcanjarvisMain(EditText editText, DialogInterface dialogInterface, int i) {
        prefs(this).edit().putString("home", editText.getText().toString().trim()).apply();
    }

    private void car() {
        if (Build.VERSION.SDK_INT >= 31 && !has("android.permission.BLUETOOTH_CONNECT")) {
            requestPermissions(new String[]{"android.permission.BLUETOOTH_CONNECT"}, 2);
            return;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BluetoothManager.class);
        BluetoothAdapter adapter = bluetoothManager == null ? null : bluetoothManager.getAdapter();
        List listEmptyList = adapter == null ? Collections.emptyList() : new ArrayList(adapter.getBondedDevices());
        if (listEmptyList.isEmpty()) {
            say("Önce aracı telefonun Bluetooth ayarlarında eşleştir.");
            return;
        }
        String[] strArr = new String[listEmptyList.size()];
        for (int i = 0; i < listEmptyList.size(); i++) {
            strArr[i] = String.valueOf(((BluetoothDevice) listEmptyList.get(i)).getName());
        }
        new AlertDialog.Builder(this).setTitle("Aracı seç").setItems(strArr, new Main$$ExternalSyntheticLambda19(this, listEmptyList)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$car$54$local-mertcan-jarvis-Main, reason: not valid java name */
    /* synthetic */ void m44lambda$car$54$localmertcanjarvisMain(List list, DialogInterface dialogInterface, int i) {
        prefs(this).edit().putString("car", ((BluetoothDevice) list.get(i)).getAddress()).apply();
        say("Araç kaydedildi.");
        if (Build.VERSION.SDK_INT < 33 || has("android.permission.POST_NOTIFICATIONS")) {
            return;
        }
        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 3);
    }

    @Override // android.speech.RecognitionListener
    public void onResults(Bundle bundle) {
        ArrayList<String> stringArrayList = bundle.getStringArrayList("results_recognition");
        if (stringArrayList == null || stringArrayList.isEmpty()) {
            return;
        }
        act(stringArrayList.get(0));
    }

    @Override // android.speech.RecognitionListener
    public void onError(int i) {
        this.status.setText("Ses tanınamadı (" + i + "). Yeniden deneyebilirsin.");
    }

    @Override // android.speech.RecognitionListener
    public void onReadyForSpeech(Bundle bundle) {
    }

    @Override // android.speech.RecognitionListener
    public void onBeginningOfSpeech() {
    }

    @Override // android.speech.RecognitionListener
    public void onRmsChanged(float f) {
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

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("homePending", this.pendingHome);
        bundle.putBoolean("telegramPending", this.pendingTelegram);
        bundle.putString("telegramRecipient", this.telegramRecipient);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 12 && this.pendingHome) {
            this.pendingHome = false;
            if (Build.VERSION.SDK_INT < 28 || !((LocationManager) getSystemService(LocationManager.class)).isLocationEnabled()) {
                this.status.setText("Konum kapalı; rota başlatılmadı.");
            } else {
                launchHome();
            }
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        GeminiAccess.foreground(this, true);
        super.onResume();
        visible = this;
        if (this.recordingOrientation.canDetectOrientation()) {
            this.recordingOrientation.enable();
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        GeminiAccess.foreground(this, false);
        EvrenTalk.suspend(this);
        DriveDashboard.suspend(this);
        if (visible == this) {
            visible = null;
        }
        this.recordingOrientation.disable();
        super.onPause();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        stopEvren();
        unregisterReceiver(this.recordStatus);
        if (this.speech != null) {
            this.speech.destroy();
        }
        if (this.voice != null) {
            this.voice.shutdown();
        }
        this.audio.abandonAudioFocusRequest(this.focus);
        super.onDestroy();
    }
}
