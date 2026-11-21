package local.mertcan.jarvis;

import android.media.MediaPlayer;
import java.util.concurrent.CountDownLatch;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class EvrenTalk$$ExternalSyntheticLambda4 implements MediaPlayer.OnCompletionListener {
    public final /* synthetic */ CountDownLatch f$0;

    public /* synthetic */ EvrenTalk$$ExternalSyntheticLambda4(CountDownLatch countDownLatch) {
        this.f$0 = countDownLatch;
    }

    @Override // android.media.MediaPlayer.OnCompletionListener
    public final void onCompletion(MediaPlayer mediaPlayer) {
        EvrenTalk.lambda$play$6(this.f$0, mediaPlayer);
    }
}
