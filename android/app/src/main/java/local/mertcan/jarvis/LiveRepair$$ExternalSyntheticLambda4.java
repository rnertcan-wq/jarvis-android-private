package local.mertcan.jarvis;

import android.media.MediaPlayer;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class LiveRepair$$ExternalSyntheticLambda4 implements MediaPlayer.OnPreparedListener {
    public final /* synthetic */ VoiceDrive f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ LiveRepair$$ExternalSyntheticLambda4(VoiceDrive voiceDrive, String str) {
        this.f$0 = voiceDrive;
        this.f$1 = str;
    }

    @Override // android.media.MediaPlayer.OnPreparedListener
    public final void onPrepared(MediaPlayer mediaPlayer) {
        LiveRepair.lambda$play$3(this.f$0, this.f$1, mediaPlayer);
    }
}
