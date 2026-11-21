package local.mertcan.jarvis;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class TrafficMonitor$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ TrafficMonitor f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ String f$2;

    public /* synthetic */ TrafficMonitor$$ExternalSyntheticLambda0(TrafficMonitor trafficMonitor, String str, String str2) {
        this.f$0 = trafficMonitor;
        this.f$1 = str;
        this.f$2 = str2;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.m96lambda$updateTraffic$8$localmertcanjarvisTrafficMonitor(this.f$1, this.f$2);
    }
}
