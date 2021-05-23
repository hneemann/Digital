#!/usr/bin/env bash
DIR="$( cd "$( dirname  "${BASH_SOURCE[0]}"  )" >/dev/null && pwd )"
JAR=$DIR/Digital.jar
ICON=$DIR/macosx/Digital.icns

# create App in same directory, even if run from FInder
cd $DIR

# use jar2app to package Mac application
git clone https://github.com/Jorl17/jar2app.git
jar2app/jar2app -i $ICON $JAR
rm -rf jar2app
