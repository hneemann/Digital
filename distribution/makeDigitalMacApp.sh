#!/usr/bin/env bash
DIR="$( cd "$( dirname  "${BASH_SOURCE[0]}"  )" >/dev/null && pwd )"
JAR=$DIR/Digital.jar
ICON=$DIR/macosx/Digital.icns

# create App in same directory, even if run from FInder
cd $DIR

# check to see if Digital.app has already been created
if [ -d "Digital.app" ]
then
    echo "Updating .jar file in Digital Mac application"
    cp $JAR Digital.app/Contents/Java/Digital.jar
else
    echo "Using jar2app to package .jar file into Mac application"
    git clone https://github.com/Jorl17/jar2app.git
    jar2app/jar2app -i $ICON $JAR
    rm -rf jar2app
fi
