# GitHub Packages Publishing Fix - Summary

## Issue
GitHub Actions workflow failing with error 422 (Unprocessable Entity) when publishing to GitHub Packages:
```
Could not PUT 'https://maven.pkg.github.com/dragme/dragme/DragMe/composeApp/969aeba-SNAPSHOT/composeApp-969aeba-20250609.133227-1.jar'. Received status code 422 from server: Unprocessable Entity
```

## Root Causes Identified
1. **Version Conflicts**: SNAPSHOT versions being overwritten
2. **Invalid Version Format**: Using only commit hash as version
3. **Multiple Publication Conflicts**: All Kotlin Multiplatform targets being published simultaneously

## Changes Made

### 1. Fixed Version Generation (composeApp/build.gradle.kts)
**Before:**
```kotlin
"$baseVersion-$buildNumber-$commitHash-SNAPSHOT"
```
**After:**
```kotlin
"$baseVersion-$buildNumber-$commitHash-$timestamp-SNAPSHOT"
```

### 2. Simplified Publishing Configuration
**Changed from:** Publishing all target publications (kotlinMultiplatform, desktop, android, wasmJs)
**Changed to:** Publishing only the kotlinMultiplatform publication to avoid conflicts

### 3. Updated GitHub Workflow (.github/workflows/desktop-build-publish.yml)
- Added timestamp to version generation
- Changed from generic `publish` task to specific `publishKotlinMultiplatformPublicationToGitHubPackagesRepository`
- Added better debugging and error information
- Added `--no-daemon` flag to prevent Gradle daemon issues

### 4. Added Debugging Tools
- Created `scripts/debug-github-packages.sh` for local troubleshooting
- Added publication listing in workflow for better visibility

## Key Changes Summary

### File: `composeApp/build.gradle.kts`
```kotlin
// Version generation with timestamp for uniqueness
val timestamp = System.currentTimeMillis()
"$baseVersion-$buildNumber-$commitHash-$timestamp-SNAPSHOT"

// Simplified publishing - only kotlinMultiplatform publication
named("kotlinMultiplatform") {
    groupId = "org.drag.me"
    artifactId = "dragme-multiplatform"
    version = buildVersion
    // ... pom configuration
}
```

### File: `.github/workflows/desktop-build-publish.yml`
```bash
# Version with timestamp
TIMESTAMP=$(date +%s)
VERSION="${BASE_VERSION}-${BUILD_NUMBER}-${COMMIT_HASH:0:7}-${TIMESTAMP}-SNAPSHOT"

# Specific publication task
./gradlew :composeApp:publishKotlinMultiplatformPublicationToGitHubPackagesRepository
```

## Expected Results
✅ **Unique versions** prevent conflicts
✅ **Single publication** reduces complexity  
✅ **Better error handling** for debugging
✅ **Successful GitHub Packages publishing**
✅ **Desktop builds remain unaffected**

## Testing Instructions

### 1. Local Testing
```bash
# Make the debug script executable
chmod +x scripts/debug-github-packages.sh

# Run debug script
./scripts/debug-github-packages.sh
```

### 2. Manual Publish Test
```bash
# Test with unique version
./gradlew :composeApp:publishKotlinMultiplatformPublicationToGitHubPackagesRepository \
  -Pversion="1.0.0-test-$(date +%s)-SNAPSHOT"
```

### 3. Verify GitHub Actions
1. Push changes to main branch
2. Monitor workflow execution
3. Check GitHub Packages registry for published artifacts

## Backup Plan
If issues persist, the workflow can be temporarily modified to skip publishing:
```yaml
# Add condition to skip publishing if needed
if: false  # Temporarily disable publishing
```

## Files Modified
- `composeApp/build.gradle.kts` - Version generation and publishing config
- `.github/workflows/desktop-build-publish.yml` - Workflow improvements
- `scripts/debug-github-packages.sh` - Debug tool (new)
- `docs/GITHUB_PACKAGES_FIX.md` - Detailed documentation (new)
- `docs/PUBLISH_FIX_SUMMARY.md` - This summary (new)

## Next Steps
1. Test the changes by pushing to main branch
2. Monitor the GitHub Actions workflow execution
3. Verify packages appear in GitHub Packages registry
4. If successful, document the solution for future reference
5. If issues persist, investigate alternative publishing strategies
