#!/bin/bash
DIR="$( cd "$( dirname "$( realpath "${BASH_SOURCE[0]}" )" )" >/dev/null && pwd )"
export JAVA_TOOL_OPTIONS='-Djava.awt.headless=false'

# The X11 backend of the JVM waits for the window manager to reparent the main
# window. Non reparenting window managers (sway, i3, dwm, awesome, ...) never
# do that, so AWT keeps waiting and Digital only shows an empty window. The JDK
# only knows a fixed list of such window managers, so all others have to be
# handled with this variable, which makes AWT skip that wait. It is only read
# by the X11 backend, hence the check for a display. Set
# DIGITAL_NO_AWT_WORKAROUND to skip it as well.
if [ -n "$DISPLAY" ] && [ -z "$DIGITAL_NO_AWT_WORKAROUND" ]; then
    export _JAVA_AWT_WM_NONREPARENTING=1
fi

java -jar "$DIR/Digital.jar" "$1"
