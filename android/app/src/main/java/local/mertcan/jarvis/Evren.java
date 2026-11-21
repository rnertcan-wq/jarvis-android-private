package local.mertcan.jarvis;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
final class Evren {
    private static final String BASE = "https://evren-llmapi.ssyz.org.tr/v1/";
    static volatile String key = "";
    private static final MediaType JSON = MediaType.parse("application/json");

    Evren() {
    }

    static String error(Exception exc) {
        String message = exc.getMessage() == null ? "" : exc.getMessage();
        if (message.equals("EVREN HTTP 401") || message.equals("EVREN HTTP 403")) {
            return "EVREN anahtarı veya proje erişimi reddedildi.";
        }
        if (message.equals("EVREN HTTP 429")) {
            return "EVREN kotası dolu veya istek sınırı aşıldı.";
        }
        if ((exc instanceof SocketTimeoutException) || (exc instanceof InterruptedIOException)) {
            return "EVREN yanıtı zaman aşımına uğradı.";
        }
        return message.equals("EVREN boş yanıt verdi.") ? message : "EVREN yanıt vermedi; bağlantıyı ve servis durumunu kontrol et.";
    }

    private static Request post(String str, String str2, JSONObject jSONObject) {
        return new Request.Builder().url(BASE + str).header("Authorization", "Bearer " + str2).post(RequestBody.create(JSON, jSONObject.toString())).build();
    }

    static String answer(String str, String str2) throws Exception {
        Response responseExecute = Cloud.HTTP.newCall(post("chat/completions", str2, Cloud.object("model", "deepseek-v4.1-flash", "max_tokens", 220, "messages", new JSONArray().put(Cloud.object("role", "system", "content", "Kısa ve doğal Türkçe konuş. Telefon işlemi yaptığını veya güncel kaynağa baktığını iddia etme; bu mod yalnızca sohbet.")).put(Cloud.object("role", "user", "content", str))))).execute();
        try {
            if (!responseExecute.isSuccessful() || responseExecute.body() == null) {
                throw new IOException("EVREN HTTP " + responseExecute.code());
            }
            String strTrim = new JSONObject(responseExecute.body().string()).getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content").trim();
            if (strTrim.isEmpty()) {
                throw new IOException("EVREN boş yanıt verdi.");
            }
            String strSubstring = strTrim.substring(0, Math.min(1200, strTrim.length()));
            if (responseExecute != null) {
                responseExecute.close();
            }
            return strSubstring;
        } catch (Throwable th) {
            if (responseExecute != null) {
                try {
                    responseExecute.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    static File speech(Context context, String text, String apiKey) throws Exception {
        File output = File.createTempFile("jarvis-tts-", ".wav", context.getCacheDir());
        Request request = post("audio/speech", apiKey, Cloud.object("model", "voxcpm2", "input", text.substring(0, Math.min(1200, text.length())), "voice", "default", "response_format", "wav"));
        try (Response response = Cloud.HTTP.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) throw new IOException("EVREN ses HTTP " + response.code());
            try (InputStream input = response.body().byteStream(); FileOutputStream file = new FileOutputStream(output)) {
                byte[] buffer = new byte[8192];
                int count;
                long total = 0;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > 8388608L) throw new IOException("Ses çok büyük");
                    file.write(buffer, 0, count);
                }
            }
            if (output.length() < 44) throw new IOException("Ses dosyası geçersiz.");
            return output;
        } catch (Exception e) {
            output.delete();
            throw e;
        }
    }

}
