# Fuelnivo

**Record refills, review fuel usage, and keep a clear local history for your vehicle.**

Fuelnivo is a native Android manual fuel-log tracker for personal vehicles, built with Kotlin and Jetpack Compose. It is a private, offline journal: you add vehicles, record refills by hand, and the app calculates consumption, cost, and monthly summaries from the data you enter.

## What Fuelnivo is

- A manual fuel log
- A refill history journal
- A personal vehicle mileage tracker
- An offline fuel-consumption calculator
- A monthly fuel-summary tool

## What Fuelnivo is not

Fuelnivo is **not** an OBD app, vehicle diagnostics tool, GPS/route tracker, navigation app, fuel-price comparison service, fuel-station finder, financial-planning/tax tool, fleet platform, cloud service, automatic mileage tracker, or car-manufacturer app.

## Manual tracking disclaimer

> Fuelnivo is a manual fuel log. Vehicle details, odometer values, fuel quantities, prices, costs, and notes are entered by the user. The app does not connect to a vehicle, OBD system, GPS service, fuel station, or financial service.

Fuelnivo does **not** connect to your vehicle, read fuel level or the odometer automatically, detect refills, provide mechanical diagnostics, guarantee reduced spending, give financial advice, predict fuel prices, or replace your dashboard or service inspection.

## Main features

- Add and manage multiple vehicles (name, manufacturer, model, year, fuel type, tank capacity, units, initial odometer, note).
- Record fuel refills manually: date, time, odometer, fuel amount, total cost, price per unit, full-tank and missed-refill flags, and a short note.
- Fuel quantity in **liters** or **US gallons**; distance in **kilometers** or **miles** (one unit set per vehicle).
- Average fuel consumption using the **full-tank method** (`L/100 km` or `MPG`).
- Refill history with filters and sorting.
- Monthly fuel statistics and a lifetime statistics screen.
- Distance between refills and estimated cost per distance unit.
- A large, illustrated **Segmented Fuel Tank Dashboard** on Home.
- A fully **in-app** refill reminder (no push notifications, no background work).
- All data stored **locally** on the device.

## Offline-only architecture

Fuelnivo works fully offline. It declares **no `INTERNET` permission** and makes no network calls. There is:

- **No OBD integration**, **no Bluetooth integration**, **no automatic vehicle connection**.
- **No GPS**, **no location tracking**, **no maps**.
- **No account**, **no login**, **no backend**, **no cloud sync**, **no Firebase**.
- **No ads**, **no analytics**, **no payments**, **no financial advice**, **no external APIs**.
- **No runtime permissions** requested at all.

## Privacy note

> Fuelnivo stores vehicles, refill records, odometer values, fuel quantities, costs, notes, preferences, and reminder settings locally on this device. The app has no account, no cloud sync, no internet access, no ads, no analytics, no payments, no location tracking, no vehicle connection, and no OBD integration.

## Local storage

All data is stored with **DataStore Preferences** as serialized JSON strings under three keys: `vehicles_json`, `refills_json`, and `settings_json`. The single repository (`FuelnivoRepository`) loads, deserializes, merges defaults, saves, and recovers from corrupted or missing JSON without crashing. Numeric values are stored as numbers, never as formatted text. No database (Room/SQLDelight/Realm) is used, and there is no cloud-backup code.

## Vehicle management

Create, edit, switch, and delete vehicles. Vehicle name is required and tank capacity must be greater than zero; other fields are optional. Deleting a vehicle asks for confirmation and also removes that vehicle's refills. If the active vehicle is deleted, another is selected automatically, or the empty setup state is shown. Duplicate vehicle names are allowed; IDs are generated locally.

## Refill recording

Required fields are date, odometer, and fuel amount. Optional fields include time, total cost, price per unit, full-tank toggle, missed-previous-refill toggle, and a note (up to 300 characters, with a live remaining-character count). Entering fuel amount together with either total cost or price per unit fills in the missing value; a value you edited manually is never silently overwritten. If a new odometer is lower than a previous record, Fuelnivo shows a warning and requires explicit confirmation — it never modifies the value you entered. Comma decimal separators are normalized safely.

