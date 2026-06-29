#!/usr/bin/env bash
set -euo pipefail

SOURCE="$HOME/projects/com_r35157_nenjim-hubd-api"
TARGET="$HOME/projects/com_r35157_nenjim-hubd-api_github_snapshot"

rsync -a --delete \
  --exclude '.git' \
  "$SOURCE/" \
  "$TARGET/"

cd "$TARGET"
git add -A

if git diff --cached --quiet; then
    echo "No snapshot changes to publish."
    exit 0
fi

git commit -m "Mirror snapshot"
git push
