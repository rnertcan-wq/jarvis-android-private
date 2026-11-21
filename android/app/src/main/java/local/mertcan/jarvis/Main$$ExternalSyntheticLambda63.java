package local.mertcan.jarvis;

import android.media.MediaPlayer;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Main$$ExternalSyntheticLambda63 implements MediaPlayer.OnErrorListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ Main$$ExternalSyntheticLambda63(Main main, String str) {
        this.f$0 = main;
        this.f$1 = str;
    }

    @Override // android.media.MediaPlayer.OnErrorListener
    public final boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return this.f$0.m57lambda$evrenChat$32$localmertcanjarvisMain(this.f$1, mediaPlayer, i, i2);
    }
}
