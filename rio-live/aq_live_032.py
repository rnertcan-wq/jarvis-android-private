from pathlib import Path
p=Path("build-source/RIO_0.3_Gemini_Live/app/src/main/java/com/rio/assistant/feature/gemini/GeminiLiveSession.kt")
s=p.read_text()
old='''        // Official WebSocket spec authenticates with a query parameter. No request URLs are logged.
        val url = "wss://generativelanguage.googleapis.com/ws/" +
            "google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$key"
        val request = Request.Builder().url(url).build()'''
new='''        // Google AI Studio auth keys (AQ.) and standard API keys are sent via a header.
        // Do not put credentials in the WebSocket URL, exception messages, or logs.
        val url = "wss://generativelanguage.googleapis.com/ws/" +
            "google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
        val request = Request.Builder().url(url).header("x-goog-api-key", key).build()'''
assert old in s, "WebSocket authentication snippet changed unexpectedly"
p.write_text(s.replace(old,new))
print("Gemini Live WebSocket now authenticates using x-goog-api-key header")
