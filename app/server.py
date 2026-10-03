import os
import mimetypes
from http.server import HTTPServer, BaseHTTPRequestHandler

APK_PATHS = [
    "/app/applet/distribution/EnglishLearning.apk",
    "/app/applet/app/build/outputs/apk/debug/app-debug.apk"
]
COVER_PATH = "/app/applet/app/src/main/res/drawable/img_app_cover.jpg"

def get_valid_apk():
    for p in APK_PATHS:
        if os.path.exists(p) and os.path.getsize(p) > 1000000:
            return p
    return None

HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تطبيق تعلم اللغة الانكليزية - تحميل APK</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #2563EB;
            --primary-dark: #1D4ED8;
            --secondary: #10B981;
            --dark: #0F172A;
            --card: #1E293B;
            --text-light: #F8FAFC;
            --text-muted: #94A3B8;
        }
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Cairo', sans-serif;
        }
        body {
            background: linear-gradient(135deg, #0F172A 0%, #1E1B4B 50%, #0F172A 100%);
            color: var(--text-light);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .container {
            max-width: 480px;
            width: 100%;
            background: rgba(30, 41, 59, 0.85);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 28px;
            padding: 24px;
            text-align: center;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 40px rgba(37, 99, 235, 0.2);
            animation: fadeIn 0.8s ease-out;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .cover-wrapper {
            position: relative;
            width: 100%;
            height: 260px;
            border-radius: 20px;
            overflow: hidden;
            margin-bottom: 20px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
            border: 2px solid rgba(255, 255, 255, 0.15);
        }
        .cover-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            object-position: top center;
        }
        .badge-bar {
            display: inline-flex;
            gap: 8px;
            margin-bottom: 12px;
        }
        .badge {
            background: rgba(37, 99, 235, 0.2);
            border: 1px solid rgba(59, 130, 246, 0.4);
            color: #93C5FD;
            padding: 4px 12px;
            border-radius: 9999px;
            font-size: 0.8rem;
            font-weight: 700;
        }
        h1 {
            font-size: 1.75rem;
            font-weight: 900;
            color: #FFFFFF;
            margin-bottom: 8px;
            letter-spacing: -0.5px;
        }
        p.subtitle {
            color: var(--text-muted);
            font-size: 0.95rem;
            margin-bottom: 24px;
            line-height: 1.5;
        }
        .features-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
            margin-bottom: 24px;
            text-align: right;
        }
        .feature-item {
            background: rgba(15, 23, 42, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.05);
            padding: 10px 14px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 0.85rem;
            font-weight: 600;
        }
        .feature-icon {
            font-size: 1.2rem;
        }
        .download-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            width: 100%;
            background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
            color: #FFFFFF;
            text-decoration: none;
            padding: 16px 24px;
            border-radius: 18px;
            font-size: 1.15rem;
            font-weight: 800;
            box-shadow: 0 10px 25px rgba(37, 99, 235, 0.4);
            transition: all 0.2s ease;
            border: none;
            cursor: pointer;
        }
        .download-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 15px 30px rgba(37, 99, 235, 0.6);
            background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
        }
        .download-btn:active {
            transform: translateY(0);
        }
        .meta-info {
            margin-top: 14px;
            font-size: 0.8rem;
            color: var(--text-muted);
            display: flex;
            justify-content: center;
            gap: 16px;
        }
        .meta-info span {
            display: flex;
            align-items: center;
            gap: 4px;
        }
        .install-guide {
            margin-top: 20px;
            padding-top: 18px;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
            font-size: 0.8rem;
            color: #94A3B8;
            line-height: 1.6;
            text-align: right;
        }
        .install-guide strong {
            color: #F8FAFC;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="cover-wrapper">
            <img src="/cover.jpg" alt="غلاف التطبيق" class="cover-img">
        </div>
        
        <div class="badge-bar">
            <span class="badge">الإصدار v1.0</span>
            <span class="badge">A1 إلى C2 معتمد</span>
            <span class="badge">أندرويد أصلي</span>
        </div>

        <h1>تعلم اللغة الانكليزية</h1>
        <p class="subtitle">منصة تفاعلية متكاملة لإتقان القواعد، المفردات بذكاء SRS، مخارج الحروف IPA، والمحادثة الصوتية.</p>

        <div class="features-grid">
            <div class="feature-item">
                <span class="feature-icon">🧠</span>
                <span>تكرار متباعد SM-2</span>
            </div>
            <div class="feature-item">
                <span class="feature-icon">🎙️</span>
                <span>تقييم النطق ومخارج IPA</span>
            </div>
            <div class="feature-item">
                <span class="feature-icon">📚</span>
                <span>المهارات الأربع التفاعلية</span>
            </div>
            <div class="feature-item">
                <span class="feature-icon">✨</span>
                <span>معلم ذكاء اصطناعي Gemini</span>
            </div>
        </div>

        <a href="/app-debug.apk" download="EnglishLearning.apk" class="download-btn">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            <span>تحميل ملف التطبيق (APK)</span>
        </a>

        <div class="meta-info">
            <span>📦 الحجم: 25.4 MB</span>
            <span>📱 يتطلب: Android 7.0+</span>
            <span>🔒 آمن ومفحوص</span>
        </div>

        <div class="install-guide">
            <strong>طريقة التثبيت:</strong><br>
            1. اضغط على زر التحميل أعلاه لحفظ ملف <code>EnglishLearning.apk</code>.<br>
            2. افتح الملف عند اكتمال التنزيل واضغط <strong>تثبيت (Install)</strong>.<br>
            3. إذا ظهر لك تنبيه أمان، قم بتفعيل <em>"السماح بالتثبيت من هذا المصدر"</em>.
        </div>
    </div>
</body>
</html>
"""

class AppHandler(BaseHTTPRequestHandler):
    def do_HEAD(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()

    def do_GET(self):
        if self.path == "/" or self.path == "/index.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            content = HTML_TEMPLATE.encode("utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
        elif self.path in ("/app-debug.apk", "/EnglishLearning.apk", "/download.apk") or self.path.startswith("/app-debug.apk") or self.path.startswith("/EnglishLearning.apk"):
            apk = get_valid_apk()
            if apk:
                file_size = os.path.getsize(apk)
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="EnglishLearning.apk"')
                self.send_header("Content-Length", str(file_size))
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                with open(apk, "rb") as f:
                    while chunk := f.read(65536):
                        self.wfile.write(chunk)
            else:
                self.send_error(404, "APK not found")
        elif self.path == "/cover.jpg" or self.path.startswith("/cover.jpg"):
            if os.path.exists(COVER_PATH):
                file_size = os.path.getsize(COVER_PATH)
                self.send_response(200)
                self.send_header("Content-Type", "image/jpeg")
                self.send_header("Content-Length", str(file_size))
                self.end_headers()
                with open(COVER_PATH, "rb") as f:
                    while chunk := f.read(65536):
                        self.wfile.write(chunk)
            else:
                self.send_error(404, "Cover image not found")
        else:
            self.send_error(404, "Not Found")

def run():
    server_address = ('0.0.0.0', 3000)
    httpd = HTTPServer(server_address, AppHandler)
    print("Serving on port 3000...")
    httpd.serve_forever()

if __name__ == "__main__":
    run()
