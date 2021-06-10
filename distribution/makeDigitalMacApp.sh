#!/usr/bin/env bash
DIR="$( cd "$( dirname  "${BASH_SOURCE[0]}"  )" >/dev/null && pwd )"
JAR=$DIR/Digital.jar
ICON=$DIR/macosx/Digital.icns

# create App in same directory, even if run from Finder
cd $DIR

# check to see if Digital.app has already been created
if [ -d "Digital.app" ]
then
    echo "Updating .jar file in Digital Mac application"
    cp $JAR Digital.app/Contents/Java/Digital.jar
else
    echo "Using jar2app to package .jar file into Mac application"
    curl -LJO https://github.com/Jorl17/jar2app/archive/refs/tags/stable.tar.gz
    tar zxf jar2app-stable.tar.gz
    jar2app-stable/jar2app -i $ICON $JAR
    rm -rf jar2app-stable*
fi

# NOTE:
# Digital is a Java program and requires that the Java Runtime is installed separately
