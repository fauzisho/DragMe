# 🚀 Quick Start: CI/CD Desktop App Pipeline

Get your desktop app building and publishing automatically in under 5 minutes!

## ⚡ Quick Setup

### 1. **Enable GitHub Actions** (if not already enabled)
- Go to your repository → **Settings** → **Actions** → **General**
- Select "Allow all actions and reusable workflows"

### 2. **Enable GitHub Packages**
- Go to **Settings** → **Actions** → **General** → **Workflow permissions**
- Select "Read and write permissions"
- Check "Allow GitHub Actions to create and approve pull requests"

### 3. **Commit the CI/CD Files**
```bash
git add .github/workflows/desktop-build-publish.yml
git add docs/CICD_DESKTOP.md
git add scripts/build-desktop.sh
git commit -m "feat: Add desktop app CI/CD pipeline"
git push origin main
```

### 4. **Test the Pipeline**
- Go to **Actions** tab in your repository
- You should see "🖥️ Build & Publish Desktop App" workflow running
- Wait for it to complete (usually 5-10 minutes)

## 🎯 What You Get

### ✅ Automatic Builds
- **Linux**: `.deb` packages for Ubuntu/Debian
- **Windows**: `.msi` installers for Windows 10/11  
- **macOS**: `.dmg` disk images for macOS 11+

### ✅ Publishing
- **GitHub Packages**: Maven repository for JAR artifacts
- **GitHub Releases**: Direct download links for all platforms
- **Build Artifacts**: 90-day retention for development builds

### ✅ Quality Assurance
- Automated testing before builds
- Security vulnerability scanning
- Build status reporting

## 🏷️ Creating Your First Release

### Method 1: Git Tags (Recommended)
```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

### Method 2: Manual Trigger
1. Go to **Actions** → "🖥️ Build & Publish Desktop App"
2. Click **"Run workflow"**
3. Select branch and release type
4. Click **"Run workflow"**

## 📦 Where to Find Your Apps

### Development Builds (Snapshots)
- **Location**: Actions → Workflow Run → Artifacts
- **Retention**: 90 days
- **Format**: `DragMe-{Platform}-{Version}.zip`

### Release Builds (Tags)
- **Location**: Releases page (`/releases`)
- **Retention**: Permanent
- **Format**: Direct `.deb`, `.msi`, `.dmg` downloads

### Maven Packages
- **Location**: Packages tab (`/packages`)
- **Access**: `https://maven.pkg.github.com/OWNER/REPO`
- **Authentication**: GitHub token required

## 🔧 Local Development

### Build Script Usage
```bash
# Make script executable (one time)
chmod +x scripts/build-desktop.sh

# Build all platforms
./scripts/build-desktop.sh

# Build specific platform
./scripts/build-desktop.sh --type deb     # Linux
./scripts/build-desktop.sh --type msi     # Windows  
./scripts/build-desktop.sh --type dmg     # macOS

# Clean build with tests
./scripts/build-desktop.sh --clean

# Quick build without tests
./scripts/build-desktop.sh --no-tests --type native

# Build and open results
./scripts/build-desktop.sh --open
```

### Manual Gradle Commands
```bash
# Build all desktop packages
./gradlew :composeApp:packageDeb :composeApp:packageMsi :composeApp:packageDmg

# Build specific platform
./gradlew :composeApp:packageDeb      # Linux .deb
./gradlew :composeApp:packageMsi      # Windows .msi
./gradlew :composeApp:packageDmg      # macOS .dmg

# Run desktop app locally
./gradlew :composeApp:runDistributable
```

## 🎨 Customization

### Workflow Customization
Edit `.github/workflows/desktop-build-publish.yml`:

```yaml
# Change supported platforms
strategy:
  matrix:
    include:
      - os: ubuntu-latest    # Remove if you don't want Linux
      - os: windows-latest   # Remove if you don't want Windows
      - os: macos-latest     # Remove if you don't want macOS

# Change trigger conditions
on:
  push:
    branches: [ main ]       # Add/remove branches
    tags: [ 'v*' ]          # Change tag pattern
```

### Package Customization
Edit `composeApp/build.gradle.kts`:

```kotlin
compose.desktop {
    application {
        nativeDistributions {
            packageName = "YourAppName"           # Change app name
            packageVersion = "2.0.0"              # Change version
            description = "Your app description"   # Change description
            
            // Customize per platform
            linux {
                packageName = "your-app-name"
                debMaintainer = "you@example.com"
            }
            
            windows {
                packageName = "YourApp"
                upgradeUuid = "YOUR-UNIQUE-GUID"   # Generate new GUID
            }
            
            macOS {
                bundleID = "com.yourcompany.yourapp"
            }
        }
    }
}
```

## 🚨 Troubleshooting

### Common Issues

**❌ "No matching toolchains found"**
- Solution: The workflow uses Java 17 - ensure compatibility

**❌ "Package file not found"**
- Check Gradle build configuration
- Verify target platform is supported

**❌ "Permission denied" on publishing**
- Check repository permissions in Settings → Actions
- Ensure "Read and write permissions" is enabled

**❌ Build fails on specific platform**
- Platform-specific issues are isolated
- Other platforms will still build successfully

### Getting Help

1. **Check Workflow Logs**
   - Go to Actions → Failed workflow
   - Click on the failed job
   - Review detailed logs

2. **Local Testing**
   ```bash
   # Test the exact same commands locally
   ./scripts/build-desktop.sh --type deb
   ```

3. **Debug Mode**
   - Add `enableBuildLogging: true` to workflow for verbose output

## 🎉 Success!

Your desktop app is now:
- ✅ Building automatically on every push
- ✅ Publishing to GitHub Packages
- ✅ Creating releases for tags
- ✅ Supporting Linux, Windows, and macOS
- ✅ Including security scans

## 🔗 Next Steps

- [📖 Read the full documentation](CICD_DESKTOP.md)
- [🐛 Report issues](../issues)
- [💡 Request features](../issues)
- [🤝 Contribute improvements](../pulls)

## 📊 Status Badges

Add these to your README.md:

```markdown
[![Desktop Build](https://github.com/OWNER/REPO/actions/workflows/desktop-build-publish.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/desktop-build-publish.yml)
[![GitHub release](https://img.shields.io/github/v/release/OWNER/REPO)](https://github.com/OWNER/REPO/releases/latest)
[![GitHub packages](https://img.shields.io/badge/GitHub-Packages-blue)](https://github.com/OWNER/REPO/packages)
```

---

**🎯 Pro Tip**: Star this repository and watch for updates to get the latest CI/CD improvements!
