package local.mertcan.jarvis;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/* JADX INFO: loaded from: classes.dex */
final class Secrets {
    private static final String ALIAS = "jarvis-api-keys-v1";

    Secrets() {
    }

    private static synchronized SecretKey key(boolean z) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (keyStore.containsAlias(ALIAS)) {
            return (SecretKey) keyStore.getKey(ALIAS, null);
        }
        if (!z) {
            return null;
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
        return keyGenerator.generateKey();
    }

    static void save(Context context, String str, String str2) throws Exception {
        if (str2.isEmpty()) {
            Main.prefs(context).edit().remove("secret." + str).putBoolean("bootstrapUsed." + str, true).apply();
            return;
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, key(true));
        cipher.updateAAD(str.getBytes(StandardCharsets.UTF_8));
        Main.prefs(context).edit().putString("secret." + str, Base64.encodeToString(cipher.getIV(), 2) + ":" + Base64.encodeToString(cipher.doFinal(str2.getBytes(StandardCharsets.UTF_8)), 2)).putBoolean("bootstrapUsed." + str, true).apply();
    }

    /* JADX WARN: Code duplicated, block: B:45:0x009c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    static String loadPersonal(Context context, String str) {
        String existing = load(context, str);
        if (!existing.isEmpty() || Main.prefs(context).getBoolean("bootstrapUsed." + str, false)) return existing;
        try (InputStream input = context.getAssets().open(str + ".bootstrap");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[256];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (output.size() + count > 4096) return "";
                output.write(buffer, 0, count);
            }
            String value = new String(output.toByteArray(), StandardCharsets.UTF_8).trim();
            if (!value.isEmpty()) save(context, str, value);
            return value;
        } catch (Exception ignored) {
            return "";
        }
    }

    static String load(Context context, String str) {
        try {
            String string = Main.prefs(context).getString("secret." + str, "");
            if (string.isEmpty()) {
                return "";
            }
            String[] strArrSplit = string.split(":");
            SecretKey secretKeyKey = key(false);
            if (secretKeyKey != null && strArrSplit.length == 2) {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(2, secretKeyKey, new GCMParameterSpec(128, Base64.decode(strArrSplit[0], 0)));
                cipher.updateAAD(str.getBytes(StandardCharsets.UTF_8));
                return new String(cipher.doFinal(Base64.decode(strArrSplit[1], 0)), StandardCharsets.UTF_8);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
