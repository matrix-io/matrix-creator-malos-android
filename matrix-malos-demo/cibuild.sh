#!/bin/bash

#fake fabrik keys
echo "apiSecret=3da2xx"  > app/fabric.properties
echo "apiKey=48be76xxx" >> app/fabric.properties

./gradlew assembleDebug
