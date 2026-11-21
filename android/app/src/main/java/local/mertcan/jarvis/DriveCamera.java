package local.mertcan.jarvis;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
final class DriveCamera implements LocationListener {
    private static final String ASKED_GPS = "asked_gps_v093";
    private static final int BREAK_ID = 188;
    private static final long BREAK_MS = 3600000;
    private static final int SPEED_ID = 187;
    private static final long STALE_MS = 5000;
    private static final String STOP = "local.mertcan.jarvis.STOP_RECORDING";
    private static final String STORE = "drive_camera_settings";
    private long lastFix;
    private float lastSpeed;
    private final LocationManager locationManager;
    private long movingTime;
    private final NotificationManager notifications;
    private static Dashcam recording;
    private static boolean recordingWithSound;
    private final Dashcam service;
    private static DriveCamera speedMonitor;
    private long stillSince;
    private final TrafficMonitor traffic;
    private boolean wasMoving;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ExecutorService FILE_WORK = Executors.newSingleThreadExecutor();
    private final Runnable stale = new DriveCamera$$ExternalSyntheticLambda8(this);

    public static /* synthetic */ void $r8$lambda$YPvnl9Su_6i9pCIJ6Xv4mHaRpps(DriveCamera driveCamera) {
        driveCamera.unavailable();
    }

    @Override // android.location.LocationListener
    public void onStatusChanged(String str, int i, Bundle bundle) {
    }

    private DriveCamera(Dashcam dashcam) {
        this.service = dashcam;
        this.locationManager = (LocationManager) dashcam.getSystemService(LocationManager.class);
        this.notifications = (NotificationManager) dashcam.getSystemService(NotificationManager.class);
        this.traffic = new TrafficMonitor(dashcam);
    }

    static /* synthetic */ void lambda$panelAction$0(Main main) {
        panel(main);
    }

    static Runnable panelAction(Main main) {
        return new DriveCamera$$ExternalSyntheticLambda12(main);
    }

    private static void panel(Main main) {
        new AlertDialog.Builder(main).setTitle("Sürüş kamerası").setItems(new String[]{"Arka kamera · sürüş ekranı", "Ön kamera · sessiz kayıt", "Ön + arka · sessiz kayıt", "GPS hızı · ".concat(has(main, "android.permission.ACCESS_FINE_LOCATION") ? "İzin verildi" : "Konum izni ver"), "Son kaydı galeriye koru", "Kayıttan kareleri EVREN ile incele", "Trafik ve sabit kamera ayarları"}, new DriveCamera$$ExternalSyntheticLambda9(main)).setNegativeButton("Kapat", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$panel$1(Main main, DialogInterface dialogInterface, int i) {
        if (i == 0) {
            DriveDashboard.open(main);
        }
        if (i == 1 || i == 2) {
            startSilent(main, i == 1, i == 2);
        }
        if (i == 3) {
            gpsPermission(main);
        }
        if (i == 4) {
            pinLatest(main);
        }
        if (i == 5) {
            selectClip(main);
        }
        if (i == 6) {
            TrafficMonitor.settings(main);
        }
    }

    private static void gpsPermission(Main main) {
        if (!has(main, "android.permission.ACCESS_FINE_LOCATION")) {
            main.requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 189);
        } else {
            toast(main, "GPS, her seyir kamerası kaydında otomatik başlar.");
        }
    }

    static boolean requestGpsBeforeRecording(Main main) {
        SharedPreferences sharedPreferences = main.getSharedPreferences(STORE, 0);
        if (has(main, "android.permission.ACCESS_FINE_LOCATION") || sharedPreferences.getBoolean(ASKED_GPS, false)) {
            return false;
        }
        sharedPreferences.edit().putBoolean(ASKED_GPS, true).apply();
        gpsPermission(main);
        toast(main, "GPS için kesin konum iznini ver, ardından kaydı başlat.");
        return true;
    }

    static String trafficSummary() {
        DriveCamera driveCamera = speedMonitor;
        return driveCamera == null ? "Trafiği öğrenmek için GPS izniyle seyir kamerasını başlat." : driveCamera.traffic.summary();
    }

    static String radarSummary() {
        DriveCamera driveCamera = speedMonitor;
        return driveCamera == null ? "Sabit kamera sorgusu için GPS izniyle seyir kamerasını başlat." : driveCamera.traffic.radarSummary();
    }

    static void updateTrafficKey(String str) {
        DriveCamera driveCamera = speedMonitor;
        if (driveCamera != null) {
            driveCamera.traffic.setKey(str);
        }
    }

    private static void startSilent(Main main, boolean z, boolean z2) {
        if (!has(main, "android.permission.CAMERA")) {
            main.requestPermissions(new String[]{"android.permission.CAMERA"}, 190);
            toast(main, "Kamera izninden sonra kaydı tekrar seç.");
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && !has(main, "android.permission.POST_NOTIFICATIONS")) {
            main.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 191);
            toast(main, "Bildirim izninden sonra kaydı tekrar seç.");
        } else if (recording != null) {
            toast(main, "Önce bildirimden mevcut kaydı durdur.");
        } else {
            if (requestGpsBeforeRecording(main)) {
                return;
            }
            try {
                main.startForegroundService(new Intent(main, (Class<?>) Dashcam.class).putExtra("sound", false).putExtra("front", z).putExtra("dual", z2).putExtra("displayDegrees", main.getWindowManager().getDefaultDisplay().getRotation() * 90));
            } catch (RuntimeException unused) {
                toast(main, "Kamera başlatılamadı. JARVIS ekranı açıkken tekrar dene.");
            }
        }
    }

