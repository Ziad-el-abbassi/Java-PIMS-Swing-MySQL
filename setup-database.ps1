# Run this in PowerShell AS ADMINISTRATOR.
# Resets the MySQL root password to Pims@2026 and creates the PIMS database.
$ErrorActionPreference = 'Stop'
$bin  = 'C:\Program Files\MySQL\MySQL Server 8.0\bin'
$ini  = 'C:\ProgramData\MySQL\MySQL Server 8.0\my.ini'
$init = 'C:\mysql-init.txt'
$pass = 'Pims@2026'

Write-Host '1/5 Stopping MySQL service...'
Stop-Service MySQL80

Write-Host '2/5 Starting MySQL temporarily with password-reset file...'
Set-Content $init "ALTER USER 'root'@'localhost' IDENTIFIED BY '$pass';" -Encoding ascii
$proc = Start-Process "$bin\mysqld.exe" -ArgumentList "--defaults-file=`"$ini`"", "--init-file=$init" -PassThru -WindowStyle Hidden
Start-Sleep -Seconds 15

Write-Host '3/5 Shutting down temporary server...'
& "$bin\mysqladmin.exe" -u root "-p$pass" shutdown
$proc.WaitForExit(30000) | Out-Null
Remove-Item $init -Force

Write-Host '4/5 Starting MySQL service again...'
Start-Service MySQL80
Start-Sleep -Seconds 3

Write-Host '5/5 Creating database and tables...'
cmd /c "`"$bin\mysql.exe`" -u root -p$pass < `"$PSScriptRoot\schema.sql`""
& "$bin\mysql.exe" -u root "-p$pass" -e "SHOW TABLES FROM personal_inventory"

Write-Host "`nDone! Root password is now: $pass" -ForegroundColor Green
