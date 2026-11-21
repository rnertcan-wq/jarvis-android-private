package local.mertcan.jarvis;

import java.io.File;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class LiveRepair$$ExternalSyntheticLambda3 implements Runnable {
    public final /* synthetic */ VoiceDrive f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ File f$2;

    public /* synthetic */ LiveRepair$$ExternalSyntheticLambda3(VoiceDrive voiceDrive, String str, File file) {
        this.f$0 = voiceDrive;
        this.f$1 = str;
        this.f$2 = file;
    }

    @Override // java.lang.Runnable
    public final void run() {
        LiveRepair.lambda$answer$1(this.f$0, this.f$1, this.f$2);
    }
}
