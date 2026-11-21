package local.mertcan.jarvis;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.view.KeyEvent;

/* JADX INFO: loaded from: classes.dex */
final class Actions {
    private static final String YTM = "com.google.android.apps.youtube.music";

    Actions() {
    }

    static String normalize(String str) {
        return str.trim().toLowerCase(Main.TR).replaceFirst("[.!?]+$", "").trim();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:52:0x00b5  */
    static String intent(String str) {
        String strNormalize = normalize(str);
        if ((strNormalize.contains("youtube") || strNormalize.contains("yotube") || strNormalize.contains("you tube") || strNormalize.contains("yutub")) && (strNormalize.contains("aç") || strNormalize.contains("çal") || strNormalize.contains("devam") || strNormalize.contains("oynat"))) {
            return "youtube_music";
        }
        switch (strNormalize) {
            case "müziği aç":
            case "müziğe devam et":
            case "müziği devam ettir":
                return "play";
            case "müziği durdur":
                return "pause";
            case "sonraki şarkı":
                return "next";
            case "kurlar":
            case "kurları söyle":
            case "döviz kurlarını söyle":
                return "rates";
            case "haberler":
            case "haberleri oku":
                return "news";
            default:
                return "";
        }
    }

    static String run(Context context, String str) {
        return run(context, str, "");
    }

    static String songQuery(String str) {
        String strTrim = str.trim().replaceAll("(?iu)(?<!\\p{L})(?:you\\s*tube|yotube|yutub)(?:['’]?\\s*(?:music|müzik)(?:['’]?(?:ten|tan|te|ta))?)?(?:['’]?(?:den|dan|ten|tan|de|da))?(?!\\p{L})", " ").replaceAll("(?iu)(?<!\\p{L})(?:müzik|müziği|şarkıyı|şarkısını|çal|oynat|aç|başlat|devam et)(?!\\p{L})", " ").replaceAll("\\s+", " ").replaceAll("^[\\s,:“”\"]+|[\\s.!?,:“”\"]+$", "").trim();
        return strTrim.length() > 150 ? strTrim.substring(0, 150) : strTrim;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:24:0x0046  */
    static String run(Context context, String str, String str2) {
        int i;
        try {
            switch (str) {
                case "youtube_music":
                    return youtube(context, str2.trim());
                case "rates":
                    return Brief.rates();
                case "news":
                    return Brief.news();
                case "play":
                case "pause":
                case "next":
                    AudioManager audioManager = (AudioManager) context.getSystemService(AudioManager.class);
                    if (str.equals("play")) {
                        i = 126;
                    } else {
                        i = str.equals("pause") ? 127 : 87;
                    }
                    audioManager.dispatchMediaKeyEvent(new KeyEvent(0, i));
                    audioManager.dispatchMediaKeyEvent(new KeyEvent(1, i));
                    return "Medya komutu gönderildi.";
                default:
                    return "İşlem desteklenmiyor.";
            }
        } catch (Exception e) {
            return "İşlem tamamlanamadı.";
        }
    }

    private static String youtube(Context context, String str) {
        try {
            for (MediaController mediaController : ((MediaSessionManager) context.getSystemService(MediaSessionManager.class)).getActiveSessions(new ComponentName(context, (Class<?>) Messages.class))) {
                if (YTM.equals(mediaController.getPackageName())) {
                    if (!str.isEmpty()) {
                        PlaybackState playbackState = mediaController.getPlaybackState();
                        if (playbackState == null || (playbackState.getActions() & 2048) == 0) {
                            break;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("android.intent.extra.focus", "vnd.android.cursor.item/*");
                        mediaController.getTransportControls().playFromSearch(str, bundle);
                        return "YouTube Music'e oynatma komutu gönderildi.";
                    }
                    mediaController.getTransportControls().play();
                    return "YouTube Music'e oynatma komutu gönderildi.";
                }
            }
        } catch (SecurityException e) {
        }
        return Main.launchYouTubeMusic(str);
    }

    static Intent youtubePlayIntent(String str) {
        return new Intent("android.media.action.MEDIA_PLAY_FROM_SEARCH").setPackage(YTM).putExtra("android.intent.extra.focus", "vnd.android.cursor.item/*").putExtra("query", str);
    }

    static Intent youtubeLaunchIntent(Context context) {
        return context.getPackageManager().getLaunchIntentForPackage(YTM);
    }
}
