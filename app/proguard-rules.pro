# Keep the JavaScript bridge the launch sign-in check calls from the web page.
-keepclassmembers class com.frdmplus.tv.MainActivity$AuthBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
