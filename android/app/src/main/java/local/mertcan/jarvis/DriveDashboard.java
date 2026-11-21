package local.mertcan.jarvis;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes2.dex */
final class DriveDashboard implements TextureView.SurfaceTextureListener {
    private static final String MAP = "<meta name='viewport' content='width=device-width,initial-scale=1'><style>body{margin:0;background:#111d2d;color:#a8bbcf;font:14px sans-serif;overflow:hidden}#tiles{position:absolute;inset:0}img{position:absolute;width:256px;height:256px}#pin{position:absolute;left:50%;top:50%;width:14px;height:14px;border:3px solid white;background:#0077ff;border-radius:100%;transform:translate(-50%,-50%);display:none}small{position:absolute;right:0;bottom:0;background:#111d2d;color:#c6d5e5;padding:4px}</style><div id='tiles' aria-label='Harita'>GPS konumu bekleniyor…</div><div id='pin'></div><small>© OpenStreetMap contributors</small><script>let tiles=document.getElementById('tiles'),pin=document.getElementById('pin'),images={},last;function moveMap(lat,lon){last=[lat,lon];let z=15,n=2**z,p=Math.max(-85,Math.min(85,lat))*Math.PI/180;let x=(lon+180)/360*n*256,y=(1-Math.asinh(Math.tan(p))/Math.PI)/2*n*256;let w=innerWidth,h=innerHeight,l=x-w/2,t=y-h/2,keep={};if(pin.style.display!='block'){tiles.textContent='';pin.style.display='block'}for(let i=Math.floor(l/256);i<=Math.floor((l+w)/256);i++)for(let j=Math.floor(t/256);j<=Math.floor((t+h)/256);j++){if(j<0||j>=n)continue;let k=i+','+j,im=images[k];if(!im){im=new Image();im.src='https://tile.openstreetmap.org/'+z+'/'+((i%n+n)%n)+'/'+j+'.png';tiles.append(im)}keep[k]=im;im.style.left=(i*256-l)+'px';im.style.top=(j*256-t)+'px'}for(let k in images)if(!keep[k])images[k].remove();images=keep}addEventListener('resize',()=>{if(last)moveMap(...last)});</script>";
    private static final String MAP_BASE = "https://local-mertcan-jarvis.invalid/";
    private final Main activity;
    private static boolean awaiting;
    private final LinearLayout cameraCard;
    private LinearLayout controls;
    private float normalCameraWeight;
    private boolean pipMode;
    private boolean statusWasVisible;
    private boolean trafficWasVisible;
    private boolean closed;
    private static DriveDashboard current;
    private final Dialog dialog;
    private TextView driverStatus;
    private static volatile String frontId;
    private static volatile Surface frontSurface;
    private TextureView frontTexture;
    private static boolean hasFix;
    private static double latitude;
    private TextView liveLabel;
    private static double longitude;
    private WebView map;
    private final LinearLayout mapArea;
    private final LinearLayout mapCard;
    private boolean mapExpanded;
    private static volatile Surface previewSurface;
    private SurfaceTexture retained;
    private SurfaceTexture retainedFront;
    private boolean roadBusy;
    private int rotationCorrection;
    private final TextView speed;
    private volatile boolean started;
    private final TextView status;
    private final TextureView texture;
    private final TextView traffic;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private final Runnable faceCheck = new DriveDashboard$$ExternalSyntheticLambda9(this);
    private final ExecutorService vision = Executors.newSingleThreadExecutor();
    private final FaceDetector eyeDetector = FaceDetection.getClient(
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setMinFaceSize(0.15f)
                    .enableTracking()
                    .build());
    private ToneGenerator driverAlarm;
    private long eyesClosedSince;
    private boolean drowsinessAlerted;
    private static String liveState = "Eller serbest kapalı";

