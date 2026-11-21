package local.mertcan.jarvis;

import android.media.MediaPlayer;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class LiveRepair$$ExternalSyntheticLambda6 implements MediaPlayer.OnErrorListener {
    public final /* synthetic */ VoiceDrive f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ LiveRepair$$ExternalSyntheticLambda6(VoiceDrive voiceDrive, String str) {
        this.f$0 = voiceDrive;
        this.f$1 = str;
    }

    @Override // android.media.MediaPlayer.OnErrorListener
    public final boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return LiveRepair.lambda$play$5(this.f$0, this.f$1, mediaPlayer, i, i2);
    }
}
