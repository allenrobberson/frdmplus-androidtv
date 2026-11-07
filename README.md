# FRDM+ for Android TV

A free, open-source Android TV / Google TV app for **[FRDM+](https://frdmplus.com)**.
Sign in with your FRDM+ account and watch live events and replays on your TV,
using your TV remote. This is a fan-made app, and the FRDM+ team has been contacted
about making it official on the Google Play Store.

## Install it on your TV (testers start here)

**What you need:** a Google TV or Android TV (like a Google TV Streamer, Chromecast with
Google TV, or a Sony, TCL, Hisense or Nvidia Shield TV), its remote, and about 10 minutes.
No computer needed. Do the steps in order. You only have to do Parts 1 to 3 once.

### Part 1: Unlock your TV (one time only)

1. Press the **Home** button on your remote.
2. Go to the top right corner of the screen and pick the **gear** ⚙️ (Settings).
   - Don't see a gear? Pick your **profile picture** at the top right first, then **Settings**.
3. Pick **System**.
4. Pick **About**.
5. Scroll down until **Android TV OS build** is highlighted.
6. Press the middle **OK** button on your remote **7 times** in a row.
7. You'll see a message that says **"You are now a developer!"** 🎉 (If it asks for your PIN, type it in.)
8. Press the **Home** button to go back.

### Part 2: Get the Downloader app (one time only)

1. On the home screen, go to **Apps**, or use **Search** 🔍.
2. Type **Downloader** and pick the orange app called **Downloader by AFTVnews**.
3. Pick **Install**. Wait until it says **Open**.
4. Press the **Home** button.

### Part 3: Let Downloader install apps (one time only)

1. Go to the **gear** ⚙️ (Settings) again, like in Part 1.
2. Pick **Apps**.
3. Pick **Security & restrictions**.
4. Pick **Unknown sources**.
5. Find **Downloader** and turn its switch **on** (it turns blue).
6. Press the **Home** button.

### Part 4: Get the FRDM+ app

1. Open **Downloader**.
2. The first time, it asks to use your files. Pick **Allow**. If you see a welcome
   message, pick **OK**.
3. On the left side, make sure **Home** is picked.
4. Pick the white box that says **"Enter a URL or Search Term"**. A keyboard pops up.
5. Type this exactly (big and small letters matter):

   **`https://github.com/allenrobberson/frdmplus-androidtv/releases/download/test-build/frdmplus-tv-test.apk`**

   💡 Easier way: open the **Google TV app** on your phone, tap the **remote**, then the
   **keyboard** button, and type or paste it from your phone.
6. Pick **Go**. Downloader downloads FRDM+ (it only takes a few seconds).
7. A screen asks **"Do you want to install this app?"** Pick **Install**.
   - If a box says your TV **isn't allowed to install unknown apps**, pick **Settings**,
     turn **Downloader** on, then press **Back** and pick **Install** again.
   - If **Google Play Protect** shows a warning, pick **More details**, then **Install anyway**.
     (This happens because the app isn't on the Play Store yet.)
8. When it says **App installed**, pick **Open**. 🎉
9. Downloader may ask to **delete** the download file. Pick **Delete**. It's not needed anymore,
   and the app stays installed.

### Part 5: Sign in and watch

1. FRDM+ opens on the **Sign in** screen.
2. Use the arrows to move the **white dot** (the pointer) to the **Email** box and press **OK**.
   Type your FRDM+ email.
3. Do the same for **Password**, then move the dot to **Sign in** and press **OK**.
4. That's it. Pick a video and enjoy! 🏁 Next time you open the app, you're already signed in.

Don't have a FRDM+ account yet? Make one at **frdmplus.com** on your phone or computer
first, then sign in on the TV.

### How to find the app later

New apps don't show up on the home screen by themselves. To find FRDM+:

1. On the home screen, go to **Apps**.
2. Find **Your apps** and pick **See all**.
3. Pick **FRDM+**.

⭐ To keep it on your home screen: highlight **FRDM+**, **hold down OK** until a menu pops up,
and pick **Add to favorites**.

### Remote buttons

| Button | What it does |
| --- | --- |
| Arrows | Move the white dot |
| OK (middle) | Click what the dot is on |
| Back | Go back a page (on the first page, it closes the app) |
| Play/Pause | Play or pause the video |
| Menu (☰), if your remote has one | Turn the dot off and on |

The dot hides when you're not using it. Press any arrow to bring it back.

### Getting the newest version

Do **Part 4** again. The same address always has the newest version, and it replaces
the old one. You stay signed in.

### Something went wrong?

| Problem | What to do |
| --- | --- |
| Pressing OK 7 times did nothing | Make sure **Android TV OS build** is highlighted, then try again. You may have already done it. |
| Can't find **Unknown sources** | Look in Settings → **Apps** → **Security & restrictions**. On some TVs it's under **Device Preferences** → **Security & restrictions**. |
| Downloader says **404** or **file not found** | Check the address for typos, especially big and small letters. |
| The video won't play | Make sure your FRDM+ membership is active, then close the app and open it again. |
| Still stuck? | [Tell us here](https://github.com/allenrobberson/frdmplus-androidtv/issues/new) and include your TV or streaming device model. |

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
