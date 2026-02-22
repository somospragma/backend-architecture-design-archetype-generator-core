#!/bin/bash

# Task 25.4: Test YAML Merging with Multiple Adapters
# This script tests Requirement 5 (Intelligent Application Properties Merge)

set -e  # Exit on error

echo "========================================="
echo "Task 25.4: YAML Merging Test"
echo "========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test project directory
TEST_DIR="../test-yaml-merge"
PROJECT_NAME="test-yaml-merge"

# Clean up previous test
echo "Cleaning up previous test..."
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"
cd "$TEST_DIR"

echo ""
echo "========================================="
echo "Step 1: Initialize Test Project"
echo "========================================="

# Create initial project structure
mkdir -p src/main/java/com/example/yamltest
mkdir -p src/main/resources

# Create initial application.yml with some existing properties
cat > src/main/resources/application.yml << 'EOF'
spring:
  application:
    name: test-yaml-merge
  profiles:
    active: dev
server:
  port: 8080
EOF

echo "Created initial application.yml with existing properties:"
cat src/main/resources/application.yml
echo ""

# Create .cleanarch.yml configuration
cat > .cleanarch.yml << 'EOF'
project:
  name: test-yaml-merge
  basePackage: com.example.yamltest
  pluginVersion: 0.1.15-SNAPSHOT
  createdAt: "2024-02-22T14:00:00"

architecture:
  type: onion-single
  paradigm: reactive
  framework: spring
  adaptersAsModules: false

templates:
  mode: developer
  localPath: ../backend-architecture-design-archetype-generator-templates/templates
  cache: false
EOF

echo "Created .cleanarch.yml configuration"
echo ""

# Create build.gradle
cat > build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'com.pragma.archetype-generator' version '0.1.15-SNAPSHOT'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
EOF

# Create settings.gradle
cat > settings.gradle << 'EOF'
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

rootProject.name = 'test-yaml-merge'
EOF

echo "Created build files"
echo ""

echo "========================================="
echo "Step 2: Generate MongoDB Adapter"
echo "========================================="

# Save application.yml before MongoDB generation
cp src/main/resources/application.yml src/main/resources/application.yml.before-mongodb

# Generate MongoDB adapter
echo "Generating MongoDB adapter..."
gradle generateOutputAdapter --name=UserRepository --entity=User --type=mongodb --stacktrace

echo ""
echo "Application.yml after MongoDB adapter:"
cat src/main/resources/application.yml
echo ""

# Verify MongoDB properties were added
if grep -q "mongodb" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ MongoDB properties added${NC}"
else
    echo -e "${RED}✗ MongoDB properties NOT found${NC}"
    exit 1
fi

# Verify existing properties preserved
if grep -q "server:" src/main/resources/application.yml && grep -q "port: 8080" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ Existing server properties preserved${NC}"
else
    echo -e "${RED}✗ Existing server properties NOT preserved${NC}"
    exit 1
fi

if grep -q "profiles:" src/main/resources/application.yml && grep -q "active: dev" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ Existing profile properties preserved${NC}"
else
    echo -e "${RED}✗ Existing profile properties NOT preserved${NC}"
    exit 1
fi

echo ""
echo "========================================="
echo "Step 3: Generate Redis Adapter"
echo "========================================="

# Save application.yml before Redis generation
cp src/main/resources/application.yml src/main/resources/application.yml.before-redis

# Generate Redis adapter
echo "Generating Redis adapter..."
gradle generateOutputAdapter --name=CacheRepository --entity=Cache --type=redis --stacktrace

echo ""
echo "Application.yml after Redis adapter:"
cat src/main/resources/application.yml
echo ""

# Verify Redis properties were added
if grep -q "redis" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ Redis properties added${NC}"
else
    echo -e "${RED}✗ Redis properties NOT found${NC}"
    exit 1
fi

# Verify MongoDB properties still exist
if grep -q "mongodb" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ MongoDB properties preserved after Redis generation${NC}"
else
    echo -e "${RED}✗ MongoDB properties LOST after Redis generation${NC}"
    exit 1
fi

# Verify original properties still exist
if grep -q "server:" src/main/resources/application.yml && grep -q "port: 8080" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ Original server properties still preserved${NC}"
else
    echo -e "${RED}✗ Original server properties LOST${NC}"
    exit 1
fi

echo ""
echo "========================================="
echo "Step 4: Test Property Conflict Detection"
echo "========================================="

# Manually add a MongoDB URI to test conflict detection
echo "Adding conflicting MongoDB URI..."
sed -i.bak 's|uri: mongodb://localhost:27017/test-yaml-merge|uri: mongodb://production:27017/test-yaml-merge|' src/main/resources/application.yml

echo "Modified application.yml with conflicting URI:"
cat src/main/resources/application.yml
echo ""

