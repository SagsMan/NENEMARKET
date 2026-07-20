# Nenemarket — Android & iOS App
### Complete Build Guide (Start to Finish)

> **What this project is:** A native Android/iOS app that loads **nenemarket.ng** live inside  
> a WebView. No backend changes needed — the app is the website with native features on top.

---

## QUICK ANSWER: What to do after extracting the ZIP

```
Extract ZIP  →  Install 4 tools  →  Run 3 commands  →  Open Android Studio  →  Get your APK
```

The full step-by-step is below. Follow it top to bottom.

---

## PART 1 — Install the Required Tools

You only do this once. After tools are installed, skip straight to Part 2 every time.

---

### Step 1 — Install Node.js

Node.js runs the build scripts.

1. Go to **https://nodejs.org**
2. Click the **LTS** button (the left one — "Recommended for most users")
3. Run the installer — click Next all the way through, keep all defaults
4. When done, open a terminal and confirm:

```
node --version
```
You should see something like `v20.x.x`. Anything 18 or higher is fine.

> **Windows:** Search "Command Prompt" in the Start menu and open it.  
> **Mac:** Open the Terminal app (Applications → Utilities → Terminal).

---

### Step 2 — Install pnpm (package manager)

After Node.js is installed, run this single command in your terminal:

```
npm install -g pnpm
```

Confirm it worked:
```
pnpm --version
```
You should see `8.x.x` or higher.

---

### Step 3 — Install Java JDK 17

Android Studio needs Java to build Android apps.

1. Go to **https://adoptium.net**
2. Select:
   - **Version:** Eclipse Temurin 17 (LTS)
   - **Operating System:** your OS (Windows / macOS)
   - **Architecture:** x64 (most PCs) — or arm64 if you have Apple Silicon Mac
3. Download and run the installer
4. On Windows: tick **"Set JAVA_HOME variable"** and **"Add to PATH"** during install
5. Confirm:

```
java --version
```
You should see `openjdk 17.x.x`.

---

### Step 4 — Install Android Studio

Android Studio is the tool that turns your code into an APK file.

1. Go to **https://developer.android.com/studio**
2. Click **Download Android Studio**
3. Run the installer — keep all defaults
4. On first launch, Android Studio runs a Setup Wizard:
   - Choose **Standard** installation type
   - Accept all license agreements
   - Let it download the Android SDK (this takes 5–15 minutes)
5. When the wizard finishes you will see the Android Studio welcome screen — you are ready

> **Important:** Do NOT skip the SDK download step. The SDK is what compiles your app.

---

## PART 2 — Set Up the Project

Now that your tools are installed, set up the project files.

---

### Step 5 — Extract the ZIP and open a terminal inside it

1. Find `nenemarket-app.zip` (downloaded from Replit)
2. Right-click → **Extract All** (Windows) or double-click (Mac)
3. You will get a folder called `artifacts/nenemarket-app/` inside
4. Open your terminal **inside** that `nenemarket-app` folder:

   **Windows (easiest way):**
   - Open the `nenemarket-app` folder in File Explorer
   - Click the address bar at the top, type `cmd`, press Enter
   - A Command Prompt opens already inside that folder

   **Mac:**
   - Open Terminal
   - Type `cd ` (with a space after), then drag the `nenemarket-app` folder into the Terminal window
   - Press Enter

5. Confirm you are in the right place:

```
pwd
```
The output should end with `.../nenemarket-app`

---

### Step 6 — Install project dependencies

Run this command (it downloads the Capacitor packages):

```
pnpm install
```

Wait for it to finish. You will see a progress bar and then `Done` or similar. This may take 1–3 minutes on first run.

---

### Step 7 — Build the web shell

This compiles the thin loading screen that Capacitor uses while the native WebView boots:

```
pnpm run build
```

When done you will see a `dist/` folder appear. This is normal.

---

### Step 8 — Sync the Android project

This copies the compiled assets and plugin configs into the Android Studio project:

```
npx cap sync android
```

You will see lines like `✔ Copying web assets` and `✔ Updating Android plugins`. That means it worked.

---

## PART 3 — Build the Android APK

---

### Step 9 — Open Android Studio from the project

Still in the terminal, run:

```
npx cap open android
```

This opens Android Studio with the Nenemarket project loaded automatically.

---

### Step 10 — Wait for Gradle sync

When Android Studio opens you will see a progress bar at the **bottom** of the window that says **"Gradle: Syncing..."**

