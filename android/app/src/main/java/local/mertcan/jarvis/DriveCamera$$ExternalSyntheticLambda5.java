package local.mertcan.jarvis;

import android.content.DialogInterface;
import java.io.File;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class DriveCamera$$ExternalSyntheticLambda5 implements DialogInterface.OnClickListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ File[] f$1;

    public /* synthetic */ DriveCamera$$ExternalSyntheticLambda5(Main main, File[] fileArr) {
        this.f$0 = main;
        this.f$1 = fileArr;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        DriveCamera.lambda$selectClip$7(this.f$0, this.f$1, dialogInterface, i);
    }
}
