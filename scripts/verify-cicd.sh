#!/bin/bash

# 🔍 DragMe CI/CD Verification Script
# This script verifies that the CI/CD setup is complete and ready

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Print colored output
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_header() { echo -e "${PURPLE}🔍 $1${NC}"; }

# Script banner
echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║       🔍 DragMe CI/CD Verifier         ║"
echo "║    Checking CI/CD Setup Completeness  ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Check if running from project root
if [ ! -f "gradlew" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

print_header "Verification Starting..."

# Initialize counters
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# Function to perform a check
check_item() {
    local description="$1"
    local condition="$2"
    local recommendation="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if eval "$condition"; then
        print_success "$description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        print_error "$description"
        if [ -n "$recommendation" ]; then
            echo -e "   ${YELLOW}💡 Fix: $recommendation${NC}"
        fi
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

print_header "🔧 Project Structure Verification"

check_item "Gradle wrapper exists" \
    "[ -f 'gradlew' ]" \
    "Run: gradle wrapper"

check_item "Compose app module exists" \
    "[ -d 'composeApp' ]" \
    "Ensure composeApp directory is present"

check_item "Server module exists" \
    "[ -d 'server' ]" \
    "Ensure server directory is present"

check_item "Shared module exists" \
    "[ -d 'shared' ]" \
    "Ensure shared directory is present"

print_header "📁 CI/CD Files Verification"

check_item "GitHub workflows directory exists" \
    "[ -d '.github/workflows' ]" \
    "Create: mkdir -p .github/workflows"

check_item "Desktop build workflow exists" \
    "[ -f '.github/workflows/desktop-build-publish.yml' ]" \
    "Create the desktop CI/CD workflow file"

check_item "Web deploy workflow exists" \
    "[ -f '.github/workflows/deploy.yml' ]" \
    "Web deployment workflow is present"

check_item "Build script exists" \
    "[ -f 'scripts/build-desktop.sh' ]" \
    "Create the build script for local development"

check_item "Build script is executable" \
    "[ -x 'scripts/build-desktop.sh' ]" \
    "Run: chmod +x scripts/build-desktop.sh"

print_header "📚 Documentation Verification"

check_item "CI/CD documentation exists" \
    "[ -f 'docs/CICD_DESKTOP.md' ]" \
    "Create comprehensive CI/CD documentation"

check_item "Quick start guide exists" \
    "[ -f 'docs/QUICK_START_CICD.md' ]" \
    "Create quick setup guide"

check_item "README has CI/CD info" \
    "grep -q 'CI/CD' README.md" \
    "Update README.md with CI/CD information"

print_header "⚙️  Gradle Configuration Verification"

check_item "Compose app build.gradle.kts exists" \
    "[ -f 'composeApp/build.gradle.kts' ]" \
    "Ensure composeApp build configuration exists"

check_item "Maven publish plugin configured" \
    "grep -q 'maven-publish' composeApp/build.gradle.kts" \
    "Add maven-publish plugin to composeApp/build.gradle.kts"

check_item "Desktop application configuration" \
    "grep -q 'compose.desktop' composeApp/build.gradle.kts" \
    "Configure Compose Desktop in build.gradle.kts"

check_item "Publishing configuration exists" \
    "grep -q 'publishing {' composeApp/build.gradle.kts" \
    "Add publishing configuration to build.gradle.kts"

print_header "🚀 Platform Target Verification"

check_item "Desktop main class configured" \
    "grep -q 'mainClass.*MainKt' composeApp/build.gradle.kts" \
    "Set mainClass in desktop configuration"

check_item "Native distributions configured" \
    "grep -q 'nativeDistributions' composeApp/build.gradle.kts" \
    "Configure target formats (Dmg, Msi, Deb)"

check_item "Desktop main.kt exists" \
    "[ -f 'composeApp/src/desktopMain/kotlin/org/drag/me/main.kt' ]" \
    "Create desktop entry point file"

print_header "🔐 Security and Permissions"

check_item "Workflow has package permissions" \
    "grep -q 'packages: write' .github/workflows/desktop-build-publish.yml" \
    "Add packages: write permission to workflow"

check_item "Workflow has contents permissions" \
    "grep -q 'contents: read' .github/workflows/desktop-build-publish.yml" \
    "Add contents: read permission to workflow"

print_header "🧪 Testing Setup Verification"

check_item "Test task in workflow" \
    "grep -q 'testClasses' .github/workflows/desktop-build-publish.yml" \
    "Add test execution to CI/CD workflow"

check_item "Security scan configured" \
    "grep -q 'trivy' .github/workflows/desktop-build-publish.yml" \
    "Add security scanning to workflow"

print_header "📦 Package Configuration Verification"

check_item "Package names configured" \
    "grep -q 'packageName' composeApp/build.gradle.kts" \
    "Configure package names for all platforms"

check_item "Version configuration" \
    "grep -q 'packageVersion' composeApp/build.gradle.kts" \
    "Set package version in build configuration"

check_item "GitHub Packages repository configured" \
    "grep -q 'maven.pkg.github.com' composeApp/build.gradle.kts" \
    "Configure GitHub Packages Maven repository"

print_header "📊 Build Summary Generation"

# Calculate success rate
SUCCESS_RATE=0
if command -v bc >/dev/null 2>&1 && [ $TOTAL_CHECKS -gt 0 ]; then
    SUCCESS_RATE=$(echo "scale=1; $PASSED_CHECKS * 100 / $TOTAL_CHECKS" | bc 2>/dev/null || echo "0")
elif [ $TOTAL_CHECKS -gt 0 ]; then
    SUCCESS_RATE=$(( PASSED_CHECKS * 100 / TOTAL_CHECKS ))
fi

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                    📊 Verification Results                    ║${NC}"
echo -e "${CYAN}╠══════════════════════════════════════════════════════════════╣${NC}"
printf "${CYAN}║${NC} Total Checks:     ${BLUE}%-3d${NC}                                   ${CYAN}║${NC}\n" "$TOTAL_CHECKS"
printf "${CYAN}║${NC} Passed:           ${GREEN}%-3d${NC}                                   ${CYAN}║${NC}\n" "$PASSED_CHECKS"
printf "${CYAN}║${NC} Failed:           ${RED}%-3d${NC}                                   ${CYAN}║${NC}\n" "$FAILED_CHECKS"
printf "${CYAN}║${NC} Success Rate:     ${YELLOW}%-5s%%${NC}                               ${CYAN}║${NC}\n" "$SUCCESS_RATE"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"

# Overall status
if [ $FAILED_CHECKS -eq 0 ]; then
    echo ""
    print_success "🎉 CI/CD Setup Complete! Your project is ready for automated builds."
    echo ""
    print_info "🚀 Next Steps:"
    echo "  1. Commit and push your changes"
    echo "  2. Create a tag to trigger a release build: git tag v1.0.0 && git push origin v1.0.0"
    echo "  3. Check the Actions tab for build progress"
    echo "  4. Download packages from Releases or GitHub Packages"
    echo ""
    print_info "📖 Documentation:"
    echo "  • Quick Start: docs/QUICK_START_CICD.md"
    echo "  • Full Guide: docs/CICD_DESKTOP.md"
    echo ""
elif [ $FAILED_CHECKS -le 3 ]; then
    echo ""
    print_warning "⚠️  CI/CD Setup Nearly Complete! Fix the remaining issues above."
    echo ""
    print_info "🔧 Priority fixes needed: $FAILED_CHECKS"
    echo ""
else
    echo ""
    print_error "❌ CI/CD Setup Incomplete! Several issues need to be resolved."
    echo ""
    print_info "🔧 Fixes needed: $FAILED_CHECKS"
    echo ""
    print_info "💡 Consider running the quick setup guide:"
    echo "  • docs/QUICK_START_CICD.md"
    echo ""
fi

# Additional helpful information
print_header "💡 Helpful Commands"

echo "Local Development:"
echo "  ./scripts/build-desktop.sh --type native    # Build for your platform"
echo "  ./scripts/build-desktop.sh --help           # See all options"
echo ""
echo "Testing Workflows:"
echo "  git add . && git commit -m 'feat: CI/CD setup'"
echo "  git push origin main                        # Trigger build"
echo "  git tag v1.0.0 && git push origin v1.0.0   # Create release"
echo ""
echo "Monitoring:"
echo "  • GitHub Actions: Repository → Actions tab"
echo "  • Packages: Repository → Packages tab"
echo "  • Releases: Repository → Releases section"
echo ""

# Exit with appropriate code
if [ $FAILED_CHECKS -eq 0 ]; then
    exit 0
else
    exit 1
fi
