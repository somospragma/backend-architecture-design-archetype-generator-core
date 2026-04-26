#!/bin/bash
set -e

BASE_DIR="/tmp/archetype-tests"
rm -rf "$BASE_DIR"
mkdir -p "$BASE_DIR"

ARCHITECTURES=("hexagonal-single" "hexagonal-multi" "hexagonal-multi-granular")
PARADIGMS=("imperative" "reactive")

for arch in "${ARCHITECTURES[@]}"; do
  for paradigm in "${PARADIGMS[@]}"; do
    PROJECT_NAME="test-${arch}-${paradigm}"
    PROJECT_DIR="$BASE_DIR/$PROJECT_NAME"
    
    echo "============================================"
    echo "Creating project: $PROJECT_NAME"
    echo "  Architecture: $arch"
    echo "  Paradigm: $paradigm"
    echo "============================================"
    
    mkdir -p "$PROJECT_DIR"
    
    # Create settings.gradle.kts
    cat > "$PROJECT_DIR/settings.gradle.kts" << EOF
rootProject.name = "$PROJECT_NAME"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
EOF
    
    # Create build.gradle.kts with the plugin
    cat > "$PROJECT_DIR/build.gradle.kts" << EOF
plugins {
    id("co.com.pragma.archetype-generator") version "1.3.5-SNAPSHOT"
}

repositories {
    mavenLocal()
    mavenCentral()
}
EOF
    
    # Create .cleanarch.yml pointing to local templates
    TEMPLATES_PATH="$(cd "$(dirname "$0")/../.." && pwd)/backend-architecture-design-archetype-generator-templates/templates"
    cat > "$PROJECT_DIR/.cleanarch.yml" << EOF
templates:
  mode: local
  localPath: "$TEMPLATES_PATH"
EOF
    
    echo "  Running initCleanArch..."
    cd "$PROJECT_DIR"
    gradle initCleanArch \
      --architecture="$arch" \
      --paradigm="$paradigm" \
      --framework=spring \
      --package=com.pragma.test 2>&1 || {
      echo "  ❌ FAILED to initialize $PROJECT_NAME"
      continue
    }
    
    echo "  ✅ Project initialized: $PROJECT_NAME"
    echo ""
  done
done

echo ""
echo "All projects created in $BASE_DIR"
