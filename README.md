# MD Fit — Android project with native background GPS

## Included
- MD Fit V6 HTML app (Sunset Orange & White UI + Night Run mode + EN/Hindi toggle; Live GPS Cockpit, Smart BMI & BMR/TDEE Gauge, Daily Water Tracker, Achievements, Shareable PNG Workout Cards, Manual Workout Log, Voice Coach)
- Capacitor 6 Android wrapper
- Native Java `ActivityLocationService` foreground location service
- Capacitor bridge `BackgroundLocationPlugin`
- Persistent notification while recording
- GPS points saved natively while the WebView is backgrounded
- GitHub Actions debug APK workflow

## GitHub build
1. Extract this ZIP.
2. Create a GitHub repository named `MD-Fit`.
3. Upload all contents to the repository root. Include `.github`, `native`, and `scripts`.
4. Commit to `main`.
5. Open Actions and wait for `Build MD Fit Android APK`.
6. Download `MD-Fit-debug-APK` and install `app-debug.apk`.

## Background GPS behavior
- Tap Start Activity while MD Fit is visible.
- Grant location permission; allow notifications when prompted.
- Android starts the native foreground service and shows an ongoing notification.
- The service stores GPS points independently of the WebView.
- When the app is reopened, the HTML bridge reads the saved points and updates route/distance.
- Pause stops location updates but preserves the session and elapsed time; Resume continues the same route. The app attempts to restore an active or paused session after the WebView reloads. Finish saves the activity and ends the session.

## Android requirements and limitations
- Android 14+ requires `foregroundServiceType="location"`, `FOREGROUND_SERVICE_LOCATION`, and location permission. The build script adds these declarations.
- Start tracking while the app is visible. Android restricts starting a location foreground service from the background. This app does not request `ACCESS_BACKGROUND_LOCATION`; the service is started by the visible Start button and can continue while the app is backgrounded.
- The persistent notification is required.
- Battery saver, OEM restrictions, disabled GPS, and weak GPS signal can affect tracking. Test outdoors on a real phone.
- This is a debug APK, not a Play Store signed AAB.
- Data is stored locally; no account/cloud sync.
