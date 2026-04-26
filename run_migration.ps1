Write-Host "Running admin_notifications migration..." -ForegroundColor Green
Write-Host "Please enter your MySQL root password when prompted." -ForegroundColor Yellow
Write-Host ""

try {
    Get-Content "admin_notifications_migration.sql" | mysql -u root -p cyclemart_db
    Write-Host ""
    Write-Host "Migration completed successfully!" -ForegroundColor Green
    Write-Host "The admin_notifications table has been created." -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "Migration failed. Please check your MySQL connection and database name." -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Read-Host "Press Enter to continue"