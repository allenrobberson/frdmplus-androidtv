# FRDM+ for Android TV

A free, open-source Android TV / Google TV app for **[FRDM+](https://frdmplus.com)**.
Sign in with your FRDM+ account and watch live events and replays on your TV,
using your TV remote. This is a fan-made app, and the FRDM+ team has been contacted
about making it official on the Google Play Store.

## Install it on your TV (testers start here)

Works on the Google TV Streamer, Chromecast with Google TV, and other Android TV /
Google TV devices (Sony, TCL, Hisense, Nvidia Shield and more). It takes about
5 minutes and no computer is needed.

1. **Turn on developer mode:** on the TV, go to Settings → System → About, highlight
   **Android TV OS build**, and press OK on the remote 7 times.
2. **Install Downloader:** get the free **Downloader** app (by AFTVnews) from the
   Play Store on your TV.
3. **Let Downloader install apps:** Settings → Apps → Security & restrictions →
   Unknown sources → turn on **Downloader**.
4. **Download FRDM+:** open Downloader. If it asks for permission to access files,
   choose **Allow**. On Downloader's **Home** tab, select the box that says
   **"Enter a URL or Search Term"**, type this address into it, and press **Go**:

   `https://github.com/allenrobberson/frdmplus-androidtv/releases/download/test-build/frdmplus-tv-test.apk`

   Tip: type it carefully (it's case-sensitive), or let the Google TV app on your
   phone type it for you. Downloader then downloads the app file automatically.
5. Choose **Install**, then **Open**.

**Finding it later:** Google TV doesn't put new apps on the home screen. Go to
**Apps → Your apps → See all**, find **FRDM+**, then long-press it and choose
**Add to favorites** to keep it handy.

**Using it:** sign in with the same email and password you use on frdmplus.com. The
arrow keys move the pointer, **OK** clicks, **Back** goes back, and **Menu** switches
the pointer off and on. Members-only videos need an active FRDM+ membership;
new accounts are created on frdmplus.com.

**Updating:** repeat step 4. The link always has the newest test build, and it
installs right over the old one.

**Found a problem or have an idea?** [Open an issue](https://github.com/allenrobberson/frdmplus-androidtv/issues/new)
and include your TV or streaming device model.

## Features

- **Remote-control pointer.** The arrow keys move an on-screen pointer and **OK**
  clicks, so every part of the site works with a remote. Hold an arrow to speed up;
  push past the edge of the screen to scroll. The pointer hides after 5 idle seconds.
- **Menu button** switches between pointer mode and plain arrow-key mode, which
  passes the arrow keys straight to the website.
- **Full-screen video**, with DRM (Widevine) playback allowed for protected streams.
- **Media keys**: Play/Pause, Fast-forward (+10 s) and Rewind (−10 s) control the video.
- **Back** goes to the previous page, and from the first screen returns to the TV home screen.
- **Opens on the sign-in screen** for anyone who isn't signed in; people who are
  already signed in go straight to the FRDM+ home screen.
- **Sign in** with an existing FRDM+ account using the TV keyboard (tip: the
  Google TV app on a phone can type for you). "Remember me" is ticked automatically.
- **Sign-up and subscribing** are sent to the website: opening those pages shows "Create your
  account at frdmplus.com, then sign in here". This keeps the app within Google Play's
  payments rules; set `ALLOW_SIGNUP = true` in `app/build.gradle.kts` only if FRDM+
  enrolls in Play's alternative billing / external links programs.
- Stays signed in between launches, keeps the screen awake, and shows a retry
  screen when the internet is down.
- No analytics, ads or tracking. See [PRIVACY.md](PRIVACY.md).

## For developers

### Build it

You need [Android Studio](https://developer.android.com/studio) (it includes the JDK and Android SDK).

1. Clone this repo and open the folder in Android Studio.
2. Wait for Gradle sync, then **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

Or from a terminal: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.

Every push to GitHub is also built by GitHub Actions; the APK is attached to each
run under **Actions → Build → Artifacts**.

To point the app at a different address, change `START_URL` in `app/build.gradle.kts`.

### Other ways to run it

- **Emulator:** in Android Studio, add a *Television* device in Device Manager and press Run.
- **adb:** turn on *Developer options → USB/Wireless debugging* on the TV, then
  `adb connect <tv-ip>` and `adb install frdmplus-tv-test.apk`.

Test builds are signed with the public `debug.keystore` in this repo, so they can
always update each other. Play Store builds use your private upload key instead.

### Publishing a test build

Pushing to `main` refreshes the **test-build** release automatically. Pushing a
version tag (`git tag v0.2.0 && git push origin v0.2.0`) also creates a numbered release.

### Publish to Google Play

Go through **[PLAY_STORE_CHECKLIST.md](PLAY_STORE_CHECKLIST.md)** first. It covers the
policy items (permission letter, payments, data safety, reviewer login) that decide
whether review passes.


1. **Developer account.** Sign up at [play.google.com/console](https://play.google.com/console)
   (one-time fee). New *personal* accounts must run a closed test with at least
   **12 testers for 14 days in a row** before they can publish to everyone.
   *Organization* accounts skip that but need a D-U-N-S number.
2. **Upload key.** Create one in Android Studio (**Build → Generate Signed App Bundle → Create new**),
   save the `.jks` file in this folder, and create `keystore.properties` next to it:
   ```properties
   storeFile=upload-key.jks
   storePassword=…
   keyAlias=upload
   keyPassword=…
   ```
   Both files are git-ignored. Back them up somewhere safe and never commit them.
3. **Build the bundle** (Play requires an `.aab` for TV apps): `./gradlew bundleRelease`
   → `app/build/outputs/bundle/release/app-release.aab`.
4. **Create the app** in Play Console and fill in the store listing:
   - Icon: `store/icon-512.png`. Android TV banner: `store/tv-banner-1280x720.png`.
   - At least one **Android TV screenshot** (1920×1080, taken on the TV or emulator).
   - Mention "Android TV" in the description.
   - Privacy policy URL: link to `PRIVACY.md` on GitHub (or host it on GitHub Pages).
5. **Policy forms.** In **App content**, complete the data-safety form (the app itself
   collects nothing; the website's own collection applies) and content rating.
   If FRDM+ needs a sign-in, add a **test login under *App access*** so reviewers can get in.
   Keep FRDM+'s written permission handy: Google may ask for proof that you're
   allowed to show the site and use its name and logo.
6. **Opt in to TV.** **Setup → Advanced settings → Form factors → Add → Android TV**,
   then upload the bundle to a testing track, run the test, and promote to production.
   Google reviews TV apps separately against the
   [TV app quality guidelines](https://developer.android.com/docs/quality-guidelines/tv-app-quality).

For each update, raise `versionCode` (and `versionName`) in `app/build.gradle.kts`.

### Project layout

```
app/src/main/java/com/frdmplus/tv/MainActivity.kt  WebView, remote keys, video, DRM
app/src/main/java/com/frdmplus/tv/CursorView.kt    the on-screen pointer
app/src/main/res/drawable-xhdpi/banner.png         TV home-screen banner (320×180)
store/                                             Play Store listing graphics
```

The banner and icons are simple text placeholders; swap in the official FRDM+ logo
at the same sizes.

## License

The code is under the [MIT License](LICENSE). The FRDM+ name, logo and website
content belong to FRDM+ and aren't covered by that license.
