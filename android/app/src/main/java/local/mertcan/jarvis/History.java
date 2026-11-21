package local.mertcan.jarvis;

import android.content.Context;
import java.text.DateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
final class History {
    History() {
    }

    static synchronized void add(Context context, String str, String str2) {
        if (!Main.prefs(context).getBoolean("saveHistory", true) || str2.trim().isEmpty()) {
            return;
        }
        try {
            JSONArray jSONArrayLoad = load(context);
            JSONArray jSONArray = new JSONArray();
            for (int iMax = Math.max(0, jSONArrayLoad.length() - 199); iMax < jSONArrayLoad.length(); iMax++) {
                jSONArray.put(jSONArrayLoad.get(iMax));
            }
            jSONArray.put(new JSONObject().put("role", str).put("text", str2.substring(0, Math.min(str2.length(), 1500))).put("time", System.currentTimeMillis()));
            Main.prefs(context).edit().putString("history", jSONArray.toString()).apply();
        } catch (JSONException e) {
        }
    }

    private static JSONArray load(Context context) {
        try {
            return new JSONArray(Main.prefs(context).getString("history", "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    static synchronized String display(Context context) {
        StringBuilder sb;
        JSONArray jSONArrayLoad = load(context);
        sb = new StringBuilder();
        for (int length = jSONArrayLoad.length() - 1; length >= 0; length--) {
            JSONObject jSONObjectOptJSONObject = jSONArrayLoad.optJSONObject(length);
            if (jSONObjectOptJSONObject != null) {
                sb.append(DateFormat.getDateTimeInstance().format(new Date(jSONObjectOptJSONObject.optLong("time")))).append(" • ").append(jSONObjectOptJSONObject.optString("role")).append('\n').append(jSONObjectOptJSONObject.optString("text")).append("\n\n");
            }
        }
        return sb.length() == 0 ? "Henüz geçmiş yok." : sb.toString();
    }

    static synchronized void clear(Context context) {
        Main.prefs(context).edit().remove("history").remove("voiceNote").apply();
    }
}
