# تقرير هندسة عكسية أولي — الدليل الساكن

## العينة

- الملف: `قارئ مانجا 2.0.7.6.11 (org.hamak.mangareader).apk` (قدمه المستخدم)
- SHA-256: `7cf3be41439d62163d15caa4a75ac84aa2fc300b2f7764baca671fa69afbb0f6`
- APK package: `org.hamak.mangareader`
- versionCode: `276`
- versionName: `2.0.7.6.11`
- minSdkVersion: `25`
- targetSdkVersion: `36`
- compileSdkVersion: `36`
- الحجم: حوالي 23 ميجابايت.
- عدد ملفات DEX: 5.
- عدد class definitions في الحزمة التطبيقية: **2425** من تعريفات DEX، تشمل الأصناف الداخلية والموارد؛ ليست جميعها وحدات وظيفية مستقلة.

## آثار بنيوية في Manifest والموارد

المكونات المسماة التالية مصرح بها، ما يقدم دليلًا على وجودها في بنية التطبيق. **التصريح لا يثبت أن وظيفة كل مكوّن تعمل على جهاز معين**:

| المسار | الدلالة |
| --- | --- |
| `feature.main.MainActivity` | الشاشة الرئيسية |
| `sources.SourcesActivity` | إدارة المصادر |
| `sources.migration.SourceMigrationRecoveryActivity` | استعادة هجرة المصادر |
| `feature.search.SearchActivity2` | البحث |
| `feature.preview.PreviewActivity2` | صفحة المعاينة/التفاصيل |
| `feature.read.ReadActivity2` | القارئ |
| `feature.download.DownloadsActivity2` | التنزيل |
| `feature.download.service.DownloadPauseReceiver` | إيقاف التنزيل |
| `feature.download.service.DownloadResumeReceiver` | استئناف التنزيل |
| `feature.newchapter.NewChaptersActivity` | الفصول الجديدة |
| `feature.settings.main.SettingsActivity2` | الإعدادات |
| `feature.cloud.ui.AccountActivity` | الحساب |
| `services.ImportService` و`services.ExportService` | استيراد وتصدير |
| `core.network.browser.BrowserActivity` | متصفح الويب |

وُجدت ملفات التصميم `activity_main.xml` و`activity_reader2.xml` و`activity_downloads.xml` و`activity_sources.xml` و`activity_preview2.xml` و`activity_search2.xml` بالإضافة إلى قوائم إعدادات القراءة والمصادر والتنزيلات وغيرها.

### إصدارات المكتبات المثبتة من بيانات APK

- `androidx.room:room-runtime`: 2.8.5
- `androidx.work:work-runtime`: 2.12.0
- `androidx.webkit:webkit`: 1.17.1
- `androidx.compose.runtime:runtime-annotation`: 1.9.0

### مجالات الأصناف (من جداول Class Def في DEX)

- `feature`: 1580 صنفًا / صنفًا داخليًا.
- `providers`: 252.
- `sources`: 239.
- `core`: 122.
- `preference`: 56.

**مهم:** هذه إحصاءات وأسماء ثابتة، وليست تفكيكًا إلى كود مصدر قابل لإعادة البناء كما هو. **لم تُستخدم REA/JADX في هذه الجلسة** لعدم توفرهما ولا إمكانية تنزيلهما، ولم تُشغَّل العينة.

## لماذا لم تُنسخ الشاشات الأصلية من APK؟

ملفات XML الأصلية مجمعة بصيغة Android Binary XML، والأيقونات والرسوم الأصلية قد تكون محمية بحقوق أصحابها. المشروع الجديد أعاد تنفيذ الوظائف الأساسية بنصوص وواجهات مستقلة. لم تتم محاكاة كل انتقالات الواجهة ولم تُراجع الفيديوهات ضمن هذه الجلسة.

## مهام تستدعي مراجعة ديناميكية

- مطابقة التنقلات والقوائم والهوية البصرية لكل شاشة عبر تسجيل جلسة فيديو موثقة.
- تتبع نظام مصادر المانجا والمكوّنات الأصيلة واستدعاءات API/HTML.
- تحليل صيغ قاعدة البيانات والاستيراد والتصدير والتقدم في القراءة.
- اختبار تنزيل فصول كبيرة، الاستئناف عند قطع الشبكة، البيانات المحمية، تحديثات الخلفية.
- اختبار الطي والفتح على Galaxy Z Fold4 أو محاكي قابل للطي.
- فحص الأداء، وإدارة الذاكرة، والأمان، وفقدان الشبكة.

## حدود الاستدلال

تحليل Manifest وملفات DEX يعكس ما داخل العينة المرفقة فقط؛ لا يثبت عمل الخدمات السحابية أو توافر المصادر أو نتائج تشغيل الحساب/المتجر. تطبيق "قارئ كامل" ليس استنساخًا قابلًا للتثبيت حتى ينجح بناء Android ويجتاز اختبار الجهاز.
