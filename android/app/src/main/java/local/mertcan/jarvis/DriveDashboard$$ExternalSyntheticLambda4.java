package local.mertcan.jarvis;

import android.graphics.SurfaceTexture;
import android.view.Surface;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class DriveDashboard$$ExternalSyntheticLambda4 implements Runnable {
    public final /* synthetic */ Surface f$0;
    public final /* synthetic */ SurfaceTexture f$1;
    public final /* synthetic */ Surface f$2;
    public final /* synthetic */ SurfaceTexture f$3;

    public /* synthetic */ DriveDashboard$$ExternalSyntheticLambda4(Surface surface, SurfaceTexture surfaceTexture, Surface surface2, SurfaceTexture surfaceTexture2) {
        this.f$0 = surface;
        this.f$1 = surfaceTexture;
        this.f$2 = surface2;
        this.f$3 = surfaceTexture2;
    }

    @Override // java.lang.Runnable
    public final void run() {
        DriveDashboard.lambda$close$18(this.f$0, this.f$1, this.f$2, this.f$3);
    }
}
