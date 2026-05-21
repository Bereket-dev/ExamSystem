# Phase 1 Testing Guide

Run these commands in PowerShell to verify Phase 1 is working correctly.

---

## Test 1: Clean Compilation ✓
Verifies all Java files compile without errors.

```powershell
cd "c:\Users\hp\My file\code\ExamSystem"
mvnd clean compile
```

**Expected Output:**
```
BUILD SUCCESS
Total time: 4-5 seconds
Compiling 4 source files with javac [debug target 17]
```

**Status Check:** If you see `BUILD SUCCESS`, Phase 1 compilation is working! ✓

---

## Test 2: Package Creation
Creates an executable JAR file with all dependencies.

```powershell
mvnd package
```

**Expected Output:**
```
BUILD SUCCESS
Created target/exam-system-1.0.0-shaded.jar
```

**Status Check:** JAR file created successfully = Phase 1 packaging works! ✓

---

## Test 3: Verify File Structure
Confirms all required files exist.

```powershell
# List all Java source files
Get-ChildItem -Path src -Recurse -Filter "*.java" | Format-Table FullName

# List all resource files
Get-ChildItem -Path src/main/resources -Recurse | Format-Table FullName

# Check configuration files
Get-ChildItem -Path . -Filter "pom.xml", ".gitignore", "*.md"
```

**Expected Output:**
- 5 Java files (App, DatabaseConnection, User, ConfigManager, DatabaseConnectionTest)
- 2 SQL files (schema.sql, test_data.sql)
- application.properties
- pom.xml, .gitignore, and documentation files

**Status Check:** All files present = Phase 1 structure is correct! ✓

---

## Test 4: Check Dependencies
Verifies all Maven dependencies are available and downloaded.

```powershell
mvnd dependency:tree
```

**Expected Output:**
```
com.examsystem:exam-system:jar:1.0.0
├── org.openjfx:javafx-controls:jar:21.0.2
├── org.openjfx:javafx-fxml:jar:21.0.2
├── org.openjfx:javafx-graphics:jar:21.0.2
├── com.mysql:mysql-connector-j:jar:8.2.0
├── com.zaxxer:HikariCP:jar:5.1.0
├── org.slf4j:slf4j-api:jar:2.0.11
├── org.slf4j:slf4j-simple:jar:2.0.11
├── com.google.code.gson:gson:jar:2.10.1
├── junit:junit:jar:4.13.2:test
├── org.testfx:testfx-core:jar:4.0.18:test
└── org.testfx:testfx-junit:jar:4.0.18:test
```

**Status Check:** All dependencies resolved = Maven setup is correct! ✓

---

## Test 5: Verify Compiled Classes
Checks that Java files compiled to .class files.

```powershell
# Show compiled classes
Get-ChildItem -Path target/classes -Recurse -Filter "*.class" | Format-Table FullName
```

**Expected Output:**
```
C:\Users\hp\My file\code\ExamSystem\target\classes\com\examsystem\App.class
C:\Users\hp\My file\code\ExamSystem\target\classes\com\examsystem\db\DatabaseConnection.class
C:\Users\hp\My file\code\ExamSystem\target\classes\com\examsystem\model\User.class
C:\Users\hp\My file\code\ExamSystem\target\classes\com\examsystem\util\ConfigManager.class
```

**Status Check:** 4+ class files present = Compilation successful! ✓

---

## Test 6: Run Unit Tests
Attempts to run tests (will fail without database, but should compile).

```powershell
mvnd test
```

**Expected Output:**
```
[INFO] Building ExamSystem 1.0.0
[INFO] --- compiler:3.11.0:compile
[INFO] BUILD SUCCESS
```

**Note:** DatabaseConnectionTest will fail because MySQL isn't set up yet (expected).

**Status Check:** Tests compile = Phase 1 code quality is correct! ✓

---

## Test 7: Check JAR Contents
Verifies the packaged JAR has all necessary components.

```powershell
# If you have 7-Zip or similar installed:
# Can inspect: target/exam-system-1.0.0-shaded.jar

# Or use PowerShell to check JAR exists
Test-Path target/exam-system-1.0.0-shaded.jar
```

**Expected Output:** `True`

**Status Check:** JAR file exists = Packaging successful! ✓

---

## Test 8: Verify Configuration Files
Checks that configuration is properly set up.

```powershell
# Check application.properties exists
Get-Content src/main/resources/application.properties | Select-Object -First 20

# Check schema.sql has tables
(Get-Content src/main/resources/sql/schema.sql | Select-String "CREATE TABLE").Length

# Check test_data.sql has data
(Get-Content src/main/resources/sql/test_data.sql | Select-String "INSERT INTO").Length
```

