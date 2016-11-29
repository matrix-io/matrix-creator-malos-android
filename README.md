# Matrix Creator Android MALOS Demo
Android application that interfaces with MATRIX Creator MALOS layer. <a href="https://github.com/matrix-io/matrix-creator-malos-android/blob/master/screenshot.jpg"><img src="https://github.com/matrix-io/matrix-creator-malos-android/blob/master/screenshot.jpg" align="right" height="426" width="240" ></a>

## Current Features

* Everloop RGB color control
* Humidity Sensor
* Temperature Sensor
* UV index radation
* IMU (x,y) widget visualitation
* GPIO input/output configuration (pin 0,1)
* GPIO updates callbacks
* Auto discovery Matrix Creator on LAN network
* Manual IP Matrix device target

<a href="http://www.youtube.com/watch?feature=player_embedded&v=ihV_v7zFO7A" target="_blank"><img src="http://img.youtube.com/vi/ihV_v7zFO7A/0.jpg" 
alt="Matrix Creator Android-MALOS demo" width="234" height="180" border="5" /></a>

## TODO
- [X] GPIO callback
- [X] Pressure
- [ ] Mic Array visualization (beamforming localization)
- [ ] ZigbeeBulb basic control
- [ ] LIRC custom control config
- [ ] RaspberryPi Wifi config via BT4

## Preriquisities
* Only install lastest MALOS deb package on your RaspberryPi:
```
echo "deb http://packages.matrix.one/matrix-creator/ ./" | sudo tee --append /etc/apt/sources.list
sudo apt-get update
sudo apt-get upgrade
sudo apt-get install -y libzmq3-dev xc3sprog malos-eye matrix-creator-malos matrix-creator-openocd wiringpi matrix-creator-init cmake g++ git --force-yes
```
For more info: [MALOS](https://github.com/matrix-io/matrix-creator-quickstart/wiki/2.-Getting-Started)

* Your creator on the same network
* Android 4.x or later

## Download
Pre-release for testing: [rev351](https://github.com/matrix-io/matrix-creator-malos-android/releases)(Devel branch)

## Preriquisities and dependencies for Build

#### Clone repository and submodules
```
git clone --recursive https://github.com/matrix-io/matrix-creator-malos-android.git
cd matrix-creator-malos-android
```

#### Fabric configuration
create file matrix-malos-demo/app/fabric.properties with:
```
apiSecret=<YOUR FABRIC SECRET>
apiKey=<YOUR FABRIC API KEY>
```
(or open your project on android studio and config crashlytics fabric plugin.

#### Building and install
```
./gradlew assembleDebug
./gradlew installDebug
```
(or with AndroidStudio IDE)

