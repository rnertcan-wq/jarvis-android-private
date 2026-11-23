from pathlib import Path
p=Path("build-source/RIO_0.3_Gemini_Live")
live=p/"app/src/main/java/com/rio/assistant/feature/gemini/GeminiLiveSession.kt"
s=live.read_text()
old='''        val setup = JSONObject().put("model", "models/gemini-3.8-live")
            .put("responseModalities", JSONArray().put("AUDIO"))'''
new='''        // Live API: responseModalities belongs inside generationConfig (fix WebSocket 1007).
        val setup = JSONObject().put("model", "models/gemini-3.8-live")
            .put("generationConfig", JSONObject().put("responseModalities", JSONArray().put("AUDIO")))'''
assert s.count(old)==1, "Gemini setup shape changed"
s=s.replace(old,new)
old='''            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (isCurrent(generation)) terminate("Live bağlantısı kapandı (kod $code).", error = true)
            }'''
new='''            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (isCurrent(generation)) {
                    val phase = if (mutable.value.stage == Stage.CONNECTING) "oturum kurulurken"
                        else "canlı ses/görüntü akışında"
                    val safeReason = reason.replace(key, "[gizli]")
                        .replace(Regex("AQ\\\\.[A-Za-z0-9_.-]{8,}"), "[gizli]")
                        .replace(Regex("AIza[A-Za-z0-9_-]{10,}"), "[gizli]")
                        .replace('\\n', ' ').replace('\\r', ' ').take(180)
                    val hint = if (code == 1007)
                        "Gemini 1007 ($phase): model veya istek parametresi reddedildi."
                        else "Live bağlantısı kapandı (kod $code, $phase)."
                    terminate(if (safeReason.isBlank()) hint else "$hint $safeReason", error = true)
                }
            }'''
assert s.count(old)==1, "OnClosed handler changed"
live.write_text(s.replace(old,new))
gradle=p/"app/build.gradle.kts"
s=gradle.read_text()
assert 'versionName = "0.3.2"' in s and "versionCode = 6" in s
s=s.replace('versionName = "0.3.2"','versionName = "0.3.3"').replace("versionCode = 6","versionCode = 7")
s=s.replace("testImplementation(libs.junit)",'testImplementation(libs.junit)\n    testImplementation("org.json:json:20240303")')
gradle.write_text(s)
test=p/"app/src/test/java/com/rio/assistant/GeminiLiveWireTest.kt"
test.write_text('''package com.rio.assistant

import com.rio.assistant.feature.gemini.GeminiLiveWire
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class GeminiLiveWireTest {
    @Test fun responseModalitiesMustBeNested() {
        val setup = JSONObject(GeminiLiveWire.setup()).getJSONObject("setup")
        assertEquals("models/gemini-3.8-live", setup.getString("model"))
        assertFalse(setup.has("responseModalities"))
        assertEquals("AUDIO", setup.getJSONObject("generationConfig")
            .getJSONArray("responseModalities").getString(0))
    }
    @Test fun toolsAndTranscriptionsAreDeclared() {
        val setup = JSONObject(GeminiLiveWire.setup()).getJSONObject("setup")
        assertEquals(2, setup.getJSONArray("tools").getJSONObject(0)
            .getJSONArray("functionDeclarations").length())
        assertTrue(setup.has("inputAudioTranscription"))
        assertTrue(setup.has("outputAudioTranscription"))
    }
}
''')
print("RIO 0.3.3: Live setup nesting + diagnostic reason + regression tests patched")
