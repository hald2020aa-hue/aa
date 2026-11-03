# استخدام REA على عينة APK — مراحل متابعة التحليل

المصدر الرسمي: <https://github.com/morluto/rea> ودليل Android:
<https://github.com/morluto/rea/blob/main/docs/android-analysis.md>

هذه الأوامر **لم تُنفذ في الجلسة التي أنشأت هذا المشروع**. تتطلب Node.js 22.23.4+ أو إصدارًا مدعومًا من REA، وJDK 17+ وملف JADX headless المثبت وفق تعليمات المشروع. قد تتغير الإصدارات والمتطلبات، راجع المستودع الرسمي أولًا.

```bash
# في بيئة Linux بإنترنت ومع اعتمادياتها
npx rea-agents setup

# بعد تثبيت CLI من المصدر الرسمي وتوفير JADX headless:
export REA_JADX_MCP_JAR=/absolute/path/to/jadx-headless-mcp-0.7.1-all.jar
APK='/absolute/path/to/original.apk'
rea inspect-android-package "$APK" --detail summary
rea search-android-classes "$APK" 'ReadActivity2'
rea search-android-classes "$APK" 'SourcesActivity'
rea search-android-classes "$APK" 'Download'
rea inspect-android-class "$APK" 'org.hamak.mangareader.feature.read.ReadActivity2'
rea inspect-android-class "$APK" 'org.hamak.mangareader.sources.SourcesActivity'
rea trace-android-references "$APK" 'org.hamak.mangareader.sources.SourcesActivity'
```

## فحوصات موصى بها

1. تسجيل سلوك كل شاشة بإذن المستخدم ومطابقة نقاط التنقل والعودة.
2. تتبع `MainActivity`, `SourcesActivity`, `PreviewActivity2`, `ReadActivity2`, `DownloadsActivity2` ثم تكوين خريطة للشاشات.
3. تحديد أشكال مصادر HTML/API دون استخراج أسرار أو مفاتيح دخول.
4. اختبار التكافؤ للوظائف التي أعيد تنفيذها، ثم تحديث تقرير القيود.
5. احترام الملكية الفكرية وشروط الخدمة والحقوق المتعلقة بالمحتوى.

REA لا يحوّل APK بنقرة واحدة إلى تطبيق جديد مضمون السلوك؛ التحليل ينتهي بأدلة تحتاج هندسة وتنفيذًا واختبارات.
