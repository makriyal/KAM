## Google AdMob specific rules ##
## https://developers.google.com/admob/android/quick-start ##
-keep public class com.google.android.gms.ads.** {
    public *;
}

-keep public class com.google.ads.** {
   public *;
}