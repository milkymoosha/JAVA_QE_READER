#!/usr/bin/env pwsh

# Java Code Analyzer - Build and Run Script
# This script builds and executes the Java Code Analyzer application

Write-Host "=== Java Code Analyzer ===" -ForegroundColor Green
Write-Host "Building and running the application..." -ForegroundColor Yellow

# Check if Maven is available
try {
    $mavenVersion = mvn -version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
    Write-Host "Maven found: $($mavenVersion | Select-Object -First 1)" -ForegroundColor Green
} catch {
    Write-Host "Error: Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven and ensure it's in your system PATH" -ForegroundColor Red
    exit 1
}

# Check if Java is available
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
    Write-Host "Java found: $($javaVersion | Select-Object -First 1)" -ForegroundColor Green
} catch {
    Write-Host "Error: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 15 or higher and ensure it's in your system PATH" -ForegroundColor Red
    exit 1
}

# Clean and compile the project
Write-Host "`nCleaning and compiling..." -ForegroundColor Cyan
mvn clean compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Compilation failed" -ForegroundColor Red
    exit 1
}

# Run tests
Write-Host "`nRunning tests..." -ForegroundColor Cyan
mvn test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Warning: Some tests failed, but continuing..." -ForegroundColor Yellow
}

# Run the application
Write-Host "`nStarting Java Code Analyzer..." -ForegroundColor Cyan
Write-Host "The application window should open shortly..." -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the application" -ForegroundColor Yellow

# Run the JavaFX application
mvn javafx:run

Write-Host "`nApplication closed." -ForegroundColor Green 