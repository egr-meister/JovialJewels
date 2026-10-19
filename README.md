# Jovial Jewels

A fully offline Android game in Kotlin + Jetpack Compose with two modes: **Quiz** and **Matching Pairs**.
Package / applicationId: `com.jovialjewels.game`.

---

## Features

- **Quiz "Circles, Balls & Spheres"** — 3 difficulty levels (Easy ★, Medium ★★, Hard ★★★) × 10 questions,
  4 answers each with exactly one correct. One question at a time with a progress bar and `n/10`.
  Tapping an answer marks the correct option green (✓) and a wrong pick red (✗), then auto-advances after ~0.75 s.
  Best score per difficulty is saved. Pause overlay: Continue / Restart / Menu. Results panel: `Score: X/10`, Continue / Restart / Menu.
- **Matching Pairs** — 9 levels (pairs / columns / seconds):

  | Level | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |
  |---|---|---|---|---|---|---|---|---|---|
  | Pairs | 2 | 3 | 4 | 6 | 6 | 8 | 8 | 10 | 12 |
  | Columns | 2 | 2 | 2 | 3 | 3 | 4 | 4 | 4 | 4 |
  | Seconds | 60 | 70 | 70 | 80 | 80 | 100 | 100 | 110 | 120 |

  Deck = `pairs` distinct gems from 12 icons, duplicated and shuffled. Matches stay open, mismatches flip back after 0.7 s.
  The countdown pauses while paused. All pairs before time → **Well Done**; time out → **Out of Time**.
  Best matched pairs per level is saved; a level unlocks once the previous one is cleared (level 1 is always open).
- Screens: Main, Menu, Quiz categories, Quiz, Quiz results, Levels (+ Game Rules), Pairs game, Pairs results,
  Results summary (best per quiz difficulty + pairs cleared / 9), Settings (sound, privacy, rate, reset with confirmation), Game Rules.
- Sound effects (click, correct, wrong, match, win, lose) with a persisted on/off toggle.

## Offline only

No `INTERNET` permission, no runtime permissions, no services, WorkManager, alarms or notifications.
No networking, ads, analytics, payments, maps, Firebase or attribution/tracking SDKs (AppsFlyer, OneSignal, …).
"Rate" and "Full policy" only fire an `ACTION_VIEW` intent to another app (Play Store / browser); the game itself never connects.

## Local storage & DataStore

`GameRepository` keeps one DataStore Preferences key (`game_data_json`) holding a single JSON string:

```kotlin
@Serializable data class GameData(quizBest: Map<String,Int> = emptyMap(), pairsBest: Map<String,Int> = emptyMap(), settings: Settings = Settings())
@Serializable data class Settings(soundEnabled: Boolean = true)
```

`GameSerializer` (context-free, `ignoreUnknownKeys = true`, `coerceInputValues = true`) never throws: empty storage,
missing key, empty / corrupted / wrongly-typed JSON all decode to defaults. A corrupted DataStore file is replaced
(`ReplaceFileCorruptionHandler`), and read `IOException`s fall back to defaults.

## Privacy

The game collects no personal data and sends nothing anywhere. Scores and the sound setting stay on the device
(`allowBackup="false"`, so they are not copied to cloud backups) and are removed by **Reset Progress** or uninstall.
Settings → Privacy shows this summary in-app. Google Play still requires a hosted policy URL: put it in
`PRIVACY_POLICY_URL` (`ui/screens/MenuScreens.kt`) — while it is blank only the in-app summary is shown.

## Missing integrations (intentionally not included)

Internet access, accounts/login, cloud save, leaderboards, Play Games Services, in-app purchases, ads, analytics,
crash reporting, push notifications, attribution SDKs, WebView content, remote config, Firebase.

## Visual concept — "Royal Jester Carnival"

A velvet-curtained theater stage at night: a troupe of three jesters, a jeweled jester-mask logo,
gold-framed purple plates and a treasury of gems and carnival props.

