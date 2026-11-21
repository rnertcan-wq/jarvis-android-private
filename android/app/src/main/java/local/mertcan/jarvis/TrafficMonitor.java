package local.mertcan.jarvis;

import android.R;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
final class TrafficMonitor {
    private static final String KEY = "tomtom_key";
    private static final String MAP_CHANNEL = "drive-fixed-camera";
    private static final int MAP_NOTIFICATION = 193;
    private static final long MINUTE = 60000;
    private static final String ONLINE = "fixed_camera_online";
    private static final String RADAR = "fixed_camera_enabled";
    private static final String SETTINGS = "drive_traffic";
    private long cameraDataAt;
    private boolean closed;
    private final Context context;
    private String key;
    private long lastAlert;
    private Location lastCameraQuery;
    private Location lastFix;
    private final NotificationManager notifications;
    private final SharedPreferences preferences;
    private boolean radarPending;
    private long radarQueryAt;
    private long trafficAt;
    private boolean trafficPending;
    private long trafficQueryAt;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ExecutorService NETWORK = Executors.newFixedThreadPool(2);
    private final Set<Long> announced = new HashSet();
    private ArrayList<CameraPoint> cameras = new ArrayList<>();
    private ArrayList<CameraPoint> bundled = new ArrayList<>();
    private String traffic = "";

    TrafficMonitor(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(SETTINGS, 0);
        this.key = Secrets.load(context, KEY);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.notifications = notificationManager;
        notificationManager.createNotificationChannel(new NotificationChannel(MAP_CHANNEL, "Sabit hız kamerası", 4));
        NETWORK.execute(new TrafficMonitor$$ExternalSyntheticLambda7(this, context));
    }

    /* JADX INFO: renamed from: lambda$new$1$local-mertcan-jarvis-TrafficMonitor, reason: not valid java name */
    /* synthetic */ void m93lambda$new$1$localmertcanjarvisTrafficMonitor(Context context) {
        MAIN.post(new TrafficMonitor$$ExternalSyntheticLambda1(this, loadBundled(context)));
    }

    /* JADX INFO: renamed from: lambda$new$0$local-mertcan-jarvis-TrafficMonitor, reason: not valid java name */
    /* synthetic */ void m92lambda$new$0$localmertcanjarvisTrafficMonitor(ArrayList arrayList) {
        if (this.closed) {
            return;
        }
        this.bundled = arrayList;
    }

