#!/bin/bash

# 🖥️ DragMe Desktop Build Script
# This script helps build desktop applications locally for testing

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
print_header() { echo -e "${PURPLE}🚀 $1${NC}"; }

# Script banner
echo -e "${CYAN}"
echo "╔════════════════════════════════════════╗"
echo "║        🖥️  DragMe Desktop Builder       ║"
echo "║     Local Development Build Script     ║"
echo "╚════════════════════════════════════════╝"
echo -e "${NC}"

# Check if running from project root
if [ ! -f "gradlew" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

# Default values
BUILD_TYPE="all"
CLEAN_BUILD=false
RUN_TESTS=true
OPEN_BUILD_DIR=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            BUILD_TYPE="$2"
            shift 2
            ;;
        -c|--clean)
            CLEAN_BUILD=true
            shift
            ;;
        --no-tests)
            RUN_TESTS=false
            shift
            ;;
        -o|--open)
            OPEN_BUILD_DIR=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -t, --type TYPE     Build type: all, deb, msi, dmg (default: all)"
            echo "  -c, --clean         Clean build before compiling"
            echo "  --no-tests          Skip running tests"
            echo "  -o, --open          Open build directory after completion"
            echo "  -h, --help          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                  # Build all platform packages"
            echo "  $0 -t deb           # Build only Linux .deb package"
            echo "  $0 -c -t dmg        # Clean build macOS .dmg package"
            echo "  $0 --no-tests -o    # Build without tests and open build dir"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use -h or --help for usage information"
            exit 1
            ;;
    esac
done

# Detect current platform
case "$(uname -s)" in
    Darwin*)
        CURRENT_PLATFORM="macOS"
        NATIVE_BUILD="dmg"
        ;;
    Linux*)
        CURRENT_PLATFORM="Linux"
        NATIVE_BUILD="deb"
        ;;
    CYGWIN*|MINGW32*|MSYS*|MINGW*)
        CURRENT_PLATFORM="Windows"
        NATIVE_BUILD="msi"
        ;;
    *)
        CURRENT_PLATFORM="Unknown"
        NATIVE_BUILD="deb"
        ;;
esac

print_info "Detected platform: $CURRENT_PLATFORM"

# Validate build type
case $BUILD_TYPE in
    all|deb|msi|dmg|native)
        ;;
    *)
        print_error "Invalid build type: $BUILD_TYPE"
        print_info "Valid options: all, deb, msi, dmg, native"
        exit 1
        ;;
esac

# Set build type to native if requested
if [ "$BUILD_TYPE" = "native" ]; then
    BUILD_TYPE="$NATIVE_BUILD"
    print_info "Building native package for $CURRENT_PLATFORM: $BUILD_TYPE"
fi

# Make gradlew executable
chmod +x ./gradlew

print_header "Starting Desktop Build Process"

# Clean build if requested
if [ "$CLEAN_BUILD" = true ]; then
    print_info "Cleaning previous builds..."
    ./gradlew clean
    print_success "Clean completed"
fi

# Run tests if enabled
if [ "$RUN_TESTS" = true ]; then
    print_info "Running tests..."
    ./gradlew :composeApp:testClasses
    print_success "Tests completed"
fi

# Function to build specific package type
build_package() {
    local package_type=$1
    local task_name=""
    local output_dir=""
    local file_extension=""
    
    case $package_type in
        deb)
            task_name="packageDeb"
            output_dir="composeApp/build/compose/binaries/main/deb"
            file_extension=".deb"
            ;;
        msi)
            task_name="packageMsi"
            output_dir="composeApp/build/compose/binaries/main/msi"
            file_extension=".msi"
            ;;
        dmg)
            task_name="packageDmg"
            output_dir="composeApp/build/compose/binaries/main/dmg"
            file_extension=".dmg"
            ;;
        *)
            print_error "Unknown package type: $package_type"
            return 1
            ;;
    esac
    
    print_info "Building $package_type package..."
    
    if ./gradlew :composeApp:$task_name; then
        # Find the generated package
        if [ -d "$output_dir" ]; then
            local package_file=$(find "$output_dir" -name "*$file_extension" | head -1)
            if [ -n "$package_file" ]; then
                local file_size=$(du -h "$package_file" | cut -f1)
                print_success "$package_type package built successfully"
                print_info "📦 Package: $(basename "$package_file")"
                print_info "📏 Size: $file_size"
                print_info "📁 Location: $package_file"
                
                # Store package info for summary
                echo "$package_type:$package_file:$file_size" >> /tmp/dragme_build_summary.txt
            else
                print_warning "Package file not found in $output_dir"
            fi
        else
            print_warning "Output directory not found: $output_dir"
        fi
    else
        print_error "Failed to build $package_type package"
        return 1
    fi
}

