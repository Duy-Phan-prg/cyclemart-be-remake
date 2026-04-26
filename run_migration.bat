@echo off
echo Running admin_notifications migration...
echo Please enter your MySQL root password when prompted.
echo.

mysql -u root -p cyclemart_db < admin_notifications_migration.sql

if %errorlevel% equ 0 (
    echo.
    echo Migration completed successfully!
    echo The admin_notifications table has been created.
) else (
    echo.
    echo Migration failed. Please check your MySQL connection and database name.
)

pause