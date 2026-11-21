package local.mertcan.jarvis;

import android.media.MediaPlayer;
import java.util.concurrent.CountDownLatch;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class EvrenTalk$$ExternalSyntheticLambda5 implements MediaPlayer.OnErrorListener {
    public final /* synthetic */ CountDownLatch f$0;

    public /* synthetic */ EvrenTalk$$ExternalSyntheticLambda5(CountDownLatch countDownLatch) {
        this.f$0 = countDownLatch;
    }

    @Override // android.media.MediaPlayer.OnErrorListener
    public final boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return EvrenTalk.lambda$play$7(this.f$0, mediaPlayer, i, i2);
    }
}