    static int foregroundTypes(Dashcam dashcam, int i) {
        return !has(dashcam, "android.permission.ACCESS_FINE_LOCATION") ? i : i | 8;
    }

    static void register(Dashcam dashcam, boolean z) {
        recording = dashcam;
        recordingWithSound = z;
        if ((foregroundTypes(dashcam, 0) & 8) == 0) {
            return;
        }
        DriveCamera driveCamera = new DriveCamera(dashcam);
        speedMonitor = driveCamera;
        driveCamera.start();
    }

    static boolean isRecording() {
        return recording != null;
    }

    static void unregister(Dashcam dashcam) {
        if (recording != dashcam) {
            return;
        }
        recording = null;
        recordingWithSound = false;
        DriveCamera driveCamera = speedMonitor;
        if (driveCamera != null) {
            driveCamera.close();
        }
        speedMonitor = null;
    }

    static void yieldMicrophone(Context context) {
        if (recording == null || !recordingWithSound) {
            return;
        }
        context.stopService(new Intent(context, (Class<?>) Dashcam.class));
    }

    static String voiceSummary(boolean z) {
        DriveCamera driveCamera = speedMonitor;
        if (driveCamera == null || driveCamera.lastFix == 0 || SystemClock.elapsedRealtime() - driveCamera.lastFix > STALE_MS) {
            return "Güncel GPS hızı yok. Sürüş kamerasında konum izni ve GPS açık olmalı.";
        }
        int iRound = Math.round(driveCamera.lastSpeed * 3.6f);
        if (!z) {
            return "GPS hızı saatte yaklaşık " + iRound + " kilometre.";
        }
        if (iRound == 0) {
            return "GPS'e göre araç duruyor.";
        }
        return "Bu hızda iki saniyede yaklaşık " + Math.round(driveCamera.lastSpeed * 2.0f) + " metre ilerlersin. Bu bir mesafe referansı.";
    }

