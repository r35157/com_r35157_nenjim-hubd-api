#!/usr/bin/env bash
set -euo pipefail

export VERSION=$(git describe --tags --exact-match 2>/dev/null \
  || git symbolic-ref --short -q HEAD \
  || git rev-parse --short HEAD)

echo "Building and publishing (CI/CD) $VERSION..."
./gradlew -Pversion=$VERSION publish
