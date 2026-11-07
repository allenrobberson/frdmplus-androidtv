# Google Play submission checklist — FRDM+ for Android TV

Work through this before pressing **Send for review**. Items marked ⚠️ are the
ones most likely to cause a rejection for this kind of app.

## 1. Permission and branding ⚠️

Google's **Webview spam** and **Impersonation / Intellectual property** policies
reject apps that show someone else's website or use their name and logo without
proof of permission.

- [ ] Get a **signed letter from No Brakes LLC** (owner of FRDM+) saying you may
      build and publish the FRDM+ Android TV app on Google Play and use the FRDM+
      name, logo and website content. Save it as a PDF.
- [ ] **Before** submitting, send it through Google's
      [Advance notice form](https://support.google.com/googleplay/android-developer/contact/adv_note)
      ("written documentation proving permission to use a 3rd party's intellectual
      property"). Google replies within about 2 business days; submitting before
      they reply risks a rejection.
- [ ] Better still, publish from a Play developer account **owned by No Brakes LLC**
      (an organization account) and add yourself as a user. The developer name then
      matches the brand, which avoids most impersonation questions, and organization
      accounts skip the 12-tester / 14-day closed-test rule.

## 2. Payments ⚠️

Selling digital subscriptions inside a Play app requires Google Play Billing, or
enrollment in Play's alternative-billing / external-content-links programs.

- [x] The app sends sign-up, subscribe, gift-purchase and other payment pages to the
      website instead of opening them (`ALLOW_SIGNUP = false`).
- [ ] Don't mention prices, discounts or "subscribe at frdmplus.com" calls to action
      in the **store listing**.
- [ ] Only switch `ALLOW_SIGNUP` to `true` after FRDM+ enrolls in the
      relevant Play program.
- [ ] The site's home-page banner image shows "$19.99 a month or $199 a year". Inside
      a Play app that can read as steering users to pay outside Google Play. Ask FRDM+
      to hide pricing for the TV app (or accept the risk / enroll in Play's programs).

## 3. Privacy policy and Data safety ⚠️

Because the website **is** the app, Google treats data collected through the web
view as data the app collects.

- [ ] Privacy policy URL in Play Console:
      `https://github.com/allenrobberson/frdmplus-androidtv/blob/main/PRIVACY.md`
- [ ] Ask FRDM+ to fix their own policy at https://frdmplus.com/readme/privacy. It
      currently names "Cleetus McFarland" / cleetusmcfarland.com instead of FRDM+ /
      No Brakes LLC, and doesn't mention the TV app.
- [ ] **Data safety form**: confirm with FRDM+, then declare roughly:

  | Data type | Collected | Shared | Purpose | Required |
  | --- | --- | --- | --- | --- |
  | Personal info → Email address, Name | Yes | No | Account management | Required to sign in |
  | Personal info → User IDs | Yes | No | Account management | Required |
  | Financial info → Purchase history | Yes | No | App functionality | Required |
  | App activity → App interactions, In-app search history | Yes | Yes (analytics/marketing via Meta Pixel / Google) | Analytics, Personalization, Marketing | Required |
  | App info & performance → Diagnostics | Yes | No | Analytics | Required |
  | Device or other IDs | Yes | Yes (Meta Pixel / Google) | Analytics, Advertising or marketing, Fraud prevention | Required |

  - Data encrypted in transit: **Yes** (HTTPS only).
  - Users can request deletion: **Yes**, via FRDM+ Contact Us. FRDM+ should provide a
    web page or email for account deletion; Play asks for a link.
  - Accounts can't be created in the app, so the in-app account-deletion requirement
    doesn't apply.

## 4. Reviewer access ⚠️

- [ ] **App content → App access**: "All or some functionality is restricted" and add
      a working **subscriber** email + password, plus a note: *"Use the remote's arrow
      keys to move the pointer and OK to select. Menu toggles the pointer."*
- [ ] Keep that test account active for as long as the app is on Play.

## 5. Other App content forms

- [ ] **Content rating** (IARC questionnaire): answer honestly about the video content
      (motorsport, crashes, language, suggestive imagery on thumbnails).
- [ ] **Target audience**: 18+ (or 13+), **not** designed for children.
- [ ] **Ads**: "No, my app does not contain ads" (unless FRDM+ videos run ads).
- [ ] **News app**: No. **Government app**: No.

## 6. Android TV requirements

- [x] Leanback launcher entry, 320×180 banner showing the app name, launcher icon
- [x] Landscape only; no touchscreen required; full remote (D-pad) navigation
- [x] Video pauses when the user leaves the app; Back on the first screen exits
- [x] Shipped as an Android App Bundle (`./gradlew bundleRelease`)
- [x] Targets API 35 (Android TV apps need 34 or higher)
- [ ] Replace placeholder banner and icons with the official FRDM+ logo
      (`app/src/main/res/drawable-xhdpi/banner.png`, `mipmap-*/ic_launcher.png`,
      `store/`)
- [ ] Store listing: at least one **Android TV screenshot** (1920×1080), the TV
      banner (`store/tv-banner-1280x720.png`), icon (`store/icon-512.png`), and
      the words "Android TV" in the description
- [ ] **Setup → Advanced settings → Form factors → Android TV**: opt in

## 7. Test on a real TV first

Check each of these on the Google TV Streamer before submitting. Google's TV
reviewers check the same things:

- [x] Verified on the website (Nov 7, 2026): after sign-in the site goes to the home page,
      the profile API reports the account as signed in, and subscriber video plays
      (720p adaptive streaming, no DRM). Layout is readable at TV size (960×540).
- [ ] Sign-in screen appears on first launch; signing in works with the TV keyboard
- [ ] After signing in, relaunching goes straight to the home screen
- [ ] Live and on-demand videos play, including full-screen, with sound
- [ ] Remote play/pause, rewind and fast-forward work
- [ ] Text is readable from the couch; nothing important is cut off at the screen edges
- [ ] Back returns to the previous page, and exits from the home screen
- [ ] "Sign up now" shows the "Sign up on the web" message
- [ ] No crashes after 30+ minutes of playback

## 8. Release mechanics

- [ ] Developer account: personal accounts created after Nov 13 2023 need a closed
      test with **12+ testers opted in for 14 days in a row** before production.
- [ ] Upload key created and backed up; `keystore.properties` filled in (never committed).
- [ ] Use **Play App Signing** (the default).
- [ ] Bump `versionCode` for every upload.
