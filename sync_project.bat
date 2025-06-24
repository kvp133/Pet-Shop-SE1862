@echo off
echo Cleaning and rebuilding project...
echo.

echo Step 1: Clean project
call gradlew clean

echo.
echo Step 2: Clean Gradle cache
call gradlew --stop
if exist .gradle rmdir /s /q .gradle

echo.
echo Step 3: Build project
call gradlew assembleDebug

echo.
echo Step 4: Sync project
call gradlew --refresh-dependencies

echo.
echo Project sync completed!
echo Please restart your IDE and try running the app again.
pause 