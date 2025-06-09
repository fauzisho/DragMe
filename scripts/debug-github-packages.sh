#!/bin/bash

# Debug script for GitHub Packages publishing issues
# This script helps diagnose common GitHub Packages publishing problems

set -e

echo "🔍 GitHub Packages Publishing Debug Script"
echo "=========================================="

# Check environment variables
echo "📋 Environment Check:"
echo "GITHUB_TOKEN: ${GITHUB_TOKEN:+SET}"
echo "GITHUB_ACTOR: ${GITHUB_ACTOR:-NOT_SET}"
echo "GITHUB_REPOSITORY: ${GITHUB_REPOSITORY:-NOT_SET}"

# Check if we're in a GitHub Actions environment
if [ -n "$GITHUB_ACTIONS" ]; then
    echo "🏃 Running in GitHub Actions: YES"
else
    echo "🏃 Running in GitHub Actions: NO"
fi

# Generate a unique version for testing
TIMESTAMP=$(date +%s)
TEST_VERSION="1.0.0-debug-${TIMESTAMP}-SNAPSHOT"
echo "🏷️ Test Version: $TEST_VERSION"

# Test repository access
echo ""
echo "🌐 Testing GitHub Packages Repository Access:"
REPO_URL="https://maven.pkg.github.com/dragme/dragme"
echo "Repository URL: $REPO_URL"

if command -v curl >/dev/null 2>&1; then
    echo "Testing repository accessibility..."
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: token ${GITHUB_TOKEN}" "$REPO_URL")
    echo "HTTP Response Code: $HTTP_CODE"
    
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "404" ]; then
        echo "✅ Repository is accessible"
    else
        echo "❌ Repository access issue (HTTP $HTTP_CODE)"
    fi
else
    echo "⚠️ curl not available, skipping repository test"
fi

echo ""
echo "🔧 Gradle Configuration:"
echo "Gradle version:"
./gradlew --version | head -5

echo ""
echo "📦 Testing Gradle Build:"
echo "Running gradle clean..."
./gradlew clean

echo ""
echo "📝 Testing Publication Configuration:"
echo "Listing available publications..."
./gradlew :composeApp:publications

echo ""
echo "🚀 Attempting to build (without publish):"
./gradlew :composeApp:build -Pversion="$TEST_VERSION" --info

echo ""
echo "🎯 Dry run - Generate publication artifacts:"
./gradlew :composeApp:generatePomFileForKotlinMultiplatformPublication -Pversion="$TEST_VERSION"

echo ""
echo "📋 Generated artifacts:"
find composeApp/build -name "*.jar" -o -name "*.pom" | head -10

echo ""
echo "✅ Debug script completed!"
echo "If everything looks good above, try running the actual publish command:"
echo "./gradlew :composeApp:publish -Pversion=\"$TEST_VERSION\" -PgithubToken=\"\$GITHUB_TOKEN\" -PgithubUsername=\"\$GITHUB_ACTOR\""
