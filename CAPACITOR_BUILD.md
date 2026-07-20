# Nenemarket — Capacitor Build Guide

## What this project does

Wraps **https://nenemarket.ng** exactly as-is inside a native Android and iOS
app using Capacitor. No UI changes, no backend changes, no redesign — the
website appears pixel-perfect inside the native WebView.

`capacitor.config.ts` contains `server.url = "https://nenemarket.ng"` which
tells the Capacitor native runtime to load the live website directly. The user
sees the real nenemarket.ng on their phone, with all native features added on
top.

---

## Native features implemented

| Feature | How |
|---|---|
| Android back button | `MainActivity.java` — navigates WebView history; exits app when at root |
| File / image upload | `MainActivity.java` — overrides `onShowFileChooser` for `<input type="file">` |
| Camera support | `@capacitor/camera` + CAMERA permission in AndroidManifest |
| Gallery picker | `READ_MEDIA_IMAGES` permission + file chooser intent |
| External links | `MainActivity.java` — non-nenemarket.ng URLs open in the device browser |
| Offline banner | `src/main.ts` — `@capacitor/network` shows/hides a red banner |
| Splash screen | `@capacitor/splash-screen` — green brand colour, hides when page loads |
| Status bar | `@capacitor/status-bar` — white bg with dark icons (matches site header) |
| Keyboard | `@capacitor/keyboard` — resizes body so inputs stay visible |
| Push notification structure | `@capacitor/push-notifications` — ready for Firebase integration |
| Safe Area (iPhone) | `viewport-fit=cover` + `env(safe-area-inset-*)` in index.html |
| Cookie / session persistence | Native WebView persists cookies automatically |
| Keep users logged in | PHP session cookie survives app restarts (native cookie jar) |

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Node.js | 18+ |
| pnpm | 8+ |
| Java JDK | 17+ |
| Android Studio | Hedgehog+ |
| Xcode *(Mac only)* | 15+ |
| CocoaPods *(Mac only)* | 1.13+ |

---

## Android build (APK / AAB)

```bash
# 1 — Install dependencies (already done if cloning this repo)
pnpm install

# 2 — Build the thin web shell
pnpm --filter @workspace/nenemarket-app run build

# 3 — Sync web assets and plugins into the Android project
cd artifacts/nenemarket-app
npx cap sync android

# 4 — Open Android Studio
npx cap open android
```

Inside Android Studio:
- Wait for Gradle sync to complete
- **Run** on a connected device or emulator to test
- **Build → Generate Signed Bundle / APK** → choose AAB for Play Store,
  APK for direct install

---

## iOS build (IPA — Mac required)

```bash
# 1 — Add iOS platform (first time only)
cd artifacts/nenemarket-app
npx cap add ios

# 2 — Install CocoaPods dependencies
cd ios/App && pod install && cd ../..

# 3 — Sync
npx cap sync ios

# 4 — Open Xcode
npx cap open ios
```

Inside Xcode:
- Select your Apple Developer Team under **Signing & Capabilities**
- Choose a real device or simulator
- **Product → Run** to test
- **Product → Archive** then **Distribute App** for App Store submission

---

## After any code change

```bash
pnpm --filter @workspace/nenemarket-app run build
cd artifacts/nenemarket-app && npx cap sync
# Then rebuild in Android Studio / Xcode
```

---

## App configuration

| Key | Value |
|---|---|
| App ID | `ng.nenemarket.app` |
| App Name | Nenemarket |
| Website loaded | `https://nenemarket.ng` |
| Splash background | `#0b7a3d` (brand green) |
| Web assets dir | `dist/public` |

To change any setting edit `capacitor.config.ts`, rebuild, and run `npx cap sync`.

---

## Push notifications (future)

`@capacitor/push-notifications` is installed and declared in `AndroidManifest.xml`.
To activate it:
1. Add a Firebase project, download `google-services.json`, place it in
   `android/app/`
2. Add the Firebase Gradle plugin to `android/build.gradle`
3. On the backend, use the FCM token (logged to console by the plugin) to
   send push messages

iOS push requires an APNs certificate from your Apple Developer account.