    static void settings(Main main) {
        SharedPreferences sharedPreferences = main.getSharedPreferences(SETTINGS, 0);
        boolean z = sharedPreferences.getBoolean(RADAR, true);
        boolean z2 = sharedPreferences.getBoolean(ONLINE, false);
        new AlertDialog.Builder(main).setTitle("Trafik ve sabit kameralar").setItems(new String[]{"Sabit hız kamerası uyarısı · ".concat(z ? "Açık" : "Kapalı"), "Çevrimiçi kamera sorgusu · ".concat(z2 ? "Açık" : "Kapalı"), "TomTom trafik anahtarı · ".concat(Secrets.load(main, KEY).isEmpty() ? "Yok" : "Kayıtlı"), "Harita verisi ve lisans"}, new TrafficMonitor$$ExternalSyntheticLambda8(sharedPreferences, z, z2, main)).setNegativeButton("Kapat", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$settings$5(SharedPreferences sharedPreferences, boolean z, boolean z2, Main main, DialogInterface dialogInterface, int i) {
        if (i == 0) {
            sharedPreferences.edit().putBoolean(RADAR, !z).apply();
            return;
        }
        if (i == 1 && z2) {
            sharedPreferences.edit().putBoolean(ONLINE, false).apply();
            return;
        }
        if (i == 1) {
            new AlertDialog.Builder(main).setTitle("Çevrimiçi sabit kamera sorgusu").setMessage("Yaklaşık konum OpenStreetMap verisi sunan Overpass hizmetine gönderilir.").setPositiveButton("Aç", new TrafficMonitor$$ExternalSyntheticLambda4(sharedPreferences)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
            return;
        }
        if (i == 3) {
            new AlertDialog.Builder(main).setTitle("Sabit kamera verisi").setMessage("© OpenStreetMap contributors · ODbL 1.0\nTürkiye verisi: Geofabrik\nhttps://www.openstreetmap.org/copyright").setPositiveButton("Kapat", (DialogInterface.OnClickListener) null).show();
            return;
        }
        EditText editText = new EditText(main);
        editText.setSingleLine(true);
        editText.setHint("TomTom Traffic API anahtarı");
        editText.setInputType(129);
        new AlertDialog.Builder(main).setTitle("TomTom canlı trafik").setMessage("Kayıt sırasında yakın yolun hızını ve çevredeki olayları sorgulamak için yaklaşık konum TomTom'a gönderilir.").setView(editText).setPositiveButton("Kaydet", new TrafficMonitor$$ExternalSyntheticLambda5(editText, main)).setNeutralButton("Anahtarı sil", new TrafficMonitor$$ExternalSyntheticLambda6(main)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$settings$2(SharedPreferences sharedPreferences, DialogInterface dialogInterface, int i) {
        sharedPreferences.edit().putBoolean(ONLINE, true).apply();
    }

    static /* synthetic */ void lambda$settings$3(EditText editText, Main main, DialogInterface dialogInterface, int i) {
        String strTrim = editText.getText().toString().trim();
        if (!strTrim.matches("[A-Za-z0-9._-]{8,128}")) {
            Toast.makeText(main, "TomTom anahtarını kontrol et.", 1).show();
        } else {
            saveKey(main, strTrim);
        }
    }

    static /* synthetic */ void lambda$settings$4(Main main, DialogInterface dialogInterface, int i) {
        saveKey(main, "");
    }

    private static void saveKey(Main main, String str) {
        try {
            Secrets.save(main, KEY, str);
            DriveCamera.updateTrafficKey(str);
        } catch (Exception unused) {
            Toast.makeText(main, "TomTom anahtarı kaydedilemedi.", 1).show();
        }
    }

    void setKey(String str) {
        this.key = str;
        this.traffic = "";
        this.trafficAt = 0L;
        this.trafficQueryAt = 0L;
    }

    String currentTraffic() {
        return (this.key.isEmpty() || SystemClock.elapsedRealtime() - this.trafficAt > 130000) ? "" : this.traffic;
    }

    void onFix(Location location) {
        Location location2;
        if (this.closed || !location.hasAccuracy() || location.getAccuracy() > 45.0f) {
            return;
        }
        this.lastFix = new Location(location);
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        if (this.preferences.getBoolean(RADAR, true)) {
            if (this.preferences.getBoolean(ONLINE, false) && !this.radarPending) {
                long j = this.radarQueryAt;
                if ((j == 0 || jElapsedRealtime - j >= MINUTE) && ((location2 = this.lastCameraQuery) == null || location2.distanceTo(location) >= 1600.0f || jElapsedRealtime - this.radarQueryAt >= 300000)) {
                    updateCameras(this.lastFix, jElapsedRealtime);
                }
            }
            alertIfNear(location);
        } else {
            this.notifications.cancel(MAP_NOTIFICATION);
        }
        if (this.key.isEmpty() || this.trafficPending) {
            return;
        }
        long j2 = this.trafficQueryAt;
        if (j2 == 0 || jElapsedRealtime - j2 >= MINUTE) {
            updateTraffic(this.lastFix, this.key, jElapsedRealtime);
        }
    }

    String summary() {
        if (this.key.isEmpty()) {
            return "Canlı trafik için sürüş kamerası menüsünden TomTom anahtarı ekle.";
        }
        if (this.traffic.isEmpty() || SystemClock.elapsedRealtime() - this.trafficAt > 130000) {
            return "Şu anda güncel TomTom trafik verisi yok.";
        }
        return this.traffic;
    }

    String radarSummary() {
        if (!this.preferences.getBoolean(RADAR, true)) {
            return "Sabit kamera uyarısı kapalı.";
        }
        if (!onlineDataAvailable()) {
            return !this.bundled.isEmpty() ? "Türkiye için kayıtlı OpenStreetMap sabit kamera verisi kullanılıyor." : "Sabit kamera verisi alınamadı.";
        }
        return "Çevrimiçi tarama alanında " + this.cameras.size() + " sabit kamera kayıtlı.";
    }

    void close() {
        this.closed = true;
        this.notifications.cancel(MAP_NOTIFICATION);
    }

    private void updateCameras(Location location, long j) {
        this.radarPending = true;
        this.radarQueryAt = j;
        NETWORK.execute(new TrafficMonitor$$ExternalSyntheticLambda2(this, location));
    }

    /* JADX INFO: renamed from: lambda$updateCameras$7$local-mertcan-jarvis-TrafficMonitor, reason: not valid java name */
    /* synthetic */ void m95lambda$updateCameras$7$localmertcanjarvisTrafficMonitor(Location location) {
        ArrayList<CameraPoint> arrayListLoadCameras;
        try {
            arrayListLoadCameras = loadCameras(location);
        } catch (Exception unused) {
            arrayListLoadCameras = null;
        }
        MAIN.post(new TrafficMonitor$$ExternalSyntheticLambda3(this, arrayListLoadCameras, location));
    }

    /* JADX INFO: renamed from: lambda$updateCameras$6$local-mertcan-jarvis-TrafficMonitor, reason: not valid java name */
    /* synthetic */ void m94lambda$updateCameras$6$localmertcanjarvisTrafficMonitor(ArrayList arrayList, Location location) {
        this.radarPending = false;
        if (this.closed || arrayList == null || !this.preferences.getBoolean(RADAR, true) || !this.preferences.getBoolean(ONLINE, false)) {
            return;
        }
        this.lastCameraQuery = location;
        long jElapsedRealtime = SystemClock.elapsedRealtime();
        this.cameraDataAt = jElapsedRealtime;
        this.cameras = arrayList;
        Location location2 = this.lastFix;
        if (location2 == null || jElapsedRealtime - (location2.getElapsedRealtimeNanos() / 1000000) > 5000) {
            return;
        }
        alertIfNear(this.lastFix);
    }

    private static ArrayList<CameraPoint> loadCameras(Location location) throws Exception {
        JSONArray jSONArrayCameraNodes;
        String str = String.format(Locale.US, "[out:json][timeout:10];node[\"highway\"=\"speed_camera\"](around:4800,%.3f,%.3f);out 160;", Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
        try {
            jSONArrayCameraNodes = cameraNodes("https://overpass-api.de/api/interpreter", str);
        } catch (Exception unused) {
            jSONArrayCameraNodes = cameraNodes("https://overpass.private.coffee/api/interpreter", str);
        }
        ArrayList<CameraPoint> arrayList = new ArrayList<>();
        for (int i = 0; i < jSONArrayCameraNodes.length(); i++) {
            arrayList.add(new CameraPoint(jSONArrayCameraNodes.getJSONObject(i)));
        }
        return arrayList;
    }

    private static JSONArray cameraNodes(String str, String str2) throws Exception {
        return get(Uri.parse(str).buildUpon().appendQueryParameter("data", str2).build()).getJSONArray("elements");
    }

    private boolean onlineDataAvailable() {
        return this.preferences.getBoolean(ONLINE, false) && this.cameraDataAt > 0 && SystemClock.elapsedRealtime() - this.cameraDataAt < 600000;
    }

    private static ArrayList<CameraPoint> loadBundled(Context context) {
        ArrayList<CameraPoint> arrayList = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("turkey-speed-cameras.csv"), StandardCharsets.UTF_8));
            while (true) {
                try {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (!line.startsWith("#")) {
                        String[] strArrSplit = line.split(",");
                        if (strArrSplit.length == 3) {
                            arrayList.add(new CameraPoint(Long.parseLong(strArrSplit[0]), Double.parseDouble(strArrSplit[1]), Double.parseDouble(strArrSplit[2])));
                        }
                    }
                    return arrayList;
                } catch (Throwable th) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            }
            bufferedReader.close();
        } catch (Exception unused) {
        }
        return arrayList;
    }

    private void alertIfNear(Location location) {
        Location location2;
        if ((this.lastAlert <= 0 || SystemClock.elapsedRealtime() - this.lastAlert >= 20000) && location.hasSpeed() && !(location.getSpeed() < 4.0f) && location.hasBearing()) {
            if (!location.hasBearingAccuracy() || !(location.getBearingAccuracyDegrees() > 40.0f)) {
                float fMin = Math.min(500.0f, Math.max(180.0f, location.getSpeed() * 14.0f));
                CameraPoint cameraPoint = null;
                for (CameraPoint cameraPoint2 : (!onlineDataAvailable() || (location2 = this.lastCameraQuery) == null || !(location2.distanceTo(location) < 3900.0f)) ? this.bundled : this.cameras) {
                    if (!(Math.abs(cameraPoint2.location.getLatitude() - location.getLatitude()) > 0.006d) && !(Math.abs(cameraPoint2.location.getLongitude() - location.getLongitude()) > 0.009d)) {
                        float fDistanceTo = location.distanceTo(cameraPoint2.location);
                        float fAngle = angle(location.getBearing(), location.bearingTo(cameraPoint2.location));
                        if (!this.announced.contains(Long.valueOf(cameraPoint2.id)) && !(fDistanceTo < 30.0f) && !(fDistanceTo > fMin) && !(fAngle > 24.0f)) {
                            cameraPoint = cameraPoint2;
                            fMin = fDistanceTo;
                        }
                    }
                }
                if (cameraPoint == null) {
                    return;
                }
                this.announced.add(Long.valueOf(cameraPoint.id));
                this.lastAlert = SystemClock.elapsedRealtime();
                int iRound = Math.round(fMin / 10.0f) * 10;
                this.notifications.notify(MAP_NOTIFICATION, new Notification.Builder(this.context, MAP_CHANNEL).setSmallIcon(R.drawable.ic_menu_camera).setContentTitle("Haritada sabit hız kamerası").setContentText("İleride yaklaşık " + iRound + " m · OpenStreetMap").setAutoCancel(true).setTimeoutAfter(12000L).build());
            }
        }
    }

    private static float angle(float f, float f2) {
        return Math.abs((((f - f2) + 540.0f) % 360.0f) - 180.0f);
    }

    private void updateTraffic(Location location, String str, long j) {
        this.trafficPending = true;
        this.trafficQueryAt = j;
        NETWORK.execute(new TrafficMonitor$$ExternalSyntheticLambda9(this, location, str));
    }

    /* JADX INFO: renamed from: lambda$updateTraffic$9$local-mertcan-jarvis-TrafficMonitor, reason: not valid java name */
    /* synthetic */ void m97lambda$updateTraffic$9$localmertcanjarvisTrafficMonitor(Location location, String str) {
        String strLoadFlow;
        String strLoadIncidents = null;
        try {
            strLoadFlow = loadFlow(location, str);
        } catch (Exception unused) {
            strLoadFlow = null;
        }
        try {
            strLoadIncidents = loadIncidents(location, str);
        } catch (Exception unused2) {
        }
        if (strLoadFlow == null) {
            strLoadFlow = strLoadIncidents;
        } else if (strLoadIncidents != null) {
            strLoadFlow = strLoadFlow + " " + strLoadIncidents;
        }
        MAIN.post(new TrafficMonitor$$ExternalSyntheticLambda0(this, str, strLoadFlow));
    }

    /* JADX INFO: renamed from: lambda$updateTraffic$8$local-mertcan-jarvis-TrafficMonitor, reason: not valid java name */
    /* synthetic */ void m96lambda$updateTraffic$8$localmertcanjarvisTrafficMonitor(String str, String str2) {
        this.trafficPending = false;
        if (this.closed || !str.equals(this.key) || str2 == null) {
            return;
        }
        this.traffic = str2;
        this.trafficAt = SystemClock.elapsedRealtime();
    }

    private static String loadFlow(Location location, String str) throws Exception {
        JSONObject jSONObject = get(Uri.parse("https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/14/json").buildUpon().appendQueryParameter("point", String.format(Locale.US, "%.4f,%.4f", Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()))).appendQueryParameter("unit", "kmph").appendQueryParameter("key", str).build()).getJSONObject("flowSegmentData");
        if (jSONObject.optBoolean("roadClosure")) {
            return "TomTom en yakın yol kesimini kapalı gösteriyor.";
        }
        if (jSONObject.optDouble("confidence", 0.0d) < 0.6d) {
            return null;
        }
        int iOptInt = jSONObject.optInt("currentSpeed", -1);
        int iOptInt2 = jSONObject.optInt("freeFlowSpeed", -1);
        if (iOptInt < 0 || iOptInt2 <= 0) {
            return null;
        }
        return "TomTom en yakın yol kesiminde saatte " + iOptInt + " kilometre akış gösteriyor. Normal akış " + iOptInt2 + " kilometre.";
    }

    private static String loadIncidents(Location location, String str) throws Exception {
        double dRound = Math.round(location.getLatitude() * 10000.0d) / 10000.0d;
        double dRound2 = Math.round(location.getLongitude() * 10000.0d) / 10000.0d;
        JSONArray jSONArray = get(Uri.parse("https://api.tomtom.com/traffic/services/5/incidentDetails").buildUpon().appendQueryParameter("bbox", String.format(Locale.US, "%.4f,%.4f,%.4f,%.4f", Double.valueOf(dRound2 - 0.017d), Double.valueOf(dRound - 0.013d), Double.valueOf(dRound2 + 0.017d), Double.valueOf(dRound + 0.013d))).appendQueryParameter("fields", "{incidents{properties{iconCategory}}}").appendQueryParameter("timeValidityFilter", "present").appendQueryParameter("key", str).build()).getJSONArray("incidents");
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (int i5 = 0; i5 < jSONArray.length(); i5++) {
            int iOptInt = jSONArray.getJSONObject(i5).getJSONObject("properties").optInt("iconCategory");
            if (iOptInt == 1) {
                i++;
            }
            if (iOptInt == 6) {
                i4++;
            }
            if (iOptInt == 8) {
                i2++;
            }
            if (iOptInt == 9) {
                i3++;
            }
        }
        if (jSONArray.length() == 0) {
            return "TomTom çevrede kayıtlı trafik olayı göstermiyor.";
        }
        return "TomTom çevrede " + jSONArray.length() + " trafik olayı gösteriyor. Bunların " + i + " tanesi kaza, " + i2 + " tanesi kapanma, " + i3 + " tanesi yol çalışması, " + i4 + " tanesi sıkışıklık.";
    }

    private static JSONObject get(Uri uri) throws Exception {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(uri.toString()).openConnection();
        httpsURLConnection.setConnectTimeout(10000);
        httpsURLConnection.setReadTimeout(15000);
        httpsURLConnection.setInstanceFollowRedirects(false);
        httpsURLConnection.setRequestProperty("Accept", "application/json");
        httpsURLConnection.setRequestProperty("User-Agent", "JarvisAndroid/0.9.3 (personal dashcam)");
        try {
            if (httpsURLConnection.getResponseCode() != 200) {
                throw new IOException("Map data unavailable");
            }
            InputStream inputStream = httpsURLConnection.getInputStream();
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[4096];
                while (true) {
                    int i = inputStream.read(bArr);
                    if (i != -1) {
                        if (byteArrayOutputStream.size() + i > 262144) {
                            throw new IOException("Map response too large");
                        }
                        byteArrayOutputStream.write(bArr, 0, i);
                    } else {
                        JSONObject jSONObject = new JSONObject(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        httpsURLConnection.disconnect();
                        return jSONObject;
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
            httpsURLConnection.disconnect();
            throw th3;
        }
    }

    private static final class CameraPoint {
        final long id;
        final Location location;

        CameraPoint(JSONObject jSONObject) throws Exception {
            this(jSONObject.getLong("id"), jSONObject.getDouble("lat"), jSONObject.getDouble("lon"));
        }

        CameraPoint(long j, double d, double d2) {
            Location location = new Location("osm");
            this.location = location;
            this.id = j;
            location.setLatitude(d);
            location.setLongitude(d2);
        }
    }
}
