package local.mertcan.jarvis;

import android.media.MediaRecorder;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Dashcam$Track$$ExternalSyntheticLambda0 implements MediaRecorder.OnErrorListener {
    public final /* synthetic */ Dashcam.Track f$0;

    public /* synthetic */ Dashcam$Track$$ExternalSyntheticLambda0(Dashcam.Track track) {
        this.f$0 = track;
    }

    @Override // android.media.MediaRecorder.OnErrorListener
    public final void onError(MediaRecorder mediaRecorder, int i, int i2) {
        this.f$0.m5lambda$prepare$0$localmertcanjarvisDashcam$Track(mediaRecorder, i, i2);
    }
}
