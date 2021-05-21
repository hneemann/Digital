#!/bin/bash
DIR="$( cd "$( dirname "$( realpath "${BASH_SOURCE[0]}" )" )" >/dev/null && pwd )"
java -jar "$DIR/Digital.jar" "$1"
