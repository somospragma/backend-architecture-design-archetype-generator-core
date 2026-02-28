# Pre-commit Setup Guide

This project uses [pre-commit](https://pre-commit.com/) to ensure code quality before commits.

## Installation

### 1. Install pre-commit

**Using pip:**
```bash
pip install pre-commit
```

**Using Homebrew (macOS):**
```bash
brew install pre-commit
```

**Using conda:**
```bash
conda install -c conda-forge pre-commit
```

### 2. Install the git hooks

From the project root directory:
```bash
pre-commit install
```

## Usage

### Automatic execution

Once installed, pre-commit will run automatically on every `git commit`.

### Manual execution

Run on all files:
```bash
pre-commit run --all-files
```

Run on specific files:
```bash
pre-commit run --files src/main/java/com/pragma/archetype/MyClass.java
```

## Configured Hooks

1. **trailing-whitespace** - Removes trailing whitespace
2. **end-of-file-fixer** - Ensures files end with a newline
3. **check-yaml** - Validates YAML syntax
4. **check-added-large-files** - Prevents committing large files (>500KB)
5. **check-merge-conflict** - Detects merge conflict markers
6. **spotless-apply** - Formats Java code automatically
7. **compile** - Compiles Java code to catch syntax errors

## Skipping Hooks

If you need to skip hooks (not recommended):
```bash
git commit --no-verify
```

## Updating Hooks

To update to the latest versions:
```bash
pre-commit autoupdate
```

## Troubleshooting

### Hook fails with "command not found"

Make sure you're in the project root directory and that `./gradlew` is executable:
```bash
chmod +x gradlew
```

### Spotless formatting conflicts

If Spotless makes changes, stage them and commit again:
```bash
git add .
git commit -m "Your message"
```
