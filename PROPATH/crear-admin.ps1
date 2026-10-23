
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

$antCmd = "C:\Program Files\NetBeans-25\netbeans\extide\ant\bin\ant.bat"
$buildXml = "D:\PROYECTO_PROPATH\PROPATH\build.xml"

try {
    $plainPass | & $antCmd -f $buildXml crear-admin -q
}
finally {
    $plainPass = $null
    [System.GC]::Collect()
}