## Full-tank calculation method

Consumption is calculated only between two **full-tank** refills:

- A refill is a valid calculation endpoint when it is marked full tank, a previous valid full-tank refill exists, and no refill in the interval is marked as missed.
- Distance uses the two full-tank odometer values.

### Partial refill behavior

If partial refills exist between two full-tank records, their fuel quantities are added to the ending full-tank refill's fuel amount, and the distance between the two full-tank odometers is used.

### Missed refill behavior

If "previous refill was missed" is enabled for any refill in an interval, that interval is excluded from consumption calculations and shown as "Not calculated" / a clear reason. Fuelnivo never shows zero as a valid consumption result when data is missing — it shows "More full-tank records are needed."

## Units

- Fuel: Liters (`L`) and US Gallons (`gal`). `1 US gallon = 3.785411784 liters`.
- Distance: Kilometers (`km`) and Miles (`mi`). `1 mile = 1.609344 kilometers`.

Each vehicle uses one fuel unit and one distance unit. Changing a unit after records exist prompts for confirmation and safely converts all stored fuel/odometer/price values (and tank capacity) — old values are never reinterpreted without conversion.

## Fuel consumption formulas

- **Kilometers + liters:** `L/100 km = fuelAmount / distance × 100`
- **Miles + US gallons:** `MPG = distance / fuelAmount`

Display precision: fuel 2 dp, cost 2 dp, price per unit 3 dp, consumption 1–2 dp, distance 0–1 dp.

## Cost calculation

Fuelnivo computes total refill cost, price per unit, monthly fuel cost, average cost per refill, cost per 100 km / per mile, and lifetime recorded cost — only when the required data exists. It uses neutral labels ("Recorded cost", "Average refill cost", "Cost per 100 km", "Cost per mile") and never gives financial recommendations. Currency is a user-defined code (default `USD`), used purely as a label; there are no online exchange rates.

## Monthly statistics

A dedicated monthly screen shows total fuel, total cost, refill count, total distance, average refill amount and cost, valid average consumption, highest refill, and longest interval, plus month navigation (previous/next/return to current). Fuel amounts are visualized with Compose-built columns (no chart library). Empty months show "No refill data for this month." The statistics screen also includes a neutral current-vs-previous month comparison ("Higher than previous month", "Lower than previous month", "No change").

## In-app refill reminder

The reminder is evaluated on the client only — when the app opens, when Home becomes active, when the active vehicle changes, and when a refill is added or edited. It compares the latest refill date for the active vehicle to the current local date and shows a panel when the configured interval (7 / 14 / 21 or a custom 1–90 days) has passed. If no refill exists, a gentle first-record prompt is shown. Dismissing hides it for the current session; saving a refill resets it.

**Reminders are not push notifications.** Fuelnivo uses no `POST_NOTIFICATIONS` permission, no `AlarmManager`/exact alarms, no `WorkManager`, and no background services. Reminders appear only inside the app.

## Visual concept

**Segmented Fuel Tank Dashboard** — the Home screen is built around a large Compose-drawn fuel tank with an inlet cap, eight horizontal measurement segments, an amber fill level, a percentage label, and a small fuel drop. The fill is based on the latest manually recorded refill amount relative to the vehicle's configured capacity, clamped to 0–100%, with the label "Based on your latest manual refill." This is a manual progress representation, **not** a live fuel gauge.

### Layout uniqueness

Home uses its own standalone layout: compact vehicle selector, large asymmetrical tank with attached latest-refill info, a narrow mileage strip, a horizontal monthly summary rail, a compact recent-refill timeline, an integrated "Add Refill" control, a reminder panel only when applicable, and bottom navigation (Home, History, Statistics, Vehicles, Settings). It deliberately avoids the generic "mascot → title → stats card → button stack" pattern and mixes tank illustration, segmented bars, timeline rows, strips, and outlined controls.

