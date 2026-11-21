package local.mertcan.jarvis;

import android.R;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Size;
import android.view.Surface;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public final class Dashcam extends Service {
    private static final long SEGMENT_MS = 300000;
    static final String STATUS = "local.mertcan.jarvis.RECORDING_STATUS";
    static final String STOP = "local.mertcan.jarvis.STOP_RECORDING";
    private boolean closing;
    private int displayDegrees;
    private boolean dual;
    private boolean frontOnly;
    private int generation;
    private Handler handler;
    private boolean sound;
    private volatile boolean stopRequested;
    private HandlerThread worker;
    private final List<Track> tracks = new ArrayList();

    /* JADX INFO: renamed from: $r8$lambda$1v9O9dvmSwi89a23pEBx-DvK0eE, reason: not valid java name */
    public static /* synthetic */ void m0$r8$lambda$1v9O9dvmSwi89a23pEBxDvK0eE(Dashcam dashcam) {
        dashcam.startSegment();
    }

    static /* synthetic */ int access$000(Dashcam dashcam) {
        return dashcam.generation;
    }

    static /* synthetic */ boolean access$100(Dashcam dashcam) {
        return dashcam.closing;
    }

    static /* synthetic */ Notification access$1000(Dashcam dashcam, String str) {
        return dashcam.notification(str);
    }

    static /* synthetic */ void access$1100(Dashcam dashcam, String str) {
        dashcam.report(str);
    }

    static /* synthetic */ void access$1200(Dashcam dashcam, int i) {
        dashcam.rotate(i);
    }

    static /* synthetic */ int access$1300(Dashcam dashcam) {
        return dashcam.displayDegrees;
    }

    static /* synthetic */ boolean access$200(Dashcam dashcam) {
        return dashcam.stopRequested;
    }

    static /* synthetic */ List access$300(Dashcam dashcam) {
        return dashcam.tracks;
    }

    static /* synthetic */ void access$400(Dashcam dashcam, Track track, int i) {
        dashcam.configure(track, i);
    }

    static /* synthetic */ void access$500(Dashcam dashcam, String str) {
        dashcam.end(str);
    }

    static /* synthetic */ Handler access$600(Dashcam dashcam) {
        return dashcam.handler;
    }

    static /* synthetic */ boolean access$700(Dashcam dashcam) {
        return dashcam.dual;
    }

    static /* synthetic */ boolean access$800(Dashcam dashcam) {
        return dashcam.frontOnly;
    }

    static /* synthetic */ boolean access$900(Dashcam dashcam) {
        return dashcam.sound;
    }

    static List<String> cameras(CameraManager cameraManager, boolean z) throws CameraAccessException {
        return cameras(cameraManager, z, false);
    }

    private static List<String> cameras(CameraManager cameraManager, boolean z, boolean z2) throws CameraAccessException {
        if (z && Build.VERSION.SDK_INT >= 30) {
            Iterator<Set<String>> it = cameraManager.getConcurrentCameraIds().iterator();
            while (it.hasNext()) {
                String str = null;
                String str2 = null;
                for (String str3 : it.next()) {
                    Integer num = (Integer) cameraManager.getCameraCharacteristics(str3).get(CameraCharacteristics.LENS_FACING);
                    if (Objects.equals(num, 0)) {
                        str = str3;
                    }
                    if (Objects.equals(num, 1)) {
                        str2 = str3;
                    }
                }
                if (str != null && str2 != null) {
                    return Arrays.asList(str2, str);
                }
            }
            return Collections.emptyList();
        }
        if (z) {
            return Collections.emptyList();
        }
        for (String str4 : cameraManager.getCameraIdList()) {
            if (Objects.equals(cameraManager.getCameraCharacteristics(str4).get(CameraCharacteristics.LENS_FACING), Integer.valueOf(!z2 ? 1 : 0))) {
                return Collections.singletonList(str4);
            }
        }
        return Collections.emptyList();
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.worker = new HandlerThread("dashcam");
        this.worker.start();
        this.handler = new Handler(this.worker.getLooper());
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* JADX INFO: renamed from: lambda$onStartCommand$0$local-mertcan-jarvis-Dashcam, reason: not valid java name */
    /* synthetic */ void m2lambda$onStartCommand$0$localmertcanjarvisDashcam() {
        end("Kayıt durduruldu.");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent != null && STOP.equals(intent.getAction())) {
            this.stopRequested = true;
            this.handler.post(new Dashcam$$ExternalSyntheticLambda3(this));
            return 2;
        }
        if (intent == null || this.generation != 0 || this.stopRequested) {
            return 2;
        }
        this.sound = intent.getBooleanExtra("sound", false);
        this.dual = intent.getBooleanExtra("dual", false);
        this.frontOnly = intent.getBooleanExtra("front", false);
        this.displayDegrees = intent.getIntExtra("displayDegrees", 0);
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                startForeground(79, notification("Kameralar hazırlanıyor…"), DriveCamera.foregroundTypes(this, 64 | (this.sound ? 128 : 0)));
            } else {
                startForeground(79, notification("Kameralar hazırlanıyor…"));
            }
            this.generation = 1;
            DriveCamera.register(this, this.sound);
            this.handler.post(new Dashcam$$ExternalSyntheticLambda1(this));
            return 2;
        } catch (RuntimeException e) {
            report("Kayıt başlatılamadı; uygulama açıkken tekrar dene.");
            stopSelf();
            return 2;
        }
    }

    private Notification notification(String str) {
        ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(new NotificationChannel("dashcam", "Sürüş kaydı", 2));
        PendingIntent service = PendingIntent.getService(this, 79, new Intent(this, (Class<?>) Dashcam.class).setAction(STOP), 67108864);
        return new Notification.Builder(this, "dashcam").setSmallIcon(R.drawable.ic_menu_camera).setContentTitle("Jarvis sürüş kaydı").setContentText(str).setContentIntent(PendingIntent.getActivity(this, 80, new Intent(this, (Class<?>) Main.class), 67108864)).setOngoing(true).addAction(new Notification.Action.Builder((Icon) null, "Durdur", service).build()).build();
    }

    private void report(String str) {
        DriveDashboard.cameraStatus(str);
        sendBroadcast(new Intent(STATUS).setPackage(getPackageName()).putExtra("text", str));
    }

    private void startSegment() {
        if (this.closing || this.stopRequested) {
            return;
        }
        CameraManager cameraManager = (CameraManager) getSystemService(CameraManager.class);
        int i = this.generation;
        try {
            Retention.purge(this);
            List<String> listCameras = cameras(cameraManager, this.dual, this.frontOnly);
            if (listCameras.isEmpty()) {
                end(this.dual ? "Bu telefon çift kamerayı aynı anda açmıyor. Tek kamera seç." : (this.frontOnly ? "Ön" : "Arka") + " kamera bulunamadı.");
                return;
            }
            long jCurrentTimeMillis = System.currentTimeMillis();
            int i2 = 0;
            while (i2 < listCameras.size()) {
                this.tracks.add(new Track(listCameras.get(i2), (i2 != 0 || this.frontOnly) ? "on" : "arka", jCurrentTimeMillis, this.sound && i2 == 0, this.dual && i2 != 0));
                i2++;
            }
            for (Track track : this.tracks) {
                track.prepare(cameraManager);
                cameraManager.openCamera(track.id, new AnonymousClass1(i, track), this.handler);
            }
            this.handler.postDelayed(new Dashcam$$ExternalSyntheticLambda0(this, i), 12000L);
        } catch (Exception e) {
            end("Kamera veya mikrofon hazırlanamadı.");
        }
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.Dashcam$1, reason: invalid class name */
    class AnonymousClass1 extends CameraDevice.StateCallback {
        final /* synthetic */ int val$g;
        final /* synthetic */ Track val$t;

        AnonymousClass1(int i, Track track) {
            this.val$g = i;
            this.val$t = track;
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onOpened(CameraDevice cameraDevice) {
            if (this.val$g != Dashcam.access$000(Dashcam.this) || Dashcam.access$100(Dashcam.this) || Dashcam.access$200(Dashcam.this)) {
                cameraDevice.close();
                return;
            }
            this.val$t.device = cameraDevice;
            Iterator it = Dashcam.access$300(Dashcam.this).iterator();
            while (it.hasNext()) {
                if (((Track) it.next()).device == null) {
                    return;
                }
            }
            Iterator it2 = Dashcam.access$300(Dashcam.this).iterator();
            while (it2.hasNext()) {
                Dashcam.access$400(Dashcam.this, (Track) it2.next(), this.val$g);
            }
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            if (this.val$g != Dashcam.access$000(Dashcam.this) || Dashcam.access$100(Dashcam.this)) {
                return;
            }
            Dashcam.access$500(Dashcam.this, "Kamera bağlantısı kesildi.");
        }

        @Override // android.hardware.camera2.CameraDevice.StateCallback
        public void onError(CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            if (this.val$g != Dashcam.access$000(Dashcam.this) || Dashcam.access$100(Dashcam.this)) {
                return;
            }
            Dashcam.access$500(Dashcam.this, "Kamera açılamadı (" + i + ").");
        }
    }

    /* JADX INFO: renamed from: lambda$startSegment$1$local-mertcan-jarvis-Dashcam, reason: not valid java name */
    /* synthetic */ void m3lambda$startSegment$1$localmertcanjarvisDashcam(int i) {
        if (i != this.generation || this.closing) {
            return;
        }
        Iterator<Track> it = this.tracks.iterator();
        while (it.hasNext()) {
            if (!it.next().started) {
                end("Kamera zamanında açılamadı.");
                return;
            }
        }
    }

    private void configure(Track track, int i) {
        if (this.closing || this.stopRequested || i != this.generation) {
            return;
        }
        try {
            Surface surface = track.recorder == null ? null : track.recorder.getSurface();
            CaptureRequest.Builder builderCreateCaptureRequest = track.device.createCaptureRequest(3);
            if (surface != null) builderCreateCaptureRequest.addTarget(surface);
            DriveDashboard.createSession(track.device, surface, builderCreateCaptureRequest, new AnonymousClass2(i, track, builderCreateCaptureRequest), this.handler);
        } catch (Exception e) {
            end("Kamera oturumu kurulamadı.");
        }
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.Dashcam$2, reason: invalid class name */
    class AnonymousClass2 extends CameraCaptureSession.StateCallback {
        final /* synthetic */ int val$g;
        final /* synthetic */ CaptureRequest.Builder val$req;
        final /* synthetic */ Track val$t;

        AnonymousClass2(int i, Track track, CaptureRequest.Builder builder) {
            this.val$g = i;
            this.val$t = track;
            this.val$req = builder;
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            if (Dashcam.access$100(Dashcam.this) || Dashcam.access$200(Dashcam.this) || this.val$g != Dashcam.access$000(Dashcam.this)) {
                cameraCaptureSession.close();
                return;
            }
            this.val$t.session = cameraCaptureSession;
            try {
                cameraCaptureSession.setRepeatingRequest(this.val$req.build(), null, Dashcam.access$600(Dashcam.this));
                Iterator it = Dashcam.access$300(Dashcam.this).iterator();
                while (it.hasNext()) {
                    if (((Track) it.next()).session == null) {
                        return;
                    }
                }
                for (Object item : Dashcam.access$300(Dashcam.this)) {
                    Track track = (Track) item;
                    if (track.recorder != null) track.recorder.start();
                    track.started = true;
                }
                String string = (Dashcam.access$700(Dashcam.this)
                        ? "Arka kamera kayıtta; ön kamera yalnızca sürücü kontrolü için açık"
                        : Dashcam.access$800(Dashcam.this) ? "Ön kamera kayıtta" : "Arka kamera kayıtta")
                        + (Dashcam.access$900(Dashcam.this) ? " (sesli)." : ".");
                ((NotificationManager) Dashcam.this.getSystemService(NotificationManager.class)).notify(79, Dashcam.access$1000(Dashcam.this, string));
                Dashcam.access$1100(Dashcam.this, string);
                Dashcam.access$600(Dashcam.this).postDelayed(new Dashcam$2$$ExternalSyntheticLambda0(this, this.val$g), Dashcam.SEGMENT_MS);
            } catch (Exception e) {
                Dashcam.access$500(Dashcam.this, "Kayıt açılamadı; tek kamera veya sessiz kayıt dene.");
            }
        }

        /* JADX INFO: renamed from: lambda$onConfigured$0$local-mertcan-jarvis-Dashcam$2, reason: not valid java name */
        /* synthetic */ void m4lambda$onConfigured$0$localmertcanjarvisDashcam$2(int i) {
            Dashcam.access$1200(Dashcam.this, i);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            cameraCaptureSession.close();
            if (Dashcam.access$100(Dashcam.this) || this.val$g != Dashcam.access$000(Dashcam.this)) {
                return;
            }
            Dashcam.access$500(Dashcam.this, "Bu kamera kombinasyonunda video kaydı açılamadı.");
        }
    }

    private void rotate(int i) {
        if (this.closing || this.stopRequested || i != this.generation) {
            return;
        }
        this.generation++;
        release();
        this.handler.postDelayed(new Dashcam$$ExternalSyntheticLambda1(this), 700L);
    }

    private void end(String str) {
        if (this.closing) {
            return;
        }
        this.closing = true;
        this.generation++;
        this.handler.removeCallbacksAndMessages(null);
        release();
        report(str);
        stopForeground(1);
        stopSelf();
    }

    private void release() {
        Iterator<Track> it = this.tracks.iterator();
        while (it.hasNext()) {
            it.next().close();
        }
        this.tracks.clear();
    }

    @Override // android.app.Service
    public void onDestroy() {
        DriveCamera.unregister(this);
        this.stopRequested = true;
        this.handler.post(new Dashcam$$ExternalSyntheticLambda2(this));
        super.onDestroy();
    }

    /* JADX INFO: renamed from: lambda$onDestroy$2$local-mertcan-jarvis-Dashcam, reason: not valid java name */
    /* synthetic */ void m1lambda$onDestroy$2$localmertcanjarvisDashcam() {
        release();
        this.worker.quitSafely();
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class Track {
        CameraDevice device;
        final File finalFile;
        final String id;
        final boolean mic;
        final boolean previewOnly;
        final File partial;
        MediaRecorder recorder;
        CameraCaptureSession session;
        boolean started;

        Track(String str, String str2, long j, boolean z, boolean previewOnly) {
            this.id = str;
            this.mic = z;
            this.previewOnly = previewOnly;
            File fileDirectory = Retention.directory(Dashcam.this);
            this.partial = new File(fileDirectory, j + "-" + str2 + ".partial");
            this.finalFile = new File(fileDirectory, j + "-" + str2 + ".mp4");
        }

        void prepare(CameraManager cameraManager) throws Exception {
            if (this.previewOnly) return;
            if (Retention.directory(Dashcam.this).getUsableSpace() < 262144000) {
                throw new IOException("Storage low");
            }
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(this.id);
            StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size size = null;
            Size[] outputSizes = streamConfigurationMap == null ? null : streamConfigurationMap.getOutputSizes(MediaRecorder.class);
            if (outputSizes != null) {
                for (Size size2 : outputSizes) {
                    int width = size2.getWidth() * size2.getHeight();
                    if (width <= 921600 && width >= 76800 && (size == null || width > size.getWidth() * size.getHeight())) {
                        size = size2;
                    }
                }
            }
            if (size == null) {
                throw new IOException("Video size unavailable");
            }
            this.recorder = Build.VERSION.SDK_INT >= 31 ? new MediaRecorder(Dashcam.this) : new MediaRecorder();
            if (this.mic) {
                this.recorder.setAudioSource(5);
            }
            this.recorder.setVideoSource(2);
            this.recorder.setOutputFormat(2);
            this.recorder.setVideoEncoder(2);
            this.recorder.setVideoSize(size.getWidth(), size.getHeight());
            this.recorder.setVideoFrameRate(30);
            this.recorder.setVideoEncodingBitRate(2200000);
            Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            this.recorder.setOrientationHint((((num != null ? num.intValue() : 0) + (Objects.equals(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING), 0) ? Dashcam.access$1300(Dashcam.this) : -Dashcam.access$1300(Dashcam.this))) + 360) % 360);
            if (this.mic) {
                this.recorder.setAudioEncoder(3);
                this.recorder.setAudioEncodingBitRate(96000);
            }
            this.recorder.setOutputFile(this.partial.getAbsolutePath());
            this.recorder.setOnErrorListener(new Dashcam$Track$$ExternalSyntheticLambda0(this));
            this.recorder.prepare();
        }

        /* JADX INFO: renamed from: lambda$prepare$0$local-mertcan-jarvis-Dashcam$Track, reason: not valid java name */
        /* synthetic */ void m5lambda$prepare$0$localmertcanjarvisDashcam$Track(MediaRecorder mediaRecorder, int i, int i2) {
            if (this.recorder != mediaRecorder || Dashcam.access$100(Dashcam.this)) {
                return;
            }
            Dashcam.access$500(Dashcam.this, "Kayıt cihazı hata verdi.");
        }

        void close() {
            if (this.session != null) {
                try {
                    this.session.stopRepeating();
                } catch (Exception e) {
                }
                this.session.close();
            }
            boolean z = false;
            if (this.recorder != null) {
                if (this.started) {
                    try {
                        this.recorder.stop();
                        z = true;
                    } catch (RuntimeException e2) {
                    }
                }
                this.recorder.release();
                this.recorder = null;
            }
            if (this.device != null) {
                this.device.close();
            }
            if (z) {
                if (this.partial.renameTo(this.finalFile)) {
                    this.finalFile.setLastModified(System.currentTimeMillis());
                    return;
                }
                return;
            }
            this.partial.delete();
        }
    }
}
