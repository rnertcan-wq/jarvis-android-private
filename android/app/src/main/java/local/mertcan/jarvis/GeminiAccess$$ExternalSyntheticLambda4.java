package local.mertcan.jarvis;

import android.content.DialogInterface;
import android.widget.EditText;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class GeminiAccess$$ExternalSyntheticLambda4 implements DialogInterface.OnClickListener {
    public final /* synthetic */ EditText f$0;
    public final /* synthetic */ Main f$1;

    public /* synthetic */ GeminiAccess$$ExternalSyntheticLambda4(EditText editText, Main main) {
        this.f$0 = editText;
        this.f$1 = main;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        GeminiAccess.lambda$open$0(this.f$0, this.f$1, dialogInterface, i);
    }
}
