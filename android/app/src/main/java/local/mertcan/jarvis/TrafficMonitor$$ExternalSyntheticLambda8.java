package local.mertcan.jarvis;

import android.content.DialogInterface;
import android.content.SharedPreferences;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class TrafficMonitor$$ExternalSyntheticLambda8 implements DialogInterface.OnClickListener {
    public final /* synthetic */ SharedPreferences f$0;
    public final /* synthetic */ boolean f$1;
    public final /* synthetic */ boolean f$2;
    public final /* synthetic */ Main f$3;

    public /* synthetic */ TrafficMonitor$$ExternalSyntheticLambda8(SharedPreferences sharedPreferences, boolean z, boolean z2, Main main) {
        this.f$0 = sharedPreferences;
        this.f$1 = z;
        this.f$2 = z2;
        this.f$3 = main;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        TrafficMonitor.lambda$settings$5(this.f$0, this.f$1, this.f$2, this.f$3, dialogInterface, i);
    }
}