# Save application.yml before conflict test
cp src/main/resources/application.yml src/main/resources/application.yml.before-conflict

# Try to generate another MongoDB adapter (should detect conflict)
echo "Generating another MongoDB adapter to test conflict detection..."
gradle generateOutputAdapter --name=OrderRepository --entity=Order --type=mongodb --stacktrace 2>&1 | tee mongodb-conflict.log

echo ""
echo "Checking for conflict warnings in output..."
if grep -qi "conflict\|warning\|already exists" mongodb-conflict.log; then
    echo -e "${GREEN}✓ Conflict detection working (warnings found)${NC}"
else
    echo -e "${YELLOW}⚠ No conflict warnings found (may need to check implementation)${NC}"
fi

# Verify the production URI was NOT overwritten
if grep -q "uri: mongodb://production:27017/test-yaml-merge" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ Existing MongoDB URI preserved (not overwritten)${NC}"
else
    echo -e "${RED}✗ MongoDB URI was overwritten${NC}"
    exit 1
fi

echo ""
echo "========================================="
echo "Step 5: Verify YAML Structure"
echo "========================================="

echo "Final application.yml structure:"
cat src/main/resources/application.yml
echo ""

# Check YAML is valid
if python3 -c "import yaml; yaml.safe_load(open('src/main/resources/application.yml'))" 2>/dev/null; then
    echo -e "${GREEN}✓ YAML structure is valid${NC}"
else
    echo -e "${RED}✗ YAML structure is INVALID${NC}"
    exit 1
fi

# Check indentation (should be 2 spaces)
if grep -q "^  [^ ]" src/main/resources/application.yml && ! grep -q "^   [^ ]" src/main/resources/application.yml; then
    echo -e "${GREEN}✓ YAML indentation is correct (2 spaces)${NC}"
else
    echo -e "${YELLOW}⚠ YAML indentation may not be consistent${NC}"
fi

# Check properties are grouped
echo ""
echo "Checking property grouping..."
if grep -A 5 "mongodb:" src/main/resources/application.yml | grep -q "database:\|uri:\|auto-index-creation:"; then
    echo -e "${GREEN}✓ MongoDB properties are grouped together${NC}"
else
    echo -e "${YELLOW}⚠ MongoDB properties may not be properly grouped${NC}"
fi

if grep -A 3 "redis:" src/main/resources/application.yml | grep -q "host:\|port:"; then
    echo -e "${GREEN}✓ Redis properties are grouped together${NC}"
else
    echo -e "${YELLOW}⚠ Redis properties may not be properly grouped${NC}"
fi

echo ""
echo "========================================="
echo "Step 6: Verify All Properties Present"
echo "========================================="

# Create a checklist of expected properties
declare -a expected_properties=(
    "spring.application.name"
    "spring.profiles.active"
    "server.port"
    "spring.data.mongodb"
    "spring.data.redis"
)

echo "Verifying all expected properties are present:"
all_present=true
for prop in "${expected_properties[@]}"; do
    # Convert dot notation to YAML path check
    if echo "$prop" | grep -q "\."; then
        # For nested properties, just check the parent exists
        parent=$(echo "$prop" | cut -d. -f1-2)
        if grep -q "${parent//./:}" src/main/resources/application.yml || grep -q "$(echo $parent | sed 's/\./:/g')" src/main/resources/application.yml; then
            echo -e "  ${GREEN}✓${NC} $prop"
        else
            echo -e "  ${RED}✗${NC} $prop"
            all_present=false
        fi
    else
        if grep -q "$prop:" src/main/resources/application.yml; then
            echo -e "  ${GREEN}✓${NC} $prop"
        else
            echo -e "  ${RED}✗${NC} $prop"
            all_present=false
        fi
    fi
done

if [ "$all_present" = true ]; then
    echo -e "${GREEN}✓ All expected properties are present${NC}"
else
    echo -e "${RED}✗ Some properties are missing${NC}"
    exit 1
fi

echo ""
echo "========================================="
echo "Test Summary"
echo "========================================="
echo ""
echo "Test Results:"
echo "  ✓ Initial application.yml created with existing properties"
echo "  ✓ MongoDB adapter generated and properties merged"
echo "  ✓ Redis adapter generated and properties merged"
echo "  ✓ Existing properties preserved through all generations"
echo "  ✓ Property conflicts detected (or existing values preserved)"
echo "  ✓ YAML structure is valid"
echo "  ✓ All expected properties are present"
echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}Task 25.4: YAML Merging Test PASSED${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "Test artifacts saved in: $TEST_DIR"
echo "  - application.yml.before-mongodb"
echo "  - application.yml.before-redis"
echo "  - application.yml.before-conflict"
echo "  - application.yml (final)"
echo ""
