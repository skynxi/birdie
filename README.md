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

**Without the key on GitHub (current setup):** build the APK locally (see "Building locally" below), then create a release on GitHub named after the version (for example `v5.3.0`) and attach the `.apk` file. The tag-triggered workflow skips the build when the key isn't set, so it never fails.

**With the key on GitHub:**

```bash
git add . && git commit -m "What changed"
git tag v5.2.0
git push && git push origin v5.2.0
```

GitHub builds a signed APK and attaches it to a release named after the tag. Edit the release notes on GitHub if you like: Birdie shows them in the update screen.

On the phone, open **Settings → App updates → Check for updates → Download and install**. The first time, Android asks you to allow installs from Birdie. Data stays, because the key is the same.
Birdie also checks once a day and puts a dot on the settings gear when a new version is out.

Version numbers: always go up (`v5.2.0`, then `v5.2.1` or `v5.3.0` …). Tags must look like `vMAJOR.MINOR.PATCH`.

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
