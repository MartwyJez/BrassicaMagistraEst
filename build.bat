$env:ANDROID_SDK_ROOT="C:\Users\MartwyJez\heartapp\cmdline-tools"
cd C:\Users\MartwyJez\heartapp\
rm .\my-aligned.apk
.\gradlew clean
.\gradlew build
.\cmdline-tools\build-tools\31.0.0\zipalign.exe -p 4 "C:\Users\MartwyJez\heartapp\app\build\outputs\apk\release\app-release-unsigned.apk" my-aligned.apk
.\cmdline-tools\build-tools\31.0.0\apksigner sign --ks-key-alias app --ks my.keystore .\my-aligned.apk
.\cmdline-tools\platform-tools-2\adb.exe install .\my-aligned.apk  