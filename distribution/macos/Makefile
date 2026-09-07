# Makefile for macOS distribution automation of Digital Circuit Simulator

.PHONY: all clean icon input app help install test

# Default target
all: app

# Help
help:
	@echo "🚀 Digital Circuit Simulator - macOS Automation"
	@echo "==============================================="
	@echo ""
	@echo "Available targets:"
	@echo "  all        - Create complete app (default)"
	@echo "  app        - Create Digital.app"
	@echo "  icon       - Generate only .icns icon"
	@echo "  input      - Prepare only input directory"
	@echo "  clean      - Clean all generated files"
	@echo "  install    - Install app in /Applications"
	@echo "  test       - Test created app"
	@echo "  help       - Show this help"
	@echo ""
	@echo "Examples:"
	@echo "  make app           # Create complete app"
	@echo "  make clean app     # Clean and recreate app"
	@echo "  make install       # Install app in /Applications"

# Cleanup
clean:
	@echo "🧹 Cleaning generated files..."
	@rm -rf Digital.app
	@rm -rf jpackage-input
	@rm -f icon.icns
	@echo "✅ Cleanup completed"

# Icon generation
icon:
	@echo "🎨 Generating macOS icon..."
	@./generate-icon.sh

# Input preparation
input:
	@echo "📁 Preparing input directory..."
	@./prepare-input.sh

# Complete app creation
app:
	@echo "🚀 Creating Digital.app..."
	@./create-digital-app.sh

# Installation in /Applications
install: app
	@echo "📱 Installing in /Applications..."
	@if [ -d "/Applications/Digital.app" ]; then \
		echo "   🗑️  Removing existing version..."; \
		rm -rf "/Applications/Digital.app"; \
	fi
	@cp -r Digital.app /Applications/
	@echo "✅ Digital.app installed in /Applications"

# App testing
test: app
	@echo "🧪 Testing created app..."
	@if [ -d "Digital.app" ]; then \
		echo "   ✅ Digital.app found"; \
		echo "   📊 Size: $$(du -sh Digital.app | cut -f1)"; \
		echo "   🔍 Verifying structure..."; \
		if [ -f "Digital.app/Contents/Info.plist" ]; then \
			echo "   ✅ Info.plist present"; \
		else \
			echo "   ❌ Info.plist missing"; \
			exit 1; \
		fi; \
		if [ -f "Digital.app/Contents/MacOS/Digital" ]; then \
			echo "   ✅ Executable present"; \
		else \
			echo "   ❌ Executable missing"; \
			exit 1; \
		fi; \
		if [ -f "Digital.app/Contents/app/Digital.jar" ]; then \
			echo "   ✅ Main JAR present"; \
		else \
			echo "   ❌ Main JAR missing"; \
			exit 1; \
		fi; \
		echo "   🚀 Starting app test..."; \
		open Digital.app; \
		echo "   ✅ App started successfully"; \
	else \
		echo "   ❌ Digital.app not found"; \
		exit 1; \
	fi

# Developer targets
dev-clean: clean
	@echo "🧹 Complete cleanup for developers..."
	@cd ../.. && mvn clean
	@echo "✅ Maven cleanup completed"

dev-build:
	@echo "🔨 Complete build for developers..."
	@cd ../.. && mvn package
	@echo "✅ Maven build completed"

dev-all: dev-clean dev-build app
	@echo "✅ Complete build and app created"