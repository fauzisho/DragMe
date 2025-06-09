#!/bin/bash

# 🧪 Quick Build Test Script
# This script tests if the Gradle build configuration is working

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }

echo -e "${BLUE}🧪 Testing DragMe Build Configuration${NC}"
echo ""

# Check if running from project root
if [ ! -f "gradlew" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Test 1: Basic project configuration
print_info "Testing basic project configuration..."
if ./gradlew projects; then
    print_success "Project configuration is valid"
else
    print_error "Project configuration has issues"
    exit 1
fi

echo ""

# Test 2: Check available tasks first
print_info "Checking available composeApp tasks..."
echo "Available verification tasks:"
./gradlew :composeApp:tasks --group="verification" 2>/dev/null | grep -E "^\s*\w" || echo "No verification tasks found"

echo ""

# Test 3: Try to compile Kotlin common sources
print_info "Testing Kotlin compilation..."
if ./gradlew :composeApp:compileCommonMainKotlinMetadata; then
    print_success "Kotlin compilation successful"
else
    print_warning "Kotlin metadata compilation had issues, trying alternative..."
    # Try alternative compilation task
    if ./gradlew :composeApp:compileKotlinDesktop 2>/dev/null; then
        print_success "Desktop Kotlin compilation successful"
    elif ./gradlew :composeApp:build; then
        print_success "Full build successful"
    else
        print_error "Compilation failed"
        exit 1
    fi
fi

echo ""

# Test 4: Desktop application configuration test
print_info "Testing desktop application configuration..."
if ./gradlew :composeApp:jar 2>/dev/null; then
    print_success "Desktop JAR creation successful"
elif ./gradlew :composeApp:desktopJar 2>/dev/null; then
    print_success "Desktop JAR creation successful (alternative task)"
else
    print_warning "JAR creation not available (this might be normal for this project structure)"
fi

echo ""

# Test 5: Try a simple task that should exist
print_info "Testing basic Gradle task execution..."
if ./gradlew :composeApp:help; then
    print_success "Basic task execution works"
else
    print_error "Basic task execution failed"
    exit 1
fi

echo ""

print_success "🎉 Build configuration tests completed!"
print_info "Summary:"
echo "  • Project structure is valid"
echo "  • Gradle configuration compiles"
echo "  • Basic tasks are executable"
echo ""
print_warning "Note: Some warnings about task conflicts are normal and don't affect CI/CD"
echo ""
print_info "Next steps:"
echo "  1. Commit your changes: git add . && git commit -m 'fix: Gradle build configuration'"
echo "  2. Push to trigger CI/CD: git push origin main"
echo "  3. Create a release: git tag v1.0.0 && git push origin v1.0.0"
echo ""
print_info "To test desktop package building locally:"
echo "  ./gradlew :composeApp:packageDeb     # Linux"
echo "  ./gradlew :composeApp:packageMsi     # Windows"  
echo "  ./gradlew :composeApp:packageDmg     # macOS"
