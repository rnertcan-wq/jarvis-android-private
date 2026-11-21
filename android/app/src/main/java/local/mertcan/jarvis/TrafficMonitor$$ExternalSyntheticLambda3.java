package local.mertcan.jarvis;

import android.location.Location;
import java.util.ArrayList;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class TrafficMonitor$$ExternalSyntheticLambda3 implements Runnable {
    public final /* synthetic */ TrafficMonitor f$0;
    public final /* synthetic */ ArrayList f$1;
    public final /* synthetic */ Location f$2;

    public /* synthetic */ TrafficMonitor$$ExternalSyntheticLambda3(TrafficMonitor trafficMonitor, ArrayList arrayList, Location location) {
        this.f$0 = trafficMonitor;
        this.f$1 = arrayList;
        this.f$2 = location;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.m94lambda$updateCameras$6$localmertcanjarvisTrafficMonitor(this.f$1, this.f$2);
    }
}
