package local.mertcan.jarvis;

import android.location.Location;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class TrafficMonitor$$ExternalSyntheticLambda2 implements Runnable {
    public final /* synthetic */ TrafficMonitor f$0;
    public final /* synthetic */ Location f$1;

    public /* synthetic */ TrafficMonitor$$ExternalSyntheticLambda2(TrafficMonitor trafficMonitor, Location location) {
        this.f$0 = trafficMonitor;
        this.f$1 = location;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.m95lambda$updateCameras$7$localmertcanjarvisTrafficMonitor(this.f$1);
    }
}
