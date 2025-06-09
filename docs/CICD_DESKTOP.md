# 🖥️ Desktop App CI/CD Pipeline

This document describes the automated CI/CD pipeline for building and publishing the DragMe desktop application to GitHub Packages.

## 🚀 Overview

The CI/CD pipeline automatically:
- **Builds** desktop applications for Linux, Windows, and macOS
- **Tests** the applications 
- **Publishes** packages to GitHub Packages
- **Creates** GitHub releases for tagged versions
- **Performs** security scans

## 📦 Supported Platforms

| Platform | Package Format | Target OS |
|----------|----------------|-----------|
| **Linux** | `.deb` | Ubuntu 18.04+ / Debian 10+ |
| **Windows** | `.msi` | Windows 10/11 |
| **macOS** | `.dmg` | macOS 11+ (Big Sur) |

## 🔄 Workflow Triggers

### Automatic Triggers
- **Push to main/master**: Builds and publishes snapshot versions
- **Tagged releases**: Builds and publishes release versions with GitHub Release
- **Pull requests**: Builds and tests (no publishing)

### Manual Triggers
- **workflow_dispatch**: Manual trigger with release type selection
  - `snapshot`: Development build
  - `release`: Production build

## 📋 Workflow Jobs

### 1. 🏗️ Build Desktop Apps (`build-desktop`)

**Matrix Strategy**: Builds on multiple OS simultaneously
- **Ubuntu Latest**: Builds `.deb` package
- **Windows Latest**: Builds `.msi` installer  
- **macOS Latest**: Builds `.dmg` disk image

**Steps**:
1. Checkout repository with full history
2. Setup Java 17 (Temurin distribution)
3. Setup Gradle with caching
4. Run tests (`testClasses`)
5. Build platform-specific package
6. Upload artifacts with 90-day retention

### 2. 📦 Publish Packages (`publish-packages`)

**Runs on**: Ubuntu Latest  
**Condition**: Only on main/master branch pushes or tags

**Steps**:
1. Download all built artifacts
2. Determine version (tag-based or snapshot)
3. Create distribution structure with manifest
4. Publish JAR to GitHub Packages Maven repository
5. Create GitHub Release (for tagged versions only)

### 3. 🔒 Security Scan (`security-scan`)

**Runs on**: Ubuntu Latest  
**Tools**: Trivy vulnerability scanner

**Steps**:
1. Scan filesystem for vulnerabilities
2. Upload results to GitHub Security tab
3. Generate SARIF report

## 🏷️ Versioning Strategy

### Release Versions (Tags)
- **Format**: `v1.0.0`, `v1.1.0-beta`, etc.
- **Trigger**: Git tags starting with `v`
- **Output**: Clean version number (e.g., `1.0.0`)
- **Publishing**: GitHub Packages + GitHub Release

### Snapshot Versions (Development)
- **Format**: `1.0.0-abc123-SNAPSHOT`
- **Trigger**: Pushes to main/master
- **Output**: Git describe + `-SNAPSHOT` suffix
- **Publishing**: GitHub Packages only

## 📦 GitHub Packages Integration

### Maven Repository
- **Repository**: `https://maven.pkg.github.com/OWNER/REPO`
- **Group ID**: `org.drag.me`
- **Artifact ID**: `dragme-desktop`
- **Authentication**: GitHub token required

### Package Metadata
```xml
<groupId>org.drag.me</groupId>
<artifactId>dragme-desktop</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>
```

## 🔧 Configuration

### Repository Secrets
No additional secrets required! The workflow uses:
- `GITHUB_TOKEN`: Automatically provided by GitHub Actions
- Standard GitHub Actions permissions

### Gradle Properties
The following properties can be set via command line or environment:

| Property | Description | Default |
|----------|-------------|---------|
| `version` | Package version | `1.0.0-SNAPSHOT` |
| `githubUsername` | GitHub username | `$GITHUB_ACTOR` |
| `githubToken` | GitHub token | `$GITHUB_TOKEN` |
| `github.repository` | Repository name | `dragme/dragme` |

## 📥 Installation Instructions

### From GitHub Packages (Maven)

Add to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/OWNER/REPO")
        credentials {
            username = "YOUR_GITHUB_USERNAME"
            password = "YOUR_GITHUB_TOKEN"
        }
    }
}

dependencies {
    implementation("org.drag.me:dragme-desktop:1.0.0")
}
```

### From GitHub Releases

1. Go to the [Releases page](../../releases)
2. Download the appropriate package for your OS:
   - **Linux**: `dragme_1.0.0_amd64.deb`
   - **Windows**: `DragMe-1.0.0.msi`
   - **macOS**: `DragMe-1.0.0.dmg`

### Installation Commands

**Linux (Ubuntu/Debian):**
```bash
wget https://github.com/OWNER/REPO/releases/download/v1.0.0/dragme_1.0.0_amd64.deb
sudo dpkg -i dragme_1.0.0_amd64.deb
sudo apt-get install -f  # Fix dependencies if needed
```

**Windows:**
```powershell
# Download and run the MSI installer
Invoke-WebRequest -Uri "https://github.com/OWNER/REPO/releases/download/v1.0.0/DragMe-1.0.0.msi" -OutFile "DragMe-1.0.0.msi"
Start-Process "DragMe-1.0.0.msi" -Wait
```

**macOS:**
```bash
# Download and mount DMG
wget https://github.com/OWNER/REPO/releases/download/v1.0.0/DragMe-1.0.0.dmg
hdiutil attach DragMe-1.0.0.dmg
cp -R "/Volumes/DragMe/DragMe.app" "/Applications/"
hdiutil detach "/Volumes/DragMe"
```

## 🔍 Monitoring and Debugging

### Workflow Status
- View workflow runs in the **Actions** tab
- Each job provides detailed logs and summaries
- Build artifacts are available for download

### Common Issues

**Build Failures:**
1. Check Java version compatibility
2. Verify Gradle configuration
3. Review test failures in logs

**Publishing Failures:**
1. Verify GitHub token permissions
2. Check repository package settings
3. Ensure correct version format

**Package Installation Issues:**
1. Check OS compatibility
2. Verify package integrity
3. Review installation logs

## 🚀 Usage Examples

### Triggering Builds

**Create a Release:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

**Manual Development Build:**
1. Go to Actions → Desktop Build & Publish
2. Click "Run workflow"
3. Select "snapshot" as release type
4. Click "Run workflow"

### Consuming Packages

**Gradle:**
```kotlin
dependencies {
    implementation("org.drag.me:dragme-desktop:1.0.0")
}
```

**Maven:**
```xml
<dependency>
    <groupId>org.drag.me</groupId>
    <artifactId>dragme-desktop</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🔗 Related Links

- [GitHub Packages Documentation](https://docs.github.com/en/packages)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [GitHub Actions](https://docs.github.com/en/actions)
- [Gradle Publishing](https://docs.gradle.org/current/userguide/publishing_maven.html)

## 🤝 Contributing

To improve the CI/CD pipeline:

1. Fork the repository
2. Create a feature branch
3. Modify workflow files in `.github/workflows/`
4. Test changes thoroughly
5. Submit a pull request

## 📄 License

This CI/CD configuration is part of the DragMe project and follows the same license terms.
