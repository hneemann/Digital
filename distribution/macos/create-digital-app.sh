#!/bin/bash

# Main script to create Digital.app for macOS

set -e  # Exit on error

echo "🚀 Creating Digital.app for macOS - Digital Circuit Simulator"
echo "=============================================================="

# Working directories
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
INPUT_DIR="$SCRIPT_DIR/jpackage-input"
ICON_FILE="$SCRIPT_DIR/icon.icns"
APP_OUTPUT="$SCRIPT_DIR/Digital.app"

# App configuration
APP_NAME="Digital"
APP_VERSION="1.0"
APP_VENDOR="Digital Circuit Simulator"
APP_DESCRIPTION="Digital Circuit Simulator - Digital Circuit Design and Simulation Tool"
MAIN_CLASS="de.neemann.digital.gui.Main"

# Function to check prerequisites
check_prerequisites() {
    echo "🔍 Checking prerequisites..."
    
    # Check Java and jpackage
    if ! command -v java &> /dev/null; then
        echo "❌ Error: Java not found. Install Java 17 or higher."
        exit 1
    fi
    
    if ! command -v jpackage &> /dev/null; then
        echo "❌ Error: jpackage not found. Requires Java 17 or higher."
        exit 1
    fi
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "❌ Error: Java 17 or higher required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    echo "   ✅ Java $JAVA_VERSION found"
    echo "   ✅ jpackage available"
    
    # Check that we're on macOS
    if [[ "$OSTYPE" != "darwin"* ]]; then
        echo "❌ Error: This script must be run on macOS"
        exit 1
    fi
    
    echo "   ✅ macOS system confirmed"
}

# Function to clean existing files
cleanup_existing() {
    echo "🧹 Cleaning existing files..."
    
    if [ -d "$APP_OUTPUT" ]; then
        echo "   🗑️  Removing existing Digital.app..."
        rm -rf "$APP_OUTPUT"
    fi
    
    if [ -d "$INPUT_DIR" ]; then
        echo "   🗑️  Removing existing input directory..."
        rm -rf "$INPUT_DIR"
    fi
    
    echo "   ✅ Cleanup completed"
}

# Function to generate icon
generate_icon() {
    echo "🎨 Generating macOS icon..."
    
    if [ -f "$ICON_FILE" ]; then
        echo "   ℹ️  Existing icon found, reusing it"
        return 0
    fi
    
    echo "   🔄 Running icon generation script..."
    "$SCRIPT_DIR/generate-icon.sh"
    
    if [ ! -f "$ICON_FILE" ]; then
        echo "❌ Error: Icon generation failed"
        exit 1
    fi
    
    echo "   ✅ Icon generated successfully"
}

# Function to prepare input
prepare_input() {
    echo "📁 Preparing input directory..."
    
    echo "   🔄 Running input preparation script..."
    "$SCRIPT_DIR/prepare-input.sh"
    
    if [ ! -d "$INPUT_DIR" ]; then
        echo "❌ Error: Input preparation failed"
        exit 1
    fi
    
    if [ ! -f "$INPUT_DIR/Digital.jar" ]; then
        echo "❌ Error: Digital.jar not found in input directory"
        exit 1
    fi
    
    echo "   ✅ Input directory prepared"
}

# Function to create app bundle
create_app_bundle() {
    echo "🔨 Creating app bundle with jpackage..."
    
    # Complete jpackage command with file association
    jpackage \
        --input "$INPUT_DIR" \
        --name "$APP_NAME" \
        --main-jar "Digital.jar" \
        --main-class "$MAIN_CLASS" \
        --type app-image \
        --icon "$ICON_FILE" \
        --app-version "$APP_VERSION" \
        --vendor "$APP_VENDOR" \
        --description "$APP_DESCRIPTION" \
        --dest "$SCRIPT_DIR" \
        --file-associations "$SCRIPT_DIR/dig-file-association.properties" \
        --verbose
    
    if [ ! -d "$APP_OUTPUT" ]; then
        echo "❌ Error: App bundle creation failed"
        exit 1
    fi
    
    echo "   ✅ App bundle created successfully"
}

# Function to verify result
verify_result() {
    echo "✅ Verifying final result..."
    
    if [ ! -d "$APP_OUTPUT" ]; then
        echo "❌ Error: Digital.app not found"
        exit 1
    fi
    
    # Verify app structure
    if [ ! -f "$APP_OUTPUT/Contents/Info.plist" ]; then
        echo "❌ Error: Invalid app structure (Info.plist missing)"
        exit 1
    fi
    
    if [ ! -f "$APP_OUTPUT/Contents/MacOS/$APP_NAME" ]; then
        echo "❌ Error: Main executable missing"
        exit 1
    fi
    
    # Verify resources
    if [ ! -f "$APP_OUTPUT/Contents/app/Digital.jar" ]; then
        echo "❌ Error: Main JAR missing in app"
        exit 1
    fi
    
    echo "   ✅ App structure verified"
    echo "   📊 Size: $(du -sh "$APP_OUTPUT" | cut -f1)"
    
    # Show main content
    echo "   📁 Main content:"
    ls -la "$APP_OUTPUT/Contents/app/" | head -10 | sed 's|^|      |'
    
    if [ -d "$APP_OUTPUT/Contents/app/lib" ]; then
        echo "   📚 Libraries found: $(find "$APP_OUTPUT/Contents/app/lib" -name "*.dig" | wc -l) .dig files"
    fi
    
    if [ -d "$APP_OUTPUT/Contents/app/examples" ]; then
        echo "   🔬 Examples found: $(find "$APP_OUTPUT/Contents/app/examples" -name "*.dig" | wc -l) .dig files"
    fi
}

# Function to show final instructions
show_final_instructions() {
    echo ""
    echo "🎉 Digital.app created successfully!"
    echo "===================================="
    echo ""
    echo "📍 Location: $APP_OUTPUT"
    echo "📊 Size: $(du -sh "$APP_OUTPUT" | cut -f1)"
    echo ""
    echo "🚀 How to use:"
    echo "   • Double-click Digital.app to launch"
    echo "   • Drag to /Applications to install"
    echo "   • Share the .app file to distribute"
    echo ""
    echo "✨ Features:"
    echo "   ✅ Integrated Java runtime (no Java installation required)"
    echo "   ✅ All component libraries included"
    echo "   ✅ Complete circuit examples"
    echo "   ✅ Native macOS icon"
    echo "   ✅ Full system integration"
    echo ""
    echo "💡 To test the app:"
    echo "   open '$APP_OUTPUT'"
}

# Main execution
main() {
    echo "Starting Digital.app creation process..."
    echo "Timestamp: $(date)"
    echo ""
    
    check_prerequisites
    cleanup_existing
    generate_icon
    prepare_input
    create_app_bundle
    verify_result
    show_final_instructions
    
    echo ""
    echo "✅ Process completed successfully!"
}

# Error handling
trap 'echo "❌ Error during execution. Process interrupted."; exit 1' ERR

# Execution
main "$@"