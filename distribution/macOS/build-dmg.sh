#!/bin/bash
#
# Builds the macOS application Digital.app, which contains its own Java runtime,
# and a disk image (DMG) to distribute it.
#
# Usage:   distribution/macOS/build-dmg.sh
# Needs:   macOS, git, Maven and a JDK >= 17 including jpackage (JAVA_HOME).
# Output:  target/macos/
#            Digital-<version>-macOS-<arch>.dmg             the disk image
#            Digital-<version>-macOS-<arch>.manifest.txt    hash of every file in Digital.app
#            Digital-<version>-macOS-<arch>.build-info.txt  commit and tools used
#            Digital-<version>-macOS-<arch>.sha256          checksums of the files above
#
# The build is reproducible: building the same commit with the same JDK build on
# the same macOS version creates an identical Digital.app, so everybody can check
# that a published DMG was built from the published sources. On other macOS versions
# the code signatures of the native files can differ. See README.md in this folder.
#
# Environment:
#   JAVA_HOME     JDK used for compiling and packaging (default: /usr/libexec/java_home -v 17+)
#   ALLOW_DIRTY=1 build even if the working tree has uncommitted changes;
#                 the result can not be verified by others
#   RUN_TESTS=1   run the unit tests (mvn verify) instead of skipping them
#
set -euo pipefail
# the permissions of the files in Digital.app must not depend on the user's settings
umask 022

die() { echo "error: $*" >&2; exit 1; }

ROOT=$(cd "$(dirname "$0")/../.." && pwd)
MAC="$ROOT/distribution/macOS"
OUT="$ROOT/target/macos"
cd "$ROOT"

[ "$(uname -s)" = Darwin ] || die "this script has to run on macOS"

# Ensure JAVA_HOME has jpackage (JDK 17 or newer)
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/jpackage" ]; then
    if [ -x /usr/libexec/java_home ]; then
        DETECTED_JAVA=$(/usr/libexec/java_home -v 17+ 2>/dev/null || true)
        if [ -n "$DETECTED_JAVA" ] && [ -x "$DETECTED_JAVA/bin/jpackage" ]; then
            JAVA_HOME="$DETECTED_JAVA"
        fi
    fi
fi

JAVA_HOME=${JAVA_HOME:-}
export JAVA_HOME
[ -n "$JAVA_HOME" ] || die "JAVA_HOME could not be determined"

JPACKAGE="$JAVA_HOME/bin/jpackage"
[ -x "$JPACKAGE" ] || die "jpackage not found in $JAVA_HOME (JDK 17 or newer required)"
command -v mvn >/dev/null || die "Maven (mvn) not found"

# Only committed sources can be verified by others.
if [ -n "$(git status --porcelain)" ]; then
    git status --short >&2
    if [ "${ALLOW_DIRTY:-}" = 1 ]; then
        echo "warning: uncommitted changes, the result is not reproducible from the repository" >&2
    else
        die "uncommitted changes (set ALLOW_DIRTY=1 to build anyway)"
    fi
fi

# Version from the last tag: v0.31 -> 0.31, 34 commits later -> 0.31.34
DESCRIBE=$(git describe --tags --match 'v[0-9]*' --long)
[[ $DESCRIBE =~ ^v([0-9]+(\.[0-9]+)*)-([0-9]+)-g[0-9a-f]+$ ]] || die "unexpected git describe: $DESCRIBE"
VERSION=${BASH_REMATCH[1]}
COMMITS=${BASH_REMATCH[3]}
if [ "$COMMITS" != 0 ] && [ "$(echo "$VERSION" | tr -cd . | wc -c)" -lt 2 ]; then
    VERSION="$VERSION.$COMMITS"
fi

# All timestamps are taken from the commit, not from the clock.
COMMIT=$(git rev-parse HEAD)
COMMIT_EPOCH=$(git log -1 --format=%ct)
OUTPUT_TIMESTAMP=$(date -u -r "$COMMIT_EPOCH" +%Y-%m-%dT%H:%M:%SZ)
BUILD_TIME=$(date -u -r "$COMMIT_EPOCH" '+%Y-%m-%d %H:%M')
YEAR=$(date -u -r "$COMMIT_EPOCH" +%Y)

echo "building Digital $VERSION ($DESCRIBE) with $("$JAVA_HOME/bin/java" -version 2>&1 | head -1)"

MVN_GOAL=package
MVN_SKIP=(-DskipTests -Djacoco.skip=true)
if [ "${RUN_TESTS:-}" = 1 ]; then
    MVN_GOAL=verify
    MVN_SKIP=()
fi
mvn -B -ntp "${MVN_SKIP[@]+"${MVN_SKIP[@]}"}" \
    -Dproject.build.outputTimestamp="$OUTPUT_TIMESTAMP" \
    -DbuildTimestamp="$BUILD_TIME" \
    clean "$MVN_GOAL"
[ -f target/Digital.jar ] || die "target/Digital.jar was not built"

