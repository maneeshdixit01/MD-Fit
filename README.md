# MD Fit — Android App (Capacitor + GitHub Actions)

## Folder Structure
- `www/index.html` — Main app UI & logic
- `package.json` — Capacitor 6 dependencies
- `capacitor.config.json` — App ID (`com.md.fit`) aur App Name (`MD Fit`)
- `.github/workflows/build-android.yml` — Automated APK & AAB build workflow

## GitHub par Upload aur Build karne ka tarika
1. Apne GitHub account par ek naya repository banayein (e.g. `md-fit`).
2. Is ZIP ko extract karein aur saari files (`.github` folder samet) repository mein upload/push karein.
3. Push karte hi GitHub ke **Actions** tab mein build apne aap start ho jayega.
4. Build complete hone par **Artifacts** section se `MD-Fit-Debug-APK` aur `MD-Fit-Release-AAB` download kar lein.

## Play Store ke liye Signed AAB & Release APK (Optional)
Repository ke **Settings -> Secrets and variables -> Actions** mein ye 4 secrets add karein:
- `KEYSTORE_BASE64` (aapki `.jks` file ka Base64 text)
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Secrets add karne ke baad **Actions** tab se workflow dubara run karein — aapko Signed Release APK aur Play Store-ready Signed AAB mil jayega.
