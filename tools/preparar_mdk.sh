#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ ! -f gradlew || ! -f gradle/wrapper/gradle-wrapper.jar ]]; then
  archive="$(mktemp --suffix=.zip)"; dir="$(mktemp -d)"
  trap 'rm -f "$archive"; rm -rf "$dir"' EXIT
  curl --fail --location --retry 2 --max-time 120 --silent --show-error 'https://maven.minecraftforge.net/net/minecraftforge/forge/1.12.2-14.23.5.2859/forge-1.12.2-14.23.5.2859-mdk.zip' -o "$archive"
  echo '347338fc96359640c3d9673438db26742bd658d5  '"$archive" | sha1sum -c -
  unzip -qq "$archive" 'gradlew' 'gradlew.bat' 'gradle/wrapper/*' -d "$dir"
  mkdir -p gradle/wrapper
  cp "$dir/gradlew" "$dir/gradlew.bat" .
  cp "$dir/gradle/wrapper/"* gradle/wrapper/
fi
cat > gradle/wrapper/gradle-wrapper.properties <<'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-7.3.3-bin.zip
EOF
chmod +x gradlew
echo 'Wrapper configurado: Gradle 7.3.3'
