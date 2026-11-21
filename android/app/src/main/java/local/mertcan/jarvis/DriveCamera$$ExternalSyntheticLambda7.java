package local.mertcan.jarvis;

import android.app.Activity;
import android.app.AlertDialog;
import java.io.File;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class DriveCamera$$ExternalSyntheticLambda7 implements Runnable {
    public final /* synthetic */ File f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ Activity f$2;
    public final /* synthetic */ AlertDialog f$3;

    public /* synthetic */ DriveCamera$$ExternalSyntheticLambda7(File file, String str, Activity activity, AlertDialog alertDialog) {
        this.f$0 = file;
        this.f$1 = str;
        this.f$2 = activity;
        this.f$3 = alertDialog;
    }

    @Override // java.lang.Runnable
    public final void run() {
        DriveCamera.lambda$analyze$9(this.f$0, this.f$1, this.f$2, this.f$3);
    }
}
