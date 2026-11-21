package local.mertcan.jarvis;

import android.content.DialogInterface;
import java.io.File;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Main$$ExternalSyntheticLambda12 implements DialogInterface.OnClickListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ File[] f$1;

    public /* synthetic */ Main$$ExternalSyntheticLambda12(Main main, File[] fileArr) {
        this.f$0 = main;
        this.f$1 = fileArr;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.m63lambda$exportRecording$51$localmertcanjarvisMain(this.f$1, dialogInterface, i);
    }
}
