# Phase 2 - Quick Test Commands

## Compile Phase 2
```powershell
mvnd clean compile -DskipTests
```
**Expected**: BUILD SUCCESS ✓

---

## Run Phase 2 Tests
```powershell
mvnd test
```
**Test Suite**: UserRepositoryTest (8 test cases)

---

## View All Java Files
```powershell
Get-ChildItem -Path src -Recurse -Filter "*.java" -Name | Sort-Object
```
**Count**: 13 files total

---

## Verify Compiled Classes
```powershell
(Get-ChildItem -Path target/classes/com/examsystem -Recurse -Filter "*.class" | Measure-Object).Count
```
**Expected**: 14+ class files

---

## Create Package
```powershell
mvnd package -DskipTests
```
**Output**: JAR file in target/

---

## View Project Structure
```powershell
tree src/main/java -L 3
```
**Or use Explorer to browse folders**

---

## Run Specific Test
```powershell
mvnd test -Dtest=UserRepositoryTest
```

---

## Full Build with Tests
```powershell
mvnd clean install
```
**Includes**: compile + test + package

---

## Quick Status Check

```powershell
cd "c:\Users\hp\My file\code\ExamSystem"

Write-Host "Phase 2 Status Check" -ForegroundColor Cyan
Write-Host ""

# Files
$java = (Get-ChildItem -Path src -Recurse -Filter "*.java").Count
$classes = (Get-ChildItem -Path target/classes/com/examsystem -Recurse -Filter "*.class").Count
Write-Host "Java Files: $java"
Write-Host "Compiled Classes: $classes"
Write-Host ""

# Compile
Write-Host "Building..." -ForegroundColor Yellow
mvnd clean compile -DskipTests | Select-String "BUILD"
```

---

## Files Created in Phase 2

### Interfaces
- UserRepository.java
- ExamRepository.java

### Implementations  
- UserRepositoryImpl.java
- UserService.java

### Models
- Exam.java
- Question.java
- Student.java
- User.java (enhanced)

### Tests
- UserRepositoryTest.java

---

## Next Steps

1. **Test Phase 2**:
   ```powershell
   mvnd clean compile
   ```

2. **Verify Structure**:
   ```powershell
   Get-ChildItem src -Recurse -Filter "*.java"
   ```

3. **Move to Phase 3**:
   - Create authentication system
   - Build login UI
   - Implement LoginController

---

**Phase 2 Complete!** ✓  
Ready for Phase 3 - Authentication System
