from pathlib import Path
root = Path("build-source/RIO_0.3_Gemini_Live")
runtime = root / "app/src/main/java/com/rio/assistant/core/session/RioRuntime.kt"
src = runtime.read_text()
src = src.replace("    val live = GeminiLiveSession(context,", "    val live: GeminiLiveSession = GeminiLiveSession(context,")
src = src.replace("    private fun liveCameraChange(enabled: Boolean) = live.setCameraEnabled(enabled)",
                  "    private fun liveCameraChange(enabled: Boolean) { live.setCameraEnabled(enabled) }")
runtime.write_text(src)
live = root / "app/src/main/java/com/rio/assistant/feature/gemini/GeminiLiveSession.kt"
src = live.read_text()
src = src.replace('"Bir görüntü gördüğünü söylemeden önce kamera durumunu doğrula.")))))',
                  '"Bir görüntü gördüğünü söylemeden önce kamera durumunu doğrula."))))')
live.write_text(src)
limitations = root / "LIMITATIONS.md"
txt = limitations.read_text()
txt = txt.replace("Gemini sohbeti REST ile metin alışverişidir; Gemini Live ses/görüntü akışı henüz entegre değildir. Gemini yalnız tanınmayan sorulara cevap verir, Android araçlarını doğrudan çağırmaz.",
"Gemini Live 3.8 ses/görüntü API akışı eklendi; gerçek cihaz ve kullanıcı API anahtarıyla uçtan uca testi gereklidir. Araç çağrıları RIO'nun yerel izin ve onay kapısından geçer.")
limitations.write_text(txt)
print("RIO Live Kotlin fixes applied")