    public static /* synthetic */ void $r8$lambda$5adlwB6O0i_GZ4Fc52tVwFF6UAI(DriveDashboard driveDashboard) {
        driveDashboard.checkFace();
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    static /* synthetic */ boolean access$000(DriveDashboard driveDashboard) {
        return driveDashboard.closed;
    }

    static /* synthetic */ Surface access$102(Surface surface) {
        frontSurface = surface;
        return surface;
    }

    static /* synthetic */ void access$200(DriveDashboard driveDashboard) {
        driveDashboard.startRecordingIfReady();
    }

    static /* synthetic */ boolean access$300(DriveDashboard driveDashboard) {
        return driveDashboard.started;
    }

    static /* synthetic */ SurfaceTexture access$402(DriveDashboard driveDashboard, SurfaceTexture surfaceTexture) {
        driveDashboard.retainedFront = surfaceTexture;
        return surfaceTexture;
    }

    static /* synthetic */ void access$500(DriveDashboard driveDashboard) {
        driveDashboard.updateMap();
    }

    static void open(Main main) {
        if (current != null) {
            return;
        }
        if (DriveCamera.isRecording()) {
            new AlertDialog.Builder(main).setMessage("Önizlemeli sürüş ekranı için devam eden kaydı durdur.").setPositiveButton("Kaydı durdur", new DriveDashboard$$ExternalSyntheticLambda7(main)).setNegativeButton("Kapat", (DialogInterface.OnClickListener) null).show();
            return;
        }
        ArrayList arrayList = new ArrayList();
        if (!granted(main, "android.permission.CAMERA")) {
            arrayList.add("android.permission.CAMERA");
        }
        if (!granted(main, "android.permission.ACCESS_FINE_LOCATION")) {
            arrayList.add("android.permission.ACCESS_FINE_LOCATION");
            arrayList.add("android.permission.ACCESS_COARSE_LOCATION");
        }
        if (Build.VERSION.SDK_INT >= 33 && !granted(main, "android.permission.POST_NOTIFICATIONS")) {
            arrayList.add("android.permission.POST_NOTIFICATIONS");
        }
        if (!arrayList.isEmpty() && !awaiting) {
            awaiting = true;
            main.requestPermissions((String[]) arrayList.toArray(new String[0]), 194);
        } else if (granted(main, "android.permission.CAMERA")) {
            show(main);
        }
    }

    static /* synthetic */ void lambda$open$0(Main main, DialogInterface dialogInterface, int i) {
        main.stopService(new Intent(main, (Class<?>) Dashcam.class));
    }

    static void suspend(Main main) {
        DriveDashboard driveDashboard = current;
        if (driveDashboard == null || driveDashboard.activity != main || main.isInPictureInPictureMode()) {
            return;
        }
        driveDashboard.dialog.dismiss();
    }

    static void pipMode(Main main, boolean enabled) {
        DriveDashboard driveDashboard = current;
        if (driveDashboard != null && driveDashboard.activity == main) {
            driveDashboard.setPipMode(enabled);
        }
    }

    private void setPipMode(boolean enabled) {
        if (this.closed || this.pipMode == enabled) return;
        this.pipMode = enabled;
        if (enabled) {
            this.statusWasVisible = this.status.getVisibility() == View.VISIBLE;
            this.trafficWasVisible = this.traffic.getVisibility() == View.VISIBLE;
            this.mapCard.setVisibility(View.GONE);
            if (this.controls != null) this.controls.setVisibility(View.GONE);
            this.status.setVisibility(View.GONE);
            this.traffic.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.cameraCard.getLayoutParams();
            params.weight = 1.0f;
            this.cameraCard.setLayoutParams(params);
            this.driverStatus.setText("JARVIS kamera canlı");
        } else {
            this.mapCard.setVisibility(View.VISIBLE);
            if (this.controls != null) this.controls.setVisibility(View.VISIBLE);
            this.status.setVisibility(this.statusWasVisible ? View.VISIBLE : View.GONE);
            this.traffic.setVisibility(this.trafficWasVisible ? View.VISIBLE : View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.cameraCard.getLayoutParams();
            params.weight = this.normalCameraWeight;
            this.cameraCard.setLayoutParams(params);
            this.mapCard.requestLayout();
            updateMap();
        }
    }

    static void onPermission(Main main, int i) {
        if (i != 194) {
            return;
        }
        awaiting = false;
        if (!granted(main, "android.permission.CAMERA") || main.isFinishing()) {
            Toast.makeText(main, "Önizleme için kamera izni gerekli.", 1).show();
        } else {
            show(main);
        }
    }

    private static boolean granted(Main main, String str) {
        return main.checkSelfPermission(str) == 0;
    }

    private static void show(Main main) {
        if (current != null) {
            return;
        }
        hasFix = false;
        DriveDashboard driveDashboard = new DriveDashboard(main);
        current = driveDashboard;
        driveDashboard.dialog.show();
        Window window = current.dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -1);
            window.setBackgroundDrawable(new ColorDrawable(-16249578));
            window.addFlags(128);
        }
    }

