# Matrix Creator Android MALOS Demo
Android application that interfaces with MATRIX Creator MALOS layer. <a href="https://github.com/matrix-io/matrix-creator-malos-android/blob/master/screenshot.jpg"><img src="https://github.com/matrix-io/matrix-creator-malos-android/blob/master/screenshot.jpg" align="right" height="426" width="240" ></a>

**WARNING**: Actual version only works with MALOS developer version, please see [prerequisites](#preriquisities) section.

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
- [ ] MALOS WakeWord config via Android (in development)

## Preriquisities

* Please install MALOS service package on your RaspberryPi and reboot it:

``` bash
curl https://apt.matrix.one/doc/apt-key.gpg | sudo apt-key add -
echo "deb https://apt.matrix.one/raspbian $(lsb_release -sc) main" | sudo tee /etc/apt/sources.list.d/matrixlabs.list
sudo apt update
sudo apt upgrade
sudo apt install matrixio-malos
reboot
```

**NOTE**: 
* For more details: [Getting Started Guide](https://matrix-io.github.io/matrix-documentation/matrix-core/)
* Your creator on the same network
* Android 4.x or later

## Download
Pre-releases for testing, please download [here](https://github.com/matrix-io/matrix-creator-malos-android/releases).

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
(or open your project on AndroidStudio and config crashlytics fabric plugin or remove this dependency on gradle app file)

#### Building and install

```
./gradlew assembleDebug
./gradlew installDebug
```
(or with AndroidStudio IDE)