| Color | HEX | Usage |
|---|---|---|
| Amethyst | `#1C0A36` | App / splash background |
| Night Plum | `#12052A` | Panel gradient bottom, HUD chips, scrim base |
| Plum | `#3E1670` | Panel gradient top, card faces, difficulty cards |
| Ruby | `#D3214F` | Primary accent |
| Topaz (gold) | `#FFC93C` | Titles, borders, highlights, stars |
| Sapphire | `#2747D6` → Royal Deep `#0E1C6B` | Card backs (royal-blue velvet) |
| Emerald | `#127A50` | Correct answer, matched card |
| Garnet | `#C81D3A` | Wrong answer, low time |
| Pearl | `#FFF6E8` | Primary text |
| Lavender | `#D4C2F5` | Secondary text |
| Slate | `#6B6080` | Locked / disabled |

**Unique home layout — "Royal Jester Stage":** not the usual mascot → title → stats card → button stack.
The logo floats inside a slowly turning 3-D carousel of eight jewels (back gems smaller and dimmer), and the
three jesters perform on the background's theater stage — the juggler in the spotlight in the middle, the
other two bobbing at the sides — with the ornate Start plate at the front of the stage.

**Typography:** Material 3 type scale on the system sans-serif (no bundled fonts); white bold labels with a
dark shadow on every plate; gold titles.

### Assets

All art comes from the supplied pack, cropped to its visible bounds, resized and palette-compressed
(≈2 MB total). Everything is wired through the `Assets` object with fixed names in `res/drawable-nodpi`:

| File | Source art | Used for |
|---|---|---|
| `bg_main.jpg` | theater stage | every screen background |
| `logo` | jester-mask logo | home, menu, card backs, splash, themed icon |
| `mascot_a` / `mascot_b` / `mascot_c` | bright / bowing / juggling jester | home stage, jester parade under the quiz answers and on menus/results |
| `btn_menu` | purple game plaque | ornate menu buttons (aspect ratio read from the image) |
| `btn_plate` | same plaque | answers & buttons, drawn as a 3-slice so corners never stretch |
| `panel_frame` | empty purple panel | quiz question card (9-slice) |
| `plate_round` | round purple button | level bubbles (greyscale + padlock when locked) |
| `icon_back`, `icon_pause` | back / pause buttons | top bars |
| `item_01`…`item_12` | ruby, sapphire, emerald, amethyst, star brooch, crown, mask, jester cap, bell, goblet, key, drum | the 12 Matching Pairs icons + home carousel |
| `deco_*` | chest, hourglass, fan, lute, scepter, shoes | results, timer chip, pause, difficulty emblems, rules |

Replace any file with the same name to change the art — no code change. Modal panels (pause / results /
rules) are drawn in Compose (gradient + gold border), never a stretched frame image.

### Layout rules

- **Matching Pairs never scrolls**: card size is computed from the space under the HUD, the column count and
  the row count (card ratio kept between 0.62 and 0.9), so all 4…24 cards always fit the screen.
- **Quiz fits one screen**: framed question card (text auto-shrinks to fit) + 4 answer plates; the jester
  troupe fills whatever empty space remains under the answers and hides itself on short screens.
- Other screens use the same "fill, then scroll only if needed" column (huge font scales still work).

### App icon

`icon.jpg` from the pack. Adaptive icon (`mipmap-anydpi-v26`): blurred icon artwork as the background layer
and the full icon (rounded) inside the safe zone as the foreground, so nothing is cropped by any launcher
mask; the monochrome (themed icon) layer is a white silhouette of the jester-mask logo. Density PNGs
(`mipmap-mdpi` … `xxxhdpi`, square + round) cover API 24–25.

### Splash

API 31+: system splash with `windowSplashScreenBackground = #1C0A36` and the logo inside the safe circle.
API 24–30: a `layer-list` window background (amethyst + 220 dp logo). No full-screen bitmap.

### Accessibility

