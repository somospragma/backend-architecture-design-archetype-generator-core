#!/bin/bash

# Integration Test Script for Task 25.1
# Tests complete workflow: init Onion project, generate multiple adapters, verify build succeeds

set -e  # Exit on error

echo "========================================="
echo "Task 25.1: Complete Workflow Integration Test"
echo "========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test project name
PROJECT_NAME="test-onion-project"
TEST_DIR="../test-onion-integration"

# Create test directory
mkdir -p "$TEST_DIR"
cd "$TEST_DIR"

# Clean up any previous test
echo "Cleaning up previous test..."
rm -rf "$PROJECT_NAME"

# Step 1: Initialize Onion architecture project
echo ""
echo "${YELLOW}Step 1: Initializing Onion architecture project...${NC}"
mkdir -p "$PROJECT_NAME"
cd "$PROJECT_NAME"

# Create build.gradle to apply the plugin
cat > build.gradle << 'EOF'
plugins {
    id 'java'
    id 'com.pragma.archetype-generator' version '0.1.15-SNAPSHOT'
}

group = 'com.example'
version = '1.0.0'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux:3.2.0'
    testImplementation 'org.springframework.boot:spring-boot-starter-test:3.2.0'
}
EOF

# Create settings.gradle with plugin repository
cat > settings.gradle << 'EOF'
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
EOF

cat >> settings.gradle << EOF
rootProject.name = '$PROJECT_NAME'
EOF

# Run initCleanArch task
echo "Running initCleanArch task..."
../../backend-architecture-design-archetype-generator-core/gradlew initCleanArch \
    --packageName="com.example.onion" \
    --architecture="onion-single" \
    --paradigm="reactive" \
    --framework="spring"

if [ $? -eq 0 ]; then
    echo "${GREEN}✓ Project initialized successfully${NC}"
else
    echo "${RED}✗ Project initialization failed${NC}"
    exit 1
fi

# Verify project structure
echo ""
echo "${YELLOW}Verifying project structure...${NC}"
EXPECTED_DIRS=(
    "src/main/java/com/example/onion/core/domain"
    "src/main/java/com/example/onion/core/application/service"
    "src/main/java/com/example/onion/core/application/port"
    "src/main/java/com/example/onion/infrastructure/adapter/in"
    "src/main/java/com/example/onion/infrastructure/adapter/out"
)

for dir in "${EXPECTED_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "${GREEN}✓ $dir exists${NC}"
    else
        echo "${RED}✗ $dir missing${NC}"
        exit 1
    fi
done

# Verify .cleanarch.yml was created
if [ -f ".cleanarch.yml" ]; then
    echo "${GREEN}✓ .cleanarch.yml created${NC}"
    echo "Configuration:"
    cat .cleanarch.yml
else
    echo "${RED}✗ .cleanarch.yml missing${NC}"
    exit 1
fi

# Step 2: Generate MongoDB adapter
echo ""
echo "${YELLOW}Step 2: Generating MongoDB adapter...${NC}"
../../backend-architecture-design-archetype-generator-core/gradlew generateOutputAdapter \
    --name="mongodb" \
    --type="mongodb" \
    --entity="User"

if [ $? -eq 0 ]; then
    echo "${GREEN}✓ MongoDB adapter generated successfully${NC}"
else
    echo "${RED}✗ MongoDB adapter generation failed${NC}"
    exit 1
fi

# Verify MongoDB adapter files
echo "Verifying MongoDB adapter files..."
if [ -d "src/main/java/com/example/onion/infrastructure/adapter/out/mongodb" ]; then
    echo "${GREEN}✓ MongoDB adapter directory created${NC}"
    ls -la src/main/java/com/example/onion/infrastructure/adapter/out/mongodb/
else
    echo "${RED}✗ MongoDB adapter directory missing${NC}"
    exit 1
fi

