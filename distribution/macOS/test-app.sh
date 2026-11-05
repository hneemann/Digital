#!/bin/bash
#
# Starts Digital.app from a disk image, to check that it runs on this processor
# type and macOS version:
#  - runs "digital test" and "digital svg" with a circuit of this repository,
#  - opens the circuit in the simulator with the same "open files" event as a double
#    click in the Finder, and waits until the simulator has loaded it.
#
# Unlike verify-dmg.sh this runs code from the disk image, so only use it with a disk
# image you trust. A window is shown for a short time, and the circuit is added to the
# recent files of Digital until the next start, because it no longer exists then.
# The output of the simulator is written to the output folder, on a CI system
# also a screenshot.
#
# Usage: test-app.sh <Digital.dmg> <output folder>
#
set -euo pipefail

die() { echo "FAILED: $*" >&2; exit 1; }

[ $# -eq 2 ] || { echo "usage: $0 <Digital.dmg> <output folder>" >&2; exit 1; }
DMG=$1
[ -f "$DMG" ] || die "$DMG not found"
mkdir -p "$2"
OUT=$(cd "$2" && pwd)
ROOT=$(cd "$(dirname "$0")/../.." && pwd)

# pwd -P: the simulator is found by the real path of the app
WORK=$(cd "$(mktemp -d "${TMPDIR:-/tmp}/digital-test.XXXXXX")" && pwd -P)
MOUNT="$WORK/mount"
APP="$WORK/Digital.app"
LSREGISTER=/System/Library/Frameworks/CoreServices.framework/Frameworks/LaunchServices.framework/Support/lsregister
ATTACHED=
PID=
cleanup() {
    if [ -n "$PID" ]; then
        kill "$PID" 2>/dev/null || true
    fi
    if [ -n "$ATTACHED" ] && ! hdiutil detach -quiet "$MOUNT"; then
        echo "warning: the disk image is still attached at $MOUNT" >&2
    fi
    # macOS must not remember the copy as an application for *.dig files
    "$LSREGISTER" -u "$APP" 2>/dev/null || true
    rm -rf "$WORK"
}
trap cleanup EXIT

# The app is copied out of the disk image, like a user installs it.
mkdir "$MOUNT"
hdiutil attach -quiet -readonly -nobrowse -noautoopen -mountpoint "$MOUNT" "$DMG"
ATTACHED=1
ditto "$MOUNT/Digital.app" "$APP"
hdiutil detach -quiet "$MOUNT"
ATTACHED=

CIRCUIT="$WORK/test circuit.dig"
cp "$ROOT/src/main/dig/combinatorial/FullAdder.dig" "$CIRCUIT"

echo "command line interface"
"$APP/Contents/app/bin/digital" test -circ "$CIRCUIT" || die "digital test"
"$APP/Contents/app/bin/digital" svg -dig "$CIRCUIT" -svg "$WORK/circuit.svg" || die "digital svg"
grep -q '<svg' "$WORK/circuit.svg" || die "digital svg has not created an SVG file"

echo "simulator"
LOG="$OUT/simulator-output.txt"
open -g -a "$APP" --stdout "$LOG" --stderr "$LOG" "$CIRCUIT"

for _ in $(seq 1 30); do
    PID=$(pgrep -n -f "^$APP/Contents/MacOS/Digital" || true)
    [ -z "$PID" ] || break
    sleep 1
done
[ -n "$PID" ] || die "the simulator has not started"

# Digital adds a circuit to its recent files after loading it.
LOADED=
for _ in $(seq 1 90); do
    kill -0 "$PID" 2>/dev/null || die "the simulator has stopped"
    if defaults read com.apple.java.util.prefs 2>/dev/null | grep -qF "$CIRCUIT"; then
        LOADED=1
        break
    fi
    sleep 1
done
[ -n "$LOADED" ] || die "the simulator has not loaded the circuit"

# gives the window time to be drawn, and shows a crash shortly after loading
sleep 5
kill -0 "$PID" 2>/dev/null || die "the simulator has stopped"
if [ -n "${CI:-}" ]; then
    screencapture -x "$OUT/screenshot.png" || echo "warning: no screenshot" >&2
fi
echo "OK"
