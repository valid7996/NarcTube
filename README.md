# NarcTube

دانلودر حرفه‌ای ویدیوهای یوتیوب برای اندروید — نوشته شده با **Kotlin** و **Jetpack Compose**.

- دانلود ویدیو با انتخاب کیفیت (۱۰۸۰p / ۷۲۰p / ۴۸۰p / ۳۶۰p و ...)
- دانلود صدا به‌صورت **MP3**
- نمایش پیشرفت دانلود با نوتیفیکیشن زنده (Foreground Service)
- تاریخچه دانلودها با قابلیت لغو / تلاش مجدد / حذف / باز کردن فایل
- پشتیبانی از Share Sheet یوتیوب (اشتراک‌گذاری لینک مستقیم به داخل برنامه)
- تم Material 3 با پالت رنگی اختصاصی (بنفش/کریمسون، حالت تیره پیش‌فرض)

---

## ساختار پروژه (Architecture)

معماری **MVVM** با لایه‌بندی ساده:

```
app/src/main/java/com/narctube/app/
├── NarcTubeApplication.kt      # init یوتیوب-دی‌ال و FFmpeg، نوتیفیکیشن چنل
├── MainActivity.kt             # نقطه ورود Compose + مدیریت Share Intent
├── data/
│   ├── model/                  # VideoDetails, VideoFormat, DownloadItem, DownloadStatus
│   ├── local/                  # Room: AppDatabase, DownloadDao, Converters
│   └── repository/
│       ├── YoutubeRepository   # استخراج اطلاعات ویدیو (getInfo)
│       └── DownloadRepository  # صف دانلود + پل ارتباطی با DownloadService
├── service/
│   └── DownloadService.kt      # Foreground Service اجرای واقعی yt-dlp
└── ui/
    ├── theme/                  # رنگ‌ها، تایپوگرافی، Theme.NarcTube
    ├── navigation/              # NavHost + Bottom Navigation
    ├── home/                   # صفحه ورود لینک + انتخاب کیفیت
    ├── downloads/               # صفحه تاریخچه دانلودها
    ├── settings/                # صفحه تنظیمات و یادآوری قانونی
    └── components/              # کامپوننت‌های قابل استفاده مجدد
```

