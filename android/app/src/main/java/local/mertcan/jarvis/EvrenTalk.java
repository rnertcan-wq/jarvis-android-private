package local.mertcan.jarvis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes2.dex */
final class EvrenTalk {
    private static final int RATE = 16000;
    private static EvrenTalk active;
    private final Main activity;
    private volatile boolean closed;
    private AlertDialog dialog;
    private volatile Thread worker;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /* JADX INFO: renamed from: $r8$lambda$0MlypkHKeKsk0KeCBG6-CD5ymng, reason: not valid java name */
    public static /* synthetic */ void m22$r8$lambda$0MlypkHKeKsk0KeCBG6CD5ymng(EvrenTalk evrenTalk) {
        evrenTalk.start();
    }

    private EvrenTalk(Main main) {
        this.activity = main;
    }

    static void open(Main main) {
        if (active != null) {
            return;
        }
        if (Evren.key.isEmpty()) {
            Toast.makeText(main, "EVREN anahtarı yüklenemedi.", 1).show();
            return;
        }
        if (main.checkSelfPermission("android.permission.RECORD_AUDIO") != 0) {
            main.requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 195);
            return;
        }
        main.stopService(new Intent(main, (Class<?>) VoiceDrive.class));
        main.stopService(new Intent(main, (Class<?>) GeminiLive.class));
        DriveCamera.yieldMicrophone(main);
        main.releaseMicForEvrenTalk();
        EvrenTalk evrenTalk = new EvrenTalk(main);
        active = evrenTalk;
        AlertDialog alertDialogCreate = new AlertDialog.Builder(main).setTitle("EVREN Live")
                .setMessage("Eller serbest dinleme açık. Konuşman bitince yanıt vereceğim. Kapatmak için “sessiz ol” diyebilirsin.")
                .setNegativeButton("Kapat", new EvrenTalk$ExternalSyntheticLambda1(evrenTalk)).create();
        evrenTalk.dialog = alertDialogCreate;
        alertDialogCreate.setOnDismissListener(new EvrenTalk$ExternalSyntheticLambda2(evrenTalk));
        evrenTalk.dialog.show();
        evrenTalk.message("Mikrofon hazırlanıyor…");
        evrenTalk.start();
    }

    static /* synthetic */ void lambda$open$0(EvrenTalk evrenTalk, DialogInterface dialogInterface, int i) {
        evrenTalk.stopAndResumeWake();
    }

    static /* synthetic */ void lambda$open$1(EvrenTalk evrenTalk, DialogInterface dialogInterface) {
        evrenTalk.stop();
    }

    static /* synthetic */ void lambda$open$2(EvrenTalk evrenTalk, View view) {
        view.setEnabled(false);
        evrenTalk.message("Mikrofon hazırlanıyor…");
        Handler handler = MAIN;
        Objects.requireNonNull(evrenTalk);
        handler.postDelayed(new EvrenTalk$$ExternalSyntheticLambda8(evrenTalk), 500L);
    }

    static void permission(Main main, int i) {
        if (i != 195) {
            return;
        }
        if (main.checkSelfPermission("android.permission.RECORD_AUDIO") == 0) {
            open(main);
        } else {
            Toast.makeText(main, "EVREN Live için mikrofon izni gerekli.", 1).show();
        }
    }

    static void suspend(Main main) {
        EvrenTalk evrenTalk = active;
        if (evrenTalk == null || evrenTalk.activity != main) {
            return;
        }
        evrenTalk.dialog.dismiss();
    }

    private void start() {
        if (this.closed) {
            return;
        }
        this.worker = new Thread(new EvrenTalk$$ExternalSyntheticLambda7(this, Evren.key), "EvrenTalk");
        this.worker.start();
    }

    /* JADX INFO: renamed from: lambda$start$3$local-mertcan-jarvis-EvrenTalk, reason: not valid java name */
    /* synthetic */ void m26lambda$start$3$localmertcanjarvisEvrenTalk(String str) {
        listen(str);
    }

    private void stop() {
        this.closed = true;
        if (active == this) {
            active = null;
        }
        Thread thread = this.worker;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void stopAndResumeWake() {
        stop();
        MAIN.postDelayed(() -> {
            if (this.dialog != null && this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (Main.isVisible(this.activity) && !VoiceDrive.active) {
                try {
                    Intent intent = new Intent(this.activity, VoiceDrive.class);
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        this.activity.startForegroundService(intent);
                    } else {
                        this.activity.startService(intent);
                    }
                } catch (RuntimeException e) {
                    Toast.makeText(this.activity, "Uyandırma dinleyicisi yeniden başlatılamadı.", Toast.LENGTH_SHORT).show();
                }
            }
        }, 700L);
    }

    private void listen(String str) {
        while (!this.closed) {
            try {
                message("Dinliyorum. Konuşabilirsin.");
                byte[] bArrRecord = record();
                if (this.closed) {
                    return;
                }
                if (bArrRecord != null) {
                    message("EVREN sesi yazıya çeviriyor…");
                    String strTranscribe = transcribe(bArrRecord, str);
                    if (!strTranscribe.isEmpty() && !this.closed) {
                        boolean stopAfterReply = GeminiLive.sleepPhrase(strTranscribe);
                        message("Sen: " + strTranscribe + "\n\nEVREN yanıtlıyor…");
                        String strLocalOnMain = stopAfterReply
                                ? "Tamam. Hey Jarvis dediğinde yeniden dinleyeceğim."
                                : localOnMain(strTranscribe);
                        if (this.closed) {
                            return;
                        }
                        if (strLocalOnMain == null) {
                            strLocalOnMain = Evren.answer(strTranscribe.substring(0, Math.min(strTranscribe.length(), 1800)), str);
                        }
                        message("Sen: " + strTranscribe + "\n\nEVREN: " + strLocalOnMain);
                        if (this.closed) {
                            return;
                        }
                        try {
                            File fileSpeech = Evren.speech(this.activity, strLocalOnMain, str);
                            try {
                                if (!this.closed) {
                                    play(fileSpeech);
                                }
                                fileSpeech.delete();
                            } catch (Throwable th) {
                                fileSpeech.delete();
                                throw th;
                            }
                        } catch (InterruptedException unused) {
                            return;
                        } catch (Exception unused2) {
                            message("EVREN: " + strLocalOnMain + "\n\nSeslendirme alınamadı.");
                            Thread.sleep(2000L);
                        }
                        if (stopAfterReply) {
                            stopAndResumeWake();
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                if (this.closed) {
                    return;
                }
                String message = e.getMessage();
                if (message == null || (!message.startsWith("EVREN HTTP ") && !message.startsWith("Mikrofon"))) {
                    message = "Canlı oturum kesildi. İnternet ve mikrofon erişimini kontrol edip yeniden başlat.";
                }
                message(message);
                MAIN.post(new EvrenTalk$$ExternalSyntheticLambda6(this));
                return;
            }
        }
    }

    /* JADX INFO: renamed from: lambda$listen$4$local-mertcan-jarvis-EvrenTalk, reason: not valid java name */
    /* synthetic */ void m23lambda$listen$4$localmertcanjarvisEvrenTalk() {
        if (this.closed || this.dialog == null || !this.dialog.isShowing()) {
            return;
        }
        this.dialog.setMessage("Canlı oturum durdu. Kapatıp yeniden açabilirsin.");
    }

    private String localOnMain(String str) throws Exception {
        FutureTask futureTask = new FutureTask(new EvrenTalk$$ExternalSyntheticLambda9(this, str));
        MAIN.post(futureTask);
        return (String) futureTask.get(3L, TimeUnit.SECONDS);
    }

    /* JADX INFO: renamed from: lambda$localOnMain$5$local-mertcan-jarvis-EvrenTalk, reason: not valid java name */
    /* synthetic */ String m24lambda$localOnMain$5$localmertcanjarvisEvrenTalk(String str) throws Exception {
        if (this.closed) {
            return null;
        }
        return LiveRepair.local(this.activity, str);
    }

    private byte[] record() throws IOException {
        int minBufferSize = AudioRecord.getMinBufferSize(RATE, 16, 2);
        if (minBufferSize <= 0) throw new IOException("Mikrofon biçimi desteklenmiyor.");
        AudioRecord recorder = new AudioRecord(6, RATE, 16, 2, Math.max(minBufferSize * 2, 6400));
        ByteArrayOutputStream spoken = new ByteArrayOutputStream();
        byte[][] preroll = new byte[10][640];
        byte[] chunk = new byte[640];
        int ringIndex = 0, ringCount = 0, loudRun = 0, quietRun = 0, loudCount = 0;
        boolean speaking = false;
        try {
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) throw new IOException("Mikrofon başlatılamadı.");
            recorder.startRecording();
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) throw new IOException("Mikrofon meşgul.");
            for (int frame = 0; !this.closed && frame < 1500; frame++) {
                int read = recorder.read(chunk, 0, chunk.length);
                if (read < 0) throw new IOException("Mikrofon sesi okunamadı.");
                if (read != chunk.length) continue;
                double energy = 0.0d;
                for (int i = 0; i < read; i += 2) {
                    short sample = (short) ((chunk[i] & 255) | (chunk[i + 1] << 8));
                    energy += (double) sample * (double) sample;
                }
                boolean loud = Math.sqrt(energy / (read / 2.0d)) > 350.0d;
                if (!speaking) {
                    System.arraycopy(chunk, 0, preroll[ringIndex], 0, chunk.length);
                    ringIndex = (ringIndex + 1) % preroll.length;
                    ringCount = Math.min(ringCount + 1, preroll.length);
                    loudRun = loud ? loudRun + 1 : 0;
                    if (loudRun >= 4) {
                        int start = (ringIndex - ringCount + preroll.length) % preroll.length;
                        for (int i = 0; i < ringCount; i++) spoken.write(preroll[(start + i) % preroll.length], 0, 640);
                        speaking = true;
                        loudCount = loudRun;
                    }
                } else {
                    spoken.write(chunk, 0, read);
                    if (loud) { loudCount++; quietRun = 0; } else { quietRun++; }
                    if (quietRun >= 38 || spoken.size() >= 448000) break;
                }
            }
            return loudCount >= 10 ? wav(spoken.toByteArray()) : null;
        } finally {
            try { if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) recorder.stop(); } catch (RuntimeException ignored) { }
            recorder.release();
        }
    }

    private static byte[] wav(byte[] bArr) {
        ByteBuffer byteBufferOrder = ByteBuffer.allocate(bArr.length + 44).order(ByteOrder.LITTLE_ENDIAN);
        byteBufferOrder.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(bArr.length + 36);
        byteBufferOrder.put("WAVEfmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16).putShort((short) 1).putShort((short) 1);
        byteBufferOrder.putInt(RATE).putInt(32000).putShort((short) 2).putShort((short) 16);
        byteBufferOrder.put("data".getBytes(StandardCharsets.US_ASCII)).putInt(bArr.length).put(bArr);
        return byteBufferOrder.array();
    }

    private static String transcribe(byte[] bArr, String str) throws Exception {
        String str2 = "Jarvis" + UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(("--" + str2 + "\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\nqwen3-asr-1.7b\r\n--" + str2 + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"turn.wav\"\r\nContent-Type: audio/wav\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrayOutputStream.write(bArr);
        StringBuilder sb = new StringBuilder("\r\n--");
        sb.append(str2);
        sb.append("--\r\n");
        byteArrayOutputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://evren-llmapi.ssyz.org.tr/v1/audio/transcriptions").openConnection();
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setInstanceFollowRedirects(false);
        httpsURLConnection.setConnectTimeout(8000);
        httpsURLConnection.setReadTimeout(35000);
        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + str);
        httpsURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + str2);
        httpsURLConnection.setFixedLengthStreamingMode(byteArrayOutputStream.size());
        try {
            OutputStream outputStream = httpsURLConnection.getOutputStream();
            try {
                byteArrayOutputStream.writeTo(outputStream);
                if (outputStream != null) {
                    outputStream.close();
                }
                int responseCode = httpsURLConnection.getResponseCode();
                if (responseCode != 200) {
                    throw new IOException("EVREN HTTP " + responseCode + " · Ses tanıma isteği başarısız.");
                }
                InputStream inputStream = httpsURLConnection.getInputStream();
                try {
                    ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                    byte[] bArr2 = new byte[2048];
                    while (true) {
                        int i = inputStream.read(bArr2);
                        if (i != -1) {
                            if (byteArrayOutputStream2.size() + i > 131072) {
                                throw new IOException();
                            }
                            byteArrayOutputStream2.write(bArr2, 0, i);
                        } else {
                            String strTrim = new JSONObject(new String(byteArrayOutputStream2.toByteArray(), StandardCharsets.UTF_8)).optString("text").trim();
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            httpsURLConnection.disconnect();
                            return strTrim;
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
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        } catch (Throwable th5) {
            httpsURLConnection.disconnect();
            throw th5;
        }
    }

    private void play(File file) throws Exception {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setOnCompletionListener(new EvrenTalk$$ExternalSyntheticLambda4(countDownLatch));
            mediaPlayer.setOnErrorListener(new EvrenTalk$$ExternalSyntheticLambda5(countDownLatch));
            mediaPlayer.prepare();
            mediaPlayer.start();
            countDownLatch.await(40L, TimeUnit.SECONDS);
        } finally {
            mediaPlayer.release();
        }
    }

    static /* synthetic */ void lambda$play$6(CountDownLatch countDownLatch, MediaPlayer mediaPlayer) {
        countDownLatch.countDown();
    }

    static /* synthetic */ boolean lambda$play$7(CountDownLatch countDownLatch, MediaPlayer mediaPlayer, int i, int i2) {
        countDownLatch.countDown();
        return true;
    }

    /* JADX INFO: renamed from: lambda$message$8$local-mertcan-jarvis-EvrenTalk, reason: not valid java name */
    /* synthetic */ void m25lambda$message$8$localmertcanjarvisEvrenTalk(String str) {
        if (this.closed || !this.dialog.isShowing()) {
            return;
        }
        this.dialog.setMessage(str);
    }

    private void message(String str) {
        MAIN.post(new EvrenTalk$$ExternalSyntheticLambda0(this, str));
    }
}
