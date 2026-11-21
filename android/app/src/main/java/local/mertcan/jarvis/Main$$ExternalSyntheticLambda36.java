package local.mertcan.jarvis;

import android.media.AudioAttributes;
import android.speech.tts.TextToSpeech;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Main$$ExternalSyntheticLambda36 implements TextToSpeech.OnInitListener {
    public final /* synthetic */ Main f$0;
    public final /* synthetic */ AudioAttributes f$1;

    public /* synthetic */ Main$$ExternalSyntheticLambda36(Main main, AudioAttributes audioAttributes) {
        this.f$0 = main;
        this.f$1 = audioAttributes;
    }

    @Override // android.speech.tts.TextToSpeech.OnInitListener
    public final void onInit(int i) {
        this.f$0.m77lambda$onCreate$2$localmertcanjarvisMain(this.f$1, i);
    }
}