### App icon concept

A custom adaptive icon: dark graphite rounded-square background, an amber segmented tank with simplified measurement divisions, and one small white fuel drop. No text, no manufacturer or fuel-station logos, no currency or OBD symbols. A non-adaptive vector fallback is provided for API 24–25.

### Splash screen concept

A stable static splash using the AndroidX core-splashscreen library: a deep-graphite background with a centered segmented fuel-tank vector and the system splash icon treatment. No heavy image assets or animations.

## Technology stack

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Android ViewModel, Kotlin Coroutines, Kotlin Flow, DataStore Preferences, Kotlinx Serialization, Gradle Kotlin DSL with a version catalog.

## Project architecture

Simple MVVM with a single local repository and a single shared `MainViewModel` exposing immutable `AppUiState` via `StateFlow`. No dependency-injection framework, no unnecessary domain layer.

```
app/src/main/java/com/fuelnivo/app
├─ data/            Models (Vehicle, RefillRecord, settings) + FuelnivoRepository (DataStore)
├─ util/            Conversions, DateUtils, NumberInput, Formatting, FuelCalculations,
│                   Statistics, Reminder, Validation, Ids, DisclaimerText
├─ ui/theme/        Colors, typography, Material 3 theme
├─ ui/navigation/   Routes, bottom destinations
├─ ui/components/   FuelTankView + shared/form components
├─ ui/screens/      onboarding, home, refill, history, statistics, monthly, vehicles, settings
├─ MainViewModel.kt / FuelnivoApp.kt
├─ MainActivity.kt / FuelnivoApplication.kt
```

## Data reset behavior

Settings offers three destructive actions, each behind an explicit confirmation dialog: delete refill records for the active vehicle, delete all vehicles and refills, and reset all local data (clears DataStore entirely).

## Known calculation limitations (manual input)

Because every value is entered by hand, results depend on data quality. Consumption requires at least two valid full-tank refills; out-of-order odometers, skipped refills, or partial-only histories produce "More full-tank records are needed" rather than misleading numbers. Distances are computed from odometer differences, so a mistyped odometer affects that interval until corrected.

---

# Build, signing, CI, and release

## Requirements

- **JDK 17**
- Android SDK with **API 35** platform and **Build Tools 35.0.0**
- `compileSdk = 35`, `targetSdk = 35`, `minSdk = 24`
- A stable Android Gradle Plugin compatible with API 35 (AGP 8.7.x) and Gradle 8.11.1

### 16 KB memory page-size compatibility

Fuelnivo is pure Kotlin/Compose with no third-party native binaries, so the release AAB is compatible with Android 15+ 16 KB memory page sizes. Still verify the final bundle for native-library compatibility before publishing.

## Open in Android Studio

1. Open the project root in a recent Android Studio (Ladybug or newer).
2. Android Studio regenerates the Gradle wrapper automatically. If you build from the command line first and `gradle/wrapper/gradle-wrapper.jar` is missing, run once with an installed Gradle 8.9+: `gradle wrapper --gradle-version 8.11.1`.
3. Let Gradle sync, then run the `app` configuration.

## Build a debug APK

```
./gradlew assembleDebug
```

## Build a non-minified release (do this first)

The release build type ships with `isMinifyEnabled = false` and `isShrinkResources = false`. Build, install, and test this first:

```
./gradlew assembleRelease
```

## Generate a PKCS12 keystore

```
keytool -genkeypair -v -storetype PKCS12 \
  -keystore fuelnivo-release-key.p12 \
  -alias fuelnivo_key \
  -keyalg RSA -keysize 2048 -validity 10000
```

Never commit the `.p12`, passwords, alias, decoded keystores, or local signing property files (all are covered by `.gitignore`).

## Configure local release signing

Create a **git-ignored** `keystore.properties` in the project root:

