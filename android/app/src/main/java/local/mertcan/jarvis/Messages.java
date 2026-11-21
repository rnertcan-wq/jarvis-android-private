package local.mertcan.jarvis;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/* JADX INFO: loaded from: classes.dex */
public final class Messages extends NotificationListenerService {
    private static volatile StatusBarNotification latest;
    private static Draft pending;

    private static final class Draft {
        final StatusBarNotification notification;
        final String text;
        final long expires = SystemClock.elapsedRealtime() + 60000;

        Draft(StatusBarNotification statusBarNotification, String str) {
            this.notification = statusBarNotification;
            this.text = str;
        }
    }

    private static boolean allowed(String str) {
        return str.equals("com.whatsapp") || str.equals("com.whatsapp.w4b") || str.equals("com.google.android.apps.messaging") || str.equals("com.samsung.android.messaging") || str.equals("org.telegram.messenger") || str.equals("org.telegram.messenger.web");
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if (!Main.prefs(this).getBoolean("driving", false) || !allowed(statusBarNotification.getPackageName())) {
            return;
        }
        Notification notification = statusBarNotification.getNotification();
        if ((notification.flags & 512) != 0 || notification.extras.getCharSequence("android.text") == null) {
            return;
        }
        latest = statusBarNotification;
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        StatusBarNotification statusBarNotification2 = latest;
        if (statusBarNotification2 == null || !statusBarNotification2.getKey().equals(statusBarNotification.getKey())) {
            return;
        }
        latest = null;
    }

    static synchronized void clear() {
        latest = null;
        pending = null;
    }

    static String text() {
        StatusBarNotification statusBarNotification = latest;
        if (statusBarNotification == null) {
            return null;
        }
        Bundle bundle = statusBarNotification.getNotification().extras;
        return String.valueOf(bundle.getCharSequence("android.title", "Mesaj")) + ": " + ((Object) bundle.getCharSequence("android.text", ""));
    }

    static synchronized String draft(Context context, String str) {
        pending = null;
        if (!Main.prefs(context).getBoolean("driving", false)) {
            return "Sürüş modu kapalı.";
        }
        String strTrim = str.trim();
        if (!strTrim.isEmpty() && strTrim.length() <= 300) {
            StatusBarNotification statusBarNotification = latest;
            if (replyAction(statusBarNotification) == null) {
                return "Yanıtlanabilir bir bildirim yok.";
            }
            pending = new Draft(statusBarNotification, strTrim);
            return String.valueOf(statusBarNotification.getNotification().extras.getCharSequence("android.title", "Mesaj")) + " için taslak: " + strTrim + ". Göndermek için Gönder, vazgeçmek için İptal de.";
        }
        return "Bir ile 300 karakter arasında bir yanıt söyle.";
    }

    static synchronized String cancel() {
        pending = null;
        return "Taslak iptal edildi.";
    }

    static synchronized String confirm(Context context) {
        Draft draft = pending;
        pending = null;
        if (draft != null && SystemClock.elapsedRealtime() <= draft.expires) {
            StatusBarNotification statusBarNotification = latest;
            if (Main.prefs(context).getBoolean("driving", false) && statusBarNotification != null && draft.notification.getKey().equals(statusBarNotification.getKey()) && draft.notification.getPostTime() == statusBarNotification.getPostTime()) {
                Notification.Action actionReplyAction = replyAction(draft.notification);
                if (actionReplyAction == null) {
                    return "Bildirim artık yanıtlanamıyor.";
                }
                for (RemoteInput remoteInput : actionReplyAction.getRemoteInputs()) {
                    if (remoteInput.getAllowFreeFormInput()) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putCharSequence(remoteInput.getResultKey(), draft.text);
                        RemoteInput.addResultsToIntent(new RemoteInput[]{remoteInput}, intent, bundle);
                        try {
                            actionReplyAction.actionIntent.send(context, 0, intent);
                            return "Yanıt uygulamaya iletildi.";
                        } catch (PendingIntent.CanceledException | SecurityException e) {
                            return "Yanıt iletilemedi.";
                        }
                    }
                }
                return "Yanıt iletilemedi.";
            }
            return "Bildirim değişti; taslak gönderilmedi.";
        }
        return "Onay bekleyen taslak yok.";
    }

    private static Notification.Action replyAction(StatusBarNotification statusBarNotification) {
        if (Build.VERSION.SDK_INT < 28 || statusBarNotification == null || statusBarNotification.getNotification().actions == null) {
            return null;
        }
        for (Notification.Action action : statusBarNotification.getNotification().actions) {
            if (action.getSemanticAction() == 1 && action.getRemoteInputs() != null && action.actionIntent != null) {
                for (RemoteInput remoteInput : action.getRemoteInputs()) {
                    if (remoteInput.getAllowFreeFormInput()) {
                        return action;
                    }
                }
            }
        }
        return null;
    }
}
