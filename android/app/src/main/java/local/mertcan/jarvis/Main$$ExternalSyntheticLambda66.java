package local.mertcan.jarvis;

import android.content.DialogInterface;
import android.widget.EditText;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Main$$ExternalSyntheticLambda66 implements DialogInterface.OnClickListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ EditText f$1;

    public /* synthetic */ Main$$ExternalSyntheticLambda66(Main main, EditText editText) {
        this.f$0 = main;
        this.f$1 = editText;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.m90lambda$searchDialog$36$localmertcanjarvisMain(this.f$1, dialogInterface, i);
    }
}
