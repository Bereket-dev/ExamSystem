@echo off
REM Phase 1 Verification Script for ExamSystem
REM This script checks all Phase 1 components

echo.
echo ============================================
echo Phase 1 ExamSystem Verification Script
echo ============================================
echo.

cd /d "%~dp0"

REM Check Java files
echo Checking Java Source Files...
echo.
dir /s /b src\main\java\com\examsystem\*.java
echo.

REM Check Resource files
echo Checking Resource Files...
echo.
dir /s /b src\main\resources
echo.

REM Check Configuration files
echo Checking Configuration Files...
echo.
if exist pom.xml (
    echo [OK] pom.xml exists
) else (
    echo [FAIL] pom.xml missing
)

if exist .gitignore (
    echo [OK] .gitignore exists
) else (
    echo [FAIL] .gitignore missing
)

if exist PHASE_1_SETUP.md (
    echo [OK] PHASE_1_SETUP.md exists
) else (
    echo [FAIL] PHASE_1_SETUP.md missing
)

if exist PHASE_1_VERIFICATION.md (
    echo [OK] PHASE_1_VERIFICATION.md exists
) else (
    echo [FAIL] PHASE_1_VERIFICATION.md missing
)

if exist PHASE_1_VERIFICATION_REPORT.md (
    echo [OK] PHASE_1_VERIFICATION_REPORT.md exists
) else (
    echo [FAIL] PHASE_1_VERIFICATION_REPORT.md missing
)

echo.
echo ============================================
echo Running Maven Clean Compile...
echo ============================================
echo.

mvnd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Phase 1 verification complete!
    echo.
    echo All checks passed. Phase 1 is ready.
    echo Next step: Setup MySQL database
    echo.
) else (
    echo.
    echo [FAIL] Build failed. Check errors above.
    echo.
)

pause
