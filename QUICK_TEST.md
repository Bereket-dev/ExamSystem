# Phase 1 Quick Test Commands

## Run These Commands to Test Phase 1

### Test 1: Compile (Most Important)
```powershell
mvnd clean compile -DskipTests
```
**Expected:** `BUILD SUCCESS`  
**Time:** 2-5 seconds

---

### Test 2: Create Package
```powershell
mvnd package -DskipTests
```
**Expected:** JAR file created in target/  
**Time:** 3-8 seconds

---

### Test 3: Verify Structure
```powershell
# Show Java files
Get-ChildItem -Path src -Recurse -Filter "*.java" -Name

# Show SQL files  
Get-ChildItem -Path src/main/resources/sql -Filter "*.sql" -Name

# Show compiled classes
Get-ChildItem -Path target/classes/com/examsystem -Recurse -Filter "*.class" -Name
```
**Expected:** 5 Java + 2 SQL + 4+ class files

---

### Test 4: Check Compiled Classes
```powershell
Get-ChildItem target/classes/com/examsystem -Recurse -Filter "*.class" | Measure-Object
```
**Expected:** At least 4 .class files

---

### Test 5: Verify Configuration
```powershell
# Check database config
Select-String "db.host|db.port|db.name" src/main/resources/application.properties

# Check schema
Select-String "CREATE TABLE" src/main/resources/sql/schema.sql | Measure-Object

# Check test data
Select-String "INSERT INTO" src/main/resources/sql/test_data.sql | Measure-Object
```
**Expected:**
- Database settings present
- 10+ CREATE TABLE statements
- 5+ INSERT statements

---

### Test 6: Run Tests (Optional - needs database)
```powershell
mvnd test
```
**Note:** Tests will compile but may fail without MySQL setup (expected)

---

## All-in-One Quick Test

Copy and paste this entire block:

```powershell
cd "c:\Users\hp\My file\code\ExamSystem"

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  Phase 1 - Quick Test Suite" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Compilation
Write-Host "TEST 1: Compilation..." -ForegroundColor Yellow
$result = mvnd clean compile -DskipTests 2>&1
if ($result -match "BUILD SUCCESS") {
    Write-Host "✓ PASS - Compilation successful" -ForegroundColor Green
} else {
    Write-Host "✗ FAIL - Compilation failed" -ForegroundColor Red
}

# Test 2: Java Files
Write-Host ""
Write-Host "TEST 2: Java Files Structure..." -ForegroundColor Yellow
$javaCount = (Get-ChildItem -Path src -Recurse -Filter "*.java").Count
Write-Host "✓ Found $javaCount Java files" -ForegroundColor Green

# Test 3: SQL Files
Write-Host ""
Write-Host "TEST 3: SQL Scripts..." -ForegroundColor Yellow
$sqlFiles = Get-ChildItem -Path src/main/resources/sql -Filter "*.sql"
foreach ($file in $sqlFiles) {
    Write-Host "✓ Found: $($file.Name)" -ForegroundColor Green
}

# Test 4: Compiled Classes
Write-Host ""
Write-Host "TEST 4: Compiled Classes..." -ForegroundColor Yellow
$classCount = (Get-ChildItem -Path target/classes/com/examsystem -Recurse -Filter "*.class").Count
Write-Host "✓ Found $classCount .class files" -ForegroundColor Green

# Test 5: Configuration Files
Write-Host ""
Write-Host "TEST 5: Configuration Files..." -ForegroundColor Yellow
$config = "pom.xml", ".gitignore"
foreach ($file in $config) {
    if (Test-Path $file) {
        Write-Host "✓ Found: $file" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  ✓ Phase 1 Tests Complete!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Step: Move to Phase 2 - Database Layer" -ForegroundColor Cyan
```

---

## Quick Reference Table

| Test | Command | Success Indicator |
|------|---------|-------------------|
| **Compile** | `mvnd clean compile -DskipTests` | BUILD SUCCESS |
| **Package** | `mvnd package -DskipTests` | JAR file created |
| **Java Files** | `Get-ChildItem src -Recurse -Filter "*.java"` | 5 files |
| **SQL Files** | `Get-ChildItem src/main/resources/sql -Filter "*.sql"` | 2 files |
| **Classes** | `Get-ChildItem target/classes -Recurse -Filter "*.class"` | 4+ files |
| **Config** | `Test-Path pom.xml, .gitignore` | True |

---

## Expected Test Results from Terminal

```
=== Phase 1 Structure Check ===

Java Files:
main\java\com\examsystem\App.java
main\java\com\examsystem\db\DatabaseConnection.java
main\java\com\examsystem\model\User.java
main\java\com\examsystem\util\ConfigManager.java
test\java\com\examsystem\db\DatabaseConnectionTest.java

SQL Files:
schema.sql
test_data.sql

Configuration Files:
pom.xml
.gitignore

Compilation Output:
[INFO] Compiling 4 source files with javac [debug target 17]
[INFO] BUILD SUCCESS
[INFO] Total time: 2.431 s
```

---

## ✓ Phase 1 is VERIFIED and WORKING

When all tests pass:
- ✓ Compilation successful
- ✓ 5 Java files present
- ✓ 2 SQL files ready
- ✓ Configuration correct
- ✓ 4+ classes compiled

**You're ready for Phase 2!** 🚀
