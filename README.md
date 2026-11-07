# FRDM+ for Android TV

An open-source Android TV app for **[FRDM+](https://frdmplus.com)**. It opens
the FRDM+ website full-screen on your TV and makes it work with a TV remote.
Published with permission from FRDM+.

## Features

- **Remote-control pointer.** The arrow keys move an on-screen pointer and **OK**
  clicks, so every part of the site works with a remote. Hold an arrow to speed up;
  push past the edge of the screen to scroll. The pointer hides after 5 idle seconds.
- **Menu button** switches between pointer mode and plain arrow-key mode, which
  passes the arrow keys straight to the website.
- **Full-screen video**, with DRM (Widevine) playback allowed for protected streams.
- **Media keys**: Play/Pause, Fast-forward (+10 s) and Rewind (−10 s) control the video.
- **Back** goes to the previous page; on the first page, press it twice to exit.
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

## Build it

You need [Android Studio](https://developer.android.com/studio) (it includes the JDK and Android SDK).

1. Clone this repo and open the folder in Android Studio.
2. Wait for Gradle sync, then **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

Or from a terminal: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.

Every push to GitHub is also built by GitHub Actions; the APK is attached to each
run under **Actions → Build → Artifacts**.

To point the app at a different address, change `START_URL` in `app/build.gradle.kts`.

## Try it on a TV

### Google TV Streamer / Chromecast with Google TV (no computer needed)

1. **Turn on developer mode:** Settings → System → About → highlight **Android TV OS build**
   and press OK 7 times.
2. On the TV, install the free **Downloader** app (by AFTVnews) from the Play Store.
3. **Allow it to install apps:** Settings → Apps → Security & restrictions →
   Unknown sources → turn on **Downloader**.
4. Open Downloader, type this address and press Go:
   `https://github.com/allenrobberson/frdmplus-androidtv/releases/latest/download/frdmplus-tv-test.apk`
5. Choose **Install**, then **Open**. FRDM+ also appears in your Apps list.

New test builds install right over the old one: repeat step 4.

### Other ways

- **Emulator:** in Android Studio, add a *Television* device in Device Manager and press Run.
- **adb:** turn on *Developer options → USB/Wireless debugging* on the TV, then
  `adb connect <tv-ip>` and `adb install frdmplus-tv-test.apk`.

Test builds are signed with the public `debug.keystore` in this repo, so they can
always update each other. Play Store builds use your private upload key instead.

### Publishing a new test build

Push a version tag and GitHub publishes the APK at the link above:
`git tag v0.1.1 && git push origin v0.1.1`

## Publish to Google Play

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

## Project layout

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
