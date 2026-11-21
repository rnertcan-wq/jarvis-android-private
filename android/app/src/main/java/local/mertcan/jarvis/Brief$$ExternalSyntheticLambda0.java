package local.mertcan.jarvis;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes.dex */
public final /* synthetic */ class Brief$$ExternalSyntheticLambda0 implements EntityResolver {
    @Override // org.xml.sax.EntityResolver
    public final InputSource resolveEntity(String str, String str2) throws org.xml.sax.SAXException, java.io.IOException {
        return Brief.lambda$load$0(str, str2);
    }
}
