#!/usr/bin/env bash
#
# Tag a release using the version in gradle.properties, so the tag can never
# disagree with what the build produces. Pushing the tag triggers
# .github/workflows/release.yml, which builds, signs and publishes the APK.
#
# Usage: ./scripts/tag-release.sh

set -euo pipefail

cd "$(dirname "$0")/.."

read_prop() { grep "^$1=" gradle.properties | cut -d= -f2 | tr -d '[:space:]'; }

version="$(read_prop VERSION_MAJOR).$(read_prop VERSION_MINOR).$(read_prop VERSION_PATCH)"
tag="v$version"

if [ -n "$(git status --porcelain)" ]; then
  echo "工作目錄不乾淨。請先 commit 或 stash，否則 tag 指到的內容跟你手上的不一樣。" >&2
  exit 1
fi

if git rev-parse -q --verify "refs/tags/$tag" >/dev/null; then
  echo "tag $tag 已存在。要發新版請先提高 gradle.properties 裡的版本號。" >&2
  exit 1
fi

# Braces are required, not cosmetic: macOS bash 3.2 is not multibyte-aware and
# folds the leading byte of an adjacent full-width character into the variable
# name, so a bare $tag here resolves to an unset name and set -u aborts.
echo "準備發版 ${tag}（來自 gradle.properties）"
git tag -a "$tag" -m "Release $version"
git push origin "$tag"

remote_url=$(git remote get-url origin)
repo_path=$(echo "$remote_url" | sed -E 's#(git@github\.com:|https://github\.com/)##; s#\.git$##')

echo
echo "已推送 ${tag}。建置進度："
echo "  https://github.com/$repo_path/actions"
