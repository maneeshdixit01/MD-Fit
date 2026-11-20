#!/usr/bin/env python3
from pathlib import Path
import shutil, re

root = Path(__file__).resolve().parents[1]
android = root / "android"
src = root / "native/android/app/src/main/java/com/mdfit/activitytracker"
dst = android / "app/src/main/java/com/mdfit/activitytracker"
dst.mkdir(parents=True, exist_ok=True)

for name in ("BackgroundLocationPlugin.java", "ActivityLocationService.java"):
    shutil.copy2(src / name, dst / name)
shutil.copy2(root / "native/android/MainActivity.java", dst / "MainActivity.java")

manifest = android / "app/src/main/AndroidManifest.xml"
s = manifest.read_text(encoding="utf-8")
permissions = [
    "android.permission.INTERNET",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_LOCATION",
    "android.permission.POST_NOTIFICATIONS",
]
for name in permissions:
    if f'android:name="{name}"' not in s:
        line = f'    <uses-permission android:name="{name}" />'
        s = re.sub(r'(<application\b)', lambda m: line + "\n    " + m.group(1), s, count=1)

service = """
        <service
            android:name="com.mdfit.activitytracker.ActivityLocationService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="location" />
"""
if "ActivityLocationService" not in s:
    s = s.replace("</application>", service + "\n    </application>")
manifest.write_text(s, encoding="utf-8")
print("MD Fit native foreground GPS service installed.")