# Step 3: Generate Redis adapter
echo ""
echo "${YELLOW}Step 3: Generating Redis adapter...${NC}"
../../backend-architecture-design-archetype-generator-core/gradlew generateOutputAdapter \
    --name="redis" \
    --type="redis" \
    --entity="Cache"

if [ $? -eq 0 ]; then
    echo "${GREEN}✓ Redis adapter generated successfully${NC}"
else
    echo "${RED}✗ Redis adapter generation failed${NC}"
    exit 1
fi

# Verify Redis adapter files
echo "Verifying Redis adapter files..."
if [ -d "src/main/java/com/example/onion/infrastructure/adapter/out/redis" ]; then
    echo "${GREEN}✓ Redis adapter directory created${NC}"
    ls -la src/main/java/com/example/onion/infrastructure/adapter/out/redis/
else
    echo "${RED}✗ Redis adapter directory missing${NC}"
    exit 1
fi

# Step 4: Generate REST controller adapter
echo ""
echo "${YELLOW}Step 4: Generating REST controller adapter...${NC}"
../../backend-architecture-design-archetype-generator-core/gradlew generateInputAdapter \
    --name="rest" \
    --type="rest" \
    --useCase="GetUser"

if [ $? -eq 0 ]; then
    echo "${GREEN}✓ REST controller adapter generated successfully${NC}"
else
    echo "${RED}✗ REST controller adapter generation failed${NC}"
    exit 1
fi

# Verify REST adapter files
echo "Verifying REST adapter files..."
if [ -d "src/main/java/com/example/onion/infrastructure/adapter/in/rest" ]; then
    echo "${GREEN}✓ REST adapter directory created${NC}"
    ls -la src/main/java/com/example/onion/infrastructure/adapter/in/rest/
else
    echo "${RED}✗ REST adapter directory missing${NC}"
    exit 1
fi

# Step 5: Verify application.yml was updated
echo ""
echo "${YELLOW}Step 5: Verifying application.yml configuration...${NC}"
if [ -f "src/main/resources/application.yml" ]; then
    echo "${GREEN}✓ application.yml exists${NC}"
    echo "Configuration:"
    cat src/main/resources/application.yml
else
    echo "${YELLOW}⚠ application.yml not found (may not be required)${NC}"
fi

# Step 6: Verify build.gradle was updated with dependencies
echo ""
echo "${YELLOW}Step 6: Verifying build.gradle dependencies...${NC}"
if grep -q "spring-boot-starter-data-mongodb-reactive" build.gradle; then
    echo "${GREEN}✓ MongoDB dependency added${NC}"
else
    echo "${YELLOW}⚠ MongoDB dependency not found in build.gradle${NC}"
fi

if grep -q "spring-boot-starter-data-redis-reactive" build.gradle; then
    echo "${GREEN}✓ Redis dependency added${NC}"
else
    echo "${YELLOW}⚠ Redis dependency not found in build.gradle${NC}"
fi

# Step 7: Attempt to build the project
echo ""
echo "${YELLOW}Step 7: Building the project...${NC}"
../../backend-architecture-design-archetype-generator-core/gradlew build -x test

if [ $? -eq 0 ]; then
    echo "${GREEN}✓ Project built successfully${NC}"
else
    echo "${RED}✗ Project build failed${NC}"
    echo "Build output:"
    ../../backend-architecture-design-archetype-generator-core/gradlew build -x test --stacktrace
    exit 1
fi

# Summary
echo ""
echo "========================================="
echo "${GREEN}Integration Test Summary${NC}"
echo "========================================="
echo "${GREEN}✓ Onion project initialized${NC}"
echo "${GREEN}✓ MongoDB adapter generated${NC}"
echo "${GREEN}✓ Redis adapter generated${NC}"
echo "${GREEN}✓ REST controller adapter generated${NC}"
echo "${GREEN}✓ Project structure verified${NC}"
echo "${GREEN}✓ Project builds successfully${NC}"
echo ""
echo "${GREEN}All tests passed!${NC}"
echo ""
