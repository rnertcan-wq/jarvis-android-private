package local.mertcan.jarvis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
final class GeminiAccess {
    private static volatile boolean foreground;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static WeakReference<Main> screen = new WeakReference<>(null);

    GeminiAccess() {
    }

    static void open(Main main) {
        screen = new WeakReference<>(main);
        if (!Cloud.key.isEmpty()) {
            main.confirmGemini();
            return;
        }
        EditText editText = new EditText(main);
        editText.setSingleLine(true);
        editText.setInputType(129);
        editText.setHint("Gemini API anahtarı");
        new AlertDialog.Builder(main).setTitle("Gemini Live bağlantısı").setMessage("Google AI Studio’dan aldığın, Live API erişimi olan anahtarı gir.").setView(editText).setPositiveButton("Kaydet ve devam et", new GeminiAccess$$ExternalSyntheticLambda4(editText, main)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$open$0(EditText editText, Main main, DialogInterface dialogInterface, int i) {
        String strTrim = editText.getText().toString().trim();
        if (!strTrim.matches("[A-Za-z0-9_-]{20,128}")) {
            Toast.makeText(main, "Gemini anahtar biçimini kontrol et.", 1).show();
            return;
        }
        try {
            Secrets.save(main, "gemini", strTrim);
            Cloud.key = strTrim;
            main.confirmGemini();
        } catch (Exception unused) {
            Toast.makeText(main, "Gemini anahtarı kaydedilemedi.", 1).show();
        }
    }

    static String liveUrl() {
        return "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=" + Uri.encode(Cloud.key);
    }

    static String setup() throws JSONException {
        JSONArray jSONArrayPut = new JSONArray().put(function("sleep_live", "Sessiz ol, Live modu bitir veya sohbeti bitir komutunda beklemeye dön.", null)).put(function("map_view", "Sürüş ekranındaki haritayı büyütmek için size=expand, küçültmek için size=shrink gönder.", "size")).put(function("stop_music", "Müziği duraklat veya YouTube Music'i kapat.", null)).put(function("device_clock", "Telefonun gerçek yerel saat ve tarihini oku.", null)).put(function("phone_call", "Telefon araması için onay ekranını aç.", "target")).put(function("research", "Google'da web araması ve güncel bilgi doğrulaması yap.", "query")).put(function("rates", "Güncel döviz kurlarını oku.", null)).put(function("news", "Güncel haber başlıklarını oku.", null));
        JSONObject jSONObjectFunction = function("music", "Müzik aç, duraklat veya devam ettir.", "action");
        jSONObjectFunction.getJSONObject("parameters").getJSONObject("properties").put("query", new JSONObject().put("type", "STRING"));
        jSONArrayPut.put(jSONObjectFunction);
        return new JSONObject().put("setup", new JSONObject().put("model", "models/gemini-3.8-live").put("generationConfig", new JSONObject().put("responseModalities", new JSONArray().put("AUDIO"))).put("inputAudioTranscription", new JSONObject()).put("outputAudioTranscription", new JSONObject()).put("tools", new JSONArray().put(new JSONObject().put("functionDeclarations", jSONArrayPut))).put("systemInstruction", new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", "Türkçe konuş. Saat ve tarih için device_clock, internet için research, telefon için phone_call, müziği durdurmak için stop_music kullan. YouTube Music açmak için music ve action=youtube_music kullan. Sessiz ol veya Live modu bitir denirse sleep_live çağır. Harita veya Maps büyüt/küçült komutlarında map_view kullan. Araç sonucu olmadan yetki veya başarı hakkında tahmin yürütme."))))).toString();
    }

    static String startupFailure(Throwable th) {
        return "Gemini Live başlatılamadı · " + th.getClass().getSimpleName();
    }

    private static JSONObject function(String str, String str2, String str3) throws JSONException {
        JSONObject jSONObjectPut = new JSONObject().put("name", str).put("description", str2);
        return str3 == null ? jSONObjectPut : jSONObjectPut.put("parameters", new JSONObject().put("type", "OBJECT").put("properties", new JSONObject().put(str3, new JSONObject().put("type", "STRING"))).put("required", new JSONArray().put(str3)));
    }

    static String dispatchDeviceTool(Context context, String str, JSONObject jSONObject) {
        String strTrim;
        if ("map_view".equals(str)) {
            JSONObject jSONObjectOptJSONObject = jSONObject.optJSONObject("args");
            strTrim = jSONObjectOptJSONObject != null ? jSONObjectOptJSONObject.optString("size") : "";
            if ("expand".equals(strTrim)) {
                return DriveDashboard.mapCommand(true);
            }
            return "shrink".equals(strTrim) ? DriveDashboard.mapCommand(false) : "Harita boyutu için büyüt veya küçült de.";
        }
        if ("stop_music".equals(str)) {
            AudioManager audioManager = (AudioManager) context.getSystemService(AudioManager.class);
            if (audioManager == null) {
                return "Android medya denetimi kullanılamıyor.";
            }
            try {
                audioManager.dispatchMediaKeyEvent(new KeyEvent(0, 127));
                audioManager.dispatchMediaKeyEvent(new KeyEvent(1, 127));
                return "Durdurma komutu Android'in etkin medya oynatıcısına gönderildi.";
            } catch (RuntimeException unused) {
                return "Medya durdurma komutu gönderilemedi.";
            }
        }
        if ("device_clock".equals(str)) {
            return new SimpleDateFormat("d MMMM yyyy EEEE, saat HH:mm", Locale.forLanguageTag("tr-TR")).format(new Date());
        }
        if (!"phone_call".equals(str)) {
            return null;
        }
        JSONObject jSONObjectOptJSONObject2 = jSONObject.optJSONObject("args");
        strTrim = jSONObjectOptJSONObject2 != null ? jSONObjectOptJSONObject2.optString("target").trim() : "";
        if (strTrim.isEmpty() || strTrim.length() > 100) {
            return "Aranacak adı veya numarayı sor.";
        }
        Main main = screen.get();
        if (main == null || main.isDestroyed() || !foreground) {
            return "Arama onayı için JARVIS ekranını aç.";
        }
        MAIN.post(new GeminiAccess$$ExternalSyntheticLambda2(main, strTrim));
        return "Telefon aramasını başlatmak için ekrandaki onayı iste.";
    }

    static /* synthetic */ void lambda$dispatchDeviceTool$2(Main main, String str) {
        if (!main.isDestroyed() && foreground && GeminiLive.active) {
            new AlertDialog.Builder(main).setTitle("Telefon araması").setMessage(str + " aransın mı?").setPositiveButton("Ara", new GeminiAccess$$ExternalSyntheticLambda3(main, str)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
        }
    }

    static /* synthetic */ void lambda$dispatchDeviceTool$1(Main main, String str, DialogInterface dialogInterface, int i) {
        String strLocal = LiveRepair.local(main, "ara " + str);
        if (strLocal != null) {
            main.showLiveStatus(strLocal);
        }
    }

    static void foreground(Main main, boolean z) {
        foreground = z;
        if (z) {
            screen = new WeakReference<>(main);
        }
    }

    static String closeMessage(int i, String str, boolean z) {
        String str2 = z ? "konuşma sırasında" : "bağlantı kurulurken";
        String str3 = "";
        if (str == null) {
            str = "";
        }
        String str4 = Cloud.key;
        if (str4 != null && !str4.isEmpty()) {
            str = str.replace(str4, "[gizlendi]");
        }
        String strTrim = str.replaceAll("[\\p{Cntrl}]+", " ").trim();
        if (strTrim.length() > 180) {
            strTrim = strTrim.substring(0, 180);
        }
        StringBuilder sb = new StringBuilder("Gemini Live ");
        sb.append(str2);
        sb.append(" kapandı (");
        sb.append(i);
        sb.append(").");
        if (!strTrim.isEmpty()) {
            str3 = " " + strTrim;
        }
        sb.append(str3);
        return sb.toString();
    }

    static void reportStatus(String str) {
        MAIN.post(new GeminiAccess$$ExternalSyntheticLambda0(str));
    }

    static /* synthetic */ void lambda$reportStatus$3(String str) {
        Main main = screen.get();
        if (main != null && !main.isDestroyed()) {
            main.showLiveStatus(str);
        }
        DriveDashboard.liveStatus(str);
    }

    static void report(Context context, String str) {
        MAIN.post(new GeminiAccess$$ExternalSyntheticLambda1(str, context));
    }

    static /* synthetic */ void lambda$report$4(String str, Context context) {
        Main main = screen.get();
        if (main != null && !main.isDestroyed()) {
            main.showLiveStatus(str);
        }
        Toast.makeText(context, str, 1).show();
    }
}