- Text contrast: Pearl on Amethyst/Plum ≫ 7:1; white on Emerald/Garnet ≥ 5:1; white plate labels have a dark shadow.
- Touch targets ≥ 48 dp (plates ≥ 56 dp, icon buttons 52 dp, answers ≥ 60 dp, cards ≥ 48 dp).
- Content descriptions for mascots, logo, back/pause, each card ("Golden crown, matched", "Hidden card 3"), level bubbles
  ("Level 4, Locked, best 0 of 6 pairs"); decorative images are hidden.
- Colour is never the only signal: answers add ✓/✗ and a spoken state, matched cards add a ✓ badge, locked levels show a
  padlock and the word "Locked".
- Font scaling: all text in `sp`, plates grow with their label, screens scroll; the overlay hides the game content
  from TalkBack while paused.

## Tech stack

Kotlin 2.0.21 · Jetpack Compose (BOM 2024.12.01) · Material 3 · Navigation Compose 2.8.5 (type-safe `@Serializable`
routes) · Lifecycle/ViewModel 2.8.7 · Coroutines 1.9.0 · Flow · DataStore Preferences 1.1.1 · Kotlinx Serialization 1.7.3 ·
AGP 8.7.3 · Gradle 8.9 · Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`) · JDK 17.
Every imported package is a direct catalog dependency. No DI framework, no Room, no Java logic.

## Architecture

Simple MVVM:

```
JovialJewelsApp (Application) ── creates GameRepository (DataStore) + SoundManager (SoundPool)
        │                          and syncs settings.soundEnabled → SoundManager (startup + live)
MainActivity ── edge-to-edge ── AppNavHost (Navigation Compose, typed routes)
        │
   Screens (collectAsStateWithLifecycle) ◄── StateFlow<immutable UI state> ── ViewModels
                                                ProgressViewModel / QuizViewModel / PairsViewModel
                                                (built by SimpleFactory, no DI)
   logic/GameLogic   — pure rules: level table, deck building, unlock, completed count
   logic/QuizContent — replaceable questions
   data/GameSerializer — pure JSON encode/decode
```

Navigation safety: every route argument has a default; `safeRoute()` never throws; invalid category/level/score,
locked level, reset progress or a restored process show a friendly fallback with **Back**. `safeBack()` ignores
double taps during transitions and never pops the last entry. Back closes the pause overlay first.

## Open in Android Studio

1. Android Studio Ladybug (2024.2) or newer → **Open** the project folder.
2. Gradle JDK: *Settings → Build Tools → Gradle → Gradle JDK = 17*.
3. Install **SDK Platform 36** (and Build Tools 36.0.0) in SDK Manager.
4. The Gradle wrapper JAR is not committed; Android Studio uses `gradle/wrapper/gradle-wrapper.properties` (8.9).
   For a command-line wrapper run once: `gradle wrapper --gradle-version 8.9`.

### API 36

`compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`. AGP 8.7.x predates official API 36 support, so
`android.suppressUnsupportedCompileSdk=36` is set in `gradle.properties`.

### 16 KB memory page size

Pure Kotlin/Compose with no native libraries, so the app is 16 KB page-size compatible. Still verify the AAB:

```bash
unzip -l app/build/outputs/bundle/release/app-release.aab | grep '\.so$' || echo "no native libs"
```

(CI runs the same check.) Android Studio's APK Analyzer also flags 16 KB alignment.

## Builds

```bash
# Debug (debug-signed, for development only)
./gradlew assembleDebug
./gradlew testDebugUnitTest

# Non-minified signed release (requires the signing env vars below)
./gradlew assembleRelease bundleRelease
```

Release builds **fail with a clear message** if any signing variable is missing or the keystore file does not exist —
they are never signed with the debug key.

### PKCS12 keystore generation

```bash
keytool -genkeypair -v \
  -storetype PKCS12 \
  -keystore jovialjewels-release.p12 \
  -alias jovialjewels \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=Jovial Jewels, O=Your Company, C=BY"
