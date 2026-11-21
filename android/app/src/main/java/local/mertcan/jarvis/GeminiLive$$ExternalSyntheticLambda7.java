package local.mertcan.jarvis;

import okhttp3.WebSocket;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class GeminiLive$$ExternalSyntheticLambda7 implements Runnable {
    public final /* synthetic */ GeminiLive f$0;
    public final /* synthetic */ WebSocket f$1;

    public /* synthetic */ GeminiLive$$ExternalSyntheticLambda7(GeminiLive geminiLive, WebSocket webSocket) {
        this.f$0 = geminiLive;
        this.f$1 = webSocket;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.m31lambda$receive$2$localmertcanjarvisGeminiLive(this.f$1);
    }
}
