package local.mertcan.jarvis;

import android.content.DialogInterface;
import android.content.SharedPreferences;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class TrafficMonitor$$ExternalSyntheticLambda4 implements DialogInterface.OnClickListener {
    public final /* synthetic */ SharedPreferences f$0;

    public /* synthetic */ TrafficMonitor$$ExternalSyntheticLambda4(SharedPreferences sharedPreferences) {
        this.f$0 = sharedPreferences;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        TrafficMonitor.lambda$settings$2(this.f$0, dialogInterface, i);
    }
}