```

Keep the `.p12` and passwords outside the repository (`.gitignore` already excludes `*.p12`, `*.jks`, `*.keystore`).
For PKCS12 the key password is the same as the store password. Back the keystore up — losing it means you can't update
the app (unless Play App Signing holds the app key; then this is your upload key).

### Local signing

```bash
export ANDROID_KEYSTORE_PATH="$HOME/keys/jovialjewels-release.p12"
export ANDROID_KEYSTORE_PASSWORD='…'
export ANDROID_KEY_ALIAS='jovialjewels'
export ANDROID_KEY_PASSWORD='…'
./gradlew assembleRelease bundleRelease
```

### GitHub Secrets

Repository → Settings → Secrets and variables → Actions → *New repository secret*:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 jovialjewels-release.p12` (macOS: `base64 -i jovialjewels-release.p12`) |
| `ANDROID_KEYSTORE_PASSWORD` | store password |
| `ANDROID_KEY_ALIAS` | `jovialjewels` |
| `ANDROID_KEY_PASSWORD` | key password (= store password for PKCS12) |

### GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and on manual dispatch:
JDK 17 → Android SDK Platform 36 + Build Tools 36.0.0 → Gradle 8.9 via `gradle/actions/setup-gradle` (no committed wrapper
needed) → decode keystore → unit tests → `assembleRelease bundleRelease` → `apksigner verify --print-certs`
(fails on verification error or `CN=Android Debug`) → AAB `jarsigner` check → native-lib check → upload APK and AAB.
Secrets are never echoed; the decoded keystore is deleted at the end. There is no emulator smoke test.

**A green CI build is not proof the app launches.** Always verify on a device (below).

### apksigner

```bash
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
```

Expect `Verified using v2 scheme … true` and your own DN — never `CN=Android Debug`.

### APK vs AAB

- **APK** (`jovial-jewels-release-apk`) — for sideload testing only (`adb install`).
- **AAB** (`jovial-jewels-release-aab`) — the **only** artifact uploaded to Google Play.

### Staged R8

1. First release ships with `isMinifyEnabled = false` and `isShrinkResources = false`.
2. After that build is verified on devices, set both to `true` in `app/build.gradle.kts`.
   `proguard-rules.pro` already keeps Kotlinx Serialization serializers and the typed navigation routes.
3. Rebuild and repeat the full local verification checklist — R8 issues show up only at runtime.

### Local release verification

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat -c && adb logcat '*:E' AndroidRuntime:E | grep -i jovialjewels
```

Checklist:
- [ ] Launches with the amethyst splash and custom icon (also check themed/monochrome icon).
- [ ] Main → Start → Menu; every button opens its screen; Back works everywhere, Back on Main exits.
- [ ] Quiz: green/red + ✓/✗ highlight, auto-advance, pause stops advancing, results show `Score: X/10`, best saved.
- [ ] Pairs: flip/match/mismatch, timer pauses, Well Done / Out of Time, next level unlocks, best saved.
- [ ] Results summary numbers match; Settings sound toggle mutes immediately and persists after restart.
- [ ] Reset Progress (confirm) re-locks levels 2–9 and clears bests.
- [ ] Rotate / "Don't keep activities" / kill process in background → no crash, friendly fallback where needed.
- [ ] Airplane mode: everything works; no network in Logcat.
- [ ] TalkBack + 200 % font size are usable.
- [ ] Repeat after enabling R8.

## Data reset

In-app: Settings → Reset Progress → Reset (keeps the sound preference).
Full wipe: `adb shell pm clear com.jovialjewels.game` or system App info → Storage → Clear data.

## Known limitations

- Portrait is locked in the manifest; on Android 16+ large screens (≥ 600 dp) the system may ignore the orientation
  lock, so the UI simply scrolls/letterboxes there.
- A quiz or pairs run in progress restarts if the OS kills the process (progress already saved is kept).
- Sound effects are simple synthesized tones (`res/raw/sfx_*.wav`) — drop in real SFX with the same names.
- English only; strings are in code, not yet in `strings.xml` for localisation.
- `PRIVACY_POLICY_URL` must be set to your hosted policy before publishing.
- No Gradle wrapper JAR is committed (CI provisions Gradle 8.9 directly).
