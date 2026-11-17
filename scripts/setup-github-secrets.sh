#!/usr/bin/env bash
# One-time: copies Birdie's signing key into your GitHub repo's secrets so GitHub can build updates.
# Needs the GitHub CLI (https://cli.github.com) and `gh auth login`. Run from the project folder.
set -euo pipefail
cd "$(dirname "$0")/.."
props=android/keystore.properties
[ -f keys/birdie-release.jks ] && [ -f "$props" ] || { echo "Missing keys/birdie-release.jks or $props"; exit 1; }
get() { grep "^$1=" "$props" | cut -d= -f2-; }
base64 < keys/birdie-release.jks | tr -d '\n' | gh secret set BIRDIE_KEYSTORE_BASE64
get storePassword | gh secret set BIRDIE_STORE_PASSWORD
get keyAlias      | gh secret set BIRDIE_KEY_ALIAS
get keyPassword   | gh secret set BIRDIE_KEY_PASSWORD
echo "Done. GitHub can now build signed Birdie updates."
