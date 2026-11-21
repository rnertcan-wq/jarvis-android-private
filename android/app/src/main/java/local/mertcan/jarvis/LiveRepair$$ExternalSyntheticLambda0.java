package local.mertcan.jarvis;

import android.content.Context;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class LiveRepair$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ Context f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ LiveRepair$$ExternalSyntheticLambda0(Context context, String str) {
        this.f$0 = context;
        this.f$1 = str;
    }

    @Override // java.lang.Runnable
    public final void run() {
        LiveRepair.lambda$geminiCommand$7(this.f$0, this.f$1);
    }
}
