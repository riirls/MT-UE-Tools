# MT-UE-Tools

This repository contains the initial skeleton for MT Manager plugin targeting Unreal Engine resource analysis.

How to build and package as .mtp

1. Open the project in Android Studio.
2. Build a signed or debug APK (Build -> Build Bundle(s) / APK(s) -> Build APK(s)).
3. The generated APK (app-release.apk or app-debug.apk) can be renamed to *.mtp and copied to your Android device.
4. Open MT Manager on your device and install/load the plugin (depends on MT Manager version).

Usage

- From MT Manager, select a file and choose to open with plugin. The plugin will display a dialog with file name, path, size and header bytes.

Notes

- This is a minimal skeleton. Future commits will add Pak / UAsset / AES / IoStore support and more features.
