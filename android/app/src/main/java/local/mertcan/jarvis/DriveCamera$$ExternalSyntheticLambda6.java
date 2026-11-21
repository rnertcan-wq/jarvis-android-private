package local.mertcan.jarvis;

import android.content.Context;
import java.io.File;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class DriveCamera$$ExternalSyntheticLambda6 implements Runnable {
    public final /* synthetic */ File[] f$0;
    public final /* synthetic */ Context f$1;

    public /* synthetic */ DriveCamera$$ExternalSyntheticLambda6(File[] fileArr, Context context) {
        this.f$0 = fileArr;
        this.f$1 = context;
    }

    @Override // java.lang.Runnable
    public final void run() {
        DriveCamera.lambda$pinLatest$5(this.f$0, this.f$1);
    }
}
