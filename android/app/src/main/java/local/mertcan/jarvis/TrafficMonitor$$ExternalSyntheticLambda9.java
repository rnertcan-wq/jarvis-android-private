package local.mertcan.jarvis;

import android.location.Location;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class TrafficMonitor$$ExternalSyntheticLambda9 implements Runnable {
    public final /* synthetic */ TrafficMonitor f$0;
    public final /* synthetic */ Location f$1;
    public final /* synthetic */ String f$2;

    public /* synthetic */ TrafficMonitor$$ExternalSyntheticLambda9(TrafficMonitor trafficMonitor, Location location, String str) {
        this.f$0 = trafficMonitor;
        this.f$1 = location;
        this.f$2 = str;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.m97lambda$updateTraffic$9$localmertcanjarvisTrafficMonitor(this.f$1, this.f$2);
    }
}
