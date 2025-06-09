#!/bin/bash

echo "🔍 GitHub Packages Publishing Debug Script"
echo "=========================================="

# Check environment variables
echo "📋 Environment Variables:"
echo "GITHUB_TOKEN: ${GITHUB_TOKEN:0:10}..." 
echo "GITHUB_ACTOR: $GITHUB_ACTOR"
echo "GITHUB_REPOSITORY: $GITHUB_REPOSITORY"

# Check repository name format
REPO_NAME=$(echo $GITHUB_REPOSITORY | cut -d'/' -f2)
REPO_OWNER=$(echo $GITHUB_REPOSITORY | cut -d'/' -f1)

echo ""
echo "📦 Repository Info:"
echo "Owner: $REPO_OWNER"
echo "Repository: $REPO_NAME"
echo "Expected URL: https://maven.pkg.github.com/$REPO_OWNER/$REPO_NAME"

# Validate the URL format
echo ""
echo "🔗 URL Validation:"
if [[ "$REPO_OWNER" == "dragme" && "$REPO_NAME" == "dragme" ]]; then
    echo "✅ Repository URL format looks correct"
else
    echo "⚠️  Repository name mismatch detected!"
    echo "   Expected: dragme/dragme"
    echo "   Actual: $REPO_OWNER/$REPO_NAME"
fi

# Check for existing packages
echo ""
echo "📋 Checking existing packages..."
if [ ! -z "$GITHUB_TOKEN" ]; then
    echo "Making API call to check existing packages..."
    curl -s -H "Authorization: token $GITHUB_TOKEN" \
         -H "Accept: application/vnd.github.v3+json" \
         "https://api.github.com/users/$REPO_OWNER/packages?package_type=maven" | \
         jq -r '.[] | select(.name | contains("dragme")) | {name: .name, repository: .repository.name}'
else
    echo "❌ GITHUB_TOKEN not set, cannot check existing packages"
fi

echo ""
echo "🚀 Ready to debug publishing issues!"
