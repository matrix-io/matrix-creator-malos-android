# matrix-creator-malos-android
Android application that interfaces with MATRIX Creator MALOS layer.

## Prerequisities 
create file matrix-malos-demo/app/fabric.properties with:
```
apiSecret=<YOUR FABRIC SECRET>
apiKey=<YOUR FABRIC API KEY>

```
(or open your project on android studio and config crashlytics fabric plugin.

## Build and install
```
git clone --recursive https://github.com/matrix-io/matrix-creator-malos-android.git
cd matrix-creator-malos-android
./gradlew installDebug
```

