from pathlib import Path
p = Path("build-source/RIO_0.3_Gemini_Live")
base = p / "app/src/main/java/com/rio/assistant"
ui = base / "feature/ui/SetupSettings.kt"
gate = base / "feature/gemini/GeminiGateway.kt"
rules = base / "feature/gemini/GeminiKeyRules.kt"
rules.write_text("""package com.rio.assistant.feature.gemini

/** Local format check only; API access is not verified until the first Live connection. */
object GeminiKeyRules {
    fun normalize(raw: String): String = raw.trim()
    fun isValid(raw: String): Boolean {
        val key = normalize(raw)
        return key.length in 18..256 && key.all {
            it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == '_' || it == '-' || it == '.'
        }
    }
}
""")
s = gate.read_text()
old = '''        val key = raw.trim()
        if (key.length !in 18..256 || !key.all { it.isLetterOrDigit() || it in "_-" }) return false
        return secrets.save(key)'''
new = '''        val key = GeminiKeyRules.normalize(raw)
        if (!GeminiKeyRules.isValid(key)) return false
        return secrets.save(key)'''
assert old in s, "old gateway validation not found"
s = s.replace(old, new)
old = '''        prefs.edit().putString("v", Base64.encodeToString(cipher.iv + cipher.doFinal(raw.toByteArray()),
            Base64.NO_WRAP)).commit()'''
new = '''        prefs.edit().putString("v", Base64.encodeToString(cipher.iv + cipher.doFinal(raw.toByteArray()),
            Base64.NO_WRAP)).commit() && load() == raw'''
assert old in s, "secure save section not found"
s = s.replace(old, new).replace('fun clear() { prefs.edit().remove("v").apply() }',
                         'fun clear() { prefs.edit().remove("v").commit() }')
gate.write_text(s)
s = ui.read_text()
s = s.replace('import androidx.compose.material3.OutlinedTextField', '''import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.rio.assistant.feature.gemini.GeminiKeyRules''')
s = s.replace('    var geminiReady by remember { mutableStateOf(runtime.geminiReady) }',
'''    var geminiReady by remember { mutableStateOf(runtime.geminiReady) }
    var showGeminiKey by remember { mutableStateOf(false) }
    val geminiKeyValid = GeminiKeyRules.isValid(geminiKey)''')
old = '''            OutlinedTextField(geminiKey, { geminiKey = it.take(256) }, Modifier.fillMaxWidth(),
                label = { Text("Gemini API anahtarı") }, visualTransformation = PasswordVisualTransformation(),
                singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Secondary("Anahtarı kaydet", enabled = !driving && geminiKey.isNotBlank()) {
                    geminiReady = runtime.saveGeminiKey(geminiKey)
                    geminiInfo = if (geminiReady) "Anahtar Android Keystore ile korundu." else "Anahtar geçersiz veya kaydedilemedi."
                    geminiKey = ""
                }
                Secondary("Gemini'yi kapat", enabled = !driving && geminiReady) {
                    runtime.removeGeminiKey()
                    geminiReady = false
                    geminiInfo = "Gemini anahtarı cihazdan kaldırıldı."
                }
            }'''
new = '''            if (driving) Text("Sürüş modu açık. Anahtar ayarını yalnız araç güvenli biçimde park halindeyken yap.", color = Mist)
            OutlinedTextField(geminiKey, { geminiKey = it.take(256); geminiInfo = "" }, Modifier.fillMaxWidth(),
                label = { Text("Gemini API anahtarı") },
                visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                singleLine = true,
                isError = geminiKey.isNotBlank() && !geminiKeyValid,
                supportingText = { Text(when {
                    geminiKey.isBlank() -> "Anahtarını buraya yapıştır. Kopyalama sırasında boşluk eklenirse temizlenir."
                    !geminiKeyValid -> "Anahtar biçimi geçersiz. Google AI Studio API anahtarı gir."
                    else -> "Kaydetmeye hazır. Anahtar cihazında şifreli saklanacak."
                }) },
                trailingIcon = { TextButton(onClick = { showGeminiKey = !showGeminiKey }) {
                    Text(if (showGeminiKey) "Gizle" else "Göster")
                } })
            Primary("Anahtarı kaydet", Modifier.fillMaxWidth(), enabled = geminiKeyValid) {
                val saved = runtime.saveGeminiKey(geminiKey)
                geminiReady = saved || runtime.geminiReady
                geminiInfo = if (saved) "Anahtar kaydedildi ve cihazdan geri okunarak doğrulandı. API erişimi Live başlatılınca sınanır."
                    else "Anahtar kaydedilemedi. Girdiğini kontrol edip yeniden dene."
                if (saved) { geminiKey = ""; showGeminiKey = false }
            }
            if (geminiReady) Secondary("Kayıtlı Gemini anahtarını sil", Modifier.fillMaxWidth()) {
                runtime.removeGeminiKey()
                geminiReady = runtime.geminiReady
                geminiInfo = if (geminiReady) "Anahtar silinemedi." else "Gemini anahtarı cihazdan kaldırıldı."
            }'''
assert old in s, "settings segment not found"
s = s.replace(old,new)
ui.write_text(s)
tests = p / "app/src/test/java/com/rio/assistant/GeminiKeyRulesTest.kt"
tests.write_text("""package com.rio.assistant

import com.rio.assistant.feature.gemini.GeminiKeyRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiKeyRulesTest {
    @Test fun typicalKeyIsAccepted() {
        assertTrue(GeminiKeyRules.isValid("AIzaSyExampleDemoKeyWithEnoughChars123456"))
    }
    @Test fun pastedWhitespaceIsTrimmed() {
        assertTrue(GeminiKeyRules.isValid("  AIzaSyExampleDemoKeyWithEnoughChars123456\\n"))
        assertEquals("ABC", GeminiKeyRules.normalize(" \\tABC \\n"))
    }
    @Test fun newGoogleAuthorizationKeyIsAccepted() {
        assertTrue(GeminiKeyRules.isValid("AQ.abcdefghijklmnopqrstuvwxyz0123456789_-ABCDEFGHI"))
        assertTrue(GeminiKeyRules.isValid("  AQ.abcdefghijklmnopqrstuvwxyz0123456789_-ABCDEFGHI  "))
    }
    @Test fun emptyAndShortKeysAreRejected() {
        assertFalse(GeminiKeyRules.isValid(""))
        assertFalse(GeminiKeyRules.isValid("short"))
    }
    @Test fun spacesAndPunctuationAreRejected() {
        assertFalse(GeminiKeyRules.isValid("AIza SyExampleDemoKeyWithEnoughChars123456"))
        assertFalse(GeminiKeyRules.isValid("AIzaSyExampleDemoKeyWithEnoughChars12345!"))
    }
}
""")
build=p/"app/build.gradle.kts"
s=build.read_text()
s=s.replace('versionName = "0.3.0"','versionName = "0.3.2"')
s=s.replace('versionCode = 4','versionCode = 6')
build.write_text(s)
print("RIO 0.3.1 key-fix applied")
