param(
    [ValidateSet('Initialize','Start','Reset','Status')][string]$Action = 'Start',
    [string]$RootUser = 'root',
    [ValidateRange(1,65535)][int]$MySqlPort = 3306,
    [Security.SecureString]$RootPassword
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$settingsPath = Join-Path $projectRoot '.env.demo.local'
$mysqlExecutable = (Get-Command mysql -ErrorAction Stop).Source
$allowedKeys = @('DEMO_DB_URL','DEMO_DB_USERNAME','DEMO_DB_PASSWORD','DEMO_ADMIN_USERNAME','DEMO_ADMIN_PASSWORD','DEMO_PORT')
function Read-DemoSettings {
    $values = @{}
    if (Test-Path -LiteralPath $settingsPath) {
        foreach ($line in Get-Content -LiteralPath $settingsPath) {
            if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('#')) { continue }
            $parts = $line.Split('=',2)
            if ($parts.Count -ne 2 -or $parts[0] -notin $allowedKeys) { throw 'Configuracion demo no valida.' }
            $values[$parts[0]] = $parts[1]
        }
    }
    return $values
}
function Invoke-LocalMysql([string]$Login,[string]$Password,[string]$Sql,[string]$Database = '') {
    $previous = [Environment]::GetEnvironmentVariable('MYSQL_PWD','Process')
    try {
        [Environment]::SetEnvironmentVariable('MYSQL_PWD',$Password,'Process')
        $arguments = @("--user=$Login",'--host=localhost',"--port=$MySqlPort",'--connect-timeout=5','--batch','--skip-column-names','--default-character-set=utf8mb4')
        if ($Database) { $arguments += "--database=$Database" }
        $result = $Sql | & $mysqlExecutable @arguments 2>&1
        if ($LASTEXITCODE -ne 0) { throw 'MySQL no completo la operacion. Revisa el servicio, puerto, usuario y permisos; no se muestran credenciales.' }
        return ($result -join "`n").Trim()
    } finally { [Environment]::SetEnvironmentVariable('MYSQL_PWD',$previous,'Process') }
}
function New-DemoPassword { return 'DE!' + [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(18)) }
function Assert-DemoSettings($Settings) {
    foreach ($key in $allowedKeys) { if (!$Settings.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($Settings[$key])) { throw "Falta $key. Ejecuta Initialize." } }
    if ($Settings.DEMO_DB_USERNAME -ne 'deluxe_demo_local' -or $Settings.DEMO_DB_URL -notmatch '^jdbc:mysql://127\.0\.0\.1:(\d{1,5})/deluxe_eterno_demo\?') { throw 'La configuracion no apunta a la base demo autorizada.' }
    $script:MySqlPort = [int]$Matches[1]
    if ($Settings.DEMO_ADMIN_PASSWORD.Length -lt 12) { throw 'La clave administrativa demo debe tener al menos doce caracteres.' }
}
$settings = Read-DemoSettings
if ($Action -eq 'Initialize') {
    if (!$RootPassword) { $RootPassword = Read-Host 'Contrasena MySQL para crear el entorno demo' -AsSecureString }
    $rootSecret = [Management.Automation.PSCredential]::new($RootUser,$RootPassword).GetNetworkCredential().Password
    $tableCount = [int](Invoke-LocalMysql $RootUser $rootSecret "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='deluxe_eterno_demo';")
    if ($tableCount -gt 0) {
        $markerCount = [int](Invoke-LocalMysql $RootUser $rootSecret "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='deluxe_eterno_demo' AND table_name='demo_environment';")
        if ($markerCount -ne 1) { throw 'La base demo ya contiene tablas sin identificacion. No se sobrescribira.' }
        $identity = Invoke-LocalMysql $RootUser $rootSecret "SELECT app_key FROM demo_environment WHERE id=1;" 'deluxe_eterno_demo'
        if ($identity -ne 'deluxe-eterno-demo') { throw 'La base existente no pertenece a este entorno demo.' }
    }
    $userCount = [int](Invoke-LocalMysql $RootUser $rootSecret "SELECT COUNT(*) FROM mysql.user WHERE user='deluxe_demo_local' AND host='localhost';")
    if ($userCount -gt 0) {
        if ($settings.Count -eq 0) { throw 'El usuario deluxe_demo_local ya existe y faltan las credenciales locales. No se modifica automaticamente.' }
        $grants = Invoke-LocalMysql $RootUser $rootSecret "SHOW GRANTS FOR 'deluxe_demo_local'@'localhost';"
        foreach ($grant in $grants.Split("`n")) {
            if ($grant -notmatch '^GRANT USAGE ON \*\.\* TO ' -and $grant -notmatch '^GRANT .+ ON `deluxe_eterno_demo`\.\* TO ') { throw 'El usuario existente tiene permisos fuera del entorno demo. Operacion cancelada.' }
        }
    }
    if ($settings.Count -eq 0) {
        $settings = @{
            DEMO_DB_URL = "jdbc:mysql://127.0.0.1:$MySqlPort/deluxe_eterno_demo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
            DEMO_DB_USERNAME = 'deluxe_demo_local'
            DEMO_DB_PASSWORD = New-DemoPassword
            DEMO_ADMIN_USERNAME = 'admin-demo'
            DEMO_ADMIN_PASSWORD = New-DemoPassword
            DEMO_PORT = '8081'
        }
    }
    Assert-DemoSettings $settings
    if ($settings.DEMO_DB_PASSWORD -notmatch '^[A-Za-z0-9!_-]{12,100}$') { throw 'Formato de credencial demo no valido.' }
    $account = "'deluxe_demo_local'@'localhost'"
    $accountSql = if ($userCount -eq 0) { "CREATE USER $account IDENTIFIED BY '$($settings.DEMO_DB_PASSWORD)';" } else { "ALTER USER $account IDENTIFIED BY '$($settings.DEMO_DB_PASSWORD)';" }
    $sql = @"
CREATE DATABASE IF NOT EXISTS deluxe_eterno_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS deluxe_eterno_demo.demo_environment (id TINYINT PRIMARY KEY, app_key VARCHAR(80) NOT NULL, dataset_version VARCHAR(30) NULL, initialized_at TIMESTAMP NULL) ENGINE=InnoDB;
INSERT INTO deluxe_eterno_demo.demo_environment (id,app_key) VALUES (1,'deluxe-eterno-demo') ON DUPLICATE KEY UPDATE id=id;
$accountSql
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,INDEX,REFERENCES,DROP ON deluxe_eterno_demo.* TO $account;
"@
    $null = Invoke-LocalMysql $RootUser $rootSecret $sql
    $lines = @('# Credenciales locales del entorno demo. No incluir en Git.') + @($allowedKeys | ForEach-Object { "$_=$($settings[$_])" })
    [IO.File]::WriteAllLines($settingsPath,$lines,[Text.UTF8Encoding]::new($false))
    Write-Host 'Base demo y usuario limitado preparados. Credenciales guardadas en .env.demo.local (ignorado por Git).'
    Write-Host 'Ejecuta .\scripts\demo.ps1 -Action Start para crear el esquema y precargar los escenarios.'
    exit 0
}
Assert-DemoSettings $settings
if ($Action -eq 'Status') {
    $result = Invoke-LocalMysql $settings.DEMO_DB_USERNAME $settings.DEMO_DB_PASSWORD 'SELECT DATABASE(),app_key,dataset_version,initialized_at FROM demo_environment WHERE id=1;' 'deluxe_eterno_demo'
    Write-Host $result
    exit 0
}
if ($Action -eq 'Reset') {
    $client = [Net.Sockets.TcpClient]::new()
    try { $client.Connect('127.0.0.1',[int]$settings.DEMO_PORT); $running = $true } catch { $running = $false } finally { $client.Dispose() }
    if ($running) { throw 'Deten primero la instancia demo del puerto configurado; la tienda habitual puede continuar abierta.' }
    Write-Host 'Se restableceran los datos de deluxe_eterno_demo. La base habitual no se utiliza.'
}
$environmentKeys = $allowedKeys + @('SPRING_PROFILES_ACTIVE','SPRING_DATASOURCE_URL','SPRING_DATASOURCE_USERNAME','SPRING_DATASOURCE_PASSWORD','SPRING_JPA_HIBERNATE_DDL_AUTO')
$oldEnvironment = @{}
Push-Location $projectRoot
try {
    foreach ($key in $environmentKeys) { $oldEnvironment[$key] = [Environment]::GetEnvironmentVariable($key,'Process') }
    foreach ($key in $allowedKeys) { [Environment]::SetEnvironmentVariable($key,$settings[$key],'Process') }
    $env:SPRING_PROFILES_ACTIVE = 'demo'
    $env:SPRING_DATASOURCE_URL = $settings.DEMO_DB_URL
    $env:SPRING_DATASOURCE_USERNAME = $settings.DEMO_DB_USERNAME
    $env:SPRING_DATASOURCE_PASSWORD = $settings.DEMO_DB_PASSWORD
    $env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'update'
    if ($Action -eq 'Reset') {
        & mvn spring-boot:run '-Dspring-boot.run.profiles=demo' '-Dspring-boot.run.arguments=--app.demo.reset-on-start=true --app.demo.exit-after-initialize=true --server.port=0'
    } else {
        & mvn spring-boot:run '-Dspring-boot.run.profiles=demo'
    }
    if ($LASTEXITCODE -ne 0) { throw 'Spring Boot no completo la operacion demo. Revisa el mensaje anterior.' }
} finally {
    foreach ($key in $environmentKeys) { [Environment]::SetEnvironmentVariable($key,$oldEnvironment[$key],'Process') }
    Pop-Location
}