- ⏳ Wait for it to finish — usually 2–5 minutes the first time
- Do NOT click anything until the progress bar disappears
- If it asks you to update Gradle plugins, click **"Don't remind me again"** (the existing version works fine)
- When done, the project tree on the left will fully expand

> **If you see an error about SDK location:**  
> Click `File → Project Structure → SDK Location` and point it to where Android Studio installed the SDK.  
> Usually `C:\Users\YourName\AppData\Local\Android\Sdk` on Windows,  
> or `/Users/YourName/Library/Android/sdk` on Mac.

---

### Step 11 — Test on a real device or emulator (optional but recommended)

**Option A — Real Android phone (recommended):**
1. On your phone: Settings → About phone → tap **Build number** 7 times → Developer mode enabled
2. Settings → Developer options → turn on **USB Debugging**
3. Plug phone into PC with a USB cable
4. In Android Studio, your phone appears in the toolbar at the top
5. Click the ▶ **Run** button
6. The app installs and opens on your phone — you will see nenemarket.ng loading live

**Option B — Android Emulator:**
1. In Android Studio: `Tools → Device Manager → Create Device`
2. Choose **Pixel 7** → Next → select **API 34** → Finish
3. Click ▶ Run with the emulator selected

---

### Step 12 — Build the APK file (for sharing/distribution)

When you are happy the app works:

1. In Android Studio click **Build** in the top menu bar
2. Click **Generate Signed Bundle / APK...**
3. A dialog appears — choose **APK** (for sharing directly) or **Android App Bundle** (for Google Play)
4. Click **Next**

**Keystore (app signing — first time only):**

A keystore is a file that proves the APK came from you. You create it once and keep it safe.

- Click **Create new...**
- Fill in:
  - **Key store path:** click the folder icon, choose a safe location (e.g. Desktop), name it `nenemarket-keystore.jks`
  - **Password:** choose any strong password — **write it down, you will need it forever**
  - **Key alias:** `nenemarket`
  - **Key password:** same as above
  - **Validity:** 25
  - **First and last name:** your name or company name
  - **Country code:** NG
- Click **OK**

5. Back in the signing dialog: make sure your new keystore is selected, enter the passwords
6. Click **Next**
7. Choose **release** build variant
8. Click **Create**
9. Wait 1–3 minutes — a notification pops up at the bottom right: **"APK(s) generated successfully"**
10. Click **locate** in that notification to find your APK file

