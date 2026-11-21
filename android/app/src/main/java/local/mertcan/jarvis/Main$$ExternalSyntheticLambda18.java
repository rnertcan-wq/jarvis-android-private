package local.mertcan.jarvis;

import android.content.DialogInterface;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Main$$ExternalSyntheticLambda18 implements DialogInterface.OnClickListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ boolean f$1;
    public final /* synthetic */ boolean f$2;

    public /* synthetic */ Main$$ExternalSyntheticLambda18(Main main, boolean z, boolean z2) {
        this.f$0 = main;
        this.f$1 = z;
        this.f$2 = z2;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.m48lambda$confirmRecord$47$localmertcanjarvisMain(this.f$1, this.f$2, dialogInterface, i);
    }
}