# The app is created in a temporary folder: codesign refuses bundles with extended
# attributes, which are added e.g. by iCloud Drive if the repository is synced.
WORK=$(mktemp -d "${TMPDIR:-/tmp}/digital-macos.XXXXXX")
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$OUT" "$WORK/input/bin" "$WORK/resources"
cp target/Digital.jar src/main/resources/icons/splash.png "$WORK/input/"
# the command "digital", Contents/app/bin/digital in the bundle
cp "$MAC/digital" "$WORK/input/bin/digital"
chmod 755 "$WORK/input/bin/digital"
# jpackage only accepts versions starting with a number > 0, so the real version is
# written to Info.plist directly and jpackage gets a placeholder, which is used only
# in its internal files (.jpackage.xml and Digital.cfg).
# The name of the *.dig document type.
DOC_TYPE="Digital Circuit"
DOC_TYPE_SED=$(printf '%s' "$DOC_TYPE" | sed -e 's/&/\&amp;/g' -e 's/</\&lt;/g' -e 's/>/\&gt;/g' -e 's/[\\|&]/\\&/g')
sed -e "s/DIGITAL_VERSION/$VERSION/g" -e "s|DIGITAL_DOCUMENT_TYPE|$DOC_TYPE_SED|g" \
    "$MAC/resources/Info.plist" > "$WORK/resources/Info.plist"

# Second launcher Contents/MacOS/digital-cli for the command line interface,
# like "java -cp Digital.jar CLI". Headless, so no window or dock icon appears.
printf 'main-class=CLI\njava-options=-Djava.awt.headless=true\n' > "$WORK/digital-cli.properties"

# The document type itself is declared in Info.plist. The file association is only
# passed to jpackage to copy the document icon to Contents/Resources.
printf 'extension=dig\nmime-type=text/x-digital\nicon=%s\n' \
    "$MAC/digital-doc.icns" > "$WORK/file-association.properties"

# Modules of the bundled runtime, as determined by
#   jdeps --print-module-deps --ignore-missing-deps target/Digital.jar
# (this covers the simulator, the command line interface and all libraries),
# plus the EC crypto provider needed for https (release check) in JDK 21 and the
# locale data of the languages supported by Digital (e.g. decimal separators).
MODULES=java.base,java.desktop,java.prefs,java.sql,jdk.unsupported,jdk.crypto.ec,jdk.localedata
LOCALES=en,de,es,fr,it,pt,zh

# Info.plist is taken from resources/ (see comment there). jpackage signs the
# bundle (ad hoc, if no certificate is given), so nothing must be changed afterwards.
"$JPACKAGE" --type app-image \
    --dest "$WORK" \
    --name Digital \
    --input "$WORK/input" \
    --main-jar Digital.jar \
    --main-class de.neemann.digital.gui.Main \
    --java-options '-splash:$APPDIR/splash.png' \
    --app-version 1.0 \
    --copyright "Copyright (c) 2016-$YEAR Helmut Neemann" \
    --vendor "Helmut Neemann" \
    --icon "$MAC/Digital.icns" \
    --resource-dir "$WORK/resources" \
    --mac-package-identifier de.neemann.digital \
    --mac-package-name Digital \
    --add-launcher digital-cli="$WORK/digital-cli.properties" \
    --file-associations "$WORK/file-association.properties" \
    --add-modules "$MODULES" \
    --jlink-options "--strip-debug --no-man-pages --no-header-files --strip-native-commands --include-locales=$LOCALES"

APP="$WORK/Digital.app"
codesign --verify --deep --strict "$APP" || die "the signature of Digital.app is not valid"
for f in MacOS/Digital MacOS/digital-cli app/bin/digital; do
    [ -x "$APP/Contents/$f" ] || die "Contents/$f is missing in Digital.app"
done
[ -f "$APP/Contents/Resources/digital-doc.icns" ] || die "the document icon is missing in Digital.app"

ARCH=$(lipo -archs "$APP/Contents/MacOS/Digital")
NAME="Digital-$VERSION-macOS-$ARCH"

"$MAC/app-manifest.sh" "$APP" > "$OUT/$NAME.manifest.txt"

{
    echo "commit:      $COMMIT"
    echo "describe:    $DESCRIBE"
    echo "version:     $VERSION"
    echo "commit time: $OUTPUT_TIMESTAMP"
    echo "jdk:         $(grep -E '^(IMPLEMENTOR|JAVA_RUNTIME_VERSION)=' "$JAVA_HOME/release" | cut -d= -f2 | tr -d '"' | tr '\n' ' ')($("$JAVA_HOME/bin/java" -XshowSettings:properties -version 2>&1 | awk '/os.arch =/{print $3}'))"
    echo "maven:       $(mvn -v | head -1)"
    echo "macOS:       $(sw_vers -productVersion) ($(uname -m))"
    echo "dirty:       $([ -n "$(git status --porcelain)" ] && echo yes || echo no)"
} > "$OUT/$NAME.build-info.txt"

# Disk image with the app and a link to /Applications for installing by drag and drop.
# The manual of hdiutil in recent macOS versions recommends "diskutil image" instead.
# hdiutil is still used: it works, and "diskutil image" is missing on older systems
# (create needs macOS 14) and offers no replacement for "hdiutil verify".
STAGE="$WORK/dmg"
mkdir "$STAGE"
mv "$APP" "$STAGE/Digital.app"
ln -s /Applications "$STAGE/Applications"
hdiutil create -quiet -volname Digital -srcfolder "$STAGE" -fs APFS -format UDZO -ov "$OUT/$NAME.dmg"

# Check that the disk image contains exactly the app described by the manifest.
"$MAC/verify-dmg.sh" "$OUT/$NAME.dmg" "$OUT/$NAME.manifest.txt"

(cd "$OUT" && shasum -a 256 "$NAME.dmg" "$NAME.manifest.txt" "$NAME.build-info.txt" > "$NAME.sha256")
echo
cat "$OUT/$NAME.build-info.txt"
echo
cat "$OUT/$NAME.sha256"