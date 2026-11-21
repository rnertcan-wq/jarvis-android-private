package local.mertcan.jarvis;

import android.content.Context;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class GeminiAccess$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ Context f$1;

    public /* synthetic */ GeminiAccess$$ExternalSyntheticLambda1(String str, Context context) {
        this.f$0 = str;
        this.f$1 = context;
    }

    @Override // java.lang.Runnable
    public final void run() {
        GeminiAccess.lambda$report$4(this.f$0, this.f$1);
    }
}