    private void start() {
        this.notifications.createNotificationChannel(new NotificationChannel("dashcam-speed", "GPS hızı", 2));
        this.notifications.createNotificationChannel(new NotificationChannel("dashcam-break", "Mola hatırlatıcı", 3));
        LocationManager locationManager = this.locationManager;
        if (locationManager == null) {
            return;
        }
        try {
            locationManager.requestLocationUpdates("gps", 1000L, 0.0f, this, Looper.getMainLooper());
            speedStatus("GPS hızı bekleniyor.");
        } catch (RuntimeException unused) {
            speedStatus("GPS hızı alınamıyor.");
        }
    }

    private void close() {
        MAIN.removeCallbacks(this.stale);
        this.traffic.close();
        LocationManager locationManager = this.locationManager;
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (RuntimeException unused) {
            }
        }
        this.notifications.cancel(SPEED_ID);
        this.notifications.cancel(BREAK_ID);
    }

    @Override // android.location.LocationListener
    public void onLocationChanged(Location location) {
        if (speedMonitor != this) {
            return;
        }
        Handler handler = MAIN;
        handler.removeCallbacks(this.stale);
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        long elapsedRealtimeNanos = jElapsedRealtime - (location.getElapsedRealtimeNanos() / 1000000);
        if (elapsedRealtimeNanos >= 0 && elapsedRealtimeNanos <= STALE_MS && location.hasAccuracy() && location.getAccuracy() <= 75.0f) {
            DriveDashboard.position(location.getLatitude(), location.getLongitude());
        }
        if (!location.hasSpeed() || elapsedRealtimeNanos > STALE_MS || elapsedRealtimeNanos < 0 || !Float.isFinite(location.getSpeed()) || location.getSpeed() < 0.0f || (location.hasSpeedAccuracy() && location.getSpeedAccuracyMetersPerSecond() > 2.0f)) {
            unavailable();
            return;
        }
        float speed = location.getSpeed();
        boolean z = speed >= 2.8f;
        if (z && this.wasMoving) {
            long j = this.lastFix;
            if (j > 0 && jElapsedRealtime - j <= STALE_MS) {
                this.movingTime += jElapsedRealtime - j;
            }
        }
        if (z) {
            long j2 = this.stillSince;
            if (j2 > 0 && jElapsedRealtime - j2 >= 600000) {
                this.movingTime = 0L;
            }
            this.stillSince = 0L;
        } else if (this.stillSince == 0) {
            this.stillSince = jElapsedRealtime;
        }
        this.wasMoving = z;
        this.lastFix = jElapsedRealtime;
        this.lastSpeed = speed;
        int iRound = Math.round(3.6f * speed);
        int iRound2 = Math.round(speed * 2.0f);
        DriveDashboard.gps(iRound, iRound2, this.traffic.currentTraffic());
        this.traffic.onFix(location);
        speedStatus(iRound + " km/sa · 2 saniyede yaklaşık " + iRound2 + " m");
        handler.postDelayed(this.stale, STALE_MS);
        if (this.movingTime >= BREAK_MS) {
            this.movingTime = 0L;
            this.notifications.notify(BREAK_ID, new Notification.Builder(this.service, "dashcam-break").setSmallIcon(R.drawable.ic_dialog_info).setContentTitle("Mola hatırlatıcı").setContentText("Yaklaşık bir saattir hareket hâlindesin. Uygun bir yerde mola ver.").setAutoCancel(true).build());
        }
    }

    private void unavailable() {
        DriveDashboard.gpsUnavailable();
        this.lastFix = 0L;
        this.wasMoving = false;
        this.stillSince = 0L;
        speedStatus("GPS hızı alınamıyor.");
    }

    @Override // android.location.LocationListener
    public void onProviderDisabled(String str) {
        unavailable();
    }

    @Override // android.location.LocationListener
    public void onProviderEnabled(String str) {
        speedStatus("GPS hızı bekleniyor.");
    }

    private void speedStatus(String str) {
        String str2;
        PendingIntent service = PendingIntent.getService(this.service, SPEED_ID, new Intent(this.service, (Class<?>) Dashcam.class).setAction(STOP), 67108864);
        String strCurrentTraffic = this.traffic.currentTraffic();
        if (strCurrentTraffic.isEmpty()) {
            str2 = str;
        } else {
            str2 = str + "\n" + strCurrentTraffic;
        }
        this.notifications.notify(SPEED_ID, new Notification.Builder(this.service, "dashcam-speed").setSmallIcon(R.drawable.ic_menu_compass).setContentTitle("JARVIS · GPS göstergesi").setContentText(str).setStyle(new Notification.BigTextStyle().bigText(str2)).setOngoing(true).setOnlyAlertOnce(true).addAction(new Notification.Action.Builder((Icon) null, "Kaydı durdur", service).build()).build());
    }

    private static File[] clips(Context context) {
        File[] fileArrListFiles = Retention.directory(context).listFiles(new DriveCamera$$ExternalSyntheticLambda2());
        if (fileArrListFiles == null) {
            return new File[0];
        }
        Arrays.sort(fileArrListFiles, Comparator.comparingLong(new DriveCamera$$ExternalSyntheticLambda3()).reversed().thenComparing(new DriveCamera$$ExternalSyntheticLambda4()));
        return fileArrListFiles;
    }

    static /* synthetic */ boolean lambda$clips$2(File file, String str) {
        return str.endsWith(".mp4");
    }

    private static void pinLatest(Main main) {
        File[] fileArrClips = clips(main);
        if (fileArrClips.length == 0) {
            toast(main, "Önce bir kayıt bölümünü tamamla veya kaydı durdur.");
        } else if (Build.VERSION.SDK_INT < 29) {
            toast(main, "Bu seçenek Android 10 ve sonrasını gerektirir.");
        } else {
            FILE_WORK.execute(new DriveCamera$$ExternalSyntheticLambda6(fileArrClips, main.getApplicationContext()));
        }
    }

    static /* synthetic */ void lambda$pinLatest$5(File[] fileArr, Context context) {
        Uri uriInsert;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", "Jarvis-" + fileArr[0].getName());
            contentValues.put("mime_type", "video/mp4");
            contentValues.put("relative_path", "Movies/Jarvis");
            contentValues.put("is_pending", (Integer) 1);
            uriInsert = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                if (uriInsert == null) {
                    throw new IOException();
                }
                OutputStream outputStreamOpenOutputStream = context.getContentResolver().openOutputStream(uriInsert);
                try {
                    if (outputStreamOpenOutputStream == null) {
                        throw new IOException();
                    }
                    Files.copy(fileArr[0].toPath(), outputStreamOpenOutputStream);
                    if (outputStreamOpenOutputStream != null) {
                        outputStreamOpenOutputStream.close();
                    }
                    contentValues.clear();
                    contentValues.put("is_pending", (Integer) 0);
                    if (context.getContentResolver().update(uriInsert, contentValues, null, null) != 1) {
                        throw new IOException();
                    }
                    MAIN.post(new DriveCamera$$ExternalSyntheticLambda0(context));
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
            } catch (Exception unused) {
                if (uriInsert != null) {
                    try {
                        context.getContentResolver().delete(uriInsert, null, null);
                    } catch (RuntimeException unused2) {
                    }
                }
                MAIN.post(new DriveCamera$$ExternalSyntheticLambda1(context));
            }
        } catch (Exception unused3) {
            uriInsert = null;
        }
    }

    static /* synthetic */ void lambda$pinLatest$3(Context context) {
        toast(context, "Son tamamlanan kayıt Movies/Jarvis klasörüne kopyalandı.");
    }

    static /* synthetic */ void lambda$pinLatest$4(Context context) {
        toast(context, "Kayıt galeriye kopyalanamadı.");
    }

    private static void selectClip(Main main) {
        if (recording != null) {
            toast(main, "Kare incelemesi için park hâlindeyken kaydı durdur.");
            return;
        }
        if (Evren.key.isEmpty()) {
            toast(main, "EVREN anahtarı yüklenemedi.");
            return;
        }
        File[] fileArrClips = clips(main);
        if (fileArrClips.length == 0) {
            toast(main, "İncelenecek tamamlanmış kayıt yok.");
            return;
        }
        int iMin = Math.min(fileArrClips.length, 15);
        String[] strArr = new String[iMin];
        for (int i = 0; i < iMin; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(isFront(fileArrClips[i]) ? "Ön kamera" : "Arka kamera");
            sb.append(" · ");
            sb.append(DateFormat.getDateTimeInstance(3, 3, Locale.forLanguageTag("tr")).format(new Date(fileArrClips[i].lastModified())));
            strArr[i] = sb.toString();
        }
        new AlertDialog.Builder(main).setTitle("İncelenecek kaydı seç").setItems(strArr, new DriveCamera$$ExternalSyntheticLambda5(main, fileArrClips)).setNegativeButton("Kapat", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$selectClip$7(Main main, File[] fileArr, DialogInterface dialogInterface, int i) {
        new AlertDialog.Builder(main).setTitle("Üç kareyi EVREN'e gönder").setMessage("Videodan başlangıç, orta ve sona yakın üç görüntü seçilir. Karelerde yüz veya plaka bulunabilir. Videonun sesi gönderilmez.").setPositiveButton("Gönder ve incele", new DriveCamera$$ExternalSyntheticLambda10(main, fileArr, i)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$selectClip$6(Main main, File[] fileArr, int i, DialogInterface dialogInterface, int i2) {
        analyze(main, fileArr[i]);
    }

    private static boolean isFront(File file) {
        return file.getName().endsWith("-on.mp4");
    }

    private static void analyze(Activity activity, File file) {
        if (recording != null) {
            return;
        }
        FILE_WORK.execute(new DriveCamera$$ExternalSyntheticLambda7(file, Evren.key, activity, new AlertDialog.Builder(activity).setTitle("EVREN kare incelemesi").setMessage("Üç kare hazırlanıyor ve inceleniyor…").setCancelable(false).setNegativeButton("Kapat", (DialogInterface.OnClickListener) null).show()));
    }

    static /* synthetic */ void lambda$analyze$9(File file, String str, Activity activity, AlertDialog alertDialog) {
        String strInspectClip;
        try {
            strInspectClip = inspectClip(file, str);
        } catch (Exception unused) {
            strInspectClip = "Kareler incelenemedi. Kaydı ve EVREN bağlantısını kontrol et.";
        }
        MAIN.post(new DriveCamera$$ExternalSyntheticLambda11(activity, alertDialog, strInspectClip));
    }

    static /* synthetic */ void lambda$analyze$8(Activity activity, AlertDialog alertDialog, String str) {
        if (activity.isFinishing() || activity.isDestroyed() || !alertDialog.isShowing()) {
            return;
        }
        alertDialog.dismiss();
        new AlertDialog.Builder(activity).setTitle("EVREN · Kayıt incelemesi").setMessage(str).setPositiveButton("Kapat", (DialogInterface.OnClickListener) null).show();
    }

    private static String inspectClip(File file, String str) throws Exception {
        String str2;
        Bitmap frameAtTime;
        JSONArray jSONArray = new JSONArray();
        if (isFront(file)) {
            str2 = "Üç kareyi sırayla ve Türkçe incele. Yüz ve gözlerin görünürlüğünü, baş pozisyonunu ve kadrajı anlat. Uyku, sağlık veya dikkat teşhisi yapma. Belirsiz görüntülerde bunu belirt.";
        } else {
            str2 = "Üç kareyi sırayla ve Türkçe incele. Görülebilen trafik lambalarının rengini, şeritleri, araçları ve görüş koşullarını anlat. Hangi şeride ait olduğu belirsiz ışıkları belirt. Metre tahmini veya sürüş talimatı verme.";
        }
        jSONArray.put(new JSONObject().put("type", "text").put("text", str2));
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
            long j = Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
            if (j <= 0) {
                throw new IOException();
            }
            int[] iArr = {15, 50, 85};
            for (int i = 0; i < 3; i++) {
                long j2 = (((long) iArr[i]) * j) / 100;
                if (Build.VERSION.SDK_INT >= 27) {
                    frameAtTime = mediaMetadataRetriever.getScaledFrameAtTime(j2 * 1000, 3, 640, 480);
                } else {
                    frameAtTime = mediaMetadataRetriever.getFrameAtTime(j2 * 1000, 3);
                }
                if (frameAtTime == null) {
                    throw new IOException();
                }
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if (!frameAtTime.compress(Bitmap.CompressFormat.JPEG, 82, byteArrayOutputStream)) {
                        throw new IOException();
                    }
                    String str3 = "data:image/jpeg;base64," + Base64.encodeToString(byteArrayOutputStream.toByteArray(), 2);
                    jSONArray.put(new JSONObject().put("type", "text").put("text", "Kayıt saniyesi: " + (j2 / 1000)));
                    jSONArray.put(new JSONObject().put("type", "image_url").put("image_url", new JSONObject().put("url", str3)));
                    frameAtTime.recycle();
                } catch (Throwable th) {
                    frameAtTime.recycle();
                    throw th;
                }
            }
            mediaMetadataRetriever.release();
            return requestVision(new JSONObject().put("model", "qwen3-vl-30b").put("max_tokens", 420).put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", jSONArray))), str);
        } catch (Throwable th2) {
            mediaMetadataRetriever.release();
            throw th2;
        }
    }

    static String describeRoad(Bitmap bitmap) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return requestVision(new JSONObject().put("model", "qwen3-vl-30b").put("max_tokens", 160).put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", new JSONArray().put(new JSONObject().put("type", "text").put("text", "Arka kameradan alınan tek karede gördüklerini Türkçe, en fazla iki kısa cümlede anlat. Yol, araç ve görünür ışıklar. Kesin olmayanı belirt. Sürüş talimatı veya metre tahmini verme.")).put(new JSONObject().put("type", "image_url").put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + Base64.encodeToString(byteArrayOutputStream.toByteArray(), 2))))))), Evren.key);
    }

    private static String requestVision(JSONObject jSONObject, String str) throws Exception {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://evren-llmapi.ssyz.org.tr/v1/chat/completions").openConnection();
        httpsURLConnection.setInstanceFollowRedirects(false);
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setConnectTimeout(10000);
        httpsURLConnection.setReadTimeout(45000);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + str);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        byte[] bytes = jSONObject.toString().getBytes(StandardCharsets.UTF_8);
        httpsURLConnection.setFixedLengthStreamingMode(bytes.length);
        try {
            OutputStream outputStream = httpsURLConnection.getOutputStream();
            try {
                outputStream.write(bytes);
                if (outputStream != null) {
                    outputStream.close();
                }
                if (httpsURLConnection.getResponseCode() != 200) {
                    throw new IOException();
                }
                InputStream inputStream = httpsURLConnection.getInputStream();
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bArr = new byte[4096];
                    while (true) {
                        int i = inputStream.read(bArr);
                        if (i >= 0) {
                            if (byteArrayOutputStream.size() + i > 262144) {
                                throw new IOException();
                            }
                            byteArrayOutputStream.write(bArr, 0, i);
                        } else {
                            String strTrim = new JSONObject(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8)).getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content").trim();
                            if (strTrim.isEmpty()) {
                                throw new IOException();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            httpsURLConnection.disconnect();
                            return strTrim;
                        }
                    }
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        } catch (Throwable th5) {
            httpsURLConnection.disconnect();
            throw th5;
        }
    }

    private static boolean has(Context context, String str) {
        return context.checkSelfPermission(str) == 0;
    }

    private static void toast(Context context, String str) {
        Toast.makeText(context, str, 1).show();
    }
}
