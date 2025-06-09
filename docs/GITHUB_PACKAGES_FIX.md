# GitHub Packages Publishing Fix

## Problem
The GitHub Actions workflow was failing during the "Build and Publish JAR to Github Packages" phase with error:
```
Could not PUT 'https://maven.pkg.github.com/dragme/dragme/DragMe/composeApp/969aeba-SNAPSHOT/composeApp-969aeba-20250609.133227-1.jar'. Received status code 422 from server: Unprocessable Entity
```

## Root Cause
The error 422 (Unprocessable Entity) from GitHub Packages typically occurs due to:

1. **Version Collision**: Attempting to publish the same SNAPSHOT version multiple times
2. **Invalid Version Format**: The version `969aeba-SNAPSHOT` was just a commit hash, which is not a proper semantic version
3. **Artifact Naming**: Potential conflicts with existing package names

## Solutions Implemented

### 1. Fixed Version Generation
**Before:**
```kotlin
"$baseVersion-$buildNumber-$commitHash-SNAPSHOT"
```

**After:**
```kotlin
"$baseVersion-$buildNumber-$commitHash-$timestamp-SNAPSHOT"
```

This ensures each build gets a unique version with timestamp to prevent collisions.

### 2. Updated GitHub Workflow Versioning
**Before:**
```bash
VERSION="${BASE_VERSION}-${BUILD_NUMBER}-${COMMIT_HASH:0:7}-SNAPSHOT"
```

**After:**
```bash
TIMESTAMP=$(date +%s)
VERSION="${BASE_VERSION}-${BUILD_NUMBER}-${COMMIT_HASH:0:7}-${TIMESTAMP}-SNAPSHOT"
```

### 3. Improved Error Handling & Debugging
- Added more detailed logging in the GitHub workflow
- Added `--no-daemon` flag to prevent Gradle daemon issues
- Added token existence check
- Created debug script for troubleshooting

### 4. Maintained Proper Artifact Naming
```kotlin
artifactId = when (name) {
    "kotlinMultiplatform" -> "dragme-multiplatform"
    "desktop" -> "dragme-desktop"
    "android" -> "dragme-android"
    "wasmJs" -> "dragme-wasmjs"
    else -> "dragme-${name.lowercase()}"
}
```

## Testing the Fix

1. **Local Testing:**
   ```bash
   ./scripts/debug-github-packages.sh
   ```

2. **Manual Publish Test:**
   ```bash
   ./gradlew :composeApp:publish -Pversion="1.0.0-test-$(date +%s)-SNAPSHOT"
   ```

3. **CI/CD Testing:**
   Push to main branch and monitor the GitHub Actions workflow.

## Expected Behavior After Fix

1. ✅ Unique versions for each build preventing collisions
2. ✅ Proper semantic versioning format
3. ✅ Successful publication to GitHub Packages
4. ✅ Desktop builds continue to work (unaffected)
5. ✅ Better error messages and debugging information

## Alternative Solutions (if issue persists)

If the 422 error continues, consider:

1. **Change Repository URL**: Use a different package name or repository
2. **Use Release Versions**: Only publish tagged releases, not snapshots
3. **Manual Package Cleanup**: Delete conflicting packages from GitHub Packages UI
4. **Different Publication Strategy**: Publish only specific targets instead of all

## Verification

After applying the fix, verify:
- [ ] GitHub Actions workflow completes successfully
- [ ] Packages appear in GitHub Packages registry
- [ ] Desktop builds still work correctly
- [ ] No version conflicts in subsequent runs
