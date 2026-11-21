package local.mertcan.jarvis;

import android.media.MediaPlayer;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class LiveRepair$$ExternalSyntheticLambda5 implements MediaPlayer.OnCompletionListener {
    public final /* synthetic */ VoiceDrive f$0;

    public /* synthetic */ LiveRepair$$ExternalSyntheticLambda5(VoiceDrive voiceDrive) {
        this.f$0 = voiceDrive;
    }

    @Override // android.media.MediaPlayer.OnCompletionListener
    public final void onCompletion(MediaPlayer mediaPlayer) {
        LiveRepair.lambda$play$4(this.f$0, mediaPlayer);
    }
}
