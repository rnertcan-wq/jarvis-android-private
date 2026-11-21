package local.mertcan.jarvis;

import java.util.concurrent.Callable;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class LiveRepair$$ExternalSyntheticLambda2 implements Callable {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ VoiceDrive f$3;

    public /* synthetic */ LiveRepair$$ExternalSyntheticLambda2(String str, String str2, String str3, VoiceDrive voiceDrive) {
        this.f$0 = str;
        this.f$1 = str2;
        this.f$2 = str3;
        this.f$3 = voiceDrive;
    }

    @Override // java.util.concurrent.Callable
    public final Object call() throws Exception {
        return LiveRepair.lambda$answer$2(this.f$0, this.f$1, this.f$2, this.f$3);
    }
}
