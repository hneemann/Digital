#!/bin/bash
DIR="$( cd "$( dirname "$( realpath "${BASH_SOURCE[0]}" )" )" >/dev/null && pwd )"
export JAVA_TOOL_OPTIONS='-Djava.awt.headless=false'
java -jar "$DIR/Digital.jar" "$1"
