
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   PROPATH - REGISTRO SEGURO DE ADMINISTRADOR     " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Nombre: Administrador PROPATH"
Write-Host "Correo: admin@propath.com"
Write-Host "Rol:    ADMIN"
Write-Host "Estado: ACTIVO"
Write-Host ""

$securePass = Read-Host -Prompt "Ingrese la contraseña para el Administrador" -AsSecureString
if ($null -eq $securePass -or $securePass.Length -eq 0) {
    Write-Host "❌ Error: La contraseña no puede estar vacía." -ForegroundColor Red
    exit 1
}

$bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePass)
$plainPass = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)

$mavenCmd = "C:\Program Files\NetBeans-25\netbeans\java\maven\bin\mvn.cmd"
$backendDir = "d:\PROYECTO_PROPATH\propath-backend"

Push-Location $backendDir
try {
    $plainPass | & $mavenCmd exec:java "-Dexec.mainClass=com.propath.util.CrearAdmin" -q
} finally {
    Pop-Location
    $plainPass = $null
    [System.GC]::Collect()
}
