package local.mertcan.jarvis;

import android.content.DialogInterface;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class GeminiAccess$$ExternalSyntheticLambda3 implements DialogInterface.OnClickListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ GeminiAccess$$ExternalSyntheticLambda3(Main main, String str) {
        this.f$0 = main;
        this.f$1 = str;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        GeminiAccess.lambda$dispatchDeviceTool$1(this.f$0, this.f$1, dialogInterface, i);
    }
}
