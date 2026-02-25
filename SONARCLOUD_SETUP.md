# SonarCloud Setup Guide

This guide explains how to configure SonarCloud for the Clean Architecture Generator Core project.

## Prerequisites

1. A SonarCloud account (sign up at https://sonarcloud.io)
2. Admin access to the GitHub repository
3. The project must be built and tests must pass

## Step 1: Create SonarCloud Project

1. Go to https://sonarcloud.io
2. Click on "+" → "Analyze new project"
3. Select your GitHub organization and repository
4. Follow the setup wizard

## Step 2: Get SonarCloud Credentials

After creating the project, you'll need:

1. **Organization Key**: Found in your SonarCloud organization settings
   - Go to: https://sonarcloud.io/organizations/[your-org]/projects
   - The organization key is in the URL

2. **Project Key**: Found in your project settings
   - Go to: Project Settings → General Settings
   - Look for "Project Key"

3. **Token**: Generate a new token
   - Go to: My Account → Security → Generate Tokens
   - Name: `GitHub Actions - [Project Name]`
   - Type: User Token
   - Expiration: No expiration (or set as needed)
   - Click "Generate"
   - **IMPORTANT**: Copy the token immediately (you won't see it again)

## Step 3: Configure GitHub Secrets

Add the following secrets to your GitHub repository:

1. Go to: Repository → Settings → Secrets and variables → Actions
2. Click "New repository secret" for each:

### Required Secrets

| Secret Name | Description | Example Value |
|-------------|-------------|---------------|
| `SONAR_TOKEN` | SonarCloud authentication token | `squ_1234567890abcdef...` |
| `SONAR_PROJECT_KEY` | SonarCloud project key | `com.pragma:archetype-generator-core` |
| `SONAR_ORGANIZATION` | SonarCloud organization key | `somospragma` |

### How to Add Secrets

```bash
# Navigate to your repository on GitHub
# Settings → Secrets and variables → Actions → New repository secret

Name: SONAR_TOKEN
Value: [paste your token here]

Name: SONAR_PROJECT_KEY
Value: com.pragma:archetype-generator-core

Name: SONAR_ORGANIZATION
Value: [your-organization-key]
```

## Step 4: Verify Configuration

### Local Testing (Optional)

You can test SonarCloud analysis locally:

```bash
# 1. Generate coverage report
./gradlew clean test jacocoTestReport

# 2. Run SonarCloud analysis
./gradlew sonar \
  -Dsonar.projectKey=com.pragma:archetype-generator-core \
  -Dsonar.organization=your-org \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=your-token
```

### GitHub Actions

The workflow will run automatically on:
- Push to `main` or `develop` branches
- Pull requests

Check the workflow status:
1. Go to: Repository → Actions
2. Look for "SonarCloud Analysis" workflow
3. Click on a run to see details

## Step 5: View Results

After the workflow completes:

1. Go to https://sonarcloud.io
2. Navigate to your project
3. View:
   - **Overview**: Quality Gate status, coverage, bugs, vulnerabilities
   - **Code**: Line-by-line analysis
   - **Coverage**: Detailed coverage report
   - **Issues**: All detected issues

## Coverage Report Location

The JaCoCo coverage report is generated at:
- **XML**: `build/reports/jacoco/test/jacocoTestReport.xml`
- **HTML**: `build/reports/jacoco/test/html/index.html`

## Troubleshooting

### Coverage Not Showing

**Problem**: SonarCloud shows "No data available" for coverage

**Solutions**:

1. **Verify JaCoCo report exists**:
   ```bash
   ./gradlew clean test jacocoTestReport
   ls -lh build/reports/jacoco/test/jacocoTestReport.xml
   ```

2. **Check report path in configuration**:
   - File: `sonar-project.properties`
   - Property: `sonar.coverage.jacoco.xmlReportPaths`
   - Should be: `build/reports/jacoco/test/jacocoTestReport.xml`

3. **Verify workflow runs tests before analysis**:
   ```yaml
   - name: Build and run tests with coverage
     run: ./gradlew clean build test jacocoTestReport
   ```

4. **Check SonarCloud logs**:
   - Go to GitHub Actions → SonarCloud Analysis workflow
   - Look for "SonarCloud Scan" step
   - Check for coverage-related messages

### Authentication Failed

**Problem**: `Error: Not authorized. Please check the properties sonar.token`

**Solutions**:

1. Verify `SONAR_TOKEN` secret is set correctly
2. Regenerate token in SonarCloud if expired
3. Check token has correct permissions

### Project Key Mismatch

**Problem**: `Project key not found`

**Solutions**:

1. Verify `SONAR_PROJECT_KEY` matches SonarCloud project key exactly
2. Check for typos or extra spaces
3. Ensure project exists in SonarCloud

## Configuration Files

### sonar-project.properties

Local configuration file for SonarCloud analysis:

```properties
sonar.projectKey=com.pragma:archetype-generator-core
sonar.projectName=Clean Architecture Generator Core
sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml
```

### build.gradle.kts

Gradle configuration for SonarQube plugin:

```kotlin
plugins {
    id("org.sonarqube") version "4.4.1.3373"
}

sonar {
    properties {
        property("sonar.projectKey", "com.pragma:archetype-generator-core")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}
```

### .github/workflows/sonarcloud.yml

GitHub Actions workflow for automated analysis:

```yaml
- name: SonarCloud Scan
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: |
    ./gradlew sonar \
      -Dsonar.projectKey=${{ secrets.SONAR_PROJECT_KEY }} \
      -Dsonar.organization=${{ secrets.SONAR_ORGANIZATION }}
```

## Quality Gate

Configure quality gate thresholds in SonarCloud:

1. Go to: Project Settings → Quality Gates
2. Set thresholds:
   - Coverage: ≥ 80%
   - Duplications: ≤ 3%
   - Maintainability Rating: A
   - Reliability Rating: A
   - Security Rating: A

## Best Practices

1. **Run tests locally** before pushing:
   ```bash
   ./gradlew clean test jacocoTestReport
   ```

2. **Check coverage locally**:
   ```bash
   open build/reports/jacoco/test/html/index.html
   ```

3. **Fix issues before merging**:
   - Review SonarCloud analysis results
   - Address critical and high-priority issues
   - Maintain coverage above threshold

4. **Monitor trends**:
   - Track coverage over time
   - Watch for increasing technical debt
   - Review new issues regularly

## Additional Resources

- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Gradle SonarQube Plugin](https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/)

## Support

If you encounter issues:

1. Check GitHub Actions logs
2. Review SonarCloud project logs
3. Consult this troubleshooting guide
4. Contact the development team
