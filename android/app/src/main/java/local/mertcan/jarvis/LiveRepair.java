package local.mertcan.jarvis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.speech.SpeechRecognizer;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
final class LiveRepair {
    private static volatile long geminiQuietUntil;
    private static VoiceDrive owner;
    private static AudioManager playbackAudio;
    private static AudioFocusRequest playbackFocus;
    private static Runnable playbackTimeout;
    private static MediaPlayer player;
    private static File playingFile;
    private static boolean requested;
    private static FutureTask<Void> task;
    private static final Locale TURKISH = Locale.forLanguageTag("tr-TR");
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Pattern GEMINI_WAKE = Pattern.compile("^(?:hey )?(?:jarvis|carvis)[ ,:]+(.+)$");
    private static final Pattern WIKI = Pattern.compile("^(?:vikipedi(?:'de| de)?|wikipedia(?:'da| da)?)[ ,:]+(?:ara(?:stir)? )?");
    private static final Pattern CALL = Pattern.compile("^(?:ara (.+)|(.+?) (?:telefonla )?ara)$");
    private static final List<ContactMatcher.Choice> pendingCallChoices = new ArrayList<>();
    private static int pendingCallMode;
    private static long pendingCallExpiry;

    LiveRepair() {
    }

    static void select(Main main) {
        new AlertDialog.Builder(main).setTitle("Canlı ses modu").setItems(new String[]{"EVREN Live", "Gemini Live", "EVREN · Android ses tanıma"}, new LiveRepair$$ExternalSyntheticLambda1(main)).setNegativeButton("Vazgeç", (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$select$0(Main main, DialogInterface dialogInterface, int i) {
        if (i == 0) {
            EvrenTalk.open(main);
        }
        if (i == 1) {
            GeminiAccess.open(main);
        }
        if (i == 2) {
            main.startEvrenLive();
        }
    }

    static boolean request(Activity activity) {
        if (Evren.key.isEmpty()) {
            Toast.makeText(activity, "EVREN anahtarı yüklenemedi.", 1).show();
            return false;
        }
        requested = true;
        return true;
    }

    static boolean requested() {
        return requested;
    }

    static boolean stop(Context context) {
        if (!requested && owner == null) {
            return false;
        }
        requested = false;
        context.stopService(new Intent(context, (Class<?>) VoiceDrive.class));
        return true;
    }

    static void attach(VoiceDrive voiceDrive) {
        if (requested) {
            owner = voiceDrive;
        }
    }

    static boolean available(Context context) {
        // Prefer on-device Turkish recognition, but do not disable hands-free mode
        // on devices whose OEM speech service only exposes the network recognizer.
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    static SpeechRecognizer recognizer(Context context) {
        if (requested) return SpeechRecognizer.createSpeechRecognizer(context);
        if (Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            return SpeechRecognizer.createOnDeviceSpeechRecognizer(context);
        }
        return SpeechRecognizer.createSpeechRecognizer(context);
    }

    static String local(Context context, String str) {
        String strNormalize = normalize(str);
        String pendingResponse = resolvePendingCall(context, strNormalize);
        if (pendingResponse != null) return pendingResponse;
        strNormalize.hashCode();
        int i = -1;
        switch (strNormalize) {
            case "kamerayi kapat":
            case "kamerayi kapat harita kalsin":
            case "sadece harita":
            case "google maps ac":
            case "navigasyonu ac":
            case "haritayi ac":
                return DriveDashboard.cameraOffAndOpenMaps(context);
            case "haritayi kucult":
            case "kucult":
            case "maps i kucult":
                return DriveDashboard.mapCommand(false);
            case "haritayi buyut":
            case "buyut":
            case "maps i buyut":
                return DriveDashboard.mapCommand(true);
            case "sesi yukselt":
            case "ses artir":
            case "muzigin sesini ac":
            case "ses yukselt":
            case "sesi arttir":
            case "ses ac":
            case "sesi artir":
            case "sesi biraz ac":
            case "ses arttir":
            case "sesi ac":
                i = 1;
                break;
            case "ses azalt":
            case "ses dusur":
            case "sesi azalt":
            case "sesi dusur":
            case "muzigin sesini kis":
            case "sesi biraz kis":
            case "sesi kis":
            case "ses kis":
                break;
            case "takip mesafem ne olmali":
            case "takip mesafesi":
                return DriveCamera.voiceSummary(true);
            case "hizim kac":
            case "gps hizim":
            case "ne kadar hizliyim":
                return DriveCamera.voiceSummary(false);
            case "trafik durumu":
            case "trafik":
            case "trafik nasil":
                return DriveCamera.trafficSummary();
            case "radar durumu":
            case "radar":
            case "sabit kamera durumu":
                return DriveCamera.radarSummary();
            default:
                return call(context, strNormalize);
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(AudioManager.class);
        if (audioManager == null || audioManager.isVolumeFixed()) {
            return "Bu cihaz medya sesini değiştirmeye izin vermiyor.";
        }
        try {
            int streamVolume = audioManager.getStreamVolume(3);
            audioManager.adjustStreamVolume(3, i, 0);
            if (streamVolume == audioManager.getStreamVolume(3)) {
                return "Medya sesi bu yönde daha fazla değişmedi.";
            }
            return i == 1 ? "Medya sesini açtım." : "Medya sesini kıstım.";
        } catch (SecurityException unused) {
            return "Android medya sesini değiştirmeye izin vermedi.";
        }
    }

    private static String call(Context context, String str) {
        if (str.startsWith("internette ") || str.startsWith("google'da ") || str.startsWith("google da ")
                || str.startsWith("youtube'da ") || str.startsWith("youtube da ")) return null;
        if (str.equals("ara") || str.equals("arama yap")) return "Kimi arayayım?";
        Matcher matcher = CALL.matcher(str);
        if (!matcher.matches()) return null;
        String target = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
        String directNumber = number(target);
        List<ContactMatcher.Choice> choices;
        if (!directNumber.isEmpty()) {
            choices = Collections.singletonList(new ContactMatcher.Choice(-1L, "Bu numara", directNumber, "", 100));
        } else {
            if (!allowed(context, "android.permission.READ_CONTACTS")) {
                return permission(context, "android.permission.READ_CONTACTS", "Kişiler izni gerekli. Jarvis ekranında izin verip tekrar söyle.");
            }
            choices = ContactMatcher.find(context, target);
            if (choices.isEmpty()) return "Rehberde “" + target + "” için eşleşen bir kişi ve telefon numarası bulamadım. Kişinin rehberde kayıtlı adını veya numarasını söyle.";
        }
        synchronized (pendingCallChoices) {
            pendingCallChoices.clear();
            pendingCallChoices.addAll(choices);
            pendingCallExpiry = SystemClock.elapsedRealtime() + 60000L;
            if (choices.size() == 1) {
                pendingCallMode = 2;
                return confirmationPrompt(choices.get(0));
            }
            pendingCallMode = 1;
            StringBuilder prompt = new StringBuilder("Birden fazla eşleşme buldum. ");
            int limit = Math.min(choices.size(), 3);
            for (int i = 0; i < limit; i++) {
                ContactMatcher.Choice choice = choices.get(i);
                prompt.append(ordinalName(i + 1)).append(": ").append(choice.spokenLabel())
                        .append(". Numara: ").append(ContactMatcher.spokenNumber(choice.number)).append(". ");
            }
            prompt.append("Aranacak kişiyi birinci, ikinci veya üçüncü diyerek seç.");
            return prompt.toString();
        }
    }

    private static String resolvePendingCall(Context context, String spoken) {
        synchronized (pendingCallChoices) {
            if (pendingCallMode == 0) return null;
            if (SystemClock.elapsedRealtime() > pendingCallExpiry) {
                clearPendingCallLocked();
                return "Arama isteğinin süresi doldu. Yeniden söyleyebilirsin.";
            }
            if (pendingCallMode == 1) {
                int selected = selectedChoice(spoken, pendingCallChoices);
                if (selected < 0 || selected >= pendingCallChoices.size() || selected >= 3) {
                    if (isNo(spoken)) {
                        clearPendingCallLocked();
                        return "Arama iptal edildi.";
                    }
                    return "Seçimi anlayamadım. Birinci, ikinci veya üçüncü de. İptal demen de yeterli.";
                }
                ContactMatcher.Choice choice = pendingCallChoices.get(selected);
                pendingCallChoices.clear();
                pendingCallChoices.add(choice);
                pendingCallMode = 2;
                pendingCallExpiry = SystemClock.elapsedRealtime() + 60000L;
                return confirmationPrompt(choice);
            }
            if (isNo(spoken)) {
                clearPendingCallLocked();
                return "Tamam, aramayı iptal ettim.";
            }
            if (!isYes(spoken)) return "Arama beklemede. Başlatmak için evet, vazgeçmek için hayır de.";
            if (pendingCallChoices.isEmpty()) {
                clearPendingCallLocked();
                return "Aranacak numara bulunamadı. Tekrar dene.";
            }
            ContactMatcher.Choice choice = pendingCallChoices.get(0);
            if (!allowed(context, "android.permission.CALL_PHONE")) {
                pendingCallExpiry = SystemClock.elapsedRealtime() + 60000L;
                return permission(context, "android.permission.CALL_PHONE", "Telefon arama izni gerekli. İzin verdikten sonra tekrar evet de.");
            }
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(TelecomManager.class);
            if (telecomManager == null) {
                clearPendingCallLocked();
                return "Bu cihazda telefon hizmeti bulunmuyor.";
            }
            try {
                telecomManager.placeCall(Uri.fromParts("tel", choice.number, null), new Bundle());
                clearPendingCallLocked();
                return choice.name + " aranıyor.";
            } catch (SecurityException e) {
                return "Android arama iznini reddetti. Telefon iznini kontrol et.";
            } catch (RuntimeException e) {
                clearPendingCallLocked();
                return "Telefon aramayı başlatamadı.";
            }
        }
    }

    private static String confirmationPrompt(ContactMatcher.Choice choice) {
        return choice.spokenLabel() + ", " + ContactMatcher.spokenNumber(choice.number)
                + " numarasını arayayım mı? Başlatmak için evet, iptal etmek için hayır de.";
    }

    private static int selectedChoice(String spoken, List<ContactMatcher.Choice> choices) {
        String value = normalize(spoken);
        if (value.matches(".*\\b(birinci|ilk|bir|1)\\b.*")) return 0;
        if (value.matches(".*\\b(ikinci|iki|2)\\b.*")) return 1;
        if (value.matches(".*\\b(ucuncu|üçüncü|uc|3)\\b.*")) return 2;
        for (int i = 0; i < Math.min(choices.size(), 3); i++) {
            String digits = choices.get(i).number.replaceAll("\\D", "");
            if (digits.length() >= 4 && value.contains(digits.substring(digits.length() - 4))) return i;
        }
        return -1;
    }

    private static boolean isYes(String value) {
        return value.equals("evet") || value.equals("evet ara") || value.equals("onayla")
                || value.equals("tamam") || value.equals("dogru") || value.equals("baslat")
                || value.equals("ara") || value.equals("olur");
    }

    private static boolean isNo(String value) {
        return value.equals("hayir") || value.equals("iptal") || value.equals("vazgec")
                || value.equals("dur") || value.equals("arama") || value.equals("hayir arama");
    }

    private static String ordinalName(int value) {
        if (value == 1) return "Birinci";
        if (value == 2) return "İkinci";
        return "Üçüncü";
    }

    private static void clearPendingCallLocked() {
        pendingCallChoices.clear();
        pendingCallMode = 0;
        pendingCallExpiry = 0L;
    }

    private static String number(String str) {
        if (str == null || !str.matches("[+0-9 ().-]+")) {
            return "";
        }
        String strStripSeparators = PhoneNumberUtils.stripSeparators(str);
        return strStripSeparators.matches("\\+?[0-9]{3,15}") ? strStripSeparators : "";
    }

    private static boolean allowed(Context context, String str) {
        return context.checkSelfPermission(str) == 0;
    }

    private static String permission(Context context, String str, String str2) {
        if (context instanceof Activity) {
            ((Activity) context).requestPermissions(new String[]{str}, 181);
        }
        return str2;
    }

    private static String normalize(String str) {
        if (str == null) {
            return "";
        }
        return Normalizer.normalize(str.trim().toLowerCase(TURKISH), Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replace((char) 305, 'i').replaceAll("[.!?]+$", "").replaceAll("\\s+", " ").trim();
    }

    static boolean busy(VoiceDrive voiceDrive) {
        return owner == voiceDrive && !(task == null && player == null);
    }

    static boolean wikiCommand(VoiceDrive voiceDrive, String str) {
        return owner == voiceDrive && WIKI.matcher(normalize(str)).find() && answer(voiceDrive, str);
    }

    private static String lookup(String str) throws Exception {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(Uri.parse("https://tr.wikipedia.org/w/api.php").buildUpon().appendQueryParameter("action", "query").appendQueryParameter("generator", "search").appendQueryParameter("gsrsearch", str).appendQueryParameter("gsrlimit", "1").appendQueryParameter("prop", "extracts|info").appendQueryParameter("inprop", "url").appendQueryParameter("exintro", "1").appendQueryParameter("explaintext", "1").appendQueryParameter("exchars", "750").appendQueryParameter("format", "json").appendQueryParameter("formatversion", "2").build().toString()).openConnection();
        httpsURLConnection.setInstanceFollowRedirects(false);
        httpsURLConnection.setConnectTimeout(6000);
        httpsURLConnection.setReadTimeout(8000);
        httpsURLConnection.setRequestProperty("User-Agent", "JarvisAndroid/0.9.1 (personal Android client)");
        try {
            InputStream inputStream = httpsURLConnection.getInputStream();
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[4096];
                while (true) {
                    int i = inputStream.read(bArr);
                    if (i != -1) {
                        if (byteArrayOutputStream.size() + i > 131072) {
                            throw new IOException("Large response");
                        }
                        byteArrayOutputStream.write(bArr, 0, i);
                    } else {
                        JSONObject jSONObjectOptJSONObject = new JSONObject(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8)).optJSONObject("query");
                        JSONObject jSONObjectOptJSONObject2 = null;
                        JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject == null ? null : jSONObjectOptJSONObject.optJSONArray("pages");
                        if (jSONArrayOptJSONArray != null) {
                            jSONObjectOptJSONObject2 = jSONArrayOptJSONArray.optJSONObject(0);
                        }
                        if (jSONObjectOptJSONObject2 != null && !jSONObjectOptJSONObject2.optString("extract").trim().isEmpty()) {
                            String str2 = "Türkçe Vikipedi, " + jSONObjectOptJSONObject2.optString("title") + ". " + jSONObjectOptJSONObject2.optString("extract").replaceAll("\\s+", " ").trim();
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            httpsURLConnection.disconnect();
                            return str2;
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        httpsURLConnection.disconnect();
                        return "Vikipedi'de bu konuda okunabilir bir sonuç bulamadım.";
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
            httpsURLConnection.disconnect();
            throw th3;
        }
    }

    static boolean answer(VoiceDrive voiceDrive, String str) {
        if (owner != voiceDrive) {
            return false;
        }
        String strNormalize = normalize(str);
        if (strNormalize.equals("live modu kapat") || strNormalize.equals("canli sohbeti kapat") || strNormalize.equals("sohbeti kapat") || strNormalize.equals("evren live kapat")) {
            stop(voiceDrive);
            return true;
        }
        if (task == null && player == null) {
            String str2 = Evren.key;
            Matcher matcher = WIKI.matcher(strNormalize);
            task = new FutureTask<>(new LiveRepair$$ExternalSyntheticLambda2(matcher.find() ? strNormalize.substring(matcher.end()).trim() : null, str, str2, voiceDrive));
            new Thread(task, "evren-live-turn").start();
        }
        return true;
    }

    static /* synthetic */ Void lambda$answer$2(String str, String str2, String str3, VoiceDrive voiceDrive) throws Exception {
        String strError;
        File fileSpeech = null;
        try {
            if (str == null) {
                strError = Evren.answer(str2.substring(0, Math.min(1500, str2.length())), str3);
            } else {
                strError = str.isEmpty() ? "Vikipedi'de hangi konuyu arayayım?" : lookup(str.substring(0, Math.min(150, str.length())));
            }
            if (Thread.currentThread().isInterrupted()) {
                fileSpeech = null;
            } else {
                try {
                    fileSpeech = Evren.speech(voiceDrive, strError, str3);
                } catch (Exception unused) {
                    fileSpeech = null;
                }
            }
        } catch (Exception e) {
            strError = str == null ? Evren.error(e) : "Vikipedi bağlantısı kurulamadı.";
        }
        MAIN.post(new LiveRepair$$ExternalSyntheticLambda3(voiceDrive, strError, fileSpeech));
        return null;
    }

    static /* synthetic */ void lambda$answer$1(VoiceDrive voiceDrive, String str, File file) {
        play(voiceDrive, str, file);
    }

    private static void play(VoiceDrive voiceDrive, String str, File file) {
        if (owner != voiceDrive) {
            if (file != null) {
                file.delete();
                return;
            }
            return;
        }
        task = null;
        History.add(voiceDrive, "Jarvis · EVREN Live", str);
        if (file == null) {
            voiceDrive.liveReply(str);
            return;
        }
        try {
            playingFile = file;
            AudioAttributes audioAttributesBuild = new AudioAttributes.Builder().setUsage(1).setContentType(1).build();
            playbackAudio = (AudioManager) voiceDrive.getSystemService(AudioManager.class);
            AudioFocusRequest audioFocusRequestBuild = new AudioFocusRequest.Builder(3).setAudioAttributes(audioAttributesBuild).build();
            playbackFocus = audioFocusRequestBuild;
            AudioManager audioManager = playbackAudio;
            if (audioManager != null) {
                audioManager.requestAudioFocus(audioFocusRequestBuild);
            }
            MediaPlayer mediaPlayer = new MediaPlayer();
            player = mediaPlayer;
            mediaPlayer.setAudioAttributes(audioAttributesBuild);
            player.setDataSource(file.getAbsolutePath());
            player.setOnPreparedListener(new LiveRepair$$ExternalSyntheticLambda4(voiceDrive, str));
            player.setOnCompletionListener(new LiveRepair$$ExternalSyntheticLambda5(voiceDrive));
            player.setOnErrorListener(new LiveRepair$$ExternalSyntheticLambda6(voiceDrive, str));
            LiveRepair$$ExternalSyntheticLambda7 liveRepair$$ExternalSyntheticLambda7 = new LiveRepair$$ExternalSyntheticLambda7(voiceDrive, str);
            playbackTimeout = liveRepair$$ExternalSyntheticLambda7;
            MAIN.postDelayed(liveRepair$$ExternalSyntheticLambda7, 60000L);
            player.prepareAsync();
        } catch (Exception unused) {
            clearPlayback();
            voiceDrive.liveReply(str);
        }
    }

    static /* synthetic */ void lambda$play$3(VoiceDrive voiceDrive, String str, MediaPlayer mediaPlayer) {
        if (player != mediaPlayer) {
            return;
        }
        try {
            mediaPlayer.start();
        } catch (IllegalStateException unused) {
            clearPlayback();
            voiceDrive.liveReply(str);
        }
    }

    static /* synthetic */ void lambda$play$4(VoiceDrive voiceDrive, MediaPlayer mediaPlayer) {
        if (player != mediaPlayer) {
            return;
        }
        clearPlayback();
        voiceDrive.liveResume();
    }

    static /* synthetic */ boolean lambda$play$5(VoiceDrive voiceDrive, String str, MediaPlayer mediaPlayer, int i, int i2) {
        if (player != mediaPlayer) {
            return true;
        }
        clearPlayback();
        voiceDrive.liveReply(str);
        return true;
    }

    static /* synthetic */ void lambda$play$6(VoiceDrive voiceDrive, String str) {
        clearPlayback();
        if (owner == voiceDrive) {
            voiceDrive.liveReply(str);
        }
    }

    private static void clearPlayback() {
        AudioFocusRequest audioFocusRequest;
        Runnable runnable = playbackTimeout;
        if (runnable != null) {
            MAIN.removeCallbacks(runnable);
        }
        playbackTimeout = null;
        MediaPlayer mediaPlayer = player;
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        player = null;
        File file = playingFile;
        if (file != null) {
            file.delete();
        }
        playingFile = null;
        AudioManager audioManager = playbackAudio;
        if (audioManager != null && (audioFocusRequest = playbackFocus) != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
        playbackAudio = null;
        playbackFocus = null;
    }

    static void detach(VoiceDrive voiceDrive) {
        if (owner != voiceDrive) {
            return;
        }
        owner = null;
        requested = false;
        FutureTask<Void> futureTask = task;
        if (futureTask != null) {
            futureTask.cancel(true);
        }
        task = null;
        clearPlayback();
    }

    static void geminiCommand(Context context, String str) {
        Matcher matcher = GEMINI_WAKE.matcher(normalize(str));
        if (matcher.matches()) {
            String strGroup = matcher.group(1);
            if (CALL.matcher(strGroup).matches()) {
                return;
            }
            MAIN.post(new LiveRepair$$ExternalSyntheticLambda0(context, strGroup));
        }
    }

    static /* synthetic */ void lambda$geminiCommand$7(Context context, String str) {
        String strLocal;
        if (GeminiLive.active && (strLocal = local(context, str)) != null) {
            Toast.makeText(context, strLocal, 0).show();
        }
    }

    static void markGeminiPlayback(int i) {
        geminiQuietUntil = SystemClock.elapsedRealtime() + 450 + (((long) i) / 48);
    }

    static void clearGeminiPlayback() {
        geminiQuietUntil = 0L;
    }

    static boolean geminiPlaying() {
        return SystemClock.elapsedRealtime() < geminiQuietUntil;
    }
}
