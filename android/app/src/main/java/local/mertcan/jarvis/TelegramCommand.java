package local.mertcan.jarvis;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
final class TelegramCommand {
    final String body;
    final String recipient;
    static final String[] PACKAGES = {"org.telegram.messenger", "org.telegram.messenger.web"};
    private static final Pattern MESSAGE = Pattern.compile("(?iu)^(?:telegram(?:['’]?[ıi])?\\s+aç[\\s,.:]+|telegram(?:['’]?dan|['’]?da)?\\s+)?(.+?)\\s+mesaj\\s+(?:at|gönder)(?:(?:\\s*[:,-]\\s*|\\s+)(.+))?[.!?]*$");

    TelegramCommand(String str, String str2) {
        this.recipient = str;
        this.body = str2;
    }

    static TelegramCommand parse(String str, boolean z) {
        if (!z && !str.toLowerCase(Main.TR).startsWith("telegram")) {
            return null;
        }
        Matcher matcher = MESSAGE.matcher(str.trim());
        if (!matcher.matches()) {
            return null;
        }
        return new TelegramCommand(matcher.group(1).trim().replaceFirst("(?iu)(?:\\s+|['’])(?:ya|ye|a|e)$", ""), matcher.group(2) != null ? matcher.group(2).trim() : "");
    }

    static String key(String str) {
        return "tg.alias." + Normalizer.normalize(str.trim().toLowerCase(Main.TR), Normalizer.Form.NFC);
    }

    static boolean valid(String str) {
        return str.matches("@?[A-Za-z][A-Za-z0-9_]{3,31}") || str.matches("\\+[1-9][0-9]{6,14}");
    }

    static boolean isOpen(String str) {
        return str.matches("(?iu)^telegram(?:['’]?[ıi])?\\s+aç[.!?]*$");
    }

    static Intent app(Context context) {
        for (String str : PACKAGES) {
            Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(str);
            if (launchIntentForPackage != null) {
                return launchIntentForPackage;
            }
        }
        return null;
    }

    static Intent draft(String str, String str2) {
        Uri.Builder builderAppendQueryParameter = new Uri.Builder().scheme("tg").authority("resolve").appendQueryParameter(str.startsWith("+") ? "phone" : "domain", str.replaceFirst("^[@+]", ""));
        if (!str2.isEmpty()) {
            builderAppendQueryParameter.appendQueryParameter("text", str2);
        }
        return new Intent("android.intent.action.VIEW", builderAppendQueryParameter.build());
    }
}
