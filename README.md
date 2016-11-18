# Matrix Creator Android MALOS Demo
Android application that interfaces with MATRIX Creator MALOS layer. <a href="url"><img src="https://github.com/matrix-io/matrix-creator-malos-android/blob/master/screenshot.jpg" align="right" height="426" width="240" ></a>

## Current Features

* Everloop RGB color control
* Humidity Sensor
* Temperature Sensor
* UV index radation
* IMU (x,y) widget visualitation
* GPIO input/output configuration (pin 0,1)
* Auto discovery Matrix Creator on LAN network
* Manual IP Matrix device target

## TODO
- [ ] Pressure
- [ ] ZigbeeBulb basic control
- [ ] Mic Array visualization
- [ ] LIRC custom control config
- [ ] RaspberryPi Wifi config via BT4


## Preriquisities and dependencies
```
git clone --recursive https://github.com/matrix-io/matrix-creator-malos-android.git
cd matrix-creator-malos-android
```
create file matrix-malos-demo/app/fabric.properties with:
```
apiSecret=<YOUR FABRIC SECRET>
apiKey=<YOUR FABRIC API KEY>
```
(or open your project on android studio and config crashlytics fabric plugin.

### Building and install
```
./gradlew assembleDebug
./gradlew installDebug
```

