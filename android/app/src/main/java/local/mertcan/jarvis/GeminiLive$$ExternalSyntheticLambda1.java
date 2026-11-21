package local.mertcan.jarvis;

import okhttp3.WebSocket;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes2.dex */
public final /* synthetic */ class GeminiLive$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ GeminiLive f$0;
    public final /* synthetic */ WebSocket f$1;
    public final /* synthetic */ String f$2;

    public /* synthetic */ GeminiLive$$ExternalSyntheticLambda1(GeminiLive geminiLive, WebSocket webSocket, String str) {
        this.f$0 = geminiLive;
        this.f$1 = webSocket;
        this.f$2 = str;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.f$0.m34lambda$sessionError$1$localmertcanjarvisGeminiLive(this.f$1, this.f$2);
    }
}
