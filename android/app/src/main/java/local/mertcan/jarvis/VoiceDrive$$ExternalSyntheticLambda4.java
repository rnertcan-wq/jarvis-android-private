package local.mertcan.jarvis;

import android.media.AudioAttributes;
import android.speech.tts.TextToSpeech;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class VoiceDrive$$ExternalSyntheticLambda4 implements TextToSpeech.OnInitListener {
    public final /* synthetic */ VoiceDrive f$0;
    public final /* synthetic */ AudioAttributes f$1;

    public /* synthetic */ VoiceDrive$$ExternalSyntheticLambda4(VoiceDrive voiceDrive, AudioAttributes audioAttributes) {
        this.f$0 = voiceDrive;
        this.f$1 = audioAttributes;
    }

    @Override // android.speech.tts.TextToSpeech.OnInitListener
    public final void onInit(int i) {
        this.f$0.m105lambda$onStartCommand$1$localmertcanjarvisVoiceDrive(this.f$1, i);
    }
}
