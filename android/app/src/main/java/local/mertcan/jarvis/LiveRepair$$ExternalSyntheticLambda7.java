package local.mertcan.jarvis;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class LiveRepair$$ExternalSyntheticLambda7 implements Runnable {
    public final /* synthetic */ VoiceDrive f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ LiveRepair$$ExternalSyntheticLambda7(VoiceDrive voiceDrive, String str) {
        this.f$0 = voiceDrive;
        this.f$1 = str;
    }

    @Override // java.lang.Runnable
    public final void run() {
        LiveRepair.lambda$play$6(this.f$0, this.f$1);
    }
}