```
storeFile=/absolute/path/to/fuelnivo-release-key.p12
storePassword=your_store_password
keyAlias=fuelnivo_key
keyPassword=your_key_password
```

Alternatively, provide the same values as the environment variables `ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. If none are provided, the build **fails clearly** for release tasks instead of silently signing with the debug key. Use the same password for the keystore and key.

## Required GitHub Secrets

- `ANDROID_KEYSTORE_BASE64` — base64 of the `.p12` file
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Create the base64 with: `base64 -w0 fuelnivo-release-key.p12 > keystore.b64` (macOS: `base64 -i fuelnivo-release-key.p12 -o keystore.b64`).

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and via manual dispatch. It checks out the repo, sets up JDK 17 and the Android SDK, installs Platform 35 and Build Tools 35.0.0, restores Gradle caches, decodes `ANDROID_KEYSTORE_BASE64` into a temporary PKCS12 file on the disposable runner, provides the signing secrets as environment variables, runs unit tests, and builds the **signed release APK and AAB**. It never prints passwords or secret contents, and does not run an emulator smoke test.

### apksigner verification

The workflow locates the release APK and runs:

```
apksigner verify --print-certs app-release.apk
```

It prints the certificate to the logs and **fails** if the output contains `CN=Android Debug` or if signature verification fails. The APK is uploaded as a test artifact and the AAB as the Google Play artifact.

> CI compilation and signing prove the build is valid; they do **not** prove the app launches. Always perform local launch verification too.

## Signed APK and AAB / Google Play

- The **APK** is for local installation and verification.
- The **AAB** is for Google Play. **Only the `.aab` is uploaded to Google Play** as the production artifact; do not upload the APK as the production artifact.

## R8 / ProGuard staged enablement

1. Build, install, and test the release with `isMinifyEnabled = false` and `isShrinkResources = false`.
2. Once verified, set **both** to `true` in `app/build.gradle.kts` (the block uses `proguard-android-optimize.txt` + `proguard-rules.pro`, which already keeps kotlinx.serialization and the data models).
3. Rebuild and reinstall the minified release, then re-verify serialization and DataStore behavior.

## Local release verification

```
./gradlew assembleRelease
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat
```

The signing certificate must **not** contain `CN=Android Debug`. Repeat the whole flow after enabling R8 and resource shrinking.

## Local launch checklist

Test: first launch with empty storage; onboarding and skip; create one and multiple vehicles; switch/edit/delete vehicles (including the active one); refills in liters, gallons, km, miles; partial and full-tank refills; several partials between full tanks; missed-refill exclusion; entering total cost and price per unit and the auto-fill helper; notes; edit/delete refill; refill detail; history empty and with many records; history filters and sort; statistics with insufficient and valid data; monthly statistics empty and with data; month navigation; trigger and dismiss the reminder; add a refill and confirm the reminder resets; change interval; disable reminder; change currency; change fuel and distance units with conversion; reset active-vehicle records; reset all data; relaunch; launch in airplane mode; confirm no `INTERNET` permission and no runtime permission dialogs; confirm no OBD/Bluetooth behavior; inspect `adb logcat` for crashes; verify the release certificate; verify the AAB is produced; verify API 35 configuration and 16 KB page-size compatibility.

Watch `logcat` for `ClassNotFoundException`, `NoSuchMethodError`, serialization exceptions, DataStore corruption crashes, `NumberFormatException`, date-parsing crashes, navigation-argument crashes, division by zero, null active-vehicle errors, missing-refill errors, R8 errors, and signing misconfiguration.

## Tests

Unit tests cover unit conversions, `L/100 km` and `MPG`, division-by-zero protection, partial-refill aggregation, full-tank intervals, missed-refill exclusion, monthly fuel/cost totals, cost per 100 km, reminder due calculation, invalid date handling, active-vehicle fallback, and JSON default/corruption handling.

```
./gradlew testDebugUnitTest
```