# Initialize summary file
rm -f /tmp/dragme_build_summary.txt
touch /tmp/dragme_build_summary.txt

# Build packages based on type
case $BUILD_TYPE in
    all)
        print_info "Building all platform packages..."
        build_package "deb"
        build_package "msi" 
        build_package "dmg"
        ;;
    deb|msi|dmg)
        build_package "$BUILD_TYPE"
        ;;
esac

# Display build summary
print_header "Build Summary"

if [ -s /tmp/dragme_build_summary.txt ]; then
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                        📦 Build Results                       ║${NC}"
    echo -e "${CYAN}╠══════════════════════════════════════════════════════════════╣${NC}"
    
    while IFS=':' read -r type file size; do
        printf "${CYAN}║${NC} %-8s ${GREEN}%-35s${NC} ${YELLOW}%8s${NC} ${CYAN}║${NC}\n" \
               "[$type]" "$(basename "$file")" "$size"
    done < /tmp/dragme_build_summary.txt
    
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
    
    # Count successful builds
    local total_builds=$(wc -l < /tmp/dragme_build_summary.txt)
    print_success "Successfully built $total_builds package(s)"
    
    # Show platform-specific installation instructions
    echo ""
    print_header "Installation Instructions"
    
    while IFS=':' read -r type file size; do
        case $type in
            deb)
                echo -e "${GREEN}Linux (Ubuntu/Debian):${NC}"
                echo "  sudo dpkg -i \"$file\""
                echo "  sudo apt-get install -f  # Fix dependencies if needed"
                echo ""
                ;;
            msi)
                echo -e "${GREEN}Windows:${NC}"
                echo "  Double-click: \"$file\""
                echo "  Or via PowerShell: Start-Process \"$file\" -Wait"
                echo ""
                ;;
            dmg)
                echo -e "${GREEN}macOS:${NC}"
                echo "  hdiutil attach \"$file\""
                echo "  cp -R \"/Volumes/DragMe/DragMe.app\" \"/Applications/\""
                echo "  hdiutil detach \"/Volumes/DragMe\""
                echo ""
                ;;
        esac
    done < /tmp/dragme_build_summary.txt
    
else
    print_error "No packages were built successfully"
    exit 1
fi

# Open build directory if requested
if [ "$OPEN_BUILD_DIR" = true ]; then
    build_dir="composeApp/build/compose/binaries/main"
    if [ -d "$build_dir" ]; then
        print_info "Opening build directory..."
        case $CURRENT_PLATFORM in
            macOS)
                open "$build_dir"
                ;;
            Linux)
                if command -v xdg-open > /dev/null; then
                    xdg-open "$build_dir"
                elif command -v nautilus > /dev/null; then
                    nautilus "$build_dir"
                else
                    print_info "Build directory: $build_dir"
                fi
                ;;
            Windows)
                if command -v explorer.exe > /dev/null; then
                    explorer.exe "$build_dir"
                else
                    print_info "Build directory: $build_dir"
                fi
                ;;
            *)
                print_info "Build directory: $build_dir"
                ;;
        esac
    fi
fi

# Cleanup
rm -f /tmp/dragme_build_summary.txt

print_success "Desktop build process completed! 🎉"
echo ""
print_info "💡 Tips:"
echo "  • Test the package on your target platform before distribution"
echo "  • Use GitHub Actions for automated cross-platform builds"
echo "  • Check the docs/CICD_DESKTOP.md for CI/CD pipeline details"
echo ""
