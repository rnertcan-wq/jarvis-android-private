package local.mertcan.jarvis;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class VoiceDrive$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ VoiceDrive f$0;

    public /* synthetic */ VoiceDrive$$ExternalSyntheticLambda1(VoiceDrive voiceDrive) {
        this.f$0 = voiceDrive;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.stopSelf();
    }
}
