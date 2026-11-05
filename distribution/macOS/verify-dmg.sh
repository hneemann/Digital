#!/bin/bash
#
# Checks a Digital disk image without running anything from it:
#  - the image contains only Digital.app and a link to /Applications,
#  - the code signature of Digital.app is valid and nothing was modified after signing,
#  - optionally: every file in Digital.app matches a manifest, e.g. the one created
#    by build-dmg.sh when building the same commit yourself.
#
# Usage: verify-dmg.sh <Digital.dmg> [<manifest.txt>]
#
set -euo pipefail

die() { echo "FAILED: $*" >&2; exit 1; }

[ $# -ge 1 ] && [ $# -le 2 ] || { echo "usage: $0 <Digital.dmg> [<manifest.txt>]" >&2; exit 1; }
DMG=$1
MANIFEST=${2:-}
[ -f "$DMG" ] || die "$DMG not found"
[ -z "$MANIFEST" ] || [ -f "$MANIFEST" ] || die "$MANIFEST not found"
MAC=$(cd "$(dirname "$0")" && pwd)

echo "disk image: $DMG"
echo "sha256:     $(shasum -a 256 "$DMG" | cut -d' ' -f1)"
hdiutil verify -quiet "$DMG" || die "checksum of the disk image is not valid"

MOUNT=$(mktemp -d)
ACTUAL=$(mktemp)
ATTACHED=
cleanup() {
    if [ -n "$ATTACHED" ] && ! hdiutil detach -quiet "$MOUNT"; then
        echo "warning: the disk image is still attached at $MOUNT" >&2
    fi
    rmdir "$MOUNT" 2>/dev/null || true
    rm -f "$ACTUAL" "$ACTUAL.diff"
}
trap cleanup EXIT
hdiutil attach -quiet -readonly -nobrowse -noautoopen -mountpoint "$MOUNT" "$DMG"
ATTACHED=1

# Exactly two entries are expected (.fseventsd is created by macOS itself).
CONTENT=$(cd "$MOUNT" && ls -A | grep -v '^\.fseventsd$' | LC_ALL=C sort | tr '\n' ' ')
[ "$CONTENT" = "Applications Digital.app " ] || die "unexpected content of the disk image: $CONTENT"
[ -L "$MOUNT/Applications" ] && [ "$(readlink "$MOUNT/Applications")" = /Applications ] \
    || die "Applications is not a link to /Applications"

APP="$MOUNT/Digital.app"
[ -d "$APP" ] && [ ! -L "$APP" ] || die "Digital.app is not a folder"
codesign --verify --deep --strict "$APP" || die "code signature of Digital.app is not valid"
[ -f "$APP/Contents/app/Digital.jar" ] && [ ! -L "$APP/Contents/app/Digital.jar" ] \
    || die "Digital.app contains no Digital.jar"
INFO="$APP/Contents/Info.plist"
echo "bundle:     $(plutil -extract CFBundleIdentifier raw "$INFO") $(plutil -extract CFBundleShortVersionString raw "$INFO")," \
    "macOS >= $(plutil -extract LSMinimumSystemVersion raw "$INFO")," \
    "$(lipo -archs "$APP/Contents/MacOS/Digital")"
echo "signature:  valid ($(codesign -dv "$APP" 2>&1 | grep -E '^Signature=' | cut -d= -f2))"
echo "Digital.jar sha256: $(shasum -a 256 "$APP/Contents/app/Digital.jar" | cut -d' ' -f1)"

"$MAC/app-manifest.sh" "$APP" > "$ACTUAL"
if [ -n "$MANIFEST" ]; then
    if diff -u "$MANIFEST" "$ACTUAL" > "$ACTUAL.diff"; then
        echo "manifest:   all $(wc -l < "$ACTUAL" | tr -d ' ') entries of Digital.app match $MANIFEST"
    else
        cat "$ACTUAL.diff" >&2
        die "Digital.app differs from $MANIFEST"
    fi
else
    echo "manifest:   $(wc -l < "$ACTUAL" | tr -d ' ') entries, sha256 $(shasum -a 256 "$ACTUAL" | cut -d' ' -f1)"
    echo "            (pass a manifest as second argument to compare)"
fi
echo "OK"