Your APK is ready. You can:
- Copy it to an Android phone and install it (allow "Install from unknown sources" in phone settings)
- Upload it to the **Google Play Console** (https://play.google.com/console)

> ⚠️ **Keep your keystore file and passwords safe.** If you lose them you cannot update the app.  
> Back up `nenemarket-keystore.jks` to Google Drive or a USB drive.

---

## PART 4 — Build iOS IPA (Mac only)

> **You need a Mac for iOS.** You also need an **Apple Developer account** ($99/year) from  
> https://developer.apple.com to install on real iPhones or submit to the App Store.

---

### Step 13 — Install CocoaPods (Mac only)

Open Terminal and run:

```
sudo gem install cocoapods
```

Enter your Mac password when asked. Wait for it to finish.

---

### Step 14 — Add the iOS platform

In the terminal, inside the `nenemarket-app` folder:

```
npx cap add ios
npx cap sync ios
npx cap open ios
```

This opens **Xcode** with the Nenemarket project.

---

### Step 15 — Configure signing in Xcode

1. In Xcode, click **App** in the left file tree (the top item)
2. Click the **Signing & Capabilities** tab
3. Under **Team**, click the dropdown and sign in with your Apple Developer account
4. Set **Bundle Identifier** to `ng.nenemarket.app`
5. Xcode will show a green tick when signing is set up correctly

---

### Step 16 — Test on iPhone

1. Plug your iPhone into the Mac
2. Select your iPhone in the device dropdown at the top of Xcode
3. Click ▶ Run
4. First time: on your iPhone go to **Settings → General → VPN & Device Management** → trust the developer certificate

---

### Step 17 — Build the IPA for App Store

1. Unplug the iPhone
2. In Xcode select **"Any iOS Device (arm64)"** in the device dropdown
3. Click **Product → Archive**
4. When archiving finishes the **Organizer** window opens
5. Click **Distribute App → App Store Connect → Next → Next → Upload**
6. Go to **https://appstoreconnect.apple.com** and submit your app for review

---

## PART 5 — How the App Stays in Sync with the Website

```
┌─────────────────────────────────────────┐
│         Phone (Android or iPhone)        │
│                                           │
│   Native Shell (Capacitor)               │
│   ┌───────────────────────────────────┐  │
│   │         Native WebView            │  │
│   │                                   │  │
│   │  Loads https://nenemarket.ng      │  │
│   │  live on every screen             │  │
│   └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
              │  HTTPS  │
              ▼         ▼
┌─────────────────────────────────────────┐
│       nenemarket.ng Server               │
│   PHP + MySQL — listings, users,         │
│   search, photos, messages               │
└─────────────────────────────────────────┘
```

| What the user does | What happens |
|---|---|
| Opens the app | WebView loads nenemarket.ng homepage — always fresh |
| Searches for an item | Hits `/search?q=keyword` on the server — MySQL results in real time |
| Posts a listing | Sends data to PHP — live on website instantly |
| Logs in | PHP sets a session cookie stored in the native cookie jar |
| Stays logged in | Cookie persists even when app is closed and reopened |
| Browses listings | Every page is fetched live from the server |

**No sync is needed** — there is only one source of truth: the nenemarket.ng MySQL database.  
When the website is updated (new design, new feature, price change) every app user sees it automatically with no app update required.

---

## PART 6 — Troubleshooting Common Problems

| Problem | Fix |
|---|---|
| `pnpm: command not found` | Run `npm install -g pnpm` first |
| `java: command not found` | Reinstall JDK 17 from adoptium.net and make sure "Add to PATH" is ticked |
| Gradle sync fails with "SDK not found" | `File → Project Structure → SDK Location` → point to Android SDK folder |
| Gradle sync fails with "Could not resolve" | You are offline. Connect to internet and try `File → Sync Project with Gradle Files` |
| `npx cap open android` does nothing | Open Android Studio manually, then `File → Open → nenemarket-app/android` |
| App installs but shows blank screen | Make sure your phone/emulator has internet access |
| "Install blocked" on Android phone | Go to Settings → Security → Allow install from unknown sources |
| Keystore password forgotten | You cannot recover it. Create a new keystore — but the Play Store will treat it as a different app |
| iOS: "No team selected" | Sign in to Apple Developer account inside Xcode preferences |
| iOS: "Untrusted developer" on phone | Settings → General → VPN & Device Management → trust your certificate |

---

## PART 7 — File Reference

| File | What it does |
|---|---|
| `capacitor.config.ts` | App ID (`ng.nenemarket.app`), live URL, splash settings |
| `src/main.ts` | Status bar colour, offline banner, back button, splash hide |
| `android/app/src/main/java/…/MainActivity.java` | Photo upload, external links, back navigation |
| `android/app/src/main/AndroidManifest.xml` | Android permissions (Camera, Internet, Storage) |
| `index.html` | Splash screen shown while WebView starts |
| `public/mockup.html` | Browser preview — open in Chrome to see all app screens |
| `public/iphone-xr.html` | iPhone XR frame screenshot preview |

---

## PART 8 — What to Do When the Website Changes

| Scenario | Action needed |
|---|---|
| New listings / content on website | Nothing — app shows it automatically |
| New page or feature on nenemarket.ng | Nothing — app shows it automatically |
| You want to change the app icon | Replace icon files in `android/app/src/main/res/` → rebuild |
| You want to change splash colour | Edit `backgroundColor` in `capacitor.config.ts` → rebuild |
| Play Store update / version bump | Edit `versionCode` + `versionName` in `android/app/build.gradle` → rebuild → upload |

---

## Summary Checklist

```
ANDROID APK
□ Install Node.js (nodejs.org — LTS version)
□ Run: npm install -g pnpm
□ Install Java JDK 17 (adoptium.net)
□ Install Android Studio (developer.android.com/studio)
□ Extract nenemarket-app.zip
□ Open terminal inside nenemarket-app folder
□ Run: pnpm install
□ Run: pnpm run build
□ Run: npx cap sync android
□ Run: npx cap open android
□ Wait for Gradle sync to finish
□ Build → Generate Signed Bundle / APK
□ Create keystore (first time) — SAVE THE FILE AND PASSWORD
□ Click Create → wait → APK ready

iOS IPA (Mac only)
□ sudo gem install cocoapods
□ Run: npx cap add ios
□ Run: npx cap sync ios
□ Run: npx cap open ios
□ Set team and Bundle ID in Xcode Signing tab
□ Product → Archive → Distribute App
```

---

Website: [nenemarket.ng](https://nenemarket.ng) · App ID: `ng.nenemarket.app`
