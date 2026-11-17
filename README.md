# Birdie

Birdie is a personal app for to-dos, habits, sleep, people and money, with a bird mascot who cheers you on.
It is built with [Capacitor](https://capacitorjs.com): the app itself is plain HTML/JS in `dist/`, wrapped for Android (`android/`) and iOS (`ios/`).

## What's where

| Folder | What it is |
| --- | --- |
| `src/app.js` | The app's code (copy it to `dist/app.js` after editing) |
| `dist/` | The finished web app: `index.html` (styles), `app.js`, fonts, icons |
| `android/` | Android project, including widgets, the sleep notification and the in-app updater |
| `ios/` | iOS project (open in Xcode on a Mac) |
| `.github/workflows/` | GitHub builds: signed Android releases, and the iPhone web app |

## First-time GitHub setup

1. **Keep the signing key private.** `keys/` and `android/keystore.properties` are already in `.gitignore`. Never commit them, and keep your own copy somewhere safe (for example Google Drive). Every update must be signed with this same key, or it won't install over your current app.
2. Create the repo and push:
   ```bash
   git init && git add . && git commit -m "Birdie"
   git branch -M main
   git remote add origin https://github.com/skynxi/birdie.git
   git push -u origin main
   ```
   Make the repo **public**. The in-app updater reads releases without logging in, which only works for public repos.
3. Give GitHub the signing key (one time). Either:
   - run `scripts/setup-github-secrets.sh` (needs the [GitHub CLI](https://cli.github.com) and `gh auth login`), or
   - add these four secrets by hand under **Settings → Secrets and variables → Actions**:
     `BIRDIE_KEYSTORE_BASE64` (output of `base64 -w0 keys/birdie-release.jks`), `BIRDIE_STORE_PASSWORD`, `BIRDIE_KEY_ALIAS`, `BIRDIE_KEY_PASSWORD` (the values in `android/keystore.properties`).

## Releasing an Android update

Birdie checks `updates/latest.json` in this repo every time it opens. To ship a new version:

1. Build the signed APK (see "Building locally") with a higher `VERSION_NAME`, e.g. `5.4.0`.
2. Put it in `updates/` as `Birdie-5.4.0.apk` and delete the previous APK.
3. Update `updates/latest.json`: `version`, `title`, `apk`, `size` (bytes) and `notes` (a list of short lines shown in the update pop-up).
4. Commit and push to `main`.

Phones see the new version on their next launch and offer Update now / Remind me later / Don't show again. Data stays because the APK is signed with the same key.

Version numbers must always go up (`5.3.0` → `5.3.1` or `5.4.0`).

GitHub Releases also work: Birdie falls back to the latest release with an `.apk` attached if `latest.json` is missing.

## iPhone

**Option A: web app (free, works today).** In the repo go to **Settings → Pages → Source: GitHub Actions**, then run the "Web app (iPhone)" workflow from the Actions tab. It publishes Birdie to `https://skynxi.github.io/birdie/`. On the iPhone, open it in Safari, tap **Share → Add to Home Screen**. It opens full screen, works offline and updates itself after every push to `main`.
What the web app can't do on iPhone: reminders when the app is closed, home-screen widgets, the sleep notification, and vibration. Use **Settings → Backup** now and then, since its data lives only in Safari's storage.

**Option B: real iOS app (needs a Mac with Xcode).**
```bash
npm install
npx cap sync ios
npx cap open ios
```
In Xcode pick your Apple ID under **Signing & Capabilities**, plug in the iPhone and press Run.
With a free Apple ID the app stops opening after 7 days and has to be run again from Xcode. With the Apple Developer Program ($99/year) you can use TestFlight for proper installs and updates.
Reminders, haptics, backup and sharing work on iOS. Widgets, the "I'm awake" sleep notification and the in-app updater are Android-only for now.

## Building locally

```bash
npm install
cp src/app.js dist/app.js
npx cap sync android
cd android && VERSION_NAME=5.2.0 ./gradlew assembleRelease
```
The APK lands in `android/app/build/outputs/apk/release/`.