    private DriveDashboard(Main main) {
        ViewGroup.LayoutParams layoutParams;
        this.activity = main;
        this.rotationCorrection = main.getPreferences(0).getInt("drive_preview_rotation", 0);
        Dialog dialog = new Dialog(main);
        this.dialog = dialog;
        dialog.requestWindowFeature(1);
        int i = main.getResources().getConfiguration().orientation == 2 ? 1 : 0;
        LinearLayout linearLayout = new LinearLayout(main);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(14), dp(8), dp(14), dp(8));
        linearLayout.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{-15852757, -16249578, -15722973}));
        LinearLayout linearLayout2 = new LinearLayout(main);
        linearLayout2.setGravity(16);
        TextView textViewLabel = label("Sürüş", i != 0 ? 19 : 22);
        textViewLabel.setTypeface(Typeface.create("sans-serif-medium", 0));
        textViewLabel.setGravity(8388611);
        linearLayout2.addView(textViewLabel, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textViewLabel2 = label("— km/sa", i != 0 ? 23 : 29);
        this.speed = textViewLabel2;
        textViewLabel2.setTextColor(-8460305);
        textViewLabel2.setTypeface(Typeface.DEFAULT_BOLD);
        TextView textViewLabel3 = label(liveState, 11);
        this.liveLabel = textViewLabel3;
        textViewLabel3.setTextColor(-8136240);
        this.liveLabel.setMaxLines(1);
        this.liveLabel.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -2, 2.0f);
        layoutParams2.rightMargin = dp(8);
        linearLayout2.addView(this.liveLabel, layoutParams2);
        linearLayout2.addView(textViewLabel2);
        linearLayout.addView(linearLayout2);
        TextView textViewLabel4 = label(granted(main, "android.permission.ACCESS_FINE_LOCATION") ? "Kamera hazırlanıyor · GPS bekleniyor" : "GPS için kesin konum izni gerekli", 12);
        this.status = textViewLabel4;
        textViewLabel4.setTextColor(-5719089);
        textViewLabel4.setGravity(8388611);
        linearLayout.addView(textViewLabel4);
        TextView textViewLabel5 = label("", 12);
        this.traffic = textViewLabel5;
        textViewLabel5.setMaxLines(2);
        textViewLabel5.setVisibility(8);
        linearLayout.addView(textViewLabel5);
        LinearLayout linearLayout3 = new LinearLayout(main);
        linearLayout3.setOrientation(i ^ 1);
        linearLayout.addView(linearLayout3, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayoutPanel = panel("Arka kamera");
        this.cameraCard = linearLayoutPanel;
        TextureView textureView = new TextureView(main);
        this.texture = textureView;
        textureView.setSurfaceTextureListener(this);
        FrameLayout frameLayout = new FrameLayout(main);
        frameLayout.addView(textureView, new FrameLayout.LayoutParams(-1, -1));
        linearLayoutPanel.addView(frameLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        frontId = dualFrontCamera();
        if (frontId != null && main.getPreferences(0).getBoolean("drive_front_enabled", false)) {
            this.frontTexture = new TextureView(main);
            FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(dp(120), dp(92), 8388693);
            layoutParams3.setMargins(dp(7), dp(7), dp(7), dp(7));
            frameLayout.addView(this.frontTexture, layoutParams3);
            this.frontTexture.setSurfaceTextureListener(new AnonymousClass1());
        }
        TextView textViewLabel6 = label(this.frontTexture == null ? "Ön kamera kapalı" : "Sürücü görüntüsü bekleniyor", 11);
        this.driverStatus = textViewLabel6;
        textViewLabel6.setTextColor(-5719089);
        linearLayoutPanel.addView(this.driverStatus);
        LinearLayout.LayoutParams layoutParams4 = i != 0 ? new LinearLayout.LayoutParams(0, -1, 1.55f) : new LinearLayout.LayoutParams(-1, 0, 1.4f);
        if (i != 0) {
            layoutParams4.rightMargin = dp(10);
        } else {
            layoutParams4.bottomMargin = dp(10);
        }
        this.normalCameraWeight = layoutParams4.weight;
        linearLayout3.addView(linearLayoutPanel, layoutParams4);
        LinearLayout linearLayoutPanel2 = panel("Harita");
        this.mapCard = linearLayoutPanel2;
        LinearLayout linearLayout4 = new LinearLayout(main);
        this.mapArea = linearLayout4;
        linearLayout4.setOrientation(1);
        linearLayout4.setGravity(17);
        linearLayoutPanel2.addView(linearLayout4, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        if (i != 0) {
            layoutParams = new LinearLayout.LayoutParams(0, -1, 1.0f);
        } else {
            layoutParams = new LinearLayout.LayoutParams(-1, 0, 1.0f);
        }
        linearLayout3.addView(linearLayoutPanel2, layoutParams);
        Button buttonControl = control("Haritayı aç", -14666420, -1772545);
        buttonControl.setOnClickListener(new DriveDashboard$$ExternalSyntheticLambda10(this, main));
        linearLayout4.addView(buttonControl);
        LinearLayout linearLayout5 = new LinearLayout(main);
        linearLayout5.setPadding(0, dp(8), 0, 0);
        Button buttonControl2 = control("Google Maps", -15584184, -5969155);
        buttonControl2.setOnClickListener(new DriveDashboard$$ExternalSyntheticLambda11(this));
        addControl(linearLayout5, buttonControl2, false);
        Button buttonControl3 = control("Harita boyutu", -14997445, -2496780);
        buttonControl3.setOnClickListener(new DriveDashboard$$ExternalSyntheticLambda12(this));
        addControl(linearLayout5, buttonControl3, true);
        Button buttonControl4 = control("Ayarlar", -14997445, -2496780);
        buttonControl4.setOnClickListener(new DriveDashboard$$ExternalSyntheticLambda13(this));
        addControl(linearLayout5, buttonControl4, true);
        Button buttonControl5 = control("Kaydı bitir", -12311761, -15155);
        buttonControl5.setOnClickListener(new DriveDashboard$$ExternalSyntheticLambda14(this));
        addControl(linearLayout5, buttonControl5, true);
        this.controls = linearLayout5;
        linearLayout.addView(linearLayout5);
        dialog.setContentView(linearLayout);
        dialog.setOnDismissListener(new DriveDashboard$$ExternalSyntheticLambda15(this));
        dialog.setOnShowListener(new DriveDashboard$$ExternalSyntheticLambda16(this, main));
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.DriveDashboard$1, reason: invalid class name */
    class AnonymousClass1 implements TextureView.SurfaceTextureListener {
        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

        AnonymousClass1() {
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            if (DriveDashboard.access$000(DriveDashboard.this)) {
                return;
            }
            surfaceTexture.setDefaultBufferSize(320, 240);
            DriveDashboard.access$102(new Surface(surfaceTexture));
            DriveDashboard.access$200(DriveDashboard.this);
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (!DriveDashboard.access$300(DriveDashboard.this) || DriveDashboard.access$000(DriveDashboard.this)) {
                return true;
            }
            DriveDashboard.access$402(DriveDashboard.this, surfaceTexture);
            return false;
        }
    }

    /* JADX INFO: renamed from: lambda$new$2$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m13lambda$new$2$localmertcanjarvisDriveDashboard(Main main, View view) {
        if (!granted(main, "android.permission.ACCESS_FINE_LOCATION")) {
            this.status.setText("Haritada konum için kesin konum izni gerekli.");
        } else {
            new AlertDialog.Builder(main).setTitle("Çevrimiçi harita").setMessage("Haritanın görüntülenmesi için konumuna yakın harita kareleri OpenStreetMap sunucusundan istenir.").setPositiveButton("Haritayı aç", new DriveDashboard$$ExternalSyntheticLambda18(this, main)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
        }
    }

    /* JADX INFO: renamed from: lambda$new$1$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m12lambda$new$1$localmertcanjarvisDriveDashboard(Main main, DialogInterface dialogInterface, int i) {
        main.getPreferences(0).edit().putBoolean("drive_map", true).apply();
        showMap();
    }

    /* JADX INFO: renamed from: lambda$new$3$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m14lambda$new$3$localmertcanjarvisDriveDashboard(View view) {
        openGoogleMaps();
    }

    /* JADX INFO: renamed from: lambda$new$4$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m15lambda$new$4$localmertcanjarvisDriveDashboard(View view) {
        resizeMap(!this.mapExpanded);
    }

    /* JADX INFO: renamed from: lambda$new$5$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m16lambda$new$5$localmertcanjarvisDriveDashboard(View view) {
        showOptions();
    }

    /* JADX INFO: renamed from: lambda$new$6$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m17lambda$new$6$localmertcanjarvisDriveDashboard(View view) {
        this.dialog.dismiss();
    }

    /* JADX INFO: renamed from: lambda$new$7$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m18lambda$new$7$localmertcanjarvisDriveDashboard(DialogInterface dialogInterface) {
        close();
    }

    /* JADX INFO: renamed from: lambda$new$8$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m19lambda$new$8$localmertcanjarvisDriveDashboard(Main main, DialogInterface dialogInterface) {
        if (granted(main, "android.permission.ACCESS_FINE_LOCATION") && main.getPreferences(0).getBoolean("drive_map", false)) {
            showMap();
        }
    }

    static void liveStatus(String str) {
        liveState = str;
        DriveDashboard driveDashboard = current;
        if (driveDashboard == null || driveDashboard.closed) {
            return;
        }
        driveDashboard.liveLabel.setText(str);
    }

    private void showOptions() {
        new AlertDialog.Builder(this.activity).setTitle("Sürüş ayarları").setItems(new String[]{GeminiLive.active ? "Eller serbest dinlemeyi kapat" : "Hey Jarvis dinlemesini aç", "Kamera yönünü döndür", "Ön kamera ayarı", "Yol karesini incele"}, new DriveDashboard$$ExternalSyntheticLambda3(this)).setNegativeButton("Kapat", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$showOptions$9$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m21lambda$showOptions$9$localmertcanjarvisDriveDashboard(DialogInterface dialogInterface, int i) {
        if (i == 0) {
            if (GeminiLive.active) {
                this.activity.stopService(new Intent(this.activity, (Class<?>) GeminiLive.class));
                return;
            } else {
                GeminiAccess.open(this.activity);
                return;
            }
        }
        if (i == 1) {
            this.rotationCorrection = (this.rotationCorrection + 90) % 360;
            this.activity.getPreferences(0).edit().putInt("drive_preview_rotation", this.rotationCorrection).apply();
            rotate(this.texture.getWidth(), this.texture.getHeight());
        } else if (i == 2) {
            configureFront();
        } else {
            inspectRoad();
        }
    }

    static String mapCommand(boolean z) {
        DriveDashboard driveDashboard = current;
        if (driveDashboard == null) {
            return "Harita boyutunu değiştirmek için sürüş ekranını aç.";
        }
        MAIN.post(new DriveDashboard$$ExternalSyntheticLambda1(driveDashboard, z));
        return z ? "Harita büyütülüyor." : "Harita küçültülüyor.";
    }

    static /* synthetic */ void lambda$mapCommand$10(DriveDashboard driveDashboard, boolean z) {
        if (driveDashboard.closed) {
            return;
        }
        driveDashboard.resizeMap(z);
    }

    private void resizeMap(boolean z) {
        float f;
        this.mapExpanded = z;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.cameraCard.getLayoutParams();
        boolean z2 = ((LinearLayout) this.cameraCard.getParent()).getOrientation() == 0;
        if (z) {
            f = 0.0f;
        } else {
            f = z2 ? 1.55f : 1.4f;
        }
        layoutParams.weight = f;
        if (z2) {
            layoutParams.rightMargin = z ? 0 : dp(10);
        } else {
            layoutParams.bottomMargin = z ? 0 : dp(10);
        }
        this.cameraCard.setLayoutParams(layoutParams);
        this.cameraCard.setAlpha(z ? 0.0f : 1.0f);
        this.mapCard.requestLayout();
    }

    private String dualFrontCamera() {
        if (Build.VERSION.SDK_INT < 30) {
            return null;
        }
        CameraManager cameraManager = (CameraManager) this.activity.getSystemService(CameraManager.class);
        try {
            Iterator<Set<String>> it = cameraManager.getConcurrentCameraIds().iterator();
            while (it.hasNext()) {
                String str = null;
                boolean z = false;
                for (String str2 : it.next()) {
                    Integer num = (Integer) cameraManager.getCameraCharacteristics(str2).get(CameraCharacteristics.LENS_FACING);
                    Integer num2 = 1;
                    if (num2.equals(num)) {
                        z = true;
                    }
                    Integer num3 = 0;
                    if (num3.equals(num)) {
                        str = str2;
                    }
                }
                if (z && str != null) {
                    return str;
                }
            }
        } catch (Exception unused) {
        }
        return null;
    }

    private void configureFront() {
        String str;
        if (dualFrontCamera() == null) {
            this.status.setText("Telefon ön ve arka kameranın eşzamanlı açılmasını desteklemiyor.");
            return;
        }
        boolean z = this.frontTexture != null;
        AlertDialog.Builder title = new AlertDialog.Builder(this.activity).setTitle("Ön kamera");
        if (z) {
            str = "Bir sonraki sürüş kaydında ön kamerayı kapat.";
        } else {
            str = "Ön kamera görüntüsü cihaz üzerinde yüz ve göz açıklığı kontrolü için kullanılır; ön video kaydedilmez veya yüklenmez. Bu uyarı destek amaçlıdır, sürüş güvenliğinin yerini tutmaz. Yeniden açtığında devreye girer.";
        }
        title.setMessage(str).setPositiveButton(z ? "Sonraki kayıtta kapat" : "Sonraki kayıtta aç", new DriveDashboard$$ExternalSyntheticLambda19(this, z)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$configureFront$11$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m8lambda$configureFront$11$localmertcanjarvisDriveDashboard(boolean z, DialogInterface dialogInterface, int i) {
        this.activity.getPreferences(0).edit().putBoolean("drive_front_enabled", !z).apply();
        this.status.setText("Ön kamera ayarını uygulamak için sürüş ekranını yeniden aç.");
    }

    private void checkFace() {
        TextureView front = this.frontTexture;
        if (this.closed || front == null) return;
        Bitmap bitmap = front.isAvailable() ? front.getBitmap(320, 240) : null;
        if (bitmap == null) {
            MAIN.postDelayed(this.faceCheck, 1200L);
            return;
        }
        try {
            this.eyeDetector.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener(faces -> {
                        if (this.closed) return;
                        if (faces.isEmpty()) {
                            this.driverStatus.setText("Sürücü yüzü görünmüyor");
                            this.eyesClosedSince = 0L;
                            this.drowsinessAlerted = false;
                            return;
                        }
                        Face face = faces.get(0);
                        Float left = face.getLeftEyeOpenProbability();
                        Float right = face.getRightEyeOpenProbability();
                        if (left == null || right == null) {
                            this.driverStatus.setText("Yüz algılandı · göz analizi bekleniyor");
                            return;
                        }
                        if (left < 0.22f && right < 0.22f) {
                            if (this.eyesClosedSince == 0L) {
                                this.eyesClosedSince = SystemClock.elapsedRealtime();
                            }
                            long closedFor = SystemClock.elapsedRealtime() - this.eyesClosedSince;
                            if (closedFor >= 1800L && !this.drowsinessAlerted) {
                                this.drowsinessAlerted = true;
                                this.driverStatus.setText("DİKKAT · gözler uzun süre kapalı");
                                alertDrowsiness();
                            } else {
                                this.driverStatus.setText("Göz kapanması izleniyor");
                            }
                        } else {
                            this.eyesClosedSince = 0L;
                            this.drowsinessAlerted = false;
                            this.driverStatus.setText("Gözler açık · sürücü izleniyor");
                        }
                    })
                    .addOnFailureListener(error -> {
                        if (!this.closed) this.driverStatus.setText("Sürücü takibi geçici olarak kullanılamıyor");
                    })
                    .addOnCompleteListener(task -> {
                        bitmap.recycle();
                        if (!this.closed) MAIN.postDelayed(this.faceCheck, 800L);
                    });
        } catch (RuntimeException error) {
            bitmap.recycle();
            MAIN.postDelayed(this.faceCheck, 1500L);
        }
    }

    private void alertDrowsiness() {
        try {
            if (this.driverAlarm == null) {
                this.driverAlarm = new ToneGenerator(AudioManager.STREAM_ALARM, 90);
            }
            this.driverAlarm.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1200);
        } catch (RuntimeException ignored) {
        }
    }

    /* JADX WARN: Code duplicated, block: B:14:0x002a A[PHI: r0 r1
      0x002a: PHI (r0v2 boolean) = (r0v0 boolean), (r0v4 boolean) binds: [B:13:0x0028, B:6:0x001b] A[DONT_GENERATE, DONT_INLINE]
      0x002a: PHI (r1v3 android.graphics.Bitmap) = (r1v2 android.graphics.Bitmap), (r1v5 android.graphics.Bitmap) binds: [B:13:0x0028, B:6:0x001b] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX INFO: renamed from: lambda$checkFace$13$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m7lambda$checkFace$13$localmertcanjarvisDriveDashboard(Bitmap bitmap) {
        boolean z = false;
        Bitmap bitmapCopy = null;
        try {
            bitmapCopy = bitmap.copy(Bitmap.Config.RGB_565, false);
            z = new FaceDetector(160, 120, 1).findFaces(bitmapCopy, new FaceDetector.Face[1]) > 0;
            if (bitmapCopy != null) {
                bitmapCopy.recycle();
            }
        } catch (RuntimeException unused) {
            if (bitmapCopy != null) {
                bitmapCopy.recycle();
            }
        } catch (Throwable th) {
            if (bitmapCopy != null) {
                bitmapCopy.recycle();
            }
            bitmap.recycle();
            throw th;
        }
        bitmap.recycle();
        MAIN.post(new DriveDashboard$$ExternalSyntheticLambda2(this, z));
    }

    /* JADX INFO: renamed from: lambda$checkFace$12$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m6lambda$checkFace$12$localmertcanjarvisDriveDashboard(boolean z) {
        if (this.closed) {
            return;
        }
        this.driverStatus.setText(z ? "Sürücü yüzü görünür" : "Yüz algılanmadı");
    }

    private void inspectRoad() {
        if (!this.texture.isAvailable() || this.mapExpanded || !this.started) {
            this.status.setText("İnceleme için arka kamera önizlemesi gerekli.");
        } else {
            if (this.roadBusy) {
                return;
            }
            new AlertDialog.Builder(this.activity).setTitle("Yol görüntüsünü incele").setMessage("Arka kameradan bir kare EVREN’e gönderilir. Plaka gibi bilgiler görüntüde bulunabilir.").setPositiveButton("Kareyi gönder", new DriveDashboard$$ExternalSyntheticLambda17(this)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
        }
    }

    /* JADX INFO: renamed from: lambda$inspectRoad$16$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m11lambda$inspectRoad$16$localmertcanjarvisDriveDashboard(DialogInterface dialogInterface, int i) {
        Bitmap bitmap;
        if (this.closed || this.roadBusy || (bitmap = this.texture.getBitmap(640, 480)) == null) {
            return;
        }
        this.roadBusy = true;
        this.status.setText("Yol karesi inceleniyor…");
        this.vision.execute(new DriveDashboard$$ExternalSyntheticLambda5(this, bitmap));
    }

    /* JADX INFO: renamed from: lambda$inspectRoad$15$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m10lambda$inspectRoad$15$localmertcanjarvisDriveDashboard(Bitmap bitmap) {
        String strDescribeRoad;
        try {
            strDescribeRoad = DriveCamera.describeRoad(bitmap);
        } catch (Exception unused) {
            strDescribeRoad = "EVREN yol karesini inceleyemedi.";
        } finally {
            bitmap.recycle();
        }
        MAIN.post(new DriveDashboard$$ExternalSyntheticLambda6(this, strDescribeRoad));
    }

    /* JADX INFO: renamed from: lambda$inspectRoad$14$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m9lambda$inspectRoad$14$localmertcanjarvisDriveDashboard(String str) {
        this.roadBusy = false;
        if (this.closed) {
            return;
        }
        this.status.setText(str);
    }

    private int dp(int i) {
        return Math.round(i * this.activity.getResources().getDisplayMetrics().density);
    }

    private GradientDrawable rounded(int i, int i2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(i2));
        return gradientDrawable;
    }

    private LinearLayout panel(String str) {
        LinearLayout linearLayout = new LinearLayout(this.activity);
        linearLayout.setOrientation(1);
        GradientDrawable gradientDrawableRounded = rounded(-15524565, 20);
        gradientDrawableRounded.setStroke(dp(1), -14076086);
        linearLayout.setBackground(gradientDrawableRounded);
        linearLayout.setClipToOutline(true);
        TextView textViewLabel = label(str, 13);
        textViewLabel.setTextColor(-4337436);
        textViewLabel.setGravity(8388611);
        linearLayout.addView(textViewLabel);
        return linearLayout;
    }

    private Button control(String str, int i, int i2) {
        Button button = new Button(this.activity);
        button.setText(str);
        button.setAllCaps(false);
        button.setTextSize(13.0f);
        button.setTextColor(i2);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(6), 0, dp(6), 0);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(922746879), rounded(i, 16), null));
        return button;
    }

    private void addControl(LinearLayout linearLayout, Button button, boolean z) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        if (z) {
            layoutParams.leftMargin = dp(8);
        }
        linearLayout.addView(button, layoutParams);
    }

    private void openGoogleMaps() {
        cameraOffAndOpenMaps(this.activity);
    }

    static String cameraOffAndOpenMaps(android.content.Context context) {
        DriveDashboard dashboard = current;
        boolean pip = false;
        if (dashboard != null) {
            dashboard.setPipMode(true);
            pip = Main.enterDrivingPip();
            if (!pip) {
                dashboard.setPipMode(false);
                if (dashboard.dialog.isShowing()) dashboard.dialog.dismiss();
                else dashboard.close();
            }
        }
        Uri.Builder builder = Uri.parse("https://www.google.com/maps/@").buildUpon()
                .appendQueryParameter("api", "1").appendQueryParameter("map_action", "map");
        if (hasFix) {
            builder.appendQueryParameter("center", String.format(Locale.US, "%.6f,%.6f", Double.valueOf(latitude), Double.valueOf(longitude)))
                    .appendQueryParameter("zoom", "16");
        }
        Uri mapUri = builder.build();
        try {
            context.startActivity(new Intent("android.intent.action.VIEW", mapUri)
                    .setPackage("com.google.android.apps.maps").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return pip
                    ? "Google Maps açılıyor. JARVIS kamera penceresi küçük PiP görünümünde kalacak."
                    : "Google Maps açılıyor.";
        } catch (ActivityNotFoundException missingMaps) {
            try {
                context.startActivity(new Intent("android.intent.action.VIEW", mapUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return pip
                        ? "Google Maps uygulaması bulunamadı; haritayı tarayıcıda açıyorum. JARVIS kamera penceresi açık kalacak."
                        : "Google Maps uygulaması bulunamadı; haritayı tarayıcıda açıyorum.";
            } catch (RuntimeException failure) {
                return "Google Maps açılamadı. Harita uygulamasını kontrol et.";
            }
        } catch (RuntimeException failure) {
            return "Google Maps açılamadı. Harita uygulamasını kontrol et.";
        }
    }

    /* JADX INFO: renamed from: lambda$openGoogleMaps$17$local-mertcan-jarvis-DriveDashboard, reason: not valid java name */
    /* synthetic */ void m20lambda$openGoogleMaps$17$localmertcanjarvisDriveDashboard(DialogInterface dialogInterface, int i) {
        String str = hasFix ? String.format(Locale.US, "%.6f,%.6f", Double.valueOf(latitude), Double.valueOf(longitude)) : "";
        Uri.Builder builderAppendQueryParameter = Uri.parse("https://www.google.com/maps/@").buildUpon().appendQueryParameter("api", "1").appendQueryParameter("map_action", "map");
        if (!str.isEmpty()) {
            builderAppendQueryParameter.appendQueryParameter("center", str).appendQueryParameter("zoom", "16");
        }
        try {
            try {
                this.activity.startActivity(new Intent("android.intent.action.VIEW", builderAppendQueryParameter.build()).setPackage("com.google.android.apps.maps"));
            } catch (ActivityNotFoundException unused) {
                this.activity.startActivity(new Intent("android.intent.action.VIEW", builderAppendQueryParameter.build()));
            }
        } catch (ActivityNotFoundException unused2) {
            this.status.setText("Google Maps açılamadı.");
        }
    }

    private TextView label(String str, int i) {
        TextView textView = new TextView(this.activity);
        textView.setText(str);
        textView.setTextColor(-1);
        textView.setTextSize(i);
        textView.setGravity(17);
        textView.setPadding(10, 6, 10, 6);
        return textView;
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        if (this.closed) {
            return;
        }
        surfaceTexture.setDefaultBufferSize(640, 480);
        rotate(i, i2);
        if (this.started) {
            return;
        }
        previewSurface = new Surface(surfaceTexture);
        startRecordingIfReady();
    }

    private void startRecordingIfReady() {
        if (this.closed || this.started || previewSurface == null) {
            return;
        }
        if (this.frontTexture == null || frontSurface != null) {
            try {
                this.activity.startForegroundService(new Intent(this.activity, (Class<?>) Dashcam.class).putExtra("sound", false).putExtra("front", false).putExtra("dual", this.frontTexture != null).putExtra("displayDegrees", this.activity.getWindowManager().getDefaultDisplay().getRotation() * 90));
                this.started = true;
                if (this.frontTexture != null) {
                    MAIN.postDelayed(this.faceCheck, 2500L);
                }
            } catch (RuntimeException unused) {
                this.status.setText("Kamera kaydı başlatılamadı.");
            }
        }
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        rotate(i, i2);
    }

    @Override // android.view.TextureView.SurfaceTextureListener
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (!this.started || this.closed) {
            return true;
        }
        this.retained = surfaceTexture;
        return false;
    }

    private void rotate(int i, int i2) {
        int iIntValue = 90;
        try {
            CameraManager cameraManager = (CameraManager) this.activity.getSystemService(CameraManager.class);
            String[] cameraIdList = cameraManager.getCameraIdList();
            int length = cameraIdList.length;
            int i3 = 0;
            while (true) {
                if (i3 < length) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdList[i3]);
                    Integer num = 1;
                    if (num.equals(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING))) {
                        Integer num2 = (Integer) cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                        if (num2 != null) {
                            iIntValue = num2.intValue();
                            break;
                        }
                    } else {
                        i3++;
                    }
                }
                iIntValue = 90;
                break;
            }
        } catch (Exception unused) {
        }
        int rotation = (((iIntValue - (this.activity.getWindowManager().getDefaultDisplay().getRotation() * 90)) + this.rotationCorrection) + 360) % 360;
        boolean z = rotation == 90 || rotation == 270;
        if (i == 0 || i2 == 0) {
            return;
        }
        float f = i;
        float f2 = i2;
        float fMax = Math.max(f / (z ? 480.0f : 640.0f), f2 / (z ? 640.0f : 480.0f));
        Matrix matrix = new Matrix();
        matrix.postTranslate((-i) / 2.0f, (-i2) / 2.0f);
        matrix.postScale((640.0f * fMax) / f, (fMax * 480.0f) / f2);
        matrix.postRotate(rotation);
        matrix.postTranslate(f / 2.0f, f2 / 2.0f);
        this.texture.setTransform(matrix);
    }

    private void close() {
        this.closed = true;
        Handler handler = MAIN;
        handler.removeCallbacks(this.faceCheck);
        this.vision.shutdownNow();
        this.eyeDetector.close();
        if (this.driverAlarm != null) {
            this.driverAlarm.release();
            this.driverAlarm = null;
        }
        if (current == this) {
            current = null;
        }
        Surface surface = previewSurface;
        previewSurface = null;
        Surface surface2 = frontSurface;
        frontSurface = null;
        frontId = null;
        SurfaceTexture surfaceTexture = this.retainedFront;
        if (this.started) {
            this.activity.stopService(new Intent(this.activity, (Class<?>) Dashcam.class));
        }
        handler.postDelayed(new DriveDashboard$$ExternalSyntheticLambda4(surface, this.retained, surface2, surfaceTexture), 800L);
        WebView webView = this.map;
        if (webView != null) {
            this.mapArea.removeView(webView);
            this.map.destroy();
            this.map = null;
        }
    }

    static /* synthetic */ void lambda$close$18(Surface surface, SurfaceTexture surfaceTexture, Surface surface2, SurfaceTexture surfaceTexture2) {
        if (surface != null) {
            surface.release();
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
        }
        if (surface2 != null) {
            surface2.release();
        }
        if (surfaceTexture2 != null) {
            surfaceTexture2.release();
        }
    }

    static void cameraStatus(String str) {
        MAIN.post(new DriveDashboard$$ExternalSyntheticLambda8(str));
    }

    static /* synthetic */ void lambda$cameraStatus$19(String str) {
        DriveDashboard driveDashboard = current;
        if (driveDashboard != null) {
            driveDashboard.status.setText(str);
        }
    }

    static void position(double d, double d2) {
        latitude = d;
        longitude = d2;
        hasFix = true;
        DriveDashboard driveDashboard = current;
        if (driveDashboard != null) {
            driveDashboard.updateMap();
        }
    }

    static void gps(int i, int i2, String str) {
        DriveDashboard driveDashboard = current;
        if (driveDashboard == null) {
            return;
        }
        driveDashboard.speed.setText(i + " km/sa  ·  " + i2 + " m / 2 sn");
        driveDashboard.traffic.setText(str);
        driveDashboard.traffic.setVisibility(str.isEmpty() ? 8 : 0);
    }

    static void gpsUnavailable() {
        DriveDashboard driveDashboard = current;
        if (driveDashboard != null) {
            driveDashboard.speed.setText("— km/sa · GPS hızı bekleniyor");
            driveDashboard.traffic.setVisibility(8);
        }
    }

    private void showMap() {
        if (current == this && this.map == null) {
            WebView webView = new WebView(this.activity);
            this.map = webView;
            webView.getSettings().setJavaScriptEnabled(true);
            this.map.getSettings().setAllowFileAccess(false);
            this.map.getSettings().setAllowContentAccess(false);
            this.map.getSettings().setUserAgentString(this.map.getSettings().getUserAgentString() + " JarvisAndroid/0.9.4");
            this.map.setWebViewClient(new AnonymousClass2());
            this.mapArea.removeAllViews();
            this.mapArea.addView(this.map, new LinearLayout.LayoutParams(-1, -1));
            this.map.loadDataWithBaseURL(MAP_BASE, MAP, "text/html", "UTF-8", MAP_BASE);
        }
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.DriveDashboard$2, reason: invalid class name */
    class AnonymousClass2 extends WebViewClient {
        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return true;
        }

        AnonymousClass2() {
        }

        @Override // android.webkit.WebViewClient
        public void onPageFinished(WebView webView, String str) {
            DriveDashboard.access$500(DriveDashboard.this);
        }
    }

    private void updateMap() {
        WebView webView = this.map;
        if (webView == null || !hasFix) {
            return;
        }
        webView.evaluateJavascript(String.format(Locale.US, "if(window.moveMap)moveMap(%.6f,%.6f)", Double.valueOf(latitude), Double.valueOf(longitude)), null);
    }

    static void createSession(CameraDevice cameraDevice, Surface surface, CaptureRequest.Builder builder, CameraCaptureSession.StateCallback stateCallback, Handler handler) throws Exception {
        Surface surface2 = cameraDevice.getId().equals(frontId) ? frontSurface : previewSurface;
        if (surface == null) {
            if (surface2 == null || !surface2.isValid()) throw new IllegalStateException("Önizleme yüzeyi hazır değil.");
            builder.addTarget(surface2);
            cameraDevice.createCaptureSession(Collections.singletonList(surface2), stateCallback, handler);
            return;
        }
        if (surface2 == null || !surface2.isValid()) {
            cameraDevice.createCaptureSession(Collections.singletonList(surface), stateCallback, handler);
            return;
        }
        AnonymousClass3 anonymousClass3 = new AnonymousClass3(stateCallback, builder, surface2, cameraDevice, surface, handler);
        builder.addTarget(surface2);
        try {
            cameraDevice.createCaptureSession(Arrays.asList(surface, surface2), anonymousClass3, handler);
        } catch (Exception unused) {
            builder.removeTarget(surface2);
            cameraStatus("Bu kamera, kayıtla birlikte önizleme açamadı.");
            cameraDevice.createCaptureSession(Collections.singletonList(surface), stateCallback, handler);
        }
    }

    /* JADX INFO: renamed from: local.mertcan.jarvis.DriveDashboard$3, reason: invalid class name */
    static class AnonymousClass3 extends CameraCaptureSession.StateCallback {
        final /* synthetic */ CameraDevice val$device;
        final /* synthetic */ Handler val$handler;
        final /* synthetic */ CameraCaptureSession.StateCallback val$next;
        final /* synthetic */ Surface val$preview;
        final /* synthetic */ CaptureRequest.Builder val$request;
        final /* synthetic */ Surface val$video;

        AnonymousClass3(CameraCaptureSession.StateCallback stateCallback, CaptureRequest.Builder builder, Surface surface, CameraDevice cameraDevice, Surface surface2, Handler handler) {
            this.val$next = stateCallback;
            this.val$request = builder;
            this.val$preview = surface;
            this.val$device = cameraDevice;
            this.val$video = surface2;
            this.val$handler = handler;
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            this.val$next.onConfigured(cameraCaptureSession);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            cameraCaptureSession.close();
            try {
                retry();
            } catch (Exception unused) {
                this.val$next.onConfigureFailed(cameraCaptureSession);
            }
        }

        void retry() throws Exception {
            this.val$request.removeTarget(this.val$preview);
            DriveDashboard.cameraStatus("Bu kamera, kayıtla birlikte önizleme açamadı. Kayıt sürüyor.");
            this.val$device.createCaptureSession(Collections.singletonList(this.val$video), this.val$next, this.val$handler);
        }
    }
}
