#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/offline-classes
javac -Xlint:-options -source 8 -target 8 -d build/offline-classes src/main/java/br/veilchapter/core/*.java src/test/java/br/veilchapter/core/*.java
java -cp build/offline-classes br.veilchapter.core.OfflineCoreTests
java -cp build/offline-classes br.veilchapter.core.CinematicCoreTests
