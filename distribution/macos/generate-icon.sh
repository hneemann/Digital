#!/bin/bash

# Script to generate macOS icon (.icns) for Digital Circuit Simulator
# Uses native macOS tools (sips and iconutil)

set -e  # Exit on error

echo "🎨 Generating macOS icon for Digital Circuit Simulator..."

# Working directories
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SVG_SOURCE="$PROJECT_ROOT/src/main/svg/icon.svg"
ICON_OUTPUT="$SCRIPT_DIR/icon.icns"

# Check that SVG file exists
if [ ! -f "$SVG_SOURCE" ]; then
    echo "❌ Error: SVG file not found: $SVG_SOURCE"
    exit 1
fi

# Check that sips is available (should be present on macOS)
if ! command -v sips &> /dev/null; then
    echo "❌ Error: sips not found. This script must be run on macOS."
    exit 1
fi

# Check that iconutil is available (should be present on macOS)
if ! command -v iconutil &> /dev/null; then
    echo "❌ Error: iconutil not found. This script must be run on macOS."
    exit 1
fi

echo "📁 Creating temporary directory for iconset..."
TEMP_ICONSET="$SCRIPT_DIR/icon.iconset"
rm -rf "$TEMP_ICONSET"
mkdir -p "$TEMP_ICONSET"

echo "🔄 Converting SVG to PNG for different resolutions..."

# Generate all required resolutions for macOS using native sips command
sips -s format png -z 16 16     "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_16x16.png"
sips -s format png -z 32 32     "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_16x16@2x.png"
sips -s format png -z 32 32     "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_32x32.png"
sips -s format png -z 64 64     "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_32x32@2x.png"
sips -s format png -z 128 128   "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_128x128.png"
sips -s format png -z 256 256   "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_128x128@2x.png"
sips -s format png -z 256 256   "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_256x256.png"
sips -s format png -z 512 512   "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_256x256@2x.png"
sips -s format png -z 512 512   "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_512x512.png"
sips -s format png -z 1024 1024 "$SVG_SOURCE" --out "$TEMP_ICONSET/icon_512x512@2x.png"

echo "🔨 Generating .icns file..."
iconutil -c icns "$TEMP_ICONSET" -o "$ICON_OUTPUT"

echo "🧹 Cleaning temporary files..."
rm -rf "$TEMP_ICONSET"

if [ -f "$ICON_OUTPUT" ]; then
    echo "✅ macOS icon generated successfully: $ICON_OUTPUT"
    echo "📊 Size: $(du -sh "$ICON_OUTPUT" | cut -f1)"
else
    echo "❌ Error in icon generation"
    exit 1
fi