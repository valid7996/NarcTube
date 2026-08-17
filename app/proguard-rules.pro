# youtubedl-android ships its own native binaries; keep its classes intact.
-keep class com.yausername.youtubedl_android.** { *; }
-keep class com.yausername.ffmpeg.** { *; }
-dontwarn com.yausername.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
