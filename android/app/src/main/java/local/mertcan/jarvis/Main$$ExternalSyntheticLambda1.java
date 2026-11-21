package local.mertcan.jarvis;

import android.content.DialogInterface;
import android.content.Intent;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Main$$ExternalSyntheticLambda1 implements DialogInterface.OnClickListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ Intent f$3;

    public /* synthetic */ Main$$ExternalSyntheticLambda1(Main main, String str, String str2, Intent intent) {
        this.f$0 = main;
        this.f$1 = str;
        this.f$2 = str2;
        this.f$3 = intent;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.m50lambda$deliverTelegram$19$localmertcanjarvisMain(this.f$1, this.f$2, this.f$3, dialogInterface, i);
    }
}
