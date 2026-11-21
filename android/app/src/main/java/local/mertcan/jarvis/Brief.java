package local.mertcan.jarvis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/* JADX INFO: loaded from: classes.dex */
final class Brief {
    Brief() {
    }

    private static Document load(String str) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
        httpURLConnection.setConnectTimeout(6000);
        httpURLConnection.setReadTimeout(7000);
        httpURLConnection.setInstanceFollowRedirects(false);
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            try {
                if (httpURLConnection.getResponseCode() != 200) {
                    throw new IOException("Unexpected response");
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[8192];
                while (true) {
                    int i = inputStream.read(bArr);
                    if (i != -1) {
                        if (byteArrayOutputStream.size() + i > 1048576) {
                            throw new IOException("Feed too large");
                        }
                        byteArrayOutputStream.write(bArr, 0, i);
                    } else {
                        DocumentBuilder documentBuilderNewDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        documentBuilderNewDocumentBuilder.setEntityResolver(new Brief$$ExternalSyntheticLambda0());
                        Document document = documentBuilderNewDocumentBuilder.parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        httpURLConnection.disconnect();
                        return document;
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            httpURLConnection.disconnect();
            throw th3;
        }
    }

    static /* synthetic */ InputSource lambda$load$0(String str, String str2) throws SAXException, IOException {
        throw new SAXException("External entity rejected");
    }

    static String rates() throws Exception {
        Element documentElement = load("https://www.tcmb.gov.tr/kurlar/today.xml").getDocumentElement();
        NodeList elementsByTagName = documentElement.getElementsByTagName("Currency");
        ArrayList arrayList = new ArrayList();
        String[] strArr = {"USD", "EUR", "GBP"};
        for (int i = 0; i < 3; i++) {
            String str = strArr[i];
            for (int i2 = 0; i2 < elementsByTagName.getLength(); i2++) {
                Element element = (Element) elementsByTagName.item(i2);
                if (str.equals(element.getAttribute("CurrencyCode"))) {
                    Node nodeItem = element.getElementsByTagName("ForexSelling").item(0);
                    if (nodeItem == null) {
                        break;
                    }
                    NumberFormat numberInstance = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
                    numberInstance.setMaximumFractionDigits(2);
                    arrayList.add((str.equals("USD") ? "Dolar" : str.equals("EUR") ? "Euro" : "Sterlin") + " " + numberInstance.format(Double.parseDouble(nodeItem.getTextContent().trim())) + " lira");
                    break;
                }
            }
        }
        if (arrayList.isEmpty()) {
            throw new IOException("No rates");
        }
        return "TCMB " + documentElement.getAttribute("Tarih") + " döviz satış: " + String.join(", ", arrayList) + ".";
    }

    static String news() throws Exception {
        NodeList elementsByTagName = load("https://news.google.com/rss?hl=tr&gl=TR&ceid=TR:tr").getElementsByTagName("item");
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < elementsByTagName.getLength() && arrayList.size() < 3; i++) {
            Node nodeItem = ((Element) elementsByTagName.item(i)).getElementsByTagName("title").item(0);
            if (nodeItem != null) {
                String strTrim = nodeItem.getTextContent().trim();
                if (!strTrim.isEmpty()) {
                    arrayList.add(strTrim.substring(0, Math.min(145, strTrim.length())));
                }
            }
        }
        if (arrayList.isEmpty()) {
            throw new IOException("No headlines");
        }
        return "Günün başlıkları: " + String.join(". ", arrayList) + ".";
    }
}
