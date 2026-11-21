package local.mertcan.jarvis;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class VoiceDrive$$ExternalSyntheticLambda5 implements Runnable {
    public final /* synthetic */ VoiceDrive f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ Cloud.Result f$2;

    public /* synthetic */ VoiceDrive$$ExternalSyntheticLambda5(VoiceDrive voiceDrive, int i, Cloud.Result result) {
        this.f$0 = voiceDrive;
        this.f$1 = i;
        this.f$2 = result;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.m99lambda$command$2$localmertcanjarvisVoiceDrive(this.f$1, this.f$2);
    }
}
