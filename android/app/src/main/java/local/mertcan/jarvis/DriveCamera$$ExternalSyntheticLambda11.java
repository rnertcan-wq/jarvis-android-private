package local.mertcan.jarvis;

import android.app.Activity;
import android.app.AlertDialog;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class DriveCamera$$ExternalSyntheticLambda11 implements Runnable {
    public final /* synthetic */ Activity f$0;
    public final /* synthetic */ AlertDialog f$1;
    public final /* synthetic */ String f$2;

    public /* synthetic */ DriveCamera$$ExternalSyntheticLambda11(Activity activity, AlertDialog alertDialog, String str) {
        this.f$0 = activity;
        this.f$1 = alertDialog;
        this.f$2 = str;
    }

    @Override // java.lang.Runnable
    public final void run() {
        DriveCamera.lambda$analyze$8(this.f$0, this.f$1, this.f$2);
    }
}
