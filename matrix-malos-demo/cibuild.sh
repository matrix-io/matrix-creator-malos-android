#!/bin/bash
##############################################
# CI testing build script for github workflows
##############################################

# adding FAKE! fabric keys to project:
echo "apiSecret=3da2e6ea00f6074d080bdda7c11d6df97c00b8b01cdcb3ef765b7be100e8ebf0"  >> app/fabric.properties
echo "apiKey=48be76c412597fd97e8cdd86ec60135e9d46f5a2" >> app/fabric.properties

# building debug version for CI 
./gradlew assembleDebug
