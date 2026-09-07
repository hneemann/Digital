# 🚀 Quick Start Guide - Digital.app for macOS

## ⚡ Quick Creation

### 1. Prerequisites
```bash
# Check Java 17+
java --version

# If needed, install Java
brew install openjdk@17
```

**Note**: No additional tools required! This automation uses only native macOS tools (`sips` and `iconutil`).

### 2. Build Project
```bash
# From Digital project root
mvn clean package
```

### 3. Create macOS App
```bash
# Navigate to macOS directory
cd distribution/macos

# Create the app
./create-digital-app.sh
```

### 4. Result
- ✅ **Digital.app** created and ready to use
- ✅ **~200-300 MB** with integrated Java runtime
- ✅ **All libraries** and examples included

## 🎯 Alternative Usage

### With Makefile
```bash
cd distribution/macos
make app                    # Create the app
make install               # Install to /Applications
make test                  # Test the app
```

### With Maven
```bash
# From project root
mvn clean package install  # On macOS automatically creates the app
```

## 📱 Installation

### Method 1: Drag & Drop
1. Open **Finder**
2. Drag **Digital.app** to **Applications**

### Method 2: Command
```bash
cp -r Digital.app /Applications/
```

### Method 3: Makefile
```bash
make install
```

## 🔗 File Association

The app is configured to handle `.dig` files automatically:

### Opening .dig files
```bash
# From command line
open circuit.dig

# Or double-click .dig files in Finder
```

### File Association Features
- ✅ **Automatic recognition** of `.dig` files
- ✅ **Custom icon** for Digital circuit files
- ✅ **Direct opening** from Finder or command line
- ✅ **MIME type** support (`application/x-digital-circuit`)