**موتور دانلود:** این پروژه از کتابخانه اوپن‌سورس
[`youtubedl-android`](https://github.com/yausername/youtubedl-android) استفاده می‌کند که
باینری `yt-dlp` را داخل اپ باندل می‌کند (بدون نیاز به روت یا سرور واسط). تبدیل به MP3 هم
از طریق `ffmpeg` باندل‌شده در همان کتابخانه انجام می‌شود.

---

## راه‌اندازی (Setup)

1. پروژه را در **Android Studio (Koala یا جدیدتر)** باز کنید — روی "Open" پوشه `NarcTube` را انتخاب کنید.
2. Android Studio به‌صورت خودکار Gradle Wrapper را می‌سازد (فایل `gradle-wrapper.properties` از قبل روی Gradle 8.7 تنظیم شده). اگر نیاز بود، `File → Sync Project with Gradle Files` را بزنید.
3. نسخه `youtubedl-android` در `app/build.gradle.kts` روی `0.18.1` تنظیم شده؛ قبل از ریلیز، نسخه جدیدتر را در
   [صفحه‌ی Maven Central کتابخانه](https://central.sonatype.com/artifact/io.github.junkfood02.youtubedl-android/library) چک کنید.
4. روی یک دستگاه واقعی یا امولاتور با API 24+ اجرا کنید. (توصیه می‌شود روی دستگاه واقعی تست کنید چون دانلود واقعی از یوتیوب لازم است.)

### نکات فنی مهم
- کتابخانه `youtubedl-android` باینری‌های native (پایتون + ffmpeg) دارد؛ به همین دلیل
  `abiFilters` و `extractNativeLibs="true"` در گریدل/مانیفست تنظیم شده‌اند — این‌ها را حذف نکنید.
- روی اندروید ۱۰ به بالا، فایل‌ها مستقیماً در پوشه عمومی `Download/NarcTube/` ذخیره می‌شوند
  (طبق مستندات کتابخانه، این پوشه حتی زیر Scoped Storage در دسترس مستقیم است).
- برای دانلود چند فایل هم‌زمان، هر دانلود نوتیفیکیشن پیشرفت جداگانه‌ی خودش را دارد.

---

## ⚠️ نکته‌ی مهم API کتابخانه (verify before shipping)

نگاشت خروجی JSON یت‌-دی‌ال‌پی به کلاس‌های Kotlin (`com.yausername.youtubedl_android.mapper.VideoInfo`
و آبجکت‌های فرمت داخل آن) ممکن است بین نسخه‌های مختلف کتابخانه کمی تغییر کرده باشد. کد داخل
`YoutubeRepository.kt` و `DownloadService.kt` بر اساس مستندات و نمونه‌کدهای رسمی نسخه‌ی ۰.۱۸.x
نوشته شده (فیلدهایی مثل `title`, `thumbnail`, `duration`, `uploader`, `formats`, `formatId`,
`height`, `vcodec`, `filesize`). اگر بعد از Sync با خطای "unresolved reference" روی این فیلدها
مواجه شدید، کافیست در Android Studio روی کلاس `VideoInfo` کلیک راست کرده و "Go to Declaration"
بزنید تا نام دقیق فیلدهای نسخه‌ی نصب‌شده را ببینید — بقیه‌ی معماری برنامه به این جزئیات وابسته نیست.

---

## 📜 یادآوری قانونی و کپی‌رایت (Legal Notice)

این برنامه یک **ابزار عمومی** برای دانلود ویدیو است (مشابه پروژه‌های اوپن‌سورس معروفی مثل
`yt-dlp`)، و کاربردهای قانونی زیادی دارد: دانلود محتوای خودتان، محتوای دارای مجوز Creative
Commons، محتوای Public Domain، یا هر محتوایی که صاحب اثر اجازه‌ی دانلود آن را داده باشد.

با این حال:

- دانلود محتوای دارای کپی‌رایت **بدون اجازه‌ی صاحب اثر** ممکن است در بسیاری از کشورها نقض
  قانون کپی‌رایت محسوب شود و همچنین ممکن است با [شرایط استفاده از سرویس یوتیوب](https://www.youtube.com/t/terms)
  در تضاد باشد.
- مسئولیت نحوه‌ی استفاده از این برنامه کاملاً بر عهده‌ی کاربر نهایی است. توصیه می‌شود این
  یادآوری را در صفحه‌ی معرفی برنامه (Play Store listing) یا هنگام اولین اجرا هم نمایش دهید.
- اگر قصد انتشار عمومی/تجاری این اپ را دارید، حتماً بخش زیر (License & Compliance) را هم بخوانید.

---

## License & Compliance

- کد این پروژه تحت **MIT License** منتشر شده (فایل `LICENSE`).
- اما کتابخانه‌ی `youtubedl-android` که برای دانلود استفاده می‌کنیم تحت **GPL-3.0** است.
  به‌طور کلی، وقتی یک کتابخانه‌ی GPL-3.0 به‌صورت کامپایل‌شده داخل اپ شما لینک می‌شود، توزیع
  عمومی اپ (مثلاً روی گیت‌هاب یا استور) معمولاً باید با شرایط GPL-3.0 سازگار باشد. این یک
  نکته‌ی حقوقی رایج در پروژه‌های مشابه است (مثلاً پروژه‌ی [Seal](https://github.com/JunkFood02/Seal)
  که از همین کتابخانه استفاده می‌کند، خودش هم GPL-3.0 است).
- **این توضیح صرفاً جنبه‌ی اطلاع‌رسانی دارد و مشاوره‌ی حقوقی نیست.** اگر قصد انتشار گسترده یا
  تجاری‌سازی دارید، بهتر است با یک وکیل مشورت کنید.

---

## Roadmap / بهبودهای پیشنهادی

- [ ] دانلود پلی‌لیست (در حال حاضر فقط تک‌ویدیو پشتیبانی می‌شود)
- [ ] به‌روزرسانی خودکار باینری yt-dlp از داخل اپ (کتابخانه از `YoutubeDL.updateYoutubeDL()` پشتیبانی می‌کند)
- [ ] Pause/Resume واقعی دانلودها
- [ ] پشتیبانی از aria2c برای دانلود چندبخشی سریع‌تر (کتابخانه از قبل این قابلیت را دارد)
- [ ] تست‌های واحد برای Repository ها

---

## Credits

- [yt-dlp](https://github.com/yt-dlp/yt-dlp) و [youtubedl-android](https://github.com/yausername/youtubedl-android) — موتور اصلی دانلود (GPL-3.0)
- [Coil](https://github.com/coil-kt/coil) — بارگذاری تصاویر بندانگشتی
- Jetpack Compose / Material 3 — رابط کاربری

## License

MIT برای کد این ریپازیتوری — به بخش [License & Compliance](#license--compliance) بالا برای جزئیات مربوط به وابستگی GPL-3.0 توجه کنید.