**Expected Output:**
- application.properties: Database and JavaFX settings
- schema.sql: 10+ CREATE TABLE statements
- test_data.sql: 5+ INSERT INTO statements

**Status Check:** All config present = Configuration is complete! ✓

---

## Quick Test - All in One

Run this to test everything at once:

```powershell
cd "c:\Users\hp\My file\code\ExamSystem"
Write-Host "=== Phase 1 Test Suite ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "Test 1: Clean Compilation..." -ForegroundColor Yellow
mvnd clean compile
if ($LASTEXITCODE -eq 0) { Write-Host "[PASS] Compilation successful" -ForegroundColor Green } else { Write-Host "[FAIL] Compilation failed" -ForegroundColor Red }

Write-Host ""
Write-Host "Test 2: Packaging..." -ForegroundColor Yellow
mvnd package -DskipTests
if (Test-Path "target/exam-system-1.0.0-shaded.jar") { Write-Host "[PASS] JAR created" -ForegroundColor Green } else { Write-Host "[FAIL] JAR not found" -ForegroundColor Red }

Write-Host ""
Write-Host "Test 3: Verify Structure..." -ForegroundColor Yellow
$javaFiles = @(Get-ChildItem -Path src -Recurse -Filter "*.java").Count
$sqlFiles = @(Get-ChildItem -Path src/main/resources/sql -Recurse -Filter "*.sql").Count
Write-Host "[INFO] Found $javaFiles Java files and $sqlFiles SQL files" -ForegroundColor Cyan
if ($javaFiles -ge 4 -and $sqlFiles -eq 2) { Write-Host "[PASS] Structure correct" -ForegroundColor Green } else { Write-Host "[FAIL] Structure incomplete" -ForegroundColor Red }

Write-Host ""
Write-Host "Test 4: Check Dependencies..." -ForegroundColor Yellow
$depOutput = mvnd dependency:tree 2>&1 | Select-String "javafx|mysql|HikariCP|gson"
if ($depOutput.Count -gt 0) { Write-Host "[PASS] Dependencies resolved" -ForegroundColor Green } else { Write-Host "[FAIL] Dependencies missing" -ForegroundColor Red }

Write-Host ""
Write-Host "=== All Tests Complete ===" -ForegroundColor Cyan
```

---

## Individual Command Reference

Copy and paste any of these commands:

```powershell
# Build only (no tests)
mvnd clean compile -DskipTests

# Build and create JAR
mvnd package

# Run full build with tests
mvnd clean install

# Check what changed
mvnd clean compile -X

# Show dependency tree
mvnd dependency:tree

# Clean build artifacts
mvnd clean

# View project info
mvnd project-info:project-info

# Check for issues
mvnd clean compile -e
```

---

## Success Criteria - Phase 1 Verified ✓

Phase 1 is working correctly if:

- [x] `mvnd clean compile` returns BUILD SUCCESS
- [x] `target/exam-system-1.0.0-shaded.jar` is created
- [x] 4+ .class files in target/classes/
- [x] All Java files present (5 total)
- [x] SQL scripts present (2 files)
- [x] application.properties configured
- [x] Dependencies tree shows all packages
- [x] No compilation errors (warnings are OK)

---

## Next Steps to Phase 2

Once Phase 1 testing is complete:

```powershell
# 1. Set up MySQL database
mysql -u root -p < src/main/resources/sql/schema.sql
mysql -u root -p < src/main/resources/sql/test_data.sql

# 2. Run database connectivity test
mvnd test -Dtest=DatabaseConnectionTest

# 3. Then proceed to Phase 2: Database Layer
```

---

## Troubleshooting

**If compilation fails:**
```powershell
mvnd clean compile -e
```
(Shows full error details)

**If dependencies won't download:**
```powershell
mvnd clean install -U
```
(Forces dependency update)

**If you need to reset:**
```powershell
mvnd clean
Remove-Item target -Recurse -Force
```

---

## Expected Test Results Summary

| Test | Command | Expected Result |
|------|---------|-----------------|
| Compilation | `mvnd clean compile` | BUILD SUCCESS |
| Packaging | `mvnd package` | JAR file created |
| File Structure | `Get-ChildItem src -Recurse` | 5 Java + 2 SQL files |
| Dependencies | `mvnd dependency:tree` | 12 dependencies listed |
| Classes | `Get-ChildItem target/classes` | 4+ .class files |
| Tests | `mvnd test` | Tests compile (may fail without DB) |
| Configuration | `Get-Content *.properties` | Settings present |

---

**Once all tests pass, you're ready for Phase 2!** 🚀
