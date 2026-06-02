#!/bin/bash

# Script to prepare input directory for jpackage

set -e  # Exit on error

echo "📁 Preparing input directory for jpackage..."

# Working directories
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
INPUT_DIR="$SCRIPT_DIR/jpackage-input"

# Check that main JAR exists
MAIN_JAR="$PROJECT_ROOT/target/Digital.jar"
if [ ! -f "$MAIN_JAR" ]; then
    echo "❌ Error: Digital.jar not found in $MAIN_JAR"
    echo "   Run 'mvn package' first to generate the JAR"
    exit 1
fi

echo "🧹 Cleaning existing input directory..."
rm -rf "$INPUT_DIR"
mkdir -p "$INPUT_DIR"

echo "📦 Copying main JAR..."
cp "$MAIN_JAR" "$INPUT_DIR/"

echo "📚 Copying component libraries..."
# Copy digital component libraries (DIL Chips, RAMs, etc.)
if [ -d "$PROJECT_ROOT/src/main/dig/lib" ]; then
    mkdir -p "$INPUT_DIR/lib"
    cp -r "$PROJECT_ROOT/src/main/dig/lib"/* "$INPUT_DIR/lib/"
    echo "   ✅ Component libraries copied"
else
    echo "   ⚠️  lib directory not found, skipped"
fi

echo "🔬 Copying circuit examples..."
# Create examples directory
mkdir -p "$INPUT_DIR/examples"

# Copy all examples following Assembly.xml structure
EXAMPLE_DIRS=(
    "combinatorial"
    "74xx" 
    "sequential"
    "processor"
    "hazard"
    "pld"
    "cmos"
    "nmos"
    "graphicRam"
    "generic"
    "misc"
    "hdl"
)

for dir in "${EXAMPLE_DIRS[@]}"; do
    if [ -d "$PROJECT_ROOT/src/main/dig/$dir" ]; then
        mkdir -p "$INPUT_DIR/examples/$dir"
        cp -r "$PROJECT_ROOT/src/main/dig/$dir"/* "$INPUT_DIR/examples/$dir/" 2>/dev/null || true
        echo "   ✅ $dir examples copied"
    fi
done

# Copy additional HDL examples from tests
if [ -d "$PROJECT_ROOT/src/test/resources/dig/hdl_distributable" ]; then
    cp -r "$PROJECT_ROOT/src/test/resources/dig/hdl_distributable"/* "$INPUT_DIR/examples/hdl/" 2>/dev/null || true
    echo "   ✅ Additional HDL examples copied"
fi

echo "🔧 Copying FSM examples..."
# Copy FSM examples
if [ -d "$PROJECT_ROOT/src/main/fsm" ]; then
    mkdir -p "$INPUT_DIR/examples/fsm"
    cp -r "$PROJECT_ROOT/src/main/fsm"/* "$INPUT_DIR/examples/fsm/" 2>/dev/null || true
    echo "   ✅ FSM examples copied"
fi

echo "📖 Copying documentation..."
# Copy documentation if available
if [ -d "$PROJECT_ROOT/target/docu" ]; then
    mkdir -p "$INPUT_DIR/docu"
    cp -r "$PROJECT_ROOT/target/docu"/* "$INPUT_DIR/docu/" 2>/dev/null || true
    echo "   ✅ Documentation copied"
else
    echo "   ⚠️  Documentation not found in target/docu"
    echo "   💡 Tip: run 'mvn install' to generate documentation"
fi

echo "🎨 Copying SVG icon..."
# Copy original SVG icon
if [ -f "$PROJECT_ROOT/src/main/svg/icon.svg" ]; then
    cp "$PROJECT_ROOT/src/main/svg/icon.svg" "$INPUT_DIR/"
    echo "   ✅ SVG icon copied"
fi

echo "📄 Copying information files..."
# Copy informational files
INFO_FILES=(
    "distribution/ReleaseNotes.txt"
    "distribution/Version.txt"
    "LICENSE"
    "README.md"
)

for file in "${INFO_FILES[@]}"; do
    if [ -f "$PROJECT_ROOT/$file" ]; then
        cp "$PROJECT_ROOT/$file" "$INPUT_DIR/"
        echo "   ✅ $(basename "$file") copied"
    fi
done

echo "📊 Verifying input directory content..."
echo "Structure created:"
find "$INPUT_DIR" -type d | head -20 | sed 's|^|   |'

if [ $(find "$INPUT_DIR" -type d | wc -l) -gt 20 ]; then
    echo "   ... (and other directories)"
fi

echo ""
echo "Main files:"
ls -la "$INPUT_DIR" | grep -E '\.(jar|txt|md|svg)$' | sed 's|^|   |'

echo ""
echo "📊 Total size: $(du -sh "$INPUT_DIR" | cut -f1)"
echo "✅ Input directory prepared successfully: $INPUT_DIR"